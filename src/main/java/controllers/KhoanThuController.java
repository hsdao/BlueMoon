package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.KhoanThu;
import services.KhoanThuDAO;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class KhoanThuController implements Initializable {

    @FXML private TableView<KhoanThu> tblKhoanThu;
    @FXML private TextField txtTimKiem;
    @FXML private Button btnThemMoi;
    @FXML private Button btnSua;
    @FXML private Button btnXoa;

    private ObservableList<KhoanThu> khoanThuList;
    private final KhoanThuDAO dao = new KhoanThuDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        khoanThuList = FXCollections.observableArrayList(dao.getAllKhoanThu());
        tblKhoanThu.setItems(khoanThuList);
        setupSearch(); // Đăng ký listener 1 lần duy nhất
    }

    // Tải dữ liệu thật từ DB, giữ nguyên FilteredList đã được bind
    public void loadDataFromDB() {
        khoanThuList.setAll(dao.getAllKhoanThu());
    }

    private void setupSearch() {
        if (khoanThuList == null) return;
        FilteredList<KhoanThu> filtered = new FilteredList<>(khoanThuList, p -> true);
        txtTimKiem.textProperty().addListener((obs, oldVal, newVal) -> {
            filtered.setPredicate(kt -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String lower = newVal.toLowerCase();
                if (kt.getMaKhoan() != null && kt.getMaKhoan().toLowerCase().contains(lower)) return true;
                if (kt.getTenKhoan() != null && kt.getTenKhoan().toLowerCase().contains(lower)) return true;
                return false;
            });
        });
        tblKhoanThu.setItems(filtered);
    }

    @FXML
    public void onThemMoiClick(ActionEvent event) {
        showFormKhoanThu(null);
    }

    @FXML
    public void onSuaClick(ActionEvent event) {
        KhoanThu selected = tblKhoanThu.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn một khoản thu để sửa!");
            return;
        }
        showFormKhoanThu(selected);
    }

    @FXML
    public void onXoaClick(ActionEvent event) {
        KhoanThu selected = tblKhoanThu.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn khoản thu để xóa!");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText(null);
        confirm.setContentText("Bạn có chắc muốn xóa khoản thu \"" + selected.getTenKhoan() + "\"?\nHành động này không thể hoàn tác.");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (dao.xoaKhoanThu(selected.getId())) {
                loadDataFromDB();
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xóa khoản thu thành công!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa do có dữ liệu ràng buộc (đã có lịch sử nộp tiền).");
            }
        }
    }

    private void showFormKhoanThu(KhoanThu khoanThuToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/FormKhoanThu.fxml"));
            Parent root = loader.load();

            FormKhoanThuController controller = loader.getController();
            controller.setParentController(this);
            controller.setKhoanThuData(khoanThuToEdit);

            Stage stage = new Stage();
            stage.setTitle(khoanThuToEdit == null ? "Thêm Khoản Thu Mới" : "Sửa Thông Tin Khoản Thu");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở màn hình form: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

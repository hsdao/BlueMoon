package controllers;

import models.KhoanThu;
import services.KhoanThuDAO;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller cho màn hình Danh sách Khoản Thu (KhoanThu.fxml).
 */
public class KhoanThuController implements Initializable {

    @FXML private TextField txtTimKiem;
    @FXML private TableView<KhoanThu> tblKhoanThu;
    @FXML private Button btnThemMoi;
    @FXML private Button btnSua;
    @FXML private Button btnXoa;

    private final ObservableList<KhoanThu> khoanThuList = FXCollections.observableArrayList();
    private FilteredList<KhoanThu> filteredData;
    private final KhoanThuDAO dao = new KhoanThuDAO();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // FIX: Tạo FilteredList một lần duy nhất, không tạo lại khi reload
        filteredData = new FilteredList<>(khoanThuList, p -> true);
        tblKhoanThu.setItems(filteredData);
        setupSearch();
        loadDataFromDB();
    }

    // =====================================================================
    //  DỮ LIỆU
    // =====================================================================

    /**
     * FIX: Dùng setAll() thay vì tạo ObservableList mới — FilteredList tự re-evaluate,
     * không tạo thêm listener trùng lặp.
     */
    public void loadDataFromDB() {
        List<KhoanThu> dbList = dao.getAllKhoanThu();
        khoanThuList.setAll(dbList);
    }

    // =====================================================================
    //  TÌM KIẾM
    // =====================================================================

    /**
     * Gắn listener một lần duy nhất trong initialize().
     */
    private void setupSearch() {
        txtTimKiem.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(kt -> {
                if (newValue == null || newValue.isBlank()) return true;
                String keyword = newValue.toLowerCase().trim();
                if (kt.getMaKhoan() != null && kt.getMaKhoan().toLowerCase().contains(keyword)) return true;
                if (kt.getTenKhoan() != null && kt.getTenKhoan().toLowerCase().contains(keyword)) return true;
                return false;
            });
        });
    }

    // =====================================================================
    //  XỬ LÝ SỰ KIỆN BUTTON
    // =====================================================================

    @FXML
    private void onThemMoiClick() {
        openFormDialog(null);
    }

    @FXML
    private void onSuaClick() {
        KhoanThu selected = tblKhoanThu.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Chưa chọn khoản thu", "Vui lòng chọn một khoản thu trong bảng để sửa.");
            return;
        }
        openFormDialog(selected);
    }

    @FXML
    private void onXoaClick() {
        KhoanThu selected = tblKhoanThu.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Chưa chọn khoản thu", "Vui lòng chọn một khoản thu trong bảng để xóa.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText(null);
        confirm.setContentText(
                "Bạn có chắc muốn xóa khoản thu \"" + selected.getTenKhoan() + "\" không?\n"
                + "Hành động này không thể hoàn tác.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (dao.xoaKhoanThu(selected.getId())) {
                loadDataFromDB();
                showInfo("Thành công", "Đã xóa khoản thu \"" + selected.getTenKhoan() + "\".");
            } else {
                showError("Lỗi", "Không thể xóa. Khoản thu này có thể đã có người nộp tiền.");
            }
        }
    }

    // =====================================================================
    //  MỞ FORM THÊM / SỬA
    // =====================================================================

    private void openFormDialog(KhoanThu khoanThu) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/FormKhoanThu.fxml"));
            Parent root = loader.load();

            FormKhoanThuController formController = loader.getController();
            formController.setParentController(this);
            if (khoanThu != null) formController.setEditData(khoanThu);
            else                   formController.setAddMode();

            Stage stage = new Stage();
            stage.setTitle(khoanThu == null ? "Thêm Khoản Thu Mới" : "Sửa Khoản Thu");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi hệ thống", "Không thể mở form khoản thu:\n" + e.getMessage());
        }
    }

    // =====================================================================
    //  HELPER
    // =====================================================================

    private void showWarning(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(content);
        alert.showAndWait();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(content);
        alert.showAndWait();
    }
}

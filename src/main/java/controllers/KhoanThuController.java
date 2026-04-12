package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.KhoanThu;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.util.ResourceBundle;

public class KhoanThuController implements Initializable {

    @FXML
    private TableView<KhoanThu> tblKhoanThu;
    @FXML
    private TextField txtTimKiem;
    @FXML
    private Button btnThemMoi;
    @FXML
    private Button btnSua;
    @FXML
    private Button btnXoa;

    private ObservableList<KhoanThu> khoanThuList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Init Mock Data
        khoanThuList = FXCollections.observableArrayList();
        
        KhoanThu mock1 = new KhoanThu();
        mock1.setId(1);
        mock1.setMaKhoan("KT001");
        mock1.setTenKhoan("Phí vệ sinh môi trường");
        mock1.setLoai("Bắt buộc");
        mock1.setSoTien(50000.0);
        mock1.setThangThu(Date.valueOf("2024-05-01"));
        mock1.setHanNop(Date.valueOf("2024-05-31"));
        mock1.setTrangThai("Đang Thu");

        KhoanThu mock2 = new KhoanThu();
        mock2.setId(2);
        mock2.setMaKhoan("KT002");
        mock2.setTenKhoan("Quỹ đền ơn đáp nghĩa");
        mock2.setLoai("Tự nguyện");
        mock2.setSoTien(null);
        mock2.setTrangThai("Sắp Thu");

        khoanThuList.addAll(mock1, mock2);

        // Bind data to TableView
        tblKhoanThu.setItems(khoanThuList);
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
        // Giả lập xóa offline
        khoanThuList.remove(selected);
        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xóa khoản thu thành công!");
    }

    private void showFormKhoanThu(KhoanThu khoanThuToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/FormKhoanThu.fxml"));
            Parent root = loader.load();

            FormKhoanThuController controller = loader.getController();
            controller.setKhoanThuData(khoanThuToEdit);

            Stage stage = new Stage();
            stage.setTitle(khoanThuToEdit == null ? "Thêm Khoản Thu Mới" : "Sửa Thông Tin Khoản Thu");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL); // Modal window
            stage.setResizable(false);
            
            // Xử lý nạp lại dữ liệu nếu có lưu thay đổi từ dialog (Simulator)
            stage.showAndWait();
            
            // Re-render
            tblKhoanThu.refresh();
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

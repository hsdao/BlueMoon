package controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.KhoanThu;
import services.KhoanThuService;

import java.net.URL;
import java.sql.Date;
import java.util.ResourceBundle;

public class FormKhoanThuController implements Initializable {

    @FXML
    private TextField txtMaKhoanThu;
    @FXML
    private TextField txtTenKhoanThu;
    @FXML
    private ComboBox<String> cmbLoaiKhoanThu;
    @FXML
    private TextField txtSoTien;
    @FXML
    private DatePicker dtpHanNop;
    @FXML
    private Button btnLuu;
    @FXML
    private Button btnHuy;

    private KhoanThuService khoanThuService;
    private KhoanThu editingKhoanThu;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        khoanThuService = new KhoanThuService();
        cmbLoaiKhoanThu.setItems(FXCollections.observableArrayList("Bắt buộc", "Tự nguyện"));
    }

    public void setKhoanThuData(KhoanThu khoanThuToEdit) {
        this.editingKhoanThu = khoanThuToEdit;
        if (khoanThuToEdit != null) {
            txtMaKhoanThu.setText(khoanThuToEdit.getMaKhoan());
            txtMaKhoanThu.setDisable(true); // Không cho sửa mã vì là Primary/Unique key mock
            txtTenKhoanThu.setText(khoanThuToEdit.getTenKhoan());
            cmbLoaiKhoanThu.setValue(khoanThuToEdit.getLoai());
            
            if (khoanThuToEdit.getSoTien() != null) {
                txtSoTien.setText(String.valueOf(khoanThuToEdit.getSoTien()));
            }
            if (khoanThuToEdit.getHanNop() != null) {
                dtpHanNop.setValue(khoanThuToEdit.getHanNop().toLocalDate());
            }
        }
    }

    @FXML
    public void onLuuClick(ActionEvent event) {
        String maKhoan = txtMaKhoanThu.getText();
        String tenKhoan = txtTenKhoanThu.getText();
        String loai = cmbLoaiKhoanThu.getValue();
        String soTienStr = txtSoTien.getText();

        String errorMessage = khoanThuService.validateKhoanThu(maKhoan, tenKhoan, loai, soTienStr);
        if (errorMessage != null) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Nhập Liệu", errorMessage);
            return;
        }

        // Logic giả lập lưu xuống DB
        if (editingKhoanThu != null) {
            editingKhoanThu.setTenKhoan(tenKhoan);
            editingKhoanThu.setLoai(loai);
            if (!soTienStr.isEmpty()) {
                editingKhoanThu.setSoTien(Double.parseDouble(soTienStr));
            } else {
                editingKhoanThu.setSoTien(null);
            }
            if (dtpHanNop.getValue() != null) {
                editingKhoanThu.setHanNop(Date.valueOf(dtpHanNop.getValue()));
            }
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Cập nhật khoản thu thành công!");
        } else {
            // Khi làm thật sẽ gọi DAO Insert mới
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã tạo khoản thu " + tenKhoan + " thành công!");
        }

        closeWindow();
    }

    @FXML
    public void onHuyClick(ActionEvent event) {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) btnHuy.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

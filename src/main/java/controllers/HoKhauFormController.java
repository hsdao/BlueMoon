package controllers;

import models.HoKhau;
import services.HoKhauService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.time.LocalDateTime;

import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class HoKhauFormController implements Initializable {
    @FXML private Label lblTitle;
    @FXML private TextField txtMaHo;
    @FXML private TextField txtSdt;
    @FXML private TextField txtDiaChi;
    @FXML private DatePicker dpNgayTao;
    @FXML private ComboBox<String> cbTrangThai;
    @FXML private TextArea txtGhiChu; // Trường mới

    private HoKhau hoKhauCurent;
    private final HoKhauService service = new HoKhauService();
    private boolean isEditMode = false;
    private HoKhauController parentController;

    // Khởi tạo form
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cbTrangThai.getItems().addAll("ACTIVE", "INACTIVE");
        cbTrangThai.setValue("ACTIVE");
        dpNgayTao.setValue(LocalDate.now());
        dpNgayTao.setDisable(true);
    }

    // Gán controller cha để reload dữ liệu sau khi lưu
    public void setParentController(HoKhauController parentController) {
        this.parentController = parentController;
    }

    // Chế độ thêm hộ khẩu
    public void setAddMode() {
        this.isEditMode = false;
    }

    // Chế độ sửa: hiển thị dữ liệu cũ vào form
    public void setEditData(HoKhau hk) {
        this.hoKhauCurent = hk;
        this.isEditMode = true;
        lblTitle.setText("Sửa Hộ Khẩu");

        txtMaHo.setText(hk.getMaHo());
        txtSdt.setText(hk.getSoDienThoaiChuHo());
        txtDiaChi.setText(hk.getDiaChi());
        cbTrangThai.setValue(hk.getTrangThai());
        txtGhiChu.setText(hk.getGhiChu());

        if (hk.getNgayTao() != null) {
            dpNgayTao.setValue(hk.getNgayTao().toLocalDateTime().toLocalDate());
        }
    }

    // Xác thực & lưu hộ khẩu
    @FXML
    private void handleSave() {
        if (txtSdt.getText().trim().isEmpty() || txtDiaChi.getText().trim().isEmpty()) {
            showAlert("Lỗi nhập liệu", "Vui lòng nhập Địa chỉ và Số điện thoại.");
            return;
        }

        String sdt = txtSdt.getText().trim();
        if (!sdt.matches("^\\+?\\d{9,14}$")) {
            showAlert("Lỗi nhập liệu", "Số điện thoại không hợp lệ!\nChỉ được chứa số (hoặc dấu + ở đầu).");
            return;
        }

        HoKhau hk = isEditMode ? hoKhauCurent : new HoKhau();
        hk.setMaHo(txtMaHo.getText().trim());
        hk.setSoDienThoaiChuHo(sdt);
        hk.setDiaChi(txtDiaChi.getText().trim());
        hk.setTrangThai(cbTrangThai.getValue());
        hk.setGhiChu(txtGhiChu.getText().trim());

        boolean success;
        if (!isEditMode) {
            hk.setNgayTao(Timestamp.valueOf(LocalDateTime.now()));
            hk.setSoThanhVien(0);
            success = service.addHoKhau(hk);
        } else {
            success = service.updateHoKhau(hk);
        }

        if (success) {
            parentController.loadDataFromDB();
            closeWindow();
        } else {
            showAlert("Lỗi Database", "Không thể lưu dữ liệu. Có thể SĐT đã bị trùng!");
        }
    }

    // Hủy bỏ: đóng cửa sổ form
    @FXML
    private void handleCancel() { closeWindow(); }

    private void closeWindow() {
        Stage stage = (Stage) txtMaHo.getScene().getWindow();
        stage.close();
    }

    // Hiển thị thông báo lỗi
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
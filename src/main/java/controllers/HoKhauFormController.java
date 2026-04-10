package controllers;

import models.HoKhau;
import services.HoKhauDAO;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class HoKhauFormController implements Initializable {
    @FXML private Label lblTitle;
    @FXML private TextField txtMaHo;
    @FXML private TextField txtChuHoId;
    @FXML private TextField txtSdt;
    @FXML private TextField txtDiaChi;
    @FXML private TextField txtSoThanhVien;
    @FXML private DatePicker dpNgayTao;
    @FXML private ComboBox<String> cbTrangThai;
    @FXML private TextArea txtGhiChu; // Trường mới

    private HoKhau hoKhauCurent;
    private final HoKhauDAO dao = new HoKhauDAO();
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
        txtChuHoId.setText(hk.getChuHoId() == null ? "" : String.valueOf(hk.getChuHoId()));
        txtSdt.setText(hk.getSoDienThoaiChuHo());
        txtDiaChi.setText(hk.getDiaChi());
        txtSoThanhVien.setText(String.valueOf(hk.getSoThanhVien()));
        cbTrangThai.setValue(hk.getTrangThai());
        txtGhiChu.setText(hk.getGhiChu());

        if (hk.getNgayTao() != null) {
            dpNgayTao.setValue(hk.getNgayTao().toLocalDateTime().toLocalDate());
        }
    }

    // Xác thực & lưu hộ khẩu
    @FXML
    private void handleSave() {
        // 1. Kiểm tra rỗng
        if (txtSdt.getText().trim().isEmpty() || txtDiaChi.getText().trim().isEmpty() || txtSoThanhVien.getText().trim().isEmpty()) {
            showAlert("Lỗi nhập liệu", "Vui lòng nhập đầy đủ các trường bắt buộc (*).");
            return;
        }
        HoKhau hk = isEditMode ? hoKhauCurent : new HoKhau();

        try {
            String chuHoIdStr = txtChuHoId.getText().trim();
            hk.setChuHoId(chuHoIdStr.isEmpty() ? null : Integer.parseInt(chuHoIdStr));
        } catch (NumberFormatException e) {
            showAlert("Lỗi", "ID Chủ hộ phải là số!");
            return;
        }

        hk.setMaHo(txtMaHo.getText().trim());
        hk.setSoDienThoaiChuHo(txtSdt.getText().trim());
        hk.setDiaChi(txtDiaChi.getText().trim());
        hk.setTrangThai(cbTrangThai.getValue());

        String ghiChu = txtGhiChu.getText() == null ? "" : txtGhiChu.getText().trim();
        hk.setGhiChu(ghiChu);

        try {
            hk.setSoThanhVien(Integer.parseInt(txtSoThanhVien.getText().trim()));
        } catch (NumberFormatException e) {
            hk.setSoThanhVien(0);
        }

        if (!isEditMode) {
            hk.setNgayTao(new java.sql.Timestamp(System.currentTimeMillis()));
            boolean success = dao.themHoKhau(hk);
            if (success) {
                parentController.loadDataFromDB();
                closeWindow();
            } else {
                showAlert("Lỗi Database", "Không thể thêm mới. Mã hộ hoặc SĐT đã tồn tại!");
            }
        } else {
            dao.capNhatHoKhau(hk);

            parentController.loadDataFromDB();
            closeWindow();
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
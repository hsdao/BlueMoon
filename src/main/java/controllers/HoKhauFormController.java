package controllers;

import models.HoKhau;
import services.HoKhauDAO;
import services.HoKhauService;

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
    private final HoKhauService service = new HoKhauService();
    private boolean isEditMode = false;
    private HoKhauController parentController;

    // Khởi tạo form
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cbTrangThai.getItems().addAll("ACTIVE", "INACTIVE");
        cbTrangThai.setValue("ACTIVE");
        dpNgayTao.setValue(LocalDate.now());
    }

    // Gán controller cha để reload dữ liệu sau khi lưu
    public void setParentController(HoKhauController parentController) {
        this.parentController = parentController;
    }

    // Chế độ thêm hộ khẩu
    public void setAddMode() {
        isEditMode = false;
        lblTitle.setText("Thêm Hộ Khẩu Mới");
        // Khoá DatePicker — ngày tạo tự sinh khi thêm mới
        dpNgayTao.setDisable(true);
    }

    // Chế độ sửa: hiển thị dữ liệu cũ vào form
    public void setEditData(HoKhau hk) {
        hoKhauCurent = hk;
        isEditMode   = true;
        lblTitle.setText("Sửa Hộ Khẩu");
        dpNgayTao.setDisable(false); // Cho phép sửa ngày tạo khi edit

        txtMaHo.setText(hk.getMaHo());
        txtChuHoId.setText(hk.getChuHoId() == null ? "" : String.valueOf(hk.getChuHoId()));
        txtSdt.setText(hk.getSoDienThoaiChuHo());
        txtDiaChi.setText(hk.getDiaChi());
        txtSoThanhVien.setText(String.valueOf(hk.getSoThanhVien()));
        cbTrangThai.setValue(hk.getTrangThai());
        txtGhiChu.setText(hk.getGhiChu() != null ? hk.getGhiChu() : "");

        if (hk.getNgayTao() != null) {
            dpNgayTao.setValue(hk.getNgayTao().toLocalDateTime().toLocalDate());
        }
    }

    // Xác thực & lưu hộ khẩu
    @FXML
    private void handleSave() {
        String maHo          = txtMaHo.getText().trim();
        String sdt           = txtSdt.getText().trim();
        String diaChi        = txtDiaChi.getText().trim();
        String soThanhVienStr = txtSoThanhVien.getText().trim();

        // 1. Validate qua Service
        String err = service.validateHoKhau(maHo, sdt, diaChi, soThanhVienStr);
        if (err != null && !err.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi nhập liệu", err);
            return;
        }

        // 2. Parse ID chủ hộ (tuỳ chọn)
        Integer chuHoId = null;
        String chuHoIdStr = txtChuHoId.getText().trim();
        if (!chuHoIdStr.isEmpty()) {
            try {
                chuHoId = Integer.parseInt(chuHoIdStr);
                if (chuHoId <= 0) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi nhập liệu",
                            "ID Chủ hộ phải là số nguyên dương!");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi nhập liệu", "ID Chủ hộ phải là số!");
                return;
            }
        }

        // 3. Build đối tượng
        HoKhau hk = isEditMode ? hoKhauCurent : new HoKhau();
        hk.setMaHo(maHo);
        hk.setChuHoId(chuHoId);
        hk.setSoDienThoaiChuHo(sdt);
        hk.setDiaChi(diaChi);
        hk.setSoThanhVien(Integer.parseInt(soThanhVienStr));
        hk.setTrangThai(cbTrangThai.getValue());
        hk.setGhiChu(txtGhiChu.getText() != null ? txtGhiChu.getText().trim() : "");

        if (!isEditMode) {
            // Ngày tạo = thời điểm hiện tại khi thêm mới
            hk.setNgayTao(new java.sql.Timestamp(System.currentTimeMillis()));
        }
        // Khi sửa: giữ nguyên ngayTao đã có trong hoKhauCurent (không đổi)

        // 4. Lưu DB
        boolean success = isEditMode ? dao.capNhatHoKhau(hk) : dao.themHoKhau(hk);

        if (success) {
            if (parentController != null) parentController.loadDataFromDB();
            showAlert(Alert.AlertType.INFORMATION, "Thành công",
                    isEditMode ? "Cập nhật hộ khẩu thành công!" : "Thêm hộ khẩu mới thành công!");
            closeWindow();
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi Database",
                    isEditMode
                            ? "Không thể cập nhật. Mã hộ hoặc SĐT có thể đã tồn tại!"
                            : "Không thể thêm mới. Mã hộ hoặc SĐT đã tồn tại!");
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
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
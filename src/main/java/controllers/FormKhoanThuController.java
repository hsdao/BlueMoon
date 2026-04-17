package controllers;

import models.KhoanThu;
import services.KhoanThuDAO;
import services.KhoanThuService;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ResourceBundle;

/**
 * Controller cho cửa sổ Form Thêm / Sửa Khoản Thu (FormKhoanThu.fxml).
 * Nhiệm vụ (P3):
 *  - Điền sẵn dữ liệu khi ở chế độ Sửa
 *  - Xác thực (validate) đầu vào qua KhoanThuService trước khi lưu
 *  - Gọi KhoanThuDAO để INSERT (thêm) hoặc UPDATE (sửa) vào database
 *  - Thông báo kết quả và đóng cửa sổ form sau khi lưu thành công
 */
public class FormKhoanThuController implements Initializable {

    // --- Map fx:id từ FormKhoanThu.fxml ---
    @FXML private TextField   txtMaKhoanThu;
    @FXML private TextField   txtTenKhoanThu;
    @FXML private ComboBox<String> cmbLoaiKhoanThu;
    @FXML private TextField   txtSoTien;
    @FXML private DatePicker  dtpHanNop;
    @FXML private Button      btnLuu;
    @FXML private Button      btnHuy;

    // --- Dữ liệu & Service ---
    private KhoanThu khoanThuCurrent;          // null → chế độ thêm mới
    private boolean  isEditMode = false;
    private KhoanThuController parentController; // để gọi lại loadDataFromDB()

    private final KhoanThuDAO     dao     = new KhoanThuDAO();
    private final KhoanThuService service = new KhoanThuService();

    /**
     * Khởi tạo form: nạp các lựa chọn Loại khoản thu vào ComboBox.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cmbLoaiKhoanThu.getItems().addAll("Bắt buộc", "Tự nguyện");
        // Mặc định chọn "Bắt buộc"
        cmbLoaiKhoanThu.setValue("Bắt buộc");
    }

    // =====================================================================
    //  THIẾT LẬP CHẾ ĐỘ
    // =====================================================================

    /**
     * Truyền controller cha để form có thể gọi loadDataFromDB() sau khi lưu.
     */
    public void setParentController(KhoanThuController parent) {
        this.parentController = parent;
    }

    /**
     * Chế độ Thêm mới: giữ nguyên form trống.
     */
    public void setAddMode() {
        this.isEditMode = false;
        this.khoanThuCurrent = null;
    }

    /**
     * Chế độ Sửa: điền dữ liệu của khoản thu vào các trường form.
     *
     * @param kt khoản thu cần sửa
     */
    public void setEditData(KhoanThu kt) {
        this.isEditMode = true;
        this.khoanThuCurrent = kt;

        txtMaKhoanThu.setText(kt.getMaKhoan());
        txtTenKhoanThu.setText(kt.getTenKhoan());
        cmbLoaiKhoanThu.setValue(kt.getLoai());
        txtSoTien.setText(kt.getSoTien() != null ? String.valueOf(kt.getSoTien()) : "");

        if (kt.getHanNop() != null) {
            dtpHanNop.setValue(kt.getHanNop().toLocalDate());
        }
    }

    // =====================================================================
    //  XỬ LÝ SỰ KIỆN BUTTON
    // =====================================================================

    /**
     * Nút "Lưu Thông Tin":
     *  1. Lấy dữ liệu từ form
     *  2. Validate qua KhoanThuService
     *  3. Nếu hợp lệ → lưu DB → reload danh sách → đóng form
     */
    @FXML
    private void onLuuClick() {
        // 1. Thu thập dữ liệu từ form
        String maKhoan  = txtMaKhoanThu.getText().trim();
        String tenKhoan = txtTenKhoanThu.getText().trim();
        String loai     = cmbLoaiKhoanThu.getValue();
        String soTienStr = txtSoTien.getText().trim();

        // 2. Validate
        String errorMsg = service.validateKhoanThu(maKhoan, tenKhoan, loai, soTienStr);
        if (errorMsg != null && !errorMsg.isEmpty()) {
            showError("Lỗi nhập liệu", errorMsg);
            return;
        }

        // 3. Tạo / cập nhật đối tượng KhoanThu
        KhoanThu kt = isEditMode ? khoanThuCurrent : new KhoanThu();
        kt.setMaKhoan(maKhoan);
        kt.setTenKhoan(tenKhoan);
        kt.setLoai(loai);

        // Số tiền: có thể để trống (null) nếu là tự nguyện
        if (!soTienStr.isEmpty()) {
            kt.setSoTien(Double.parseDouble(soTienStr));
        } else {
            kt.setSoTien(null);
        }

        // Hạn nộp: có thể để trống
        LocalDate hanNopLocal = dtpHanNop.getValue();
        kt.setHanNop(hanNopLocal != null ? Date.valueOf(hanNopLocal) : null);

        // Trạng thái mặc định khi tạo mới
        if (!isEditMode) {
            kt.setTrangThai("Chưa thu");
        }

        // 4. Lưu vào DB
        boolean success;
        if (isEditMode) {
            success = dao.capNhatKhoanThu(kt);
        } else {
            success = dao.themKhoanThu(kt);
        }

        if (success) {
            // Reload danh sách bên màn hình cha
            if (parentController != null) {
                parentController.loadDataFromDB();
            }
            showInfo("Thành công",
                isEditMode ? "Cập nhật khoản thu thành công!" : "Thêm khoản thu mới thành công!"
            );
            closeWindow();
        } else {
            showError("Lỗi Database",
                isEditMode
                    ? "Không thể cập nhật khoản thu. Vui lòng thử lại."
                    : "Không thể thêm khoản thu. Mã khoản đã tồn tại hoặc lỗi kết nối."
            );
        }
    }

    /**
     * Nút "Hủy Bỏ": đóng cửa sổ, không lưu gì cả.
     */
    @FXML
    private void onHuyClick() {
        closeWindow();
    }

    // =====================================================================
    //  HELPER
    // =====================================================================

    /** Đóng cửa sổ form hiện tại. */
    private void closeWindow() {
        Stage stage = (Stage) txtMaKhoanThu.getScene().getWindow();
        stage.close();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

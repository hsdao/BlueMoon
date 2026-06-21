package controllers;

import models.KhoanThu;
import models.Labels;
import services.KhoanThuDAO;
import services.KhoanThuService;
import services.TinhPhiService;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
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
    @FXML private ComboBox<String> cmbCachTinh; // cách tính tiền (P3 v2.0)
    @FXML private TextField   txtSoThang;       // số tháng thu
    @FXML private TextField   txtDonGiaXeMay;   // đơn giá xe máy (PER_XE)
    @FXML private TextField   txtDonGiaOTo;     // đơn giá ô tô (PER_XE)
    @FXML private HBox        boxDonGiaXe;      // hàng đơn giá xe (chỉ hiện khi PER_XE)
    @FXML private Label       lblSoTien;        // nhãn ô số tiền (đổi theo cách tính)
    @FXML private TextField   txtSoTien;
    @FXML private DatePicker  dtpHanNop;
    @FXML private ComboBox<String> cmbTrangThai;  // Đang mở / Đã đóng
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
    /** Các mã cách tính theo đúng thứ tự nhãn nạp vào ComboBox. */
    private static final String[] CACH_TINH_CODES = {
        TinhPhiService.FLAT, TinhPhiService.PER_NHANKHAU,
        TinhPhiService.PER_M2, TinhPhiService.PER_XE
    };

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cmbLoaiKhoanThu.getItems().addAll("Bắt buộc", "Tự nguyện");
        // Mặc định chọn "Bắt buộc"
        cmbLoaiKhoanThu.setValue("Bắt buộc");

        // Cách tính: hiển thị nhãn tiếng Việt, lưu mã (FLAT/PER_NHANKHAU/PER_M2/PER_XE)
        for (String code : CACH_TINH_CODES) {
            cmbCachTinh.getItems().add(TinhPhiService.label(code));
        }
        cmbCachTinh.setValue(TinhPhiService.label(TinhPhiService.FLAT));
        txtSoThang.setText("1");

        // Trạng thái khoản thu (mặc định Đang mở khi tạo mới)
        cmbTrangThai.getItems().addAll("Đang mở", "Đã đóng");
        cmbTrangThai.setValue("Đang mở");

        // Cập nhật giao diện theo cách tính (nhãn ô số tiền + hàng đơn giá xe)
        cmbCachTinh.valueProperty().addListener((o, a, b) -> capNhatTheoCachTinh());
        capNhatTheoCachTinh();
    }

    /**
     * Đổi nhãn ô "số tiền/đơn giá" theo cách tính cho dễ hiểu, và ẩn/hiện hàng đơn giá xe.
     *  - FLAT         → "Số tiền cố định (đ)"
     *  - PER_NHANKHAU → "Đơn giá (đ/người/tháng)"
     *  - PER_M2       → "Đơn giá (đ/m²)"
     *  - PER_XE       → ô số tiền không dùng (nhập ở 2 ô đơn giá xe bên dưới)
     */
    private void capNhatTheoCachTinh() {
        String code = selectedCachTinhCode();
        boolean perXe = TinhPhiService.PER_XE.equals(code);

        boxDonGiaXe.setVisible(perXe);
        boxDonGiaXe.setManaged(perXe);

        switch (code) {
            case TinhPhiService.PER_NHANKHAU -> lblSoTien.setText("Đơn giá (đ/người/tháng):");
            case TinhPhiService.PER_M2       -> lblSoTien.setText("Đơn giá (đ/m²):");
            case TinhPhiService.PER_XE       -> lblSoTien.setText("Số tiền (không dùng — nhập đơn giá xe bên dưới):");
            default                          -> lblSoTien.setText("Số tiền cố định (đ):");
        }
        // Theo xe: vô hiệu ô số tiền chung để tránh nhầm
        txtSoTien.setDisable(perXe);
        if (perXe) txtSoTien.clear();
    }

    /** Đổi nhãn cách tính (đang chọn trên ComboBox) về mã để lưu DB. */
    private String selectedCachTinhCode() {
        int idx = cmbCachTinh.getSelectionModel().getSelectedIndex();
        return (idx >= 0 && idx < CACH_TINH_CODES.length)
                ? CACH_TINH_CODES[idx] : TinhPhiService.FLAT;
    }

    /** Parse số tiền; trả null nếu trống/không hợp lệ (để dùng đơn giá mặc định). */
    private Double parseTienOrNull(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try { return Double.parseDouble(s.trim().replace(",", "")); }
        catch (NumberFormatException e) { return null; }
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
        cmbLoaiKhoanThu.setValue(Labels.khoanThuLoai(kt.getLoai())); // mã -> nhãn
        cmbCachTinh.setValue(TinhPhiService.label(kt.getCachTinh()));
        txtSoThang.setText(String.valueOf(Math.max(1, kt.getSoThang())));
        txtSoTien.setText(kt.getSoTien() != null ? String.valueOf(kt.getSoTien()) : "");
        txtDonGiaXeMay.setText(kt.getDonGiaXeMay() != null ? String.valueOf(kt.getDonGiaXeMay()) : "");
        txtDonGiaOTo.setText(kt.getDonGiaOTo() != null ? String.valueOf(kt.getDonGiaOTo()) : "");
        cmbTrangThai.setValue(Labels.khoanThuTrangThai(kt.getTrangThai())); // mã -> nhãn
        capNhatTheoCachTinh();

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
        String loaiCode = Labels.khoanThuLoaiCode(cmbLoaiKhoanThu.getValue()); // nhãn -> mã
        String soTienStr = txtSoTien.getText().trim();

        // 2. Validate (dùng mã chuẩn; truyền cách tính để miễn yêu cầu số tiền khi tính theo xe)
        String errorMsg = service.validateKhoanThu(maKhoan, tenKhoan, loaiCode, soTienStr, selectedCachTinhCode());
        if (errorMsg != null && !errorMsg.isEmpty()) {
            showError("Lỗi nhập liệu", errorMsg);
            return;
        }

        // Số tháng: mặc định 1 nếu để trống / không hợp lệ
        int soThang = 1;
        try {
            String s = txtSoThang.getText().trim();
            if (!s.isEmpty()) soThang = Math.max(1, Integer.parseInt(s));
        } catch (NumberFormatException ignore) { /* giữ mặc định 1 */ }

        // 3. Tạo / cập nhật đối tượng KhoanThu
        KhoanThu kt = isEditMode ? khoanThuCurrent : new KhoanThu();
        kt.setMaKhoan(maKhoan);
        kt.setTenKhoan(tenKhoan);
        kt.setLoai(loaiCode);                 // lưu MÃ: BAT_BUOC / TU_NGUYEN
        String cachTinhCode = selectedCachTinhCode();
        kt.setCachTinh(cachTinhCode);         // lưu MÃ cách tính
        kt.setSoThang(soThang);

        // Đơn giá xe: chỉ lưu khi tính theo xe; để trống -> null (TinhPhiService dùng mặc định)
        if (TinhPhiService.PER_XE.equals(cachTinhCode)) {
            kt.setDonGiaXeMay(parseTienOrNull(txtDonGiaXeMay.getText()));
            kt.setDonGiaOTo(parseTienOrNull(txtDonGiaOTo.getText()));
        } else {
            kt.setDonGiaXeMay(null);
            kt.setDonGiaOTo(null);
        }

        // Số tiền: có thể để trống (null) nếu là tự nguyện; parseTienOrNull xử lý dấu phẩy nhất quán
        kt.setSoTien(parseTienOrNull(soTienStr));

        // Hạn nộp: có thể để trống
        LocalDate hanNopLocal = dtpHanNop.getValue();
        kt.setHanNop(hanNopLocal != null ? Date.valueOf(hanNopLocal) : null);

        // Trạng thái lấy từ ComboBox: cho phép ĐÓNG (CLOSED) / mở lại (OPEN) khi sửa
        kt.setTrangThai(Labels.khoanThuTrangThaiCode(cmbTrangThai.getValue()));

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

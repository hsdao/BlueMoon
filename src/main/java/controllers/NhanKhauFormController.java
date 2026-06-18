package controllers;

import models.HoKhau;
import models.NhanKhau;
import models.QuanHe;
import services.HoKhauDAO;
import services.NhanKhauDAO;
import services.NhanKhauService;
import services.QuanHeDAO;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class NhanKhauFormController implements Initializable {

    // ── FXML bindings ─────────────────────────────────────────────────────────
    @FXML private Label              lblTitle;
    @FXML private TextField          txtHoTen;
    @FXML private DatePicker         dtpNgaySinh;
    @FXML private ComboBox<String>   cmbGioiTinh;
    @FXML private TextField          txtCccd;
    @FXML private TextField          txtSoDienThoai;
    @FXML private TextField          txtDanToc;
    @FXML private TextField          txtTonGiao;
    @FXML private TextField          txtNgheNghiep;
    @FXML private TextField          txtNoiLamViec;
    @FXML private TextField          txtQueQuan;
    @FXML private TextField          txtDiaChiThuongTru;
    @FXML private ComboBox<QuanHe>   cmbQuanHe;
    @FXML private ComboBox<String>   cmbTrangThai;
    @FXML private ComboBox<HoKhau>   cmbHoKhau;

    // ── Dependencies ──────────────────────────────────────────────────────────
    private final NhanKhauDAO     dao     = new NhanKhauDAO();
    private final HoKhauDAO       hoDAO   = new HoKhauDAO();
    private final QuanHeDAO       qhDAO   = new QuanHeDAO();
    private final NhanKhauService service = new NhanKhauService();

    // ── State ─────────────────────────────────────────────────────────────────
    private NhanKhau           current;
    private boolean            isEditMode = false;
    private NhanKhauController parentController;

    // ── Init ──────────────────────────────────────────────────────────────────

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cmbGioiTinh.setItems(FXCollections.observableArrayList("Nam", "Nữ", "Khác"));
        cmbGioiTinh.setValue("Nam");

        cmbTrangThai.setItems(FXCollections.observableArrayList(
                "PERMANENT", "TEMPORARY", "MOVED_OUT"));
        cmbTrangThai.setValue("PERMANENT");

        loadQuanHe();
        loadHoKhau();
    }

    private void loadQuanHe() {
        // Đảm bảo Ông, Bà, Cháu tồn tại trong DB trước khi load
        ensureQuanHeExists("Ông");
        ensureQuanHeExists("Bà");
        ensureQuanHeExists("Cháu");

        List<QuanHe> list = qhDAO.getAll();
        ObservableList<QuanHe> obs = FXCollections.observableArrayList(list);
        cmbQuanHe.setItems(obs);
        cmbQuanHe.setCellFactory(lv -> cellQuanHe());
        cmbQuanHe.setButtonCell(cellQuanHe());
        if (!obs.isEmpty()) cmbQuanHe.getSelectionModel().selectFirst();
    }

    private void ensureQuanHeExists(String tenQuanHe) {
        boolean exists = qhDAO.getAll().stream()
                .anyMatch(q -> q.getTenQuanHe().equalsIgnoreCase(tenQuanHe));
        if (!exists) {
            QuanHe q = new QuanHe();
            q.setTenQuanHe(tenQuanHe);
            qhDAO.insert(q);
        }
    }

    private void loadHoKhau() {
        List<HoKhau> list = hoDAO.getAllHoKhau();
        ObservableList<HoKhau> obs = FXCollections.observableArrayList(list);
        cmbHoKhau.setItems(obs);
        cmbHoKhau.setCellFactory(lv -> cellHoKhau());
        cmbHoKhau.setButtonCell(cellHoKhau());
    }

    // ── Cell factories ────────────────────────────────────────────────────────

    private ListCell<QuanHe> cellQuanHe() {
        return new ListCell<>() {
            @Override protected void updateItem(QuanHe q, boolean empty) {
                super.updateItem(q, empty);
                setText(empty || q == null ? null : q.getTenQuanHe());
            }
        };
    }

    private ListCell<HoKhau> cellHoKhau() {
        return new ListCell<>() {
            @Override protected void updateItem(HoKhau hk, boolean empty) {
                super.updateItem(hk, empty);
                setText(empty || hk == null ? "-- Chọn hộ khẩu --"
                        : hk.getMaHo() + " – " + hk.getDiaChi());
            }
        };
    }

    // ── Mode setters ──────────────────────────────────────────────────────────

    public void setParentController(NhanKhauController parent) {
        this.parentController = parent;
    }

    public void setAddMode() {
        isEditMode = false;
        current    = null;
        lblTitle.setText("Thêm Nhân Khẩu Mới");
    }

    public void setEditData(NhanKhau nk) {
        isEditMode = true;
        current    = nk;
        lblTitle.setText("Sửa Thông Tin Nhân Khẩu");

        txtHoTen.setText(nk.getHoTen());
        if (nk.getNgaySinh() != null) dtpNgaySinh.setValue(nk.getNgaySinh());
        cmbGioiTinh.setValue(nk.getGioiTinh());
        txtCccd.setText(nk.getCccd() != null ? nk.getCccd() : "");
        txtSoDienThoai.setText(nk.getSoDienThoai());
        txtDanToc.setText(nk.getDanToc() != null ? nk.getDanToc() : "");
        txtTonGiao.setText(nk.getTonGiao() != null ? nk.getTonGiao() : "");
        txtNgheNghiep.setText(nk.getNgheNghiep() != null ? nk.getNgheNghiep() : "");
        txtNoiLamViec.setText(nk.getNoiLamViec() != null ? nk.getNoiLamViec() : "");
        txtQueQuan.setText(nk.getQueQuan() != null ? nk.getQueQuan() : "");
        txtDiaChiThuongTru.setText(nk.getDiaChiThuongTru() != null ? nk.getDiaChiThuongTru() : "");
        cmbTrangThai.setValue(nk.getTrangThai());

        // Chọn quan hệ khớp ID
        cmbQuanHe.getItems().stream()
                .filter(q -> q.getId() == nk.getQuanHeId())
                .findFirst().ifPresent(cmbQuanHe::setValue);

        // Chọn hộ khẩu khớp ID
        cmbHoKhau.getItems().stream()
                .filter(hk -> hk.getId() == nk.getHoKhauId())
                .findFirst().ifPresent(cmbHoKhau::setValue);
    }

    // ── Save / Cancel ─────────────────────────────────────────────────────────

    @FXML
    private void handleSave() {
        // ── 1. Thu thập ──────────────────────────────────────────────────────
        String hoTen    = txtHoTen.getText().trim();
        String gioiTinh = cmbGioiTinh.getValue();
        String sdt      = txtSoDienThoai.getText().trim();
        String cccd     = txtCccd.getText().trim();
        HoKhau hoKhau   = cmbHoKhau.getValue();
        int    hoKhauId = hoKhau != null ? hoKhau.getId() : 0;

        // ── 2. Validate UI ────────────────────────────────────────────────────
        String err = service.validateNhanKhau(hoTen, null, gioiTinh, sdt, cccd, hoKhauId);
        if (err != null && !err.isEmpty()) { showError("Lỗi nhập liệu", err); return; }

        // ngay_sinh NOT NULL trong DB
        if (dtpNgaySinh.getValue() == null) {
            showError("Lỗi nhập liệu", "Vui lòng chọn ngày sinh!");
            return;
        }
        if (dtpNgaySinh.getValue().isAfter(java.time.LocalDate.now())) {
            showError("Lỗi nhập liệu", "Ngày sinh không được ở tương lai!");
            return;
        }

        // quan_he: nếu không chọn → lấy mặc định đầu tiên (tránh FK = 0)
        QuanHe qh = cmbQuanHe.getValue();
        if (qh == null && !cmbQuanHe.getItems().isEmpty()) {
            qh = cmbQuanHe.getItems().get(0);
            cmbQuanHe.setValue(qh);
        }
        if (qh == null) {
            showError("Lỗi nhập liệu", "Vui lòng chọn quan hệ với chủ hộ!");
            return;
        }

        // (Số thành viên của hộ giờ tự tính theo số nhân khẩu thực tế — không còn giới hạn thủ công.)

        // ── 3. Build object ───────────────────────────────────────────────────
        NhanKhau nk = isEditMode ? current : new NhanKhau();

        nk.setHoKhauId(hoKhauId);                          // 1. ho_khau_id
        nk.setHoTen(hoTen);                                // 2. ho_ten
        nk.setNgaySinh(dtpNgaySinh.getValue());            // 3. ngay_sinh
        nk.setGioiTinh(gioiTinh);                          // 4. gioi_tinh
        nk.setCccd(cccd.isEmpty() ? null : cccd);          // 5. cccd
        nk.setDanToc(blankToNull(txtDanToc));              // 6. dan_toc
        nk.setTonGiao(blankToNull(txtTonGiao));            // 7. ton_giao
        nk.setNgheNghiep(blankToNull(txtNgheNghiep));     // 8. nghe_nghiep
        nk.setNoiLamViec(blankToNull(txtNoiLamViec));     // 9. noi_lam_viec
        nk.setQueQuan(blankToNull(txtQueQuan));            // 10. que_quan
        nk.setDiaChiThuongTru(blankToNull(txtDiaChiThuongTru)); // 11. dia_chi_thuong_tru
        nk.setQuanHeId(qh.getId());                        // 12. quan_he_id
        nk.setSoDienThoai(sdt.isEmpty() ? null : sdt);     // 13. so_dien_thoai (null nếu trống)
        nk.setTrangThai(cmbTrangThai.getValue());          // 14. trang_thai

        // ── 4. Lưu DB ─────────────────────────────────────────────────────────
        try {
            boolean ok = isEditMode ? dao.update(nk) : dao.insert(nk);
            if (ok) {
                if (parentController != null) parentController.loadDataFromDB();
                showInfo("Thành công",
                        isEditMode ? "Cập nhật nhân khẩu thành công!"
                                : "Thêm nhân khẩu mới thành công!");
                closeWindow();
            } else {
                // insert() trả false khi isValid() fail — không phải lỗi DB
                showError("Lỗi",
                        "Thao tác thất bại. Kiểm tra lại:\n"
                                + "• Hộ khẩu đã chọn chưa?\n"
                                + "• Số điện thoại đã tồn tại chưa?\n"
                                + "(Xem console để biết chi tiết)");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Lỗi Database", ex.getMessage());
        }
    }

    @FXML
    private void handleCancel() { closeWindow(); }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void closeWindow() {
        Stage stage = (Stage) txtHoTen.getScene().getWindow();
        stage.close();
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    /** Trả về null nếu TextField rỗng, ngược lại trả về text đã trim. */
    private String blankToNull(TextField field) {
        if (field == null) return null;
        String v = field.getText();
        return (v == null || v.trim().isEmpty()) ? null : v.trim();
    }
}
package controllers;

import models.HoKhau;
import models.NhanKhau;
import models.Phong;
import models.QuanHe;
import services.HoKhauDAO;
import services.HoKhauService;
import services.NhanKhauDAO;
import services.NhanKhauService;
import services.PhongDAO;
import services.QuanHeDAO;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

/**
 * Form Thêm/Sửa hộ khẩu.
 * THÊM: chọn phòng trống + nhập thông tin CHỦ HỘ → tạo hộ khẩu VÀ tạo chủ hộ
 * như một nhân khẩu mới (hiện luôn ở màn Nhân khẩu), rồi liên kết chu_ho_id.
 * SỬA: cập nhật thông tin hộ + cập nhật thông tin chủ hộ ở bảng nhân khẩu.
 */
public class HoKhauFormController implements Initializable {
    @FXML private Label lblTitle;
    @FXML private ComboBox<Phong> cmbPhong;
    @FXML private TextField txtDienTich;
    @FXML private TextField txtSoThanhVien;
    @FXML private TextField txtHoTen;        // họ tên chủ hộ
    @FXML private DatePicker dpNgaySinh;     // ngày sinh chủ hộ
    @FXML private ComboBox<String> cmbGioiTinh;
    @FXML private TextField txtCccd;
    @FXML private TextField txtSdt;          // SĐT chủ hộ
    @FXML private TextField txtSoXeMay;
    @FXML private TextField txtSoOTo;
    @FXML private DatePicker dpNgayTao;
    @FXML private ComboBox<String> cbTrangThai;
    @FXML private TextArea txtGhiChu;

    private HoKhau hoKhauCurent;
    private NhanKhau chuHoCurrent;   // chủ hộ hiện tại (chế độ sửa)
    private boolean isEditMode = false;
    private HoKhauController parentController;

    private final HoKhauDAO dao = new HoKhauDAO();
    private final HoKhauService service = new HoKhauService();
    private final NhanKhauDAO nhanKhauDAO = new NhanKhauDAO();
    private final NhanKhauService nhanKhauService = new NhanKhauService();
    private final PhongDAO phongDAO = new PhongDAO();
    private final QuanHeDAO quanHeDAO = new QuanHeDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cbTrangThai.getItems().addAll("ACTIVE", "INACTIVE");
        cbTrangThai.setValue("ACTIVE");
        cmbGioiTinh.getItems().addAll("Nam", "Nữ", "Khác");
        cmbGioiTinh.setValue("Nam");
        dpNgayTao.setValue(LocalDate.now());

        cmbPhong.setCellFactory(lv -> cellPhong());
        cmbPhong.setButtonCell(cellPhong());
        cmbPhong.valueProperty().addListener((o, a, p) ->
                txtDienTich.setText(p != null ? String.valueOf(p.getDienTich()) : ""));

        txtDienTich.setEditable(false);
        txtSoThanhVien.setEditable(false);
        txtSoXeMay.setText("0");
        txtSoOTo.setText("0");
    }

    private ListCell<Phong> cellPhong() {
        return new ListCell<>() {
            @Override protected void updateItem(Phong p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? null : p.getMaPhong() + "  (" + p.getDienTich() + " m²)");
            }
        };
    }

    public void setParentController(HoKhauController parentController) {
        this.parentController = parentController;
    }

    // THÊM: chọn phòng trống, nhập chủ hộ mới
    public void setAddMode() {
        isEditMode = false;
        chuHoCurrent = null;
        lblTitle.setText("Thêm Hộ Khẩu Mới");
        cmbPhong.setItems(FXCollections.observableArrayList(phongDAO.getTrong()));
        cmbPhong.setDisable(false);
        txtSoThanhVien.setText("1");
        dpNgayTao.setDisable(true);
    }

    // SỬA: phòng cố định; nạp thông tin hộ + chủ hộ
    public void setEditData(HoKhau hk) {
        hoKhauCurent = hk;
        isEditMode = true;
        lblTitle.setText("Sửa Hộ Khẩu");
        dpNgayTao.setDisable(false);

        Phong cur = new Phong(0, hk.getMaHo(), parseTang(hk.getMaHo()), hk.getDienTich());
        cmbPhong.setItems(FXCollections.observableArrayList(cur));
        cmbPhong.setValue(cur);
        cmbPhong.setDisable(true);

        txtDienTich.setText(String.valueOf(hk.getDienTich()));
        txtSoThanhVien.setText(String.valueOf(hk.getSoThanhVien()));
        txtSoXeMay.setText(String.valueOf(hk.getSoXeMay()));
        txtSoOTo.setText(String.valueOf(hk.getSoOTo()));
        cbTrangThai.setValue(hk.getTrangThai());
        txtGhiChu.setText(hk.getGhiChu() != null ? hk.getGhiChu() : "");
        if (hk.getNgayTao() != null) dpNgayTao.setValue(hk.getNgayTao().toLocalDateTime().toLocalDate());

        // Nạp thông tin chủ hộ từ bảng nhân khẩu
        if (hk.getChuHoId() != null) {
            chuHoCurrent = nhanKhauDAO.getById(hk.getChuHoId());
        }
        if (chuHoCurrent != null) {
            txtHoTen.setText(chuHoCurrent.getHoTen());
            if (chuHoCurrent.getNgaySinh() != null) dpNgaySinh.setValue(chuHoCurrent.getNgaySinh());
            if (chuHoCurrent.getGioiTinh() != null) cmbGioiTinh.setValue(chuHoCurrent.getGioiTinh());
            txtCccd.setText(chuHoCurrent.getCccd() != null ? chuHoCurrent.getCccd() : "");
            txtSdt.setText(chuHoCurrent.getSoDienThoai() != null ? chuHoCurrent.getSoDienThoai()
                    : hk.getSoDienThoaiChuHo());
        } else {
            txtSdt.setText(hk.getSoDienThoaiChuHo());
        }
    }

    @FXML
    private void handleSave() {
        Phong phong = cmbPhong.getValue();
        if (phong == null) {
            showAlert(Alert.AlertType.ERROR, "Lỗi nhập liệu", "Vui lòng chọn phòng cho hộ khẩu!");
            return;
        }
        String maHo    = phong.getMaPhong();
        String hoTen   = txtHoTen.getText() == null ? "" : txtHoTen.getText().trim();
        String gioiTinh = cmbGioiTinh.getValue();
        String cccd    = txtCccd.getText() == null ? "" : txtCccd.getText().trim();
        String sdt     = txtSdt.getText() == null ? "" : txtSdt.getText().trim();
        LocalDate ngaySinh = dpNgaySinh.getValue();
        String diaChi  = "Phòng " + maHo;
        String soTvStr = txtSoThanhVien.getText().trim().isEmpty() ? "1" : txtSoThanhVien.getText().trim();

        // 1. Validate thông tin chủ hộ (như một nhân khẩu)
        String errNk = nhanKhauService.validateNhanKhau(hoTen, null, gioiTinh, sdt, cccd, 1);
        if (errNk != null) { showAlert(Alert.AlertType.ERROR, "Lỗi nhập liệu", errNk); return; }
        if (sdt.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi nhập liệu", "SĐT chủ hộ không được để trống!");
            return;
        }
        if (ngaySinh == null) {
            showAlert(Alert.AlertType.ERROR, "Lỗi nhập liệu", "Vui lòng chọn ngày sinh chủ hộ!");
            return;
        }
        // 2. Validate thông tin hộ
        String errHk = service.validateHoKhau(maHo, sdt, diaChi, soTvStr);
        if (errHk != null) { showAlert(Alert.AlertType.ERROR, "Lỗi nhập liệu", errHk); return; }

        int soXeMay, soOTo;
        try {
            soXeMay = parseIntOrZero(txtSoXeMay.getText());
            soOTo   = parseIntOrZero(txtSoOTo.getText());
            if (soXeMay < 0 || soOTo < 0) {
                showAlert(Alert.AlertType.ERROR, "Lỗi nhập liệu", "Số xe không được âm!"); return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi nhập liệu", "Số xe phải là số nguyên!"); return;
        }

        if (isEditMode) {
            luuSua(soXeMay, soOTo, hoTen, ngaySinh, gioiTinh, cccd, sdt, phong);
        } else {
            luuThem(maHo, diaChi, soXeMay, soOTo, hoTen, ngaySinh, gioiTinh, cccd, sdt, phong);
        }
    }

    // ----- THÊM MỚI: tạo hộ + tạo chủ hộ (nhân khẩu) + liên kết -----
    private void luuThem(String maHo, String diaChi, int soXeMay, int soOTo,
                         String hoTen, LocalDate ngaySinh, String gioiTinh, String cccd, String sdt, Phong phong) {
        HoKhau hk = new HoKhau();
        hk.setMaHo(maHo);
        hk.setSoDienThoaiChuHo(sdt);
        hk.setDiaChi(diaChi);
        hk.setSoThanhVien(1);
        hk.setDienTich(phong.getDienTich());
        hk.setSoXeMay(soXeMay);
        hk.setSoOTo(soOTo);
        hk.setTrangThai(cbTrangThai.getValue());
        hk.setGhiChu(txtGhiChu.getText() != null ? txtGhiChu.getText().trim() : "");
        hk.setNgayTao(new java.sql.Timestamp(System.currentTimeMillis()));

        // Chủ hộ = nhân khẩu mới
        NhanKhau nk = new NhanKhau();
        nk.setHoTen(hoTen);
        nk.setNgaySinh(ngaySinh);
        nk.setGioiTinh(gioiTinh);
        nk.setCccd(cccd.isEmpty() ? null : cccd);
        nk.setSoDienThoai(sdt);
        nk.setQuanHeId(chuHoQuanHeId());
        nk.setTrangThai("PERMANENT");

        // Tạo hộ + chủ hộ trong MỘT giao dịch (nguyên tử)
        if (dao.themHoKhauVoiChuHo(hk, nk)) {
            ketThuc("Thêm hộ khẩu + chủ hộ thành công!");
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi",
                    "Không thể thêm. Phòng có thể đã có hộ, hoặc SĐT/CCCD chủ hộ bị trùng.");
        }
    }

    // ----- SỬA: cập nhật hộ + cập nhật chủ hộ ở bảng nhân khẩu -----
    private void luuSua(int soXeMay, int soOTo, String hoTen, LocalDate ngaySinh,
                        String gioiTinh, String cccd, String sdt, Phong phong) {
        HoKhau hk = hoKhauCurent;
        hk.setDienTich(phong.getDienTich());
        hk.setSoXeMay(soXeMay);
        hk.setSoOTo(soOTo);
        hk.setSoDienThoaiChuHo(sdt);
        hk.setTrangThai(cbTrangThai.getValue());
        hk.setGhiChu(txtGhiChu.getText() != null ? txtGhiChu.getText().trim() : "");
        if (dpNgayTao.getValue() != null)
            hk.setNgayTao(java.sql.Timestamp.valueOf(dpNgayTao.getValue().atStartOfDay()));

        // Cập nhật / tạo chủ hộ ở bảng nhân khẩu
        if (chuHoCurrent != null) {
            chuHoCurrent.setHoTen(hoTen);
            chuHoCurrent.setNgaySinh(ngaySinh);
            chuHoCurrent.setGioiTinh(gioiTinh);
            chuHoCurrent.setCccd(cccd.isEmpty() ? null : cccd);
            chuHoCurrent.setSoDienThoai(sdt);
            nhanKhauDAO.update(chuHoCurrent);
        } else {
            NhanKhau nk = new NhanKhau();
            nk.setHoKhauId(hk.getId());
            nk.setHoTen(hoTen); nk.setNgaySinh(ngaySinh); nk.setGioiTinh(gioiTinh);
            nk.setCccd(cccd.isEmpty() ? null : cccd); nk.setSoDienThoai(sdt);
            nk.setQuanHeId(chuHoQuanHeId()); nk.setTrangThai("PERMANENT");
            if (nhanKhauDAO.insert(nk)) hk.setChuHoId(nk.getId());
        }

        if (dao.capNhatHoKhau(hk)) ketThuc("Cập nhật hộ khẩu thành công!");
        else showAlert(Alert.AlertType.ERROR, "Lỗi Database", "Không thể cập nhật hộ khẩu.");
    }

    private void ketThuc(String msg) {
        if (parentController != null) parentController.loadDataFromDB();
        showAlert(Alert.AlertType.INFORMATION, "Thành công", msg);
        closeWindow();
    }

    /** Lấy id quan hệ "Chủ hộ" (tạo nếu chưa có). */
    private int chuHoQuanHeId() {
        for (QuanHe q : quanHeDAO.getAll())
            if ("Chủ hộ".equalsIgnoreCase(q.getTenQuanHe())) return q.getId();
        QuanHe q = new QuanHe();
        q.setTenQuanHe("Chủ hộ");
        quanHeDAO.insert(q);
        return q.getId() > 0 ? q.getId() : 1;
    }

    @FXML
    private void handleCancel() { closeWindow(); }

    private void closeWindow() {
        Stage stage = (Stage) cmbPhong.getScene().getWindow();
        stage.close();
    }

    private int parseIntOrZero(String s) {
        if (s == null || s.trim().isEmpty()) return 0;
        return Integer.parseInt(s.trim());
    }

    private int parseTang(String maHo) {
        if (maHo == null) return 0;
        String d = maHo.replaceAll("\\D", "");
        if (d.isEmpty()) return 0;
        try { int n = Integer.parseInt(d); return n >= 100 ? n / 100 : n; }
        catch (NumberFormatException e) { return 0; }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

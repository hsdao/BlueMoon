package controllers;

import application.UserSession;
import models.HoKhau;
import models.KhoanThu;
import models.NhanKhau;
import models.NopTien;
import models.User;
import services.HoKhauDAO;
import services.KhoanThuDAO;
import services.NhanKhauDAO;
import services.NopTienDAO;
import services.ThuPhiDAO;
import services.ThuPhiService;
import services.TinhPhiService;
import services.ExportPDFService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller màn hình Thu phí.
 * Cải tiến theo góp ý thu ngân: người thu tự điền theo tài khoản, lọc phòng,
 * chống trùng thật, khóa số tiền tự tính, sửa/xóa giao dịch, in biên lai.
 */
public class ThuPhiController implements Initializable {

    @FXML private ComboBox<KhoanThu> cmbKhoanThu;
    @FXML private ComboBox<HoKhau>   cmbHoKhau;
    @FXML private TextField           txtTimHoKhau;
    @FXML private TextField           txtSoTien;
    @FXML private DatePicker          dpNgayNop;
    @FXML private TextField           txtNguoiThu;
    @FXML private TextArea            txtGhiChu;
    @FXML private Button              btnGhiNhan;
    @FXML private Button              btnLamMoi;

    @FXML private TableView<NopTien>             tblLichSu;
    @FXML private TableColumn<NopTien, Integer>  colId;
    @FXML private TableColumn<NopTien, String>   colHoKhauId;
    @FXML private TableColumn<NopTien, String>   colSoTien;
    @FXML private TableColumn<NopTien, String>   colNguoiThu;
    @FXML private TableColumn<NopTien, String>   colGhiChu;
    @FXML private TableColumn<NopTien, String>   colNgayNop;

    private final KhoanThuDAO  khoanThuDAO = new KhoanThuDAO();
    private final HoKhauDAO    hoKhauDAO   = new HoKhauDAO();
    private final NhanKhauDAO  nhanKhauDAO = new NhanKhauDAO();
    private final ThuPhiDAO    thuPhiDAO   = new ThuPhiDAO();
    private final NopTienDAO   nopTienDAO  = new NopTienDAO();
    private final ThuPhiService service    = new ThuPhiService();
    private final TinhPhiService tinhPhiService = new TinhPhiService();
    private final ExportPDFService pdf = new ExportPDFService();

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final NumberFormat currencyFmt = NumberFormat.getInstance(new Locale("vi", "VN"));

    /** id hộ -> "số phòng – chủ hộ" và id khoản -> tên khoản (cho hiển thị + biên lai). */
    private final Map<Integer, String> hoKhauLabel = new HashMap<>();
    private final Map<Integer, String> khoanThuLabel = new HashMap<>();

    private ObservableList<HoKhau> allHoKhau;     // toàn bộ hộ (để lọc)
    private FilteredList<HoKhau>   hoKhauFiltered; // lọc theo ô tìm
    private NopTien dangSua = null;                // bản ghi đang sửa (null = thêm mới)

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupColumns();
        hoKhauDAO.recomputeAllSoThanhVien(); // phí theo nhân khẩu tính trên số thành viên mới nhất
        loadKhoanThu();
        loadHoKhau();
        dpNgayNop.setValue(LocalDate.now());

        // Người thu = tài khoản đang đăng nhập (dùng USERNAME để Đối soát quỹ gom nhất quán,
        // tránh tách 1 người thành nhiều dòng do tên hiển thị khác nhau). Không cho sửa.
        User u = UserSession.getInstance().getCurrentUser();
        txtNguoiThu.setText(u == null ? "" : u.getUsername());
        txtNguoiThu.setEditable(false);

        cmbKhoanThu.setOnAction(e -> { capNhatKhoaSoTien(); preFillSoTien(); loadLichSu(); });
        cmbHoKhau.setOnAction(e -> preFillSoTien());

        // Lọc hộ khẩu theo số phòng / tên chủ hộ
        txtTimHoKhau.textProperty().addListener((o, a, kw) -> {
            String f = kw == null ? "" : kw.toLowerCase().trim();
            hoKhauFiltered.setPredicate(hk -> {
                if (f.isEmpty()) return true;
                String label = hoKhauLabel.getOrDefault(hk.getId(), "");
                return (hk.getMaHo() != null && hk.getMaHo().toLowerCase().contains(f))
                        || (hk.getDiaChi() != null && hk.getDiaChi().toLowerCase().contains(f))
                        || label.toLowerCase().contains(f);
            });
        });
    }

    // ---- Cột bảng ----
    private void setupColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        colHoKhauId.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) { setText(null); return; }
                int hkId = getTableView().getItems().get(getIndex()).getHoKhauId();
                setText(hoKhauLabel.getOrDefault(hkId, "—"));
            }
        });

        colSoTien.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) { setText(null); return; }
                BigDecimal st = getTableView().getItems().get(getIndex()).getSoTien();
                setText(st != null ? currencyFmt.format(st) + " đ" : "—");
            }
        });

        colNguoiThu.setCellValueFactory(new PropertyValueFactory<>("nguoiThu"));
        colGhiChu.setCellValueFactory(new PropertyValueFactory<>("ghiChu"));

        colNgayNop.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) { setText(null); return; }
                LocalDate d = getTableView().getItems().get(getIndex()).getNgayNop();
                setText(d != null ? d.format(DATE_FMT) : "—");
            }
        });
    }

    private void loadKhoanThu() {
        // Chỉ thu cho khoản ĐANG MỞ (OPEN) — khoản đã đóng không cho thu nữa
        List<KhoanThu> ds = khoanThuDAO.getAllKhoanThu().stream()
                .filter(k -> "OPEN".equals(k.getTrangThai()))
                .collect(Collectors.toList());
        cmbKhoanThu.setItems(FXCollections.observableArrayList(ds));
        cmbKhoanThu.setCellFactory(lv -> cellKT());
        cmbKhoanThu.setButtonCell(cellKT());
        khoanThuLabel.clear();
        for (KhoanThu kt : ds) khoanThuLabel.put(kt.getId(), kt.getMaKhoan() + " – " + kt.getTenKhoan());
    }

    private ListCell<KhoanThu> cellKT() {
        return new ListCell<>() {
            @Override protected void updateItem(KhoanThu kt, boolean empty) {
                super.updateItem(kt, empty);
                setText(empty || kt == null ? null : kt.getMaKhoan() + " – " + kt.getTenKhoan());
            }
        };
    }

    private void loadHoKhau() {
        List<HoKhau> ds = hoKhauDAO.getAllHoKhau();
        allHoKhau = FXCollections.observableArrayList(ds);
        hoKhauFiltered = new FilteredList<>(allHoKhau, x -> true);
        cmbHoKhau.setItems(hoKhauFiltered);
        cmbHoKhau.setCellFactory(lv -> cellHK());
        cmbHoKhau.setButtonCell(cellHK());

        Map<Integer, String> tenChuHo = nhanKhauDAO.getAll().stream()
                .collect(Collectors.toMap(NhanKhau::getId, NhanKhau::getHoTen, (a, b) -> a));
        hoKhauLabel.clear();
        for (HoKhau hk : ds) {
            String phong = (hk.getDiaChi() != null && !hk.getDiaChi().isBlank()) ? hk.getDiaChi() : hk.getMaHo();
            String owner = hk.getChuHoId() != null ? tenChuHo.get(hk.getChuHoId()) : null;
            hoKhauLabel.put(hk.getId(), owner != null ? phong + " – " + owner : phong);
        }
    }

    private ListCell<HoKhau> cellHK() {
        return new ListCell<>() {
            @Override protected void updateItem(HoKhau hk, boolean empty) {
                super.updateItem(hk, empty);
                setText(empty || hk == null ? null : hk.getMaHo() + " – " + hk.getDiaChi());
            }
        };
    }

    /** Khóa ô số tiền khi khoản thu tính TỰ ĐỘNG; chỉ mở khi FLAT chưa định mức (điện/nước). */
    private void capNhatKhoaSoTien() {
        KhoanThu kt = cmbKhoanThu.getValue();
        boolean choNhap = (kt != null && TinhPhiService.FLAT.equals(kt.getCachTinh()) && kt.getSoTien() == null);
        txtSoTien.setEditable(choNhap);
    }

    private void preFillSoTien() {
        KhoanThu kt = cmbKhoanThu.getValue();
        HoKhau   hk = cmbHoKhau.getValue();
        if (kt == null) { txtSoTien.clear(); return; }
        if (hk != null) {
            txtSoTien.setText(tinhPhiService.tinhPhi(kt, hk).toBigInteger().toString());
        } else if (kt.getSoTien() != null) {
            txtSoTien.setText(String.valueOf(kt.getSoTien().longValue()));
        } else {
            txtSoTien.clear();
        }
    }

    private void loadLichSu() {
        KhoanThu kt = cmbKhoanThu.getValue();
        if (kt == null) { tblLichSu.getItems().clear(); return; }
        tblLichSu.setItems(FXCollections.observableArrayList(thuPhiDAO.getByKhoanThu(kt.getId())));
    }

    // ---- Ghi nhận / cập nhật ----
    @FXML
    private void onGhiNhanClick() {
        KhoanThu kt = cmbKhoanThu.getValue();
        HoKhau   hk = cmbHoKhau.getValue();
        int ktId = kt != null ? kt.getId() : 0;
        int hkId = hk != null ? hk.getId() : 0;

        String loi = service.validate(ktId, hkId, txtSoTien.getText(), txtNguoiThu.getText(), dpNgayNop.getValue());
        if (loi != null) { showAlert(Alert.AlertType.WARNING, "Dữ liệu không hợp lệ", loi); return; }

        // CHỐNG TRÙNG THẬT: nếu thêm mới mà hộ đã nộp khoản này -> CHẶN (không cho ghi đè)
        if (dangSua == null && service.kiemTraDaNop(ktId, hkId)) {
            showAlert(Alert.AlertType.ERROR, "Đã nộp rồi",
                    "Hộ \"" + hoKhauLabel.getOrDefault(hkId, "") + "\" đã nộp khoản \""
                    + kt.getTenKhoan() + "\".\nKhông thể ghi nhận trùng. Nếu cần sửa, hãy chọn dòng và bấm \"Sửa dòng chọn\".");
            return;
        }

        NopTien nt = (dangSua != null) ? dangSua : new NopTien();
        nt.setKhoanThuId(ktId);
        nt.setHoKhauId(hkId);
        nt.setSoTien(new BigDecimal(txtSoTien.getText().trim().replace(",", "")));
        nt.setNgayNop(dpNgayNop.getValue());
        nt.setNguoiThu(txtNguoiThu.getText().trim());
        nt.setGhiChu(txtGhiChu.getText() == null ? "" : txtGhiChu.getText().trim());

        boolean ok = (dangSua != null) ? nopTienDAO.capNhatNopTien(nt) : service.ghiNhanNopTien(nt);
        if (ok) {
            boolean dangSuaCu = dangSua != null;
            dangSua = null;
            showAlert(Alert.AlertType.INFORMATION, "Thành công",
                    dangSuaCu ? "Đã cập nhật giao dịch." : "Đã ghi nhận nộp tiền thành công!");
            resetEntryFields();
            loadLichSu();
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể lưu. Vui lòng thử lại.");
        }
    }

    // ---- Sửa dòng chọn ----
    @FXML
    private void onSuaNopClick() {
        NopTien sel = tblLichSu.getSelectionModel().getSelectedItem();
        if (sel == null) { showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Hãy chọn một dòng để sửa."); return; }
        dangSua = sel;
        cmbHoKhau.getItems().stream().filter(h -> h.getId() == sel.getHoKhauId())
                .findFirst().ifPresent(cmbHoKhau::setValue);
        txtSoTien.setEditable(true); // khi sửa cho phép chỉnh số tiền
        txtSoTien.setText(sel.getSoTien() != null ? sel.getSoTien().toBigInteger().toString() : "");
        dpNgayNop.setValue(sel.getNgayNop());
        txtGhiChu.setText(sel.getGhiChu());
        showAlert(Alert.AlertType.INFORMATION, "Chế độ sửa",
                "Đang sửa giao dịch #" + sel.getId() + ". Chỉnh xong bấm \"Ghi nhận\" để lưu.");
    }

    // ---- Xóa dòng chọn ----
    @FXML
    private void onXoaNopClick() {
        NopTien sel = tblLichSu.getSelectionModel().getSelectedItem();
        if (sel == null) { showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Hãy chọn một dòng để xóa."); return; }
        Alert c = new Alert(Alert.AlertType.CONFIRMATION);
        c.setTitle("Xác nhận xóa"); c.setHeaderText(null);
        c.setContentText("Xóa giao dịch nộp tiền #" + sel.getId() + " của \""
                + hoKhauLabel.getOrDefault(sel.getHoKhauId(), "") + "\"?");
        if (c.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
        if (nopTienDAO.xoaNopTien(sel.getId())) {
            showAlert(Alert.AlertType.INFORMATION, "Đã xóa", "Đã xóa giao dịch.");
            loadLichSu();
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa giao dịch.");
        }
    }

    // ---- In biên lai dòng chọn ----
    @FXML
    private void onInBienLaiClick() {
        NopTien sel = tblLichSu.getSelectionModel().getSelectedItem();
        if (sel == null) { showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Hãy chọn một dòng để in biên lai."); return; }
        FileChooser fc = new FileChooser();
        fc.setTitle("Lưu biên lai");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        fc.setInitialFileName("BienLai_" + sel.getId() + "_" + LocalDate.now() + ".pdf");
        File f = fc.showSaveDialog(tblLichSu.getScene().getWindow());
        if (f == null) return;
        try {
            pdf.exportBienLai("BL" + sel.getId(),
                    khoanThuLabel.getOrDefault(sel.getKhoanThuId(), "—"),
                    hoKhauLabel.getOrDefault(sel.getHoKhauId(), "—"),
                    sel.getSoTien(), sel.getNguoiThu(), sel.getNgayNop(), sel.getGhiChu(), f);
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xuất biên lai:\n" + f.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không xuất được biên lai: " + e.getMessage());
        }
    }

    @FXML
    private void onLamMoiClick() {
        // Chỉ làm sạch ô nhập, GIỮ khoản thu đang chọn + bảng lịch sử
        dangSua = null;
        resetEntryFields();
        loadLichSu();
    }

    /** Xóa các ô nhập (giữ khoản thu + người thu auto). */
    private void resetEntryFields() {
        cmbHoKhau.setValue(null);
        txtTimHoKhau.clear();
        txtSoTien.clear();
        txtGhiChu.clear();
        dpNgayNop.setValue(LocalDate.now());
        capNhatKhoaSoTien();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert a = new Alert(type);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(content);
        a.showAndWait();
    }
}

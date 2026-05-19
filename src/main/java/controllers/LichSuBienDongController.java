package controllers;

import application.UserSession;
import models.HoKhau;
import models.LichSuBienDong;
import models.NhanKhau;
import services.HoKhauDAO;
import services.LichSuDAO;
import services.NhanKhauDAO;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class LichSuBienDongController implements Initializable {

    // ── FXML — Left panel ────────────────────────────────────────────────────
    @FXML private TextField          txtSearchNhanKhau;
    @FXML private Button             btnClearPerson;     // nút ✕ hiện sau khi chọn
    @FXML private ListView<NhanKhau> lvSuggestions;
    @FXML private VBox               paneNhanKhauInfo;
    @FXML private Label              lblNKTen;
    @FXML private Label              lblNKNgaySinh;
    @FXML private Label              lblNKGioiTinh;
    @FXML private Label              lblNKCccd;
    @FXML private Label              lblNKHoKhau;
    @FXML private Label              lblNKTrangThai;

    @FXML private ComboBox<String>   cmbLoaiBienDong;
    @FXML private DatePicker         dtpNgayBienDong;
    @FXML private TextArea           txaGhiChu;
    @FXML private Button             btnLuuBienDong;

    // ── FXML — Right panel ───────────────────────────────────────────────────
    @FXML private TableView<LichSuBienDong>            tblLichSu;
    @FXML private TableColumn<LichSuBienDong, Integer> colId;
    @FXML private TableColumn<LichSuBienDong, String>  colNgay;
    @FXML private TableColumn<LichSuBienDong, String>  colNhanKhau;
    @FXML private TableColumn<LichSuBienDong, String>  colLoai;
    @FXML private TableColumn<LichSuBienDong, String>  colNguoiThuc;
    @FXML private TableColumn<LichSuBienDong, String>  colGhiChu;
    @FXML private TextField                             txtTimKiem;
    @FXML private Button                               btnXoaLog;

    // ── State ────────────────────────────────────────────────────────────────
    private final LichSuDAO   lichSuDAO   = new LichSuDAO();
    private final NhanKhauDAO nhanKhauDAO = new NhanKhauDAO();
    private final HoKhauDAO   hoKhauDAO   = new HoKhauDAO();

    private List<NhanKhau>    allNhanKhau;
    private NhanKhau          selectedNK;          // null = chưa chọn
    private Map<Integer, String> nhanKhauMap;

    private final ObservableList<LichSuBienDong> lichSuList = FXCollections.observableArrayList();
    private FilteredList<LichSuBienDong>          filteredList;

    private boolean isAdmin;
    private String  nguoiThucHien;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final List<String> LOAI_BIEN_DONG = List.of(
            "Nhập khẩu", "Chuyển đi", "Khai sinh",
            "Khai tử",   "Tách hộ",   "Nhập hộ", "Thay đổi thông tin"
    );

    // ── Init ─────────────────────────────────────────────────────────────────

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        var user      = UserSession.getInstance().getCurrentUser();
        isAdmin       = "ADMIN".equals(user.getRole());
        nguoiThucHien = user.getUsername();

        allNhanKhau = nhanKhauDAO.getAll();
        nhanKhauMap = allNhanKhau.stream()
                .collect(Collectors.toMap(NhanKhau::getId, NhanKhau::getHoTen));

        setupPermissions();
        setupAutoComplete();
        setupLeftPanel();
        setupHistoryTable();
        loadDataFromDB();
        setupSearch();
    }

    // ── Phân quyền ───────────────────────────────────────────────────────────

    private void setupPermissions() {
        if (!isAdmin) {
            txtSearchNhanKhau.setDisable(true);
            cmbLoaiBienDong  .setDisable(true);
            dtpNgayBienDong  .setDisable(true);
            txaGhiChu        .setDisable(true);
            btnLuuBienDong   .setDisable(true);
            btnLuuBienDong   .setOpacity(0.5);
        }
        btnXoaLog.setVisible(isAdmin);
        btnXoaLog.setManaged(isAdmin);
    }

    // ── Auto-complete ─────────────────────────────────────────────────────────
    // Thiết kế:
    //   - Khi chưa chọn: TextField editable, listener gợi ý
    //   - Khi đã chọn  : TextField setEditable(false) + hiện nút ✕
    //   - Bấm ✕        : reset hết, TextField editable lại
    // → Không bao giờ conflict giữa "code set text" và "user gõ"

    private void setupAutoComplete() {
        lvSuggestions.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(NhanKhau nk, boolean empty) {
                super.updateItem(nk, empty);
                if (empty || nk == null) { setText(null); return; }
                setText(nk.getHoTen()
                        + "  –  ID: " + nk.getId()
                        + (nk.getCccd() != null ? "  –  CCCD: " + nk.getCccd() : ""));
            }
        });

        // Listener chỉ chạy khi TextField đang editable (chưa lock)
        txtSearchNhanKhau.textProperty().addListener((obs, old, kw) -> {
            if (!txtSearchNhanKhau.isEditable()) return; // đang locked → bỏ qua

            hideSuggestions();

            if (kw == null || kw.isBlank()) return;

            String lower = kw.toLowerCase().trim();
            List<NhanKhau> matches = allNhanKhau.stream()
                    .filter(nk ->
                            nk.getHoTen().toLowerCase().contains(lower)
                                    || String.valueOf(nk.getId()).contains(lower)
                                    || (nk.getCccd()        != null && nk.getCccd()       .contains(lower))
                                    || (nk.getSoDienThoai() != null && nk.getSoDienThoai().contains(lower)))
                    .limit(8)
                    .collect(Collectors.toList());

            if (!matches.isEmpty()) {
                lvSuggestions.setItems(FXCollections.observableArrayList(matches));
                lvSuggestions.setVisible(true);
                lvSuggestions.setManaged(true);
            }
        });

        // Click item trong list → chọn người
        lvSuggestions.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, nk) -> {
                    if (nk == null) return;
                    onNhanKhauSelected(nk);
                });
    }

    /**
     * Chọn nhân khẩu:
     * 1. Query DB lấy dữ liệu mới nhất
     * 2. Hiện info card
     * 3. Lock TextField + hiện nút ✕
     */
    private void onNhanKhauSelected(NhanKhau fromSuggest) {
        NhanKhau fromDB = nhanKhauDAO.getById(fromSuggest.getId());
        if (fromDB == null) {
            showError("Không tìm thấy",
                    "Nhân khẩu này không còn tồn tại. Danh sách sẽ được làm mới.");
            allNhanKhau = nhanKhauDAO.getAll();
            hideSuggestions();
            return;
        }

        selectedNK = fromDB;

        // Lock TextField — set text trực tiếp không cần flag vì listener check isEditable()
        txtSearchNhanKhau.setEditable(false);
        txtSearchNhanKhau.setText(fromDB.getHoTen() + "  (ID: " + fromDB.getId() + ")");
        txtSearchNhanKhau.setStyle(
                "-fx-border-color: #0969DA; -fx-border-radius: 4; -fx-background-radius: 4;");

        hideSuggestions();

        // Hiện nút ✕
        btnClearPerson.setVisible(true);
        btnClearPerson.setManaged(true);

        // Điền info card từ DB
        lblNKTen      .setText("👤 " + fromDB.getHoTen());
        lblNKNgaySinh .setText("Ngày sinh  : "
                + (fromDB.getNgaySinh() != null ? fromDB.getNgaySinh().format(DATE_FMT) : "—"));
        lblNKGioiTinh .setText("Giới tính  : " + orDash(fromDB.getGioiTinh()));
        lblNKCccd     .setText("CCCD       : " + orDash(fromDB.getCccd()));
        lblNKHoKhau   .setText("Hộ khẩu    : " + fromDB.getHoKhauId());
        lblNKTrangThai.setText("Trạng thái : " + orDash(fromDB.getTrangThai()));
        paneNhanKhauInfo.setVisible(true);
        paneNhanKhauInfo.setManaged(true);
    }

    /** Nút ✕ — reset về trạng thái ban đầu */
    @FXML
    private void handleClearPerson() {
        selectedNK = null;

        txtSearchNhanKhau.setEditable(true);
        txtSearchNhanKhau.clear();
        txtSearchNhanKhau.setStyle(
                "-fx-border-color: #D0D7DE; -fx-border-radius: 4; -fx-background-radius: 4;");
        txtSearchNhanKhau.requestFocus();

        btnClearPerson.setVisible(false);
        btnClearPerson.setManaged(false);

        hideSuggestions();
        clearInfoCard();

        // Reset selection trong ListView để lần sau chọn lại cùng người vẫn trigger listener
        lvSuggestions.getSelectionModel().clearSelection();
    }

    private void hideSuggestions() {
        lvSuggestions.setVisible(false);
        lvSuggestions.setManaged(false);
    }

    private void clearInfoCard() {
        paneNhanKhauInfo.setVisible(false);
        paneNhanKhauInfo.setManaged(false);
    }

    private String orDash(String s) {
        return (s == null || s.isBlank()) ? "—" : s;
    }

    // ── Left panel ───────────────────────────────────────────────────────────

    private void setupLeftPanel() {
        cmbLoaiBienDong.setItems(FXCollections.observableArrayList(LOAI_BIEN_DONG));
        dtpNgayBienDong.setValue(LocalDate.now());
    }

    // ── History table ─────────────────────────────────────────────────────────

    private void setupHistoryTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        colNgay.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) { setText(null); return; }
                var l = getTableView().getItems().get(getIndex());
                setText(l.getNgayBienDong() != null ? l.getNgayBienDong().format(DATE_FMT) : "");
            }
        });

        colNhanKhau.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) { setText(null); return; }
                var l = getTableView().getItems().get(getIndex());
                setText(nhanKhauMap.getOrDefault(l.getNhanKhauId(), "ID: " + l.getNhanKhauId()));
            }
        });

        colLoai     .setCellValueFactory(new PropertyValueFactory<>("loaiBienDong"));
        colNguoiThuc.setCellValueFactory(new PropertyValueFactory<>("nguoiThucHien"));
        colGhiChu   .setCellValueFactory(new PropertyValueFactory<>("ghiChu"));

        filteredList = new FilteredList<>(lichSuList, p -> true);
        tblLichSu.setItems(filteredList);
    }

    // ── Data ─────────────────────────────────────────────────────────────────

    public void loadDataFromDB() {
        lichSuList.setAll(lichSuDAO.getAll());
    }

    // ── Search ───────────────────────────────────────────────────────────────

    private void setupSearch() {
        txtTimKiem.textProperty().addListener(
                (ObservableValue<? extends String> obs, String o, String n) -> applySearch());
    }

    private void applySearch() {
        String kw = txtTimKiem.getText() == null ? ""
                : txtTimKiem.getText().toLowerCase().trim();
        filteredList.setPredicate(l -> {
            if (kw.isEmpty()) return true;
            String tenNK = nhanKhauMap.getOrDefault(l.getNhanKhauId(), "").toLowerCase();
            if (tenNK.contains(kw)) return true;
            if (l.getLoaiBienDong()  != null && l.getLoaiBienDong() .toLowerCase().contains(kw)) return true;
            if (l.getGhiChu()        != null && l.getGhiChu()       .toLowerCase().contains(kw)) return true;
            if (l.getNguoiThucHien() != null && l.getNguoiThucHien().toLowerCase().contains(kw)) return true;
            if (l.getNgayBienDong()  != null && l.getNgayBienDong() .format(DATE_FMT).contains(kw)) return true;
            return false;
        });
    }

    // ── Save Change ───────────────────────────────────────────────────────────

    @FXML
    private void handleSaveChange() {
        if (!isAdmin) return;

        // Validate
        if (selectedNK == null) {
            showError("Thiếu thông tin",
                    "Vui lòng gõ tên và chọn nhân khẩu từ danh sách gợi ý!");
            return;
        }
        String loai = cmbLoaiBienDong.getValue();
        if (loai == null || loai.isBlank()) {
            showError("Thiếu thông tin", "Vui lòng chọn loại biến động!");
            return;
        }
        LocalDate ngay = dtpNgayBienDong.getValue();
        if (ngay == null) {
            showError("Thiếu thông tin", "Vui lòng chọn ngày thực hiện!");
            return;
        }
        if (ngay.isAfter(LocalDate.now())) {
            showError("Ngày không hợp lệ", "Ngày thực hiện không được ở tương lai!");
            return;
        }

        // Inner check: verify DB lần cuối
        NhanKhau verified = nhanKhauDAO.getById(selectedNK.getId());
        if (verified == null) {
            showError("Dữ liệu thay đổi",
                    "Nhân khẩu \"" + selectedNK.getHoTen()
                            + "\" không còn tồn tại! Vui lòng chọn lại.");
            handleClearPerson();
            return;
        }

        String ghiChu = txaGhiChu.getText() == null ? "" : txaGhiChu.getText().trim();

        // Insert
        LichSuBienDong log = new LichSuBienDong();
        log.setNhanKhauId(verified.getId());
        log.setHoKhauId(verified.getHoKhauId() > 0 ? verified.getHoKhauId() : null);
        log.setLoaiBienDong(loai);
        log.setNgayBienDong(ngay);
        log.setGhiChu(ghiChu.isEmpty() ? null : ghiChu);
        log.setNguoiThucHien(nguoiThucHien);

        boolean ok = lichSuDAO.insert(log);
        if (ok) {
            applyBienDongSideEffects(loai, verified);
            loadDataFromDB();
            clearForm();
            showInfo("Thành công", "Đã ghi nhận biến động cho: " + verified.getHoTen());
        } else {
            showError("Lỗi Database", "Không thể lưu biến động. Vui lòng thử lại.");
        }
    }

    // ── Side effects ──────────────────────────────────────────────────────────

    private void applyBienDongSideEffects(String loai, NhanKhau nk) {
        switch (loai) {
            case "Khai tử" -> {
                nhanKhauDAO.updateTrangThai(nk.getId(), "DECEASED");
                if (nk.getHoKhauId() > 0) decreaseSoThanhVien(nk.getHoKhauId());
            }
            case "Chuyển đi" -> {
                nhanKhauDAO.updateTrangThai(nk.getId(), "MOVED");
                if (nk.getHoKhauId() > 0) decreaseSoThanhVien(nk.getHoKhauId());
            }
            case "Nhập khẩu", "Khai sinh", "Nhập hộ" -> {
                nhanKhauDAO.updateTrangThai(nk.getId(), "ACTIVE");
                if (nk.getHoKhauId() > 0) {
                    HoKhau hk = hoKhauDAO.getAllHoKhau().stream()
                        .filter(h -> h.getId() == nk.getHoKhauId()).findFirst().orElse(null);
                    if (hk != null) hoKhauDAO.updateSoThanhVien(hk.getId(), hk.getSoThanhVien() + 1);
                }
            }
            default -> { /* Tách hộ, Thay đổi thông tin — không thay đổi tự động */ }
        }
    }

    private void decreaseSoThanhVien(int hoKhauId) {
        HoKhau hk = hoKhauDAO.getAllHoKhau().stream()
            .filter(h -> h.getId() == hoKhauId).findFirst().orElse(null);
        if (hk != null) hoKhauDAO.updateSoThanhVien(hk.getId(), hk.getSoThanhVien() - 1);
    }

    // ── Delete Log ────────────────────────────────────────────────────────────

    @FXML
    private void handleDeleteLog() {
        if (!isAdmin) return;

        LichSuBienDong selected = tblLichSu.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarn("Chưa chọn", "Vui lòng chọn một dòng lịch sử để xóa.");
            return;
        }

        if (lichSuDAO.delete(selected.getId())) {
            loadDataFromDB();
        } else {
            showError("Lỗi", "Không thể xóa bản ghi này.");
        }
    }

    // ── Clear form ────────────────────────────────────────────────────────────

    private void clearForm() {
        handleClearPerson();
        cmbLoaiBienDong.setValue(null);
        dtpNgayBienDong.setValue(LocalDate.now());
        txaGhiChu.clear();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
    private void showWarn(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}
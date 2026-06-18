package controllers;

import application.UserSession;
import models.HoKhau;
import models.KhoanThu;
import models.NhanKhau;
import models.NopTien;
import services.HoKhauDAO;
import services.KhoanThuDAO;
import services.NhanKhauDAO;
import services.NopTienDAO;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller cho màn hình Lịch sử nộp phí (LichSuNopTien.fxml).
 * FIX: Hiển thị tên khoản thu và mã hộ khẩu thay vì raw ID.
 */
public class LichSuNopTienController implements Initializable {

    @FXML private ComboBox<KhoanThu> cmbLocKhoanThu;
    @FXML private ComboBox<HoKhau>   cmbLocHoKhau;
    @FXML private TextField           txtTimKiem;
    @FXML private Button              btnLamMoi;

    @FXML private TableView<NopTien>             tblLichSu;
    @FXML private TableColumn<NopTien, Integer>  colStt;
    @FXML private TableColumn<NopTien, String>   colKhoanThuId;   // Hiển thị tên khoản thu
    @FXML private TableColumn<NopTien, String>   colHoKhauId;     // Hiển thị mã hộ
    @FXML private TableColumn<NopTien, BigDecimal> colSoTien;
    @FXML private TableColumn<NopTien, String>   colNgayNop;      // Hiển thị dd/MM/yyyy
    @FXML private TableColumn<NopTien, String>   colNguoiThu;
    @FXML private TableColumn<NopTien, String>   colGhiChu;
    @FXML private TableColumn<NopTien, Void>     colHanhDong;

    private final NopTienDAO  nopTienDAO  = new NopTienDAO();
    private final KhoanThuDAO khoanThuDAO = new KhoanThuDAO();
    private final HoKhauDAO   hoKhauDAO   = new HoKhauDAO();
    private final NhanKhauDAO nhanKhauDAO = new NhanKhauDAO();
    private boolean isAdmin;

    private ObservableList<NopTien> allData;
    private FilteredList<NopTien>   filteredData;

    /** Cache id → tên khoản thu */
    private Map<Integer, String> khoanThuMap;
    /** Cache id → mã hộ */
    private Map<Integer, String> hoKhauMap;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        isAdmin = "ADMIN".equals(UserSession.getInstance().getCurrentUser().getRole());
        buildLookupMaps();
        setupColumns();
        loadComboBoxes();
        loadAllData();
        setupFilters();
    }

    // ── Build lookup maps ────────────────────────────────────────────────────

    private void buildLookupMaps() {
        khoanThuMap = khoanThuDAO.getAllKhoanThu().stream()
                .collect(Collectors.toMap(KhoanThu::getId, KhoanThu::getTenKhoan, (a, b) -> a));

        // Map id chủ hộ -> tên, để hiển thị "số phòng – tên chủ hộ" thay vì ID
        Map<Integer, String> tenChuHo = nhanKhauDAO.getAll().stream()
                .collect(Collectors.toMap(NhanKhau::getId, NhanKhau::getHoTen, (a, b) -> a));
        hoKhauMap = new java.util.HashMap<>();
        for (HoKhau hk : hoKhauDAO.getAllHoKhau()) {
            String phong = (hk.getDiaChi() != null && !hk.getDiaChi().isBlank())
                    ? hk.getDiaChi() : hk.getMaHo();
            String owner = hk.getChuHoId() != null ? tenChuHo.get(hk.getChuHoId()) : null;
            hoKhauMap.put(hk.getId(), owner != null ? phong + " – " + owner : phong);
        }
    }

    // ── Setup columns ────────────────────────────────────────────────────────

    private void setupColumns() {
        colStt.setCellValueFactory(new PropertyValueFactory<>("id"));

        // FIX: Hiển thị tên khoản thu thay vì raw ID
        colKhoanThuId.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) { setText(null); return; }
                int ktId = getTableView().getItems().get(getIndex()).getKhoanThuId();
                setText(khoanThuMap.getOrDefault(ktId, "ID: " + ktId));
            }
        });

        // FIX: Hiển thị mã hộ khẩu thay vì raw ID
        colHoKhauId.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) { setText(null); return; }
                int hkId = getTableView().getItems().get(getIndex()).getHoKhauId();
                setText(hoKhauMap.getOrDefault(hkId, "ID: " + hkId));
            }
        });

        colSoTien.setCellValueFactory(new PropertyValueFactory<>("soTien"));

        // FIX: Hiển thị ngày dạng dd/MM/yyyy
        colNgayNop.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) { setText(null); return; }
                LocalDate d = getTableView().getItems().get(getIndex()).getNgayNop();
                setText(d != null ? d.format(DATE_FMT) : "—");
            }
        });

        colNguoiThu.setCellValueFactory(new PropertyValueFactory<>("nguoiThu"));
        colGhiChu.setCellValueFactory(new PropertyValueFactory<>("ghiChu"));

        colHanhDong.setCellFactory(col -> new TableCell<>() {
            private final Button btnXoa = new Button("Xóa");
            private final HBox   box    = new HBox(btnXoa);
            {
                box.setAlignment(Pos.CENTER);
                btnXoa.setStyle("-fx-background-color:#82071E;-fx-text-fill:white;"
                        + "-fx-background-radius:4;-fx-cursor:hand;");
                btnXoa.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || !isAdmin ? null : box);
            }
        });
    }

    private void handleDelete(NopTien nt) {
        String info = khoanThuMap.getOrDefault(nt.getKhoanThuId(), "ID:" + nt.getKhoanThuId())
                + " | " + hoKhauMap.getOrDefault(nt.getHoKhauId(), "HK:" + nt.getHoKhauId());
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText(null);
        confirm.setContentText("Xóa bản ghi nộp tiền: " + info + "?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (nopTienDAO.xoaNopTien(nt.getId())) {
                onLamMoiClick();
            } else {
                new Alert(Alert.AlertType.ERROR) {{
                    setHeaderText(null);
                    setContentText("Không thể xóa bản ghi này.");
                    showAndWait();
                }};
            }
        }
    }

    // ── ComboBoxes ───────────────────────────────────────────────────────────

    private void loadComboBoxes() {
        ObservableList<KhoanThu> ktList = FXCollections.observableArrayList();
        ktList.add(null);
        ktList.addAll(khoanThuDAO.getAllKhoanThu());
        cmbLocKhoanThu.setItems(ktList);
        cmbLocKhoanThu.setCellFactory(lv -> cellKT());
        cmbLocKhoanThu.setButtonCell(cellKT());
        cmbLocKhoanThu.getSelectionModel().selectFirst();

        ObservableList<HoKhau> hkList = FXCollections.observableArrayList();
        hkList.add(null);
        hkList.addAll(hoKhauDAO.getAllHoKhau());
        cmbLocHoKhau.setItems(hkList);
        cmbLocHoKhau.setCellFactory(lv -> cellHK());
        cmbLocHoKhau.setButtonCell(cellHK());
        cmbLocHoKhau.getSelectionModel().selectFirst();
    }

    private ListCell<KhoanThu> cellKT() {
        return new ListCell<>() {
            @Override protected void updateItem(KhoanThu kt, boolean empty) {
                super.updateItem(kt, empty);
                setText(empty ? null : (kt == null ? "— Tất cả —" : kt.getMaKhoan() + " – " + kt.getTenKhoan()));
            }
        };
    }

    private ListCell<HoKhau> cellHK() {
        return new ListCell<>() {
            @Override protected void updateItem(HoKhau hk, boolean empty) {
                super.updateItem(hk, empty);
                setText(empty ? null : (hk == null ? "— Tất cả —" : hk.getMaHo() + " – " + hk.getDiaChi()));
            }
        };
    }

    // ── Data & Filters ───────────────────────────────────────────────────────

    private void loadAllData() {
        allData = FXCollections.observableArrayList(nopTienDAO.getAllNopTien());
    }

    private void setupFilters() {
        // FIX: Tạo FilteredList từ allData và gán vào bảng
        filteredData = new FilteredList<>(allData, p -> true);
        tblLichSu.setItems(filteredData);

        Runnable apply = () -> filteredData.setPredicate(nt -> {
            KhoanThu selKt = cmbLocKhoanThu.getValue();
            HoKhau   selHk = cmbLocHoKhau.getValue();
            String kw = txtTimKiem.getText() == null ? "" : txtTimKiem.getText().toLowerCase().trim();
            if (selKt != null && nt.getKhoanThuId() != selKt.getId()) return false;
            if (selHk != null && nt.getHoKhauId()   != selHk.getId()) return false;
            if (!kw.isEmpty()) {
                boolean m1 = nt.getNguoiThu() != null && nt.getNguoiThu().toLowerCase().contains(kw);
                boolean m2 = nt.getGhiChu()   != null && nt.getGhiChu().toLowerCase().contains(kw);
                // Tìm theo tên khoản thu và mã hộ
                String tenKt = khoanThuMap.getOrDefault(nt.getKhoanThuId(), "");
                String maHo  = hoKhauMap.getOrDefault(nt.getHoKhauId(), "");
                boolean m3 = tenKt.toLowerCase().contains(kw);
                boolean m4 = maHo.toLowerCase().contains(kw);
                if (!m1 && !m2 && !m3 && !m4) return false;
            }
            return true;
        });
        cmbLocKhoanThu.setOnAction(e -> apply.run());
        cmbLocHoKhau.setOnAction(e -> apply.run());
        txtTimKiem.textProperty().addListener((obs, o, n) -> apply.run());
    }

    @FXML
    private void onLamMoiClick() {
        // FIX: reload data thật rồi cập nhật FilteredList đang bind với bảng
        loadAllData();
        filteredData = new FilteredList<>(allData, p -> true);
        tblLichSu.setItems(filteredData);
        cmbLocKhoanThu.getSelectionModel().selectFirst();
        cmbLocHoKhau.getSelectionModel().selectFirst();
        txtTimKiem.clear();
        setupFilters();
    }
}

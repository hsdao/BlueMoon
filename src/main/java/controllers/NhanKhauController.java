package controllers;

import models.HoKhau;
import models.NhanKhau;
import services.HoKhauDAO;
import services.NhanKhauDAO;
import services.QuanHeDAO;
import models.QuanHe;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import application.UserSession;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class NhanKhauController implements Initializable {

    // ── FXML bindings ─────────────────────────────────────────────────────────
    @FXML private TableView<NhanKhau>            tableNhanKhau;
    @FXML private TableColumn<NhanKhau, String>  colHoKhauId;  // hiển thị mã PHÒNG
    @FXML private TableColumn<NhanKhau, String>  colHoTen;
    @FXML private TableColumn<NhanKhau, String>  colNgaySinh;   // custom cell
    @FXML private TableColumn<NhanKhau, String>  colGioiTinh;
    @FXML private TableColumn<NhanKhau, String>  colCccd;
    @FXML private TableColumn<NhanKhau, String>  colSoDienThoai;
    @FXML private TableColumn<NhanKhau, String>  colQuanHe;     // hiển thị tên
    @FXML private TableColumn<NhanKhau, String>  colTrangThai;
    @FXML private TableColumn<NhanKhau, Void>    colHanhDong;

    @FXML private TextField        searchField;
    @FXML private ComboBox<HoKhau> cmbFilterHoKhau;
    @FXML private Button           btnThemMoi;

    // ── State ─────────────────────────────────────────────────────────────────
    private final ObservableList<NhanKhau> nhanKhauList = FXCollections.observableArrayList();
    private FilteredList<NhanKhau>         filteredList;

    private final NhanKhauDAO nhanKhauDAO = new NhanKhauDAO();
    private final HoKhauDAO   hoKhauDAO   = new HoKhauDAO();
    private final QuanHeDAO   quanHeDAO   = new QuanHeDAO();

    /** Map quanHeId → tenQuanHe để hiển thị trong cột Quan hệ */
    private Map<Integer, String> quanHeMap;
    /** Map hoKhauId → soThanhVien để validate khi thêm nhân khẩu */
    private Map<Integer, Integer> hoKhauSoTvMap;
    /** Map hoKhauId → mã phòng (ma_ho) để hiển thị cột Phòng thay vì id */
    private Map<Integer, String> hoKhauPhongMap = new java.util.HashMap<>();

    private boolean isAdmin;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ── Init ──────────────────────────────────────────────────────────────────

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Phân quyền
        isAdmin = "ADMIN".equals(UserSession.getInstance().getCurrentUser().getRole());
        // STAFF không được thêm/sửa/xóa nhân khẩu
        btnThemMoi.setVisible(isAdmin);
        btnThemMoi.setManaged(isAdmin);

        // Lookup maps
        quanHeMap = quanHeDAO.getAll().stream()
                .collect(Collectors.toMap(QuanHe::getId, QuanHe::getTenQuanHe));

        setupColumns();
        setupActionColumn();
        loadDataFromDB();
        setupFilterCombo();
        setupSearch();
    }

    // ── Columns ───────────────────────────────────────────────────────────────

    private void setupColumns() {
        colHoTen      .setCellValueFactory(new PropertyValueFactory<>("hoTen"));
        colGioiTinh   .setCellValueFactory(new PropertyValueFactory<>("gioiTinh"));
        colCccd       .setCellValueFactory(new PropertyValueFactory<>("cccd"));
        colSoDienThoai.setCellValueFactory(new PropertyValueFactory<>("soDienThoai"));
        colTrangThai  .setCellValueFactory(new PropertyValueFactory<>("trangThaiLabel"));

        // Cột Phòng: tra map hoKhauId -> mã phòng (không hiện id thô)
        colHoKhauId.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setText(null); return;
                }
                NhanKhau nk = getTableView().getItems().get(getIndex());
                setText(hoKhauPhongMap.getOrDefault(nk.getHoKhauId(), "—"));
            }
        });

        // Ngày sinh: format dd/MM/yyyy
        colNgaySinh.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setText(null); return;
                }
                NhanKhau nk = getTableView().getItems().get(getIndex());
                setText(nk.getNgaySinh() != null ? nk.getNgaySinh().format(DATE_FMT) : "");
            }
        });

        // Quan hệ: tra map → hiển thị tên thay vì id
        colQuanHe.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setText(null); return;
                }
                NhanKhau nk = getTableView().getItems().get(getIndex());
                setText(quanHeMap.getOrDefault(nk.getQuanHeId(), ""));
            }
        });
    }

    private void setupActionColumn() {
        Callback<TableColumn<NhanKhau, Void>, TableCell<NhanKhau, Void>> factory = param ->
                new TableCell<>() {
                    private final Button btnEdit   = new Button("Sửa");
                    private final Button btnDelete = new Button("Xóa");
                    private final HBox   pane      = new HBox(8, btnEdit, btnDelete);
                    {
                        pane.setAlignment(Pos.CENTER);
                        btnEdit.setStyle("-fx-background-color: #0969DA; -fx-text-fill: white;"
                                + " -fx-background-radius: 4; -fx-cursor: hand;");
                        btnDelete.setStyle("-fx-background-color: #82071E; -fx-text-fill: white;"
                                + " -fx-background-radius: 4; -fx-cursor: hand;");

                        // Chỉ ADMIN mới thấy và dùng được nút Sửa / Xóa
                        btnEdit.setVisible(isAdmin);
                        btnEdit.setManaged(isAdmin);
                        btnDelete.setVisible(isAdmin);
                        btnDelete.setManaged(isAdmin);

                        btnEdit.setOnAction(e ->
                                openFormDialog(getTableView().getItems().get(getIndex())));
                        btnDelete.setOnAction(e ->
                                handleDelete(getTableView().getItems().get(getIndex())));
                    }
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : pane);
                    }
                };
        colHanhDong.setCellFactory(factory);
    }

    // ── Data ──────────────────────────────────────────────────────────────────

    /** Gọi sau mỗi thao tác CRUD. setAll() → filteredList tự re-evaluate. */
    public void loadDataFromDB() {
        hoKhauDAO.recomputeAllSoThanhVien();   // số thành viên = số nhân khẩu thực tế
        nhanKhauList.setAll(nhanKhauDAO.getAll());
        // Rebuild các map tra cứu hộ khẩu (số thành viên để validate + mã phòng để hiển thị)
        List<HoKhau> hos = hoKhauDAO.getAllHoKhau();
        hoKhauSoTvMap = hos.stream()
                .collect(Collectors.toMap(HoKhau::getId, HoKhau::getSoThanhVien, (a, b) -> a));
        hoKhauPhongMap = hos.stream()
                .collect(Collectors.toMap(HoKhau::getId, HoKhau::getMaHo, (a, b) -> a));
    }

    // ── Filter combo ──────────────────────────────────────────────────────────

    private void setupFilterCombo() {
        List<HoKhau> list = hoKhauDAO.getAllHoKhau();
        ObservableList<HoKhau> obs = FXCollections.observableArrayList();
        obs.add(null);          // "Tất cả"
        obs.addAll(list);
        cmbFilterHoKhau.setItems(obs);

        cmbFilterHoKhau.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(HoKhau hk, boolean empty) {
                super.updateItem(hk, empty);
                setText(empty ? null : formatHoKhau(hk));
            }
        });
        cmbFilterHoKhau.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(HoKhau hk, boolean empty) {
                super.updateItem(hk, empty);
                setText(empty ? null : formatHoKhau(hk));
            }
        });
        cmbFilterHoKhau.getSelectionModel().selectFirst();

        // Tạo filteredList ở đây (trước setupSearch)
        filteredList = new FilteredList<>(nhanKhauList, p -> true);
        tableNhanKhau.setItems(filteredList);

        cmbFilterHoKhau.valueProperty().addListener((obs2, old, nw) -> applyFilters());
    }

    private String formatHoKhau(HoKhau hk) {
        if (hk == null) return "Tất cả hộ khẩu";
        return hk.getMaHo() + " – " + hk.getDiaChi();
    }

    // ── Search ────────────────────────────────────────────────────────────────

    private void setupSearch() {
        searchField.textProperty().addListener(
                (ObservableValue<? extends String> obs, String o, String n) -> applyFilters());
    }

    private void applyFilters() {
        if (filteredList == null) return;
        HoKhau selectedHK = cmbFilterHoKhau.getValue();
        String kw = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();

        filteredList.setPredicate(nk -> {
            // Filter 1: hộ khẩu
            if (selectedHK != null && nk.getHoKhauId() != selectedHK.getId()) return false;
            // Filter 2: từ khoá — tìm trên tất cả cột hiển thị
            if (!kw.isEmpty()) {
                boolean m1 = contains(nk.getHoTen(),      kw);
                boolean m2 = contains(nk.getCccd(),       kw);
                boolean m3 = contains(nk.getSoDienThoai(),kw);
                boolean m4 = contains(nk.getGioiTinh(),   kw);
                boolean m5 = contains(nk.getTrangThai(),  kw);
                // Ngày sinh dạng dd/MM/yyyy
                boolean m6 = nk.getNgaySinh() != null
                        && nk.getNgaySinh().format(DATE_FMT).contains(kw);
                // Quan hệ: tra map lấy tên
                String tenQH = quanHeMap.getOrDefault(nk.getQuanHeId(), "");
                boolean m7 = tenQH.toLowerCase().contains(kw);

                if (!m1 && !m2 && !m3 && !m4 && !m5 && !m6 && !m7) return false;
            }
            return true;
        });
    }

    /** Null-safe contains (case-insensitive). */
    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword);
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @FXML
    private void handleAdd() { openFormDialog(null); }

    private void openFormDialog(NhanKhau nk) {
        try {
            java.net.URL fxmlUrl = getClass().getResource("/views/NhanKhauForm.fxml");
            if (fxmlUrl == null) {
                fxmlUrl = Thread.currentThread().getContextClassLoader()
                        .getResource("views/NhanKhauForm.fxml");
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            NhanKhauFormController fc = loader.getController();
            fc.setParentController(this);
            if (nk != null) fc.setEditData(nk);
            else            fc.setAddMode();

            Stage stage = new Stage();
            stage.setTitle(nk == null ? "Thêm Nhân Khẩu Mới" : "Sửa Thông Tin Nhân Khẩu");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Không thể mở form: " + e.getMessage()).showAndWait();
        }
    }

    private void handleDelete(NhanKhau nk) {
        // Chặn xóa nếu nhân khẩu đang là CHỦ HỘ của một hộ (tránh hộ bị treo chủ hộ)
        boolean laChuHo = hoKhauDAO.getAllHoKhau().stream()
                .anyMatch(h -> h.getChuHoId() != null && h.getChuHoId() == nk.getId());
        if (laChuHo) {
            new Alert(Alert.AlertType.WARNING,
                    "\"" + nk.getHoTen() + "\" đang là CHỦ HỘ. Hãy đổi chủ hộ khác cho hộ này "
                    + "(sửa hộ khẩu) trước khi xóa nhân khẩu.").showAndWait();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText(null);
        confirm.setContentText("Bạn có chắc muốn xóa nhân khẩu \""
                + nk.getHoTen() + "\"?\nHành động này không thể hoàn tác.");

        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            if (nhanKhauDAO.delete(nk.getId())) {
                loadDataFromDB();
            } else {
                new Alert(Alert.AlertType.ERROR,
                        "Không thể xóa. Có thể do ràng buộc dữ liệu.").showAndWait();
            }
        }
    }
}
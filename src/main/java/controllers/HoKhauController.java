package controllers;

import application.UserSession;
import models.HoKhau;
import services.HoKhauDAO;
import services.NhanKhauDAO;

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

import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class HoKhauController implements Initializable {

    // ── FXML bindings ─────────────────────────────────────────────────────────
    @FXML private TableView<HoKhau>            tableHoKhau;
    @FXML private TableColumn<HoKhau, String>  colMaHo;
    @FXML private TableColumn<HoKhau, String>  colChuHo;   // Hiển thị tên, không phải ID
    @FXML private TableColumn<HoKhau, String>  colSdt;
    @FXML private TableColumn<HoKhau, String>  colDiaChi;
    @FXML private TableColumn<HoKhau, Integer> colSoThanhVien;
    @FXML private TableColumn<HoKhau, Timestamp> colNgayTao;
    @FXML private TableColumn<HoKhau, String>  colTrangThai;
    @FXML private TableColumn<HoKhau, Void>    colHanhDong;
    @FXML private TableColumn<HoKhau, String>  colGhiChu;
    @FXML private TextField searchField;
    @FXML private Button btnThemMoi;

    // ── State ─────────────────────────────────────────────────────────────────
    private final ObservableList<HoKhau> hoKhauList = FXCollections.observableArrayList();
    private FilteredList<HoKhau> filteredList;
    private final HoKhauDAO    dao         = new HoKhauDAO();
    private final NhanKhauDAO  nhanKhauDAO = new NhanKhauDAO();
    /** Cache chuHoId → hoTen để render cột Chủ Hộ */
    private Map<Integer, String> tenNhanKhauMap;
    private boolean isAdmin;

    // ── Init ──────────────────────────────────────────────────────────────────

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        isAdmin = "ADMIN".equals(UserSession.getInstance().getCurrentUser().getRole());

        btnThemMoi.setVisible(isAdmin);
        btnThemMoi.setManaged(isAdmin);

        buildTenNhanKhauMap();
        setupTable();
        loadDataFromDB();
        setupSearch();
    }

    /** Nạp map id→tên nhân khẩu để hiển thị cột chủ hộ */
    private void buildTenNhanKhauMap() {
        tenNhanKhauMap = nhanKhauDAO.getAll().stream()
                .collect(Collectors.toMap(
                        nk -> nk.getId(),
                        nk -> nk.getHoTen(),
                        (a, b) -> a  // xử lý key trùng (không xảy ra trong thực tế)
                ));
    }

    // ── Table ─────────────────────────────────────────────────────────────────

    private void setupTable() {
        colMaHo.setCellValueFactory(new PropertyValueFactory<>("maHo"));
        colSdt.setCellValueFactory(new PropertyValueFactory<>("soDienThoaiChuHo"));
        colDiaChi.setCellValueFactory(new PropertyValueFactory<>("diaChi"));
        colSoThanhVien.setCellValueFactory(new PropertyValueFactory<>("soThanhVien"));
        colNgayTao.setCellValueFactory(new PropertyValueFactory<>("ngayTao"));
        colTrangThai.setCellValueFactory(new PropertyValueFactory<>("trangThaiLabel"));
        colGhiChu.setCellValueFactory(new PropertyValueFactory<>("ghiChu"));

        // FIX: Cột Chủ Hộ hiển thị tên người thay vì raw ID
        colChuHo.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setText(null);
                    return;
                }
                HoKhau hk = getTableView().getItems().get(getIndex());
                if (hk.getChuHoId() == null) {
                    setText("—");
                } else {
                    setText(tenNhanKhauMap.getOrDefault(hk.getChuHoId(), "ID: " + hk.getChuHoId()));
                }
            }
        });

        colNgayTao.setCellFactory(col -> new TableCell<>() {
            private final SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy");
            @Override
            protected void updateItem(Timestamp item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : fmt.format(item));
            }
        });

        setupActionColumn();
    }

    private void setupActionColumn() {
        Callback<TableColumn<HoKhau, Void>, TableCell<HoKhau, Void>> factory = param -> new TableCell<>() {
            private final Button btnEdit   = new Button("Sửa");
            private final Button btnDelete = new Button("Xóa");
            private final HBox   pane      = new HBox(10, btnEdit, btnDelete);
            {
                pane.setAlignment(Pos.CENTER);
                btnEdit.setStyle("-fx-background-color: #0969DA; -fx-text-fill: white;"
                        + " -fx-background-radius: 4; -fx-cursor: hand;");
                btnDelete.setStyle("-fx-background-color: #82071E; -fx-text-fill: white;"
                        + " -fx-background-radius: 4; -fx-cursor: hand;");

                btnEdit.setVisible(isAdmin);
                btnEdit.setManaged(isAdmin);
                btnDelete.setVisible(isAdmin);
                btnDelete.setManaged(isAdmin);

                btnEdit.setOnAction(e -> openFormDialog(getTableView().getItems().get(getIndex())));
                btnDelete.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
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

    public void loadDataFromDB() {
        dao.recomputeAllSoThanhVien();   // số thành viên = số nhân khẩu thực tế (tự tính)
        buildTenNhanKhauMap(); // refresh cache khi reload
        List<HoKhau> dbList = dao.getAllHoKhau();
        hoKhauList.setAll(dbList);
    }

    // ── Search ────────────────────────────────────────────────────────────────

    private void setupSearch() {
        filteredList = new FilteredList<>(hoKhauList, p -> true);

        searchField.textProperty().addListener(
                (ObservableValue<? extends String> obs, String oldVal, String newVal) -> {
                    filteredList.setPredicate(hk -> {
                        if (newVal == null || newVal.isEmpty()) return true;
                        String kw = newVal.toLowerCase();
                        if (hk.getMaHo() != null && hk.getMaHo().toLowerCase().contains(kw)) return true;
                        if (hk.getDiaChi() != null && hk.getDiaChi().toLowerCase().contains(kw)) return true;
                        if (hk.getSoDienThoaiChuHo() != null && hk.getSoDienThoaiChuHo().contains(kw)) return true;
                        if (hk.getTrangThai() != null && hk.getTrangThai().toLowerCase().contains(kw)) return true;
                        // Tìm theo tên chủ hộ
                        if (hk.getChuHoId() != null) {
                            String ten = tenNhanKhauMap.getOrDefault(hk.getChuHoId(), "");
                            if (ten.toLowerCase().contains(kw)) return true;
                        }
                        return false;
                    });
                });

        tableHoKhau.setItems(filteredList);
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @FXML
    private void handleAdd() {
        if (!isAdmin) return;
        openFormDialog(null);
    }

    private void openFormDialog(HoKhau hoKhau) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/HoKhauForm.fxml"));
            Parent root = loader.load();

            HoKhauFormController fc = loader.getController();
            fc.setParentController(this);
            if (hoKhau != null) fc.setEditData(hoKhau);
            else                fc.setAddMode();

            Stage stage = new Stage();
            stage.setTitle(hoKhau == null ? "Thêm mới Hộ Khẩu" : "Sửa Hộ Khẩu");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Không thể mở form hộ khẩu:\n" + e.getMessage()).showAndWait();
        }
    }

    private void handleDelete(HoKhau hk) {
        // Chặn xóa khi hộ còn nhân khẩu (tránh để lại nhân khẩu "mồ côi" không thuộc hộ nào)
        int soNK = nhanKhauDAO.findByHoKhau(hk.getId()).size();
        if (soNK > 0) {
            new Alert(Alert.AlertType.WARNING, "Hộ \"" + hk.getMaHo() + "\" còn " + soNK
                    + " nhân khẩu.\nHãy chuyển hoặc xóa hết nhân khẩu trong hộ trước khi xóa hộ khẩu.")
                    .showAndWait();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Kiểm tra trước khi xóa");
        confirm.setContentText(
                "Mã hộ khẩu : " + hk.getMaHo() + "\n"
                        + "Địa chỉ    : " + hk.getDiaChi() + "\n"
                        + "Trạng thái : " + hk.getTrangThai() + "\n\n"
                        + "Hộ này không còn nhân khẩu. Xóa luôn các lịch sử/nộp tiền liên quan.\n"
                        + "Không thể hoàn tác. Bạn có chắc chắn?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (dao.xoaHoKhau(hk.getId())) {
                loadDataFromDB();
            } else {
                new Alert(Alert.AlertType.ERROR,
                        "Không thể xóa hộ khẩu. Vui lòng thử lại.").showAndWait();
            }
        }
    }
}
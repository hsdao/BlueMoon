package controllers;

import application.UserSession;
import models.HoKhau;
import services.HoKhauDAO;

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
import java.util.Optional;
import java.util.ResourceBundle;

public class HoKhauController implements Initializable {

    // ── FXML bindings ─────────────────────────────────────────────────────────
    @FXML private TableView<HoKhau>              tableHoKhau;
    @FXML private TableColumn<HoKhau, String>    colMaHo;
    @FXML private TableColumn<HoKhau, String>    colChuHo;
    @FXML private TableColumn<HoKhau, String>    colSdt;
    @FXML private TableColumn<HoKhau, String>    colDiaChi;
    @FXML private TableColumn<HoKhau, Integer>   colSoThanhVien;
    @FXML private TableColumn<HoKhau, Timestamp> colNgayTao;
    @FXML private TableColumn<HoKhau, String>    colTrangThai;
    @FXML private TableColumn<HoKhau, Void>      colHanhDong;
    @FXML private TableColumn<HoKhau, String>    colGhiChu;
    @FXML private TextField                       searchField;
    @FXML private Button                          btnThemMoi;

    // ── State ─────────────────────────────────────────────────────────────────
    private final ObservableList<HoKhau> hoKhauList  = FXCollections.observableArrayList();
    private FilteredList<HoKhau>         filteredList;
    private final HoKhauDAO              dao         = new HoKhauDAO();
    private boolean                      isAdmin;

    // ── Init ──────────────────────────────────────────────────────────────────

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        isAdmin = "ADMIN".equals(UserSession.getInstance().getCurrentUser().getRole());

        // STAFF: ẩn nút Thêm mới
        btnThemMoi.setVisible(isAdmin);
        btnThemMoi.setManaged(isAdmin);

        setupTable();
        loadDataFromDB();
        setupSearch();      // gọi 1 lần duy nhất — tạo filteredList và bind table
    }

    // ── Table ─────────────────────────────────────────────────────────────────

    private void setupTable() {
        colMaHo       .setCellValueFactory(new PropertyValueFactory<>("maHo"));
        colChuHo      .setCellValueFactory(new PropertyValueFactory<>("chuHoId"));
        colSdt        .setCellValueFactory(new PropertyValueFactory<>("soDienThoaiChuHo"));
        colDiaChi     .setCellValueFactory(new PropertyValueFactory<>("diaChi"));
        colSoThanhVien.setCellValueFactory(new PropertyValueFactory<>("soThanhVien"));
        colNgayTao    .setCellValueFactory(new PropertyValueFactory<>("ngayTao"));
        colTrangThai  .setCellValueFactory(new PropertyValueFactory<>("trangThai"));
        colGhiChu     .setCellValueFactory(new PropertyValueFactory<>("ghiChu"));

        colNgayTao.setCellFactory(col -> new TableCell<>() {
            private final SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy");
            @Override protected void updateItem(Timestamp item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : fmt.format(item));
            }
        });

        setupActionColumn();
    }

    private void setupActionColumn() {
        Callback<TableColumn<HoKhau, Void>, TableCell<HoKhau, Void>> factory = param ->
                new TableCell<>() {
                    private final Button btnEdit   = new Button("Sửa");
                    private final Button btnDelete = new Button("Xóa");
                    private final HBox   pane      = new HBox(10, btnEdit, btnDelete);
                    {
                        pane.setAlignment(Pos.CENTER);
                        btnEdit.setStyle("-fx-background-color: #0969DA; -fx-text-fill: white;"
                                + " -fx-background-radius: 4; -fx-cursor: hand;");
                        btnDelete.setStyle("-fx-background-color: #82071E; -fx-text-fill: white;"
                                + " -fx-background-radius: 4; -fx-cursor: hand;");

                        // Chỉ ADMIN thấy Sửa / Xóa
                        btnEdit.setVisible(isAdmin);
                        btnEdit.setManaged(isAdmin);
                        btnDelete.setVisible(isAdmin);
                        btnDelete.setManaged(isAdmin);

                        btnEdit.setOnAction(e ->
                                openFormDialog(getTableView().getItems().get(getIndex())));
                        btnDelete.setOnAction(e ->
                                handleDelete(getTableView().getItems().get(getIndex())));
                    }
                    @Override protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : pane);
                    }
                };
        colHanhDong.setCellFactory(factory);
    }

    // ── Data ──────────────────────────────────────────────────────────────────

    /**
     * Dùng setAll() để FilteredList tự re-evaluate — KHÔNG tạo list mới,
     * KHÔNG gọi lại setupSearch().
     */
    public void loadDataFromDB() {
        List<HoKhau> dbList = dao.getAllHoKhau();
        hoKhauList.setAll(dbList);
    }

    // ── Search ────────────────────────────────────────────────────────────────

    /** Gọi 1 lần duy nhất trong initialize(). */
    private void setupSearch() {
        filteredList = new FilteredList<>(hoKhauList, p -> true);

        searchField.textProperty().addListener(
                (ObservableValue<? extends String> obs, String oldVal, String newVal) -> {
                    filteredList.setPredicate(hk -> {
                        if (newVal == null || newVal.isEmpty()) return true;
                        String kw = newVal.toLowerCase();
                        if (hk.getMaHo()            != null && hk.getMaHo()           .toLowerCase().contains(kw)) return true;
                        if (hk.getDiaChi()           != null && hk.getDiaChi()          .toLowerCase().contains(kw)) return true;
                        if (hk.getSoDienThoaiChuHo() != null && hk.getSoDienThoaiChuHo().contains(kw))              return true;
                        if (hk.getTrangThai()        != null && hk.getTrangThai()       .toLowerCase().contains(kw)) return true;
                        return false;
                    });
                });

        tableHoKhau.setItems(filteredList);
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @FXML
    private void handleAdd() {
        if (!isAdmin) return; // guard phòng hờ
        openFormDialog(null);
    }

    private void openFormDialog(HoKhau hoKhau) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/HoKhauForm.fxml"));
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
            new Alert(Alert.AlertType.ERROR,
                    "Không thể mở form hộ khẩu:\n" + e.getMessage()).showAndWait();
        }
    }

    private void handleDelete(HoKhau hk) {
        // Hiển thị thông tin trước khi xóa
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Kiểm tra trước khi xóa");
        confirm.setContentText(
                "Mã hộ khẩu : " + hk.getMaHo() + "\n"
                        + "Địa chỉ    : " + hk.getDiaChi() + "\n"
                        + "Số TV      : " + hk.getSoThanhVien() + "\n"
                        + "Trạng thái : " + hk.getTrangThai() + "\n\n"
                        + "⚠ Hành động này sẽ xóa toàn bộ nhân khẩu thuộc hộ này!\n"
                        + "Không thể hoàn tác. Bạn có chắc chắn?"
        );

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (dao.xoaHoKhau(hk.getId())) {
                loadDataFromDB();
            } else {
                new Alert(Alert.AlertType.ERROR,
                        "Không thể xóa. Hộ khẩu này có dữ liệu ràng buộc "
                                + "(nhân khẩu, lịch sử, nộp tiền).").showAndWait();
            }
        }
    }
}
package controllers;

import application.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import models.NhanKhau;
import models.TamTruTamVang;
import services.NhanKhauDAO;
import services.TamTruDAO;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller cho màn hình Quản lý Tạm trú / Tạm vắng.
 * FIX: Thêm phân quyền ADMIN/STAFF — STAFF chỉ xem, ADMIN mới có quyền Thêm/Sửa/Xóa.
 */
public class TamTruController implements Initializable {

    // ===== FXML bindings =====
    @FXML private TextField txtTimKiem;
    @FXML private ComboBox<String> cmbLoai;
    @FXML private Button btnThemMoi;

    @FXML private TableView<TamTruTamVang>               tblTamTru;
    @FXML private TableColumn<TamTruTamVang, Integer>    colId;
    @FXML private TableColumn<TamTruTamVang, String>     colLoai;
    @FXML private TableColumn<TamTruTamVang, String>     colHoTen;
    @FXML private TableColumn<TamTruTamVang, LocalDate>  colTuNgay;
    @FXML private TableColumn<TamTruTamVang, LocalDate>  colDenNgay;
    @FXML private TableColumn<TamTruTamVang, String>     colDiaChi;
    @FXML private TableColumn<TamTruTamVang, String>     colLyDo;
    @FXML private TableColumn<TamTruTamVang, String>     colTrangThai;
    @FXML private TableColumn<TamTruTamVang, Void>       colHanhDong;

    // ===== DAO =====
    private final TamTruDAO   tamTruDAO   = new TamTruDAO();
    private final NhanKhauDAO nhanKhauDAO = new NhanKhauDAO();

    /** Cache id → họ tên nhân khẩu để tránh query lại từng dòng */
    private Map<Integer, String> nhanKhauCache = new HashMap<>();

    private ObservableList<TamTruTamVang> masterList;
    private FilteredList<TamTruTamVang>   filteredList;

    // FIX: Phân quyền
    private boolean isAdmin;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ===================================================================
    //  KHỞI TẠO
    // ===================================================================

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // FIX: Lấy quyền từ UserSession
        isAdmin = "ADMIN".equals(UserSession.getInstance().getCurrentUser().getRole());

        // FIX: Ẩn nút Thêm mới nếu không phải ADMIN
        btnThemMoi.setVisible(isAdmin);
        btnThemMoi.setManaged(isAdmin);

        buildNhanKhauCache();
        setupColumns();
        setupFilters();
        loadDataFromDB();
    }

    /** Nạp toàn bộ nhân khẩu vào Map để tra nhanh khi render bảng */
    private void buildNhanKhauCache() {
        nhanKhauCache.clear();
        List<NhanKhau> list = nhanKhauDAO.getAll();
        for (NhanKhau nk : list) {
            nhanKhauCache.put(nk.getId(), nk.getHoTen());
        }
    }

    // ===================================================================
    //  CẤU HÌNH CỘT TableView
    // ===================================================================

    private void setupColumns() {
        colId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
        colLoai.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("loai"));
        colTrangThai.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("trangThai"));
        colDiaChi.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("diaChiTamTru"));
        colLyDo.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("lyDo"));

        // Cột họ tên: tra từ cache
        colHoTen.setCellValueFactory(cellData -> {
            int nkId = cellData.getValue().getNhanKhauId();
            String name = nhanKhauCache.getOrDefault(nkId, "ID: " + nkId);
            return new javafx.beans.property.SimpleStringProperty(name);
        });

        // Cột ngày: format dd/MM/yyyy
        colTuNgay.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(DATE_FMT));
            }
        });
        colTuNgay.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("tuNgay"));

        colDenNgay.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "—" : item.format(DATE_FMT));
            }
        });
        colDenNgay.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("denNgay"));

        // FIX: Cột hành động — nút Sửa/Xóa chỉ hiển thị cho ADMIN
        colHanhDong.setCellFactory(col -> new TableCell<>() {
            private final Button btnSua = new Button("Sửa");
            private final Button btnXoa = new Button("Xóa");
            private final HBox   box    = new HBox(8, btnSua, btnXoa);

            {
                box.setAlignment(Pos.CENTER);
                btnSua.setStyle("-fx-background-color:#0969DA;-fx-text-fill:white;"
                        + "-fx-background-radius:4;-fx-cursor:hand;");
                btnXoa.setStyle("-fx-background-color:#82071E;-fx-text-fill:white;"
                        + "-fx-background-radius:4;-fx-cursor:hand;");

                // FIX: Ẩn nút Sửa/Xóa cho STAFF
                btnSua.setVisible(isAdmin);
                btnSua.setManaged(isAdmin);
                btnXoa.setVisible(isAdmin);
                btnXoa.setManaged(isAdmin);

                btnSua.setOnAction(e -> {
                    TamTruTamVang row = getTableView().getItems().get(getIndex());
                    openFormDialog(row);
                });
                btnXoa.setOnAction(e -> {
                    TamTruTamVang row = getTableView().getItems().get(getIndex());
                    handleDelete(row);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    // ===================================================================
    //  BỘ LỌC & TÌM KIẾM
    // ===================================================================

    private void setupFilters() {
        cmbLoai.getItems().setAll("Tất cả", "Tạm trú", "Tạm vắng");
        cmbLoai.setValue("Tất cả");
        cmbLoai.valueProperty().addListener((obs, o, n) -> applyFilter());
        txtTimKiem.textProperty().addListener((obs, o, n) -> applyFilter());
    }

    private void applyFilter() {
        if (filteredList == null) return;
        String keyword = txtTimKiem.getText() == null ? "" : txtTimKiem.getText().toLowerCase().trim();
        String loai    = cmbLoai.getValue();

        filteredList.setPredicate(t -> {
            if (loai != null && !"Tất cả".equals(loai) && !loai.equals(t.getLoai())) return false;
            if (!keyword.isEmpty()) {
                String hoTen = nhanKhauCache.getOrDefault(t.getNhanKhauId(), "").toLowerCase();
                return hoTen.contains(keyword);
            }
            return true;
        });
    }

    // ===================================================================
    //  LOAD DỮ LIỆU
    // ===================================================================

    public void loadDataFromDB() {
        buildNhanKhauCache();
        List<TamTruTamVang> dbList = tamTruDAO.getAll();
        masterList   = FXCollections.observableArrayList(dbList);
        filteredList = new FilteredList<>(masterList, p -> true);
        tblTamTru.setItems(filteredList);
        applyFilter();
    }

    // ===================================================================
    //  XỬ LÝ SỰ KIỆN
    // ===================================================================

    @FXML
    private void onThemMoiClick() {
        if (!isAdmin) return; // guard
        openFormDialog(null);
    }

    private void openFormDialog(TamTruTamVang data) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/views/TamTruForm.fxml"));
            javafx.scene.Parent root = loader.load();

            TamTruFormController formCtrl = loader.getController();
            formCtrl.setParentController(this);

            if (data != null) formCtrl.setEditData(data);
            else              formCtrl.setAddMode();

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle(data == null ? "Thêm đăng ký tạm trú / tạm vắng"
                                        : "Sửa đăng ký tạm trú / tạm vắng");
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setScene(new javafx.scene.Scene(root));
            stage.setResizable(false);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi hệ thống", "Không thể mở form:\n" + e.getMessage());
        }
    }

    private void handleDelete(TamTruTamVang t) {
        String hoTen = nhanKhauCache.getOrDefault(t.getNhanKhauId(), "ID " + t.getNhanKhauId());

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText(null);
        confirm.setContentText("Bạn có chắc muốn xóa đăng ký " + t.getLoai()
                + " của nhân khẩu \"" + hoTen + "\" không?\nHành động này không thể hoàn tác.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (tamTruDAO.delete(t.getId())) {
                loadDataFromDB();
                showInfo("Thành công", "Đã xóa đăng ký " + t.getLoai() + " của \"" + hoTen + "\".");
            } else {
                showError("Lỗi", "Không thể xóa bản ghi này.");
            }
        }
    }

    // ===================================================================
    //  HELPER – ALERT
    // ===================================================================

    private void showInfo(String title, String content) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(content); a.showAndWait();
    }

    private void showError(String title, String content) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(content); a.showAndWait();
    }
}

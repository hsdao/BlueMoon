package controllers;

import models.HoKhau;
import services.HoKhauDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.FilteredList;
import java.text.SimpleDateFormat;

import java.net.URL;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;


public class HoKhauController implements Initializable {

    @FXML private TableView<HoKhau> tableHoKhau;
    @FXML private TableColumn<HoKhau, String> colMaHo;
    @FXML private TableColumn<HoKhau, String> colChuHo;
    @FXML private TableColumn<HoKhau, String> colSdt;
    @FXML private TableColumn<HoKhau, String> colDiaChi;
    @FXML private TableColumn<HoKhau, Integer> colSoThanhVien;
    @FXML private TableColumn<HoKhau, Timestamp> colNgayTao;
    @FXML private TableColumn<HoKhau, String> colTrangThai;
    @FXML private TableColumn<HoKhau, Void> colHanhDong;
    @FXML private TableColumn<HoKhau, String> colGhiChu;
    @FXML private TextField searchField;

    private ObservableList<HoKhau> hoKhauList;
    private final HoKhauDAO dao = new HoKhauDAO();

    // Khởi tạo controller
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        loadDataFromDB();
        setupSearch();
    }

    // Bảng và nút hành động (Sửa, Xóa)
    private void setupTable() {
        colMaHo.setCellValueFactory(new PropertyValueFactory<>("maHo"));
        colChuHo.setCellValueFactory(new PropertyValueFactory<>("chuHoId"));
        colSdt.setCellValueFactory(new PropertyValueFactory<>("soDienThoaiChuHo")); // Map SĐT
        colDiaChi.setCellValueFactory(new PropertyValueFactory<>("diaChi"));
        colSoThanhVien.setCellValueFactory(new PropertyValueFactory<>("soThanhVien"));
        colNgayTao.setCellValueFactory(new PropertyValueFactory<>("ngayTao"));
        colTrangThai.setCellValueFactory(new PropertyValueFactory<>("trangThai"));
        colGhiChu.setCellValueFactory(new PropertyValueFactory<>("ghiChu"));

        colNgayTao.setCellFactory(column -> new TableCell<HoKhau, Timestamp>() {
            private final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
            @Override
            protected void updateItem(Timestamp item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(format.format(item));
                }
            }
        });

        Callback<TableColumn<HoKhau, Void>, TableCell<HoKhau, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<HoKhau, Void> call(final TableColumn<HoKhau, Void> param) {
                return new TableCell<>() {
                    private final Button btnEdit = new Button("Sửa");
                    private final Button btnDelete = new Button("Xóa");
                    private final HBox pane = new HBox(10, btnEdit, btnDelete);

                    {
                        pane.setAlignment(Pos.CENTER);
                        btnEdit.setStyle("-fx-background-color: #0969DA; -fx-text-fill: white; -fx-background-radius: 4; -fx-cursor: hand;");
                        btnDelete.setStyle("-fx-background-color: #82071E; -fx-text-fill: white; -fx-background-radius: 4; -fx-cursor: hand;");

                        btnEdit.setOnAction(event -> {
                            HoKhau data = getTableView().getItems().get(getIndex());
                            openFormDialog(data);
                        });

                        btnDelete.setOnAction(event -> {
                            HoKhau data = getTableView().getItems().get(getIndex());
                            handleDelete(data);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : pane);
                    }
                };
            }
        };
        colHanhDong.setCellFactory(cellFactory);
    }

    private void setupSearch() {
        FilteredList<HoKhau> filteredData = new FilteredList<>(hoKhauList, p -> true);

        searchField.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            filteredData.setPredicate(hoKhau -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                if (hoKhau.getMaHo() != null && hoKhau.getMaHo().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }

                if (hoKhau.getDiaChi() != null && hoKhau.getDiaChi().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }

                if (hoKhau.getSoDienThoaiChuHo() != null && hoKhau.getSoDienThoaiChuHo().contains(lowerCaseFilter)) {
                    return true;
                }

                return false;
            });
        });

        tableHoKhau.setItems(filteredData);
    }

    // Tải danh sách hộ khẩu từ DB vào bảng
    public void loadDataFromDB() {
        List<HoKhau> dbList = dao.getAllHoKhau();
        hoKhauList = FXCollections.observableArrayList(dbList);
        tableHoKhau.setItems(hoKhauList);
        // Không gọi setupSearch() ở đây – listener chỉ cần đăng ký 1 lần trong initialize()
    }

    // Form thêm hộ khẩu
    @FXML
    private void handleAdd() {
        openFormDialog(null);
    }

    private void openFormDialog(HoKhau hoKhau) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/HoKhauForm.fxml"));
            Parent root = loader.load();

            HoKhauFormController controller = loader.getController();
            controller.setParentController(this);

            if (hoKhau != null) {
                controller.setEditData(hoKhau);
            } else {
                controller.setAddMode();
            }

            Stage stage = new Stage();
            stage.setTitle(hoKhau == null ? "Thêm mới Hộ Khẩu" : "Sửa Hộ Khẩu");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Xóa hộ khẩu với xác nhận từ người dùng
    private void handleDelete(HoKhau hk) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText(null);
        confirm.setContentText("Bạn có chắc chắn muốn xóa Hộ khẩu: " + hk.getMaHo() + " không?\nHành động này không thể hoàn tác.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (dao.xoaHoKhau(hk.getId())) {
                loadDataFromDB();
            } else {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setContentText("Lỗi! Không thể xóa do có dữ liệu ràng buộc.");
                error.showAndWait();
            }
        }
    }
}
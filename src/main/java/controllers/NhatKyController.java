package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import models.AuditLog;
import services.AuditDAO;

import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;

/** Màn hình xem Nhật ký thao tác (chỉ ADMIN truy cập qua menu). */
public class NhatKyController implements Initializable {

    @FXML private TextField txtTimKiem;
    @FXML private TableView<AuditLog> tblNhatKy;
    @FXML private TableColumn<AuditLog, Timestamp> colThoiGian;
    @FXML private TableColumn<AuditLog, String> colUsername;
    @FXML private TableColumn<AuditLog, String> colHanhDong;
    @FXML private TableColumn<AuditLog, String> colDoiTuong;
    @FXML private TableColumn<AuditLog, String> colMoTa;

    private final AuditDAO dao = new AuditDAO();
    private final ObservableList<AuditLog> master = FXCollections.observableArrayList();
    private FilteredList<AuditLog> filtered;
    private static final SimpleDateFormat FMT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colHanhDong.setCellValueFactory(new PropertyValueFactory<>("hanhDong"));
        colDoiTuong.setCellValueFactory(new PropertyValueFactory<>("doiTuong"));
        colMoTa.setCellValueFactory(new PropertyValueFactory<>("moTa"));
        colThoiGian.setCellValueFactory(new PropertyValueFactory<>("thoiGian"));
        colThoiGian.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Timestamp t, boolean empty) {
                super.updateItem(t, empty);
                setText(empty || t == null ? null : FMT.format(t));
            }
        });

        filtered = new FilteredList<>(master, p -> true);
        tblNhatKy.setItems(filtered);
        txtTimKiem.textProperty().addListener((o, a, kw) -> applyFilter(kw));
        loadData();
    }

    private void loadData() {
        master.setAll(dao.getRecent(1000));
    }

    private void applyFilter(String kw) {
        if (kw == null || kw.isBlank()) { filtered.setPredicate(p -> true); return; }
        String k = kw.toLowerCase().trim();
        filtered.setPredicate(a ->
                (a.getUsername() != null && a.getUsername().toLowerCase().contains(k))
                || (a.getHanhDong() != null && a.getHanhDong().toLowerCase().contains(k))
                || (a.getDoiTuong() != null && a.getDoiTuong().toLowerCase().contains(k))
                || (a.getMoTa() != null && a.getMoTa().toLowerCase().contains(k)));
    }

    @FXML private void onLamMoiClick() { txtTimKiem.clear(); loadData(); }
}

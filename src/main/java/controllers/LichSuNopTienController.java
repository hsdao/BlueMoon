package controllers;

import models.HoKhau;
import models.KhoanThu;
import models.NopTien;
import services.HoKhauDAO;
import services.KhoanThuDAO;
import services.NopTienDAO;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

/**
 * Controller cho màn hình Lịch sử nộp phí (LichSuNopTien.fxml).
 * Hỗ trợ lọc theo khoản thu, hộ khẩu và tìm kiếm từ khóa.
 */
public class LichSuNopTienController implements Initializable {

    @FXML private ComboBox<KhoanThu> cmbLocKhoanThu;
    @FXML private ComboBox<HoKhau>   cmbLocHoKhau;
    @FXML private TextField           txtTimKiem;
    @FXML private Button              btnLamMoi;

    @FXML private TableView<NopTien>          tblLichSu;
    @FXML private TableColumn<NopTien, Integer>    colStt;
    @FXML private TableColumn<NopTien, Integer>    colKhoanThuId;
    @FXML private TableColumn<NopTien, Integer>    colHoKhauId;
    @FXML private TableColumn<NopTien, BigDecimal> colSoTien;
    @FXML private TableColumn<NopTien, LocalDate>  colNgayNop;
    @FXML private TableColumn<NopTien, String>     colNguoiThu;
    @FXML private TableColumn<NopTien, String>     colGhiChu;

    private final NopTienDAO  nopTienDAO  = new NopTienDAO();
    private final KhoanThuDAO khoanThuDAO = new KhoanThuDAO();
    private final HoKhauDAO   hoKhauDAO   = new HoKhauDAO();

    private ObservableList<NopTien> allData;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupColumns();
        loadComboBoxes();
        loadAllData();
        setupFilters();
    }

    private void setupColumns() {
        colStt.setCellValueFactory(new PropertyValueFactory<>("id"));
        colKhoanThuId.setCellValueFactory(new PropertyValueFactory<>("khoanThuId"));
        colHoKhauId.setCellValueFactory(new PropertyValueFactory<>("hoKhauId"));
        colSoTien.setCellValueFactory(new PropertyValueFactory<>("soTien"));
        colNgayNop.setCellValueFactory(new PropertyValueFactory<>("ngayNop"));
        colNguoiThu.setCellValueFactory(new PropertyValueFactory<>("nguoiThu"));
        colGhiChu.setCellValueFactory(new PropertyValueFactory<>("ghiChu"));
    }

    private void loadComboBoxes() {
        // -- Khoản thu --
        ObservableList<KhoanThu> ktList = FXCollections.observableArrayList();
        ktList.add(null);
        ktList.addAll(khoanThuDAO.getAllKhoanThu());
        cmbLocKhoanThu.setItems(ktList);
        cmbLocKhoanThu.setCellFactory(lv -> cellKT());
        cmbLocKhoanThu.setButtonCell(cellKT());
        cmbLocKhoanThu.getSelectionModel().selectFirst();

        // -- Hộ khẩu --
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

    private void loadAllData() {
        allData = FXCollections.observableArrayList(nopTienDAO.getAllNopTien());
    }

    private void setupFilters() {
        FilteredList<NopTien> filtered = new FilteredList<>(allData, p -> true);
        Runnable apply = () -> filtered.setPredicate(nt -> {
            KhoanThu selKt = cmbLocKhoanThu.getValue();
            HoKhau   selHk = cmbLocHoKhau.getValue();
            String kw = txtTimKiem.getText() == null ? "" : txtTimKiem.getText().toLowerCase().trim();
            if (selKt != null && nt.getKhoanThuId() != selKt.getId()) return false;
            if (selHk != null && nt.getHoKhauId()   != selHk.getId()) return false;
            if (!kw.isEmpty()) {
                boolean m1 = nt.getNguoiThu() != null && nt.getNguoiThu().toLowerCase().contains(kw);
                boolean m2 = nt.getGhiChu()   != null && nt.getGhiChu().toLowerCase().contains(kw);
                if (!m1 && !m2) return false;
            }
            return true;
        });
        cmbLocKhoanThu.setOnAction(e -> apply.run());
        cmbLocHoKhau.setOnAction(e -> apply.run());
        txtTimKiem.textProperty().addListener((obs, o, n) -> apply.run());
        tblLichSu.setItems(filtered);
    }

    @FXML
    private void onLamMoiClick() {
        loadAllData();
        cmbLocKhoanThu.getSelectionModel().selectFirst();
        cmbLocHoKhau.getSelectionModel().selectFirst();
        txtTimKiem.clear();
        setupFilters();
    }
}

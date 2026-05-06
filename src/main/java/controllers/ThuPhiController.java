package controllers;

import models.HoKhau;
import models.KhoanThu;
import models.NopTien;
import services.HoKhauDAO;
import services.KhoanThuDAO;
import services.NopTienDAO;
import services.ThuPhiDAO;
import services.ThuPhiService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller cho màn hình Thu phí (ThuPhi.fxml).
 * Nghiệp vụ: chọn khoản thu + hộ khẩu, validate,
 * check trùng, ghi DB, hiển thị lịch sử gần đây.
 */
public class ThuPhiController implements Initializable {

    @FXML private ComboBox<KhoanThu> cmbKhoanThu;
    @FXML private ComboBox<HoKhau>   cmbHoKhau;
    @FXML private TextField           txtSoTien;
    @FXML private DatePicker          dpNgayNop;
    @FXML private TextField           txtNguoiThu;
    @FXML private TextArea            txtGhiChu;
    @FXML private Button              btnGhiNhan;
    @FXML private Button              btnLamMoi;

    @FXML private TableView<NopTien>          tblLichSu;
    @FXML private TableColumn<NopTien, Integer>    colId;
    @FXML private TableColumn<NopTien, Integer>    colHoKhauId;
    @FXML private TableColumn<NopTien, BigDecimal> colSoTien;
    @FXML private TableColumn<NopTien, String>     colNguoiThu;
    @FXML private TableColumn<NopTien, String>     colGhiChu;
    @FXML private TableColumn<NopTien, LocalDate>  colNgayNop;

    private final KhoanThuDAO  khoanThuDAO = new KhoanThuDAO();
    private final HoKhauDAO    hoKhauDAO   = new HoKhauDAO();
    private final ThuPhiDAO    thuPhiDAO   = new ThuPhiDAO();
    private final ThuPhiService service    = new ThuPhiService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupColumns();
        loadKhoanThu();
        loadHoKhau();
        dpNgayNop.setValue(LocalDate.now());
        cmbKhoanThu.setOnAction(e -> { preFillSoTien(); loadLichSu(); });
    }

    // ---- Setup ----

    private void setupColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colHoKhauId.setCellValueFactory(new PropertyValueFactory<>("hoKhauId"));
        colSoTien.setCellValueFactory(new PropertyValueFactory<>("soTien"));
        colNguoiThu.setCellValueFactory(new PropertyValueFactory<>("nguoiThu"));
        colGhiChu.setCellValueFactory(new PropertyValueFactory<>("ghiChu"));
        colNgayNop.setCellValueFactory(new PropertyValueFactory<>("ngayNop"));
    }

    private void loadKhoanThu() {
        ObservableList<KhoanThu> obs = FXCollections.observableArrayList(khoanThuDAO.getAllKhoanThu());
        cmbKhoanThu.setItems(obs);
        cmbKhoanThu.setCellFactory(lv -> cellKT());
        cmbKhoanThu.setButtonCell(cellKT());
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
        ObservableList<HoKhau> obs = FXCollections.observableArrayList(hoKhauDAO.getAllHoKhau());
        cmbHoKhau.setItems(obs);
        cmbHoKhau.setCellFactory(lv -> cellHK());
        cmbHoKhau.setButtonCell(cellHK());
    }

    private ListCell<HoKhau> cellHK() {
        return new ListCell<>() {
            @Override protected void updateItem(HoKhau hk, boolean empty) {
                super.updateItem(hk, empty);
                setText(empty || hk == null ? null : hk.getMaHo() + " – " + hk.getDiaChi());
            }
        };
    }

    private void preFillSoTien() {
        KhoanThu kt = cmbKhoanThu.getValue();
        if (kt != null && kt.getSoTien() != null)
            txtSoTien.setText(String.valueOf(kt.getSoTien().longValue()));
        else txtSoTien.clear();
    }

    private void loadLichSu() {
        KhoanThu kt = cmbKhoanThu.getValue();
        if (kt == null) { tblLichSu.getItems().clear(); return; }
        tblLichSu.setItems(FXCollections.observableArrayList(thuPhiDAO.getByKhoanThu(kt.getId())));
    }

    // ---- Sự kiện ----

    @FXML
    private void onGhiNhanClick() {
        KhoanThu kt = cmbKhoanThu.getValue();
        HoKhau   hk = cmbHoKhau.getValue();
        int ktId = kt != null ? kt.getId() : 0;
        int hkId = hk != null ? hk.getId() : 0;

        String loi = service.validate(ktId, hkId, txtSoTien.getText(),
                                      txtNguoiThu.getText(), dpNgayNop.getValue());
        if (loi != null) { showAlert(Alert.AlertType.WARNING, "Dữ liệu không hợp lệ", loi); return; }

        if (service.kiemTraDaNop(ktId, hkId)) {
            Alert c = new Alert(Alert.AlertType.CONFIRMATION);
            c.setTitle("Cảnh báo trùng nộp");
            c.setHeaderText(null);
            c.setContentText("Hộ này đã nộp khoản \"" + kt.getTenKhoan() + "\" rồi.\nVẫn ghi nhận tiếp?");
            if (c.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
        }

        NopTien nt = new NopTien();
        nt.setKhoanThuId(ktId);
        nt.setHoKhauId(hkId);
        nt.setSoTien(new BigDecimal(txtSoTien.getText().trim().replace(",", "")));
        nt.setNgayNop(dpNgayNop.getValue());
        nt.setNguoiThu(txtNguoiThu.getText().trim());
        nt.setGhiChu(txtGhiChu.getText().trim());

        if (service.ghiNhanNopTien(nt)) {
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã ghi nhận nộp tiền thành công!");
            resetForm();
            loadLichSu();
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể ghi nhận. Vui lòng thử lại.");
        }
    }

    @FXML
    private void onLamMoiClick() { resetForm(); tblLichSu.getItems().clear(); }

    private void resetForm() {
        cmbKhoanThu.setValue(null); cmbHoKhau.setValue(null);
        txtSoTien.clear(); dpNgayNop.setValue(LocalDate.now());
        txtNguoiThu.clear(); txtGhiChu.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert a = new Alert(type);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(content);
        a.showAndWait();
    }
}

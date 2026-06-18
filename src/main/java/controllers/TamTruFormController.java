package controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.NhanKhau;
import models.TamTruTamVang;
import services.NhanKhauDAO;
import services.TamTruDAO;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

/**
 * <ul>
 *   <li>Validate dữ liệu đầu vào (nhân khẩu, loại, từ ngày bắt buộc)</li>
 *   <li>Thêm mới / Cập nhật bản ghi qua {@link TamTruDAO}</li>
 *   <li>Callback reload danh sách ở màn hình cha</li>
 * </ul>
 */
public class TamTruFormController implements Initializable {

    // ===== FXML bindings =====
    @FXML private ComboBox<NhanKhau>  cmbNhanKhau;
    @FXML private ComboBox<String>    cmbLoai;
    @FXML private DatePicker          dpTuNgay;
    @FXML private DatePicker          dpDenNgay;
    @FXML private TextField           txtDiaChi;
    @FXML private TextField           txtLyDo;
    @FXML private ComboBox<String>    cmbTrangThai;
    @FXML private Label               lblError;
    @FXML private Button              btnLuu;
    @FXML private Button              btnHuy;

    // ===== DAO =====
    private final TamTruDAO   tamTruDAO   = new TamTruDAO();
    private final NhanKhauDAO nhanKhauDAO = new NhanKhauDAO();

    // ===== State =====
    private TamTruController parentController;
    private TamTruTamVang    editingData;     // null = thêm mới
    private boolean          isEditMode = false;

    // ===================================================================
    //  KHỞI TẠO
    // ===================================================================

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Nạp danh sách nhân khẩu vào ComboBox
        List<NhanKhau> nkList = nhanKhauDAO.getAll();
        cmbNhanKhau.setItems(FXCollections.observableArrayList(nkList));
        cmbNhanKhau.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(NhanKhau item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getId() + " – " + item.getHoTen());
            }
        });
        cmbNhanKhau.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(NhanKhau item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getId() + " – " + item.getHoTen());
            }
        });

        // Loại tạm trú / tạm vắng
        cmbLoai.setItems(FXCollections.observableArrayList("Tạm trú", "Tạm vắng"));

        // Trạng thái
        cmbTrangThai.setItems(FXCollections.observableArrayList(
                "Đang hiệu lực", "Hết hạn", "Đã hủy"));
        cmbTrangThai.setValue("Đang hiệu lực");

        // Ngày mặc định
        dpTuNgay.setValue(LocalDate.now());

        // Sự kiện nút
        btnLuu.setOnAction(e -> handleLuu());
        btnHuy.setOnAction(e -> closeDialog());
    }

    // ===================================================================
    //  PUBLIC API – gọi từ TamTruController
    // ===================================================================

    /** Chế độ thêm mới */
    public void setAddMode() {
        isEditMode = false;
        editingData = null;
    }

    /** Chế độ sửa – điền dữ liệu sẵn vào form */
    public void setEditData(TamTruTamVang t) {
        isEditMode  = true;
        editingData = t;

        // Chọn nhân khẩu tương ứng
        cmbNhanKhau.getItems().stream()
                .filter(nk -> nk.getId() == t.getNhanKhauId())
                .findFirst()
                .ifPresent(cmbNhanKhau::setValue);

        cmbLoai.setValue(t.getLoai());
        dpTuNgay.setValue(t.getTuNgay());
        dpDenNgay.setValue(t.getDenNgay());
        txtDiaChi.setText(t.getDiaChiTamTru());
        txtLyDo.setText(t.getLyDo());
        cmbTrangThai.setValue(t.getTrangThai());
    }

    /** Gán controller cha để gọi reload sau khi lưu */
    public void setParentController(TamTruController parent) {
        this.parentController = parent;
    }

    // ===================================================================
    //  XỬ LÝ LƯU
    // ===================================================================

    private void handleLuu() {
        lblError.setText("");

        // --- Validate ---
        NhanKhau nk = cmbNhanKhau.getValue();
        if (nk == null) {
            lblError.setText("Vui lòng chọn nhân khẩu!");
            return;
        }

        String loai = cmbLoai.getValue();
        if (loai == null || loai.isEmpty()) {
            lblError.setText("Vui lòng chọn loại (Tạm trú / Tạm vắng)!");
            return;
        }

        LocalDate tuNgay = dpTuNgay.getValue();
        if (tuNgay == null) {
            lblError.setText("Vui lòng nhập ngày bắt đầu!");
            return;
        }

        LocalDate denNgay = dpDenNgay.getValue();
        if (denNgay != null && !denNgay.isAfter(tuNgay)) {
            lblError.setText("Ngày kết thúc phải sau ngày bắt đầu!");
            return;
        }

        // --- Build model ---
        TamTruTamVang t = isEditMode ? editingData : new TamTruTamVang();
        t.setNhanKhauId(nk.getId());
        t.setLoai(loai);
        t.setTuNgay(tuNgay);
        t.setDenNgay(denNgay);
        t.setDiaChiTamTru(txtDiaChi.getText().trim());
        t.setLyDo(txtLyDo.getText().trim());
        t.setTrangThai(
                cmbTrangThai.getValue() != null ? cmbTrangThai.getValue() : "Đang hiệu lực");

        // --- Lưu DB ---
        boolean ok;
        if (isEditMode) {
            ok = tamTruDAO.update(t);
        } else {
            ok = tamTruDAO.insert(t);
        }

        if (ok) {
            if (parentController != null) {
                parentController.loadDataFromDB();
            }
            closeDialog();
        } else {
            lblError.setText("Lỗi khi lưu dữ liệu. Vui lòng thử lại!");
        }
    }

    // ===================================================================
    //  HELPER
    // ===================================================================

    private void closeDialog() {
        Stage stage = (Stage) btnHuy.getScene().getWindow();
        stage.close();
    }
}

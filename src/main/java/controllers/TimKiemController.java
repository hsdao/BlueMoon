package controllers;

import models.HoKhau;
import models.KhoanThu;
import models.NhanKhau;
import models.SearchResult;
import services.SearchService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller cho màn hình Tìm kiếm toàn cục (TimKiem.fxml).
 *
 * Tuần 4 – Task 37 + 38:
 *  - Search bar tìm đồng thời Hộ khẩu, Nhân khẩu, Khoản thu
 *  - Kết quả phân tab riêng biệt
 *  - Bộ lọc nâng cao đa điều kiện cho từng tab
 *  - Sắp xếp cột (TableView tự hỗ trợ khi click header)
 */
public class TimKiemController implements Initializable {

    // ---- Search bar chính ----
    @FXML private TextField txtTimKiem;
    @FXML private Button    btnTimKiem;
    @FXML private Button    btnXoaTim;

    // ---- TabPane ----
    @FXML private TabPane tabPane;

    // ---- Tab Hộ khẩu ----
    @FXML private ComboBox<String>         cmbTrangThaiHK;
    @FXML private Button                   btnLocHK;
    @FXML private TableView<HoKhau>        tblHoKhau;
    @FXML private TableColumn<HoKhau, String>  colHkMaHo;
    @FXML private TableColumn<HoKhau, String>  colHkDiaChi;
    @FXML private TableColumn<HoKhau, Integer> colHkSoTv;
    @FXML private TableColumn<HoKhau, String>  colHkSdt;
    @FXML private TableColumn<HoKhau, String>  colHkTrangThai;
    @FXML private Label                    lblSoHK;

    // ---- Tab Nhân khẩu ----
    @FXML private TableView<NhanKhau>           tblNhanKhau;
    @FXML private TableColumn<NhanKhau, String> colNkHoTen;
    @FXML private TableColumn<NhanKhau, String> colNkCccd;
    @FXML private TableColumn<NhanKhau, String> colNkGioiTinh;
    @FXML private TableColumn<NhanKhau, String> colNkNgheNghiep;
    @FXML private TableColumn<NhanKhau, String> colNkSdt;
    @FXML private TableColumn<NhanKhau, String> colNkTrangThai;
    @FXML private Label                         lblSoNK;

    // ---- Tab Khoản thu ----
    @FXML private ComboBox<String>           cmbLoaiKT;
    @FXML private ComboBox<String>           cmbTrangThaiKT;
    @FXML private Button                     btnLocKT;
    @FXML private TableView<KhoanThu>        tblKhoanThu;
    @FXML private TableColumn<KhoanThu, String>  colKtMaKhoan;
    @FXML private TableColumn<KhoanThu, String>  colKtTenKhoan;
    @FXML private TableColumn<KhoanThu, String>  colKtLoai;
    @FXML private TableColumn<KhoanThu, Double>  colKtSoTien;
    @FXML private TableColumn<KhoanThu, String>  colKtTrangThai;
    @FXML private Label                          lblSoKT;

    private final SearchService searchService = new SearchService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupColumns();
        setupFilterCombos();

        // Enter để tìm kiếm
        txtTimKiem.setOnAction(e -> onTimKiemClick());
    }

    // =========================================================
    //  SETUP
    // =========================================================

    private void setupColumns() {
        // Hộ khẩu
        colHkMaHo.setCellValueFactory(new PropertyValueFactory<>("maHo"));
        colHkDiaChi.setCellValueFactory(new PropertyValueFactory<>("diaChi"));
        colHkSoTv.setCellValueFactory(new PropertyValueFactory<>("soThanhVien"));
        colHkSdt.setCellValueFactory(new PropertyValueFactory<>("soDienThoaiChuHo"));
        colHkTrangThai.setCellValueFactory(new PropertyValueFactory<>("trangThai"));
        tblHoKhau.setSortPolicy(tv -> { FXCollections.sort(tv.getItems(), tv.getComparator()); return true; });

        // Nhân khẩu
        colNkHoTen.setCellValueFactory(new PropertyValueFactory<>("hoTen"));
        colNkCccd.setCellValueFactory(new PropertyValueFactory<>("cccd"));
        colNkGioiTinh.setCellValueFactory(new PropertyValueFactory<>("gioiTinh"));
        colNkNgheNghiep.setCellValueFactory(new PropertyValueFactory<>("ngheNghiep"));
        colNkSdt.setCellValueFactory(new PropertyValueFactory<>("soDienThoai"));
        colNkTrangThai.setCellValueFactory(new PropertyValueFactory<>("trangThai"));
        tblNhanKhau.setSortPolicy(tv -> { FXCollections.sort(tv.getItems(), tv.getComparator()); return true; });

        // Khoản thu
        colKtMaKhoan.setCellValueFactory(new PropertyValueFactory<>("maKhoan"));
        colKtTenKhoan.setCellValueFactory(new PropertyValueFactory<>("tenKhoan"));
        colKtLoai.setCellValueFactory(new PropertyValueFactory<>("loai"));
        colKtSoTien.setCellValueFactory(new PropertyValueFactory<>("soTien"));
        colKtTrangThai.setCellValueFactory(new PropertyValueFactory<>("trangThai"));
        tblKhoanThu.setSortPolicy(tv -> { FXCollections.sort(tv.getItems(), tv.getComparator()); return true; });
    }

    private void setupFilterCombos() {
        // Trạng thái hộ khẩu — khớp với giá trị DB: ACTIVE / INACTIVE
        cmbTrangThaiHK.setItems(FXCollections.observableArrayList(
            "", "ACTIVE", "INACTIVE"
        ));
        cmbTrangThaiHK.getSelectionModel().selectFirst();

        // Loại khoản thu — khớp với giá trị DB: BAT_BUOC / TU_NGUYEN / Bắt buộc / Tự nguyện
        cmbLoaiKT.setItems(FXCollections.observableArrayList(
            "", "BAT_BUOC", "TU_NGUYEN", "Bắt buộc", "Tự nguyện"
        ));
        cmbLoaiKT.getSelectionModel().selectFirst();

        // Trạng thái khoản thu — khớp với giá trị DB: OPEN / CLOSED / Chưa thu
        cmbTrangThaiKT.setItems(FXCollections.observableArrayList(
            "", "OPEN", "CLOSED", "Chưa thu"
        ));
        cmbTrangThaiKT.getSelectionModel().selectFirst();
    }

    // =========================================================
    //  SỰ KIỆN
    // =========================================================

    /** Nút Tìm kiếm hoặc nhấn Enter trong ô tìm. */
    @FXML
    private void onTimKiemClick() {
        String kw = txtTimKiem.getText();
        List<SearchResult> all = searchService.timKiem(kw);

        // Phân loại kết quả vào từng bảng
        ObservableList<HoKhau>   hkList = FXCollections.observableArrayList();
        ObservableList<NhanKhau> nkList = FXCollections.observableArrayList();
        ObservableList<KhoanThu> ktList = FXCollections.observableArrayList();

        for (SearchResult r : all) {
            switch (r.getLoai()) {
                case HO_KHAU   -> hkList.add((HoKhau)   r.getDuLieuGoc());
                case NHAN_KHAU -> nkList.add((NhanKhau) r.getDuLieuGoc());
                case KHOAN_THU -> ktList.add((KhoanThu) r.getDuLieuGoc());
            }
        }

        tblHoKhau.setItems(hkList);
        tblNhanKhau.setItems(nkList);
        tblKhoanThu.setItems(ktList);

        lblSoHK.setText("Tìm thấy: " + hkList.size());
        lblSoNK.setText("Tìm thấy: " + nkList.size());
        lblSoKT.setText("Tìm thấy: " + ktList.size());
    }

    /** Nút Xóa tìm kiếm – reset về trạng thái ban đầu. */
    @FXML
    private void onXoaTimClick() {
        txtTimKiem.clear();
        tblHoKhau.getItems().clear();
        tblNhanKhau.getItems().clear();
        tblKhoanThu.getItems().clear();
        lblSoHK.setText("");
        lblSoNK.setText("");
        lblSoKT.setText("");
        cmbTrangThaiHK.getSelectionModel().selectFirst();
        cmbLoaiKT.getSelectionModel().selectFirst();
        cmbTrangThaiKT.getSelectionModel().selectFirst();
    }

    /** Nút Lọc trong tab Hộ khẩu — lọc nâng cao. */
    @FXML
    private void onLocHoKhauClick() {
        String kw = txtTimKiem.getText();
        String trangThai = cmbTrangThaiHK.getValue();
        List<HoKhau> list = searchService.locHoKhau(kw, trangThai);
        tblHoKhau.setItems(FXCollections.observableArrayList(list));
        lblSoHK.setText("Tìm thấy: " + list.size());
    }

    /** Nút Lọc trong tab Khoản thu — lọc nâng cao. */
    @FXML
    private void onLocKhoanThuClick() {
        String kw       = txtTimKiem.getText();
        String loai     = cmbLoaiKT.getValue();
        String trangThai = cmbTrangThaiKT.getValue();
        List<KhoanThu> list = searchService.locKhoanThu(kw, loai, trangThai);
        tblKhoanThu.setItems(FXCollections.observableArrayList(list));
        lblSoKT.setText("Tìm thấy: " + list.size());
    }
}

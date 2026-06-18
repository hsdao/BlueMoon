package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import models.KhoanThu;
import models.ThongKeModel;
import services.ThongKeServiceDAO;
import services.ExportPDFService;
import services.ExportExcelService;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class ThongKeController implements Initializable {

    @FXML private ComboBox<KhoanThu> cmbKhoanThu;
    @FXML private TextField          txtTimKiem;
    @FXML private Label              lblTongHoDaNop;
    @FXML private Label              lblTongTienThu;

    @FXML private PieChart                            pieChart;
    @FXML private TableView<ThongKeModel>             tvThongKe;
    @FXML private TableColumn<ThongKeModel, String>   colMaHo;
    @FXML private TableColumn<ThongKeModel, String>   colTenChuHo;
    @FXML private TableColumn<ThongKeModel, String>   colPhaiNop;     // Số tiền phải nộp (tự tính)
    @FXML private TableColumn<ThongKeModel, String>   colSoTienNop;   // Hiển thị tiền format
    @FXML private TableColumn<ThongKeModel, String>   colNgayNop;     // Hiển thị dd/MM/yyyy
    @FXML private TableColumn<ThongKeModel, String>   colTrangThai;
    @FXML private TableColumn<ThongKeModel, String>   colGhiChu;

    private ThongKeServiceDAO thongKeServiceDAO;
    private ObservableList<ThongKeModel> masterData;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        thongKeServiceDAO = new ThongKeServiceDAO();
        masterData = FXCollections.observableArrayList();
        setupTableColumns();
        setupComboBox();
        // FIX: Gắn listener cho ô tìm kiếm (trước đây không hoạt động)
        setupSearch();
    }

    private void setupTableColumns() {
        colMaHo.setCellValueFactory(new PropertyValueFactory<>("maHoKhau"));
        colTenChuHo.setCellValueFactory(new PropertyValueFactory<>("tenChuHo"));
        colTrangThai.setCellValueFactory(new PropertyValueFactory<>("trangThai"));
        colGhiChu.setCellValueFactory(new PropertyValueFactory<>("ghiChu"));

        // Số tiền PHẢI NỘP (tự tính theo cách tính khoản thu)
        colPhaiNop.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) { setText(null); return; }
                BigDecimal pn = getTableView().getItems().get(getIndex()).getSoTienPhaiNop();
                setText(pn != null && pn.signum() > 0 ? currencyFormat.format(pn) + " đ" : "—");
            }
        });

        // FIX: Số tiền hiển thị định dạng tiền tệ VN thay vì toString() mặc định
        colSoTienNop.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) { setText(null); return; }
                BigDecimal soTien = getTableView().getItems().get(getIndex()).getSoTienNop();
                setText(soTien != null ? currencyFormat.format(soTien) + " đ" : "—");
            }
        });

        // FIX: Ngày nộp hiển thị dd/MM/yyyy thay vì toString() mặc định
        colNgayNop.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) { setText(null); return; }
                LocalDate ngay = getTableView().getItems().get(getIndex()).getNgayNop();
                setText(ngay != null ? ngay.format(DATE_FMT) : "—");
            }
        });
    }

    private void setupComboBox() {
        List<KhoanThu> danhSachKhoanThu = thongKeServiceDAO.getAllKhoanThu();
        cmbKhoanThu.setItems(FXCollections.observableArrayList(danhSachKhoanThu));

        cmbKhoanThu.setConverter(new StringConverter<KhoanThu>() {
            @Override public String toString(KhoanThu kt) { return kt == null ? null : kt.getTenKhoan(); }
            @Override public KhoanThu fromString(String string) { return null; }
        });

        cmbKhoanThu.setOnAction(event -> handleKhoanThuSelection());
    }

    // FIX: Thêm listener tìm kiếm cho ô txtTimKiem
    private void setupSearch() {
        txtTimKiem.textProperty().addListener((obs, oldVal, newVal) -> applySearch(newVal));
    }

    private void applySearch(String keyword) {
        if (masterData == null) return;
        if (keyword == null || keyword.isBlank()) {
            tvThongKe.setItems(masterData);
            return;
        }
        String kw = keyword.toLowerCase().trim();
        ObservableList<ThongKeModel> filtered = FXCollections.observableArrayList();
        for (ThongKeModel row : masterData) {
            boolean match =
                    (row.getMaHoKhau()  != null && row.getMaHoKhau().toLowerCase().contains(kw)) ||
                    (row.getTenChuHo()  != null && row.getTenChuHo().toLowerCase().contains(kw)) ||
                    (row.getTrangThai() != null && row.getTrangThai().toLowerCase().contains(kw)) ||
                    (row.getGhiChu()    != null && row.getGhiChu().toLowerCase().contains(kw));
            if (match) filtered.add(row);
        }
        tvThongKe.setItems(filtered);
    }

    private void handleKhoanThuSelection() {
        KhoanThu selectedKhoanThu = cmbKhoanThu.getValue();
        if (selectedKhoanThu == null) return;

        List<ThongKeModel> data = thongKeServiceDAO.getThongKeByKhoanThu(selectedKhoanThu.getId());
        masterData = FXCollections.observableArrayList(data);
        tvThongKe.setItems(masterData);

        // Reset ô tìm kiếm khi đổi khoản thu
        txtTimKiem.clear();

        int tongHoDaNop = 0;
        int tongHoChuaNop = 0;
        BigDecimal tongTien = BigDecimal.ZERO;
        for (ThongKeModel row : data) {
            if ("Đã nộp".equals(row.getTrangThai())) {
                tongHoDaNop++;
                if (row.getSoTienNop() != null) tongTien = tongTien.add(row.getSoTienNop());
            } else {
                tongHoChuaNop++;
            }
        }
        lblTongHoDaNop.setText(tongHoDaNop + " hộ");
        lblTongTienThu.setText(currencyFormat.format(tongTien) + " đ");

        // FIX (Tuần 4): Cập nhật PieChart tỉ lệ hộ đã/chưa nộp
        updatePieChart(tongHoDaNop, tongHoChuaNop);
    }

    /**
     * Cập nhật PieChart với dữ liệu tỉ lệ hộ đã nộp vs chưa nộp.
     */
    private void updatePieChart(int daNop, int chuaNop) {
        if (pieChart == null) return;
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                new PieChart.Data("Đã nộp (" + daNop + ")", Math.max(daNop, 0)),
                new PieChart.Data("Chưa nộp (" + chuaNop + ")", Math.max(chuaNop, 0))
        );
        pieChart.setData(pieData);
        pieChart.setTitle("");
    }

    @FXML
    void handleXuatExcel(ActionEvent event) {
        if (tvThongKe.getItems().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Thông báo");
            alert.setHeaderText(null);
            alert.setContentText("Không có dữ liệu để xuất!");
            alert.showAndWait();
            return;
        }
        javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
        fc.setTitle("Lưu File Excel");
        fc.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        fc.setInitialFileName("Bao_Cao_Thong_Ke_" + LocalDate.now() + ".xlsx");
        File file = fc.showSaveDialog(tvThongKe.getScene().getWindow());
        if (file != null) {
            try {
                ExportExcelService service = new ExportExcelService();
                String title = cmbKhoanThu.getValue() != null
                        ? "Báo Cáo Thống Kê: " + cmbKhoanThu.getValue().getTenKhoan()
                        : "Báo Cáo Thống Kê";
                service.exportThongKe(tvThongKe.getItems(), title, file);
                Alert ok = new Alert(Alert.AlertType.INFORMATION);
                ok.setTitle("Thành công");
                ok.setHeaderText(null);
                ok.setContentText("File Excel đã được xuất:\n" + file.getAbsolutePath());
                ok.showAndWait();
            } catch (Exception e) {
                e.printStackTrace();
                Alert err = new Alert(Alert.AlertType.ERROR);
                err.setContentText("Lỗi xuất Excel: " + e.getMessage());
                err.showAndWait();
            }
        }
    }

    @FXML
    void handleXuatPDF(ActionEvent event) {
        if (tvThongKe.getItems().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Thông báo");
            alert.setHeaderText(null);
            alert.setContentText("Không có dữ liệu để xuất báo cáo!");
            alert.showAndWait();
            return;
        }

        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Lưu Báo Cáo PDF");
        fileChooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName("Bao_Cao_Thong_Ke_" + LocalDate.now() + ".pdf");

        File file = fileChooser.showSaveDialog(tvThongKe.getScene().getWindow());
        if (file != null) {
            try {
                ExportPDFService service = new ExportPDFService();
                String title = cmbKhoanThu.getValue() != null
                        ? "Báo Cáo Thống Kê: " + cmbKhoanThu.getValue().getTenKhoan()
                        : "Báo Cáo Thống Kê";
                service.exportThongKe(tvThongKe.getItems(), title, file);

                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Thành công");
                success.setHeaderText(null);
                success.setContentText("Báo cáo đã được xuất thành công tại:\n" + file.getAbsolutePath());
                success.showAndWait();

            } catch (Exception e) {
                e.printStackTrace();
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setContentText("Có lỗi xảy ra khi xuất PDF: " + e.getMessage());
                error.showAndWait();
            }
        }
    }
}
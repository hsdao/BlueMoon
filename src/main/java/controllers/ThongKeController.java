package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import models.KhoanThu;
import models.ThongKeModel;
import services.ThongKeServiceDAO;
import services.ExportPDFService;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.scene.control.Alert;

public class ThongKeController implements Initializable {

    @FXML private ComboBox<KhoanThu> cmbKhoanThu;
    @FXML private TextField txtTimKiem;
    @FXML private Label lblTongHoDaNop;
    @FXML private Label lblTongTienThu;

    // TableView và các cột
    @FXML private TableView<ThongKeModel> tvThongKe;
    @FXML private TableColumn<ThongKeModel, String> colMaHo;
    @FXML private TableColumn<ThongKeModel, String> colTenChuHo;
    @FXML private TableColumn<ThongKeModel, BigDecimal> colSoTienNop;
    @FXML private TableColumn<ThongKeModel, LocalDate> colNgayNop;
    @FXML private TableColumn<ThongKeModel, String> colTrangThai;
    @FXML private TableColumn<ThongKeModel, String> colGhiChu;

    private ThongKeServiceDAO thongKeServiceDAO;
    private final NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        thongKeServiceDAO = new ThongKeServiceDAO();
        setupTableColumns();
        setupComboBox();
    }

    private void setupTableColumns() {
        // Ánh xạ các cột với thuộc tính trong class ThongKeModel
        colMaHo.setCellValueFactory(new PropertyValueFactory<>("maHoKhau"));
        colTenChuHo.setCellValueFactory(new PropertyValueFactory<>("tenChuHo"));
        colSoTienNop.setCellValueFactory(new PropertyValueFactory<>("soTienNop"));
        colNgayNop.setCellValueFactory(new PropertyValueFactory<>("ngayNop"));
        colTrangThai.setCellValueFactory(new PropertyValueFactory<>("trangThai"));
        colGhiChu.setCellValueFactory(new PropertyValueFactory<>("ghiChu"));
    }

    private void setupComboBox() {
        // Lấy dữ liệu khoản thu từ DB
        List<KhoanThu> danhSachKhoanThu = thongKeServiceDAO.getAllKhoanThu();
        cmbKhoanThu.setItems(FXCollections.observableArrayList(danhSachKhoanThu));

        // Format ComboBox để chỉ hiển thị tên khoản thu
        cmbKhoanThu.setConverter(new StringConverter<KhoanThu>() {
            @Override
            public String toString(KhoanThu kt) {
                return kt == null ? null : kt.getTenKhoan();
            }
            @Override
            public KhoanThu fromString(String string) {
                return null;
            }
        });

        // Bắt sự kiện khi người dùng chọn 1 khoản thu
        cmbKhoanThu.setOnAction(event -> handleKhoanThuSelection());
    }

    private void handleKhoanThuSelection() {
        KhoanThu selectedKhoanThu = cmbKhoanThu.getValue();
        if (selectedKhoanThu != null) {
            // Lấy dữ liệu thống kê từ Service
            List<ThongKeModel> data = thongKeServiceDAO.getThongKeByKhoanThu(selectedKhoanThu.getId());
            ObservableList<ThongKeModel> observableData = FXCollections.observableArrayList(data);

            // Đổ dữ liệu vào bảng
            tvThongKe.setItems(observableData);

            // Tính toán tổng số hộ đã nộp và tổng tiền
            int tongHoDaNop = 0;
            BigDecimal tongTien = BigDecimal.ZERO;

            for (ThongKeModel row : data) {
                if ("Đã nộp".equals(row.getTrangThai())) {
                    tongHoDaNop++;
                    tongTien = tongTien.add(row.getSoTienNop());
                }
            }

            // Cập nhật lên UI
            lblTongHoDaNop.setText(tongHoDaNop + " hộ");
            lblTongTienThu.setText(currencyFormat.format(tongTien) + " VNĐ");
        }
    }

        @FXML
        void handleXuatPDF(ActionEvent event) {
            // 1. Kiểm tra xem có dữ liệu trên bảng không
            if (tvThongKe.getItems().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Thông báo");
                alert.setHeaderText(null);
                alert.setContentText("Không có dữ liệu để xuất báo cáo!");
                alert.showAndWait();
                return;
            }

            // 2. Mở hộp thoại FileChooser để chọn nơi lưu file
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Lưu Báo Cáo PDF");
            fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            fileChooser.setInitialFileName("Bao_Cao_Thong_Ke_" + LocalDate.now() + ".pdf");

            File file = fileChooser.showSaveDialog(tvThongKe.getScene().getWindow());

            if (file != null) {
                try {
                    ExportPDFService service = new ExportPDFService();
                    String title = "Báo Cáo Thống Kê: " + cmbKhoanThu.getValue().getTenKhoan();

                    // Gọi Service thực thi xuất file
                    service.exportThongKe(tvThongKe.getItems(), title, file);

                    // Hiển thị thông báo thành công
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
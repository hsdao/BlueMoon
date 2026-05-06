package controllers;
import javafx.scene.control.Button;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.text.NumberFormat;
import java.util.Locale;

// Import các DAO mà team (Dương) đã làm
// import dao.HoKhauDAO;
// import dao.KhoanThuDAO;
// import dao.NopTienDAO;

public class DashboardController implements Initializable {

    // 1. Khai báo các thành phần giao diện (Map đúng fx:id bên Dashboard.fxml)
    @FXML
    private Button btnTaoKhoanThu;

    @FXML
    private Button btnThuPhiNgay;

    @FXML
    private Label lblTongHo;

    @FXML
    private Label lblTongKhoanThu;

    @FXML
    private Label lblTongTienThang;

    @FXML
    private BarChart<String, Number> chartDoanhThu;

    // Khởi tạo formatter để hiển thị tiền tệ đẹp mắt (VD: 10,000,000)
    private final NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Hàm này tự động chạy ngay khi màn hình Dashboard được load
        loadSummaryData();
        loadChartData();
    }

    /**
     * Hàm lấy dữ liệu cho 3 thẻ thống kê tổng quan
     */
    private void loadSummaryData() {
        try {
            // Bước 1: Đếm tổng số hộ từ bảng ho_khau (Dựa trên HoKhau Model)
            // int tongHo = HoKhauDAO.countTotal();
            int tongHo = 120; // Số liệu giả lập để test giao diện
            lblTongHo.setText(String.valueOf(tongHo));

            // Bước 2: Đếm tổng số khoản thu từ bảng khoan_thu (Dựa trên KhoanThu Model)
            // int tongKhoanThu = KhoanThuDAO.countTotal();
            int tongKhoanThu = 15; // Số liệu giả lập
            lblTongKhoanThu.setText(String.valueOf(tongKhoanThu));

            // Bước 3: Tính tổng doanh thu tháng hiện tại từ bảng nop_tien (Dựa trên NopTien Model)
            LocalDate currentDate = LocalDate.now();
            int currentMonth = currentDate.getMonthValue();
            int currentYear = currentDate.getYear();

            // Vì NopTien dùng BigDecimal, hàm sumByMonth của DAO nên trả về BigDecimal
            // BigDecimal tongTienThang = NopTienDAO.sumByMonth(currentMonth, currentYear);
            BigDecimal tongTienThang = new BigDecimal("25500000"); // Số liệu giả lập
            lblTongTienThang.setText(currencyFormat.format(tongTienThang));

        } catch (Exception e) {
            e.printStackTrace();
            // Xử lý ghi log lỗi nếu không kết nối được CSDL
        }
    }

    /**
     * Hàm vẽ biểu đồ BarChart doanh thu các tháng
     */
    private void loadChartData() {
        // Xóa dữ liệu cũ nếu có
        chartDoanhThu.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Doanh thu năm nay");

        try {
            // Thực tế bạn sẽ gọi hàm DAO lấy List dữ liệu thống kê theo tháng
            // Map<String, BigDecimal> doanhThuThang = NopTienDAO.getDoanhThuCacThangGhiNhan();

            // Đổ dữ liệu giả lập để xem layout
            series.getData().add(new XYChart.Data<>("Tháng 1", 15000000));
            series.getData().add(new XYChart.Data<>("Tháng 2", 18500000));
            series.getData().add(new XYChart.Data<>("Tháng 3", 12000000));
            series.getData().add(new XYChart.Data<>("Tháng 4", 25500000));

            // Đưa series vào biểu đồ
            chartDoanhThu.getData().add(series);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- Các hàm xử lý sự kiện cho Phím tắt (Shortcuts) ---

    @FXML
    void handleTaoKhoanThu() {
        // Mở popup hoặc chuyển tab sang màn hình Thêm Khoản Thu của phần Thu Phí
        System.out.println("Chuyển sang màn hình Tạo Khoản Thu...");
    }

    @FXML
    void handleThuPhiNgay() {
        // Mở popup hoặc chuyển tab sang màn hình Nộp Tiền
        System.out.println("Chuyển sang màn hình Thu Phí...");
    }
}
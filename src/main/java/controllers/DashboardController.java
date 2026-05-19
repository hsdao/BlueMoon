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
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import services.HoKhauDAO;
import services.KhoanThuDAO;
import services.NopTienDAO;

public class DashboardController implements Initializable {

    @FXML private Button btnTaoKhoanThu;
    @FXML private Button btnThuPhiNgay;
    @FXML private Label lblTongHo;
    @FXML private Label lblTongKhoanThu;
    @FXML private Label lblTongTienThang;
    @FXML private BarChart<String, Number> chartDoanhThu;

    private final HoKhauDAO  hoKhauDAO  = new HoKhauDAO();
    private final KhoanThuDAO khoanThuDAO = new KhoanThuDAO();
    private final NopTienDAO  nopTienDAO  = new NopTienDAO();
    private final NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadSummaryData();
        loadChartData();
    }

    /**
     * Hàm lấy dữ liệu thật từ DB cho 3 thẻ thống kê tổng quan
     */
    private void loadSummaryData() {
        try {
            // Tổng số hộ khẩu từ DB
            int tongHo = hoKhauDAO.countTotal();
            lblTongHo.setText(String.valueOf(tongHo));

            // Tổng số khoản thu từ DB
            int tongKhoanThu = khoanThuDAO.countTotal();
            lblTongKhoanThu.setText(String.valueOf(tongKhoanThu));

            // Tổng tiền thu tháng hiện tại từ DB
            LocalDate now = LocalDate.now();
            BigDecimal tongTienThang = nopTienDAO.sumByMonth(now.getMonthValue(), now.getYear());
            lblTongTienThang.setText(currencyFormat.format(tongTienThang) + " đ");

        } catch (Exception e) {
            e.printStackTrace();
            lblTongHo.setText("—");
            lblTongKhoanThu.setText("—");
            lblTongTienThang.setText("—");
        }
    }

    /**
     * Vẽ biểu đồ BarChart doanh thu thực tế các tháng trong năm hiện tại
     */
    private void loadChartData() {
        chartDoanhThu.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Doanh thu " + LocalDate.now().getYear());

        try {
            List<BigDecimal> doanhThu = nopTienDAO.sumByEachMonthInYear(LocalDate.now().getYear());
            String[] tenThang = {"T1","T2","T3","T4","T5","T6","T7","T8","T9","T10","T11","T12"};

            for (int i = 0; i < 12; i++) {
                series.getData().add(new XYChart.Data<>(tenThang[i], doanhThu.get(i)));
            }
            chartDoanhThu.getData().add(series);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- Điều hướng màn hình từ nút tắt Dashboard ---

    @FXML
    void handleTaoKhoanThu() {
        // Mở màn hình Khoản Thu thông qua DashBoard FXML (nếu có SideBar điều hướng)
        // Thực tế: menu sidebar sẽ xử lý điều hướng; đây là nút tắt
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/views/KhoanThu.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Quản lý Khoản Thu");
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setScene(new javafx.scene.Scene(root));
            stage.setWidth(1200);
            stage.setHeight(800);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleThuPhiNgay() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/views/ThuPhi.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Thu Phí");
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setScene(new javafx.scene.Scene(root));
            stage.setWidth(1200);
            stage.setHeight(800);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
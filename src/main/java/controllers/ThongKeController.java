package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class ThongKeController implements Initializable {

    // --- Khai báo các phần tử giao diện (Map chuẩn với ThongKe.fxml) ---

    @FXML
    private ComboBox<String> cmbKhoanThu;

    @FXML
    private TextField txtTimKiem;

    @FXML
    private Button btnXuatPDF;

    @FXML
    private Label lblTongHoDaNop;

    @FXML
    private Label lblTongTienThu;

    // Khai báo TableView và các cột (Giả sử bạn dùng một class tên là ThongKeModel để chứa dữ liệu)
    // Tạm thời để <Object, Object> để không báo lỗi đỏ, sau này thay bằng Model thật của team
    @FXML
    private TableView<Object> tvThongKe;

    @FXML
    private TableColumn<Object, String> colMaHo;

    @FXML
    private TableColumn<Object, String> colTenChuHo;

    @FXML
    private TableColumn<Object, String> colSoTienNop;

    @FXML
    private TableColumn<Object, String> colNgayNop;

    @FXML
    private TableColumn<Object, String> colTrangThai;

    @FXML
    private TableColumn<Object, String> colGhiChu;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Code load danh sách khoản thu vào ComboBox sẽ nằm ở đây
        // Code cấu hình cột cho TableView sẽ nằm ở đây
        System.out.println("Màn hình Thống Kê đã khởi tạo thành công!");
    }

    // --- Xử lý sự kiện ---

    @FXML
    void handleXuatPDF(ActionEvent event) {
        System.out.println("Bắt đầu gọi thư viện iText 8 để xuất PDF...");
        // Logic dùng iText 8 lấy dữ liệu từ tvThongKe và vẽ ra file PDF sẽ viết tại đây
    }
}
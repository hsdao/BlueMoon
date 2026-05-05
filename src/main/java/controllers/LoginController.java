package controllers;

import application.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.User;
import services.UserDAO;

public class LoginController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<String> cmbRole;
    @FXML private Button btnLogin;
    @FXML private Label lblError;

    private UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        // Nạp dữ liệu vào ComboBox Role
        cmbRole.getItems().addAll("ADMIN", "STAFF");

        // Bắt sự kiện click nút Login
        btnLogin.setOnAction(event -> handleLogin());
    }

    private void handleLogin() {
        String username = txtUsername.getText();
        String password = txtPassword.getText();
        String role = cmbRole.getValue();

        // Validate form trống
        if (username.isEmpty() || password.isEmpty() || role == null) {
            lblError.setText("Vui lòng nhập đầy đủ thông tin và chọn Role!");
            lblError.setStyle("-fx-text-fill: red;");
            return;
        }

        // Kiểm tra database
        User loggedInUser = userDAO.kiemTraDangNhap(username, password);

        if (loggedInUser != null) {
            // Kiểm tra Role
            if (!loggedInUser.getRole().equals(role)) {
                lblError.setText("Tài khoản không có quyền " + role + "!");
                lblError.setStyle("-fx-text-fill: red;");
                return;
            }

            // Đăng nhập thành công -> Lưu vào Session
            UserSession.getInstance().setCurrentUser(loggedInUser);

            // Chuyển sang màn hình chính
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/HoKhau.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) btnLogin.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setWidth(1440);
                stage.setHeight(860);
                stage.centerOnScreen();
            } catch (Exception e) {
                lblError.setText("Lỗi hệ thống: Không thể tải màn hình chính!");
                lblError.setStyle("-fx-text-fill: red;");
                e.printStackTrace();
            }

        } else {
            lblError.setText("Sai tài khoản hoặc mật khẩu!");
            lblError.setStyle("-fx-text-fill: red;");
        }
    }
}
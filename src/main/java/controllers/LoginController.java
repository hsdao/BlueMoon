package controllers;

import application.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
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

            // Đăng nhập thành công -> Lưu vào Session(trong file UserSession.java)
            UserSession.getInstance().setCurrentUser(loggedInUser);

            lblError.setText("Đăng nhập thành công!");
            lblError.setStyle("-fx-text-fill: green;");

            // TODO: Bàn giao lại cho nhóm để viết code chuyển sang màn hình Main/Dashboard

        } else {
            lblError.setText("Sai tài khoản hoặc mật khẩu!");
            lblError.setStyle("-fx-text-fill: red;");
        }
    }
}
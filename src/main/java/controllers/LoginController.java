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
import services.NotificationService;
import services.UserDAO;

public class LoginController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<String> cmbRole;
    @FXML private Button btnLogin;
    @FXML private Label lblError;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        cmbRole.getItems().addAll("ADMIN", "STAFF");
        btnLogin.setOnAction(event -> handleLogin());
        // Cho phép nhấn Enter trong ô mật khẩu để đăng nhập
        txtPassword.setOnAction(event -> handleLogin());
    }

    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();
        String role = cmbRole.getValue();

        if (username.isEmpty() || password.isEmpty() || role == null) {
            lblError.setText("Vui lòng nhập đầy đủ thông tin và chọn Role!");
            lblError.setStyle("-fx-text-fill: red;");
            return;
        }

        User loggedInUser = userDAO.kiemTraDangNhap(username, password);

        if (loggedInUser != null) {
            if (!loggedInUser.getRole().equals(role)) {
                lblError.setText("Tài khoản không có quyền " + role + "!");
                lblError.setStyle("-fx-text-fill: red;");
                return;
            }

            UserSession.getInstance().setCurrentUser(loggedInUser);

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/MainLayout.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) btnLogin.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setResizable(true);
                stage.setMinWidth(1100);
                stage.setMinHeight(700);
                stage.setMaximized(true);   // khớp mọi kích thước màn hình

                // FIX: Gọi NotificationService sau khi màn hình chính đã load
                NotificationService notifService = new NotificationService();
                notifService.checkAndNotify(stage);

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
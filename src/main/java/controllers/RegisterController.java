package controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.User;
import services.UserDAO;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Màn hình Đăng ký tài khoản (luồng nghiệp vụ "Đăng kí tài khoản" trong đề bài).
 * Mở dạng dialog từ màn hình Login. Mật khẩu được băm bởi {@link UserDAO#addUser}.
 */
public class RegisterController implements Initializable {

    @FXML private TextField     txtUsername;
    @FXML private TextField     txtFullName;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private ComboBox<String> cmbRole;
    @FXML private Label         lblMsg;
    @FXML private Button        btnRegister;
    @FXML private Button        btnCancel;

    private final UserDAO userDAO = new UserDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Đăng ký công khai từ màn Login CHỈ tạo tài khoản STAFF.
        // Việc nâng quyền ADMIN do ADMIN thực hiện trong màn Quản lý tài khoản.
        cmbRole.setItems(FXCollections.observableArrayList("STAFF"));
        cmbRole.setValue("STAFF");
        cmbRole.setDisable(true);
        // Nút con mắt hiện/ẩn mật khẩu
        application.PasswordToggle.install(txtPassword);
        application.PasswordToggle.install(txtConfirmPassword);
    }

    @FXML
    private void handleRegister() {
        String username = txtUsername.getText() == null ? "" : txtUsername.getText().trim();
        String fullName = txtFullName.getText() == null ? "" : txtFullName.getText().trim();
        String password = txtPassword.getText();
        String confirm  = txtConfirmPassword.getText();
        String role     = "STAFF"; // đăng ký công khai luôn là STAFF (không cho tự cấp ADMIN)

        // Validate
        if (username.isEmpty() || password == null || password.isEmpty()) {
            showError("Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!");
            return;
        }
        if (!username.matches("^[A-Za-z0-9_]{3,50}$")) {
            showError("Tên đăng nhập 3-50 ký tự, chỉ gồm chữ, số và dấu gạch dưới!");
            return;
        }
        if (password.length() < 3) {
            showError("Mật khẩu phải có ít nhất 3 ký tự!");
            return;
        }
        if (!password.equals(confirm)) {
            showError("Mật khẩu nhập lại không khớp!");
            return;
        }

        // Tạo tài khoản (UserDAO tự băm mật khẩu; username trùng -> addUser trả false)
        User u = new User(username, password, role);
        u.setFullName(fullName.isEmpty() ? null : fullName);

        if (userDAO.addUser(u)) {
            services.AuditService.log("DANG_KY", "Tài khoản", "Đăng ký TK: " + username + " (" + role + ")");
            lblMsg.setText("✅ Đăng ký thành công! Bạn có thể đăng nhập.");
            lblMsg.setStyle("-fx-text-fill: -color-success-fg;");
            btnRegister.setDisable(true);
        } else {
            showError("Không thể đăng ký. Tên đăng nhập có thể đã tồn tại.");
        }
    }

    @FXML
    private void handleCancel() {
        ((Stage) btnCancel.getScene().getWindow()).close();
    }

    private void showError(String msg) {
        lblMsg.setText("⚠ " + msg);
        lblMsg.setStyle("-fx-text-fill: -color-danger-fg;");
    }
}

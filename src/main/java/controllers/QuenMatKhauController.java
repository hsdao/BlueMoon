package controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.User;
import services.PasswordUtil;
import services.UserDAO;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Đặt lại mật khẩu (Quên mật khẩu) — bản đơn giản cho ứng dụng nội bộ:
 * nhập tên đăng nhập + mật khẩu mới. (Thực tế nên xác minh danh tính qua email/OTP.)
 */
public class QuenMatKhauController implements Initializable {

    @FXML private TextField     txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirm;
    @FXML private Label         lblMsg;
    @FXML private Button        btnReset;
    @FXML private Button        btnCancel;

    private final UserDAO userDAO = new UserDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        application.PasswordToggle.install(txtPassword);
        application.PasswordToggle.install(txtConfirm);
    }

    @FXML
    private void handleReset() {
        String username = txtUsername.getText() == null ? "" : txtUsername.getText().trim();
        String pass = txtPassword.getText();
        String confirm = txtConfirm.getText();

        if (username.isEmpty() || pass == null || pass.isEmpty()) {
            showError("Vui lòng nhập tên đăng nhập và mật khẩu mới!");
            return;
        }
        if (pass.length() < 3) { showError("Mật khẩu phải có ít nhất 3 ký tự!"); return; }
        if (!pass.equals(confirm)) { showError("Mật khẩu nhập lại không khớp!"); return; }

        User u = userDAO.findByUsername(username);
        if (u == null) { showError("Không tìm thấy tài khoản \"" + username + "\"."); return; }

        u.setPassword(PasswordUtil.hash(pass));
        if (userDAO.updateUser(u)) {
            services.AuditService.log("DAT_LAI_MK", "Hệ thống", "Đặt lại mật khẩu cho: " + username);
            lblMsg.setText("✅ Đã đặt lại mật khẩu. Bạn có thể đăng nhập lại.");
            lblMsg.setStyle("-fx-text-fill: -color-success-fg;");
            btnReset.setDisable(true);
        } else {
            showError("Không thể cập nhật mật khẩu. Thử lại sau.");
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

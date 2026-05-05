package controllers;

import application.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import models.User;
import services.UserDAO;

public class ChangePasswordController {

    @FXML private PasswordField txtOldPassword;
    @FXML private PasswordField txtNewPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private Button btnConfirm;
    @FXML private Button btnCancel;

    private UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        btnConfirm.setOnAction(event -> handleConfirm());
        btnCancel.setOnAction(event -> handleCancel());
    }

    private void handleConfirm() {
        String oldPass = txtOldPassword.getText();
        String newPass = txtNewPassword.getText();
        String confirmPass = txtConfirmPassword.getText();

        // 1. Validate form trống
        if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập đầy đủ các trường mật khẩu!");
            return;
        }

        // 2. Validate mật khẩu mới khớp nhau
        if (!newPass.equals(confirmPass)) {
            showAlert(Alert.AlertType.ERROR, "Lỗi nhập liệu", "Mật khẩu mới không khớp!");
            return;
        }

        // 3. Lấy User từ Session
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Không tìm thấy phiên đăng nhập. Vui lòng đăng nhập lại!");
            return;
        }

        // 4. Validate mật khẩu cũ
        if (!currentUser.getPassword().equals(oldPass)) {
            showAlert(Alert.AlertType.ERROR, "Lỗi bảo mật", "Mật khẩu cũ không chính xác!");
            return;
        }

        // 5. Cập nhật mật khẩu mới xuống Database
        currentUser.setPassword(newPass);
        boolean isUpdated = userDAO.updateUser(currentUser);

        if (isUpdated) {
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đổi mật khẩu thành công!");
            // Xóa rỗng các ô
            txtOldPassword.clear();
            txtNewPassword.clear();
            txtConfirmPassword.clear();
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi cơ sở dữ liệu", "Đã xảy ra lỗi khi cập nhật mật khẩu. Vui lòng thử lại!");
        }
    }

    private void handleCancel() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
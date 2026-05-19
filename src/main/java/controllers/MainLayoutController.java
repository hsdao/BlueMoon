package controllers;

import application.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import models.User;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainLayoutController implements Initializable {

    @FXML private StackPane contentArea;
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;
    @FXML private Label lblAdminSection;
    @FXML private Button btnQuanLyTK;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        User user = UserSession.getInstance().getCurrentUser();
        if (user != null) {
            lblUserName.setText(user.getUsername());
            lblUserRole.setText(user.getRole());
            if (!"ADMIN".equals(user.getRole())) {
                lblAdminSection.setVisible(false);
                lblAdminSection.setManaged(false);
                btnQuanLyTK.setVisible(false);
                btnQuanLyTK.setManaged(false);
            }
        }
        goDashboard();
    }

    private void loadContent(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node content = loader.load();
            contentArea.getChildren().setAll(content);
        } catch (IOException e) {
            e.printStackTrace();
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Loi he thong");
            a.setHeaderText(null);
            a.setContentText("Khong the tai: " + fxmlPath + "\n" + e.getMessage());
            a.showAndWait();
        }
    }

    @FXML private void goDashboard()      { loadContent("/views/DashBoard.fxml"); }
    @FXML private void goHoKhau()         { loadContent("/views/HoKhau.fxml"); }
    @FXML private void goNhanKhau()       { loadContent("/views/NhanKhau.fxml"); }
    @FXML private void goKhoanThu()       { loadContent("/views/KhoanThu.fxml"); }
    @FXML private void goThuPhi()         { loadContent("/views/ThuPhi.fxml"); }
    @FXML private void goThongKe()        { loadContent("/views/ThongKe.fxml"); }
    @FXML private void goTamTru()         { loadContent("/views/TamTruTamVang.fxml"); }
    @FXML private void goLichSuBienDong() { loadContent("/views/LichSuBienDong.fxml"); }
    @FXML private void goLichSuNopTien()  { loadContent("/views/LichSuNopTien.fxml"); }
    @FXML private void goTimKiem()        { loadContent("/views/TimKiem.fxml"); }
    @FXML private void goQuanLyTaiKhoan() { loadContent("/views/QuanLyTaiKhoan.fxml"); }
    @FXML private void goSettings()       { loadContent("/views/Settings.fxml"); }
    @FXML private void goChangePassword() { loadContent("/views/ChangePassword.fxml"); }

    @FXML
    private void handleLogout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Dang xuat");
        confirm.setHeaderText(null);
        confirm.setContentText("Ban co chac muon dang xuat khong?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            UserSession.getInstance().cleanUserSession();
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Login.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) contentArea.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setWidth(1120);
                stage.setHeight(645);
                stage.centerOnScreen();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

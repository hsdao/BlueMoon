package controllers;

import application.UserSession;
import javafx.event.ActionEvent;
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
import javafx.scene.layout.VBox;
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
    @FXML private Button btnNhatKy;
    @FXML private Button btnDashboard;
    @FXML private VBox navBox;
    @FXML private VBox navBottom;

    /** Tham chiếu instance hiện tại để các màn con (vd Dashboard) điều hướng nội bộ. */
    private static MainLayoutController instance;

    /** Điều hướng nội bộ tới một FXML trong khung nội dung chính. */
    public static void navigate(String fxmlPath) {
        if (instance != null) instance.loadContent(fxmlPath);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        instance = this;
        User user = UserSession.getInstance().getCurrentUser();
        if (user != null) {
            lblUserName.setText(user.getUsername());
            lblUserRole.setText(user.getRole());
            boolean admin = "ADMIN".equals(user.getRole());
            if (!admin) {
                hide(lblAdminSection);
                hide(btnQuanLyTK);
                hide(btnNhatKy);
            }
        }
        // Mở Tổng quan mặc định + đánh dấu mục đang chọn
        setActive(btnDashboard);
        loadContent("/views/DashBoard.fxml");
    }

    private void hide(Node n) {
        n.setVisible(false);
        n.setManaged(false);
    }

    private void loadContent(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node content = loader.load();
            contentArea.getChildren().setAll(content);
        } catch (IOException e) {
            e.printStackTrace();
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Lỗi hệ thống");
            a.setHeaderText(null);
            a.setContentText("Không thể tải: " + fxmlPath + "\n" + e.getMessage());
            a.showAndWait();
        }
    }

    /** Bỏ đánh dấu tất cả nút điều hướng rồi tô đậm nút đang chọn. */
    private void setActive(Button active) {
        clearActive(navBox);
        clearActive(navBottom);
        if (active != null && !active.getStyleClass().contains("nav-active")) {
            active.getStyleClass().add("nav-active");
        }
    }

    private void clearActive(VBox box) {
        if (box == null) return;
        for (Node n : box.getChildren()) {
            if (n instanceof Button b) b.getStyleClass().remove("nav-active");
        }
    }

    private Button src(ActionEvent e) {
        return (e != null && e.getSource() instanceof Button b) ? b : null;
    }

    @FXML private void goDashboard(ActionEvent e)      { setActive(btnDashboard); loadContent("/views/DashBoard.fxml"); }
    @FXML private void goHoKhau(ActionEvent e)         { setActive(src(e)); loadContent("/views/HoKhau.fxml"); }
    @FXML private void goSoDoPhong(ActionEvent e)      { setActive(src(e)); loadContent("/views/SoDoPhong.fxml"); }
    @FXML private void goNhanKhau(ActionEvent e)       { setActive(src(e)); loadContent("/views/NhanKhau.fxml"); }
    @FXML private void goKhoanThu(ActionEvent e)       { setActive(src(e)); loadContent("/views/KhoanThu.fxml"); }
    @FXML private void goThuPhi(ActionEvent e)         { setActive(src(e)); loadContent("/views/ThuPhi.fxml"); }
    @FXML private void goCongNo(ActionEvent e)         { setActive(src(e)); loadContent("/views/CongNo.fxml"); }
    @FXML private void goDoiSoat(ActionEvent e)        { setActive(src(e)); loadContent("/views/DoiSoatQuy.fxml"); }
    @FXML private void goThongKe(ActionEvent e)        { setActive(src(e)); loadContent("/views/ThongKe.fxml"); }
    @FXML private void goTamTru(ActionEvent e)         { setActive(src(e)); loadContent("/views/TamTruTamVang.fxml"); }
    @FXML private void goLichSuBienDong(ActionEvent e) { setActive(src(e)); loadContent("/views/LichSuBienDong.fxml"); }
    @FXML private void goLichSuNopTien(ActionEvent e)  { setActive(src(e)); loadContent("/views/LichSuNopTien.fxml"); }
    @FXML private void goTimKiem(ActionEvent e)        { setActive(src(e)); loadContent("/views/TimKiem.fxml"); }
    @FXML private void goQuanLyTaiKhoan(ActionEvent e) { setActive(src(e)); loadContent("/views/QuanLyTaiKhoan.fxml"); }
    @FXML private void goNhatKy(ActionEvent e)         { setActive(src(e)); loadContent("/views/NhatKy.fxml"); }
    @FXML private void goSettings(ActionEvent e)       { setActive(src(e)); loadContent("/views/Settings.fxml"); }
    @FXML private void goChangePassword(ActionEvent e) { setActive(src(e)); loadContent("/views/ChangePassword.fxml"); }

    @FXML
    private void handleLogout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Đăng xuất");
        confirm.setHeaderText(null);
        confirm.setContentText("Bạn có chắc muốn đăng xuất không?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            UserSession.getInstance().cleanUserSession();
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Login.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) contentArea.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setResizable(false);
                stage.setWidth(1120);
                stage.setHeight(645);
                stage.centerOnScreen();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

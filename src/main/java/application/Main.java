package application;

import controllers.SettingsController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Entry point của ứng dụng BlueMoon.
 * Tuần 4: Tích hợp load theme từ settings.properties (SettingsController.applyStoredTheme)
 * trước khi hiển thị màn hình Login.
 */
public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        // Tuần 4: Load theme đã lưu (mặc định PrimerLight nếu chưa có file preference)
        SettingsController.applyStoredTheme();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Login.fxml"));
            Parent root = loader.load();
            primaryStage.setTitle("BlueMoon - Quản lý thu phí chung cư");
            primaryStage.setScene(new Scene(root));
            primaryStage.setWidth(1120);
            primaryStage.setHeight(645);
            primaryStage.setResizable(false);
            primaryStage.show();
        } catch (Exception e) {
            System.err.println("Lỗi: Không thể load màn hình Login!");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

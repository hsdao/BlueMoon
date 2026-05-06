/* package application;

import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        // Set Theme AtlantaFX (PrimerLight) defaults as required in specs
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

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
*/


package application;

import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            // Set Theme AtlantaFX (PrimerLight) defaults as required in specs
            Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

            // Load file DashBoard.fxml để test
            Parent root = FXMLLoader.load(getClass().getResource("/views/ThongKe.fxml"));

            // Khởi tạo Scene với kích thước chuẩn của Dashboard là 1190 x 964
            Scene scene = new Scene(root, 1190, 964);

            primaryStage.setTitle("Test Màn Hình Thống Kê - BlueMoon");
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen(); // Hiển thị ra giữa màn hình
            primaryStage.setResizable(false); // Khóa kích thước cửa sổ
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
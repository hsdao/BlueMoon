package application;

import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/HoKhau.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 1440, 1024);

            primaryStage.setTitle("BlueMoon - Quản lý thu phí chung cư");
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
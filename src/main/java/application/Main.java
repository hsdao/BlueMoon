package application;

import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        // Set Theme AtlantaFX (PrimerLight) defaults as required in specs
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

        primaryStage.setTitle("BlueMoon - Quản lý thu phí chung cư");
        primaryStage.setWidth(1120);
        primaryStage.setHeight(645);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
package com.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TestFxml extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            System.out.println("Loading patients.fxml...");
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/app/view/patients.fxml"));
            Parent root = fxmlLoader.load();
            System.out.println("SUCCESS! patients.fxml loaded.");
            System.exit(0);
        } catch (Exception e) {
            System.err.println("FAILED to load patients.fxml:");
            e.printStackTrace();
            System.exit(1);
        }
    }
}

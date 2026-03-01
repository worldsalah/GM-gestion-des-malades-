package com.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class MainApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        // Make the window borderless for a modern look
        stage.initStyle(StageStyle.UNDECORATED);

        // Auto-Maximize logic for initial launch
        javafx.stage.Screen screen = javafx.stage.Screen.getPrimary();
        javafx.geometry.Rectangle2D bounds = screen.getVisualBounds();

        // Save fallback bounds (centered 900x600)
        lastX = (bounds.getWidth() - 900) / 2;
        lastY = (bounds.getHeight() - 600) / 2;
        lastWidth = 900;
        lastHeight = 600;

        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());
        isMaximized = true;

        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("/com/app/view/login.fxml"));
        Parent root = fxmlLoader.load();

        Scene scene = new Scene(root, 900, 600);

        // Add CSS
        String css = MainApp.class.getResource("/com/app/css/style.css").toExternalForm();
        scene.getStylesheets().add(css);

        stage.setTitle("UniLearn - App Login");
        stage.setScene(scene);
        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("/com/app/view/" + fxml + ".fxml"));
        Parent root = fxmlLoader.load();
        primaryStage.getScene().setRoot(root);

        // Only reset size and position if NOT maximized
        if (!isMaximized) {
            primaryStage.sizeToScene();
            primaryStage.centerOnScreen();
        }
    }

    public static Stage getStage() {
        return primaryStage;
    }

    private static double lastX, lastY, lastWidth, lastHeight;
    public static boolean isMaximized = false;

    public static void toggleMaximize(Stage stage) {
        javafx.stage.Screen screen = javafx.stage.Screen.getPrimary();
        javafx.geometry.Rectangle2D bounds = screen.getVisualBounds();

        if (isMaximized) {
            stage.setX(lastX);
            stage.setY(lastY);
            stage.setWidth(lastWidth);
            stage.setHeight(lastHeight);
            isMaximized = false;
        } else {
            lastX = stage.getX();
            lastY = stage.getY();
            lastWidth = stage.getWidth();
            lastHeight = stage.getHeight();

            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());
            isMaximized = true;
        }
    }

    public static void closeApp() {
        if (primaryStage != null) {
            primaryStage.close();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

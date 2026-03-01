package com.app.controller;

import com.app.MainApp;
import com.app.util.DatabaseManager;
import com.app.util.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {

    @FXML
    private AnchorPane rootPane;

    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    @FXML
    public void initialize() {
        // Initialization if needed
    }

    @FXML
    public void loginAction(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setStyle("-fx-text-fill: #ff4757;");
            messageLabel.setText("Please enter both username and password!");
            return;
        }

        if (authenticate(username, password)) {
            messageLabel.setStyle("-fx-text-fill: #2ed573;");
            messageLabel.setText("Login successful! Welcome " + username);
            // Here you can transition to the main dashboard of the application

            // Transition to Dashboard
            try {
                MainApp.setRoot("dashboard");
            } catch (IOException e) {
                messageLabel.setText("Error loading dashboard: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            messageLabel.setStyle("-fx-text-fill: #ff4757;");
            if (lastDbError != null) {
                messageLabel.setText("DB Error: " + lastDbError);
            } else {
                messageLabel.setText("Invalid credentials. Try again.");
            }
        }
    }

    private String lastDbError = null;

    private boolean authenticate(String username, String password) {
        lastDbError = null;
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) {
                lastDbError = "Database connection object is null. Check URL/Port.";
                System.err.println("Database connection is null.");
                return false;
            }

            try (PreparedStatement pst = conn.prepareStatement(query)) {
                pst.setString(1, username);
                pst.setString(2, password);

                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        UserSession.setInstance(
                                getStringSafe(rs, "username"),
                                getStringSafe(rs, "role"),
                                getStringSafe(rs, "first_name"),
                                getStringSafe(rs, "last_name"),
                                getStringSafe(rs, "email"),
                                getStringSafe(rs, "phone"),
                                getStringSafe(rs, "gender"),
                                getStringSafe(rs, "profile_image"));
                        return true;
                    }
                    return false;
                }
            }
        } catch (SQLException e) {
            lastDbError = e.getMessage();
            e.printStackTrace();
            return false;
        }
    }

    @FXML
    public void closeApp(ActionEvent event) {
        MainApp.closeApp();
    }

    @FXML
    public void onWindowPressed(MouseEvent event) {
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
    }

    @FXML
    public void onWindowDragged(MouseEvent event) {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        if (!MainApp.isMaximized) {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        }
    }

    @FXML
    public void handleMinimize(ActionEvent event) {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    public void handleMaximize(ActionEvent event) {
        MainApp.toggleMaximize((Stage) rootPane.getScene().getWindow());
    }

    @FXML
    public void handleClose(ActionEvent event) {
        MainApp.closeApp();
    }

    @FXML
    public void openRegisterPage(MouseEvent event) {
        try {
            MainApp.setRoot("register");
        } catch (IOException e) {
            messageLabel.setText("Error loading register page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String getStringSafe(ResultSet rs, String column) throws SQLException {
        String val = rs.getString(column);
        return val == null ? "" : val;
    }
}

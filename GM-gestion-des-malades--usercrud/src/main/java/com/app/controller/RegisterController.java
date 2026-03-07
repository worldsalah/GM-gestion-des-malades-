package com.app.controller;

import com.app.MainApp;
import com.app.util.DatabaseManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.app.util.FaceRecognitionService;

public class RegisterController {

    @FXML
    private AnchorPane rootPane;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private ComboBox<String> genderBox;

    @FXML
    private ComboBox<String> roleBox;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    private double xOffset = 0;
    private double yOffset = 0;

    private FaceRecognitionService faceService = new FaceRecognitionService();
    private String capturedFaceTemplate = null;

    private String registrationError = null;

    @FXML
    public void registerAction(ActionEvent event) {
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String gender = genderBox.getValue();
        String role = roleBox.getValue();
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phone.isEmpty() ||
                gender == null || role == null || username.isEmpty() || password.isEmpty()) {
            messageLabel.setStyle("-fx-text-fill: #ff4757;");
            messageLabel.setText("Please fill in all fields!");
            return;
        }

        if (registerUser(username, password, firstName, lastName, email, phone, gender, role)) {
            messageLabel.setStyle("-fx-text-fill: #2ed573;");
            messageLabel.setText("✅ REGISTRATION SUCCESSFUL! You can now login.");
            // Clear fields for visual feedback
            firstNameField.clear();
            lastNameField.clear();
            emailField.clear();
            phoneField.clear();
            usernameField.clear();
            passwordField.clear();
        } else {
            messageLabel.setStyle("-fx-text-fill: #ff4757;");
            if (registrationError != null) {
                messageLabel.setText("❌ Error: " + registrationError);
            } else {
                messageLabel.setText("❌ Registration failed. System error.");
            }
        }
    }

    private boolean registerUser(String username, String password, String firstName, String lastName, String email,
            String phone, String gender, String role) {
        registrationError = null;
        // Check for common columns and ensure face_template is included.
        // We add 'profile_image' as well just in case it's required.
        String query = "INSERT INTO users (username, password, first_name, last_name, email, phone, gender, role, face_template, profile_image) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) {
                registrationError = "Could not connect to database.";
                return false;
            }
            try (PreparedStatement pst = conn.prepareStatement(query)) {
                pst.setString(1, username);
                pst.setString(2, password);
                pst.setString(3, firstName);
                pst.setString(4, lastName);
                pst.setString(5, email);
                pst.setString(6, phone);
                pst.setString(7, gender);
                pst.setString(8, role);
                pst.setString(9, capturedFaceTemplate);
                pst.setString(10, ""); // Default profile image
                int affectedRows = pst.executeUpdate();
                return affectedRows > 0;
            }
        } catch (SQLException e) {
            System.err.println("❌ Registration SQL Error: " + e.getMessage());
            e.printStackTrace();
            registrationError = e.getMessage();
            return false;
        }
    }

    @FXML
    public void backToLogin(MouseEvent event) {
        try {
            MainApp.setRoot("login");
        } catch (IOException e) {
            e.printStackTrace();
        }
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
    public void handleGoogleSignup(ActionEvent event) {
        System.out.println("Google Signup Initiated...");
        messageLabel.setStyle("-fx-text-fill: #3b82f6;");
        messageLabel.setText("Signing up with Google...");
    }

    @FXML
    public void handleFacebookSignup(ActionEvent event) {
        System.out.println("Facebook Signup Initiated...");
        messageLabel.setStyle("-fx-text-fill: #1877f2;");
        messageLabel.setText("Signing up with Facebook...");
    }

    @FXML
    public void handleFaceRegistration(ActionEvent event) {
        System.out.println("Face Registration Initiated...");
        messageLabel.setStyle("-fx-text-fill: #6366f1;");
        messageLabel.setText("Position your face in front of the camera...");

        capturedFaceTemplate = faceService.captureFaceTemplate();

        if (capturedFaceTemplate != null) {
            messageLabel.setStyle("-fx-text-fill: #10b981;");
            messageLabel.setText("✅ Face captured successfully! Complete the form to finish.");
        } else {
            messageLabel.setStyle("-fx-text-fill: #ef4444;");
            messageLabel.setText("❌ Failed to capture face. Please try again.");
        }
    }
}

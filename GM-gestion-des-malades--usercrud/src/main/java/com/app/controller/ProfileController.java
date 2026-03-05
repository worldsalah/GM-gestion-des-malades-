package com.app.controller;

import com.app.MainApp;
import com.app.util.DatabaseManager;
import com.app.util.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ProfileController {

    @FXML
    private AnchorPane rootPane;

    @FXML
    private ImageView profileImage;

    @FXML
    private Label nameLabelTop;

    @FXML
    private TextField usernameVal;

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

    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    public void initialize() {
        loadUserData();
    }

    private void loadUserData() {
        UserSession session = UserSession.getInstance();
        if (session != null) {
            String fullName = session.getFirstName() + " " + session.getLastName();
            if (nameLabelTop != null)
                nameLabelTop.setText("Dr. " + fullName);

            usernameVal.setText(session.getUsername());
            firstNameField.setText(session.getFirstName());
            lastNameField.setText(session.getLastName());
            emailField.setText(session.getEmail());
            phoneField.setText(session.getPhone());
            genderBox.setValue(session.getGender());
            roleBox.setValue(session.getRole());

            try {
                String imagePath = session.getProfileImagePath();
                if (imagePath != null && !imagePath.isEmpty()) {
                    Image image = new Image("file:" + imagePath);
                    profileImage.setImage(image);
                } else {
                    var resource = getClass().getResourceAsStream("/com/app/img/profile.png");
                    if (resource != null) {
                        profileImage.setImage(new Image(resource));
                    }
                }
            } catch (Exception e) {
                System.err.println("Could not load profile image: " + e.getMessage());
            }
        }
    }

    @FXML
    public void uploadPhotoAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Photo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

        Stage stage = (Stage) rootPane.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            String newPath = selectedFile.getAbsolutePath();
            if (updateProfileImageInDb(newPath)) {
                UserSession.getInstance().setProfileImagePath(newPath);
                profileImage.setImage(new Image("file:" + newPath));
            }
        }
    }

    private boolean updateProfileImageInDb(String path) {
        String query = "UPDATE users SET profile_image = ? WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null)
                return false;
            try (PreparedStatement pst = conn.prepareStatement(query)) {
                pst.setString(1, path);
                pst.setString(2, UserSession.getInstance().getUsername());
                int rows = pst.executeUpdate();
                return rows > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @FXML
    public void saveChangesAction(ActionEvent event) {
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String gender = genderBox.getValue();
        String role = roleBox.getValue();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Name and Email are required.");
            return;
        }

        String query = "UPDATE users SET first_name = ?, last_name = ?, email = ?, phone = ?, gender = ?, role = ? WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pst = conn.prepareStatement(query)) {

            pst.setString(1, firstName);
            pst.setString(2, lastName);
            pst.setString(3, email);
            pst.setString(4, phone);
            pst.setString(5, gender);
            pst.setString(6, role);
            pst.setString(7, UserSession.getInstance().getUsername());

            int rows = pst.executeUpdate();
            if (rows > 0) {
                // Update Session
                UserSession session = UserSession.getInstance();
                session.setFirstName(firstName);
                session.setLastName(lastName);
                session.setEmail(email);
                session.setPhone(phone);
                session.setGender(gender);
                session.setRole(role);

                // Update UI Labels
                String fullName = firstName + " " + lastName;
                if (nameLabelTop != null)
                    nameLabelTop.setText("Dr. " + fullName);

                showAlert(Alert.AlertType.INFORMATION, "Success", "Profile updated successfully!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not update profile.");
        }
    }

    @FXML
    public void deleteAccountAction(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Account");
        alert.setHeaderText("Are you sure you want to delete your account?");
        alert.setContentText("This action cannot be undone.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String query = "DELETE FROM users WHERE username = ?";
                try (Connection conn = DatabaseManager.getConnection();
                        PreparedStatement pst = conn.prepareStatement(query)) {

                    pst.setString(1, UserSession.getInstance().getUsername());
                    pst.executeUpdate();

                    UserSession.cleanUserSession();
                    MainApp.setRoot("login");
                } catch (SQLException | IOException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error", "Could not delete account.");
                }
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    public void backToDashboard(ActionEvent event) {
        try {
            MainApp.setRoot("dashboard");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void logoutAction(ActionEvent event) {
        UserSession.cleanUserSession();
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

    @javafx.fxml.FXML
    public void navDashboard(javafx.scene.input.MouseEvent event) {
        try {
            com.app.MainApp.setRoot("dashboard");
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    @javafx.fxml.FXML
    public void navPatients(javafx.scene.input.MouseEvent event) {
        try {
            com.app.MainApp.setRoot("patients");
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    @javafx.fxml.FXML
    public void navAppointments(javafx.scene.input.MouseEvent event) {
        try {
            com.app.MainApp.setRoot("appointments");
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    @javafx.fxml.FXML
    public void navTasks(javafx.scene.input.MouseEvent event) {
        try {
            com.app.MainApp.setRoot("tasks");
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    @javafx.fxml.FXML
    public void navProfile(javafx.scene.input.MouseEvent event) {
        try {
            com.app.MainApp.setRoot("profile");
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    @javafx.fxml.FXML
    public void navLogout(javafx.scene.input.MouseEvent event) {
        try {
            com.app.MainApp.setRoot("login");
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}

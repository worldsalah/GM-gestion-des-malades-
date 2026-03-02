package com.app.controller;

import com.app.MainApp;
import com.app.util.UserSession;
import javafx.event.ActionEvent;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class DashboardController {

    @FXML
    private AnchorPane rootPane;

    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    private ImageView profileImage;

    @FXML
    private LineChart<String, Number> activityChart;

    @FXML
    private Label drNameLabel;

    @FXML
    private Label profileName;

    @FXML
    private VBox sidebarMenu;

    @FXML
    private Label profileSubtitle;

    @FXML
    public void initialize() {
        UserSession session = UserSession.getInstance();
        if (session != null) {
            // Set profile image
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

            // Set Name and Role
            drNameLabel
                    .setText("Hello " + session.getRole() + " " + session.getFirstName() + " " + session.getLastName());
            if (profileSubtitle != null) {
                profileSubtitle.setText(session.getRole() + " - UniLearn");
            }
        }

        // Setup Activity Chart
        setupActivityChart();
    }

    private void setupActivityChart() {
        XYChart.Series<String, Number> consultations = new XYChart.Series<>();
        consultations.setName("Consultations");
        consultations.getData().add(new XYChart.Data<>("Jan", 130));
        consultations.getData().add(new XYChart.Data<>("Feb", 110));
        consultations.getData().add(new XYChart.Data<>("Mar", 150));
        consultations.getData().add(new XYChart.Data<>("Apr", 140));
        consultations.getData().add(new XYChart.Data<>("May", 180));
        consultations.getData().add(new XYChart.Data<>("Jun", 160));
        consultations.getData().add(new XYChart.Data<>("Jul", 170));
        consultations.getData().add(new XYChart.Data<>("Aug", 150));
        consultations.getData().add(new XYChart.Data<>("Sep", 210));
        consultations.getData().add(new XYChart.Data<>("Oct", 190));
        consultations.getData().add(new XYChart.Data<>("Nov", 200));
        consultations.getData().add(new XYChart.Data<>("Dec", 180));

        XYChart.Series<String, Number> patients = new XYChart.Series<>();
        patients.setName("Patients");
        patients.getData().add(new XYChart.Data<>("Jan", 90));
        patients.getData().add(new XYChart.Data<>("Feb", 80));
        patients.getData().add(new XYChart.Data<>("Mar", 100));
        patients.getData().add(new XYChart.Data<>("Apr", 95));
        patients.getData().add(new XYChart.Data<>("May", 120));
        patients.getData().add(new XYChart.Data<>("Jun", 110));
        patients.getData().add(new XYChart.Data<>("Jul", 115));
        patients.getData().add(new XYChart.Data<>("Aug", 100));
        patients.getData().add(new XYChart.Data<>("Sep", 140));
        patients.getData().add(new XYChart.Data<>("Oct", 130));
        patients.getData().add(new XYChart.Data<>("Nov", 135));
        patients.getData().add(new XYChart.Data<>("Dec", 120));

        activityChart.getData().addAll(consultations, patients);
    }

    @FXML
    public void logoutAction(MouseEvent event) {
        UserSession.cleanUserSession();
        try {
            MainApp.setRoot("login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void openProfilePage(MouseEvent event) {
        try {
            MainApp.setRoot("profile");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void openPatientsPage(MouseEvent event) {
        try {
            MainApp.setRoot("patients");
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
}

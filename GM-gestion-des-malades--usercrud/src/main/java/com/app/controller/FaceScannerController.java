package com.app.controller;

import com.app.MainApp;
import com.app.util.DatabaseManager;
import com.app.util.FaceRecognitionService;
import com.app.util.UserSession;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FaceScannerController {

    @FXML
    private AnchorPane rootPane;
    @FXML
    private ImageView cameraView;
    @FXML
    private Label statusLabel;
    @FXML
    private Button scanButton;

    private VideoCapture capture;
    private ScheduledExecutorService timer;
    private FaceRecognitionService faceService = new FaceRecognitionService();
    private boolean isScanning = false;

    @FXML
    public void initialize() {
        startCamera();
    }

    private void startCamera() {
        capture = new VideoCapture(0);
        if (capture.isOpened()) {
            statusLabel.setText("Camera online. Position your face in the circle.");

            // Grab frames at 30 FPS
            timer = Executors.newSingleThreadScheduledExecutor();
            timer.scheduleAtFixedRate(() -> {
                Mat frame = new Mat();
                if (capture.read(frame) && !frame.empty()) {
                    Platform.runLater(() -> {
                        cameraView.setImage(faceService.mat2Image(frame));
                    });
                }
            }, 0, 33, TimeUnit.MILLISECONDS);
        } else {
            statusLabel.setText("❌ Error: Could not open camera.");
            scanButton.setDisable(true);
        }
    }

    @FXML
    public void handleScan(ActionEvent event) {
        if (isScanning)
            return;
        isScanning = true;

        statusLabel.setText("⏳ Scanning... Please stay still.");
        scanButton.setDisable(true);

        // Perform scan in a background thread to keep UI responsive
        new Thread(() -> {
            try {
                Mat frame = new Mat();
                capture.read(frame);
                if (frame.empty()) {
                    updateStatus("❌ Failed to capture frame.");
                    isScanning = false;
                    Platform.runLater(() -> scanButton.setDisable(false));
                    return;
                }

                String scannedTemplate = faceService.encodeFace(frame, faceService.detectFace(frame));
                if (scannedTemplate == null) {
                    updateStatus("❌ Failed to capture image.");
                    isScanning = false;
                    Platform.runLater(() -> scanButton.setDisable(false));
                    return;
                }

                updateStatus("🔍 Identifying you... Contacting server.");

                // Search database
                boolean found = searchUser(scannedTemplate);

                if (!found) {
                    updateStatus("❌ Identity not recognized.");
                    isScanning = false;
                    Platform.runLater(() -> scanButton.setDisable(false));
                }
            } catch (Exception e) {
                updateStatus("❌ System error: " + e.getMessage());
                isScanning = false;
                Platform.runLater(() -> scanButton.setDisable(false));
            }
        }).start();
    }

    private void updateStatus(String msg) {
        Platform.runLater(() -> statusLabel.setText(msg));
    }

    private boolean searchUser(String scannedTemplate) {
        String sql = "SELECT * FROM users WHERE face_template IS NOT NULL AND face_template != ''";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement st = conn.prepareStatement(sql);
                ResultSet rs = st.executeQuery()) {

            while (rs.next()) {
                String storedTemplate = rs.getString("face_template");
                if (faceService.verifyFace(scannedTemplate, storedTemplate)) {
                    // Success!
                    final String username = rs.getString("username");
                    final String firstName = rs.getString("first_name");

                    UserSession.setInstance(
                            username,
                            rs.getString("role"),
                            firstName,
                            rs.getString("last_name"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            rs.getString("gender"),
                            rs.getString("profile_image"));

                    updateStatus("✅ Welcome, " + firstName + "!");

                    Platform.runLater(() -> {
                        stopCamera();
                        try {
                            MainApp.setRoot("dashboard");
                            ((Stage) rootPane.getScene().getWindow()).close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @FXML
    public void handleCancel(ActionEvent event) {
        stopCamera();
        ((Stage) rootPane.getScene().getWindow()).close();
    }

    private void stopCamera() {
        if (timer != null && !timer.isShutdown()) {
            timer.shutdown();
            try {
                timer.awaitTermination(33, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
            }
        }
        if (capture != null && capture.isOpened()) {
            capture.release();
        }
    }
}

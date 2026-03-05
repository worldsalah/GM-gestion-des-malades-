package com.app.controller;

import com.app.MainApp;
import com.app.util.UserSession;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import com.app.util.DatabaseManager;
import java.time.LocalDate;

public class DashboardController {

    @FXML
    private AnchorPane rootPane;

    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    private VBox sidebarMenu;

    // --- Essential Cards ---
    @FXML
    private Label totalPatientsLabel;
    @FXML
    private Label todayApptsLabel;
    @FXML
    private Label todayRevenueLabel;
    @FXML
    private Label treatmentsMonthLabel;
    @FXML
    private Label pendingPaymentsLabel;

    // --- Schedule Table ---
    @FXML
    private TableView<ScheduleItem> scheduleTable;
    @FXML
    private TableColumn<ScheduleItem, String> colTime;
    @FXML
    private TableColumn<ScheduleItem, String> colPatient;
    @FXML
    private TableColumn<ScheduleItem, String> colTreatment;
    @FXML
    private TableColumn<ScheduleItem, String> colStatus;
    @FXML
    private TableColumn<ScheduleItem, Void> colAction;

    // --- Analytics Charts ---
    @FXML
    private BarChart<String, Number> monthlyRevenueChart;
    @FXML
    private BarChart<String, Number> treatmentRevenueChart;
    @FXML
    private PieChart paymentStatusChart;

    // --- Search ---
    @FXML
    private TextField searchField;

    private ObservableList<ScheduleItem> masterData = FXCollections.observableArrayList();
    private FilteredList<ScheduleItem> filteredData;

    // --- Recent Activity ---
    @FXML
    private VBox recentActivityContainer;

    @FXML
    public void initialize() {
        // Essential Cards Setup
        setupCards();

        // Schedule Table Setup
        setupScheduleTable();

        // Charts Setup
        setupCharts();

        // Recent Activity Setup
        setupRecentActivity();

        // Ensure table fills parent
        scheduleTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void setupCards() {
        // Load real statistics from DatabaseManager
        animateCounter(totalPatientsLabel, DatabaseManager.getTotalPatients());
        animateCounter(todayApptsLabel, DatabaseManager.getTodayAppointmentsCount());

        // Revenue counter with $ formatting
        double revenue = DatabaseManager.getTodayRevenue();
        IntegerProperty countProperty = new SimpleIntegerProperty(0);
        countProperty.addListener((obs, oldValue, newValue) -> {
            todayRevenueLabel.setText(String.format("%,d", newValue.intValue()));
        });
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(countProperty, 0)),
                new KeyFrame(Duration.millis(2000), new KeyValue(countProperty, (int) revenue, Interpolator.EASE_OUT)));
        timeline.play();

        animateCounter(treatmentsMonthLabel, DatabaseManager.getMonthlyTreatments());
        animateCounter(pendingPaymentsLabel, 0); // Placeholder for now
    }

    private void animateCounter(Label label, int targetValue) {
        IntegerProperty countProperty = new SimpleIntegerProperty(0);
        countProperty.addListener((obs, oldValue, newValue) -> {
            label.setText(String.format("%,d", newValue.intValue()));
        });

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(countProperty, 0)),
                new KeyFrame(Duration.millis(2000), new KeyValue(countProperty, targetValue, Interpolator.EASE_OUT)));
        timeline.play();
    }

    private void setupScheduleTable() {
        colTime.setCellValueFactory(new PropertyValueFactory<>("time"));
        colPatient.setCellValueFactory(new PropertyValueFactory<>("patient"));
        colTreatment.setCellValueFactory(new PropertyValueFactory<>("treatment"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Initialize FilteredList with masterData
        filteredData = new FilteredList<>(masterData, p -> true);

        // Add search listener
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(item -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                if (item.getPatient().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (item.getTreatment().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (item.getStatus().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        });

        // Wrap in SortedList for sorting support
        SortedList<ScheduleItem> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(scheduleTable.comparatorProperty());
        scheduleTable.setItems(sortedData);

        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    HBox box = new HBox(8);
                    box.setAlignment(Pos.CENTER_LEFT);
                    Circle dot = new Circle(4);

                    if (status.equalsIgnoreCase("Waiting"))
                        dot.setStyle("-fx-fill: #f59e0b;");
                    else if (status.equalsIgnoreCase("In Progress"))
                        dot.setStyle("-fx-fill: #3b82f6;");
                    else if (status.equalsIgnoreCase("Done"))
                        dot.setStyle("-fx-fill: #10b981;");
                    else
                        dot.setStyle("-fx-fill: #ef4444;"); // Cancelled

                    Label label = new Label(status);
                    label.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569;");
                    box.getChildren().addAll(dot, label);
                    setGraphic(box);
                    setText(null);
                }
            }
        });

        colAction.setCellFactory(column -> new TableCell<>() {
            private final Button btn = new Button();
            {
                btn.getStyleClass().add("btn-primary");
                btn.setStyle("-fx-padding: 5 15; -fx-font-size: 11px;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    ScheduleItem data = getTableView().getItems().get(getIndex());
                    String status = data.getStatus();

                    if (status.equalsIgnoreCase("Waiting")) {
                        btn.setText("Start");
                        btn.setStyle(
                                "-fx-background-color: #5352ed; -fx-text-fill: white; -fx-padding: 5 15; -fx-font-size: 11px;");
                        btn.setDisable(false);
                    } else if (status.equalsIgnoreCase("In Progress")) {
                        btn.setText("Finish");
                        btn.setStyle(
                                "-fx-background-color: #10b981; -fx-text-fill: white; -fx-padding: 5 15; -fx-font-size: 11px;");
                        btn.setDisable(false);
                    } else {
                        btn.setText("Done");
                        btn.setStyle(
                                "-fx-background-color: #f1f5f9; -fx-text-fill: #64748b; -fx-padding: 5 15; -fx-font-size: 11px;");
                        btn.setDisable(true);
                    }

                    setGraphic(btn);
                    setAlignment(Pos.CENTER);

                    btn.setOnAction(event -> {
                        handleStatusTransition(data);
                    });
                }
            }

            private void handleStatusTransition(ScheduleItem item) {
                String currentStatus = item.getStatus();
                String nextStatus = "";

                if (currentStatus.equalsIgnoreCase("Waiting")) {
                    nextStatus = "In Progress";
                } else if (currentStatus.equalsIgnoreCase("In Progress")) {
                    nextStatus = "Done";
                } else {
                    return; // Already Done or Cancelled
                }

                if (nextStatus.equals("Done")) {
                    TextInputDialog dialog = new TextInputDialog("0.00");
                    dialog.setTitle("Record Charge");
                    dialog.setHeaderText("Appointment Completed for " + item.getPatient());
                    dialog.setContentText("Enter charge amount ($):");

                    Optional<String> result = dialog.showAndWait();
                    if (result.isPresent()) {
                        try {
                            double amount = Double.parseDouble(result.get());
                            updateAppointmentStatusAndCharge(item.getId(), "Done", amount);
                        } catch (NumberFormatException e) {
                            showError("Invalid Input", "Please enter a valid numeric amount.");
                            return;
                        }
                    } else {
                        return; // User cancelled the dialog
                    }
                } else {
                    updateAppointmentStatusAndCharge(item.getId(), nextStatus, 0.0);
                }

                // Refresh table and cards
                setupScheduleTable();
                setupCards();
            }

            private void updateAppointmentStatusAndCharge(int appointmentId, String status, double charge) {
                String sql = "UPDATE appointments SET status = ?, charge_amount = ? WHERE id = ?";
                try (Connection conn = DatabaseManager.getConnection();
                        PreparedStatement st = conn.prepareStatement(sql)) {
                    st.setString(1, status);
                    st.setDouble(2, charge);
                    st.setInt(3, appointmentId);
                    st.executeUpdate();
                } catch (SQLException e) {
                    showError("Database Error", "Failed to update appointment: " + e.getMessage());
                }
            }
        });

        loadTodaySchedule();
    }

    private void loadTodaySchedule() {
        masterData.clear();
        String sql = "SELECT a.id, a.appointment_time, CONCAT(p.first_name, ' ', p.last_name) as patient_name, a.reason, a.status "
                + "FROM appointments a " +
                "JOIN patients p ON a.patient_id = p.id " +
                "WHERE a.appointment_date = CURRENT_DATE " +
                "ORDER BY a.appointment_time ASC";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement st = conn.prepareStatement(sql);
                ResultSet rs = st.executeQuery()) {

            while (rs.next()) {
                masterData.add(new ScheduleItem(
                        rs.getInt("id"),
                        rs.getString("appointment_time"),
                        rs.getString("patient_name"),
                        rs.getString("reason"),
                        rs.getString("status")));
            }
        } catch (SQLException e) {
            showError("Database Error", "Failed to load schedule: " + e.getMessage());
        }
    }

    private void setupCharts() {
        // 1. Monthly Revenue
        XYChart.Series<String, Number> revSeries = new XYChart.Series<>();
        revSeries.setName("Revenue");
        revSeries.getData().add(new XYChart.Data<>("Jan", 12000));
        revSeries.getData().add(new XYChart.Data<>("Feb", 15000));
        revSeries.getData().add(new XYChart.Data<>("Mar", 18500));
        revSeries.getData().add(new XYChart.Data<>("Apr", 22000));
        revSeries.getData().add(new XYChart.Data<>("May", 17000));
        revSeries.getData().add(new XYChart.Data<>("Jun", 24000));
        monthlyRevenueChart.getData().add(revSeries);

        // 2. Revenue by Treatment
        XYChart.Series<String, Number> treatSeries = new XYChart.Series<>();
        treatSeries.setName("Treatments");
        treatSeries.getData().add(new XYChart.Data<>("Extraction", 5000));
        treatSeries.getData().add(new XYChart.Data<>("Filling", 8000));
        treatSeries.getData().add(new XYChart.Data<>("Cleaning", 3500));
        treatSeries.getData().add(new XYChart.Data<>("Braces", 15000));
        treatSeries.getData().add(new XYChart.Data<>("Implants", 25000));
        treatmentRevenueChart.getData().add(treatSeries);

        // 3. Payment Status Pie Chart
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                new PieChart.Data("Paid", 75),
                new PieChart.Data("Pending", 20),
                new PieChart.Data("Overdue", 5));
        paymentStatusChart.setData(pieData);
    }

    private void setupRecentActivity() {
        addActivityItem("green", "New Patient Added", "Sarah Jenkins registered today.", "10 mins ago");
        addActivityItem("blue", "Treatment Completed", "Omar Zaki - Root Canal.", "2 hours ago");
        addActivityItem("orange", "Payment Received", "$500 from Ali Mahmoud.", "5 hours ago");
        addActivityItem("purple", "Appointment Updated", "Nour Said rescheduled to tomorrow.", "1 day ago");
    }

    private void addActivityItem(String colorType, String title, String desc, String time) {
        HBox box = new HBox(15);
        box.setAlignment(Pos.CENTER_LEFT);

        Circle dot = new Circle(8);
        if (colorType.equals("green"))
            dot.setStyle("-fx-fill: #10b981;");
        else if (colorType.equals("blue"))
            dot.setStyle("-fx-fill: #3b82f6;");
        else if (colorType.equals("orange"))
            dot.setStyle("-fx-fill: #f59e0b;");
        else
            dot.setStyle("-fx-fill: #8b5cf6;");

        VBox textVBox = new VBox(3);
        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b;");
        Label descLbl = new Label(desc);
        descLbl.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");
        textVBox.getChildren().addAll(titleLbl, descLbl);

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Label timeLbl = new Label(time);
        timeLbl.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");

        box.getChildren().addAll(dot, textVBox, spacer, timeLbl);
        recentActivityContainer.getChildren().add(box);
    }

    // --- Navigation & Window Handling ---
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
    public void openAppointmentsPage(Event event) {
        try {
            MainApp.setRoot("appointments");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void openTasksPage(Event event) {
        try {
            MainApp.setRoot("tasks");
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

    // Inner class for Table Data
    public static class ScheduleItem {
        private int id;
        private String time;
        private String patient;
        private String treatment;
        private String status;

        public ScheduleItem(int id, String time, String patient, String treatment, String status) {
            this.id = id;
            this.time = time;
            this.patient = patient;
            this.treatment = treatment;
            this.status = status;
        }

        public int getId() {
            return id;
        }

        public String getTime() {
            return time;
        }

        public String getPatient() {
            return patient;
        }

        public String getTreatment() {
            return treatment;
        }

        public String getStatus() {
            return status;
        }
    }

    @javafx.fxml.FXML
    public void navDashboard(javafx.scene.input.MouseEvent event) {
        try {
            com.app.MainApp.setRoot("dashboard");
        } catch (java.io.IOException e) {
            e.printStackTrace();
            showError("Navigation Error", "Could not load dashboard: " + e.getMessage());
        }
    }

    @javafx.fxml.FXML
    public void navPatients(javafx.scene.input.MouseEvent event) {
        try {
            com.app.MainApp.setRoot("patients");
        } catch (java.io.IOException e) {
            e.printStackTrace();
            showError("Navigation Error", "Could not load patients: " + e.getMessage());
        }
    }

    @javafx.fxml.FXML
    public void navAppointments(javafx.scene.input.MouseEvent event) {
        try {
            com.app.MainApp.setRoot("appointments");
        } catch (java.io.IOException e) {
            e.printStackTrace();
            showError("Navigation Error", "Could not load appointments: " + e.getMessage());
        }
    }

    @javafx.fxml.FXML
    public void navTasks(javafx.scene.input.MouseEvent event) {
        try {
            com.app.MainApp.setRoot("tasks");
        } catch (java.io.IOException e) {
            e.printStackTrace();
            showError("Navigation Error", "Could not load tasks: " + e.getMessage());
        }
    }

    @javafx.fxml.FXML
    public void navProfile(javafx.scene.input.MouseEvent event) {
        try {
            com.app.MainApp.setRoot("profile");
        } catch (java.io.IOException e) {
            e.printStackTrace();
            showError("Navigation Error", "Could not load profile: " + e.getMessage());
        }
    }

    @javafx.fxml.FXML
    public void navLogout(javafx.scene.input.MouseEvent event) {
        try {
            com.app.MainApp.setRoot("login");
        } catch (java.io.IOException e) {
            e.printStackTrace();
            showError("Navigation Error", "Could not load login: " + e.getMessage());
        }
    }

    private void showError(String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

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
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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
        // Animate dummy values for demo purposes
        animateCounter(totalPatientsLabel, 1284);
        animateCounter(todayApptsLabel, 42);
        animateCounter(todayRevenueLabel, 8560);
        animateCounter(treatmentsMonthLabel, 340);
        animateCounter(pendingPaymentsLabel, 15);
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
                    if (data.getStatus().equalsIgnoreCase("Done") || data.getStatus().equalsIgnoreCase("Cancelled")) {
                        btn.setText("View");
                        btn.setStyle(
                                "-fx-background-color: #f1f5f9; -fx-text-fill: #64748b; -fx-padding: 5 15; -fx-font-size: 11px;");
                    } else {
                        btn.setText("Start");
                        btn.setStyle(
                                "-fx-background-color: #5352ed; -fx-text-fill: white; -fx-padding: 5 15; -fx-font-size: 11px;");
                    }
                    setGraphic(btn);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        ObservableList<ScheduleItem> dummySchedule = FXCollections.observableArrayList(
                new ScheduleItem("09:00", "Ahmed Hassan", "Filling", "Waiting"),
                new ScheduleItem("09:30", "Sara Ali", "Extraction", "Done"),
                new ScheduleItem("10:00", "Omar Zaki", "Checkup", "In Progress"),
                new ScheduleItem("10:45", "Nour Said", "Cleaning", "Waiting"),
                new ScheduleItem("11:30", "Mona Youssef", "Braces Adjust", "Cancelled"));
        scheduleTable.setItems(dummySchedule);
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
        private String time;
        private String patient;
        private String treatment;
        private String status;

        public ScheduleItem(String time, String patient, String treatment, String status) {
            this.time = time;
            this.patient = patient;
            this.treatment = treatment;
            this.status = status;
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
}

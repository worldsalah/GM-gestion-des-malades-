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

    @FXML
    private VBox revenueCard;
    @FXML
    private VBox pendingPaymentsCard;
    @FXML
    private VBox analyticsColumn;

    // --- Enhanced Revenue Statistics Section ---
    @FXML
    private VBox revenueStatisticsContainer;
    @FXML
    private HBox revenueCardsRow;
    @FXML
    private VBox todayRevenueCard;
    @FXML
    private VBox weeklyRevenueCard;
    @FXML
    private VBox monthlyRevenueCard;
    @FXML
    private VBox yearlyRevenueCard;
    @FXML
    private Label todayRevenueBigLabel;
    @FXML
    private Label weeklyRevenueBigLabel;
    @FXML
    private Label monthlyRevenueBigLabel;
    @FXML
    private Label yearlyRevenueBigLabel;
    @FXML
    private Label todayRevenueChangeLabel;
    @FXML
    private Label weeklyRevenueChangeLabel;
    @FXML
    private Label monthlyRevenueChangeLabel;
    @FXML
    private Label yearlyRevenueChangeLabel;

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
    private BarChart<String, Number> doctorRevenueChart;
    @FXML
    private PieChart paymentStatusChart;

    // --- Revenue Breakdown Components ---
    @FXML
    private VBox dailyRevenueContainer;
    @FXML
    private VBox monthlyRevenueContainer;
    @FXML
    private TableView<DailyRevenueRow> dailyRevenueTable;
    @FXML
    private TableColumn<DailyRevenueRow, String> dailyDateCol;
    @FXML
    private TableColumn<DailyRevenueRow, Double> dailyRevenueCol;
    @FXML
    private TableColumn<DailyRevenueRow, Integer> dailyAppointmentsCol;
    @FXML
    private TableView<MonthlyRevenueRow> monthlyRevenueTable;
    @FXML
    private TableColumn<MonthlyRevenueRow, String> monthlyMonthCol;
    @FXML
    private TableColumn<MonthlyRevenueRow, Double> monthlyRevenueCol;
    @FXML
    private TableColumn<MonthlyRevenueRow, Integer> monthlyAppointmentsCol;
    @FXML
    private TableColumn<MonthlyRevenueRow, Double> monthlyAvgCol;

    // --- Revenue Summary Cards ---
    @FXML
    private Label yesterdayRevenueLabel;
    @FXML
    private Label thisMonthRevenueLabel;
    @FXML
    private Label lastMonthRevenueLabel;
    @FXML
    private Label dailyChangeLabel;
    @FXML
    private Label monthlyChangeLabel;

    // --- Search ---
    @FXML
    private TextField searchField;

    private ObservableList<ScheduleItem> masterData = FXCollections.observableArrayList();
    private FilteredList<ScheduleItem> filteredData;

    // --- Recent Activity ---
    @FXML
    private VBox recentActivityContainer;

    // --- SMS Notification Status ---
    @FXML
    private VBox notificationCard;
    @FXML
    private Label notificationStatusBadge;
    @FXML
    private Label notificationStatusLabel;
    @FXML
    private TableView<NotifRow> notificationPatientsTable;
    @FXML
    private TableColumn<NotifRow, Integer> notifColNum;
    @FXML
    private TableColumn<NotifRow, String> notifColPatient;
    @FXML
    private TableColumn<NotifRow, String> notifColTime;

    @FXML
    public void initialize() {
        // Essential Cards Setup
        setupCards();

        // Schedule Table Setup
        setupScheduleTable();

        // Charts Setup
        setupCharts();

        // Revenue Tables Setup
        setupRevenueTables();

        // Prominent Revenue Statistics Setup
        setupRevenueStatistics();

        // Recent Activity Setup
        setupRecentActivity();

        // SMS Notification Status
        loadNotificationStatus();

        // Security checks
        applySecurityRestrictions();

        // Ensure table fills parent
        scheduleTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void applySecurityRestrictions() {
        if (UserSession.getInstance() == null)
            return;
        String role = UserSession.getInstance().getRole();
        if (role != null && (role.equalsIgnoreCase("Secretaire") || role.equalsIgnoreCase("Stagiaire"))) {
            if (revenueCard != null) {
                revenueCard.setVisible(false);
                revenueCard.setManaged(false);
            }
            if (pendingPaymentsCard != null) {
                pendingPaymentsCard.setVisible(false);
                pendingPaymentsCard.setManaged(false);
            }
            if (analyticsColumn != null) {
                analyticsColumn.setVisible(false);
                analyticsColumn.setManaged(false);
            }
        }
    }

    private void setupCards() {
        // Determine the doctorId filter based on role
        Integer doctorId = null;
        String role = UserSession.getInstance() != null ? UserSession.getInstance().getRole() : null;
        if (role != null && (role.equalsIgnoreCase("Doctor") || role.equalsIgnoreCase("Stagiaire"))) {
            doctorId = UserSession.getInstance().getUserId();
        }

        final Integer filterDoctorId = doctorId;

        // Load real statistics from DatabaseManager — filtered by doctorId if applicable
        animateCounter(totalPatientsLabel, DatabaseManager.getTotalPatients(filterDoctorId));
        animateCounter(todayApptsLabel, DatabaseManager.getTodayAppointmentsCount(filterDoctorId));

        // Enhanced revenue counter with doctor-specific formatting
        double revenue = DatabaseManager.getTodayRevenue(filterDoctorId);
        IntegerProperty countProperty = new SimpleIntegerProperty(0);
        countProperty.addListener((obs, oldValue, newValue) -> {
            String roleText = (role != null && role.equalsIgnoreCase("Doctor")) ? " (Your Revenue)" : "";
            todayRevenueLabel.setText(String.format("$%,d%s", newValue.intValue(), roleText));
        });
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(countProperty, 0)),
                new KeyFrame(Duration.millis(2000), new KeyValue(countProperty, (int) revenue, Interpolator.EASE_OUT)));
        timeline.play();

        animateCounter(treatmentsMonthLabel, DatabaseManager.getMonthlyTreatments(filterDoctorId));
        
        // Enhanced pending payments - show actual pending revenue
        double pendingRevenue = getPendingRevenue(filterDoctorId);
        animateCounter(pendingPaymentsLabel, (int) pendingRevenue);
        
        // Setup revenue comparison cards
        setupRevenueComparisonCards(filterDoctorId);
        
        // Update card titles based on role
        if (role != null && role.equalsIgnoreCase("Doctor")) {
            if (todayRevenueLabel != null) {
                todayRevenueLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #10b981;");
            }
        }
    }

    private void setupRevenueComparisonCards(Integer doctorId) {
        // Daily comparison (today vs yesterday)
        Object[] dailyComparison = DatabaseManager.getRevenueComparison(doctorId, false);
        double todayRevenue = (Double) dailyComparison[0];
        double yesterdayRevenue = (Double) dailyComparison[1];
        double dailyChange = (Double) dailyComparison[2];
        
        if (todayRevenueLabel != null) {
            todayRevenueLabel.setText(String.format("$%.2f", todayRevenue));
        }
        if (yesterdayRevenueLabel != null) {
            yesterdayRevenueLabel.setText(String.format("$%.2f", yesterdayRevenue));
        }
        if (dailyChangeLabel != null) {
            String changeText = String.format("%+.1f%%", dailyChange);
            if (dailyChange > 0) {
                dailyChangeLabel.setText("↑ " + changeText);
                dailyChangeLabel.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
            } else if (dailyChange < 0) {
                dailyChangeLabel.setText("↓ " + changeText);
                dailyChangeLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
            } else {
                dailyChangeLabel.setText("→ " + changeText);
                dailyChangeLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-weight: bold;");
            }
        }

        // Monthly comparison (this month vs last month)
        Object[] monthlyComparison = DatabaseManager.getRevenueComparison(doctorId, true);
        double thisMonthRevenue = (Double) monthlyComparison[0];
        double lastMonthRevenue = (Double) monthlyComparison[1];
        double monthlyChange = (Double) monthlyComparison[2];
        
        if (thisMonthRevenueLabel != null) {
            thisMonthRevenueLabel.setText(String.format("$%.2f", thisMonthRevenue));
        }
        if (lastMonthRevenueLabel != null) {
            lastMonthRevenueLabel.setText(String.format("$%.2f", lastMonthRevenue));
        }
        if (monthlyChangeLabel != null) {
            String changeText = String.format("%+.1f%%", monthlyChange);
            if (monthlyChange > 0) {
                monthlyChangeLabel.setText("↑ " + changeText);
                monthlyChangeLabel.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
            } else if (monthlyChange < 0) {
                monthlyChangeLabel.setText("↓ " + changeText);
                monthlyChangeLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
            } else {
                monthlyChangeLabel.setText("→ " + changeText);
                monthlyChangeLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-weight: bold;");
            }
        }
    }

    private void setupRevenueTables() {
        // Determine doctorId filter based on role
        Integer doctorId = null;
        String role = UserSession.getInstance() != null ? UserSession.getInstance().getRole() : null;
        if (role != null && (role.equalsIgnoreCase("Doctor") || role.equalsIgnoreCase("Stagiaire"))) {
            doctorId = UserSession.getInstance().getUserId();
        }

        // Setup Daily Revenue Table
        if (dailyRevenueTable != null) {
            dailyDateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
            dailyRevenueCol.setCellValueFactory(new PropertyValueFactory<>("revenue"));
            dailyAppointmentsCol.setCellValueFactory(new PropertyValueFactory<>("appointments"));

            // Format revenue column as currency
            dailyRevenueCol.setCellFactory(column -> new TableCell<DailyRevenueRow, Double>() {
                @Override
                protected void updateItem(Double amount, boolean empty) {
                    super.updateItem(amount, empty);
                    if (empty || amount == null) {
                        setText(null);
                    } else {
                        setText(String.format("$%.2f", amount));
                    }
                }
            });

            loadDailyRevenueData(doctorId);
        }

        // Setup Monthly Revenue Table
        if (monthlyRevenueTable != null) {
            monthlyMonthCol.setCellValueFactory(new PropertyValueFactory<>("month"));
            monthlyRevenueCol.setCellValueFactory(new PropertyValueFactory<>("revenue"));
            monthlyAppointmentsCol.setCellValueFactory(new PropertyValueFactory<>("appointments"));
            monthlyAvgCol.setCellValueFactory(new PropertyValueFactory<>("avgRevenue"));

            // Format revenue columns as currency
            monthlyRevenueCol.setCellFactory(column -> new TableCell<MonthlyRevenueRow, Double>() {
                @Override
                protected void updateItem(Double amount, boolean empty) {
                    super.updateItem(amount, empty);
                    if (empty || amount == null) {
                        setText(null);
                    } else {
                        setText(String.format("$%.2f", amount));
                    }
                }
            });

            monthlyAvgCol.setCellFactory(column -> new TableCell<MonthlyRevenueRow, Double>() {
                @Override
                protected void updateItem(Double amount, boolean empty) {
                    super.updateItem(amount, empty);
                    if (empty || amount == null) {
                        setText(null);
                    } else {
                        setText(String.format("$%.2f", amount));
                    }
                }
            });

            loadMonthlyRevenueData(doctorId);
        }
    }

    private void loadDailyRevenueData(Integer doctorId) {
        if (dailyRevenueTable == null) return;

        Object[][] dailyData = DatabaseManager.getDailyRevenueData(doctorId);
        ObservableList<DailyRevenueRow> data = FXCollections.observableArrayList();

        for (Object[] row : dailyData) {
            String date = row[0].toString();
            double revenue = ((Number) row[1]).doubleValue();
            int appointments = ((Number) row[2]).intValue();
            data.add(new DailyRevenueRow(date, revenue, appointments));
        }

        dailyRevenueTable.setItems(data);
    }

    private void loadMonthlyRevenueData(Integer doctorId) {
        if (monthlyRevenueTable == null) return;

        Object[][] monthlyData = DatabaseManager.getMonthlyRevenueBreakdown(doctorId);
        ObservableList<MonthlyRevenueRow> data = FXCollections.observableArrayList();

        for (Object[] row : monthlyData) {
            String monthName = row[1].toString();
            double revenue = ((Number) row[2]).doubleValue();
            int appointments = ((Number) row[3]).intValue();
            double avgRevenue = ((Number) row[4]).doubleValue();
            data.add(new MonthlyRevenueRow(monthName, revenue, appointments, avgRevenue));
        }

        monthlyRevenueTable.setItems(data);
    }

    private void setupRevenueStatistics() {
        // Determine doctorId filter based on role
        Integer doctorId = null;
        String role = UserSession.getInstance() != null ? UserSession.getInstance().getRole() : null;
        if (role != null && (role.equalsIgnoreCase("Doctor") || role.equalsIgnoreCase("Stagiaire"))) {
            doctorId = UserSession.getInstance().getUserId();
        }

        // Get revenue data for all periods
        double todayRevenue = DatabaseManager.getTodayRevenue(doctorId);
        double weeklyRevenue = DatabaseManager.getWeeklyRevenue(doctorId);
        double monthlyRevenue = DatabaseManager.getMonthlyRevenueData(doctorId)[java.time.LocalDate.now().getMonthValue() - 1];
        double yearlyRevenue = DatabaseManager.getYearlyRevenue(doctorId);

        // Get comparison data
        double yesterdayRevenue = DatabaseManager.getTodayRevenue(doctorId) - todayRevenue; // Simplified
        double previousWeekRevenue = DatabaseManager.getPreviousWeekRevenue(doctorId);
        Object[] monthlyComparison = DatabaseManager.getRevenueComparison(doctorId, true);
        double lastMonthRevenue = (Double) monthlyComparison[1];
        double previousYearRevenue = DatabaseManager.getPreviousYearRevenue(doctorId);

        // Setup Today Revenue Card
        if (todayRevenueBigLabel != null) {
            todayRevenueBigLabel.setText(String.format("$%.2f", todayRevenue));
            todayRevenueBigLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #10b981;");
        }
        if (todayRevenueChangeLabel != null) {
            double change = yesterdayRevenue == 0 ? 0 : ((todayRevenue - yesterdayRevenue) / yesterdayRevenue) * 100;
            todayRevenueChangeLabel.setText(formatChangeIndicator(change));
            todayRevenueChangeLabel.setStyle(getChangeStyle(change));
        }

        // Setup Weekly Revenue Card
        if (weeklyRevenueBigLabel != null) {
            weeklyRevenueBigLabel.setText(String.format("$%.2f", weeklyRevenue));
            weeklyRevenueBigLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #3b82f6;");
        }
        if (weeklyRevenueChangeLabel != null) {
            double change = previousWeekRevenue == 0 ? 0 : ((weeklyRevenue - previousWeekRevenue) / previousWeekRevenue) * 100;
            weeklyRevenueChangeLabel.setText(formatChangeIndicator(change));
            weeklyRevenueChangeLabel.setStyle(getChangeStyle(change));
        }

        // Setup Monthly Revenue Card
        if (monthlyRevenueBigLabel != null) {
            monthlyRevenueBigLabel.setText(String.format("$%.2f", monthlyRevenue));
            monthlyRevenueBigLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #8b5cf6;");
        }
        if (monthlyRevenueChangeLabel != null) {
            double change = lastMonthRevenue == 0 ? 0 : ((monthlyRevenue - lastMonthRevenue) / lastMonthRevenue) * 100;
            monthlyRevenueChangeLabel.setText(formatChangeIndicator(change));
            monthlyRevenueChangeLabel.setStyle(getChangeStyle(change));
        }

        // Setup Yearly Revenue Card
        if (yearlyRevenueBigLabel != null) {
            yearlyRevenueBigLabel.setText(String.format("$%.2f", yearlyRevenue));
            yearlyRevenueBigLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #f59e0b;");
        }
        if (yearlyRevenueChangeLabel != null) {
            double change = previousYearRevenue == 0 ? 0 : ((yearlyRevenue - previousYearRevenue) / previousYearRevenue) * 100;
            yearlyRevenueChangeLabel.setText(formatChangeIndicator(change));
            yearlyRevenueChangeLabel.setStyle(getChangeStyle(change));
        }

        // Apply styling to revenue cards
        styleRevenueCards();
    }

    private String formatChangeIndicator(double change) {
        if (change > 0) {
            return String.format("↑ +%.1f%%", change);
        } else if (change < 0) {
            return String.format("↓ %.1f%%", change);
        } else {
            return "→ 0.0%";
        }
    }

    private String getChangeStyle(double change) {
        if (change > 0) {
            return "-fx-text-fill: #10b981; -fx-font-weight: bold; -fx-font-size: 14px;";
        } else if (change < 0) {
            return "-fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-font-size: 14px;";
        } else {
            return "-fx-text-fill: #6b7280; -fx-font-weight: bold; -fx-font-size: 14px;";
        }
    }

    private void styleRevenueCards() {
        // Style individual revenue cards with prominent appearance
        if (todayRevenueCard != null) {
            todayRevenueCard.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-border-radius: 15; -fx-border-color: #e5e7eb; -fx-border-width: 2; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 15, 0, 0, 5); -fx-padding: 25;");
        }
        if (weeklyRevenueCard != null) {
            weeklyRevenueCard.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-border-radius: 15; -fx-border-color: #e5e7eb; -fx-border-width: 2; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 15, 0, 0, 5); -fx-padding: 25;");
        }
        if (monthlyRevenueCard != null) {
            monthlyRevenueCard.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-border-radius: 15; -fx-border-color: #e5e7eb; -fx-border-width: 2; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 15, 0, 0, 5); -fx-padding: 25;");
        }
        if (yearlyRevenueCard != null) {
            yearlyRevenueCard.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-border-radius: 15; -fx-border-color: #e5e7eb; -fx-border-width: 2; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 15, 0, 0, 5); -fx-padding: 25;");
        }

        // Style the container to be more prominent at the top of analytics
        if (revenueStatisticsContainer != null) {
            revenueStatisticsContainer.setStyle("-fx-background-color: linear-gradient(to bottom, #f8fafc, #f1f5f9); -fx-background-radius: 20; -fx-padding: 30; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 20, 0, 0, 8);");
        }
        if (revenueCardsRow != null) {
            revenueCardsRow.setStyle("-fx-spacing: 20; -fx-alignment: center; -fx-padding: 10;");
        }
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
                    // Create a more sophisticated charge dialog
                    Dialog<Double> chargeDialog = new Dialog<>();
                    chargeDialog.setTitle("Record Payment");
                    chargeDialog.setHeaderText("Appointment Completed for " + item.getPatient() + "\nEnter the amount charged for this treatment:");

                    // Set the button types
                    ButtonType confirmButtonType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
                    ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
                    chargeDialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, cancelButtonType);

                    // Create the amount input field
                    TextField amountField = new TextField();
                    amountField.setPromptText("0.00");
                    amountField.setStyle("-fx-font-size: 16px; -fx-padding: 10px;");

                    // Add quick amount buttons
                    HBox quickAmountBox = new HBox(10);
                    Button[] quickButtons = {
                        new Button("$50"),
                        new Button("$100"),
                        new Button("$150"),
                        new Button("$200")
                    };
                    
                    for (Button btn : quickButtons) {
                        btn.setStyle("-fx-background-color: #f0f0f0; -fx-cursor: hand;");
                        btn.setOnAction(e -> amountField.setText(btn.getText().replace("$", "")));
                    }
                    quickAmountBox.getChildren().addAll(quickButtons);

                    // Create layout
                    VBox vbox = new VBox(15);
                    vbox.getChildren().addAll(
                        new Label("Amount ($)"),
                        amountField,
                        new Label("Quick amounts:"),
                        quickAmountBox
                    );

                    chargeDialog.getDialogPane().setContent(vbox);

                    // Convert the result to double when the confirm button is clicked
                    chargeDialog.setResultConverter(dialogButton -> {
                        if (dialogButton == confirmButtonType) {
                            try {
                                String amountText = amountField.getText().trim();
                                if (amountText.isEmpty()) {
                                    return 0.0;
                                }
                                return Double.parseDouble(amountText);
                            } catch (NumberFormatException e) {
                                showError("Invalid Input", "Please enter a valid numeric amount.");
                                return null;
                            }
                        }
                        return null;
                    });

                    // Show the dialog and wait for the result
                    Optional<Double> result = chargeDialog.showAndWait();
                    if (result.isPresent()) {
                        double amount = result.get();
                        if (amount >= 0) {
                            updateAppointmentStatusAndCharge(item.getId(), "Done", amount);
                            
                            // Show success message with revenue update
                            if (amount > 0) {
                                showRevenueUpdate(item.getPatient(), amount);
                            }
                        } else {
                            showError("Invalid Amount", "Amount cannot be negative.");
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
                setupRevenueTables(); // Refresh revenue tables
                setupCharts(); // Refresh charts
                setupRevenueStatistics(); // Refresh prominent revenue statistics
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
        String sql;
        int userId = UserSession.getInstance().getUserId();
        String role = UserSession.getInstance().getRole();

        if (role != null && role.equalsIgnoreCase("Doctor")) {
            sql = "SELECT a.id, a.appointment_time, CONCAT(p.first_name, ' ', p.last_name) as patient_name, a.reason, a.status "
                    + "FROM appointments a " +
                    "JOIN patients p ON a.patient_id = p.id " +
                    "WHERE a.appointment_date = CURRENT_DATE AND a.doctor_id = ? " +
                    "ORDER BY a.appointment_time ASC";
        } else {
            // Secretary and others see everything
            sql = "SELECT a.id, a.appointment_time, CONCAT(p.first_name, ' ', p.last_name) as patient_name, a.reason, a.status "
                    + "FROM appointments a " +
                    "JOIN patients p ON a.patient_id = p.id " +
                    "WHERE a.appointment_date = CURRENT_DATE " +
                    "ORDER BY a.appointment_time ASC";
        }

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement st = conn.prepareStatement(sql)) {

            if (role != null && role.equalsIgnoreCase("Doctor")) {
                st.setInt(1, userId);
            }

            try (ResultSet rs = st.executeQuery()) {
                masterData.clear();
                while (rs.next()) {
                    masterData.add(new ScheduleItem(
                            rs.getInt("id"),
                            rs.getString("appointment_time"),
                            rs.getString("patient_name"),
                            rs.getString("reason"),
                            rs.getString("status")));
                }
            }
        } catch (SQLException e) {
            showError("Database Error", "Failed to load schedule: " + e.getMessage());
        }
    }

    private void setupCharts() {
        // Determine doctorId filter based on role
        Integer doctorId = null;
        String role = UserSession.getInstance() != null ? UserSession.getInstance().getRole() : null;
        if (role != null && (role.equalsIgnoreCase("Doctor") || role.equalsIgnoreCase("Stagiaire"))) {
            doctorId = UserSession.getInstance().getUserId();
        }

        // First, ensure revenue statistics are prominently displayed at the top
        setupRevenueStatistics();

        // 1. Monthly Revenue (real data from DB)
        String[] months = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
        double[] monthlyRevenue = DatabaseManager.getMonthlyRevenueData(doctorId);
        XYChart.Series<String, Number> revSeries = new XYChart.Series<>();
        revSeries.setName("Revenue");
        for (int i = 0; i < months.length; i++) {
            revSeries.getData().add(new XYChart.Data<>(months[i], monthlyRevenue[i]));
        }
        monthlyRevenueChart.getData().add(revSeries);

        // 2. Revenue by Treatment/Reason (real data from DB)
        Object[][] reasonData = DatabaseManager.getRevenueByReasonData(doctorId);
        XYChart.Series<String, Number> treatSeries = new XYChart.Series<>();
        treatSeries.setName("Treatments");
        if (reasonData.length > 0) {
            for (Object[] row : reasonData) {
                String label = row[0] != null ? row[0].toString() : "Other";
                double amount = ((Number) row[1]).doubleValue();
                treatSeries.getData().add(new XYChart.Data<>(label, amount));
            }
        } else {
            // Placeholder when no data yet
            treatSeries.getData().add(new XYChart.Data<>("No Data", 0));
        }
        treatmentRevenueChart.getData().add(treatSeries);

        // 3. Doctor Revenue Comparison Chart (only for admin/secretary)
        if (doctorRevenueChart != null) {
            if (role != null && !role.equalsIgnoreCase("Doctor") && !role.equalsIgnoreCase("Stagiaire")) {
                // Show all doctors comparison for admin/secretary
                Object[][] doctorData = DatabaseManager.getAllDoctorsRevenueData();
                XYChart.Series<String, Number> doctorSeries = new XYChart.Series<>();
                doctorSeries.setName("Total Revenue by Doctor");
                
                if (doctorData.length > 0) {
                    for (Object[] row : doctorData) {
                        String doctorName = row[0].toString();
                        double revenue = ((Number) row[1]).doubleValue();
                        doctorSeries.getData().add(new XYChart.Data<>(doctorName, revenue));
                    }
                } else {
                    doctorSeries.getData().add(new XYChart.Data<>("No Doctors", 0));
                }
                doctorRevenueChart.getData().add(doctorSeries);
                doctorRevenueChart.setVisible(true);
                doctorRevenueChart.setManaged(true);
            } else {
                // Hide for doctors - they only see their own data in other charts
                doctorRevenueChart.setVisible(false);
                doctorRevenueChart.setManaged(false);
            }
        }

        // 4. Payment Status Pie Chart (real data from DB)
        int[] statusCounts = DatabaseManager.getPaymentStatusData(doctorId);
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        if (statusCounts[0] > 0)
            pieData.add(new PieChart.Data("Completed", statusCounts[0]));
        if (statusCounts[1] > 0)
            pieData.add(new PieChart.Data("Pending", statusCounts[1]));
        if (statusCounts[2] > 0)
            pieData.add(new PieChart.Data("Cancelled", statusCounts[2]));
        if (pieData.isEmpty()) {
            pieData.add(new PieChart.Data("No Data", 1));
        }
        paymentStatusChart.setData(pieData);

        // Ensure revenue statistics container is visible and positioned at the top
        if (revenueStatisticsContainer != null) {
            revenueStatisticsContainer.setVisible(true);
            revenueStatisticsContainer.setManaged(true);
            revenueStatisticsContainer.toFront(); // Bring to front for visibility
        }
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

    // ── Inner class for SMS Notification table ────────────────────────────────
    public static class NotifRow {
        private final javafx.beans.property.SimpleIntegerProperty num;
        private final javafx.beans.property.SimpleStringProperty patient;
        private final javafx.beans.property.SimpleStringProperty time;

        public NotifRow(int num, String patient, String time) {
            this.num = new javafx.beans.property.SimpleIntegerProperty(num);
            this.patient = new javafx.beans.property.SimpleStringProperty(patient);
            this.time = new javafx.beans.property.SimpleStringProperty(time);
        }

        public int getNum() {
            return num.get();
        }

        public String getPatient() {
            return patient.get();
        }

        public String getTime() {
            return time.get();
        }

        public javafx.beans.property.SimpleIntegerProperty numProperty() {
            return num;
        }

        public javafx.beans.property.SimpleStringProperty patientProperty() {
            return patient;
        }

        public javafx.beans.property.SimpleStringProperty timeProperty() {
            return time;
        }
    }

    /**
     * Loads today's SMS notification status for the current doctor into the
     * notification card.
     */
    private void loadNotificationStatus() {
        if (notificationCard == null)
            return;

        UserSession session = UserSession.getInstance();
        if (session == null)
            return;

        String role = session.getRole();
        // Only show for Doctor / Stagiaire
        if (role == null || (!role.equalsIgnoreCase("Doctor") && !role.equalsIgnoreCase("Stagiaire"))) {
            notificationCard.setVisible(false);
            notificationCard.setManaged(false);
            return;
        }

        int userId = session.getUserId();

        // Setup columns
        notifColNum.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("num"));
        notifColPatient.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("patient"));
        notifColTime.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("time"));

        ObservableList<NotifRow> rows = FXCollections.observableArrayList();

        // Fetch today's appointments for this doctor (shown regardless of SMS status)
        String apptSql = "SELECT CONCAT(p.first_name,' ',p.last_name) AS pname, a.appointment_time "
                + "FROM appointments a JOIN patients p ON a.patient_id = p.id "
                + "WHERE a.appointment_date = CURRENT_DATE AND a.doctor_id = ? "
                + "ORDER BY a.appointment_time";

        try (Connection conn = com.app.util.DatabaseManager.getConnection();
                PreparedStatement st = conn.prepareStatement(apptSql)) {
            st.setInt(1, userId);
            try (ResultSet rs = st.executeQuery()) {
                int i = 1;
                while (rs.next()) {
                    rows.add(new NotifRow(i++, rs.getString("pname"),
                            rs.getString("appointment_time") != null ? rs.getString("appointment_time").substring(0, 5)
                                    : "—"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        notificationPatientsTable.setItems(rows);

        // Fetch SMS status from sms_notifications
        String statusSql = "SELECT status FROM sms_notifications "
                + "WHERE doctor_id = ? AND notification_date = CURRENT_DATE LIMIT 1";
        try (Connection conn = com.app.util.DatabaseManager.getConnection();
                PreparedStatement st = conn.prepareStatement(statusSql)) {
            st.setInt(1, userId);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    String status = rs.getString("status");
                    if ("Sent".equalsIgnoreCase(status)) {
                        notificationStatusBadge.setText("✅ Notified");
                        notificationStatusBadge.setStyle(
                                "-fx-background-color:#10b981; -fx-text-fill:white; -fx-padding:4 12; -fx-background-radius:12;");
                        notificationStatusLabel.setText("You were notified about today's schedule via SMS.");
                    } else {
                        notificationStatusBadge.setText("❌ Not Notified");
                        notificationStatusBadge.setStyle(
                                "-fx-background-color:#ef4444; -fx-text-fill:white; -fx-padding:4 12; -fx-background-radius:12;");
                        notificationStatusLabel.setText("SMS notification failed or is pending today.");
                    }
                } else {
                    notificationStatusBadge.setText("⏳ Pending");
                    notificationStatusBadge.setStyle(
                            "-fx-background-color:#f59e0b; -fx-text-fill:white; -fx-padding:4 12; -fx-background-radius:12;");
                    notificationStatusLabel.setText("No notification sent yet. Scheduled for 07:00.");
                }
            }
        } catch (Exception e) {
            notificationStatusLabel.setText("Could not load notification status.");
            e.printStackTrace();
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
    public void navAskMe(javafx.scene.input.MouseEvent event) {
        try {
            com.app.MainApp.setRoot("ask_me");
        } catch (java.io.IOException e) {
            e.printStackTrace();
            showError("Navigation Error", "Could not load AI Assistant: " + e.getMessage());
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

    @javafx.fxml.FXML
    public void navRevenue(javafx.scene.input.MouseEvent event) {
        // Instead of navigating to a separate page, let's show revenue in the current dashboard
        // We'll scroll to the revenue section or show a revenue dialog
        showRevenueDialog();
    }

    private void showRevenueDialog() {
        // Create a simple dialog to show revenue data
        javafx.scene.control.Dialog<Void> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Analyse des revenus");
        dialog.setHeaderText("Aperçu des revenus");
        
        // Create content
        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(20);
        content.setStyle("-fx-padding: 20px;");
        
        // Get revenue data
        Integer doctorId = getDoctorId();
        double todayRevenue = com.app.util.DatabaseManager.getTodayRevenue(doctorId);
        double weeklyRevenue = com.app.util.DatabaseManager.getWeeklyRevenue(doctorId);
        double monthlyRevenue = com.app.util.DatabaseManager.getMonthlyRevenueData(doctorId)[java.time.LocalDate.now().getMonthValue() - 1];
        double yearlyRevenue = com.app.util.DatabaseManager.getYearlyRevenue(doctorId);
        
        // Add revenue labels
        content.getChildren().addAll(
            new javafx.scene.control.Label("Revenus d'aujourd'hui: $" + String.format("%.2f", todayRevenue)),
            new javafx.scene.control.Label("Revenus hebdomadaires: $" + String.format("%.2f", weeklyRevenue)),
            new javafx.scene.control.Label("Revenus mensuels: $" + String.format("%.2f", monthlyRevenue)),
            new javafx.scene.control.Label("Revenus annuels: $" + String.format("%.2f", yearlyRevenue))
        );
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(javafx.scene.control.ButtonType.OK);
        dialog.showAndWait();
    }

    private Integer getDoctorId() {
        String role = UserSession.getInstance() != null ? UserSession.getInstance().getRole() : null;
        if (role != null && (role.equalsIgnoreCase("Doctor") || role.equalsIgnoreCase("Stagiaire"))) {
            return UserSession.getInstance().getUserId();
        }
        return null; // Admin/Secretary see all data
    }

    private double getPendingRevenue(Integer doctorId) {
        String sql = doctorId != null
                ? "SELECT COALESCE(SUM(charge_amount), 0) FROM appointments WHERE appointment_date = CURRENT_DATE AND status IN ('Waiting', 'In Progress') AND doctor_id = ?"
                : "SELECT COALESCE(SUM(charge_amount), 0) FROM appointments WHERE appointment_date = CURRENT_DATE AND status IN ('Waiting', 'In Progress')";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement st = conn.prepareStatement(sql)) {
            if (doctorId != null)
                st.setInt(1, doctorId);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next())
                    return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    private void showRevenueUpdate(String patientName, double amount) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Payment Recorded");
        alert.setHeaderText("✅ Payment Successfully Recorded");
        alert.setContentText(String.format("Patient: %s\nAmount: $%.2f\n\nThis amount has been added to today's revenue analytics.", patientName, amount));
        alert.showAndWait();
    }

    private void showError(String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // --- Inner Classes for Revenue Tables ---

    public static class DailyRevenueRow {
        private final javafx.beans.property.SimpleStringProperty date;
        private final javafx.beans.property.SimpleDoubleProperty revenue;
        private final javafx.beans.property.SimpleIntegerProperty appointments;

        public DailyRevenueRow(String date, double revenue, int appointments) {
            this.date = new javafx.beans.property.SimpleStringProperty(date);
            this.revenue = new javafx.beans.property.SimpleDoubleProperty(revenue);
            this.appointments = new javafx.beans.property.SimpleIntegerProperty(appointments);
        }

        public String getDate() {
            return date.get();
        }

        public double getRevenue() {
            return revenue.get();
        }

        public int getAppointments() {
            return appointments.get();
        }

        public javafx.beans.property.SimpleStringProperty dateProperty() {
            return date;
        }

        public javafx.beans.property.SimpleDoubleProperty revenueProperty() {
            return revenue;
        }

        public javafx.beans.property.SimpleIntegerProperty appointmentsProperty() {
            return appointments;
        }
    }

    public static class MonthlyRevenueRow {
        private final javafx.beans.property.SimpleStringProperty month;
        private final javafx.beans.property.SimpleDoubleProperty revenue;
        private final javafx.beans.property.SimpleIntegerProperty appointments;
        private final javafx.beans.property.SimpleDoubleProperty avgRevenue;

        public MonthlyRevenueRow(String month, double revenue, int appointments, double avgRevenue) {
            this.month = new javafx.beans.property.SimpleStringProperty(month);
            this.revenue = new javafx.beans.property.SimpleDoubleProperty(revenue);
            this.appointments = new javafx.beans.property.SimpleIntegerProperty(appointments);
            this.avgRevenue = new javafx.beans.property.SimpleDoubleProperty(avgRevenue);
        }

        public String getMonth() {
            return month.get();
        }

        public double getRevenue() {
            return revenue.get();
        }

        public int getAppointments() {
            return appointments.get();
        }

        public double getAvgRevenue() {
            return avgRevenue.get();
        }

        public javafx.beans.property.SimpleStringProperty monthProperty() {
            return month;
        }

        public javafx.beans.property.SimpleDoubleProperty revenueProperty() {
            return revenue;
        }

        public javafx.beans.property.SimpleIntegerProperty appointmentsProperty() {
            return appointments;
        }

        public javafx.beans.property.SimpleDoubleProperty avgRevenueProperty() {
            return avgRevenue;
        }
    }
}

package com.app.controller;

import com.app.MainApp;
import com.app.util.DatabaseManager;
import com.app.util.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class RevenueController {

    @FXML
    private AnchorPane rootPane;

    private double xOffset = 0;
    private double yOffset = 0;

    // --- Revenue Summary Cards ---
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

    // --- Revenue Charts ---
    @FXML
    private LineChart<String, Number> revenueTrendChart;
    @FXML
    private BarChart<String, Number> monthlyRevenueChart;
    @FXML
    private BarChart<String, Number> treatmentRevenueChart;
    @FXML
    private PieChart paymentStatusChart;

    // --- Revenue Tables ---
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

    // --- Filter Controls ---
    @FXML
    private ComboBox<String> periodComboBox;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private Button filterButton;
    @FXML
    private Button exportButton;
    @FXML
    private Button resetButton;

    // --- Statistics Labels ---
    @FXML
    private Label totalRevenueLabel;
    @FXML
    private Label avgDailyRevenueLabel;
    @FXML
    private Label bestDayLabel;
    @FXML
    private Label totalAppointmentsLabel;

    @FXML
    public void initialize() {
        setupFilterControls();
        setupRevenueCards();
        setupCharts();
        setupTables();
        loadRevenueData();
        applySecurityRestrictions();
    }

    private void setupFilterControls() {
        periodComboBox.setItems(FXCollections.observableArrayList(
            "Last 7 Days", "Last 30 Days", "Last 3 Months", "Last 6 Months", "This Year", "Custom"
        ));
        periodComboBox.setValue("Last 30 Days");

        // Set default dates
        endDatePicker.setValue(LocalDate.now());
        startDatePicker.setValue(LocalDate.now().minusDays(30));

        // Add listeners
        periodComboBox.setOnAction(e -> handlePeriodChange());
        filterButton.setOnAction(e -> applyFilters());
        resetButton.setOnAction(e -> resetFilters());
        exportButton.setOnAction(e -> exportRevenueData());
    }

    private void handlePeriodChange() {
        String selectedPeriod = periodComboBox.getValue();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate;

        switch (selectedPeriod) {
            case "Last 7 Days":
                startDate = endDate.minusDays(7);
                break;
            case "Last 30 Days":
                startDate = endDate.minusDays(30);
                break;
            case "Last 3 Months":
                startDate = endDate.minusMonths(3);
                break;
            case "Last 6 Months":
                startDate = endDate.minusMonths(6);
                break;
            case "This Year":
                startDate = LocalDate.of(endDate.getYear(), 1, 1);
                break;
            case "Custom":
                return; // Don't change custom dates
            default:
                startDate = endDate.minusDays(30);
        }

        startDatePicker.setValue(startDate);
        endDatePicker.setValue(endDate);
        applyFilters();
    }

    private void applyFilters() {
        loadRevenueData();
    }

    private void resetFilters() {
        periodComboBox.setValue("Last 30 Days");
        startDatePicker.setValue(LocalDate.now().minusDays(30));
        endDatePicker.setValue(LocalDate.now());
        loadRevenueData();
    }

    private void exportRevenueData() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Export Revenue Data");
        alert.setHeaderText("Revenue Export");
        alert.setContentText("Revenue data export functionality would be implemented here.\n\nThis would generate a CSV/Excel file with the current revenue data.");
        alert.showAndWait();
    }

    private void setupRevenueCards() {
        Integer doctorId = getDoctorId();
        
        // Get revenue data
        double todayRevenue = DatabaseManager.getTodayRevenue(doctorId);
        double weeklyRevenue = DatabaseManager.getWeeklyRevenue(doctorId);
        double monthlyRevenue = DatabaseManager.getMonthlyRevenueData(doctorId)[LocalDate.now().getMonthValue() - 1];
        double yearlyRevenue = DatabaseManager.getYearlyRevenue(doctorId);

        // Get comparison data
        double previousWeekRevenue = DatabaseManager.getPreviousWeekRevenue(doctorId);
        Object[] monthlyComparison = DatabaseManager.getRevenueComparison(doctorId, true);
        double lastMonthRevenue = (Double) monthlyComparison[1];
        double previousYearRevenue = DatabaseManager.getPreviousYearRevenue(doctorId);

        // Setup Today Revenue Card
        if (todayRevenueBigLabel != null) {
            todayRevenueBigLabel.setText(String.format("$%.2f", todayRevenue));
            todayRevenueBigLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #10b981;");
        }
        if (todayRevenueChangeLabel != null) {
            double change = getDailyChange(doctorId);
            todayRevenueChangeLabel.setText(formatChangeIndicator(change));
            todayRevenueChangeLabel.setStyle(getChangeStyle(change));
        }

        // Setup Weekly Revenue Card
        if (weeklyRevenueBigLabel != null) {
            weeklyRevenueBigLabel.setText(String.format("$%.2f", weeklyRevenue));
            weeklyRevenueBigLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #3b82f6;");
        }
        if (weeklyRevenueChangeLabel != null) {
            double change = previousWeekRevenue == 0 ? 0 : ((weeklyRevenue - previousWeekRevenue) / previousWeekRevenue) * 100;
            weeklyRevenueChangeLabel.setText(formatChangeIndicator(change));
            weeklyRevenueChangeLabel.setStyle(getChangeStyle(change));
        }

        // Setup Monthly Revenue Card
        if (monthlyRevenueBigLabel != null) {
            monthlyRevenueBigLabel.setText(String.format("$%.2f", monthlyRevenue));
            monthlyRevenueBigLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #8b5cf6;");
        }
        if (monthlyRevenueChangeLabel != null) {
            double change = lastMonthRevenue == 0 ? 0 : ((monthlyRevenue - lastMonthRevenue) / lastMonthRevenue) * 100;
            monthlyRevenueChangeLabel.setText(formatChangeIndicator(change));
            monthlyRevenueChangeLabel.setStyle(getChangeStyle(change));
        }

        // Setup Yearly Revenue Card
        if (yearlyRevenueBigLabel != null) {
            yearlyRevenueBigLabel.setText(String.format("$%.2f", yearlyRevenue));
            yearlyRevenueBigLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #f59e0b;");
        }
        if (yearlyRevenueChangeLabel != null) {
            double change = previousYearRevenue == 0 ? 0 : ((yearlyRevenue - previousYearRevenue) / previousYearRevenue) * 100;
            yearlyRevenueChangeLabel.setText(formatChangeIndicator(change));
            yearlyRevenueChangeLabel.setStyle(getChangeStyle(change));
        }

        styleRevenueCards();
    }

    private double getDailyChange(Integer doctorId) {
        // Simplified daily change calculation
        Object[] comparison = DatabaseManager.getRevenueComparison(doctorId, false);
        double todayRevenue = (Double) comparison[0];
        double yesterdayRevenue = (Double) comparison[1];
        return yesterdayRevenue == 0 ? 0 : ((todayRevenue - yesterdayRevenue) / yesterdayRevenue) * 100;
    }

    private void setupCharts() {
        Integer doctorId = getDoctorId();

        // Revenue Trend Chart
        setupRevenueTrendChart(doctorId);

        // Monthly Revenue Chart
        setupMonthlyRevenueChart(doctorId);

        // Treatment Revenue Chart
        setupTreatmentRevenueChart(doctorId);

        // Payment Status Chart
        setupPaymentStatusChart(doctorId);
    }

    private void setupRevenueTrendChart(Integer doctorId) {
        revenueTrendChart.getData().clear();
        
        Object[][] dailyData = DatabaseManager.getDailyRevenueData(doctorId);
        XYChart.Series<String, Number> trendSeries = new XYChart.Series<>();
        trendSeries.setName("Revenue Trend");

        // Show last 14 days for trend
        int daysToShow = Math.min(14, dailyData.length);
        for (int i = daysToShow - 1; i >= 0; i--) {
            String date = dailyData[i][0].toString();
            double revenue = ((Number) dailyData[i][1]).doubleValue();
            // Format date to show only day and month
            String formattedDate = LocalDate.parse(date).format(DateTimeFormatter.ofPattern("MMM dd"));
            trendSeries.getData().add(new XYChart.Data<>(formattedDate, revenue));
        }

        revenueTrendChart.getData().add(trendSeries);
    }

    private void setupMonthlyRevenueChart(Integer doctorId) {
        monthlyRevenueChart.getData().clear();
        
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        double[] monthlyRevenue = DatabaseManager.getMonthlyRevenueData(doctorId);
        
        XYChart.Series<String, Number> revSeries = new XYChart.Series<>();
        revSeries.setName("Monthly Revenue");
        
        for (int i = 0; i < months.length; i++) {
            revSeries.getData().add(new XYChart.Data<>(months[i], monthlyRevenue[i]));
        }
        
        monthlyRevenueChart.getData().add(revSeries);
    }

    private void setupTreatmentRevenueChart(Integer doctorId) {
        treatmentRevenueChart.getData().clear();
        
        Object[][] reasonData = DatabaseManager.getRevenueByReasonData(doctorId);
        XYChart.Series<String, Number> treatSeries = new XYChart.Series<>();
        treatSeries.setName("Revenue by Treatment");
        
        if (reasonData.length > 0) {
            for (Object[] row : reasonData) {
                String label = row[0] != null ? row[0].toString() : "Other";
                double amount = ((Number) row[1]).doubleValue();
                treatSeries.getData().add(new XYChart.Data<>(label, amount));
            }
        } else {
            treatSeries.getData().add(new XYChart.Data<>("No Data", 0));
        }
        
        treatmentRevenueChart.getData().add(treatSeries);
    }

    private void setupPaymentStatusChart(Integer doctorId) {
        paymentStatusChart.getData().clear();
        
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
    }

    private void setupTables() {
        Integer doctorId = getDoctorId();

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

            loadDailyRevenueTable(doctorId);
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

            loadMonthlyRevenueTable(doctorId);
        }
    }

    private void loadDailyRevenueTable(Integer doctorId) {
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

    private void loadMonthlyRevenueTable(Integer doctorId) {
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

    private void loadRevenueData() {
        setupRevenueCards();
        setupCharts();
        setupTables();
        updateStatistics();
    }

    private void updateStatistics() {
        Integer doctorId = getDoctorId();
        
        // Calculate statistics based on selected period
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        
        // This would be enhanced with actual period-based calculations
        double totalRevenue = DatabaseManager.getYearlyRevenue(doctorId);
        int totalAppointments = DatabaseManager.getMonthlyTreatments(doctorId);
        
        if (totalRevenueLabel != null) {
            totalRevenueLabel.setText(String.format("$%.2f", totalRevenue));
        }
        if (avgDailyRevenueLabel != null) {
            double avgDaily = totalRevenue / 365; // Simplified
            avgDailyRevenueLabel.setText(String.format("$%.2f", avgDaily));
        }
        if (totalAppointmentsLabel != null) {
            totalAppointmentsLabel.setText(String.format("%,d", totalAppointments));
        }
        if (bestDayLabel != null) {
            bestDayLabel.setText("Monday"); // Would be calculated from actual data
        }
    }

    private Integer getDoctorId() {
        String role = UserSession.getInstance() != null ? UserSession.getInstance().getRole() : null;
        if (role != null && (role.equalsIgnoreCase("Doctor") || role.equalsIgnoreCase("Stagiaire"))) {
            return UserSession.getInstance().getUserId();
        }
        return null; // Admin/Secretary see all data
    }

    private void applySecurityRestrictions() {
        String role = UserSession.getInstance() != null ? UserSession.getInstance().getRole() : null;
        // Add any role-specific restrictions if needed
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
            return "-fx-text-fill: #10b981; -fx-font-weight: bold; -fx-font-size: 16px;";
        } else if (change < 0) {
            return "-fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-font-size: 16px;";
        } else {
            return "-fx-text-fill: #6b7280; -fx-font-weight: bold; -fx-font-size: 16px;";
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
    }

    // --- Navigation Methods ---
    @FXML
    public void navDashboard(MouseEvent event) {
        try {
            MainApp.setRoot("dashboard");
        } catch (IOException e) {
            e.printStackTrace();
            showError("Navigation Error", "Could not load dashboard: " + e.getMessage());
        }
    }

    @FXML
    public void navPatients(MouseEvent event) {
        try {
            MainApp.setRoot("patients");
        } catch (IOException e) {
            e.printStackTrace();
            showError("Navigation Error", "Could not load patients: " + e.getMessage());
        }
    }

    @FXML
    public void navAppointments(MouseEvent event) {
        try {
            MainApp.setRoot("appointments");
        } catch (IOException e) {
            e.printStackTrace();
            showError("Navigation Error", "Could not load appointments: " + e.getMessage());
        }
    }

    @FXML
    public void navTasks(MouseEvent event) {
        try {
            MainApp.setRoot("tasks");
        } catch (IOException e) {
            e.printStackTrace();
            showError("Navigation Error", "Could not load tasks: " + e.getMessage());
        }
    }

    @FXML
    public void navRevenue(MouseEvent event) {
        // Revenue is already the current page, so we don't need to navigate
        // This method exists to prevent the FXML loading error
    }

    @FXML
    public void navProfile(MouseEvent event) {
        try {
            MainApp.setRoot("profile");
        } catch (IOException e) {
            e.printStackTrace();
            showError("Navigation Error", "Could not load profile: " + e.getMessage());
        }
    }

    @FXML
    public void navAskMe(MouseEvent event) {
        try {
            MainApp.setRoot("ask_me");
        } catch (IOException e) {
            e.printStackTrace();
            showError("Navigation Error", "Could not load AI Assistant: " + e.getMessage());
        }
    }

    @FXML
    public void navLogout(MouseEvent event) {
        try {
            MainApp.setRoot("login");
        } catch (IOException e) {
            e.printStackTrace();
            showError("Navigation Error", "Could not load login: " + e.getMessage());
        }
    }

    // --- Window Control Methods ---
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

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // --- Inner Classes for Tables ---
    public static class DailyRevenueRow {
        private final javafx.beans.property.SimpleStringProperty date;
        private final javafx.beans.property.SimpleDoubleProperty revenue;
        private final javafx.beans.property.SimpleIntegerProperty appointments;

        public DailyRevenueRow(String date, double revenue, int appointments) {
            this.date = new javafx.beans.property.SimpleStringProperty(date);
            this.revenue = new javafx.beans.property.SimpleDoubleProperty(revenue);
            this.appointments = new javafx.beans.property.SimpleIntegerProperty(appointments);
        }

        public String getDate() { return date.get(); }
        public double getRevenue() { return revenue.get(); }
        public int getAppointments() { return appointments.get(); }

        public javafx.beans.property.SimpleStringProperty dateProperty() { return date; }
        public javafx.beans.property.SimpleDoubleProperty revenueProperty() { return revenue; }
        public javafx.beans.property.SimpleIntegerProperty appointmentsProperty() { return appointments; }
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

        public String getMonth() { return month.get(); }
        public double getRevenue() { return revenue.get(); }
        public int getAppointments() { return appointments.get(); }
        public double getAvgRevenue() { return avgRevenue.get(); }

        public javafx.beans.property.SimpleStringProperty monthProperty() { return month; }
        public javafx.beans.property.SimpleDoubleProperty revenueProperty() { return revenue; }
        public javafx.beans.property.SimpleIntegerProperty appointmentsProperty() { return appointments; }
        public javafx.beans.property.SimpleDoubleProperty avgRevenueProperty() { return avgRevenue; }
    }
}

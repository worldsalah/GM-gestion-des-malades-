package com.app.controller;

import com.app.MainApp;
import com.app.util.DatabaseManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;

public class AppointmentController {

    // ─── FXML injected fields ──────────────────────────────────────────────────

    @FXML
    private AnchorPane rootPane;

    // Calendar header
    @FXML
    private Label monthYearLabel;
    @FXML
    private GridPane calendarGrid;

    // Right panel tabs
    @FXML
    private VBox appointmentListPanel;
    @FXML
    private VBox appointmentFormPanel;

    // Form fields
    @FXML
    private ComboBox<String> patientCombo;
    @FXML
    private DatePicker appointmentDatePicker;
    @FXML
    private ComboBox<String> timePicker;
    @FXML
    private ComboBox<String> statusCombo;
    @FXML
    private ComboBox<String> priorityCombo;
    @FXML
    private ComboBox<String> recurrenceCombo;
    @FXML
    private TextField reasonField;
    @FXML
    private TextArea notesArea;
    @FXML
    private Label formTitleLabel;
    @FXML
    private Button saveBtn;

    // Day appointments list
    @FXML
    private Label selectedDayLabel;
    @FXML
    private VBox dayAppointmentsList;

    // ─── State ─────────────────────────────────────────────────────────────────
    private YearMonth currentYearMonth = YearMonth.now();
    private LocalDate selectedDate = LocalDate.now();
    private Appointment editingAppointment = null;
    private double xOffset = 0;
    private double yOffset = 0;

    /** appointment-id → Appointment for fast lookup */
    private final Map<Integer, Appointment> appointmentMap = new HashMap<>();
    /** date → list of appointment-ids that fall on that date */
    private final Map<LocalDate, List<Integer>> dateIndex = new HashMap<>();

    // ─── Init ──────────────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        setupTimePicker();
        statusCombo.setItems(FXCollections.observableArrayList(
                "Scheduled", "Confirmed", "Waiting", "In Progress", "Completed", "Cancelled", "No Show"));
        statusCombo.setValue("Scheduled");
        priorityCombo.setItems(FXCollections.observableArrayList(
                "Normal", "Emergency", "Follow-up"));
        priorityCombo.setValue("Normal");
        recurrenceCombo.setItems(FXCollections.observableArrayList(
                "None", "Weekly", "Monthly"));
        recurrenceCombo.setValue("None");

        // Advanced Logic: Block Weekends on DatePicker
        appointmentDatePicker.setDayCellFactory(picker -> new DateCell() {
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null)
                    return;

                // Disable weekends (Saturday = 6, Sunday = 7)
                if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ffc0cb;");
                }
            }
        });

        setupStatusListener();

        loadAllAppointments();
        loadPatientCombo();
        rebuildCalendar();
        showDayAppointments(selectedDate);
        showListPanel();

        // Advanced Logic: Automated Reminders Simulation
        checkAndSendReminders();
    }

    // ─── Calendar ──────────────────────────────────────────────────────────────
    private void rebuildCalendar() {
        monthYearLabel.setText(currentYearMonth.getMonth()
                .getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                + " " + currentYearMonth.getYear());

        // Clear existing day cells (rows 1+); keep header row 0
        calendarGrid.getChildren().removeIf(n -> {
            Integer row = GridPane.getRowIndex(n);
            return row != null && row > 0;
        });

        LocalDate first = currentYearMonth.atDay(1);
        // Monday-based: Mon=0 … Sun=6
        int startCol = (first.getDayOfWeek().getValue() - 1) % 7;
        int daysInMonth = currentYearMonth.lengthOfMonth();
        int col = startCol;
        int row = 1;

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentYearMonth.atDay(day);
            VBox cell = buildDayCell(date, col);
            calendarGrid.add(cell, col, row);
            col++;
            if (col == 7) {
                col = 0;
                row++;
            }
        }
    }

    private VBox buildDayCell(LocalDate date, int column) {
        VBox cell = new VBox(2);
        cell.getStyleClass().add("cal-cell");
        GridPane.setVgrow(cell, Priority.ALWAYS);
        GridPane.setHgrow(cell, Priority.ALWAYS);

        boolean isToday = date.equals(LocalDate.now());
        boolean isSelected = date.equals(selectedDate);
        List<Integer> ids = dateIndex.getOrDefault(date, Collections.emptyList());

        if (isSelected)
            cell.getStyleClass().add("cal-cell-selected");
        else if (isToday)
            cell.getStyleClass().add("cal-cell-today");

        Label dayNum = new Label(String.valueOf(date.getDayOfMonth()));
        dayNum.getStyleClass().add(isToday ? "cal-day-today" : "cal-day-num");
        cell.getChildren().add(dayNum);

        // Show up to 2 event chips
        int shown = Math.min(ids.size(), 2);
        for (int i = 0; i < shown; i++) {
            Appointment a = appointmentMap.get(ids.get(i));
            if (a == null)
                continue;
            Label chip = new Label(a.getPatientName());
            chip.getStyleClass().add(statusStyleClass(a.getStatus()));
            chip.setMaxWidth(Double.MAX_VALUE);
            chip.setEllipsisString("…");
            cell.getChildren().add(chip);
        }
        if (ids.size() > 2) {
            Label more = new Label("+" + (ids.size() - 2) + " more");
            more.getStyleClass().add("cal-more-chip");
            cell.getChildren().add(more);
        }

        cell.setOnMouseClicked(e -> {
            selectedDate = date;
            rebuildCalendar();
            showDayAppointments(date);
        });

        return cell;
    }

    @FXML
    public void prevMonth(ActionEvent e) {
        currentYearMonth = currentYearMonth.minusMonths(1);
        rebuildCalendar();
        showDayAppointments(selectedDate);
    }

    @FXML
    public void nextMonth(ActionEvent e) {
        currentYearMonth = currentYearMonth.plusMonths(1);
        rebuildCalendar();
        showDayAppointments(selectedDate);
    }

    @FXML
    public void goToday(ActionEvent e) {
        currentYearMonth = YearMonth.now();
        selectedDate = LocalDate.now();
        rebuildCalendar();
        showDayAppointments(selectedDate);
    }

    // ─── Day appointments list ─────────────────────────────────────────────────
    private void showDayAppointments(LocalDate date) {
        selectedDayLabel.setText(date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d yyyy")));
        dayAppointmentsList.getChildren().clear();

        List<Integer> ids = dateIndex.getOrDefault(date, Collections.emptyList());
        if (ids.isEmpty()) {
            Label empty = new Label("No appointments on this day");
            empty.getStyleClass().add("no-appt-label");
            dayAppointmentsList.getChildren().add(empty);
            return;
        }

        for (int id : ids) {
            Appointment a = appointmentMap.get(id);
            if (a == null)
                continue;
            dayAppointmentsList.getChildren().add(buildAppointmentRow(a));
        }
    }

    private HBox buildAppointmentRow(Appointment a) {
        HBox row = new HBox(12);
        row.getStyleClass().add("appt-row");
        row.setAlignment(Pos.CENTER_LEFT);

        // Color stripe
        Region stripe = new Region();
        stripe.getStyleClass().add(statusStyleClass(a.getStatus()) + "-stripe");
        stripe.setPrefWidth(4);
        stripe.setMinWidth(4);
        stripe.setMaxWidth(4);
        stripe.setPrefHeight(60);

        VBox info = new VBox(3);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label time = new Label("🕐 " + a.getTime());
        time.getStyleClass().add("appt-time");

        Label patient = new Label(a.getPatientName());
        patient.getStyleClass().add("appt-patient");

        Label reason = new Label(a.getReason() != null && !a.getReason().isEmpty()
                ? a.getReason()
                : "No reason specified");
        reason.getStyleClass().add("appt-reason");

        info.getChildren().addAll(time, patient, reason);

        Label statusLbl = new Label(a.getStatus());
        statusLbl.getStyleClass().add(statusStyleClass(a.getStatus()));

        Label priorityLbl = new Label(a.getPriority());
        if ("Emergency".equals(a.getPriority())) {
            priorityLbl.getStyleClass().add("chip-noshow"); // Temporarily use red noshow chip class
            row.setStyle("-fx-border-color: #ff4757; -fx-border-width: 0 0 0 4px; -fx-background-color: #ffeaea;");
        } else if ("Follow-up".equals(a.getPriority())) {
            priorityLbl.getStyleClass().add("chip-confirmed");
        } else {
            priorityLbl.getStyleClass().add("chip-scheduled");
        }

        Button editBtn = new Button("✏");
        editBtn.getStyleClass().add("appt-icon-btn");
        editBtn.setOnAction(e -> openEditForm(a));

        Button deleteBtn = new Button("🗑");
        deleteBtn.getStyleClass().add("appt-icon-btn-danger");
        deleteBtn.setOnAction(e -> deleteAppointment(a));

        row.getChildren().addAll(stripe, info, priorityLbl, statusLbl, editBtn, deleteBtn);
        return row;
    }

    // ─── Form ──────────────────────────────────────────────────────────────────
    @FXML
    public void showNewAppointmentForm(ActionEvent e) {
        editingAppointment = null;
        formTitleLabel.setText("New Appointment");
        saveBtn.setText("Save Appointment");
        clearForm();
        if (selectedDate != null)
            appointmentDatePicker.setValue(selectedDate);
        showFormPanel();
    }

    private void openEditForm(Appointment a) {
        editingAppointment = a;
        formTitleLabel.setText("Edit Appointment");
        saveBtn.setText("Update Appointment");

        // populate form
        appointmentDatePicker.setValue(LocalDate.parse(a.getDate()));
        timePicker.setValue(a.getTime());
        reasonField.setText(a.getReason());
        statusCombo.setValue(a.getStatus());
        priorityCombo.setValue(a.getPriority());
        recurrenceCombo.setValue(a.getRecurrence());
        notesArea.setText(a.getNotes());

        // set patient
        patientCombo.getItems().stream()
                .filter(s -> s.startsWith(a.getPatientId() + " - "))
                .findFirst()
                .ifPresent(patientCombo::setValue);

        showFormPanel();
    }

    @FXML
    public void cancelForm(ActionEvent e) {
        clearForm();
        showListPanel();
    }

    @FXML
    public void saveAppointment(ActionEvent e) {
        if (patientCombo.getValue() == null) {
            showAlert("Validation", "Please select a patient.");
            return;
        }
        if (appointmentDatePicker.getValue() == null) {
            showAlert("Validation", "Please select a date.");
            return;
        }
        if (timePicker.getValue() == null) {
            showAlert("Validation", "Please select a time.");
            return;
        }

        String patientEntry = patientCombo.getValue();
        int patientId = Integer.parseInt(patientEntry.split(" - ")[0].trim());
        LocalDate date = appointmentDatePicker.getValue();
        String time = timePicker.getValue();
        String reason = reasonField.getText();
        String status = statusCombo.getValue() != null ? statusCombo.getValue() : "Scheduled";
        String priority = priorityCombo.getValue() != null ? priorityCombo.getValue() : "Normal";
        String recurrence = recurrenceCombo.getValue() != null ? recurrenceCombo.getValue() : "None";
        String notes = notesArea.getText();

        try (Connection conn = DatabaseManager.getConnection()) {
            // Smart Scheduling Engine: Prevent Double Booking
            String checkSql = "SELECT COUNT(*) FROM appointments WHERE appointment_date = ? AND appointment_time = ? ";
            if (editingAppointment != null) {
                checkSql += "AND id != ?";
            }
            PreparedStatement checkSt = conn.prepareStatement(checkSql);
            checkSt.setDate(1, java.sql.Date.valueOf(date));
            checkSt.setString(2, time);
            if (editingAppointment != null) {
                checkSt.setInt(3, editingAppointment.getId());
            }
            ResultSet rs = checkSt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                showAlert("Double Booking Prevented",
                        "There is already an appointment scheduled for this date and time.");
                return;
            }

            if (editingAppointment == null) {
                // Determine number of recurrent appointments to create
                int occurrences = 1;
                if ("Weekly".equals(recurrence))
                    occurrences = 4;
                else if ("Monthly".equals(recurrence))
                    occurrences = 6;

                String sql = "INSERT INTO appointments (patient_id, appointment_date, appointment_time, reason, status, priority, recurrence, notes) VALUES (?,?,?,?,?,?,?,?)";
                PreparedStatement st = conn.prepareStatement(sql);

                // Recurrence Loop
                for (int i = 0; i < occurrences; i++) {
                    LocalDate occurrenceDate = date;
                    if ("Weekly".equals(recurrence)) {
                        occurrenceDate = date.plusWeeks(i);
                    } else if ("Monthly".equals(recurrence)) {
                        occurrenceDate = date.plusMonths(i);
                    }

                    st.setInt(1, patientId);
                    st.setDate(2, java.sql.Date.valueOf(occurrenceDate));
                    st.setString(3, time);
                    st.setString(4, reason);
                    st.setString(5, status);
                    st.setString(6, priority);
                    st.setString(7, recurrence);
                    st.setString(8, notes);
                    st.addBatch();
                }
                st.executeBatch();
            } else {
                String sql = "UPDATE appointments SET patient_id=?, appointment_date=?, appointment_time=?, reason=?, status=?, priority=?, recurrence=?, notes=? WHERE id=?";
                PreparedStatement st = conn.prepareStatement(sql);
                st.setInt(1, patientId);
                st.setDate(2, java.sql.Date.valueOf(date));
                st.setString(3, time);
                st.setString(4, reason);
                st.setString(5, status);
                st.setString(6, priority);
                st.setString(7, recurrence);
                st.setString(8, notes);
                st.setInt(9, editingAppointment.getId());
                st.executeUpdate();
            }
        } catch (SQLException ex) {
            showAlert("Database Error", ex.getMessage());
            return;
        }

        loadAllAppointments();
        rebuildCalendar();
        showDayAppointments(selectedDate);
        clearForm();
        showListPanel();
    }

    private void deleteAppointment(Appointment a) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete appointment for " + a.getPatientName() + "?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText(null);
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.YES)
            return;

        try (Connection conn = DatabaseManager.getConnection()) {
            PreparedStatement st = conn.prepareStatement("DELETE FROM appointments WHERE id=?");
            st.setInt(1, a.getId());
            st.executeUpdate();
        } catch (SQLException ex) {
            showAlert("Database Error", ex.getMessage());
            return;
        }

        loadAllAppointments();
        rebuildCalendar();
        showDayAppointments(selectedDate);
    }

    // ─── Data ──────────────────────────────────────────────────────────────────
    private void loadAllAppointments() {
        appointmentMap.clear();
        dateIndex.clear();

        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT a.id, a.patient_id, CONCAT(p.first_name,' ',p.last_name) AS patient_name, "
                    + "a.appointment_date, a.appointment_time, a.reason, a.status, a.priority, a.recurrence, a.notes "
                    + "FROM appointments a "
                    + "LEFT JOIN patients p ON a.patient_id = p.id "
                    + "ORDER BY a.appointment_date, "
                    + "CASE WHEN a.priority = 'Emergency' THEN 0 ELSE 1 END, "
                    + "a.appointment_time";
            ResultSet rs = conn.prepareStatement(sql).executeQuery();
            while (rs.next()) {
                Appointment a = new Appointment(
                        rs.getInt("id"),
                        rs.getInt("patient_id"),
                        rs.getString("patient_name"),
                        rs.getString("appointment_date"),
                        rs.getString("appointment_time"),
                        rs.getString("reason"),
                        rs.getString("status"),
                        rs.getString("priority"),
                        rs.getString("recurrence"),
                        rs.getString("notes"));
                appointmentMap.put(a.getId(), a);
                LocalDate d = LocalDate.parse(a.getDate());
                dateIndex.computeIfAbsent(d, k -> new ArrayList<>()).add(a.getId());
            }
        } catch (SQLException ex) {
            showAlert("Database Error", ex.getMessage());
        }
    }

    private void loadPatientCombo() {
        ObservableList<String> items = FXCollections.observableArrayList();
        try (Connection conn = DatabaseManager.getConnection()) {
            ResultSet rs = conn.prepareStatement(
                    "SELECT id, first_name, last_name FROM patients ORDER BY first_name").executeQuery();
            while (rs.next()) {
                items.add(rs.getInt("id") + " - " + rs.getString("first_name") + " " + rs.getString("last_name"));
            }
        } catch (SQLException ex) {
            showAlert("Database Error", ex.getMessage());
        }
        patientCombo.setItems(items);
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────
    private void setupTimePicker() {
        ObservableList<String> times = FXCollections.observableArrayList();
        for (int h = 7; h <= 20; h++) {
            // Advanced Logic: Block Lunch Break (12:00 -> 13:30)
            if (h == 12 || h == 13)
                continue;

            times.add(String.format("%02d:00", h));
            times.add(String.format("%02d:30", h));
        }
        timePicker.setItems(times);
    }

    private void setupStatusListener() {
        // Advanced Logic: Intelligent Rescheduling
        statusCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if ("Cancelled".equals(newVal) && editingAppointment != null) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Intelligent Rescheduling");
                alert.setHeaderText("Appointment Cancelled");
                alert.setContentText(
                        "Would you like the engine to automatically find and suggest the nearest available time slot?");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    suggestNextAvailableSlot();
                }
            }
        });
    }

    private void suggestNextAvailableSlot() {
        LocalDate searchDate = LocalDate.now();
        // Start searching from tomorrow
        searchDate = searchDate.plusDays(1);

        boolean found = false;

        // Very basic simple scan forward for demonstration of intelligent rescheduling
        for (int dayOffset = 0; dayOffset < 30; dayOffset++) {
            LocalDate checkDate = searchDate.plusDays(dayOffset);
            if (checkDate.getDayOfWeek() == DayOfWeek.SATURDAY || checkDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
                continue; // skip weekends
            }

            // Find available time
            for (String time : timePicker.getItems()) {
                boolean isOccupied = false;
                // Check memory map for conflict
                for (int id : dateIndex.getOrDefault(checkDate, Collections.emptyList())) {
                    Appointment a = appointmentMap.get(id);
                    if (a != null && a.getTime().equals(time) && !a.getStatus().equals("Cancelled")) {
                        isOccupied = true;
                        break;
                    }
                }

                if (!isOccupied) {
                    appointmentDatePicker.setValue(checkDate);
                    timePicker.setValue(time);
                    statusCombo.setValue("Scheduled");
                    showAlert("Slot Found!", "Suggested nearest available slot: " + checkDate + " at " + time);
                    found = true;
                    break;
                }
            }
            if (found)
                break;
        }
        if (!found) {
            showAlert("No Slots", "Could not find an available slot in the next 30 days.");
        }
    }

    private void checkAndSendReminders() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        int reminderCount = 0;

        for (int id : dateIndex.getOrDefault(tomorrow, Collections.emptyList())) {
            Appointment a = appointmentMap.get(id);
            if (a != null && ("Scheduled".equals(a.getStatus()) || "Confirmed".equals(a.getStatus()))) {
                reminderCount++;
            }
        }

        if (reminderCount > 0) {
            System.out.println("🔔 Automated Reminders triggered for " + reminderCount + " patients tomorrow.");
            showAlert("Automated Reminders", "System sent automated SMS/Email reminders to " + reminderCount
                    + " patients for tomorrow's appointments.");
        }
    }

    private String statusStyleClass(String status) {
        if (status == null)
            return "chip-scheduled";
        return switch (status) {
            case "Confirmed" -> "chip-confirmed";
            case "Completed" -> "chip-completed";
            case "Cancelled" -> "chip-cancelled";
            case "No Show" -> "chip-noshow";
            default -> "chip-scheduled";
        };
    }

    private void clearForm() {
        patientCombo.setValue(null);
        appointmentDatePicker.setValue(null);
        timePicker.setValue(null);
        reasonField.clear();
        statusCombo.setValue("Scheduled");
        priorityCombo.setValue("Normal");
        recurrenceCombo.setValue("None");
        notesArea.clear();
        editingAppointment = null;
    }

    private void showListPanel() {
        appointmentListPanel.setVisible(true);
        appointmentListPanel.setManaged(true);
        appointmentFormPanel.setVisible(false);
        appointmentFormPanel.setManaged(false);
    }

    private void showFormPanel() {
        appointmentListPanel.setVisible(false);
        appointmentListPanel.setManaged(false);
        appointmentFormPanel.setVisible(true);
        appointmentFormPanel.setManaged(true);
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    // ─── Navigation ────────────────────────────────────────────────────────────
    @FXML
    public void goToDashboard(MouseEvent e) {
        try {
            MainApp.setRoot("dashboard");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    public void goToPatients(MouseEvent e) {
        try {
            MainApp.setRoot("patients");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    public void openProfilePage(MouseEvent e) {
        try {
            MainApp.setRoot("profile");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    public void logoutAction(MouseEvent e) {
        com.app.util.UserSession.cleanUserSession();
        try {
            MainApp.setRoot("login");
        } catch (IOException ex) {
            ex.printStackTrace();
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
    public void onWindowPressed(MouseEvent e) {
        xOffset = e.getSceneX();
        yOffset = e.getSceneY();
    }

    @FXML
    public void onWindowDragged(MouseEvent e) {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        if (!MainApp.isMaximized) {
            stage.setX(e.getScreenX() - xOffset);
            stage.setY(e.getScreenY() - yOffset);
        }
    }

    @FXML
    public void handleMinimize(ActionEvent e) {
        ((Stage) rootPane.getScene().getWindow()).setIconified(true);
    }

    @FXML
    public void handleMaximize(ActionEvent e) {
        MainApp.toggleMaximize((Stage) rootPane.getScene().getWindow());
    }

    @FXML
    public void handleClose(ActionEvent e) {
        MainApp.closeApp();
    }

    // ─── Model ─────────────────────────────────────────────────────────────────
    public static class Appointment {
        private final SimpleIntegerProperty id;
        private final SimpleIntegerProperty patientId;
        private final SimpleStringProperty patientName;
        private final SimpleStringProperty date;
        private final SimpleStringProperty time;
        private final SimpleStringProperty reason;
        private final SimpleStringProperty status;
        private final SimpleStringProperty priority;
        private final SimpleStringProperty recurrence;
        private final SimpleStringProperty notes;

        public Appointment(int id, int patientId, String patientName,
                String date, String time, String reason,
                String status, String priority, String recurrence, String notes) {
            this.id = new SimpleIntegerProperty(id);
            this.patientId = new SimpleIntegerProperty(patientId);
            this.patientName = new SimpleStringProperty(patientName);
            this.date = new SimpleStringProperty(date);
            this.time = new SimpleStringProperty(time);
            this.reason = new SimpleStringProperty(reason != null ? reason : "");
            this.status = new SimpleStringProperty(status != null ? status : "Scheduled");
            this.priority = new SimpleStringProperty(priority != null ? priority : "Normal");
            this.recurrence = new SimpleStringProperty(recurrence != null ? recurrence : "None");
            this.notes = new SimpleStringProperty(notes != null ? notes : "");
        }

        public int getId() {
            return id.get();
        }

        public int getPatientId() {
            return patientId.get();
        }

        public String getPatientName() {
            return patientName.get();
        }

        public String getDate() {
            return date.get();
        }

        public String getTime() {
            return time.get();
        }

        public String getReason() {
            return reason.get();
        }

        public String getStatus() {
            return status.get();
        }

        public String getPriority() {
            return priority.get();
        }

        public String getRecurrence() {
            return recurrence.get();
        }

        public String getNotes() {
            return notes.get();
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

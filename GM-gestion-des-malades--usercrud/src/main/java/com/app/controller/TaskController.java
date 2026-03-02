package com.app.controller;

import com.app.MainApp;
import com.app.util.DatabaseManager;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.format.DateTimeFormatter;

public class TaskController {

    // ── Calendar / window ───────────────────────────────────────────────────
    @FXML
    private javafx.scene.layout.AnchorPane rootPane;
    private double xOffset = 0;
    private double yOffset = 0;

    // ── Board columns ────────────────────────────────────────────────────────
    @FXML
    private VBox todoColumn;
    @FXML
    private VBox doingColumn;
    @FXML
    private VBox doneColumn;

    @FXML
    private Label todoCount;
    @FXML
    private Label doingCount;
    @FXML
    private Label doneCount;

    // ── Quick-add bar ────────────────────────────────────────────────────────
    @FXML
    private TextField quickAddField;
    @FXML
    private ComboBox<String> quickAddStatus;

    // ── Task form ────────────────────────────────────────────────────────────
    @FXML
    private VBox taskFormPanel;
    @FXML
    private Label formTitleLabel;
    @FXML
    private TextField titleField;
    @FXML
    private TextArea descField;
    @FXML
    private ComboBox<String> statusCombo;
    @FXML
    private ComboBox<String> priorityCombo;
    @FXML
    private Button saveBtn;

    private Integer editingTaskId = null;

    // ────────────────────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        // Quick-add status picker
        quickAddStatus.getItems().addAll("Todo", "Doing", "Done");
        quickAddStatus.setValue("Todo");

        // Full form combos
        statusCombo.getItems().addAll("Todo", "Doing", "Done");
        statusCombo.setValue("Todo");
        priorityCombo.getItems().addAll("Low", "Medium", "High", "Critical");
        priorityCombo.setValue("Medium");

        ensureTable();
        refreshBoard();
    }

    // ── DB bootstrap ────────────────────────────────────────────────────────
    private void ensureTable() {
        String sql = """
                    CREATE TABLE IF NOT EXISTS tasks (
                        id          INT AUTO_INCREMENT PRIMARY KEY,
                        title       VARCHAR(255) NOT NULL,
                        description TEXT,
                        status      ENUM('Todo','Doing','Done') DEFAULT 'Todo',
                        priority    ENUM('Low','Medium','High','Critical') DEFAULT 'Medium',
                        created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )
                """;
        try (Connection conn = DatabaseManager.getConnection();
                Statement st = conn.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ── Board refresh ────────────────────────────────────────────────────────
    private void refreshBoard() {
        todoColumn.getChildren().clear();
        doingColumn.getChildren().clear();
        doneColumn.getChildren().clear();

        int[] counts = { 0, 0, 0 };

        String sql = "SELECT * FROM tasks ORDER BY FIELD(priority,'Critical','High','Medium','Low'), created_at DESC";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement st = conn.prepareStatement(sql);
                ResultSet rs = st.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                String desc = rs.getString("description");
                String status = rs.getString("status");
                String pri = rs.getString("priority");
                Timestamp ts = rs.getTimestamp("created_at");

                VBox card = buildCard(id, title, desc, status, pri, ts);

                switch (status) {
                    case "Todo" -> {
                        todoColumn.getChildren().add(card);
                        counts[0]++;
                    }
                    case "Doing" -> {
                        doingColumn.getChildren().add(card);
                        counts[1]++;
                    }
                    case "Done" -> {
                        doneColumn.getChildren().add(card);
                        counts[2]++;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        todoCount.setText(String.valueOf(counts[0]));
        doingCount.setText(String.valueOf(counts[1]));
        doneCount.setText(String.valueOf(counts[2]));
    }

    // ── Card builder ────────────────────────────────────────────────────────
    private VBox buildCard(int id, String title, String desc, String status, String priority, Timestamp createdAt) {
        VBox card = new VBox(8);
        card.getStyleClass().add("task-card");

        // Priority stripe
        HBox stripe = new HBox();
        stripe.getStyleClass().addAll("task-priority-stripe", "pri-" + priority.toLowerCase());
        stripe.setPrefHeight(4);
        stripe.setMaxWidth(Double.MAX_VALUE);

        // Priority badge
        Label priBadge = new Label(priority);
        priBadge.getStyleClass().addAll("task-priority-badge", "pri-badge-" + priority.toLowerCase());

        // Title
        Label titleLbl = new Label(title);
        titleLbl.getStyleClass().add("task-card-title");
        titleLbl.setWrapText(true);

        // Description (optional)
        VBox middle = new VBox(4);
        middle.getChildren().add(priBadge);
        middle.getChildren().add(titleLbl);
        if (desc != null && !desc.isBlank()) {
            Label descLbl = new Label(desc);
            descLbl.getStyleClass().add("task-card-desc");
            descLbl.setWrapText(true);
            middle.getChildren().add(descLbl);
        }

        // Date
        String dateStr = "";
        if (createdAt != null) {
            dateStr = createdAt.toLocalDateTime()
                    .format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
        }
        Label dateLbl = new Label("📅 " + dateStr);
        dateLbl.getStyleClass().add("task-card-date");

        // Action buttons
        Button editBtn = new Button("✏");
        editBtn.getStyleClass().add("task-icon-btn");
        editBtn.setOnAction(e -> openEditForm(id, title, desc, status, priority));

        Button deleteBtn = new Button("🗑");
        deleteBtn.getStyleClass().addAll("task-icon-btn", "task-icon-btn-danger");
        deleteBtn.setOnAction(e -> deleteTask(id));

        // Move buttons
        HBox moveBox = new HBox(6);
        moveBox.setAlignment(Pos.CENTER_LEFT);
        if (!"Todo".equals(status)) {
            Button leftBtn = new Button("◀");
            leftBtn.getStyleClass().add("task-move-btn");
            String prevStatus = "Todo".equals(status) ? "Todo" : ("Doing".equals(status) ? "Todo" : "Doing");
            leftBtn.setOnAction(e -> moveTask(id, prevStatus));
            moveBox.getChildren().add(leftBtn);
        }
        if (!"Done".equals(status)) {
            Button rightBtn = new Button("▶");
            rightBtn.getStyleClass().add("task-move-btn");
            String nextStatus = "Todo".equals(status) ? "Doing" : "Done";
            rightBtn.setOnAction(e -> moveTask(id, nextStatus));
            moveBox.getChildren().add(rightBtn);
        }
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox footer = new HBox(6);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.getChildren().addAll(dateLbl, spacer, moveBox, editBtn, deleteBtn);

        card.getChildren().addAll(stripe, middle, footer);
        return card;
    }

    // ── Quick add ────────────────────────────────────────────────────────────
    @FXML
    public void quickAddTask(ActionEvent event) {
        String title = quickAddField.getText() == null ? "" : quickAddField.getText().trim();
        if (title.isEmpty())
            return;

        String status = quickAddStatus.getValue();
        String sql = "INSERT INTO tasks (title, status, priority) VALUES (?, ?, 'Medium')";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, title);
            st.setString(2, status);
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        quickAddField.clear();
        refreshBoard();
    }

    // ── Show new task form ───────────────────────────────────────────────────
    @FXML
    public void showNewTaskForm(ActionEvent event) {
        editingTaskId = null;
        formTitleLabel.setText("New Task");
        titleField.clear();
        descField.clear();
        statusCombo.setValue("Todo");
        priorityCombo.setValue("Medium");
        saveBtn.setText("Create Task");
        taskFormPanel.setVisible(true);
        taskFormPanel.setManaged(true);
    }

    private void openEditForm(int id, String title, String desc, String status, String priority) {
        editingTaskId = id;
        formTitleLabel.setText("Edit Task");
        titleField.setText(title);
        descField.setText(desc == null ? "" : desc);
        statusCombo.setValue(status);
        priorityCombo.setValue(priority);
        saveBtn.setText("Save Changes");
        taskFormPanel.setVisible(true);
        taskFormPanel.setManaged(true);
    }

    // ── Save (insert or update) ──────────────────────────────────────────────
    @FXML
    public void saveTask(ActionEvent event) {
        String title = titleField.getText() == null ? "" : titleField.getText().trim();
        if (title.isEmpty()) {
            showAlert("Validation", "Title is required.");
            return;
        }
        String desc = descField.getText();
        String status = statusCombo.getValue();
        String priority = priorityCombo.getValue();

        try (Connection conn = DatabaseManager.getConnection()) {
            if (editingTaskId == null) {
                String sql = "INSERT INTO tasks (title, description, status, priority) VALUES (?,?,?,?)";
                PreparedStatement st = conn.prepareStatement(sql);
                st.setString(1, title);
                st.setString(2, desc);
                st.setString(3, status);
                st.setString(4, priority);
                st.executeUpdate();
            } else {
                String sql = "UPDATE tasks SET title=?, description=?, status=?, priority=? WHERE id=?";
                PreparedStatement st = conn.prepareStatement(sql);
                st.setString(1, title);
                st.setString(2, desc);
                st.setString(3, status);
                st.setString(4, priority);
                st.setInt(5, editingTaskId);
                st.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        cancelForm(event);
        refreshBoard();
    }

    // ── Cancel form ──────────────────────────────────────────────────────────
    @FXML
    public void cancelForm(ActionEvent event) {
        taskFormPanel.setVisible(false);
        taskFormPanel.setManaged(false);
        editingTaskId = null;
    }

    // ── Move task ───────────────────────────────────────────────────────────
    private void moveTask(int id, String newStatus) {
        String sql = "UPDATE tasks SET status=? WHERE id=?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, newStatus);
            st.setInt(2, id);
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        refreshBoard();
    }

    // ── Delete task ──────────────────────────────────────────────────────────
    private void deleteTask(int id) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete this task?", ButtonType.YES, ButtonType.CANCEL);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText(null);
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                String sql = "DELETE FROM tasks WHERE id=?";
                try (Connection conn = DatabaseManager.getConnection();
                        PreparedStatement st = conn.prepareStatement(sql)) {
                    st.setInt(1, id);
                    st.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                refreshBoard();
            }
        });
    }

    // ── Navigation ───────────────────────────────────────────────────────────
    @FXML
    public void goToDashboard(Event event) {
        try {
            MainApp.setRoot("dashboard");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void goToPatients(Event event) {
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
    public void openProfilePage(Event event) {
        try {
            MainApp.setRoot("profile");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void logoutAction(Event event) {
        com.app.util.UserSession.cleanUserSession();
        try {
            MainApp.setRoot("login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ── Window controls ──────────────────────────────────────────────────────
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
        ((Stage) rootPane.getScene().getWindow()).setIconified(true);
    }

    @FXML
    public void handleMaximize(ActionEvent event) {
        MainApp.toggleMaximize((Stage) rootPane.getScene().getWindow());
    }

    @FXML
    public void handleClose(ActionEvent event) {
        MainApp.closeApp();
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        a.setTitle(title);
        a.setHeaderText(null);
        a.showAndWait();
    }
}

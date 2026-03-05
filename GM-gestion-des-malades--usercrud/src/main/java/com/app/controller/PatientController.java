package com.app.controller;

import com.app.MainApp;
import com.app.util.DatabaseManager;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class PatientController {

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
    private ComboBox<String> genderComboBox;

    @FXML
    private DatePicker dobDatePicker;

    @FXML
    private TextArea addressTextArea;

    @FXML
    private ComboBox<String> bloodTypeComboBox;

    @FXML
    private TextField weightField;

    @FXML
    private TextField heightField;

    @FXML
    private TextArea allergiesTextArea;

    @FXML
    private TextArea chronicDiseasesTextArea;

    @FXML
    private TextField emergencyContactField;

    @FXML
    private TextField emergencyPhoneField;

    @FXML
    private Button addAppointmentButton;

    @FXML
    private FlowPane patientsFlowPane;

    @FXML
    private VBox gridContainer;

    @FXML
    private VBox formContainer;

    @FXML
    private Button saveButton;

    @FXML
    private Button clearButton;

    @FXML
    private Button deleteButton;

    @FXML
    private TextField searchNameField;

    @FXML
    private TextField searchPhoneField;

    @FXML
    private DatePicker searchDobPicker;

    private ObservableList<Patient> patientList = FXCollections.observableArrayList();
    private FilteredList<Patient> filteredPatientList;
    private Patient selectedPatient;
    private double xOffset = 0;
    private double yOffset = 0;

    public void initialize() {
        setupComboBoxes();

        // Wrap the ObservableList in a FilteredList (initially display all data)
        filteredPatientList = new FilteredList<>(patientList, b -> true);

        setupSearchListeners();

        setupPatientsGrid();
        loadPatients();
    }

    private void setupSearchListeners() {
        // Name Field Listener
        if (searchNameField != null) {
            searchNameField.textProperty().addListener((observable, oldValue, newValue) -> {
                updateSearchFilter();
            });
        }

        // Phone Field Listener
        if (searchPhoneField != null) {
            searchPhoneField.textProperty().addListener((observable, oldValue, newValue) -> {
                updateSearchFilter();
            });
        }

        // Date Field Listener
        if (searchDobPicker != null) {
            searchDobPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
                updateSearchFilter();
            });
        }
    }

    private void updateSearchFilter() {
        filteredPatientList.setPredicate(patient -> {

            // 1. Check Name
            String searchName = searchNameField != null ? searchNameField.getText() : "";
            boolean matchesName = true;
            if (searchName != null && !searchName.trim().isEmpty()) {
                String lowerCaseFilter = searchName.toLowerCase();
                String fullName = (patient.getFirstName() + " " + patient.getLastName()).toLowerCase();
                matchesName = fullName.contains(lowerCaseFilter);
            }

            // 2. Check Phone
            String searchPhone = searchPhoneField != null ? searchPhoneField.getText() : "";
            boolean matchesPhone = true;
            if (searchPhone != null && !searchPhone.trim().isEmpty()) {
                matchesPhone = patient.getPhone() != null && patient.getPhone().contains(searchPhone);
            }

            // 3. Check Date of Birth
            LocalDate searchDate = searchDobPicker != null ? searchDobPicker.getValue() : null;
            boolean matchesDob = true;
            if (searchDate != null) {
                matchesDob = patient.getDateOfBirth() != null
                        && patient.getDateOfBirth().startsWith(searchDate.toString());
            }

            // Patient must match ALL active criteria
            return matchesName && matchesPhone && matchesDob;
        });

        refreshGridUI();
    }

    @FXML
    public void clearFilters(ActionEvent event) {
        if (searchNameField != null)
            searchNameField.clear();
        if (searchPhoneField != null)
            searchPhoneField.clear();
        if (searchDobPicker != null)
            searchDobPicker.setValue(null);
    }

    private void refreshGridUI() {
        if (patientsFlowPane != null) {
            patientsFlowPane.getChildren().clear();
            for (Patient patient : filteredPatientList) {
                javafx.scene.layout.VBox card = createPatientCard(patient);
                patientsFlowPane.getChildren().add(card);
            }
        }
    }

    private void setupComboBoxes() {
        genderComboBox.setItems(FXCollections.observableArrayList("Male", "Female", "Other"));
        bloodTypeComboBox.setItems(FXCollections.observableArrayList("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"));
    }

    private void setupPatientsGrid() {
        showGrid(null);
    }

    @FXML
    public void showAddPatientForm(ActionEvent event) {
        clearForm();
        gridContainer.setVisible(false);
        gridContainer.setManaged(false);
        formContainer.setVisible(true);
        formContainer.setManaged(true);
    }

    @FXML
    public void showGrid(ActionEvent event) {
        formContainer.setVisible(false);
        formContainer.setManaged(false);
        gridContainer.setVisible(true);
        gridContainer.setManaged(true);
        loadPatients();
    }

    private void populateForm(Patient patient) {
        firstNameField.setText(patient.getFirstName());
        lastNameField.setText(patient.getLastName());
        emailField.setText(patient.getEmail());
        phoneField.setText(patient.getPhone());
        genderComboBox.setValue(patient.getGender());

        if (patient.getDateOfBirth() != null && !patient.getDateOfBirth().isEmpty()) {
            try {
                LocalDate date = LocalDate.parse(patient.getDateOfBirth(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                dobDatePicker.setValue(date);
            } catch (Exception e) {
                dobDatePicker.setValue(null);
            }
        }

        addressTextArea.setText(patient.getAddress());
        bloodTypeComboBox.setValue(patient.getBloodType());
        weightField.setText(patient.getWeight());
        heightField.setText(patient.getHeight());
        allergiesTextArea.setText(patient.getAllergies());
        chronicDiseasesTextArea.setText(patient.getChronicDiseases());
        emergencyContactField.setText(patient.getEmergencyContact());
        emergencyPhoneField.setText(patient.getEmergencyPhone());
    }

    private void clearForm() {
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        phoneField.clear();
        genderComboBox.setValue(null);
        dobDatePicker.setValue(null);
        addressTextArea.clear();
        bloodTypeComboBox.setValue(null);
        weightField.clear();
        heightField.clear();
        allergiesTextArea.clear();
        chronicDiseasesTextArea.clear();
        emergencyContactField.clear();
        emergencyPhoneField.clear();
        selectedPatient = null;
    }

    @FXML
    public void savePatient(ActionEvent event) {
        if (!validateForm()) {
            showAlert("Validation Error", "Please fill in all required fields.");
            return;
        }

        try (Connection conn = DatabaseManager.getConnection()) {
            String sql;
            if (selectedPatient == null) {
                sql = "INSERT INTO patients (first_name, last_name, email, phone, gender, date_of_birth, address, " +
                        "blood_type, weight, height, allergies, chronic_diseases, emergency_contact, emergency_phone) "
                        +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            } else {
                sql = "UPDATE patients SET first_name=?, last_name=?, email=?, phone=?, gender=?, date_of_birth=?, " +
                        "address=?, blood_type=?, weight=?, height=?, allergies=?, chronic_diseases=?, " +
                        "emergency_contact=?, emergency_phone=? WHERE id=?";
            }

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, firstNameField.getText());
            stmt.setString(2, lastNameField.getText());
            stmt.setString(3, emailField.getText());
            stmt.setString(4, phoneField.getText());
            stmt.setString(5, genderComboBox.getValue());

            LocalDate dob = dobDatePicker.getValue();
            stmt.setString(6, dob != null ? dob.toString() : null);

            stmt.setString(7, addressTextArea.getText());
            stmt.setString(8, bloodTypeComboBox.getValue());
            String weightStr = weightField.getText() != null ? weightField.getText().trim() : "";
            if (weightStr.isEmpty()) {
                stmt.setNull(9, java.sql.Types.DECIMAL);
            } else {
                stmt.setString(9, weightStr);
            }

            String heightStr = heightField.getText() != null ? heightField.getText().trim() : "";
            if (heightStr.isEmpty()) {
                stmt.setNull(10, java.sql.Types.DECIMAL);
            } else {
                stmt.setString(10, heightStr);
            }
            stmt.setString(11, allergiesTextArea.getText());
            stmt.setString(12, chronicDiseasesTextArea.getText());
            stmt.setString(13, emergencyContactField.getText());
            stmt.setString(14, emergencyPhoneField.getText());

            if (selectedPatient != null) {
                stmt.setInt(15, selectedPatient.getId());
            }

            stmt.executeUpdate();

            showAlert("Success",
                    selectedPatient == null ? "Patient added successfully!" : "Patient updated successfully!");
            clearForm();
            showGrid(null);

        } catch (SQLException e) {
            showAlert("Database Error", "Error saving patient: " + e.getMessage());
        }
    }

    @FXML
    public void clearForm(ActionEvent event) {
        clearForm();
    }

    @FXML
    public void deletePatient(ActionEvent event) {
        if (selectedPatient == null) {
            showAlert("No Selection", "Please select a patient to delete.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Patient");
        alert.setContentText("Are you sure you want to delete this patient?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection conn = DatabaseManager.getConnection()) {
                String sql = "DELETE FROM patients WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, selectedPatient.getId());
                stmt.executeUpdate();

                showAlert("Success", "Patient deleted successfully!");
                clearForm();
                showGrid(null);

            } catch (SQLException e) {
                showAlert("Database Error", "Error deleting patient: " + e.getMessage());
            }
        }
    }

    private void loadPatients() {
        patientList.clear();
        if (patientsFlowPane != null) {
            patientsFlowPane.getChildren().clear();
        }

        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT * FROM patients ORDER BY created_at DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                java.sql.Timestamp createdAtTimestamp = rs.getTimestamp("created_at");
                String createdAtStr = createdAtTimestamp != null ? createdAtTimestamp.toString() : "";

                Patient patient = new Patient(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("gender"),
                        rs.getString("date_of_birth"),
                        rs.getString("address"),
                        rs.getString("blood_type"),
                        rs.getString("weight"),
                        rs.getString("height"),
                        rs.getString("allergies"),
                        rs.getString("chronic_diseases"),
                        rs.getString("emergency_contact"),
                        rs.getString("emergency_phone"),
                        createdAtStr);
                patientList.add(patient);
            }

            // Refresh the grid using the newly populated filtered list
            refreshGridUI();

        } catch (SQLException e) {
            showAlert("Database Error", "Error loading patients: " + e.getMessage());
        }
    }

    private javafx.scene.layout.VBox createPatientCard(Patient patient) {
        javafx.scene.layout.VBox card = new javafx.scene.layout.VBox();
        card.getStyleClass().add("patient-card");

        // Banner image matching the UI mockup
        javafx.scene.layout.Region banner = new javafx.scene.layout.Region();
        banner.getStyleClass().add("patient-card-banner");

        // Tag / status
        Label tagLabel = new Label(patient.getBloodType() != null ? patient.getBloodType() : "Patient");
        tagLabel.getStyleClass().add("patient-tag");

        Label nameLabel = new Label(patient.getFirstName() + " " + patient.getLastName());
        nameLabel.getStyleClass().add("patient-name");

        Label descLabel = new Label((patient.getChronicDiseases() != null && !patient.getChronicDiseases().isEmpty())
                ? "Conditions: " + patient.getChronicDiseases()
                : "No chronic diseases recorded by doctor.");
        descLabel.getStyleClass().add("patient-desc");
        descLabel.setWrapText(true);

        Label phoneLabel = new Label(
                patient.getPhone() != null && !patient.getPhone().isEmpty() ? "📞 " + patient.getPhone()
                        : "📞 No phone");
        phoneLabel.getStyleClass().add("patient-phone");

        // Buttons
        javafx.scene.layout.HBox buttonBox = new javafx.scene.layout.HBox();
        buttonBox.setSpacing(10);

        Button viewBtn = new Button("View Profile");
        viewBtn.getStyleClass().add("btn-white-sm");
        viewBtn.setOnAction(e -> {
            selectedPatient = patient;
            populateForm(selectedPatient);

            gridContainer.setVisible(false);
            gridContainer.setManaged(false);
            formContainer.setVisible(true);
            formContainer.setManaged(true);
        });

        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("btn-danger-sm");
        deleteBtn.setOnAction(e -> {
            selectedPatient = patient;
            deletePatient(null);
        });

        buttonBox.getChildren().addAll(viewBtn, deleteBtn);

        card.getChildren().addAll(banner, tagLabel, nameLabel, descLabel, phoneLabel, buttonBox);
        return card;
    }

    private boolean validateForm() {
        return firstNameField.getText() != null && !firstNameField.getText().trim().isEmpty() &&
                lastNameField.getText() != null && !lastNameField.getText().trim().isEmpty() &&
                genderComboBox.getValue() != null;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
    public void goToDashboard(MouseEvent event) {
        try {
            MainApp.setRoot("dashboard");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void goBack(MouseEvent event) {
        try {
            MainApp.setRoot("dashboard");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void openAppointmentsPage(MouseEvent event) {
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

    public static class Patient {
        private final SimpleIntegerProperty id;
        private final SimpleStringProperty firstName;
        private final SimpleStringProperty lastName;
        private final SimpleStringProperty email;
        private final SimpleStringProperty phone;
        private final SimpleStringProperty gender;
        private final SimpleStringProperty dateOfBirth;
        private final SimpleStringProperty address;
        private final SimpleStringProperty bloodType;
        private final SimpleStringProperty weight;
        private final SimpleStringProperty height;
        private final SimpleStringProperty allergies;
        private final SimpleStringProperty chronicDiseases;
        private final SimpleStringProperty emergencyContact;
        private final SimpleStringProperty emergencyPhone;
        private final SimpleStringProperty createdAt;

        public Patient(int id, String firstName, String lastName, String email, String phone, String gender,
                String dateOfBirth, String address, String bloodType, String weight, String height,
                String allergies, String chronicDiseases, String emergencyContact, String emergencyPhone,
                String createdAt) {
            this.id = new SimpleIntegerProperty(id);
            this.firstName = new SimpleStringProperty(firstName);
            this.lastName = new SimpleStringProperty(lastName);
            this.email = new SimpleStringProperty(email);
            this.phone = new SimpleStringProperty(phone);
            this.gender = new SimpleStringProperty(gender);
            this.dateOfBirth = new SimpleStringProperty(dateOfBirth);
            this.address = new SimpleStringProperty(address);
            this.bloodType = new SimpleStringProperty(bloodType);
            this.weight = new SimpleStringProperty(weight);
            this.height = new SimpleStringProperty(height);
            this.allergies = new SimpleStringProperty(allergies);
            this.chronicDiseases = new SimpleStringProperty(chronicDiseases);
            this.emergencyContact = new SimpleStringProperty(emergencyContact);
            this.emergencyPhone = new SimpleStringProperty(emergencyPhone);
            this.createdAt = new SimpleStringProperty(createdAt);
        }

        public int getId() {
            return id.get();
        }

        public String getFirstName() {
            return firstName.get();
        }

        public String getLastName() {
            return lastName.get();
        }

        public String getEmail() {
            return email.get();
        }

        public String getPhone() {
            return phone.get();
        }

        public String getGender() {
            return gender.get();
        }

        public String getDateOfBirth() {
            return dateOfBirth.get();
        }

        public String getAddress() {
            return address.get();
        }

        public String getBloodType() {
            return bloodType.get();
        }

        public String getWeight() {
            return weight.get();
        }

        public String getHeight() {
            return height.get();
        }

        public String getAllergies() {
            return allergies.get();
        }

        public String getChronicDiseases() {
            return chronicDiseases.get();
        }

        public String getEmergencyContact() {
            return emergencyContact.get();
        }

        public String getEmergencyPhone() {
            return emergencyPhone.get();
        }

        public String getCreatedAt() {
            return createdAt.get();
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

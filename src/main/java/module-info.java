module com.app {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;
    requires mysql.connector.j;

    opens com.app.controller to javafx.fxml;

    exports com.app;
}

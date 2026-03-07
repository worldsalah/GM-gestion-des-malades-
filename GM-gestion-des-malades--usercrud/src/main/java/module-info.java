module com.app {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;
    requires java.net.http;
    requires mysql.connector.j;
    requires opencv;
    requires org.json;

    opens com.app.controller to javafx.fxml, javafx.base;

    exports com.app;
}

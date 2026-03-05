package com.app.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {

    private static final String USER = "root";
    private static final String[] PORTS = { "3307", "3306" };
    // User confirmed password is "SALAH"
    private static final String[] POSSIBLE_PASSWORDS = { "SALAH", "", "root", "1234", "123456", "admin", "admin123",
            "password", "mysql" };

    public static Connection getConnection() throws SQLException {
        SQLException lastException = null;
        for (String port : PORTS) {
            String dbUrl = "jdbc:mysql://127.0.0.1:" + port
                    + "/appdb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            for (String password : POSSIBLE_PASSWORDS) {
                try {
                    Connection connection = DriverManager.getConnection(dbUrl, USER, password);
                    System.out.println("✅ Database connected successfully! Port: " + port + ", Pass: "
                            + (password.isEmpty() ? "<empty>" : password));

                    // Automatically run schema updates for Advanced Appointments feature
                    applySchemaUpdates(connection);

                    return connection;
                } catch (SQLException e) {
                    lastException = e;
                    // If it's not a connection/auth error, keep trying
                }
            }
        }
        if (lastException != null) {
            throw lastException;
        }
        return null;
    }

    private static void applySchemaUpdates(Connection conn) {
        try {
            // Check and add 'priority' column
            var rs = conn.getMetaData().getColumns(null, null, "appointments", "priority");
            if (!rs.next()) {
                System.out.println("🔧 Adding 'priority' column to 'appointments' table...");
                conn.createStatement()
                        .execute("ALTER TABLE appointments ADD COLUMN priority VARCHAR(20) DEFAULT 'Normal'");
            }
            rs.close();

            // Check and add 'recurrence' column
            rs = conn.getMetaData().getColumns(null, null, "appointments", "recurrence");
            if (!rs.next()) {
                System.out.println("🔧 Adding 'recurrence' column to 'appointments' table...");
                conn.createStatement()
                        .execute("ALTER TABLE appointments ADD COLUMN recurrence VARCHAR(20) DEFAULT 'None'");
            }
            rs.close();

        } catch (SQLException e) {
            System.err.println("⚠️ Error applying schema updates: " + e.getMessage());
        }
    }
}

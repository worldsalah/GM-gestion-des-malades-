package com.app.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

            // Check and add 'charge_amount' column
            rs = conn.getMetaData().getColumns(null, null, "appointments", "charge_amount");
            if (!rs.next()) {
                System.out.println("🔧 Adding 'charge_amount' column to 'appointments' table...");
                conn.createStatement()
                        .execute("ALTER TABLE appointments ADD COLUMN charge_amount DECIMAL(10,2) DEFAULT 0.00");
            }
            rs.close();

            // Check and add 'face_template' column to 'users'
            rs = conn.getMetaData().getColumns(null, null, "users", "face_template");
            if (!rs.next()) {
                System.out.println("🔧 Adding 'face_template' column to 'users' table...");
                conn.createStatement().execute("ALTER TABLE users ADD COLUMN face_template LONGTEXT");
            } else {
                // Ensure it's LONGTEXT if it already exists as TEXT
                System.out.println("🔧 Ensuring 'face_template' is LONGTEXT...");
                conn.createStatement().execute("ALTER TABLE users MODIFY COLUMN face_template LONGTEXT");
            }
            rs.close();

            // Check and add 'social_id' column to 'users'
            rs = conn.getMetaData().getColumns(null, null, "users", "social_id");
            if (!rs.next()) {
                System.out.println("🔧 Adding 'social_id' column to 'users' table...");
                conn.createStatement().execute("ALTER TABLE users ADD COLUMN social_id VARCHAR(255)");
            }
            rs.close();

            // Check and add 'social_provider' column to 'users'
            rs = conn.getMetaData().getColumns(null, null, "users", "social_provider");
            if (!rs.next()) {
                System.out.println("🔧 Adding 'social_provider' column to 'users' table...");
                conn.createStatement().execute("ALTER TABLE users ADD COLUMN social_provider VARCHAR(50)");
            }
            rs.close();

        } catch (SQLException e) {
            System.err.println("⚠️ Error applying schema updates: " + e.getMessage());
        }
    }

    // --- Statistics Helper Methods ---

    public static int getTotalPatients() {
        String sql = "SELECT COUNT(*) FROM patients";
        try (Connection conn = getConnection();
                PreparedStatement st = conn.prepareStatement(sql);
                ResultSet rs = st.executeQuery()) {
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int getTodayAppointmentsCount() {
        String sql = "SELECT COUNT(*) FROM appointments WHERE appointment_date = CURRENT_DATE";
        try (Connection conn = getConnection();
                PreparedStatement st = conn.prepareStatement(sql);
                ResultSet rs = st.executeQuery()) {
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static double getTodayRevenue() {
        String sql = "SELECT SUM(charge_amount) FROM appointments WHERE appointment_date = CURRENT_DATE AND status = 'Done'";
        try (Connection conn = getConnection();
                PreparedStatement st = conn.prepareStatement(sql);
                ResultSet rs = st.executeQuery()) {
            if (rs.next())
                return rs.getDouble(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public static int getMonthlyTreatments() {
        String sql = "SELECT COUNT(*) FROM appointments WHERE MONTH(appointment_date) = MONTH(CURRENT_DATE) AND YEAR(appointment_date) = YEAR(CURRENT_DATE) AND status = 'Done'";
        try (Connection conn = getConnection();
                PreparedStatement st = conn.prepareStatement(sql);
                ResultSet rs = st.executeQuery()) {
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}

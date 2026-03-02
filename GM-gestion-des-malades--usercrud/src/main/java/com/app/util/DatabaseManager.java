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
}

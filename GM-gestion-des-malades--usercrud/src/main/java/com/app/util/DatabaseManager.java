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

            // --- RBAC Schema Updates ---

            // Add 'doctor_id' to patients
            rs = conn.getMetaData().getColumns(null, null, "patients", "doctor_id");
            if (!rs.next()) {
                System.out.println("🔧 Adding 'doctor_id' column to 'patients' table...");
                conn.createStatement().execute("ALTER TABLE patients ADD COLUMN doctor_id INT");
            }
            rs.close();

            // Add 'doctor_id' to appointments
            rs = conn.getMetaData().getColumns(null, null, "appointments", "doctor_id");
            if (!rs.next()) {
                System.out.println("🔧 Adding 'doctor_id' column to 'appointments' table...");
                conn.createStatement().execute("ALTER TABLE appointments ADD COLUMN doctor_id INT");
            }
            rs.close();

            // Add 'doctor_id' and 'assigned_to' to tasks (if table exists)
            try {
                rs = conn.getMetaData().getColumns(null, null, "tasks", "doctor_id");
                if (!rs.next()) {
                    System.out.println("🔧 Adding 'doctor_id' column to 'tasks' table...");
                    conn.createStatement().execute("ALTER TABLE tasks ADD COLUMN doctor_id INT");
                }
                rs.close();

                rs = conn.getMetaData().getColumns(null, null, "tasks", "assigned_to");
                if (!rs.next()) {
                    System.out.println("🔧 Adding 'assigned_to' column to 'tasks' table...");
                    conn.createStatement().execute("ALTER TABLE tasks ADD COLUMN assigned_to INT");
                }
                rs.close();
            } catch (Exception e) {
                System.out.println("ℹ️ Tasks table updates skipped (might not exist yet).");
            }

        } catch (SQLException e) {
            System.err.println("⚠️ Error applying schema updates: " + e.getMessage());
        }
    }

    // --- Statistics Helper Methods ---

    /** Returns total patients. If doctorId is not null, filters by that doctor. */
    public static int getTotalPatients(Integer doctorId) {
        String sql = doctorId != null
                ? "SELECT COUNT(*) FROM patients WHERE doctor_id = ?"
                : "SELECT COUNT(*) FROM patients";
        try (Connection conn = getConnection();
                PreparedStatement st = conn.prepareStatement(sql)) {
            if (doctorId != null)
                st.setInt(1, doctorId);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** Convenience overload that shows all patients (Secretary view). */
    public static int getTotalPatients() {
        return getTotalPatients(null);
    }

    /**
     * Returns today's appointment count. If doctorId is not null, filters by that
     * doctor.
     */
    public static int getTodayAppointmentsCount(Integer doctorId) {
        String sql = doctorId != null
                ? "SELECT COUNT(*) FROM appointments WHERE appointment_date = CURRENT_DATE AND doctor_id = ?"
                : "SELECT COUNT(*) FROM appointments WHERE appointment_date = CURRENT_DATE";
        try (Connection conn = getConnection();
                PreparedStatement st = conn.prepareStatement(sql)) {
            if (doctorId != null)
                st.setInt(1, doctorId);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int getTodayAppointmentsCount() {
        return getTodayAppointmentsCount(null);
    }

    /** Returns today's revenue. If doctorId is not null, filters by that doctor. */
    public static double getTodayRevenue(Integer doctorId) {
        String sql = doctorId != null
                ? "SELECT COALESCE(SUM(charge_amount), 0) FROM appointments WHERE appointment_date = CURRENT_DATE AND status = 'Done' AND doctor_id = ?"
                : "SELECT COALESCE(SUM(charge_amount), 0) FROM appointments WHERE appointment_date = CURRENT_DATE AND status = 'Done'";
        try (Connection conn = getConnection();
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

    public static double getTodayRevenue() {
        return getTodayRevenue(null);
    }

    /**
     * Returns completed treatments this month. If doctorId is not null, filters by
     * that doctor.
     */
    public static int getMonthlyTreatments(Integer doctorId) {
        String sql = doctorId != null
                ? "SELECT COUNT(*) FROM appointments WHERE MONTH(appointment_date) = MONTH(CURRENT_DATE) AND YEAR(appointment_date) = YEAR(CURRENT_DATE) AND status = 'Done' AND doctor_id = ?"
                : "SELECT COUNT(*) FROM appointments WHERE MONTH(appointment_date) = MONTH(CURRENT_DATE) AND YEAR(appointment_date) = YEAR(CURRENT_DATE) AND status = 'Done'";
        try (Connection conn = getConnection();
                PreparedStatement st = conn.prepareStatement(sql)) {
            if (doctorId != null)
                st.setInt(1, doctorId);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int getMonthlyTreatments() {
        return getMonthlyTreatments(null);
    }

    /**
     * Returns monthly revenue for each month of the current year.
     * Returns an array of 12 doubles (index 0=Jan, 11=Dec).
     */
    public static double[] getMonthlyRevenueData(Integer doctorId) {
        double[] data = new double[12];
        String sql = doctorId != null
                ? "SELECT MONTH(appointment_date) as m, COALESCE(SUM(charge_amount), 0) as rev FROM appointments WHERE YEAR(appointment_date) = YEAR(CURRENT_DATE) AND status = 'Done' AND doctor_id = ? GROUP BY MONTH(appointment_date)"
                : "SELECT MONTH(appointment_date) as m, COALESCE(SUM(charge_amount), 0) as rev FROM appointments WHERE YEAR(appointment_date) = YEAR(CURRENT_DATE) AND status = 'Done' GROUP BY MONTH(appointment_date)";
        try (Connection conn = getConnection();
                PreparedStatement st = conn.prepareStatement(sql)) {
            if (doctorId != null)
                st.setInt(1, doctorId);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    int month = rs.getInt("m"); // 1-12
                    data[month - 1] = rs.getDouble("rev");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    /**
     * Returns revenue grouped by treatment reason.
     * Returns a 2D array where [i][0] is the label and [i][1] is the amount.
     */
    public static Object[][] getRevenueByReasonData(Integer doctorId) {
        String sql = doctorId != null
                ? "SELECT COALESCE(reason, 'Other') as reason, COALESCE(SUM(charge_amount), 0) as rev FROM appointments WHERE status = 'Done' AND doctor_id = ? GROUP BY reason ORDER BY rev DESC LIMIT 6"
                : "SELECT COALESCE(reason, 'Other') as reason, COALESCE(SUM(charge_amount), 0) as rev FROM appointments WHERE status = 'Done' GROUP BY reason ORDER BY rev DESC LIMIT 6";
        java.util.List<Object[]> rows = new java.util.ArrayList<>();
        try (Connection conn = getConnection();
                PreparedStatement st = conn.prepareStatement(sql)) {
            if (doctorId != null)
                st.setInt(1, doctorId);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[] { rs.getString("reason"), rs.getDouble("rev") });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rows.toArray(new Object[0][]);
    }

    /** Returns appointment counts grouped by status for pie chart. */
    public static int[] getPaymentStatusData(Integer doctorId) {
        // [paid, pending, overdue] -> [Done, Waiting/In Progress, Cancelled]
        int[] counts = new int[3];
        String sql = doctorId != null
                ? "SELECT status, COUNT(*) as cnt FROM appointments WHERE doctor_id = ? GROUP BY status"
                : "SELECT status, COUNT(*) as cnt FROM appointments GROUP BY status";
        try (Connection conn = getConnection();
                PreparedStatement st = conn.prepareStatement(sql)) {
            if (doctorId != null)
                st.setInt(1, doctorId);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    String status = rs.getString("status");
                    int cnt = rs.getInt("cnt");
                    if ("Done".equalsIgnoreCase(status))
                        counts[0] += cnt;
                    else if ("Cancelled".equalsIgnoreCase(status))
                        counts[2] += cnt;
                    else
                        counts[1] += cnt; // Waiting, In Progress, etc.
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return counts;
    }

    /** Returns revenue data for all doctors (for admin/secretary view). */
    public static Object[][] getAllDoctorsRevenueData() {
        String sql = "SELECT u.id, CONCAT(u.first_name, ' ', u.last_name) as doctor_name, " +
                     "COALESCE(SUM(a.charge_amount), 0) as total_revenue " +
                     "FROM users u " +
                     "LEFT JOIN appointments a ON u.id = a.doctor_id AND a.status = 'Done' " +
                     "WHERE u.role = 'Doctor' " +
                     "GROUP BY u.id, u.first_name, u.last_name " +
                     "ORDER BY total_revenue DESC";
        java.util.List<Object[]> rows = new java.util.ArrayList<>();
        try (Connection conn = getConnection();
                PreparedStatement st = conn.prepareStatement(sql)) {
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[] { 
                        rs.getString("doctor_name"), 
                        rs.getDouble("total_revenue"),
                        rs.getInt("id")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rows.toArray(new Object[0][]);
    }

    /** Returns monthly revenue data for all doctors (for comparison chart). */
    public static Object[][] getDoctorsMonthlyRevenueData() {
        String sql = "SELECT u.id, CONCAT(u.first_name, ' ', u.last_name) as doctor_name, " +
                     "MONTH(a.appointment_date) as month, " +
                     "COALESCE(SUM(a.charge_amount), 0) as revenue " +
                     "FROM users u " +
                     "LEFT JOIN appointments a ON u.id = a.doctor_id AND a.status = 'Done' " +
                     "WHERE u.role = 'Doctor' AND YEAR(a.appointment_date) = YEAR(CURRENT_DATE) " +
                     "GROUP BY u.id, u.first_name, u.last_name, MONTH(a.appointment_date) " +
                     "ORDER BY u.id, month";
        java.util.List<Object[]> rows = new java.util.ArrayList<>();
        try (Connection conn = getConnection();
                PreparedStatement st = conn.prepareStatement(sql)) {
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[] { 
                        rs.getString("doctor_name"),
                        rs.getInt("month"),
                        rs.getDouble("revenue")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rows.toArray(new Object[0][]);
    }

    /** Returns daily revenue for the last 30 days. */
    public static Object[][] getDailyRevenueData(Integer doctorId) {
        String sql = doctorId != null
                ? "SELECT DATE(appointment_date) as date, COALESCE(SUM(charge_amount), 0) as revenue, COUNT(*) as appointments " +
                  "FROM appointments WHERE status = 'Done' AND doctor_id = ? " +
                  "AND appointment_date >= DATE_SUB(CURRENT_DATE, INTERVAL 30 DAY) " +
                  "GROUP BY DATE(appointment_date) ORDER BY date DESC"
                : "SELECT DATE(appointment_date) as date, COALESCE(SUM(charge_amount), 0) as revenue, COUNT(*) as appointments " +
                  "FROM appointments WHERE status = 'Done' " +
                  "AND appointment_date >= DATE_SUB(CURRENT_DATE, INTERVAL 30 DAY) " +
                  "GROUP BY DATE(appointment_date) ORDER BY date DESC";
        java.util.List<Object[]> rows = new java.util.ArrayList<>();
        try (Connection conn = getConnection();
                PreparedStatement st = conn.prepareStatement(sql)) {
            if (doctorId != null)
                st.setInt(1, doctorId);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[] { 
                        rs.getString("date"),
                        rs.getDouble("revenue"),
                        rs.getInt("appointments")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rows.toArray(new Object[0][]);
    }

    /** Returns monthly revenue breakdown for the current year. */
    public static Object[][] getMonthlyRevenueBreakdown(Integer doctorId) {
        String sql = doctorId != null
                ? "SELECT MONTH(appointment_date) as month, MONTHNAME(appointment_date) as month_name, " +
                  "COALESCE(SUM(charge_amount), 0) as revenue, COUNT(*) as appointments, " +
                  "COALESCE(AVG(charge_amount), 0) as avg_revenue " +
                  "FROM appointments WHERE status = 'Done' AND doctor_id = ? " +
                  "AND YEAR(appointment_date) = YEAR(CURRENT_DATE) " +
                  "GROUP BY MONTH(appointment_date), MONTHNAME(appointment_date) " +
                  "ORDER BY month"
                : "SELECT MONTH(appointment_date) as month, MONTHNAME(appointment_date) as month_name, " +
                  "COALESCE(SUM(charge_amount), 0) as revenue, COUNT(*) as appointments, " +
                  "COALESCE(AVG(charge_amount), 0) as avg_revenue " +
                  "FROM appointments WHERE status = 'Done' " +
                  "AND YEAR(appointment_date) = YEAR(CURRENT_DATE) " +
                  "GROUP BY MONTH(appointment_date), MONTHNAME(appointment_date) " +
                  "ORDER BY month";
        java.util.List<Object[]> rows = new java.util.ArrayList<>();
        try (Connection conn = getConnection();
                PreparedStatement st = conn.prepareStatement(sql)) {
            if (doctorId != null)
                st.setInt(1, doctorId);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[] { 
                        rs.getInt("month"),
                        rs.getString("month_name"),
                        rs.getDouble("revenue"),
                        rs.getInt("appointments"),
                        rs.getDouble("avg_revenue")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rows.toArray(new Object[0][]);
    }

    /** Returns revenue comparison between current period and previous period. */
    public static Object[] getRevenueComparison(Integer doctorId, boolean isMonthly) {
        String currentPeriodSql, previousPeriodSql;
        
        if (isMonthly) {
            // Compare this month vs last month
            currentPeriodSql = doctorId != null
                    ? "SELECT COALESCE(SUM(charge_amount), 0), COUNT(*) FROM appointments WHERE status = 'Done' AND doctor_id = ? AND MONTH(appointment_date) = MONTH(CURRENT_DATE) AND YEAR(appointment_date) = YEAR(CURRENT_DATE)"
                    : "SELECT COALESCE(SUM(charge_amount), 0), COUNT(*) FROM appointments WHERE status = 'Done' AND MONTH(appointment_date) = MONTH(CURRENT_DATE) AND YEAR(appointment_date) = YEAR(CURRENT_DATE)";
            previousPeriodSql = doctorId != null
                    ? "SELECT COALESCE(SUM(charge_amount), 0), COUNT(*) FROM appointments WHERE status = 'Done' AND doctor_id = ? AND MONTH(appointment_date) = MONTH(DATE_SUB(CURRENT_DATE, INTERVAL 1 MONTH)) AND YEAR(appointment_date) = YEAR(DATE_SUB(CURRENT_DATE, INTERVAL 1 MONTH))"
                    : "SELECT COALESCE(SUM(charge_amount), 0), COUNT(*) FROM appointments WHERE status = 'Done' AND MONTH(appointment_date) = MONTH(DATE_SUB(CURRENT_DATE, INTERVAL 1 MONTH)) AND YEAR(appointment_date) = YEAR(DATE_SUB(CURRENT_DATE, INTERVAL 1 MONTH))";
        } else {
            // Compare today vs yesterday
            currentPeriodSql = doctorId != null
                    ? "SELECT COALESCE(SUM(charge_amount), 0), COUNT(*) FROM appointments WHERE status = 'Done' AND doctor_id = ? AND DATE(appointment_date) = CURRENT_DATE"
                    : "SELECT COALESCE(SUM(charge_amount), 0), COUNT(*) FROM appointments WHERE status = 'Done' AND DATE(appointment_date) = CURRENT_DATE";
            previousPeriodSql = doctorId != null
                    ? "SELECT COALESCE(SUM(charge_amount), 0), COUNT(*) FROM appointments WHERE status = 'Done' AND doctor_id = ? AND DATE(appointment_date) = DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY)"
                    : "SELECT COALESCE(SUM(charge_amount), 0), COUNT(*) FROM appointments WHERE status = 'Done' AND DATE(appointment_date) = DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY)";
        }
        
        try (Connection conn = getConnection()) {
            // Get current period data
            try (PreparedStatement st = conn.prepareStatement(currentPeriodSql)) {
                if (doctorId != null) st.setInt(1, doctorId);
                try (ResultSet rs = st.executeQuery()) {
                    if (rs.next()) {
                        double currentRevenue = rs.getDouble(1);
                        int currentAppointments = rs.getInt(2);
                        
                        // Get previous period data
                        try (PreparedStatement st2 = conn.prepareStatement(previousPeriodSql)) {
                            if (doctorId != null) st2.setInt(1, doctorId);
                            try (ResultSet rs2 = st2.executeQuery()) {
                                if (rs2.next()) {
                                    double previousRevenue = rs2.getDouble(1);
                                    int previousAppointments = rs2.getInt(2);
                                    
                                    double revenueChange = previousRevenue == 0 ? 0 : ((currentRevenue - previousRevenue) / previousRevenue) * 100;
                                    double appointmentChange = previousAppointments == 0 ? 0 : ((double)(currentAppointments - previousAppointments) / previousAppointments) * 100;
                                    
                                    return new Object[] {currentRevenue, previousRevenue, revenueChange, currentAppointments, previousAppointments, appointmentChange};
                                }
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new Object[] {0.0, 0.0, 0.0, 0, 0, 0.0};
    }

    /** Returns weekly revenue (current week). */
    public static double getWeeklyRevenue(Integer doctorId) {
        String sql = doctorId != null
                ? "SELECT COALESCE(SUM(charge_amount), 0) FROM appointments WHERE status = 'Done' AND doctor_id = ? AND YEARWEEK(appointment_date, 1) = YEARWEEK(CURRENT_DATE, 1)"
                : "SELECT COALESCE(SUM(charge_amount), 0) FROM appointments WHERE status = 'Done' AND YEARWEEK(appointment_date, 1) = YEARWEEK(CURRENT_DATE, 1)";
        try (Connection conn = getConnection();
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

    /** Returns yearly revenue (current year). */
    public static double getYearlyRevenue(Integer doctorId) {
        String sql = doctorId != null
                ? "SELECT COALESCE(SUM(charge_amount), 0) FROM appointments WHERE status = 'Done' AND doctor_id = ? AND YEAR(appointment_date) = YEAR(CURRENT_DATE)"
                : "SELECT COALESCE(SUM(charge_amount), 0) FROM appointments WHERE status = 'Done' AND YEAR(appointment_date) = YEAR(CURRENT_DATE)";
        try (Connection conn = getConnection();
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

    /** Returns previous week revenue for comparison. */
    public static double getPreviousWeekRevenue(Integer doctorId) {
        String sql = doctorId != null
                ? "SELECT COALESCE(SUM(charge_amount), 0) FROM appointments WHERE status = 'Done' AND doctor_id = ? AND YEARWEEK(appointment_date, 1) = YEARWEEK(DATE_SUB(CURRENT_DATE, INTERVAL 1 WEEK), 1)"
                : "SELECT COALESCE(SUM(charge_amount), 0) FROM appointments WHERE status = 'Done' AND YEARWEEK(appointment_date, 1) = YEARWEEK(DATE_SUB(CURRENT_DATE, INTERVAL 1 WEEK), 1)";
        try (Connection conn = getConnection();
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

    /** Returns previous year revenue for comparison. */
    public static double getPreviousYearRevenue(Integer doctorId) {
        String sql = doctorId != null
                ? "SELECT COALESCE(SUM(charge_amount), 0) FROM appointments WHERE status = 'Done' AND doctor_id = ? AND YEAR(appointment_date) = YEAR(CURRENT_DATE) - 1"
                : "SELECT COALESCE(SUM(charge_amount), 0) FROM appointments WHERE status = 'Done' AND YEAR(appointment_date) = YEAR(CURRENT_DATE) - 1";
        try (Connection conn = getConnection();
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
}

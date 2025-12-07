package web;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages all interactions with the H2 database.
 * Handles connection, table creation, and data persistence for energy history.
 */
public class DatabaseManager {

    private static final String DB_URL = "jdbc:h2:./smart_energy_db"; // Creates a file in the project root
    private Connection connection;

    /**
     * Initializes the database connection and creates the necessary table.
     */
    public void init() {
        try {
            // Ensure the H2 driver class is loaded
            Class.forName("org.h2.Driver");
            connection = DriverManager.getConnection(DB_URL, "sa", "");
            System.out.print("Connecting to database... ");

            try (Statement stmt = connection.createStatement()) {
                String sql = "CREATE TABLE IF NOT EXISTS energy_history (" +
                        "  timestamp BIGINT PRIMARY KEY," +
                        "  heure INT," +
                        "  jour INT," +
                        "  weekend INT," +
                        "  actual DOUBLE," +
                        "  predicted DOUBLE," +
                        "  status VARCHAR(255)" +
                        ")";
                stmt.executeUpdate(sql);
                System.out.println("✓");
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.out.println("✗");
            System.err.println("Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Saves a single energy data record to the database.
     * @param data A map containing the energy data for one timestamp.
     */
    public void saveEnergyRecord(Map<String, Object> data) {
        String sql = "INSERT INTO energy_history(timestamp, heure, jour, weekend, actual, predicted, status) VALUES(?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, (Long) data.get("timestamp"));
            pstmt.setInt(2, (Integer) data.get("heure"));
            pstmt.setInt(3, (Integer) data.get("jour"));
            pstmt.setInt(4, (Integer) data.get("weekend"));
            pstmt.setDouble(5, (Double) data.get("actual"));
            pstmt.setDouble(6, (Double) data.get("predicted"));
            pstmt.setString(7, (String) data.get("status"));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to save energy record: " + e.getMessage());
        }
    }

    /**
     * Retrieves the last 100 energy history records from the database.
     * @return A list of maps, where each map is a historical record.
     */
    public List<Map<String, Object>> getEnergyHistory() {
        List<Map<String, Object>> history = new ArrayList<>();
        // Get the most recent 100 records
        String sql = "SELECT * FROM energy_history ORDER BY timestamp DESC LIMIT 100";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Map<String, Object> record = new HashMap<>();
                record.put("timestamp", rs.getLong("timestamp"));
                record.put("heure", rs.getInt("heure"));
                record.put("jour", rs.getInt("jour"));
                record.put("weekend", rs.getInt("weekend"));
                record.put("actual", rs.getDouble("actual"));
                record.put("predicted", rs.getDouble("predicted"));
                record.put("status", rs.getString("status"));
                history.add(record);
            }
        } catch (SQLException e) {
            System.err.println("Failed to retrieve energy history: " + e.getMessage());
        }
        // The query gets newest first, so reverse the list for chronological order on the chart
        Collections.reverse(history);
        return history;
    }

    /**
     * Retrieves the single most recent energy record from the database.
     * @return A map representing the latest record, or null if the DB is empty.
     */
    public Map<String, Object> getLatestEnergyRecord() {
        String sql = "SELECT * FROM energy_history ORDER BY timestamp DESC LIMIT 1";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                Map<String, Object> record = new HashMap<>();
                record.put("timestamp", rs.getLong("timestamp"));
                record.put("heure", rs.getInt("heure"));
                record.put("jour", rs.getInt("jour"));
                record.put("weekend", rs.getInt("weekend"));
                record.put("actual", rs.getDouble("actual"));
                record.put("predicted", rs.getDouble("predicted"));
                record.put("status", rs.getString("status"));
                return record;
            }
        } catch (SQLException e) {
            System.err.println("Failed to retrieve latest record: " + e.getMessage());
        }
        return null; // Return null if no records found
    }

    /**
     * Closes the database connection gracefully.
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("  ✓ Database connection closed");
            }
        } catch (SQLException e) {
            System.err.println("Failed to close database connection: " + e.getMessage());
        }
    }
}

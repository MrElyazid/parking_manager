package com.smartparking.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:smartparking.db"; // Database file name

    /**
     * Establishes a connection to the SQLite database.
     *
     * @return A Connection object to the database.
     * @throws SQLException if a database access error occurs.
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Ensure the JDBC driver is loaded (often automatic with modern JDBC, but good practice)
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC Driver not found.");
            e.printStackTrace();
            throw new SQLException("SQLite JDBC Driver not found.", e);
        }
        return DriverManager.getConnection(DB_URL);
    }

    /**
     * Initializes the database by creating tables if they do not exist.
     * Should be called once when the application starts.
     */
    public static void initializeDatabase() {
        String createUserTableSQL = "CREATE TABLE IF NOT EXISTS Users ("
                + " user_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " username TEXT NOT NULL UNIQUE,"
                + " password_hash TEXT NOT NULL" // Store hashed passwords, not plain text!
                + ");";

        String createSpotTableSQL = "CREATE TABLE IF NOT EXISTS ParkingSpots ("
                + " spot_id TEXT PRIMARY KEY,"
                + " location_info TEXT,"
                + " type TEXT DEFAULT 'Standard',"
                + " hourly_rate REAL DEFAULT 2.5" // Added hourly rate column
                + ");";

        // Using DATETIME for start/end times for more precision
        // Added user_id and spot_id as foreign keys (though SQLite doesn't enforce by default)
        String createReservationTableSQL = "CREATE TABLE IF NOT EXISTS Reservations ("
                + " reservation_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " user_id INTEGER NOT NULL,"
                + " spot_id TEXT NOT NULL,"
                + " start_time DATETIME NOT NULL,"
                + " end_time DATETIME NOT NULL,"
                + " calculated_price REAL,"
                + " creation_timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + " FOREIGN KEY (user_id) REFERENCES Users(user_id),"
                + " FOREIGN KEY (spot_id) REFERENCES ParkingSpots(spot_id)"
                + ");";

        // Index for faster lookups on reservation times/spots
        String createReservationIndexSQL = "CREATE INDEX IF NOT EXISTS idx_reservation_spot_time "
                                         + "ON Reservations (spot_id, start_time, end_time);";


        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Execute table creation statements
            stmt.execute(createUserTableSQL);
            System.out.println("Users table checked/created.");

            stmt.execute(createSpotTableSQL);
            System.out.println("ParkingSpots table checked/created.");

            stmt.execute(createReservationTableSQL);
            System.out.println("Reservations table checked/created.");

            stmt.execute(createReservationIndexSQL);
            System.out.println("Reservations index checked/created.");

            // Add some default spots if the table is newly created
            addDefaultSpots(conn);

        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Helper method to add default parking spots if the table is empty
    private static void addDefaultSpots(Connection conn) {
        String checkSpotsSQL = "SELECT COUNT(*) FROM ParkingSpots;";
        // Updated SQL to include hourly_rate
        String insertSpotSQL = "INSERT INTO ParkingSpots(spot_id, location_info, type, hourly_rate) VALUES(?, ?, ?, ?);";

        try (Statement checkStmt = conn.createStatement();
             java.sql.ResultSet rs = checkStmt.executeQuery(checkSpotsSQL)) {

            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("Adding default parking spots...");
                try (java.sql.PreparedStatement insertStmt = conn.prepareStatement(insertSpotSQL)) {
                    // Spot ID, Location, Type, Hourly Rate
                    insertStmt.setString(1, "A1"); insertStmt.setString(2, "Floor 1"); insertStmt.setString(3, "Standard"); insertStmt.setDouble(4, 2.50); insertStmt.addBatch();
                    insertStmt.setString(1, "A2"); insertStmt.setString(2, "Floor 1"); insertStmt.setString(3, "Standard"); insertStmt.setDouble(4, 2.50); insertStmt.addBatch();
                    insertStmt.setString(1, "B1"); insertStmt.setString(2, "Floor 2"); insertStmt.setString(3, "EV"); insertStmt.setDouble(4, 3.50); insertStmt.addBatch(); // EV spot costs more
                    insertStmt.setString(1, "C1"); insertStmt.setString(2, "Floor 1"); insertStmt.setString(3, "Disabled"); insertStmt.setDouble(4, 2.00); insertStmt.addBatch(); // Disabled spot might be cheaper

                    insertStmt.executeBatch();
                    System.out.println("Default spots added.");
                }
            }
        } catch (SQLException e) {
             System.err.println("Error adding default spots: " + e.getMessage());
        }
    }


    public static void main(String[] args) {
        // Simple test to initialize the DB from command line if needed
        System.out.println("Initializing database...");
        initializeDatabase();
        System.out.println("Database initialization complete.");
    }
}

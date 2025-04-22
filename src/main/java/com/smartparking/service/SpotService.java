package com.smartparking.service;

import com.smartparking.model.ParkingSpot;
import com.smartparking.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SpotService {

    /**
     * Retrieves all parking spots from the database.
     *
     * @return A list of ParkingSpot objects.
     */
    public List<ParkingSpot> getAllParkingSpots() {
        List<ParkingSpot> spots = new ArrayList<>();
        // Updated SQL to select hourly_rate
        String sql = "SELECT spot_id, location_info, type, hourly_rate FROM ParkingSpots ORDER BY spot_id";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                ParkingSpot spot = new ParkingSpot(
                        rs.getString("spot_id"),
                        rs.getString("location_info"),
                        rs.getString("type"),
                        rs.getDouble("hourly_rate") // Get the hourly rate
                );
                spots.add(spot);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching parking spots: " + e.getMessage());
            e.printStackTrace();
            // Return empty list on error
        }
        return spots;
    }

    /**
     * Adds a new parking spot (primarily for admin use or initial setup).
     *
     * @param spot The ParkingSpot object to add.
     * @return true if successful, false otherwise.
     */
    public boolean addParkingSpot(ParkingSpot spot) {
         // Updated SQL to insert hourly_rate
         String sql = "INSERT INTO ParkingSpots(spot_id, location_info, type, hourly_rate) VALUES(?, ?, ?, ?)";
         try (Connection conn = DatabaseManager.getConnection();
              PreparedStatement pstmt = conn.prepareStatement(sql)) {
             pstmt.setString(1, spot.getSpotId());
             pstmt.setString(2, spot.getLocationInfo());
             pstmt.setString(3, spot.getType());
             pstmt.setDouble(4, spot.getHourlyRate()); // Set the hourly rate
             int affectedRows = pstmt.executeUpdate();
             return affectedRows > 0;
         } catch (SQLException e) {
             System.err.println("Error adding parking spot '" + spot.getSpotId() + "': " + e.getMessage());
             // Could be due to duplicate spot_id (PRIMARY KEY constraint)
             return false;
         }
    }

     /**
     * Deletes a parking spot by its ID (primarily for admin use).
     *
     * @param spotId The ID of the spot to delete.
     * @return true if successful, false otherwise.
     */
    public boolean deleteParkingSpot(String spotId) {
        String sql = "DELETE FROM ParkingSpots WHERE spot_id = ?";
         try (Connection conn = DatabaseManager.getConnection();
              PreparedStatement pstmt = conn.prepareStatement(sql)) {
             pstmt.setString(1, spotId);
             int affectedRows = pstmt.executeUpdate();
             return affectedRows > 0;
         } catch (SQLException e) {
             System.err.println("Error deleting parking spot '" + spotId + "': " + e.getMessage());
             return false;
         }
    }

    // Add methods for updating spots if needed
}

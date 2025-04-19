package com.smartparking.service;

import com.smartparking.model.Reservation;
import com.smartparking.util.DatabaseManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service to manage parking reservations using the database.
 */
public class ReservationService {

    // DateTimeFormatter to parse/format database DATETIME strings (SQLite typically stores as TEXT)
    // ISO_LOCAL_DATE_TIME format: 'YYYY-MM-DDTHH:MM:SS' - Adjust if DB stores differently
    private static final DateTimeFormatter DB_DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;


    /**
     * Checks if a specific parking spot is available during the given time range.
     *
     * @param spotId    The ID of the parking spot.
     * @param startTime The desired start time.
     * @param endTime   The desired end time.
     * @return true if the spot is available, false otherwise.
     */
    public boolean isSpotAvailable(String spotId, LocalDateTime startTime, LocalDateTime endTime) {
        // SQL to find any existing reservation that overlaps with the requested time range for the specific spot.
        // Overlap conditions:
        // - Existing starts before requested ends AND Existing ends after requested starts
        String sql = "SELECT COUNT(*) FROM Reservations WHERE spot_id = ? AND start_time < ? AND end_time > ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, spotId);
            pstmt.setString(2, endTime.format(DB_DATETIME_FORMATTER));     // Existing starts before requested ends
            pstmt.setString(3, startTime.format(DB_DATETIME_FORMATTER));   // Existing ends after requested starts

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int overlappingCount = rs.getInt(1);
                return overlappingCount == 0; // Available if no overlaps found
            }
        } catch (SQLException e) {
            System.err.println("Error checking spot availability: " + e.getMessage());
            e.printStackTrace();
        }
        // Default to unavailable on error to be safe
        return false;
    }


    /**
     * Adds a new reservation to the database.
     * Assumes availability has already been checked.
     *
     * @param reservation The Reservation object (without reservationId set).
     * @return An Optional containing the reservationId if successful, otherwise empty.
     */
    public Optional<Integer> addReservation(Reservation reservation) {
        if (reservation == null) {
            System.err.println("Attempted to add a null reservation.");
            return Optional.empty();
        }

        String sql = "INSERT INTO Reservations(user_id, spot_id, start_time, end_time, calculated_price) VALUES(?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, reservation.getUserId());
            pstmt.setString(2, reservation.getSpotId());
            pstmt.setString(3, reservation.getStartTime().format(DB_DATETIME_FORMATTER));
            pstmt.setString(4, reservation.getEndTime().format(DB_DATETIME_FORMATTER));
            pstmt.setDouble(5, reservation.getCalculatedPrice());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                // Retrieve the generated reservation_id
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int reservationId = generatedKeys.getInt(1);
                        System.out.println("Reservation added successfully with ID: " + reservationId);
                        return Optional.of(reservationId);
                    } else {
                         System.err.println("Reservation inserted, but failed to retrieve generated ID.");
                         return Optional.empty(); // Indicate failure to get ID
                    }
                }
            } else {
                 System.err.println("Reservation insert failed, no rows affected.");
                 return Optional.empty();
            }

        } catch (SQLException e) {
            System.err.println("Error adding reservation: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * Retrieves all reservations from the database.
     *
     * @return A list of all Reservation objects.
     */
    public List<Reservation> getAllReservations() {
        List<Reservation> reservations = new ArrayList<>();
        // Order by start time for better readability in admin panel
        String sql = "SELECT reservation_id, user_id, spot_id, start_time, end_time, calculated_price, creation_timestamp FROM Reservations ORDER BY start_time DESC";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                reservations.add(mapResultSetToReservation(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all reservations: " + e.getMessage());
            e.printStackTrace();
        }
        return reservations;
    }

     /**
     * Retrieves all reservations for a specific user.
     *
     * @param userId The ID of the user.
     * @return A list of Reservation objects for the given user.
     */
    public List<Reservation> getReservationsForUser(int userId) {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT reservation_id, user_id, spot_id, start_time, end_time, calculated_price, creation_timestamp "
                   + "FROM Reservations WHERE user_id = ? ORDER BY start_time DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                 reservations.add(mapResultSetToReservation(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching reservations for user " + userId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return reservations;
    }


    /**
     * Deletes a reservation by its ID.
     *
     * @param reservationId The ID of the reservation to delete.
     * @return true if a reservation was found and removed, false otherwise.
     */
     public boolean deleteReservation(int reservationId) {
         String sql = "DELETE FROM Reservations WHERE reservation_id = ?";
         try (Connection conn = DatabaseManager.getConnection();
              PreparedStatement pstmt = conn.prepareStatement(sql)) {

             pstmt.setInt(1, reservationId);
             int affectedRows = pstmt.executeUpdate();

             if (affectedRows > 0) {
                 System.out.println("Reservation deleted: " + reservationId);
                 return true;
             } else {
                 System.err.println("Reservation not found for deletion: " + reservationId);
                 return false;
             }
         } catch (SQLException e) {
             System.err.println("Error deleting reservation " + reservationId + ": " + e.getMessage());
             e.printStackTrace();
             return false;
         }
     }

     /**
      * Helper method to map a ResultSet row to a Reservation object.
      * Handles potential nulls and date/time parsing.
      */
     private Reservation mapResultSetToReservation(ResultSet rs) throws SQLException {
         return new Reservation(
                 rs.getInt("reservation_id"),
                 rs.getInt("user_id"),
                 rs.getString("spot_id"),
                 parseDateTime(rs.getString("start_time")),
                 parseDateTime(rs.getString("end_time")),
                 rs.getDouble("calculated_price"),
                 parseDateTime(rs.getString("creation_timestamp"))
         );
     }

     /**
      * Helper to parse DATETIME strings from DB, returning null if invalid.
      */
     private LocalDateTime parseDateTime(String dateTimeString) {
         if (dateTimeString == null) return null;
         try {
             // SQLite might store with space instead of 'T'
             String parsableString = dateTimeString.replace(" ", "T");
             return LocalDateTime.parse(parsableString, DB_DATETIME_FORMATTER);
         } catch (Exception e) {
             System.err.println("Error parsing date/time string: " + dateTimeString + " - " + e.getMessage());
             return null; // Return null if parsing fails
         }
     }

    // --- Old In-Memory Methods (Commented out or removed) ---
    /*
    private static final List<Reservation> reservations = new ArrayList<>();
    public boolean addReservation(Reservation reservation) { ... }
    public List<Reservation> getAllReservations() { ... }
    public Optional<Reservation> findReservationById(String reservationId) { ... }
    public boolean deleteReservation(String reservationId) { ... }
    */
}

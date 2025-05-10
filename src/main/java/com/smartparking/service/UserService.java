package com.smartparking.service;

import com.smartparking.model.User;
import com.smartparking.util.DatabaseManager;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Optional;
import java.util.List; // Import List

public class UserService {

    // Simple salt for demonstration - INSECURE! Use per-user salts in production.
    private static final String DEMO_SALT = "SmartParkingDemoSalt";

    /**
     * Registers a new user in the database.
     * Hashes the password before storing.
     *
     * @param username The desired username.
     * @param password The plain text password.
     * @return true if registration is successful, false otherwise (e.g., username exists).
     */
    public boolean registerUser(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            System.err.println("Username or password cannot be empty.");
            return false;
        }

        // Check if username already exists
        if (findUserByUsername(username).isPresent()) {
             System.err.println("Username '" + username + "' already exists.");
             return false;
        }

        String hashedPassword = hashPassword(password);
        if (hashedPassword == null) {
            System.err.println("Password hashing failed.");
            return false;
        }

        String sql = "INSERT INTO Users(username, password_hash) VALUES(?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error registering user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Authenticates a user based on username and password.
     *
     * @param username The username to authenticate.
     * @param password The plain text password to verify.
     * @return An Optional containing the User object if authentication is successful, otherwise empty.
     */
    public Optional<User> authenticateUser(String username, String password) {
        String sql = "SELECT user_id, password_hash FROM Users WHERE username = ?";
        String providedHashedPassword = hashPassword(password);

        if (providedHashedPassword == null) {
             System.err.println("Password hashing failed during authentication.");
             return Optional.empty();
        }

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                int userId = rs.getInt("user_id");

                // Compare the hash of the provided password with the stored hash
                if (providedHashedPassword.equals(storedHash)) {
                    // Authentication successful - return User object without hash
                    return Optional.of(new User(userId, username));
                } else {
                    // Password mismatch
                    System.err.println("Password mismatch for user: " + username);
                    return Optional.empty();
                }
            } else {
                // User not found
                 System.err.println("User not found: " + username);
                return Optional.empty();
            }

        } catch (SQLException e) {
            System.err.println("Error authenticating user: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

     /**
     * Finds a user by username (primarily for checking existence).
     *
     * @param username The username to search for.
     * @return An Optional containing the User object if found, otherwise empty.
     */
    public Optional<User> findUserByUsername(String username) {
        String sql = "SELECT user_id FROM Users WHERE username = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("user_id");
                return Optional.of(new User(userId, username)); // Return user without hash
            } else {
                return Optional.empty(); // User not found
            }

        } catch (SQLException e) {
            System.err.println("Error finding user by username: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * Finds a user by their user ID.
     *
     * @param userId The ID of the user to find.
     * @return The User object if found, otherwise null.
     */
    public User getUserById(int userId) {
        String sql = "SELECT username FROM Users WHERE user_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String username = rs.getString("username");
                return new User(userId, username); // Return user without hash
            } else {
                return null; // User not found
            }

        } catch (SQLException e) {
            System.err.println("Error finding user by ID: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Retrieves all users from the database.
     *
     * @return A list of all User objects.
     */
    public List<User> getAllUsers() {
        List<User> users = new java.util.ArrayList<>();
        String sql = "SELECT user_id, username FROM Users";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int userId = rs.getInt("user_id");
                String username = rs.getString("username");
                users.add(new User(userId, username));
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving all users: " + e.getMessage());
            e.printStackTrace();
        }
        return users;
    }


    /**
     * Simple password hashing function (SHA-256 with a static salt).
     * WARNING: This is for demonstration only. Use a proper library like BCrypt
     * with per-user salts and adaptive hashing in production.
     *
     * @param password The plain text password.
     * @return Base64 encoded hash string, or null on error.
     */
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            // Combine password and salt before hashing
            String saltedPassword = password + DEMO_SALT;
            byte[] hashedBytes = md.digest(saltedPassword.getBytes());
            // Encode to Base64 for easier storage
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            System.err.println("SHA-256 Algorithm not found: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}

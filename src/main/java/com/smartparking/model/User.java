package com.smartparking.model;

public class User {

    private int userId;
    private String username;
    // We don't store the password hash in the model object after login for security.
    // The hash is only used during authentication by the service layer.

    // Constructor used when retrieving user data (e.g., after login)
    public User(int userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    // Getters
    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    // No setters needed if user details are immutable after creation/retrieval

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                '}';
    }

    // Optional: equals() and hashCode() if needed
}

package com.smartparking.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Reservation {

    private int reservationId; // Changed to int to match AUTOINCREMENT
    private int userId;
    private String spotId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double calculatedPrice;
    private LocalDateTime creationTimestamp;

    // Formatter for display purposes
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");


    // Constructor for creating a new reservation before saving to DB
    public Reservation(int userId, String spotId, LocalDateTime startTime, LocalDateTime endTime, double calculatedPrice) {
        // reservationId will be set by the database on insert
        this.userId = userId;
        this.spotId = spotId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.calculatedPrice = calculatedPrice;
        // creationTimestamp will be set by the database default or on retrieval
    }

    // Constructor for loading a reservation from the DB (includes all fields)
    public Reservation(int reservationId, int userId, String spotId, LocalDateTime startTime, LocalDateTime endTime, double calculatedPrice, LocalDateTime creationTimestamp) {
        this.reservationId = reservationId;
        this.userId = userId;
        this.spotId = spotId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.calculatedPrice = calculatedPrice;
        this.creationTimestamp = creationTimestamp;
    }


    // --- Getters ---
    public int getReservationId() {
        return reservationId;
    }

     public int getUserId() {
        return userId;
    }

    public String getSpotId() {
        return spotId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

     // Formatted getter for display
    public String getFormattedStartTime() {
        return startTime != null ? startTime.format(DATETIME_FORMATTER) : "N/A";
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

     // Formatted getter for display
    public String getFormattedEndTime() {
        return endTime != null ? endTime.format(DATETIME_FORMATTER) : "N/A";
    }

     public double getCalculatedPrice() {
        return calculatedPrice;
    }

    public LocalDateTime getCreationTimestamp() {
        return creationTimestamp;
    }

     // Formatted getter for display
    public String getFormattedCreationTimestamp() {
        return creationTimestamp != null ? creationTimestamp.format(DATETIME_FORMATTER) : "N/A";
    }

    // --- Setters (Optional - if needed for updates) ---
    // public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    // public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    // public void setCalculatedPrice(double calculatedPrice) { this.calculatedPrice = calculatedPrice; }


    @Override
    public String toString() {
        return "Reservation{" +
                "reservationId=" + reservationId +
                ", userId=" + userId +
                ", spotId='" + spotId + '\'' +
                ", startTime=" + getFormattedStartTime() +
                ", endTime=" + getFormattedEndTime() +
                ", calculatedPrice=" + calculatedPrice +
                ", creationTimestamp=" + getFormattedCreationTimestamp() +
                '}';
    }

    // Optional: equals() and hashCode() based on reservationId
     @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        // Only compare if ID is non-zero (assigned by DB)
        return reservationId != 0 && reservationId == that.reservationId;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(reservationId); // Use ID for hashcode if assigned
    }
}

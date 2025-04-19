package com.smartparking.model;

public class ParkingSpot {

    private String spotId;        // e.g., "A1", "B5"
    private String locationInfo;  // e.g., "Floor 1, Near Entrance"
    private String type;          // e.g., Standard, EV, Disabled

    // Constructor
    public ParkingSpot(String spotId, String locationInfo, String type) {
        this.spotId = spotId;
        this.locationInfo = locationInfo;
        this.type = type;
    }

    // Getters
    public String getSpotId() {
        return spotId;
    }

    public String getLocationInfo() {
        return locationInfo;
    }

    public String getType() {
        return type;
    }

    // No setters needed if spots are defined in DB and treated as immutable here

    @Override
    public String toString() {
        return "ParkingSpot{" +
                "spotId='" + spotId + '\'' +
                ", locationInfo='" + locationInfo + '\'' +
                ", type='" + type + '\'' +
                '}';
    }

    // Optional: equals() and hashCode() based on spotId
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParkingSpot that = (ParkingSpot) o;
        return java.util.Objects.equals(spotId, that.spotId);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(spotId);
    }
}

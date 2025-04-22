package com.smartparking.controller;

import com.smartparking.model.ParkingSpot;
import com.smartparking.model.Reservation;
import com.smartparking.model.User;
import com.smartparking.service.ReservationService;
import com.smartparking.service.SpotService;

import javafx.fxml.FXML;
import javafx.collections.FXCollections; // For ComboBox items
import javafx.collections.ObservableList; // For ComboBox items
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.event.ActionEvent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.Duration; // For price calculation
import java.util.List;
import java.util.Optional;
import java.util.HashMap; // To map buttons to spots
import java.util.Map;

public class ParkingLotController {

    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> startHourCombo;
    @FXML private ComboBox<String> startMinuteCombo;
    @FXML private ComboBox<String> endHourCombo;
    @FXML private ComboBox<String> endMinuteCombo;
    @FXML private Label availabilityMessageLabel;
    @FXML private TilePane parkingGrid;
    @FXML private Label selectedSpotLabel;
    @FXML private Button confirmReservationButton;

    private final SpotService spotService = new SpotService();
    private final ReservationService reservationService = new ReservationService();
    private MainController mainController; // To navigate back or show alerts centrally
    private User currentUser;

    private ParkingSpot selectedSpot = null;
    private LocalDateTime selectedStartTime = null;
    private LocalDateTime selectedEndTime = null;
    private Button selectedSpotButton = null; // Keep track of the selected button

    private final Map<Button, ParkingSpot> buttonSpotMap = new HashMap<>();

    // Time formatter for display/parsing if needed elsewhere, but primarily using combo values now
    // private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    // private static final double HOURLY_RATE = 2.5; // Removed fixed rate

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @FXML
    private void initialize() {
        availabilityMessageLabel.setVisible(false); // Hide initially
        availabilityMessageLabel.setManaged(false);
        selectedSpotLabel.setText("Selected Spot: None");
        confirmReservationButton.setDisable(true);
        // Set default date to today
        datePicker.setValue(LocalDate.now());

        // Populate time ComboBoxes
        populateTimeCombos();
    }

    private void populateTimeCombos() {
        ObservableList<String> hours = FXCollections.observableArrayList();
        for (int i = 0; i < 24; i++) {
            hours.add(String.format("%02d", i)); // Format as 00, 01, ..., 23
        }
        startHourCombo.setItems(hours);
        endHourCombo.setItems(hours);

        ObservableList<String> minutes = FXCollections.observableArrayList("00", "15", "30", "45"); // 15-min increments
        // Or for every minute:
        // ObservableList<String> minutes = FXCollections.observableArrayList();
        // for (int i = 0; i < 60; i++) {
        //     minutes.add(String.format("%02d", i));
        // }
        startMinuteCombo.setItems(minutes);
        endMinuteCombo.setItems(minutes);
    }

    @FXML
    private void handleDateTimeChange(ActionEvent event) {
        // Clear the grid and selection when date/time changes, forcing re-check
        clearParkingGridAndSelection();
        availabilityMessageLabel.setText("Please click 'Check Availability'.");
    }

    @FXML
    private void handleCheckAvailability() {
        LocalDate date = datePicker.getValue();
        String startHour = startHourCombo.getValue();
        String startMinute = startMinuteCombo.getValue();
        String endHour = endHourCombo.getValue();
        String endMinute = endMinuteCombo.getValue();

        // --- Input Validation ---
        if (date == null || startHour == null || startMinute == null || endHour == null || endMinute == null) {
            showAlert(Alert.AlertType.WARNING, "Input Error", "Please select date, start time, and end time.");
            return;
        }

        LocalTime startTime;
        LocalTime endTime;
        try {
            startTime = LocalTime.of(Integer.parseInt(startHour), Integer.parseInt(startMinute));
            endTime = LocalTime.of(Integer.parseInt(endHour), Integer.parseInt(endMinute));
        } catch (NumberFormatException e) {
             showAlert(Alert.AlertType.ERROR, "Internal Error", "Invalid time values selected.");
             return;
        }

        if (!endTime.isAfter(startTime)) {
            showAlert(Alert.AlertType.WARNING, "Input Error", "End time must be after start time.");
            return;
        }

        selectedStartTime = LocalDateTime.of(date, startTime);
        selectedEndTime = LocalDateTime.of(date, endTime);

        if (selectedStartTime.isBefore(LocalDateTime.now())) {
             showAlert(Alert.AlertType.WARNING, "Input Error", "Reservation start time cannot be in the past.");
             return;
        }

        // --- Populate Grid ---
        populateParkingGrid(selectedStartTime, selectedEndTime);
        availabilityMessageLabel.setText("Checking... Select an available spot (Green).");
        availabilityMessageLabel.setVisible(true);
        availabilityMessageLabel.setManaged(true);
    }

    private void populateParkingGrid(LocalDateTime start, LocalDateTime end) {
        clearParkingGridAndSelection();
        buttonSpotMap.clear(); // Clear the map

        List<ParkingSpot> allSpots = spotService.getAllParkingSpots();
        if (allSpots.isEmpty()) {
            availabilityMessageLabel.setText("No parking spots defined in the system.");
            return;
        }

        // TODO: Optimization - Fetch only relevant reservations for the day instead of checking one by one?
        // For now, we check availability for each spot individually.

        for (ParkingSpot spot : allSpots) {
            Button spotButton = new Button(spot.getSpotId());
            spotButton.setPrefSize(60, 40);
            spotButton.getStyleClass().add("spot-button");

            // Add Tooltip
            String tooltipText = String.format("Type: %s\nRate: $%.2f/hr", spot.getType(), spot.getHourlyRate());
            Tooltip tooltip = new Tooltip(tooltipText);
            spotButton.setTooltip(tooltip);

            // Add type-specific style class
            switch (spot.getType().toLowerCase()) {
                case "ev":
                    spotButton.getStyleClass().add("spot-button-ev");
                    break;
                case "disabled":
                    spotButton.getStyleClass().add("spot-button-disabled");
                    break;
                default: // Standard or other types
                    spotButton.getStyleClass().add("spot-button-standard");
                    break;
            }


            boolean isAvailable = reservationService.isSpotAvailable(spot.getSpotId(), start, end);

            if (isAvailable) {
                spotButton.getStyleClass().add("spot-button-available");
                spotButton.setOnAction(e -> handleSpotSelection(spotButton, spot));
                buttonSpotMap.put(spotButton, spot);
            } else {
                spotButton.getStyleClass().add("spot-button-reserved");
                spotButton.setDisable(true);
            }
            parkingGrid.getChildren().add(spotButton);
        }
    }

    private void handleSpotSelection(Button button, ParkingSpot spot) {
        // Deselect previous button if any
        if (selectedSpotButton != null) {
            selectedSpotButton.getStyleClass().remove("spot-button-selected");
            // Re-apply available style if it wasn't the current button
             if (selectedSpotButton != button) {
                 selectedSpotButton.getStyleClass().add("spot-button-available");
             }
        }

        // Select new button
        selectedSpot = spot;
        selectedSpotButton = button;
        selectedSpotButton.getStyleClass().remove("spot-button-available"); // Remove available class
        selectedSpotButton.getStyleClass().add("spot-button-selected"); // Add selected class

        selectedSpotLabel.setText("Selected Spot: " + spot.getSpotId());
        confirmReservationButton.setDisable(false); // Enable confirmation button
    }

    @FXML
    private void handleConfirmReservation() {
        if (currentUser == null) {
             showAlert(Alert.AlertType.ERROR, "Error", "No user logged in.");
             // Optionally navigate back to login via mainController
             return;
        }
        if (selectedSpot == null || selectedStartTime == null || selectedEndTime == null) {
             showAlert(Alert.AlertType.ERROR, "Error", "No spot selected or time range invalid.");
             return;
        }

        // Double-check availability right before confirming
         if (!reservationService.isSpotAvailable(selectedSpot.getSpotId(), selectedStartTime, selectedEndTime)) {
             showAlert(Alert.AlertType.WARNING, "Spot Taken", "Sorry, spot " + selectedSpot.getSpotId() + " was just reserved. Please select another spot or time.");
             // Refresh the grid to show the updated status
             populateParkingGrid(selectedStartTime, selectedEndTime);
             return;
         }


        // Calculate price using the selected spot's hourly rate
        long durationMinutes = Duration.between(selectedStartTime, selectedEndTime).toMinutes();
        double hours = durationMinutes / 60.0;
        // Use selectedSpot.getHourlyRate()
        double price = Math.max(1.0, hours * selectedSpot.getHourlyRate()); // Use spot-specific rate, ensure minimum charge

        Reservation newReservation = new Reservation(
                currentUser.getUserId(),
                selectedSpot.getSpotId(),
                selectedStartTime,
                selectedEndTime,
                price
        );

        Optional<Integer> reservationIdOpt = reservationService.addReservation(newReservation);

        if (reservationIdOpt.isPresent()) {
            showAlert(Alert.AlertType.INFORMATION, "Reservation Confirmed",
                    String.format("Reservation successful!\nUser: %s\nSpot: %s\nFrom: %s\nTo: %s\nPrice: $%.2f\nID: %d",
                            currentUser.getUsername(),
                            selectedSpot.getSpotId(),
                            selectedStartTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                            selectedEndTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                            price,
                            reservationIdOpt.get()));
            // Clear selection and refresh grid
            populateParkingGrid(selectedStartTime, selectedEndTime); // Refresh grid showing the new reservation
        } else {
             showAlert(Alert.AlertType.ERROR, "Reservation Failed", "Could not save your reservation. Please try again.");
        }
    }

    private void clearParkingGridAndSelection() {
        parkingGrid.getChildren().clear(); // Remove all spot buttons
        // Clear only the spot selection, not the time range which is still valid for the current check
        selectedSpot = null;
        selectedSpotButton = null;
        // selectedStartTime = null; // DO NOT RESET TIME HERE
        // selectedEndTime = null;   // DO NOT RESET TIME HERE
        selectedSpotLabel.setText("Selected Spot: None");
        confirmReservationButton.setDisable(true);
        // Keep the availability message or clear it as needed, maybe clear here:
        // availabilityMessageLabel.setText("");
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        // Use MainController's alert if available for consistency, otherwise show local
        if (mainController != null) {
            mainController.showAlert(alertType, title, message);
        } else {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }
    }
}

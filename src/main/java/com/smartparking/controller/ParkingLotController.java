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
import javafx.fxml.FXMLLoader; // Added for SpotCard.fxml
import javafx.scene.Node; // Added for spot card node
import javafx.scene.control.*;
import javafx.scene.layout.GridPane; // Changed from TilePane
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.event.ActionEvent;

import java.io.IOException; // Added for FXMLLoader
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration; // For price calculation
import java.util.List;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;
import com.smartparking.util.DatabaseManager;

public class ParkingLotController {

    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> startHourCombo;
    @FXML private ComboBox<String> startMinuteCombo;
    @FXML private ComboBox<String> endHourCombo;
    @FXML private ComboBox<String> endMinuteCombo;
    @FXML private Label availabilityMessageLabel;
    @FXML private GridPane parkingGrid; // Changed from TilePane
    @FXML private Label selectedSpotLabel;
    @FXML private HBox floorSelectionBox;

    private ToggleGroup floorToggleGroup = new ToggleGroup();
    private String selectedFloor = null;

    @FXML private Button confirmReservationButton;

    private final SpotService spotService = new SpotService();
    private final ReservationService reservationService = new ReservationService();
    private MainController mainController; // To navigate back or show alerts centrally
    private User currentUser;

    private ParkingSpot selectedSpot = null;
    private LocalDateTime selectedStartTime = null;
    private LocalDateTime selectedEndTime = null;
    private Node selectedSpotCardNode = null; // Changed from Button to Node (root of SpotCard)

    private final Map<Node, SpotCardController> spotNodeMap = new HashMap<>(); // Maps SpotCard root node to its controller

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

    // this method loads the floors :
    @FXML
    private void loadFloorsFromDatabase() {
        floorSelectionBox.getChildren().clear();
        Label label = new Label("Select Floor:");
        floorSelectionBox.getChildren().add(label);
    
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT floor FROM floors ORDER BY floor");
             ResultSet rs = stmt.executeQuery()) {
    
            while (rs.next()) {
                String floor = rs.getString("floor");
                RadioButton floorRadio = new RadioButton(floor);
                floorRadio.setToggleGroup(floorToggleGroup);
                floorRadio.setUserData(floor);
                floorRadio.setOnAction(e -> {
                    selectedFloor = (String) floorRadio.getUserData();
                    clearParkingGridAndSelection();
                    List<ParkingSpot> allSpots = spotService.getAllParkingSpots();
                
                    populateParkingGrid(selectedStartTime, selectedEndTime);
                    
                    });
                floorSelectionBox.getChildren().add(floorRadio);
            }
            

        System.out.println("floorRadioBox is: " + floorSelectionBox);
        System.out.println("Children in floorRadioBox: " + floorSelectionBox.getChildren().size());
    
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Unable to load floors: " + e.getMessage());
        }
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

        // Show floors
        loadFloorsFromDatabase();

        // --- Populate Grid ---
        populateParkingGrid(selectedStartTime, selectedEndTime);

    }

    private void populateParkingGrid(LocalDateTime start, LocalDateTime end) {
        clearParkingGridAndSelection();
        spotNodeMap.clear(); // Clear the map

        List<ParkingSpot> allSpots = spotService.getAllParkingSpots();
        availabilityMessageLabel.setText("Select an available spot."); // Updated message
        availabilityMessageLabel.setVisible(true);
        availabilityMessageLabel.setManaged(true);

        if (allSpots.isEmpty()) {
            availabilityMessageLabel.setText("No parking spots defined in the system.");
            return;
        }

        if (selectedFloor != null) {
            allSpots.removeIf(spot -> !selectedFloor.equals(spot.getLocationInfo()));
            if (allSpots.isEmpty()) {
                availabilityMessageLabel.setText("No parking spots available for this floor: " + selectedFloor);
                return;
            }
        }
        
        // Max 5 rows (A-E), 10 columns (01-10)
        // The GridPane will create cells as needed. If a spot "B05" is added,
        // it will create row 'B' (index 1) and column '05' (index 4).

        for (ParkingSpot spot : allSpots) {
            try {
                String spotId = spot.getSpotId();
                if (spotId == null || spotId.length() < 2) {
                    System.err.println("Invalid spot ID format: " + spotId + " for spot (location: " + spot.getLocationInfo() + ")");
                    continue;
                }

                char rowChar = Character.toUpperCase(spotId.charAt(0));
                int rowIndex = rowChar - 'A'; // 'A' -> 0, 'B' -> 1, ..., 'E' -> 4

                if (rowIndex < 0 || rowIndex >= 5) { // Max 5 rows (A-E)
                    System.err.println("Invalid row character '" + rowChar + "' (index " + rowIndex + ") in spot ID: " + spotId);
                    continue;
                }

                int columnIndex;
                try {
                    columnIndex = Integer.parseInt(spotId.substring(1)) - 1; // "01" -> 0, "10" -> 9
                } catch (NumberFormatException nfe) {
                    System.err.println("Invalid column number in spot ID: " + spotId + " - " + nfe.getMessage());
                    continue;
                }

                if (columnIndex < 0 || columnIndex >= 10) { // Max 10 columns (01-10 means indices 0-9)
                    System.err.println("Column index " + columnIndex + " out of bounds (0-9) for spot ID: " + spotId);
                    continue;
                }

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/smartparking/view/SpotCard.fxml"));
                VBox spotCardNode = loader.load();
                SpotCardController spotCardController = loader.getController();
                spotCardController.setData(spot);

                boolean isAvailable = reservationService.isSpotAvailable(spot.getSpotId(), start, end);

                if (isAvailable) {
                    spotCardNode.getStyleClass().add("spot-card-available");
                    spotCardNode.setOnMouseClicked(e -> {
                        handleSpotSelection(spotCardNode, spotCardController);
                    });
                    spotNodeMap.put(spotCardNode, spotCardController);
                } else {
                    spotCardNode.getStyleClass().add("spot-card-reserved");
                    spotCardNode.setDisable(true);
                }
                
                parkingGrid.add(spotCardNode, columnIndex, rowIndex);

            } catch (IOException e) {
                System.err.println("Error loading SpotCard.fxml for spot " + spot.getSpotId() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // showSpotDetailsDialog method is removed as info is on the card.

    private void handleSpotSelection(Node cardNode, SpotCardController cardController) {
        // Deselect previous card node if any
        if (selectedSpotCardNode != null) {
            selectedSpotCardNode.getStyleClass().remove("spot-card-selected");
            // Re-apply available style if it was available and not the current card
            if (selectedSpotCardNode != cardNode && spotNodeMap.containsKey(selectedSpotCardNode)) {
                 // Check if it's supposed to be available (not strictly necessary if styles are managed well)
                 // For simplicity, we assume if it's not selected, and was selectable, it's available.
                 // A more robust way would be to re-check its actual availability or store its original state.
                 // For now, if it's in spotNodeMap, it was interactive.
                selectedSpotCardNode.getStyleClass().add("spot-card-available");
            }
        }

        // Select new card node
        selectedSpot = cardController.getParkingSpot();
        selectedSpotCardNode = cardNode;
        selectedSpotCardNode.getStyleClass().remove("spot-card-available"); // Remove available class if present
        selectedSpotCardNode.getStyleClass().add("spot-card-selected");   // Add selected class

        selectedSpotLabel.setText("Selected Spot: " + selectedSpot.getSpotId());
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
                    String.format("Reservation successful!\nUser: %s\nSpot: %s\nFrom: %s\nTo: %s\nPrice: $%.2f\n",
                            currentUser.getUsername(),
                            selectedSpot.getSpotId(),
                            selectedStartTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                            selectedEndTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                            price));
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
        selectedSpotCardNode = null; // Changed from selectedSpotButton
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

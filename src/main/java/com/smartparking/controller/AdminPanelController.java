
package com.smartparking.controller;

import com.smartparking.model.Reservation;
import com.smartparking.model.User; // Import User model
import com.smartparking.model.ParkingSpot; // Import ParkingSpot model
import com.smartparking.service.ReservationService;
import com.smartparking.service.UserService; // Import UserService
import com.smartparking.service.SpotService; // Import SpotService
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableCell; // For custom cell formatting
// Chart imports definitively removed
import javafx.scene.control.TabPane; // Import TabPane
import javafx.scene.control.Tab; // Import Tab
import javafx.scene.control.Label; // Import Label
import javafx.scene.control.Dialog; // Import Dialog
import javafx.scene.control.DialogPane; // Import DialogPane
import javafx.scene.control.TextField; // Import TextField
import javafx.fxml.FXMLLoader; // Import FXMLLoader
import javafx.scene.layout.GridPane; // Import GridPane
import javafx.stage.Stage; // Import Stage
import javafx.scene.Parent; // Import Parent
import javafx.scene.Scene; // Import Scene
import java.io.IOException; // Import IOException

import java.text.NumberFormat; // For currency formatting
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Map; // Import Map
import java.util.TreeMap; // Import TreeMap for sorted data
import java.util.HashMap; // Import HashMap
import java.util.Comparator; // Import Comparator
import java.util.stream.Collectors; // Import Collectors

public class AdminPanelController {

    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML private TableView<Reservation> reservationsTable;

    // Inject columns to apply custom cell factories if needed
    @FXML private TableColumn<Reservation, Integer> reservationIdColumn;
    @FXML private TableColumn<Reservation, Integer> userIdColumn; // This column will now display Username
    @FXML private TableColumn<Reservation, String> spotIdColumn;
    @FXML private TableColumn<Reservation, String> startTimeColumn; // Use String for formatted value
    @FXML private TableColumn<Reservation, String> endTimeColumn;   // Use String for formatted value
    @FXML private TableColumn<Reservation, Double> priceColumn;
    @FXML private TableColumn<Reservation, String> createdTimestampColumn; // Use String for formatted value

    @FXML private TabPane adminTabPane; // Inject the TabPane

    // FXML elements for the User Inspection tab
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Integer> userIdColumnUserTab;
    @FXML private TableColumn<User, String> usernameColumnUserTab;
    @FXML private Label selectedUsernameLabel;
    @FXML private Label totalReservationsLabel;
    @FXML private Label totalPriceLabel; // Inject the new label
    @FXML private TableView<Reservation> userReservationsTable;

    // FXML elements for the Parking Spots tab
    @FXML private TableView<ParkingSpot> parkingSpotsTable;
    @FXML private TableColumn<ParkingSpot, String> spotIdColumnSpotsTab;
    @FXML private TableColumn<ParkingSpot, String> locationInfoColumnSpotsTab;
    @FXML private TableColumn<ParkingSpot, String> typeColumnSpotsTab;
    @FXML private TableColumn<ParkingSpot, Double> hourlyRateColumnSpotsTab;

    // FXML elements for the Statistics tab
    @FXML private Label statsTotalRevenueLabel;
    @FXML private Label statsAvgDurationLabel;
    @FXML private Label statsTotalSpotsLabel;
    @FXML private Label statsCurrentOccupancyLabel;
    @FXML private Label statsPopularSpotLabel;
    @FXML private Label statsPeakHourLabel;


    private final ReservationService reservationService = new ReservationService();
    private final UserService userService = new UserService(); // Instantiate UserService
    private final SpotService spotService = new SpotService(); // Instantiate SpotService
    private ObservableList<Reservation> reservationData = FXCollections.observableArrayList();
    private ObservableList<User> userData = FXCollections.observableArrayList(); // ObservableList for users
    private ObservableList<Reservation> userReservationData = FXCollections.observableArrayList(); // ObservableList for user reservations
    private ObservableList<ParkingSpot> parkingSpotData = FXCollections.observableArrayList(); // ObservableList for parking spots

    // Formatter for currency
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(); // Uses default locale

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
        System.out.println("AdminPanelController initialized.");

        // Configure Table Columns to use PropertyValueFactory (can also be done in FXML)
        // Ensure the property names match the getter methods in the Reservation class
        reservationIdColumn.setCellValueFactory(new PropertyValueFactory<>("reservationId"));
        // spotIdColumn.setCellValueFactory(new PropertyValueFactory<>("spotId")); // This will be set in the custom cell factory below
        spotIdColumn.setCellValueFactory(new PropertyValueFactory<>("spotId"));
        startTimeColumn.setCellValueFactory(new PropertyValueFactory<>("formattedStartTime")); // Use formatted getter
        endTimeColumn.setCellValueFactory(new PropertyValueFactory<>("formattedEndTime"));     // Use formatted getter
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("calculatedPrice"));
        createdTimestampColumn.setCellValueFactory(new PropertyValueFactory<>("formattedCreationTimestamp")); // Use formatted getter

        // --- Optional: Add Custom Cell Factories for Formatting ---

        // --- Optional: Add Custom Cell Factories for Formatting ---

        // User ID/Username column formatter
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId")); // Ensure userId is passed to the cell factory
        userIdColumn.setCellFactory(tc -> new TableCell<Reservation, Integer>() {
            @Override
            protected void updateItem(Integer userId, boolean empty) {
                super.updateItem(userId, empty);
                if (empty || userId == null) {
                    setText(null);
                } else {
                    // Fetch the user and display the username
                    User user = userService.getUserById(userId); // Assuming getUserById exists in UserService
                    setText(user != null ? user.getUsername() : "Unknown User");
                }
            }
        });

        // Price column formatter
        priceColumn.setCellFactory(tc -> new TableCell<Reservation, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                        setText(CURRENCY_FORMAT.format(price));
                }
            }
        });

        // You could add similar formatters for date/time columns if the formatted getters aren't sufficient

        // Load initial data
        loadReservationData();

        // Set the items for the table
        reservationsTable.setItems(reservationData);

        // Add listener for selection changes (optional)
        reservationsTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showReservationDetails(newValue)); // Example listener

        // Add listener to TabPane to load data when tabs are selected
        adminTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (newTab != null) {
                switch (newTab.getText()) {
                    case "Reservations":
                        loadReservationData();
                        break;
                    // Timeline case definitively removed
                    case "User Inspection":
                        loadUserData(); // Load users when User Inspection tab is selected
                        break;
                    case "Parking Spots":
                        loadParkingSpotsData(); // Load parking spots when Parking Spots tab is selected
                        break;
                    case "Statistics":
                        loadStatisticsData(); // Load statistics when Statistics tab is selected
                        break;
                }
            }
        });

        // Configure Users Table Columns
        userIdColumnUserTab.setCellValueFactory(new PropertyValueFactory<>("userId"));
        usernameColumnUserTab.setCellValueFactory(new PropertyValueFactory<>("username"));

        // Set items for the users table
        usersTable.setItems(userData);

        // Add listener for user selection changes
        usersTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showUserDetails(newValue)); // Example listener

        // Configure User Reservations Table Columns (similar to main reservations table)
        // Note: These columns are defined directly in FXML, so we don't need @FXML injection for them
        // We just need to set the items for the table.
        userReservationsTable.setItems(userReservationData);

        // Configure Parking Spots Table Columns
        spotIdColumnSpotsTab.setCellValueFactory(new PropertyValueFactory<>("spotId"));
        locationInfoColumnSpotsTab.setCellValueFactory(new PropertyValueFactory<>("locationInfo"));
        typeColumnSpotsTab.setCellValueFactory(new PropertyValueFactory<>("type"));
        hourlyRateColumnSpotsTab.setCellValueFactory(new PropertyValueFactory<>("hourlyRate"));
        // Custom formatter for hourly rate in parking spots table
        hourlyRateColumnSpotsTab.setCellFactory(tc -> new TableCell<ParkingSpot, Double>() {
            @Override
            protected void updateItem(Double rate, boolean empty) {
                super.updateItem(rate, empty);
                if (empty || rate == null) {
                    setText(null);
                } else {
                    setText(CURRENCY_FORMAT.format(rate));
                }
            }
        });


        // Set items for the parking spots table
        parkingSpotsTable.setItems(parkingSpotData);
    }

    /**
     * Loads reservation data from the service into the observable list.
     */
    private void loadReservationData() {
        List<Reservation> currentReservations = reservationService.getAllReservations();
        reservationData.setAll(currentReservations); // Clears existing and adds all new
        System.out.println("Loaded " + reservationData.size() + " reservations into table.");
    }

    /**
     * Loads user data from the service into the observable list for the users table.
     */
    private void loadUserData() {
        System.out.println("Loading user data...");
        List<User> allUsers = userService.getAllUsers();
        userData.setAll(allUsers); // Clears existing and adds all new
        System.out.println("Loaded " + userData.size() + " users into table.");
    }

    /**
     * Shows details and reservation history for the selected user.
     * @param user The selected user, or null if selection cleared.
     */
    private void showUserDetails(User user) {
        if (user != null) {
            System.out.println("Selected User: " + user.getUsername());
            selectedUsernameLabel.setText("Selected User: " + user.getUsername());

            List<Reservation> userReservations = reservationService.getReservationsForUser(user.getUserId());
            userReservationData.setAll(userReservations); // Update user reservations table

            // Calculate statistics
            totalReservationsLabel.setText("Total Reservations: " + userReservations.size());

            double totalRevenue = userReservations.stream()
                                                .mapToDouble(Reservation::getCalculatedPrice)
                                                .sum();
            totalPriceLabel.setText("Total Spent: " + CURRENCY_FORMAT.format(totalRevenue));
            // Add more statistics calculations here

        } else {
            System.out.println("User selection cleared.");
            selectedUsernameLabel.setText("Selected User: None");
            totalReservationsLabel.setText("Total Reservations: 0");
            totalPriceLabel.setText("Total Spent: $0.00"); // Reset total price label
            userReservationData.clear(); // Clear user reservations table
        }
    }

    // loadTimelineData method definitively removed

    /**
     * Loads parking spot data from the service into the observable list for the parking spots table.
     */
    private void loadParkingSpotsData() {
        System.out.println("Loading parking spot data...");
        List<ParkingSpot> allSpots = spotService.getAllParkingSpots();
        parkingSpotData.setAll(allSpots);
        System.out.println("Loaded " + parkingSpotData.size() + " parking spots into table.");
    }

    /**
     * Loads and displays parking statistics.
     */
    private void loadStatisticsData() {
        System.out.println("Loading statistics data...");
        List<Reservation> allReservations = reservationService.getAllReservations();
        List<ParkingSpot> allSpots = spotService.getAllParkingSpots();

        // Total Revenue
        double totalRevenue = allReservations.stream().mapToDouble(Reservation::getCalculatedPrice).sum();
        statsTotalRevenueLabel.setText(CURRENCY_FORMAT.format(totalRevenue));

        // Average Reservation Duration
        if (!allReservations.isEmpty()) {
            long totalDurationMinutes = allReservations.stream()
                .mapToLong(r -> java.time.Duration.between(r.getStartTime(), r.getEndTime()).toMinutes())
                .sum();
            double avgDurationMinutes = (double) totalDurationMinutes / allReservations.size();
            statsAvgDurationLabel.setText(String.format("%.2f minutes", avgDurationMinutes));
        } else {
            statsAvgDurationLabel.setText("N/A");
        }

        // Total Parking Spots
        statsTotalSpotsLabel.setText(String.valueOf(allSpots.size()));

        // Current Occupancy
        LocalDateTime now = LocalDateTime.now();
        long activeReservations = allReservations.stream()
            .filter(r -> r.getStartTime().isBefore(now) && r.getEndTime().isAfter(now))
            .count();
        if (!allSpots.isEmpty()) {
            double occupancyPercentage = ((double) activeReservations / allSpots.size()) * 100;
            statsCurrentOccupancyLabel.setText(String.format("%d / %d (%.2f%%)", activeReservations, allSpots.size(), occupancyPercentage));
        } else {
            statsCurrentOccupancyLabel.setText("N/A (No spots defined)");
        }
        
        // Most Popular Spot
        if (!allReservations.isEmpty()) {
            Map<String, Long> spotCounts = allReservations.stream()
                .collect(Collectors.groupingBy(Reservation::getSpotId, Collectors.counting()));
            Optional<Map.Entry<String, Long>> popularSpotEntry = spotCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue());
            statsPopularSpotLabel.setText(popularSpotEntry.map(entry -> entry.getKey() + " (" + entry.getValue() + " reservations)").orElse("N/A"));
        } else {
            statsPopularSpotLabel.setText("N/A");
        }

        // Peak Reservation Hour
        if (!allReservations.isEmpty()) {
            Map<Integer, Long> hourCounts = allReservations.stream()
                .collect(Collectors.groupingBy(r -> r.getStartTime().getHour(), Collectors.counting()));
            Optional<Map.Entry<Integer, Long>> peakHourEntry = hourCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue());
            statsPeakHourLabel.setText(peakHourEntry.map(entry -> String.format("%02d:00 - %02d:59 (%d reservations)", entry.getKey(), entry.getKey(), entry.getValue())).orElse("N/A"));
        } else {
            statsPeakHourLabel.setText("N/A");
        }

        System.out.println("Statistics data loaded.");
    }
    
    @FXML
    private void handleRefreshStatistics() {
        System.out.println("Refresh Statistics button clicked.");
        loadStatisticsData();
        showAlert(AlertType.INFORMATION, "Refresh", "Statistics updated.");
    }

    @FXML
    private void handleRefreshSpots() {
        System.out.println("Refresh Spots button clicked.");
        loadParkingSpotsData();
        parkingSpotsTable.refresh();
        showAlert(AlertType.INFORMATION, "Refresh", "Parking spot list updated.");
    }

    @FXML
    private void handleAddSpot() {
        System.out.println("Add Spot button clicked.");
        Optional<ParkingSpot> result = showParkingSpotDialog(null);
        result.ifPresent(spot -> {
            if (spotService.addParkingSpot(spot)) {
                loadParkingSpotsData(); // Refresh table
                showAlert(AlertType.INFORMATION, "Success", "Parking spot added successfully.");
            } else {
                showAlert(AlertType.ERROR, "Error", "Failed to add parking spot. Spot ID might already exist.");
            }
        });
    }

    @FXML
    private void handleEditSpot() {
        ParkingSpot selectedSpot = parkingSpotsTable.getSelectionModel().getSelectedItem();
        if (selectedSpot == null) {
            showAlert(AlertType.WARNING, "No Selection", "Please select a spot to edit.");
            return;
        }

        System.out.println("Edit Spot button clicked for: " + selectedSpot.getSpotId());
        Optional<ParkingSpot> result = showParkingSpotDialog(selectedSpot);
        result.ifPresent(spot -> {
            if (spotService.updateParkingSpot(spot)) {
                loadParkingSpotsData(); // Refresh table
                showAlert(AlertType.INFORMATION, "Success", "Parking spot updated successfully.");
            } else {
                showAlert(AlertType.ERROR, "Error", "Failed to update parking spot.");
            }
        });
    }

    private Optional<ParkingSpot> showParkingSpotDialog(ParkingSpot parkingSpot) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/com/smartparking/view/ParkingSpotDialog.fxml"));
            DialogPane dialogPane = loader.load();

            TextField spotIdField = (TextField) dialogPane.lookup("#spotIdField");
            TextField locationInfoField = (TextField) dialogPane.lookup("#locationInfoField");
            TextField typeField = (TextField) dialogPane.lookup("#typeField");
            TextField hourlyRateField = (TextField) dialogPane.lookup("#hourlyRateField");

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle(parkingSpot == null ? "Add New Parking Spot" : "Edit Parking Spot");

            if (parkingSpot != null) {
                spotIdField.setText(parkingSpot.getSpotId());
                spotIdField.setEditable(false); // Spot ID should not be editable
                locationInfoField.setText(parkingSpot.getLocationInfo());
                typeField.setText(parkingSpot.getType());
                hourlyRateField.setText(String.valueOf(parkingSpot.getHourlyRate()));
            } else {
                spotIdField.setEditable(true);
            }

            Optional<ButtonType> clickedButton = dialog.showAndWait();
            if (clickedButton.isPresent() && clickedButton.get() == ButtonType.OK) {
                String spotId = spotIdField.getText().trim();
                String locationInfo = locationInfoField.getText().trim();
                String type = typeField.getText().trim();
                double hourlyRate;

                if (spotId.isEmpty() || locationInfo.isEmpty() || type.isEmpty() || hourlyRateField.getText().trim().isEmpty()) {
                    showAlert(AlertType.ERROR, "Validation Error", "All fields are required.");
                    return Optional.empty();
                }

                try {
                    hourlyRate = Double.parseDouble(hourlyRateField.getText().trim());
                    if (hourlyRate < 0) {
                         showAlert(AlertType.ERROR, "Validation Error", "Hourly rate cannot be negative.");
                         return Optional.empty();
                    }
                } catch (NumberFormatException e) {
                    showAlert(AlertType.ERROR, "Validation Error", "Invalid hourly rate format.");
                    return Optional.empty();
                }
                return Optional.of(new ParkingSpot(spotId, locationInfo, type, hourlyRate));
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Dialog Error", "Could not load parking spot dialog.");
        }
        return Optional.empty();
    }

    @FXML
    private void handleDeleteSpot() {
        ParkingSpot selectedSpot = parkingSpotsTable.getSelectionModel().getSelectedItem();
        if (selectedSpot != null) {
            Alert confirmation = new Alert(AlertType.CONFIRMATION);
            confirmation.setTitle("Confirm Deletion");
            confirmation.setHeaderText("Delete Parking Spot: " + selectedSpot.getSpotId());
            confirmation.setContentText("Are you sure you want to delete spot " + selectedSpot.getSpotId() + "?");

            Optional<ButtonType> result = confirmation.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                boolean deleted = spotService.deleteParkingSpot(selectedSpot.getSpotId());
                if (deleted) {
                    parkingSpotData.remove(selectedSpot);
                    showAlert(AlertType.INFORMATION, "Deletion Successful", "Parking spot " + selectedSpot.getSpotId() + " deleted.");
                } else {
                    showAlert(AlertType.ERROR, "Deletion Failed", "Could not delete parking spot " + selectedSpot.getSpotId() + ".");
                }
            }
        } else {
            showAlert(AlertType.WARNING, "No Selection", "Please select a spot to delete.");
        }
    }


    /**
     * Handles the Refresh button action. Reloads data into the table.
     */
    @FXML
    private void handleRefresh() {
        System.out.println("Refresh button clicked.");
        loadReservationData();
        reservationsTable.refresh(); // Ensure table UI updates
        showAlert(AlertType.INFORMATION, "Refresh", "Reservation list updated.");
    }

    /**
     * Handles the Delete Selected button action.
     * Removes the selected reservation from the service and the table.
     */
    @FXML
    private void handleDelete() {
        Reservation selectedReservation = reservationsTable.getSelectionModel().getSelectedItem();

        if (selectedReservation != null) {
            // Confirmation dialog
            Alert confirmation = new Alert(AlertType.CONFIRMATION);
            confirmation.setTitle("Confirm Deletion");
            confirmation.setHeaderText("Delete Reservation ID: " + selectedReservation.getReservationId());
            confirmation.setContentText("Are you sure you want to delete reservation for spot "
                                        + selectedReservation.getSpotId() + " starting "
                                        + selectedReservation.getFormattedStartTime() + "?");

            Optional<ButtonType> result = confirmation.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Use the integer ID for deletion
                boolean deleted = reservationService.deleteReservation(selectedReservation.getReservationId());
                if (deleted) {
                    // Remove from the underlying list for the TableView
                    reservationData.remove(selectedReservation);
                    // No need to call reservationsTable.refresh() when using ObservableList backing
                    System.out.println("Reservation deleted successfully from UI and service (ID: " + selectedReservation.getReservationId() + ").");
                    showAlert(AlertType.INFORMATION, "Deletion Successful", "Reservation " + selectedReservation.getReservationId() + " deleted.");
                } else {
                    // Error message already printed by service
                    showAlert(AlertType.ERROR, "Deletion Failed", "Could not delete the selected reservation (ID: " + selectedReservation.getReservationId() + ").");
                }
            } else {
                 System.out.println("Deletion cancelled by user.");
            }
        } else {
            // Nothing selected
            showAlert(AlertType.WARNING, "No Selection", "Please select a reservation in the table to delete.");
        }
    }

    /**
     * Placeholder method to show details of the selected reservation (optional).
     * @param reservation The selected reservation, or null if selection cleared.
     */
    private void showReservationDetails(Reservation reservation) {
        if (reservation != null) {
            System.out.println("Selected Reservation: " + reservation.toString());
            // Could update a details pane here
        } else {
             System.out.println("Selection cleared.");
        }
    }

     /**
     * Helper method to show alerts.
     * @param alertType Type of the alert
     * @param title Title of the alert
     * @param message Content message
     */
    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Add other methods for admin actions (e.g., editing)

    @FXML
    private void handleLogout() {
        System.out.println("Logout button clicked by admin. Navigating to HomeView.");
        try {
            // Get the current stage
            Stage currentStage = (Stage) adminTabPane.getScene().getWindow();

            // Load the HomeView.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/smartparking/view/HomeView.fxml"));
            Parent homeRoot = loader.load();
            
            // Set the HomeView as the new scene on the current stage
            Scene homeScene = new Scene(homeRoot);
            // Explicitly add the stylesheet
            String cssPath = getClass().getResource("/com/smartparking/css/styles.css").toExternalForm();
            homeScene.getStylesheets().add(cssPath);
            
            currentStage.setScene(homeScene);
            currentStage.setTitle("Smart Parking - Welcome");
            // Resetting to a typical home view size, adjust if necessary
            currentStage.setWidth(800); 
            currentStage.setHeight(600);
            currentStage.centerOnScreen();
            // currentStage.setFullScreen(false); // Ensure not fullscreen if previously set

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Logout Error", "Could not load the home page.");
        }
    }
}

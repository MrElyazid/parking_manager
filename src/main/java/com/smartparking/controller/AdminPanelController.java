package com.smartparking.controller;

import com.smartparking.model.Reservation;
import com.smartparking.service.ReservationService;
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

import java.text.NumberFormat; // For currency formatting
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class AdminPanelController {

    @FXML private TableView<Reservation> reservationsTable;

    // Inject columns to apply custom cell factories if needed
    @FXML private TableColumn<Reservation, Integer> reservationIdColumn;
    @FXML private TableColumn<Reservation, Integer> userIdColumn; // Add if displaying user ID
    @FXML private TableColumn<Reservation, String> spotIdColumn;
    @FXML private TableColumn<Reservation, String> startTimeColumn; // Use String for formatted value
    @FXML private TableColumn<Reservation, String> endTimeColumn;   // Use String for formatted value
    @FXML private TableColumn<Reservation, Double> priceColumn;
    @FXML private TableColumn<Reservation, String> createdTimestampColumn; // Use String for formatted value


    private final ReservationService reservationService = new ReservationService();
    private ObservableList<Reservation> reservationData = FXCollections.observableArrayList();

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
        // userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId")); // Uncomment if userId column added
        spotIdColumn.setCellValueFactory(new PropertyValueFactory<>("spotId"));
        startTimeColumn.setCellValueFactory(new PropertyValueFactory<>("formattedStartTime")); // Use formatted getter
        endTimeColumn.setCellValueFactory(new PropertyValueFactory<>("formattedEndTime"));     // Use formatted getter
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("calculatedPrice"));
        createdTimestampColumn.setCellValueFactory(new PropertyValueFactory<>("formattedCreationTimestamp")); // Use formatted getter

        // --- Optional: Add Custom Cell Factories for Formatting ---

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
}

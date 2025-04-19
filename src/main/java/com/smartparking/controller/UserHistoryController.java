package com.smartparking.controller;

import com.smartparking.model.Reservation;
import com.smartparking.model.User;
import com.smartparking.service.ReservationService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.text.NumberFormat;
import java.util.List;

public class UserHistoryController {

    @FXML private TableView<Reservation> historyTable;
    @FXML private TableColumn<Reservation, Integer> reservationIdColumn;
    @FXML private TableColumn<Reservation, String> spotIdColumn;
    @FXML private TableColumn<Reservation, String> startTimeColumn;
    @FXML private TableColumn<Reservation, String> endTimeColumn;
    @FXML private TableColumn<Reservation, Double> priceColumn;
    @FXML private TableColumn<Reservation, String> bookedAtColumn;

    private MainController mainController;
    private User currentUser;
    private final ReservationService reservationService = new ReservationService();
    private ObservableList<Reservation> userReservations = FXCollections.observableArrayList();

    // Formatter for currency
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance();

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        // Load history once the user is set
        loadHistory();
    }

    @FXML
    private void initialize() {
        // Configure table columns
        reservationIdColumn.setCellValueFactory(new PropertyValueFactory<>("reservationId"));
        spotIdColumn.setCellValueFactory(new PropertyValueFactory<>("spotId"));
        startTimeColumn.setCellValueFactory(new PropertyValueFactory<>("formattedStartTime"));
        endTimeColumn.setCellValueFactory(new PropertyValueFactory<>("formattedEndTime"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("calculatedPrice"));
        bookedAtColumn.setCellValueFactory(new PropertyValueFactory<>("formattedCreationTimestamp"));

        // Format price column
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

        historyTable.setItems(userReservations);

         // Add placeholder for empty table
        historyTable.setPlaceholder(new Label("No reservation history found."));
    }

    private void loadHistory() {
        if (currentUser != null) {
            List<Reservation> history = reservationService.getReservationsForUser(currentUser.getUserId());
            userReservations.setAll(history);
            System.out.println("Loaded " + history.size() + " reservations for user " + currentUser.getUsername());
        } else {
            System.err.println("Cannot load history: current user is null.");
            userReservations.clear(); // Clear table if no user
             showAlert(Alert.AlertType.ERROR, "Error", "Could not load history because user is not identified.");
        }
    }

    @FXML
    private void handleRefresh() {
        System.out.println("Refreshing user history...");
        loadHistory();
    }

    @FXML
    private void handleGoToReservation() {
        if (mainController != null) {
            System.out.println("Navigating to parking lot view...");
            mainController.loadParkingLotView();
        } else {
             System.err.println("MainController reference not set in UserHistoryController.");
             showAlert(Alert.AlertType.ERROR, "Navigation Error", "Cannot navigate to reservation screen.");
        }
    }

    // TODO: Add handleCancelReservation method if needed

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

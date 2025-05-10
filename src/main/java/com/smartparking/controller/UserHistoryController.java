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
import javafx.scene.layout.VBox;

import java.text.NumberFormat;
import java.util.List;

public class UserHistoryController {

    @FXML private VBox cardContainer;

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



    }
    private VBox createReservationCard(Reservation reservation) {
        VBox card = new VBox(8);
        card.getStyleClass().add("reservation-card");

        Label spotId = new Label("Spot ID: " + reservation.getSpotId());
        Label Time = new Label("From: " + reservation.getFormattedStartTime()+" to :"+reservation.getFormattedEndTime());
        Label price = new Label("Price: " + CURRENCY_FORMAT.format(reservation.getCalculatedPrice()));
        Label bookedAt = new Label("Booked At: " + reservation.getFormattedCreationTimestamp());

        Button modifyButton = new Button("Modify");
        modifyButton.setOnAction(e -> handleModifyReservation(reservation));

        card.getChildren().addAll(spotId ,Time, price, bookedAt, modifyButton);
        return card;
    }

    private void handleModifyReservation(Reservation reservation) {
        // TODO: implement the logic to modify the reservation
        System.out.println("Modify clicked for reservation ID: " + reservation.getReservationId());
        showAlert(Alert.AlertType.INFORMATION, "Modify", "Modify reservation feature is not yet implemented.");
    }



    private void loadHistory() {
        cardContainer.getChildren().clear();

        if (currentUser != null) {
            List<Reservation> history = reservationService.getReservationsForUser(currentUser.getUserId());

            for (Reservation reservation : history) {
                VBox card = createReservationCard(reservation);
                cardContainer.getChildren().add(card);
            }

            System.out.println("Loaded " + history.size() + " reservations for user " + currentUser.getUsername());
        } else {
            System.err.println("Cannot load history: current user is null.");
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load history because user is not identified.");
        }
    }


    @FXML
    private void handleRefresh() {
        System.out.println("Refreshing user history...");
        loadHistory();
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

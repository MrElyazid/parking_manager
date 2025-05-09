package com.smartparking.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;

public class HomeController {

    // private Stage primaryStage; // This would ideally be set by MainApp

    // public void setPrimaryStage(Stage primaryStage) {
    //     this.primaryStage = primaryStage;
    // }

    @FXML
    public void handleSignInButtonClick(ActionEvent event) {
        try {
            // Load the MainView.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/smartparking/view/MainView.fxml"));
            Parent mainViewRoot = loader.load();
            // MainController mainController = loader.getController(); // Get controller if needed for further setup

            Scene mainViewScene = new Scene(mainViewRoot, 1200, 700);

            // Get the current stage from the event source
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.setScene(mainViewScene);
            currentStage.setTitle("Smart Parking Application");
            // currentStage.setFullScreen(true); // Optional
            currentStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load the main application view.");
        }
    }

    @FXML
    public void handleSignUpButtonClick(ActionEvent event) {
        try {
            // Load the MainView.fxml first, which will then allow navigation to UserRegisterView
            FXMLLoader mainLoader = new FXMLLoader(getClass().getResource("/com/smartparking/view/MainView.fxml"));
            Parent mainViewRoot = mainLoader.load();
            MainController mainController = mainLoader.getController();

            // After MainView is loaded (which loads UserLoginView by default in its initialize),
            // tell MainController to switch to the UserRegisterView.
            if (mainController != null) {
                mainController.loadUserRegisterView();
            } else {
                // Fallback or error: MainController not found, cannot switch to register view automatically.
                // This might happen if MainView.fxml doesn't have MainController as its controller.
                // For now, we'll just load MainView, and user has to click "Register here" link.
                 System.err.println("MainController not found after loading MainView. User may need to click register link manually.");
            }

            Scene mainViewScene = new Scene(mainViewRoot, 1200, 700);

            // Get the current stage
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.setScene(mainViewScene);
            currentStage.setTitle("Smart Parking Application");
            currentStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load the application view for sign-up.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

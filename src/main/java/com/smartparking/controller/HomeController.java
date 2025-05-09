package com.smartparking.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Alert; // Added Alert import

import java.io.IOException;

public class HomeController {

    @FXML
    public void handleSignInButtonClick() {
        try {
            // Load the MainView.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/smartparking/view/MainView.fxml"));
            Parent loginRoot = loader.load();

            // Create a new stage for the login view
            Stage stage = new Stage();
            stage.setScene(new Scene(loginRoot, 1200, 700));

            // Set the window title
            stage.setTitle("Parking App");

            // Set the stage to fullscreen

            // Show the new stage (MainView)
            stage.show();



        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleSignUpButtonClick() {
        try {
            // Load the UserRegisterView.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/smartparking/view/UserRegisterView.fxml"));
            Parent registerRoot = loader.load();

            // Create a new stage for the register view
            Stage stage = new Stage();
            stage.setScene(new Scene(registerRoot)); // Adjust size as needed

            // Set the window title
            stage.setTitle("Sign Up - Smart Parking");

            // Show the new stage
            stage.show();

            // Optionally, close the current home stage if desired
            // ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();

        } catch (IOException e) {
            e.printStackTrace();
            // Handle exception, e.g., show an error dialog
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Could not load the sign-up page.");
            alert.setContentText("An error occurred: " + e.getMessage());
            alert.showAndWait();
        }
    }
}

package com.smartparking.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

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



}

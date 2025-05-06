package com.smartparking;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import com.smartparking.util.DatabaseManager; // Import DatabaseManager

import java.io.IOException;
import java.net.URL;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {

        // Initialize the database first
        DatabaseManager.initializeDatabase();

        try {
            // Load the main FXML file (or maybe a login screen first later)
            URL fxmlUrl = getClass().getResource("/com/smartparking/view/HomeView.fxml");
            if (fxmlUrl == null) {
                System.err.println("Cannot find FXML file. Make sure it's in the correct resources path.");
                return; // Exit if FXML not found
            }
            Parent root = FXMLLoader.load(fxmlUrl);

            // Create the scene
            Scene scene = new Scene(root, 1000, 700); // Initial size

            // Load CSS (optional, but good for styling)
            URL cssUrl = getClass().getResource("/com/smartparking/css/styles.css");
             if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
             } else {
                 System.err.println("Cannot find CSS file. Styles will not be applied.");
             }


            // Set the stage
            primaryStage.setTitle("Smart Parking System");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (IOException e) {
            System.err.println("Error loading FXML file: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for debugging
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

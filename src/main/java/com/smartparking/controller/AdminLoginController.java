package com.smartparking.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AdminLoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorMessageLabel;

    private boolean loginSuccessful = false;

    @FXML
    private void initialize() {
        errorMessageLabel.setVisible(false); // Hide error message initially
    }

    /**
     * Handles the Login button action.
     * Validates credentials (hardcoded for now).
     */
    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // --- Simple Hardcoded Credentials Check ---
        // Replace with a proper authentication mechanism in a real app
        if ("admin".equals(username) && "password123".equals(password)) {
            loginSuccessful = true;
            System.out.println("Admin login successful!");
            closeDialog();
            // TODO: Trigger navigation to admin panel in the main application
        } else {
            errorMessageLabel.setText("Invalid username or password.");
            errorMessageLabel.setVisible(true);
            loginSuccessful = false;
            System.err.println("Admin login failed.");
        }
    }

    /**
     * Handles the Cancel button action.
     */
    @FXML
    private void handleCancel() {
        loginSuccessful = false;
        closeDialog();
    }

    /**
     * Closes the login dialog window.
     */
    private void closeDialog() {
        Stage stage = (Stage) usernameField.getScene().getWindow(); // Get the stage from any control
        if (stage != null) {
            stage.close();
        }
    }

    /**
     * Allows the calling controller (MainController) to check if login was successful.
     * @return true if login credentials were valid, false otherwise.
     */
    public boolean isLoginSuccessful() {
        return loginSuccessful;
    }
}

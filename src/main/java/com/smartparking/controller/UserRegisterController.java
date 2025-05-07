package com.smartparking.controller;

import com.smartparking.service.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

public class UserRegisterController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;

    private final UserService userService = new UserService();
    private MainController mainController; // Reference to switch views

    // Setter for MainController reference
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void initialize() {
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);
    }

    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // --- Input Validation ---
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showMessage("All fields are required.", Color.RED);
            return;
        }

        if (username.length() < 3) {
             showMessage("Username must be at least 3 characters long.", Color.RED);
             return;
        }

        if (password.length() < 6) { // Basic password length check
             showMessage("Password must be at least 6 characters long.", Color.RED);
             return;
        }

        if (!password.equals(confirmPassword)) {
            showMessage("Passwords do not match.", Color.RED);
            return;
        }

        // --- Attempt Registration ---
        boolean registered = userService.registerUser(username, password);

        if (registered) {
            System.out.println("User registration successful: " + username);
            showMessage("Registration successful! You can now log in.", Color.GREEN);
            // Optionally clear fields or navigate directly to login
            clearFields();
            // Consider adding a small delay before navigating or let user click login link
            // handleGoToLogin(); // Or navigate automatically after success message
        } else {
            // Error message likely printed by UserService (e.g., username exists)
            showMessage("Registration failed. Username might already exist.", Color.RED);
            System.err.println("User registration failed for: " + username);
        }
    }

    @FXML
    private void handleGoToLogin() throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/smartparking/view/MainView.fxml"));
            Parent loginRoot = loader.load();
            // Get the current stage from any control inside the window (like usernameField)
            Stage stage = (Stage) usernameField.getScene().getWindow();

            stage.getScene().setRoot(loginRoot);


        } catch (IOException e) {
            e.printStackTrace();
            showMessage("Cannot navigate to registration.", Color.RED);
        }
    }

     private void showMessage(String message, Color color) {
        messageLabel.setText(message);
        messageLabel.setTextFill(color);
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }

    private void clearFields() {
        usernameField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
    }
}

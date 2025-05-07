package com.smartparking.controller;

import com.smartparking.model.User;
import com.smartparking.service.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color; // For setting text color
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class UserLoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    private final UserService userService = new UserService();
    private MainController mainController; // Reference to switch views
    private User loggedInUser = null;

    // Setter for MainController reference (dependency injection alternative)
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void initialize() {
        messageLabel.setVisible(false);
        messageLabel.setManaged(false); // Don't reserve space when invisible
    }



    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showMessage("Username and password cannot be empty.", Color.RED);
            return;
        }

        Optional<User> userOptional = userService.authenticateUser(username, password);

        if (userOptional.isPresent()) {
            loggedInUser = userOptional.get();
            System.out.println("User login successful: " + loggedInUser.getUsername());
            showMessage("Login successful!", Color.GREEN);
            this.setMainController(mainController);

            // Notify MainController or switch view
            if (mainController != null) {
                mainController.handleUserLoginSuccess(loggedInUser);

            } else {
                 System.err.println("MainController reference not set in UserLoginController.");
                 showMessage("Login successful, but cannot switch view.", Color.ORANGE);
            }
        } else if ("admin".equals(username) && "password123".equals(password)) {
            System.out.println("Admin login successful!");
            if (mainController != null) {
                mainController. loadAdminPanelView();
            } else {
                System.err.println("MainController reference not set in UserLoginController.");
                showMessage("Login successful, but cannot switch view.", Color.ORANGE);
            }

        }else {
            loggedInUser = null;
            showMessage("Invalid username or password.", Color.RED);
            System.err.println("User login failed for: " + username);
        }
    }

    @FXML
    private void handleGoToRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/smartparking/view/UserRegisterView.fxml"));
            Parent registerRoot = loader.load();

            // Get the current stage from any control inside the window (like usernameField)
            Stage stage = (Stage) usernameField.getScene().getWindow();

            // Set a new scene or replace the root
            stage.getScene().setRoot(registerRoot);


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

    // Optional: Method for MainController to check login status if needed
    public User getLoggedInUser() {
        return loggedInUser;
    }
}

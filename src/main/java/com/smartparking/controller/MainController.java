package com.smartparking.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.fxml.FXMLLoader; // Import FXMLLoader
import javafx.scene.Parent;    // Import Parent
import javafx.scene.Scene;     // Import Scene
import javafx.stage.Modality; // Import Modality
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox; // To potentially hide/show elements

import com.smartparking.model.User; // Import User model
import com.smartparking.service.ReservationService; // Keep for now, might be needed elsewhere


import java.io.IOException;
import java.net.URL;        // Import URL


public class MainController {

    // --- Injected FXML Elements ---
    @FXML private BorderPane mainBorderPane;
    @FXML private Button historyButton;
    @FXML private Button logoutButton;
    @FXML private Label statusLabel;
    @FXML private VBox centerContentVBox;


    // --- State ---
    private User currentLoggedInUser = null;
    private final ReservationService reservationService = new ReservationService();


    @FXML
    private void initialize() {
        System.out.println("MainController initialized.");
        // Load login view by default
        updateUIForLoginState(); // Set initial state

        loadUserLoginView();
        statusLabel.setText("Please log in or register.");
    }

    // --- View Loading Methods ---

    /** Loads the User Login view into the center pane. */
    public void loadUserLoginView() {
        loadView("/com/smartparking/view/UserLoginView.fxml", true); // Pass true to set MainController ref
    }

    /** Loads the User Registration view into the center pane. */
    public void loadUserRegisterView() {
        loadView("/com/smartparking/view/UserRegisterView.fxml", true); // Pass true to set MainController ref
    }

    /** Loads the Parking Lot view into the center pane. */
    public void loadParkingLotView() {
        loadView("/com/smartparking/view/ParkingLotView.fxml", true); // Pass true to set refs
    }


    private void loadView(String fxmlPath, boolean setMainControllerRef) {
        try {
            URL viewUrl = getClass().getResource(fxmlPath);
            if (viewUrl == null) {
                System.err.println("Cannot find FXML resource: " + fxmlPath);
                statusLabel.setText("Error: Cannot load view.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(viewUrl);
            Parent viewRoot = loader.load();

            // Set the reference to this MainController if requested
            if (setMainControllerRef) {
                Object controller = loader.getController();
                if (controller instanceof UserLoginController) {
                    ((UserLoginController) controller).setMainController(this);
                } else if (controller instanceof UserRegisterController) {
                    ((UserRegisterController) controller).setMainController(this);
                } else if (controller instanceof ParkingLotController) {
                    // Pass MainController and current User to ParkingLotController
                    ParkingLotController plc = (ParkingLotController) controller;
                    plc.setMainController(this);
                    plc.setCurrentUser(this.currentLoggedInUser);
                } else if (controller instanceof UserHistoryController) {
                    // Pass MainController and current User to UserHistoryController
                    UserHistoryController uhc = (UserHistoryController) controller;
                    uhc.setMainController(this);
                    uhc.setCurrentUser(this.currentLoggedInUser); // Pass the logged-in user
                }
                // Add other controller types here if needed
            }

            // Ensure the main BorderPane is available
             if (mainBorderPane == null) {
                 // Try to get it from the scene root if not injected directly
                 if (statusLabel != null && statusLabel.getScene() != null && statusLabel.getScene().getRoot() instanceof BorderPane) {
                     mainBorderPane = (BorderPane) statusLabel.getScene().getRoot();
                 }
             }


            if (mainBorderPane != null) {
                 // Hide the current center content if it's the reservation VBox
                 if (centerContentVBox != null && mainBorderPane.getCenter() == centerContentVBox) {
                     centerContentVBox.setVisible(false);
                     centerContentVBox.setManaged(false);
                 }
                mainBorderPane.setCenter(viewRoot);
                System.out.println("Loaded view: " + fxmlPath);
            } else {
                System.err.println("Main BorderPane is null. Cannot set center view.");
                statusLabel.setText("Error: Application layout error.");
            }

        } catch (IOException e) {
            System.err.println("Error loading FXML view '" + fxmlPath + "': " + e.getMessage());
            e.printStackTrace();
            statusLabel.setText("Error loading view.");
            showAlert(AlertType.ERROR, "Loading Error", "Could not load the requested view.");
        } catch (Exception e) {
             System.err.println("An unexpected error occurred loading view '" + fxmlPath + "': " + e.getMessage());
             e.printStackTrace();
             statusLabel.setText("Unexpected error.");
             showAlert(AlertType.ERROR, "Unexpected Error", "An unexpected error occurred.");
        }
    }



    private boolean isLoggedIn() {
        return currentLoggedInUser != null;
    }

    public void handleUserLoginSuccess(User user) {
        this.currentLoggedInUser = user;
        System.out.println("Login successful for user: " + user.getUsername());
        // Load the parking lot view for the logged-in user
        updateUIForLoginState();

        loadParkingLotView();
        // Update UI elements
        statusLabel.setText("Logged in as: " + user.getUsername() + ". Select date/time and check availability.");
        // Show user-specific buttons, hide admin login?
        if (historyButton != null) { historyButton.setVisible(true); historyButton.setManaged(true); }
        if (logoutButton != null) { logoutButton.setVisible(true); logoutButton.setManaged(true); }
    }
    private void updateUIForLoginState() {
        boolean loggedIn = isLoggedIn();

        // Update top navigation
        if (mainBorderPane.getTop() != null) {
            mainBorderPane.getTop().setVisible(loggedIn);
            mainBorderPane.getTop().setManaged(loggedIn);
        }

        // Update left menu
        if (mainBorderPane.getLeft() != null) {
            mainBorderPane.getLeft().setVisible(loggedIn);
            mainBorderPane.getLeft().setManaged(loggedIn);
        }

        // Update bottom status
        if (mainBorderPane.getBottom() != null) {
            mainBorderPane.getBottom().setVisible(loggedIn);
            mainBorderPane.getBottom().setManaged(loggedIn);
        }

        // Update buttons
        if (historyButton != null) {
            historyButton.setVisible(loggedIn);
            historyButton.setManaged(loggedIn);
        }
        if (logoutButton != null) {
            logoutButton.setVisible(loggedIn);
            logoutButton.setManaged(loggedIn);
        }
    }

    /** Handles the action for the "My History" button. */
    @FXML
    private void handleShowHistory() {
        if (currentLoggedInUser == null) {
            showAlert(AlertType.WARNING, "Not Logged In", "Please log in to view your history.");
            loadUserLoginView();
            return;
        }
        System.out.println("Loading user history view...");
        loadView("/com/smartparking/view/UserHistoryView.fxml", true); // Pass true to set refs
    }

     /** Handles the action for the "Logout" button. */
    @FXML
    public void handleLogout() { // Changed to public
        System.out.println("User logging out: " + (currentLoggedInUser != null ? currentLoggedInUser.getUsername() : "null"));
        currentLoggedInUser = null;
        // Hide user-specific buttons, show admin login
        if (historyButton != null) { historyButton.setVisible(false); historyButton.setManaged(false); }
        if (logoutButton != null) { logoutButton.setVisible(false); logoutButton.setManaged(false); }
        // Return to login screen
        updateUIForLoginState(); // Set initial state

        loadUserLoginView();
        statusLabel.setText("Logged out. Please log in or register.");
    }



     public void loadAdminPanelView() {
         System.out.println("Attempting to load Admin Panel View...");
         try {
             URL adminPanelUrl = getClass().getResource("/com/smartparking/view/AdminPanelView.fxml");
             if (adminPanelUrl == null) {
                 System.err.println("Cannot find AdminPanelView.fxml resource.");
                 showAlert(AlertType.ERROR, "Load Error", "Could not find the Admin Panel view file.");
                 statusLabel.setText("Error: Admin Panel view not found.");
                 return;
             }

             FXMLLoader loader = new FXMLLoader(adminPanelUrl); // Use FXMLLoader instance
             Parent adminRoot = loader.load();

             // Pass MainController reference to AdminPanelController
             Object controller = loader.getController();
             if (controller instanceof AdminPanelController) {
                 ((AdminPanelController) controller).setMainController(this);
             }


             // Get the main layout's BorderPane
             BorderPane mainLayout = (BorderPane) statusLabel.getScene().getRoot();
             if (mainLayout != null) {
                 mainLayout.setCenter(adminRoot); // Replace center content with the admin panel
                 statusLabel.setText("Admin Panel Loaded Successfully.");
                 System.out.println("Admin Panel View loaded into center pane.");
                 // Optionally hide the user reservation form elements if they were separate nodes
             } else {
                  System.err.println("Could not find the main BorderPane layout.");
                  showAlert(AlertType.ERROR, "Layout Error", "Could not find the main application layout to display the admin panel.");
                  statusLabel.setText("Error: Could not display Admin Panel.");
             }

         } catch (IOException e) {
             System.err.println("Error loading AdminPanelView.fxml: " + e.getMessage());
             e.printStackTrace();
             showAlert(AlertType.ERROR, "Loading Error", "An error occurred while loading the Admin Panel.");
             statusLabel.setText("Error loading Admin Panel.");
         } catch (Exception e) {
             System.err.println("An unexpected error occurred loading Admin Panel: " + e.getMessage());
             e.printStackTrace();
             showAlert(AlertType.ERROR, "Unexpected Error", "An unexpected error occurred.");
             statusLabel.setText("Error loading Admin Panel.");
         }
     }
    @FXML
    private void handleGoToReservation() {

            this.loadParkingLotView();

    }


    /**
     * Helper method to show alerts.
     * (Implementation remains the same)
     * @param alertType Type of the alert (e.g., INFORMATION, WARNING, ERROR)
     * @param title Title of the alert window
     * @param message Content message of the alert
     */
    public void showAlert(AlertType alertType, String title, String message) { // Changed to public
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null); // No header text
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Add other event handler methods here (e.g., for admin panel interactions)
}

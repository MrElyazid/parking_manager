package com.smartparking.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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

import com.smartparking.model.Reservation;
import com.smartparking.model.User; // Import User model
import com.smartparking.service.ReservationService; // Keep for now, might be needed elsewhere
import com.smartparking.controller.AdminLoginController;
import com.smartparking.controller.UserLoginController;
import com.smartparking.controller.UserRegisterController;
import com.smartparking.controller.ParkingLotController;
import com.smartparking.controller.UserHistoryController; // Import UserHistoryController


import java.io.IOException;
import java.net.URL;        // Import URL
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


public class MainController {

    // --- Injected FXML Elements ---
    @FXML private BorderPane mainBorderPane;
    @FXML private Button adminLoginButton;
    @FXML private Button historyButton; // Added
    @FXML private Button logoutButton;  // Added
    @FXML private Label statusLabel;
    // @FXML private DatePicker reservationDatePicker; // Removed
    // @FXML private TextField durationField;         // Removed
    // @FXML private Button reserveButton;            // Removed
    // @FXML private Label priceLabel;               // Removed
    // We might need to inject the center VBox if we want to hide/show it
    @FXML private VBox centerContentVBox; // Keep for now to hide the old content area


    // --- State ---
    private User currentLoggedInUser = null;

    // --- Constants ---
    // private static final double HOURLY_RATE = 2.5; // Moved to ParkingLotController
    // private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // Moved or unused

    // --- Service Instances ---
    // UserService is instantiated within its controllers
    // ReservationService might still be needed for Admin Panel, keep for now
    private final ReservationService reservationService = new ReservationService();


    @FXML
    private void initialize() {
        System.out.println("MainController initialized.");
        // Load login view by default
        loadUserLoginView();
        statusLabel.setText("Please log in or register.");
        // Hide admin button initially? Or handle visibility based on login state later.
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

     /** Loads the main Reservation view (current center content) into the center pane. */
     // THIS METHOD IS NOW OBSOLETE as reservation happens via ParkingLotView
     /* private void loadReservationView() {
         // For now, we assume the reservation components are already in MainView.fxml's center.
         // If we separate it later, we'll load it like the others.
         if (centerContentVBox != null) {
             centerContentVBox.setVisible(true);
             centerContentVBox.setManaged(true);
             statusLabel.setText("Welcome, " + currentLoggedInUser.getUsername() + "! Reserve your spot.");
             priceLabel.setText("--"); // Reset price label
             // Clear date/duration fields if needed
             if(reservationDatePicker != null) reservationDatePicker.setValue(null);
             if(durationField != null) durationField.clear();

         } else {
             System.err.println("Center content VBox not injected or available.");
             // Fallback or load a dedicated reservation FXML if structure changes
             // loadView("/com/smartparking/view/ReservationView.fxml", false);
         }
         System.out.println("Displaying reservation view for user: " + currentLoggedInUser.getUsername());
     }


    /**
     * Generic method to load an FXML view into the center of the main BorderPane.
     * @param fxmlPath Path to the FXML file (relative to resources).
     * @param setMainControllerRef If true, attempts to set this MainController instance on the loaded controller.
     */
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


    // --- Event Handlers & Callbacks ---

    /**
     * Handles the action for the Admin Login button.
     * This method name must match the onAction value in the FXML.
     */
     /** Called by UserLoginController upon successful login. */
    public void handleUserLoginSuccess(User user) {
        this.currentLoggedInUser = user;
        System.out.println("Login successful for user: " + user.getUsername());
        // Load the parking lot view for the logged-in user
        loadParkingLotView();
        // Update UI elements
        statusLabel.setText("Logged in as: " + user.getUsername() + ". Select date/time and check availability.");
        // Show user-specific buttons, hide admin login?
        if (historyButton != null) { historyButton.setVisible(true); historyButton.setManaged(true); }
        if (logoutButton != null) { logoutButton.setVisible(true); logoutButton.setManaged(true); }
        if (adminLoginButton != null) { adminLoginButton.setVisible(false); adminLoginButton.setManaged(false); } // Hide admin login
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
    private void handleLogout() {
        System.out.println("User logging out: " + (currentLoggedInUser != null ? currentLoggedInUser.getUsername() : "null"));
        currentLoggedInUser = null;
        // Hide user-specific buttons, show admin login
        if (historyButton != null) { historyButton.setVisible(false); historyButton.setManaged(false); }
        if (logoutButton != null) { logoutButton.setVisible(false); logoutButton.setManaged(false); }
        if (adminLoginButton != null) { adminLoginButton.setVisible(true); adminLoginButton.setManaged(true); } // Show admin login
        // Return to login screen
        loadUserLoginView();
        statusLabel.setText("Logged out. Please log in or register.");
    }


    @FXML
    private void handleAdminLogin() {
        // Keep existing admin login logic
        System.out.println("Admin Login button clicked!");
        try {
            // Load the admin login FXML file
            FXMLLoader loader = new FXMLLoader();
            URL fxmlUrl = getClass().getResource("/com/smartparking/view/AdminLoginView.fxml");
             if (fxmlUrl == null) {
                System.err.println("Cannot find AdminLoginView.fxml");
                showAlert(AlertType.ERROR, "Error", "Could not load the admin login screen.");
                return;
            }
            loader.setLocation(fxmlUrl);
            Parent loginRoot = loader.load();

            // Create a new stage (window) for the login dialog
            Stage loginStage = new Stage();
            loginStage.setTitle("Admin Login");
            loginStage.initModality(Modality.APPLICATION_MODAL); // Block interaction with main window
            Window owner = statusLabel.getScene().getWindow(); // Get owner window
            loginStage.initOwner(owner);
            loginStage.setScene(new Scene(loginRoot));

             // Apply CSS if needed (optional, could inherit or have specific styles)
             URL cssUrl = getClass().getResource("/com/smartparking/css/styles.css");
             if (cssUrl != null) {
                 loginStage.getScene().getStylesheets().add(cssUrl.toExternalForm());
             }

            // Show the dialog and wait for it to be closed
            loginStage.showAndWait();

            // After the dialog is closed, check if login was successful
            AdminLoginController loginController = loader.getController();
            if (loginController.isLoginSuccessful()) {
                statusLabel.setText("Admin login successful. Loading admin panel...");
                System.out.println("Proceeding to admin panel...");
                // TODO: Load the Admin Panel View here
                loadAdminPanelView(); // Placeholder for the next step
            } else {
                statusLabel.setText("Admin login cancelled or failed.");
                System.out.println("Admin login was not successful.");
            }

        } catch (IOException e) {
            System.err.println("Error loading AdminLoginView.fxml: " + e.getMessage());
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Loading Error", "Could not load the admin login screen due to an error.");
        } catch (Exception e) {
             System.err.println("An unexpected error occurred during admin login: " + e.getMessage());
             e.printStackTrace();
             showAlert(AlertType.ERROR, "Unexpected Error", "An unexpected error occurred.");
        }
    }

    // handleReservation() method is removed as this logic is now in ParkingLotController


     /**
      * Loads the Admin Panel view into the center of the main BorderPane.
      * (Implementation remains the same)
      */
     private void loadAdminPanelView() {
         System.out.println("Attempting to load Admin Panel View...");
         try {
             URL adminPanelUrl = getClass().getResource("/com/smartparking/view/AdminPanelView.fxml");
             if (adminPanelUrl == null) {
                 System.err.println("Cannot find AdminPanelView.fxml resource.");
                 showAlert(AlertType.ERROR, "Load Error", "Could not find the Admin Panel view file.");
                 statusLabel.setText("Error: Admin Panel view not found.");
                 return;
             }

             // Load the admin panel FXML
             // Note: FXMLLoader.load() is static and doesn't give access to the controller easily here.
             // If controller access is needed *before* setting the view, use an instance:
             // FXMLLoader loader = new FXMLLoader(adminPanelUrl);
             // Parent adminRoot = loader.load();
             // AdminPanelController adminController = loader.getController();
             // Now you can pass data or call methods on adminController if needed.
             Parent adminRoot = FXMLLoader.load(adminPanelUrl);

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

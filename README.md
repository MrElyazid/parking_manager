# Smart Parking System (JavaFX)

## Description

This is a simple desktop application built with JavaFX to simulate a smart parking reservation system. It allows registered users to visually select and reserve parking spots for specific time slots (using ComboBoxes for time input) and view their reservation history. An admin interface is provided to manage all reservations. The application uses an SQLite database for persistence and features variable pricing based on spot type.

## Features

### User Features:
*   **User Registration & Login:** Users can register for an account and log in. Passwords are stored securely (hashed).
*   **Visual Parking Lot:** View a representation of the parking lot.
*   **Spot Availability Check:** Select a date and time range (using DatePicker and Hour/Minute ComboBoxes) to see available spots visually marked.
*   **Spot Selection:** Click on an available spot in the visual layout to select it for reservation. Tooltips show spot type and hourly rate.
*   **Variable Pricing:** Reservation cost is calculated based on the duration and the specific hourly rate associated with the selected spot type (e.g., EV spots may cost more).
*   **Reservation Confirmation:** Confirm the reservation for the selected spot and time. Data is saved to the database.
*   **Reservation History:** Logged-in users can view a table of their past and upcoming reservations.
*   **Logout:** Users can log out of their account.

### Admin Features:
*   **Admin Login:** Access the admin panel via a dedicated login button and dialog (using hardcoded credentials: `admin`/`password123`).
*   **View All Reservations:** See a list of all reservations made by all users in a table format, including calculated price.
*   **Manage Reservations:**
    *   **Refresh:** Update the list of all reservations displayed.
    *   **Delete:** Remove selected reservations from the system (database).

## Technology Stack

*   **Language:** Java 17
*   **Framework:** JavaFX 17.0.10 (using FXML for UI definition)
*   **Database:** SQLite
*   **Database Connectivity:** JDBC (via `sqlite-jdbc` driver)
*   **Build Tool:** Apache Maven 3.9+
*   **Styling:** CSS

## Project Structure

```
.
├── pom.xml                 # Maven configuration file
├── README.md               # This file
├── smartparking.db         # SQLite database file (created on first run)
└── src
    └── main
        ├── java
        │   └── com
        │       └── smartparking
        │           ├── controller    # FXML Controller classes
        │           │   ├── AdminLoginController.java
        │           │   ├── AdminPanelController.java
        │           │   ├── MainController.java
        │           │   ├── ParkingLotController.java
        │           │   ├── UserHistoryController.java
        │           │   ├── UserLoginController.java
        │           │   └── UserRegisterController.java
        │           ├── model         # Data model classes
        │           │   ├── ParkingSpot.java
        │           │   ├── Reservation.java
        │           │   └── User.java
        │           ├── service       # Business logic/data access (JDBC)
        │           │   ├── ReservationService.java
        │           │   ├── SpotService.java
        │           │   └── UserService.java
        │           ├── util          # Utility classes
        │           │   └── DatabaseManager.java
        │           └── MainApp.java  # Main application entry point
        └── resources
            └── com
                └── smartparking
                    ├── css           # CSS stylesheets
                    │   └── styles.css
                    └── view          # FXML view files
                        ├── AdminLoginView.fxml
                        ├── AdminPanelView.fxml
                        ├── MainView.fxml
                        ├── ParkingLotView.fxml
                        ├── UserHistoryView.fxml
                        ├── UserLoginView.fxml
                        └── UserRegisterView.fxml
```

*   **`com.smartparking`**: Root package.
    *   **`MainApp.java`**: Entry point of the JavaFX application. Initializes the database and loads the initial view.
    *   **`controller`**: Contains controller classes that handle UI logic and events for their corresponding FXML views.
    *   **`model`**: Contains Plain Old Java Objects (POJOs) representing the data structures (`User`, `ParkingSpot`, `Reservation`).
    *   **`service`**: Contains classes responsible for business logic and data management, interacting with the database via JDBC (`UserService`, `SpotService`, `ReservationService`).
    *   **`util`**: Contains utility classes like `DatabaseManager` for handling DB connection and setup.
    *   **`resources/com/smartparking/view`**: Contains FXML files defining the UI layout.
    *   **`resources/com/smartparking/css`**: Contains CSS files for styling the UI.
*   **`smartparking.db`**: The SQLite database file created automatically in the project root directory.

## How to Run

1.  **Prerequisites:**
    *   Java Development Kit (JDK) 17 or later installed.
    *   Apache Maven installed.
2.  **Clone the Repository (if applicable):**
    ```bash
    git clone <repository-url>
    cd <repository-directory>
    ```
3.  **Build and Run using Maven:**
    Open a terminal in the project's root directory (where `pom.xml` is located) and run the following command:
    ```bash
    mvn clean javafx:run
    ```
    This command will:
    *   Clean the project (remove previous builds).
    *   Download necessary dependencies (including the SQLite driver).
    *   Compile the source code.
    *   Create/update the `smartparking.db` file and tables if they don't exist. **Note:** If you encounter database schema errors after modifying the code (e.g., "no such column"), delete the existing `smartparking.db` file and run the command again to recreate the database with the latest schema.
    *   Run the JavaFX application.

## Credentials

*   **Admin:**
    *   Username: `admin`
    *   Password: `password123`
*   **Regular Users:** Register a new account through the application's registration screen.

## Future Improvements

*   Implement password strength indicators and more robust hashing (e.g., BCrypt).
*   Add functionality for users to cancel their own upcoming reservations.
*   Enhance the admin panel to manage parking spots (add/edit/delete).
*   Implement different pricing based on time of day or longer reservation durations (daily/weekly rates).
*   Add more detailed reporting/analytics for the admin (e.g., using JavaFX Charts).
*   Refine UI/UX further (e.g., better visual feedback, loading indicators, icons for spot types).
*   Add unit and integration tests.
*   Consider using a connection pool for database connections in a higher-load scenario.
*   Implement Vehicle Management (allow users to register vehicles and link them to reservations).
*   Support for Multiple Parking Lots/Levels.

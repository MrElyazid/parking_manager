# Smart Parking System (JavaFX)

## Description

This is a simple desktop application built with JavaFX to simulate a smart parking reservation system. It allows regular users to reserve parking spots and provides an admin interface to manage these reservations. This project serves as a learning exercise focusing on JavaFX, FXML, MVC-like patterns, and basic UI design.

## Features

### User Features:
*   **Reserve Parking:** Select a date and duration (in hours) for a parking spot reservation.
*   **Price Calculation:** Automatically calculates the estimated reservation price based on the duration (using a fixed hourly rate).
*   **Confirmation:** Displays a confirmation message with reservation details upon successful booking.

### Admin Features:
*   **Admin Login:** Access the admin panel via a dedicated login button and dialog (using hardcoded credentials for this demo).
*   **View Reservations:** See a list of all current reservations in a table format, including ID, date, duration, price, and creation time.
*   **Manage Reservations:**
    *   **Refresh:** Update the list of reservations displayed.
    *   **Delete:** Remove selected reservations from the system.

## Technology Stack

*   **Language:** Java 17
*   **Framework:** JavaFX 17.0.10 (using FXML for UI definition)
*   **Build Tool:** Apache Maven 3.9+
*   **Styling:** CSS

## Project Structure

```
.
├── pom.xml                 # Maven configuration file
├── README.md               # This file
└── src
    └── main
        ├── java
        │   └── com
        │       └── smartparking
        │           ├── controller    # FXML Controller classes
        │           │   ├── AdminLoginController.java
        │           │   ├── AdminPanelController.java
        │           │   └── MainController.java
        │           ├── model         # Data model classes
        │           │   └── Reservation.java
        │           ├── service       # Business logic/data access
        │           │   └── ReservationService.java
        │           └── MainApp.java  # Main application entry point
        └── resources
            └── com
                └── smartparking
                    ├── css           # CSS stylesheets
                    │   └── styles.css
                    └── view          # FXML view files
                        ├── AdminLoginView.fxml
                        ├── AdminPanelView.fxml
                        └── MainView.fxml
```

*   **`com.smartparking`**: Root package.
    *   **`MainApp.java`**: Entry point of the JavaFX application. Sets up the primary stage and loads the initial view.
    *   **`controller`**: Contains controller classes that handle UI logic and events for their corresponding FXML views.
    *   **`model`**: Contains Plain Old Java Objects (POJOs) representing the data structures (e.g., `Reservation`).
    *   **`service`**: Contains classes responsible for business logic and data management (e.g., `ReservationService` for in-memory storage).
    *   **`resources/com/smartparking/view`**: Contains FXML files defining the UI layout.
    *   **`resources/com/smartparking/css`**: Contains CSS files for styling the UI.

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
    *   Compile the source code.
    *   Download necessary dependencies.
    *   Run the JavaFX application.

## Admin Credentials

For demonstration purposes, the admin login credentials are hardcoded:
*   **Username:** `admin`
*   **Password:** `password123`

## Future Improvements

*   Implement actual database persistence instead of in-memory storage.
*   Add user authentication for regular users.
*   Implement spot selection/availability logic.
*   Add editing functionality for reservations in the admin panel.
*   Improve date/time and currency formatting in the table view.
*   Refine UI/UX further.
*   Add unit and integration tests.

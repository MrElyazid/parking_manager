# Smart Parking System - Sequence Diagram: Admin Views Reservations

This diagram shows the flow when an admin logs in and views the reservations.

```mermaid
sequenceDiagram
    actor Admin
    participant MC as MainController
    participant ALC as AdminLoginController
    participant APC as AdminPanelController
    participant RS as ReservationService
    participant DB as DatabaseManager

    Admin->>MC: Clicks Admin Login Button
    MC->>MC: handleAdminLogin()
    MC->>ALC: Loads AdminLoginView.fxml & Shows Stage
    Admin->>ALC: Enters Credentials
    Admin->>ALC: Clicks Login Button
    ALC->>ALC: handleLogin() (Checks hardcoded credentials)
    ALC->>ALC: closeDialog()
    MC->>ALC: isLoginSuccessful()
    ALC-->>MC: true
    MC->>MC: loadAdminPanelView()
    MC->>APC: Loads AdminPanelView.fxml
    APC->>APC: initialize()
    APC->>APC: loadReservationData()
    APC->>RS: getAllReservations()
    RS->>DB: getConnection()
    DB-->>RS: Connection
    RS->>DB: Executes SQL (SELECT * FROM Reservations)
    DB-->>RS: ResultSet (Reservations)
    RS-->>APC: List~Reservation~
    APC->>APC: Populates TableView
    APC-->>Admin: Displays Admin Panel with Reservations

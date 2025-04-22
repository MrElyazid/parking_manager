# Smart Parking System - Class Diagram

This diagram shows the main classes in the application and their relationships (associations, dependencies).

```mermaid
classDiagram
    direction LR

    class MainApp {
        +start(Stage)
        +main(String[])
    }

    class MainController {
        -currentLoggedInUser : User
        -reservationService : ReservationService
        -mainBorderPane : BorderPane
        -statusLabel : Label
        -historyButton : Button
        -logoutButton : Button
        -adminLoginButton : Button
        +initialize()
        +loadUserLoginView()
        +loadUserRegisterView()
        +loadParkingLotView()
        +loadUserHistoryView()
        +handleUserLoginSuccess(User)
        +handleLogout()
        +handleShowHistory()
        +handleAdminLogin()
        +showAlert(AlertType, String, String)
        -loadView(String, boolean)
        -loadAdminPanelView()
    }

    class UserLoginController {
        -userService : UserService
        -mainController : MainController
        -loggedInUser : User
        -usernameField : TextField
        -passwordField : PasswordField
        -messageLabel : Label
        +setMainController(MainController)
        +initialize()
        +handleLogin()
        +handleGoToRegister()
        -showMessage(String, Color)
        +getLoggedInUser() : User
    }

     class UserRegisterController {
        -userService : UserService
        -mainController : MainController
        -usernameField : TextField
        -passwordField : PasswordField
        -confirmPasswordField : PasswordField
        -messageLabel : Label
        +setMainController(MainController)
        +initialize()
        +handleRegister()
        +handleGoToLogin()
        -showMessage(String, Color)
        -clearFields()
    }

    class ParkingLotController {
        -spotService : SpotService
        -reservationService : ReservationService
        -mainController : MainController
        -currentUser : User
        -selectedSpot : ParkingSpot
        -selectedStartTime : LocalDateTime
        -selectedEndTime : LocalDateTime
        -selectedSpotButton : Button
        -buttonSpotMap : Map~Button, ParkingSpot~
        -datePicker : DatePicker
        -startHourCombo : ComboBox
        -startMinuteCombo : ComboBox
        -endHourCombo : ComboBox
        -endMinuteCombo : ComboBox
        -parkingGrid : TilePane
        -selectedSpotLabel : Label
        -confirmReservationButton : Button
        +setMainController(MainController)
        +setCurrentUser(User)
        +initialize()
        -populateTimeCombos()
        -handleDateTimeChange(ActionEvent)
        -handleCheckAvailability()
        -populateParkingGrid(LocalDateTime, LocalDateTime)
        -handleSpotSelection(Button, ParkingSpot)
        -handleConfirmReservation()
        -clearParkingGridAndSelection()
        -showAlert(AlertType, String, String)
    }

    class UserHistoryController {
        -reservationService : ReservationService
        -mainController : MainController
        -currentUser : User
        -userReservations : ObservableList~Reservation~
        -historyTable : TableView~Reservation~
        +setMainController(MainController)
        +setCurrentUser(User)
        +initialize()
        -loadHistory()
        -handleRefresh()
        -handleGoToReservation()
        -showAlert(AlertType, String, String)
    }

     class AdminLoginController {
        -loginSuccessful : boolean
        -usernameField : TextField
        -passwordField : PasswordField
        -errorMessageLabel : Label
        +initialize()
        +handleLogin()
        +handleCancel()
        -closeDialog()
        +isLoginSuccessful() : boolean
    }

    class AdminPanelController {
        -reservationService : ReservationService
        -reservationData : ObservableList~Reservation~
        -reservationsTable : TableView~Reservation~
        +initialize()
        -loadReservationData()
        -handleRefresh()
        -handleDelete()
        -showReservationDetails(Reservation)
        -showAlert(AlertType, String, String)
    }

    class UserService {
        +registerUser(String, String) : boolean
        +authenticateUser(String, String) : Optional~User~
        +findUserByUsername(String) : Optional~User~
        -hashPassword(String) : String
    }

    class SpotService {
        +getAllParkingSpots() : List~ParkingSpot~
        +addParkingSpot(ParkingSpot) : boolean
        +deleteParkingSpot(String) : boolean
    }

    class ReservationService {
        +isSpotAvailable(String, LocalDateTime, LocalDateTime) : boolean
        +addReservation(Reservation) : Optional~Integer~
        +getAllReservations() : List~Reservation~
        +getReservationsForUser(int) : List~Reservation~
        +deleteReservation(int) : boolean
        -mapResultSetToReservation(ResultSet) : Reservation
        -parseDateTime(String) : LocalDateTime
    }

    class DatabaseManager {
        +$DB_URL : String
        +$getConnection() : Connection
        +$initializeDatabase()
        -$addDefaultSpots(Connection)
    }

    class User {
        -userId : int
        -username : String
        +User(int, String)
        +getUserId() : int
        +getUsername() : String
    }

    class ParkingSpot {
        -spotId : String
        -locationInfo : String
        -type : String
        -hourlyRate : double
        +ParkingSpot(String, String, String, double)
        +getSpotId() : String
        +getLocationInfo() : String
        +getType() : String
        +getHourlyRate() : double
    }

    class Reservation {
        -reservationId : int
        -userId : int
        -spotId : String
        -startTime : LocalDateTime
        -endTime : LocalDateTime
        -calculatedPrice : double
        -creationTimestamp : LocalDateTime
        +Reservation(int, String, LocalDateTime, LocalDateTime, double)
        +Reservation(int, int, String, LocalDateTime, LocalDateTime, double, LocalDateTime)
        +getReservationId() : int
        +getUserId() : int
        +getSpotId() : String
        +getStartTime() : LocalDateTime
        +getFormattedStartTime() : String
        +getEndTime() : LocalDateTime
        +getFormattedEndTime() : String
        +getCalculatedPrice() : double
        +getCreationTimestamp() : LocalDateTime
        +getFormattedCreationTimestamp() : String
    }

    %% Relationships
    MainApp ..> MainController : creates/loads
    MainController ..> UserLoginController : loads/sets ref
    MainController ..> UserRegisterController : loads/sets ref
    MainController ..> ParkingLotController : loads/sets ref
    MainController ..> UserHistoryController : loads/sets ref
    MainController ..> AdminLoginController : loads/shows
    MainController ..> AdminPanelController : loads

    UserLoginController ..> MainController : calls methods
    UserLoginController ..> UserService : uses
    UserRegisterController ..> MainController : calls methods
    UserRegisterController ..> UserService : uses
    ParkingLotController ..> MainController : calls methods
    ParkingLotController ..> SpotService : uses
    ParkingLotController ..> ReservationService : uses
    UserHistoryController ..> MainController : calls methods
    UserHistoryController ..> ReservationService : uses
    AdminPanelController ..> ReservationService : uses

    UserService ..> DatabaseManager : uses
    UserService ..> User : creates/returns
    SpotService ..> DatabaseManager : uses
    SpotService ..> ParkingSpot : creates/returns
    ReservationService ..> DatabaseManager : uses
    ReservationService ..> Reservation : creates/returns

    ParkingLotController ..> User : uses
    ParkingLotController ..> ParkingSpot : uses
    ParkingLotController ..> Reservation : creates
    UserHistoryController ..> User : uses
    UserHistoryController ..> Reservation : uses
    AdminPanelController ..> Reservation : uses

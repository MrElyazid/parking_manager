# Smart Parking System - Sequence Diagram: User Makes Reservation

This diagram illustrates the sequence of calls when a logged-in user successfully makes a reservation.

```mermaid
sequenceDiagram
    actor User
    participant PLC as ParkingLotController
    participant SS as SpotService
    participant RS as ReservationService
    participant DB as DatabaseManager
    participant PS as ParkingSpot
    participant R as Reservation

    User->>PLC: Selects Date/Time
    User->>PLC: Clicks Check Availability
    PLC->>SS: getAllParkingSpots()
    SS->>DB: getConnection()
    DB-->>SS: Connection
    SS->>DB: Executes SQL (SELECT * FROM ParkingSpots)
    DB-->>SS: ResultSet (Spots)
    SS-->>PLC: List~ParkingSpot~
    loop for each spot
        PLC->>RS: isSpotAvailable(spotId, start, end)
        RS->>DB: getConnection()
        DB-->>RS: Connection
        RS->>DB: Executes SQL (SELECT COUNT(*) FROM Reservations WHERE overlap)
        DB-->>RS: ResultSet (Count)
        RS-->>PLC: boolean (isAvailable)
    end
    PLC->>PLC: populateParkingGrid() (Updates UI)
    User->>PLC: Clicks available spot Button
    PLC->>PLC: handleSpotSelection(button, spot) (Updates UI state)
    User->>PLC: Clicks Confirm Reservation
    PLC->>PLC: handleConfirmReservation()
    PLC->>RS: isSpotAvailable(spotId, start, end)  // Double check
    RS-->>PLC: boolean (true)
    PLC->>PS: getHourlyRate()
    PS-->>PLC: rate
    PLC->>PLC: Calculate Price
    PLC->>R: new Reservation(userId, spotId, start, end, price)
    PLC->>RS: addReservation(Reservation)
    RS->>DB: getConnection()
    DB-->>RS: Connection
    RS->>DB: Executes SQL (INSERT INTO Reservations)
    DB-->>RS: Generated ID
    RS-->>PLC: Optional~Integer~ (reservationId)
    PLC->>PLC: populateParkingGrid() (Refreshes UI)
    PLC->>User: Shows Confirmation Alert

package tests;

import model.Flight;
import model.Passenger;
import model.Reservation;
import service.ReservationManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Reservation Manager Test Suite")
public class ReservationManagerTest {
    
    private ReservationManager reservationManager;
    private Flight testFlight;
    private LocalDateTime baseDate;
    
    @BeforeEach
    void setUp() {
        reservationManager = new ReservationManager();
        baseDate = LocalDateTime.of(2025, 12, 15, 10, 0);
        testFlight = createFlight("FL001", "NYC", "LAX", 100, 200.0);
    }
    
    // Helper methods
    private Flight createFlight(String id, String source, String dest, int seats, double price) {
        LocalDateTime departure = baseDate;
        LocalDateTime arrival = departure.plusHours(5);
        return new Flight(id, source, dest, departure, arrival, seats, price, "Delta");
    }
    
    // FIXED: Updated to match Passenger constructor parameters
    private Passenger createPassenger(String passengerId, String firstName, String lastName, String email, String phoneNumber, int age) {
        return new Passenger(passengerId, firstName, lastName, email, phoneNumber, age);
    }
    
    // ==================== CREATE RESERVATION - ECP TESTS ====================
    
    @Test
    @Tag("fast")
    @DisplayName("ECP - Create Reservation with Valid Flight")
    void testCreateReservation_ValidFlight() {
        Reservation reservation = reservationManager.createReservation(testFlight);
        
        assertNotNull(reservation);
        assertNotNull(reservation.getReservationId());
        assertTrue(reservation.getReservationId().startsWith("RES"));
        assertEquals(testFlight, reservation.getFlight());
        assertEquals("PENDING", reservation.getStatus());
        assertEquals(1, reservationManager.getTotalReservations());
    }
    
    @Test
    @Tag("fast")
    @DisplayName("ECP - Create Reservation with Null Flight")
    void testCreateReservation_NullFlight() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> reservationManager.createReservation(null)
        );
        assertEquals("Flight cannot be null", exception.getMessage());
    }
    
    @Test
    @Tag("slow")
    @DisplayName("ECP - Create Reservation with No Available Seats")
    void testCreateReservation_NoSeatsAvailable() {
        testFlight.bookSeats(100); // Book all seats
        
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> reservationManager.createReservation(testFlight)
        );
        assertEquals("No available seats on this flight", exception.getMessage());
    }
    
    @Test
    @Tag("fast")
    @DisplayName("ECP - Create Multiple Reservations")
    void testCreateReservation_Multiple() {
        Reservation res1 = reservationManager.createReservation(testFlight);
        Reservation res2 = reservationManager.createReservation(testFlight);
        Reservation res3 = reservationManager.createReservation(testFlight);
        
        assertEquals(3, reservationManager.getTotalReservations());
        assertNotEquals(res1.getReservationId(), res2.getReservationId());
        assertNotEquals(res2.getReservationId(), res3.getReservationId());
    }
    
    @Test
    @Tag("slow")
    @DisplayName("ECP - Create Reservation ID Sequential")
    void testCreateReservation_SequentialIds() {
        Reservation res1 = reservationManager.createReservation(testFlight);
        Reservation res2 = reservationManager.createReservation(testFlight);
        
        assertTrue(res1.getReservationId().startsWith("RES"));
        assertTrue(res2.getReservationId().startsWith("RES"));
        
        int id1 = Integer.parseInt(res1.getReservationId().substring(3));
        int id2 = Integer.parseInt(res2.getReservationId().substring(3));
        
        assertEquals(id1 + 1, id2);
    }
    
    // ==================== ADD PASSENGER - ECP TESTS ====================
    
    @Test
    @Tag("fast")
    @DisplayName("ECP - Add Passenger to Reservation")
    void testAddPassengerToReservation_Valid() {
        Reservation reservation = reservationManager.createReservation(testFlight);
        Passenger passenger = createPassenger("P123456", "John", "Doe", "john@example.com", "1234567890", 30);
        
        assertDoesNotThrow(() -> reservationManager.addPassengerToReservation(reservation, passenger));
        assertEquals(1, reservation.getNumberOfPassengers());
        assertEquals(200.0, reservation.getTotalPrice());
    }
    
    @Test
    @Tag("slow")
    @DisplayName("ECP - Add Multiple Passengers")
    void testAddPassengerToReservation_Multiple() {
        Reservation reservation = reservationManager.createReservation(testFlight);
        Passenger passenger1 = createPassenger("P123456", "John", "Doe", "john@example.com", "1234567890", 30);
        Passenger passenger2 = createPassenger("P789012", "Jane", "Smith", "jane@example.com", "0987654321", 28);
        
        reservationManager.addPassengerToReservation(reservation, passenger1);
        reservationManager.addPassengerToReservation(reservation, passenger2);
        
        assertEquals(2, reservation.getNumberOfPassengers());
        assertEquals(400.0, reservation.getTotalPrice());
    }
    
    @Test
    @Tag("fast")
    @DisplayName("ECP - Add Passenger to Null Reservation")
    void testAddPassengerToReservation_NullReservation() {
        Passenger passenger = createPassenger("P123456", "John", "Doe", "john@example.com", "1234567890", 30);
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> reservationManager.addPassengerToReservation(null, passenger)
        );
        assertEquals("Reservation cannot be null", exception.getMessage());
    }
    
    @Test
    @Tag("fast")
    @DisplayName("ECP - Add Null Passenger to Reservation")
    void testAddPassengerToReservation_NullPassenger() {
        Reservation reservation = reservationManager.createReservation(testFlight);
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> reservationManager.addPassengerToReservation(reservation, null)
        );
        assertEquals("Passenger cannot be null", exception.getMessage());
    }
    
    @Test
    @Tag("slow")
    @DisplayName("ECP - Add Passenger to Confirmed Reservation")
    void testAddPassengerToReservation_ConfirmedReservation() {
        Reservation reservation = reservationManager.createReservation(testFlight);
        Passenger passenger1 = createPassenger("P123456", "John", "Doe", "john@example.com", "1234567890", 30);
        reservationManager.addPassengerToReservation(reservation, passenger1);
        reservationManager.confirmReservation(reservation, "PAY001");
        
        Passenger passenger2 = createPassenger("P789012", "Jane", "Smith", "jane@example.com", "0987654321", 28);
        
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> reservationManager.addPassengerToReservation(reservation, passenger2)
        );
        assertEquals("Cannot modify confirmed reservation", exception.getMessage());
    }
    
    // ==================== REMOVE PASSENGER - ECP TESTS ====================
    
    @Test
    @Tag("fast")
    @DisplayName("ECP - Remove Passenger from Reservation")
    void testRemovePassengerFromReservation_Valid() {
        Reservation reservation = reservationManager.createReservation(testFlight);
        Passenger passenger1 = createPassenger("P123456", "John", "Doe", "john@example.com", "1234567890", 30);
        Passenger passenger2 = createPassenger("P789012", "Jane", "Smith", "jane@example.com", "0987654321", 28);
        
        reservationManager.addPassengerToReservation(reservation, passenger1);
        reservationManager.addPassengerToReservation(reservation, passenger2);
        assertEquals(2, reservation.getNumberOfPassengers());
        
        reservationManager.removePassengerFromReservation(reservation, passenger1);
        assertEquals(1, reservation.getNumberOfPassengers());
        assertEquals(200.0, reservation.getTotalPrice());
    }
    
    @Test
    @Tag("fast")
    @DisplayName("ECP - Remove Passenger from Null Reservation")
    void testRemovePassengerFromReservation_NullReservation() {
        Passenger passenger = createPassenger("P123456", "John", "Doe", "john@example.com", "1234567890", 30);
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> reservationManager.removePassengerFromReservation(null, passenger)
        );
        assertEquals("Reservation cannot be null", exception.getMessage());
    }
    
    @Test
    @Tag("fast")
    @DisplayName("ECP - Remove Null Passenger from Reservation")
    void testRemovePassengerFromReservation_NullPassenger() {
        Reservation reservation = reservationManager.createReservation(testFlight);
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> reservationManager.removePassengerFromReservation(reservation, null)
        );
        assertEquals("Passenger cannot be null", exception.getMessage());
    }
    
    @Test
    @Tag("slow")
    @DisplayName("ECP - Remove Passenger from Confirmed Reservation")
    void testRemovePassengerFromReservation_ConfirmedReservation() {
        Reservation reservation = reservationManager.createReservation(testFlight);
        Passenger passenger = createPassenger("P123456", "John", "Doe", "john@example.com", "1234567890", 30);
        reservationManager.addPassengerToReservation(reservation, passenger);
        reservationManager.confirmReservation(reservation, "PAY001");
        
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> reservationManager.removePassengerFromReservation(reservation, passenger)
        );
        assertEquals("Cannot modify confirmed reservation", exception.getMessage());
    }
    
    // ==================== CONFIRM RESERVATION - ECP & BVA TESTS ====================
    
    @Test
    @Tag("slow")
    @DisplayName("ECP - Confirm Reservation with Valid Payment")
    void testConfirmReservation_Valid() {
        Reservation reservation = reservationManager.createReservation(testFlight);
        Passenger passenger = createPassenger("P123456", "John", "Doe", "john@example.com", "1234567890", 30);
        reservationManager.addPassengerToReservation(reservation, passenger);
        
        int initialSeats = testFlight.getAvailableSeats();
        boolean confirmed = reservationManager.confirmReservation(reservation, "PAY001");
        
        assertTrue(confirmed);
        assertTrue(reservation.isConfirmed());
        assertEquals("CONFIRMED", reservation.getStatus());
        assertEquals("PAY001", reservation.getPaymentId());
        assertEquals(initialSeats - 1, testFlight.getAvailableSeats());
        assertEquals(1, reservationManager.getConfirmedReservations());
    }
    
    @Test
    @Tag("fast")
    @DisplayName("ECP - Confirm Reservation with Null Reservation")
    void testConfirmReservation_NullReservation() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> reservationManager.confirmReservation(null, "PAY001")
        );
        assertEquals("Reservation cannot be null", exception.getMessage());
    }
    
    @Test
    @Tag("fast")
    @DisplayName("ECP - Confirm Reservation with Null Payment ID")
    void testConfirmReservation_NullPaymentId() {
        Reservation reservation = reservationManager.createReservation(testFlight);
        Passenger passenger = createPassenger("P123456", "John", "Doe", "john@example.com", "1234567890", 30);
        reservationManager.addPassengerToReservation(reservation, passenger);
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> reservationManager.confirmReservation(reservation, null)
        );
        assertEquals("Payment ID cannot be empty", exception.getMessage());
    }
    
    @Test
    @Tag("fast")
    @DisplayName("ECP - Confirm Reservation with Empty Payment ID")
    void testConfirmReservation_EmptyPaymentId() {
        Reservation reservation = reservationManager.createReservation(testFlight);
        Passenger passenger = createPassenger("P123456", "John", "Doe", "john@example.com", "1234567890", 30);
        reservationManager.addPassengerToReservation(reservation, passenger);
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> reservationManager.confirmReservation(reservation, "   ")
        );
        assertEquals("Payment ID cannot be empty", exception.getMessage());
    }
    
    @Test
    @Tag("slow")
    @DisplayName("ECP - Confirm Reservation Without Passengers")
    void testConfirmReservation_NoPassengers() {
        Reservation reservation = reservationManager.createReservation(testFlight);
        
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> reservationManager.confirmReservation(reservation, "PAY001")
        );
        assertEquals("Cannot confirm reservation without passengers", exception.getMessage());
    }
    
    @Test
    @Tag("fast")
    @DisplayName("BVA - Confirm Reservation with One Passenger")
    void testConfirmReservation_OnePassenger() {
        Reservation reservation = reservationManager.createReservation(testFlight);
        Passenger passenger = createPassenger("P123456", "John", "Doe", "john@example.com", "1234567890", 30);
        reservationManager.addPassengerToReservation(reservation, passenger);
        
        boolean confirmed = reservationManager.confirmReservation(reservation, "PAY001");
        
        assertTrue(confirmed);
        assertEquals(1, reservation.getNumberOfPassengers());
    }
    
    @Test
    @Tag("slow")
    @DisplayName("BVA - Confirm Reservation with Maximum Passengers")
    void testConfirmReservation_MaxPassengers() {
        Flight smallFlight = createFlight("FL002", "NYC", "LAX", 3, 200.0);
        Reservation reservation = reservationManager.createReservation(smallFlight);
        
        Passenger p1 = createPassenger("P123456", "John", "Doe", "john@example.com", "1234567890", 30);
        Passenger p2 = createPassenger("P789012", "Jane", "Smith", "jane@example.com", "0987654321", 28);
        Passenger p3 = createPassenger("P345678", "Bob", "Johnson", "bob@example.com", "5555555555", 35);
        
        reservationManager.addPassengerToReservation(reservation, p1);
        reservationManager.addPassengerToReservation(reservation, p2);
        reservationManager.addPassengerToReservation(reservation, p3);
        
        boolean confirmed = reservationManager.confirmReservation(reservation, "PAY001");
        
        assertTrue(confirmed);
        assertEquals(0, smallFlight.getAvailableSeats());
    }
    
    @Test
    @Tag("slow")
    @DisplayName("BVA - Confirm Reservation Exceeding Available Seats")
    void testConfirmReservation_ExceedingSeats() {
        Flight smallFlight = createFlight("FL002", "NYC", "LAX", 2, 200.0);
        Reservation reservation = reservationManager.createReservation(smallFlight);
        
        Passenger p1 = createPassenger("P123456", "John", "Doe", "john@example.com", "1234567890", 30);
        Passenger p2 = createPassenger("P789012", "Jane", "Smith", "jane@example.com", "0987654321", 28);
        Passenger p3 = createPassenger("P345678", "Bob", "Johnson", "bob@example.com", "5555555555", 35);
        
        reservationManager.addPassengerToReservation(reservation, p1);
        reservationManager.addPassengerToReservation(reservation, p2);
        reservationManager.addPassengerToReservation(reservation, p3);
        
        boolean confirmed = reservationManager.confirmReservation(reservation, "PAY001");
        
        assertFalse(confirmed);
        assertEquals("PENDING", reservation.getStatus());
    }
    
    // ==================== CANCEL RESERVATION - ECP TESTS ====================
    
    @Test
    @Tag("slow")
    @DisplayName("ECP - Cancel Confirmed Reservation")
    void testCancelReservation_Valid() {
        Reservation reservation = reservationManager.createReservation(testFlight);
        Passenger passenger = createPassenger("P123456", "John", "Doe", "john@example.com", "1234567890", 30);
        reservationManager.addPassengerToReservation(reservation, passenger);
        reservationManager.confirmReservation(reservation, "PAY001");
        
        int seatsBeforeCancel = testFlight.getAvailableSeats();
        boolean cancelled = reservationManager.cancelReservation(reservation.getReservationId());
        
        assertTrue(cancelled);
        assertEquals("CANCELLED", reservation.getStatus());
        assertEquals(seatsBeforeCancel + 1, testFlight.getAvailableSeats());
    }
    
    @Test
    @Tag("fast")
    @DisplayName("ECP - Cancel Non-existent Reservation")
    void testCancelReservation_NonExistent() {
        boolean cancelled = reservationManager.cancelReservation("RES9999");
        assertFalse(cancelled);
    }
    
    @Test
    @Tag("fast")
    @DisplayName("ECP - Cancel with Null Reservation ID")
    void testCancelReservation_NullId() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> reservationManager.cancelReservation(null)
        );
        assertEquals("Reservation ID cannot be empty", exception.getMessage());
    }
    
    @Test
    @Tag("fast")
    @DisplayName("ECP - Cancel with Empty Reservation ID")
    void testCancelReservation_EmptyId() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> reservationManager.cancelReservation("   ")
        );
        assertEquals("Reservation ID cannot be empty", exception.getMessage());
    }
    
    @Test
    @Tag("slow")
    @DisplayName("ECP - Cancel Unconfirmed Reservation")
    void testCancelReservation_Unconfirmed() {
        Reservation reservation = reservationManager.createReservation(testFlight);
        
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> reservationManager.cancelReservation(reservation.getReservationId())
        );
        assertEquals("Only confirmed reservations can be cancelled", exception.getMessage());
    }
    
    @Test
    @Tag("slow")
    @DisplayName("ECP - Cancel Reservation Restores Seats")
    void testCancelReservation_RestoresSeats() {
        Reservation reservation = reservationManager.createReservation(testFlight);
        Passenger p1 = createPassenger("P123456", "John", "Doe", "john@example.com", "1234567890", 30);
        Passenger p2 = createPassenger("P789012", "Jane", "Smith", "jane@example.com", "0987654321", 28);
        reservationManager.addPassengerToReservation(reservation, p1);
        reservationManager.addPassengerToReservation(reservation, p2);
        
        int initialSeats = testFlight.getAvailableSeats();
        reservationManager.confirmReservation(reservation, "PAY001");
        assertEquals(initialSeats - 2, testFlight.getAvailableSeats());
        
        reservationManager.cancelReservation(reservation.getReservationId());
        assertEquals(initialSeats, testFlight.getAvailableSeats());
    }
    
    // ==================== FIND RESERVATION BY ID - ECP TESTS ====================
    
    @Test
    @Tag("fast")
    @DisplayName("ECP - Find Reservation by Valid ID")
    void testFindReservationById_Valid() {
        Reservation reservation = reservationManager.createReservation(testFlight);
        String reservationId = reservation.getReservationId();
        
        Reservation found = reservationManager.findReservationById(reservationId);
        
        assertNotNull(found);
        assertEquals(reservationId, found.getReservationId());
        assertEquals(reservation, found);
    }
    
    @Test
    @Tag("fast")
    @DisplayName("ECP - Find Reservation by Non-existent ID")
    void testFindReservationById_NonExistent() {
        Reservation found = reservationManager.findReservationById("RES9999");
        assertNull(found);
    }
    
    @Test
    @Tag("fast")
    @DisplayName("ECP - Find Reservation with Null ID")
    void testFindReservationById_NullId() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> reservationManager.findReservationById(null)
        );
        assertEquals("Reservation ID cannot be empty", exception.getMessage());
    }
    
    @Test
    @Tag("fast")
    @DisplayName("ECP - Find Reservation with Empty ID")
    void testFindReservationById_EmptyId() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> reservationManager.findReservationById("   ")
        );
        assertEquals("Reservation ID cannot be empty", exception.getMessage());
    }
    
    // ==================== GET RESERVATIONS BY PASSENGER - ECP TESTS ====================
    
    @Test
    @Tag("slow")
    @DisplayName("ECP - Get Reservations by Passenger Email")
    void testGetReservationsByPassenger_Valid() {
        Reservation res1 = reservationManager.createReservation(testFlight);
        Reservation res2 = reservationManager.createReservation(testFlight);
        
        Passenger passenger1 = createPassenger("P123456", "John", "Doe", "john@example.com", "1234567890", 30);
        Passenger passenger2 = createPassenger("P789012", "Jane", "Smith", "jane@example.com", "0987654321", 28);
        
        reservationManager.addPassengerToReservation(res1, passenger1);
        reservationManager.addPassengerToReservation(res2, passenger1);
        reservationManager.addPassengerToReservation(res2, passenger2);
        
        List<Reservation> johnReservations = reservationManager.getReservationsByPassenger("john@example.com");
        
        assertEquals(2, johnReservations.size());
        assertTrue(johnReservations.contains(res1));
        assertTrue(johnReservations.contains(res2));
    }
    
    @Test
    @Tag("fast")
    @DisplayName("ECP - Get Reservations by Passenger No Results")
    void testGetReservationsByPassenger_NoResults() {
        Reservation reservation = reservationManager.createReservation(testFlight);
        Passenger passenger = createPassenger("P123456", "John", "Doe", "john@example.com", "1234567890", 30);
        reservationManager.addPassengerToReservation(reservation, passenger);
        
        List<Reservation> results = reservationManager.getReservationsByPassenger("unknown@example.com");
        
        assertTrue(results.isEmpty());
    }
    
    @Test
    @Tag("fast")
    @DisplayName("ECP - Get Reservations by Passenger Case Insensitive")
    void testGetReservationsByPassenger_CaseInsensitive() {
        Reservation reservation = reservationManager.createReservation(testFlight);
        Passenger passenger = createPassenger("P123456", "John", "Doe", "john@example.com", "1234567890", 30);
        reservationManager.addPassengerToReservation(reservation, passenger);
        
        List<Reservation> results = reservationManager.getReservationsByPassenger("JOHN@EXAMPLE.COM");
        
        assertEquals(1, results.size());
    }
    
    @Test
    @Tag("fast")
    @DisplayName("ECP - Get Reservations with Null Email")
    void testGetReservationsByPassenger_NullEmail() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> reservationManager.getReservationsByPassenger(null)
        );
        assertEquals("Passenger email cannot be empty", exception.getMessage());
    }
    
    @Test
    @Tag("fast")
    @DisplayName("ECP - Get Reservations with Empty Email")
    void testGetReservationsByPassenger_EmptyEmail() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> reservationManager.getReservationsByPassenger("   ")
        );
        assertEquals("Passenger email cannot be empty", exception.getMessage());
    }
    
    // ==================== STATISTICS TESTS ====================
    
    @Test
    @Tag("fast")
    @DisplayName("Statistics - Total Reservations Initial")
    void testGetTotalReservations_Initial() {
        assertEquals(0, reservationManager.getTotalReservations());
    }
    
    @Test
    @Tag("fast")
    @DisplayName("Statistics - Total Reservations After Creating")
    void testGetTotalReservations_AfterCreating() {
        reservationManager.createReservation(testFlight);
        reservationManager.createReservation(testFlight);
        reservationManager.createReservation(testFlight);
        
        assertEquals(3, reservationManager.getTotalReservations());
    }
    
    @Test
    @Tag("slow")
    @DisplayName("Statistics - Confirmed Reservations")
    void testGetConfirmedReservations() {
        Reservation res1 = reservationManager.createReservation(testFlight);
        Reservation res2 = reservationManager.createReservation(testFlight);
        Reservation res3 = reservationManager.createReservation(testFlight);
        
        Passenger p1 = createPassenger("P123456", "John", "Doe", "john@example.com", "1234567890", 30);
        Passenger p2 = createPassenger("P789012", "Jane", "Smith", "jane@example.com", "0987654321", 28);
        
        reservationManager.addPassengerToReservation(res1, p1);
        reservationManager.addPassengerToReservation(res2, p2);
        reservationManager.addPassengerToReservation(res3, p1);
        
        reservationManager.confirmReservation(res1, "PAY001");
        reservationManager.confirmReservation(res2, "PAY002");
        
        assertEquals(3, reservationManager.getTotalReservations());
        assertEquals(2, reservationManager.getConfirmedReservations());
    }
    
    @Test
    @Tag("slow")
    @DisplayName("Statistics - Confirmed After Cancellation")
    void testGetConfirmedReservations_AfterCancellation() {
        Reservation res1 = reservationManager.createReservation(testFlight);
        Reservation res2 = reservationManager.createReservation(testFlight);
        
        Passenger p1 = createPassenger("P123456", "John", "Doe", "john@example.com", "1234567890", 30);
        Passenger p2 = createPassenger("P789012", "Jane", "Smith", "jane@example.com", "0987654321", 28);
        
        reservationManager.addPassengerToReservation(res1, p1);
        reservationManager.addPassengerToReservation(res2, p2);
        
        reservationManager.confirmReservation(res1, "PAY001");
        reservationManager.confirmReservation(res2, "PAY002");
        
        assertEquals(2, reservationManager.getConfirmedReservations());
        
        reservationManager.cancelReservation(res1.getReservationId());
        
        // Cancelled reservations are still counted as "confirmed" status changes to "CANCELLED"
        assertEquals(1, reservationManager.getConfirmedReservations());
    }
    
    // ==================== PARAMETERIZED TESTS ====================
    
    @ParameterizedTest(name = "Test {index}: {0} passengers")
    @ValueSource(ints = {1, 2, 3, 5, 10})
    @Tag("slow")
    @DisplayName("Parameterized - Confirm Reservation with Different Passenger Counts")
    void testConfirmReservation_DifferentPassengerCounts(int passengerCount) {
        Reservation reservation = reservationManager.createReservation(testFlight);
        
        for (int i = 0; i < passengerCount; i++) {
            Passenger passenger = createPassenger(
                "P" + i,
                "FirstName" + i,
                "LastName" + i,
                "passenger" + i + "@example.com",
                "123456789" + i,
                25 + i
            );
            reservationManager.addPassengerToReservation(reservation, passenger);
        }
        
        int initialSeats = testFlight.getAvailableSeats();
        boolean confirmed = reservationManager.confirmReservation(reservation, "PAY001");
        
        assertTrue(confirmed);
        assertEquals(passengerCount, reservation.getNumberOfPassengers());
        assertEquals(initialSeats - passengerCount, testFlight.getAvailableSeats());
        assertEquals(200.0 * passengerCount, reservation.getTotalPrice());
    }
    
    @ParameterizedTest(name = "Test {index}: Price={0}")
    @CsvSource({
        "100.0, 1, 100.0",
        "200.0, 2, 400.0",
        "150.0, 3, 450.0",
        "250.0, 4, 1000.0",
        "99.99, 5, 499.95"
    })
    @Tag("fast")
    @DisplayName("Parameterized - Total Price Calculation")
    void testReservation_TotalPriceCalculation(double pricePerSeat, int passengers, double expectedTotal) {
        Flight flight = createFlight("FL001", "NYC", "LAX", 100, pricePerSeat);
        Reservation reservation = reservationManager.createReservation(flight);
        
        for (int i = 0; i < passengers; i++) {
            Passenger passenger = createPassenger(
                "P" + i,
                "FirstName" + i,
                "LastName" + i,
                "passenger" + i + "@example.com",
                "123456789" + i,
                25 + i
            );
            reservationManager.addPassengerToReservation(reservation, passenger);
        }
        
        assertEquals(expectedTotal, reservation.getTotalPrice(), 0.01);
    }
    
    @ParameterizedTest(name = "Test {index}: PaymentID={0}")
    @ValueSource(strings = {"PAY001", "PAY-2024-001", "PAYMENT_12345", "TX_001", "P1"})
    @Tag("fast")
    @DisplayName("Parameterized - Different Payment ID Formats")
    void testConfirmReservation_DifferentPaymentIds(String paymentId) {
        Reservation reservation = reservationManager.createReservation(testFlight);
        Passenger passenger = createPassenger("P123456", "John", "Doe", "john@example.com", "1234567890", 30);
        reservationManager.addPassengerToReservation(reservation, passenger);
        
        boolean confirmed = reservationManager.confirmReservation(reservation, paymentId);
        
        assertTrue(confirmed);
        assertEquals(paymentId, reservation.getPaymentId());
    }
    
    @ParameterizedTest(name = "Test {index}: Invalid PaymentID={0}")
    @ValueSource(strings = {"", "   ", "\t", "\n"})
    @Tag("fast")
    @DisplayName("Parameterized - Invalid Payment IDs")
    void testConfirmReservation_InvalidPaymentIds(String paymentId) {
        Reservation reservation = reservationManager.createReservation(testFlight);
        Passenger passenger = createPassenger("P123456", "John", "Doe", "john@example.com", "1234567890", 30);
        reservationManager.addPassengerToReservation(reservation, passenger);
        
        assertThrows(IllegalArgumentException.class,
            () -> reservationManager.confirmReservation(reservation, paymentId)
        );
    }
    
    @ParameterizedTest(name = "Test {index}: Invalid Email={0}")
    @ValueSource(strings = {"", "   ", "\t", "\n"})
    @Tag("fast")
    @DisplayName("Parameterized - Invalid Passenger Emails")
    void testGetReservationsByPassenger_InvalidEmails(String email) {
        assertThrows(IllegalArgumentException.class,
            () -> reservationManager.getReservationsByPassenger(email)
        );
    }
    
    @ParameterizedTest(name = "Test {index}: Invalid ReservationID={0}")
    @ValueSource(strings = {"", "   ", "\t", "\n"})
    @Tag("fast")
    @DisplayName("Parameterized - Invalid Reservation IDs for Cancel")
    void testCancelReservation_InvalidIds(String reservationId) {
        assertThrows(IllegalArgumentException.class,
            () -> reservationManager.cancelReservation(reservationId)
        );
    }
    
    // ==================== INTEGRATION TESTS ====================
    
    @Test
    @Tag("integration")
    @DisplayName("Integration - Complete Reservation Lifecycle")
    void testCompleteReservationLifecycle() {
        // Create reservation
        Reservation reservation = reservationManager.createReservation(testFlight);
        assertEquals("PENDING", reservation.getStatus());
        assertEquals(1, reservationManager.getTotalReservations());
        
        // Add passengers
        Passenger p1 = createPassenger("P123456", "John", "Doe", "john@example.com", "1234567890", 30);
        Passenger p2 = createPassenger("P789012", "Jane", "Smith", "jane@example.com", "0987654321", 28);
        reservationManager.addPassengerToReservation(reservation, p1);
        reservationManager.addPassengerToReservation(reservation, p2);
        assertEquals(2, reservation.getNumberOfPassengers());
        assertEquals(400.0, reservation.getTotalPrice());
        
        // Confirm reservation
        int seatsBeforeConfirm = testFlight.getAvailableSeats();
        boolean confirmed = reservationManager.confirmReservation(reservation, "PAY001");
        assertTrue(confirmed);
        assertEquals("CONFIRMED", reservation.getStatus());
        assertEquals(seatsBeforeConfirm - 2, testFlight.getAvailableSeats());
        assertEquals(1, reservationManager.getConfirmedReservations());
        
        // Find reservation
        Reservation found = reservationManager.findReservationById(reservation.getReservationId());
        assertNotNull(found);
        assertEquals(reservation, found);
        
        // Get by passenger email
        List<Reservation> johnReservations = reservationManager.getReservationsByPassenger("john@example.com");
        assertEquals(1, johnReservations.size());
        assertTrue(johnReservations.contains(reservation));
        
        // Cancel reservation
        boolean cancelled = reservationManager.cancelReservation(reservation.getReservationId());
        assertTrue(cancelled);
        assertEquals("CANCELLED", reservation.getStatus());
        assertEquals(seatsBeforeConfirm, testFlight.getAvailableSeats());
    }
    
    @Test
    @Tag("integration")
    @DisplayName("Integration - Multiple Reservations on Same Flight")
    void testMultipleReservationsOnSameFlight() {
        Reservation res1 = reservationManager.createReservation(testFlight);
        Reservation res2 = reservationManager.createReservation(testFlight);
        Reservation res3 = reservationManager.createReservation(testFlight);
        
        Passenger p1 = createPassenger("P123456", "John", "Doe", "john@example.com", "1234567890", 30);
        Passenger p2 = createPassenger("P789012", "Jane", "Smith", "jane@example.com", "0987654321", 28);
        Passenger p3 = createPassenger("P345678", "Bob", "Johnson", "bob@example.com", "5555555555", 35);
        
        reservationManager.addPassengerToReservation(res1, p1);
        reservationManager.addPassengerToReservation(res2, p2);
        reservationManager.addPassengerToReservation(res3, p3);
        
        int initialSeats = testFlight.getAvailableSeats();
        
        reservationManager.confirmReservation(res1, "PAY001");
        reservationManager.confirmReservation(res2, "PAY002");
        reservationManager.confirmReservation(res3, "PAY003");
        
        assertEquals(3, reservationManager.getTotalReservations());
        assertEquals(3, reservationManager.getConfirmedReservations());
        assertEquals(initialSeats - 3, testFlight.getAvailableSeats());
    }
    
    @Test
    @Tag("integration")
    @DisplayName("Integration - Passenger in Multiple Reservations")
    void testPassengerInMultipleReservations() {
        Flight flight2 = createFlight("FL002", "LAX", "SFO", 100, 150.0);
        
        Reservation res1 = reservationManager.createReservation(testFlight);
        Reservation res2 = reservationManager.createReservation(flight2);
        
        Passenger passenger = createPassenger("P123456", "John", "Doe", "john@example.com", "1234567890", 30);
        
        reservationManager.addPassengerToReservation(res1, passenger);
        reservationManager.addPassengerToReservation(res2, passenger);
        
        reservationManager.confirmReservation(res1, "PAY001");
        reservationManager.confirmReservation(res2, "PAY002");
        
        List<Reservation> johnReservations = reservationManager.getReservationsByPassenger("john@example.com");
        
        assertEquals(2, johnReservations.size());
        assertTrue(johnReservations.contains(res1));
        assertTrue(johnReservations.contains(res2));
    }
    
    @Test
    @Tag("integration")
    @DisplayName("Integration - Add and Remove Passengers Before Confirm")
    void testAddRemovePassengersBeforeConfirm() {
        Reservation reservation = reservationManager.createReservation(testFlight);
        
        Passenger p1 = createPassenger("P123456", "John", "Doe", "john@example.com", "1234567890", 30);
        Passenger p2 = createPassenger("P789012", "Jane", "Smith", "jane@example.com", "0987654321", 28);
        Passenger p3 = createPassenger("P345678", "Bob", "Johnson", "bob@example.com", "5555555555", 35);
        
        // Add all passengers
        reservationManager.addPassengerToReservation(reservation, p1);
        reservationManager.addPassengerToReservation(reservation, p2);
        reservationManager.addPassengerToReservation(reservation, p3);
        assertEquals(3, reservation.getNumberOfPassengers());
        assertEquals(600.0, reservation.getTotalPrice());
        
        // Remove one passenger
        reservationManager.removePassengerFromReservation(reservation, p2);
        assertEquals(2, reservation.getNumberOfPassengers());
        assertEquals(400.0, reservation.getTotalPrice());
        
        // Confirm with remaining passengers
        boolean confirmed = reservationManager.confirmReservation(reservation, "PAY001");
        assertTrue(confirmed);
        assertEquals(2, reservation.getNumberOfPassengers());
    }
    
    @Test
    @Tag("integration")
    @DisplayName("Integration - Flight Capacity Management")
    void testFlightCapacityManagement() {
        Flight smallFlight = createFlight("FL002", "NYC", "LAX", 5, 200.0);
        
        // Create multiple reservations
        Reservation res1 = reservationManager.createReservation(smallFlight);
        Reservation res2 = reservationManager.createReservation(smallFlight);
        Reservation res3 = reservationManager.createReservation(smallFlight);
        
        // Add passengers to each
        Passenger p1a = createPassenger("P123456", "John", "Doe", "john@example.com", "1234567890", 30);
        Passenger p1b = createPassenger("P789012", "Jane", "Doe", "jane@example.com", "0987654321", 28);
        reservationManager.addPassengerToReservation(res1, p1a);
        reservationManager.addPassengerToReservation(res1, p1b);
        
        Passenger p2 = createPassenger("P345678", "Bob", "Smith", "bob@example.com", "5555555555", 35);
        reservationManager.addPassengerToReservation(res2, p2);
        
        Passenger p3a = createPassenger("P901234", "Alice", "Johnson", "alice@example.com", "4444444444", 32);
        Passenger p3b = createPassenger("P567890", "Charlie", "Brown", "charlie@example.com", "3333333333", 40);
        Passenger p3c = createPassenger("P111111", "Diana", "Prince", "diana@example.com", "2222222222", 29);
        reservationManager.addPassengerToReservation(res3, p3a);
        reservationManager.addPassengerToReservation(res3, p3b);
        reservationManager.addPassengerToReservation(res3, p3c);
        
        // Confirm first two reservations (3 seats total)
        assertTrue(reservationManager.confirmReservation(res1, "PAY001"));
        assertTrue(reservationManager.confirmReservation(res2, "PAY002"));
        assertEquals(2, smallFlight.getAvailableSeats());
        
        // Third reservation should fail (needs 3 seats but only 2 available)
        assertFalse(reservationManager.confirmReservation(res3, "PAY003"));
        assertEquals("PENDING", res3.getStatus());
        assertEquals(2, smallFlight.getAvailableSeats());
        
        // Cancel first reservation
        reservationManager.cancelReservation(res1.getReservationId());
        assertEquals(4, smallFlight.getAvailableSeats());
    }
    
    @Test
    @Tag("integration")
    @DisplayName("Integration - Search Reservations After Operations")
    void testSearchReservationsAfterOperations() {
        // Create multiple reservations
        Reservation res1 = reservationManager.createReservation(testFlight);
        Reservation res2 = reservationManager.createReservation(testFlight);
        Reservation res3 = reservationManager.createReservation(testFlight);
        
        Passenger sharedPassenger = createPassenger("P123456", "John", "Doe", "john@example.com", "1234567890", 30);
        Passenger p2 = createPassenger("P789012", "Jane", "Smith", "jane@example.com", "0987654321", 28);
        Passenger p3 = createPassenger("P345678", "Bob", "Johnson", "bob@example.com", "5555555555", 35);
        
        reservationManager.addPassengerToReservation(res1, sharedPassenger);
        reservationManager.addPassengerToReservation(res2, sharedPassenger);
        reservationManager.addPassengerToReservation(res2, p2);
        reservationManager.addPassengerToReservation(res3, p3);
        
        // Confirm some reservations
        reservationManager.confirmReservation(res1, "PAY001");
        reservationManager.confirmReservation(res2, "PAY002");
        reservationManager.confirmReservation(res3, "PAY003");
        
        // John should appear in 2 reservations
        List<Reservation> johnReservations = reservationManager.getReservationsByPassenger("john@example.com");
        assertEquals(2, johnReservations.size());
        
        // Cancel one of John's reservations
        reservationManager.cancelReservation(res1.getReservationId());
        
        // John still appears in 2 reservations (including cancelled)
        johnReservations = reservationManager.getReservationsByPassenger("john@example.com");
        assertEquals(2, johnReservations.size());
        
        // Verify statistics
        assertEquals(3, reservationManager.getTotalReservations());
        assertEquals(2, reservationManager.getConfirmedReservations()); // res2 and res3
    }
    
    @Test
    @Tag("integration")
    @DisplayName("Integration - Edge Case: Empty Then Full Reservation")
    void testEmptyThenFullReservation() {
        Reservation reservation = reservationManager.createReservation(testFlight);
        
        // Try to confirm without passengers (should fail)
        assertThrows(IllegalStateException.class,
            () -> reservationManager.confirmReservation(reservation, "PAY001")
        );
        
        // Add passenger and confirm
        Passenger passenger = createPassenger("P123456", "John", "Doe", "john@example.com", "1234567890", 30);
        reservationManager.addPassengerToReservation(reservation, passenger);
        
        boolean confirmed = reservationManager.confirmReservation(reservation, "PAY001");
        assertTrue(confirmed);
        
        // Try to add another passenger after confirmation (should fail)
        Passenger passenger2 = createPassenger("P789012", "Jane", "Smith", "jane@example.com", "0987654321", 28);
        assertThrows(IllegalStateException.class,
            () -> reservationManager.addPassengerToReservation(reservation, passenger2)
        );
    }
    
    @Test
    @Tag("fast")
    @DisplayName("BVA - Reservation Counter Increments")
    void testReservationCounterIncrements() {
        Reservation res1 = reservationManager.createReservation(testFlight);
        Reservation res2 = reservationManager.createReservation(testFlight);
        Reservation res3 = reservationManager.createReservation(testFlight);
        
        String id1 = res1.getReservationId();
        String id2 = res2.getReservationId();
        String id3 = res3.getReservationId();
        
        // Extract numbers and verify they increment
        int num1 = Integer.parseInt(id1.replace("RES", ""));
        int num2 = Integer.parseInt(id2.replace("RES", ""));
        int num3 = Integer.parseInt(id3.replace("RES", ""));
        
        assertEquals(num1 + 1, num2);
        assertEquals(num2 + 1, num3);
    }
}
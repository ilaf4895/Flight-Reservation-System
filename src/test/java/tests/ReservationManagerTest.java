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
}

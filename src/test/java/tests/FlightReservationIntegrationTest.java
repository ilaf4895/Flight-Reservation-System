package tests;

import model.Flight;
import model.Passenger;
import model.Payment;
import model.Reservation;
import service.FlightSearchEngine;
import service.PaymentProcessor;
import service.ReservationManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Flight Reservation System - Integration Tests")
@Tag("integration")
public class FlightReservationIntegrationTest {

    private FlightSearchEngine searchEngine;
    private ReservationManager reservationManager;
    private PaymentProcessor paymentProcessor;

    @BeforeEach
    void setUp() {
        searchEngine = new FlightSearchEngine();
        reservationManager = new ReservationManager();
        paymentProcessor = new PaymentProcessor();

        // Add sample flights
        LocalDateTime travelDate = LocalDateTime.of(2025, 10, 15, 10, 0);
        Flight flight1 = new Flight("FL001", "Karachi", "Lahore",
                travelDate, travelDate.plusHours(2), 100, 5000, "PIA");
        Flight flight2 = new Flight("FL002", "Lahore", "Islamabad",
                travelDate.plusHours(3), travelDate.plusHours(4), 150, 3500, "AirBlue");

        searchEngine.addFlight(flight1);
        searchEngine.addFlight(flight2);
    }

    @Test
    @DisplayName("End-to-end: Search, Reserve, and Pay for flight")
    void completeBookingFlow_success() {
        // Step 1: Search for flights
        LocalDateTime travelDate = LocalDateTime.of(2025, 10, 15, 10, 0);
        List<Flight> flights = searchEngine.searchFlights("Karachi", "Lahore", travelDate);
        assertFalse(flights.isEmpty());

        // Step 2: Create reservation
        Flight selectedFlight = flights.get(0);
        Reservation reservation = reservationManager.createReservation(selectedFlight);
        assertNotNull(reservation);

        // Step 3: Add passengers
        Passenger passenger = new Passenger("P001", "Ahmed", "Khan",
                "ahmed@gmail.com", "03001234567", 28);
        reservationManager.addPassengerToReservation(reservation, passenger);
        assertEquals(1, reservation.getNumberOfPassengers());

        // Step 4: Process payment
        Payment payment = paymentProcessor.processPayment(
                reservation.getReservationId(),
                reservation.getTotalPrice(),
                "4532015112830366",
                "123",
                "12/25"
        );
        assertEquals("SUCCESS", payment.getStatus());

        // Step 5: Confirm reservation
        boolean confirmed = reservationManager.confirmReservation(
                reservation, payment.getPaymentId());
        assertTrue(confirmed);
        assertTrue(reservation.isConfirmed());

        // Step 6: Verify seats are booked
        assertEquals(99, selectedFlight.getAvailableSeats());
    }

    @Test
    @DisplayName("End-to-end: Cancel confirmed booking with refund")
    void cancelBooking_withRefund_success() {
        // Create and confirm booking
        LocalDateTime travelDate = LocalDateTime.of(2025, 10, 15, 10, 0);
        List<Flight> flights = searchEngine.searchFlights("Karachi", "Lahore", travelDate);
        Flight selectedFlight = flights.get(0);

        Reservation reservation = reservationManager.createReservation(selectedFlight);
        Passenger passenger = new Passenger("P001", "Ahmed", "Khan",
                "ahmed@gmail.com", "03001234567", 28);
        reservationManager.addPassengerToReservation(reservation, passenger);

        Payment payment = paymentProcessor.processPayment(
                reservation.getReservationId(),
                reservation.getTotalPrice(),
                "4532015112830366", "123", "12/25"
        );
        reservationManager.confirmReservation(reservation, payment.getPaymentId());

        // Cancel reservation
        boolean cancelled = reservationManager.cancelReservation(reservation.getReservationId());
        assertTrue(cancelled);
        assertEquals("CANCELLED", reservation.getStatus());

        // Process refund
        boolean refunded = paymentProcessor.refundPayment(payment.getPaymentId());
        assertTrue(refunded);
        assertEquals("REFUNDED", payment.getStatus());

        // Verify seats are released
        assertEquals(100, selectedFlight.getAvailableSeats());
    }

    @Test
    @DisplayName("Multi-passenger booking scenario")
    void multiPassengerBooking_success() {
        LocalDateTime travelDate = LocalDateTime.of(2025, 10, 15, 10, 0);
        List<Flight> flights = searchEngine.searchFlights("Karachi", "Lahore", travelDate);
        Flight selectedFlight = flights.get(0);

        Reservation reservation = reservationManager.createReservation(selectedFlight);

        // Add multiple passengers
        Passenger p1 = new Passenger("P001", "Ahmed", "Khan", "ahmed@gmail.com", "03001234567", 28);
        Passenger p2 = new Passenger("P002", "Fatima", "Ali", "fatima@gmail.com", "03009876543", 35);
        Passenger p3 = new Passenger("P003", "Hassan", "Ahmed", "hassan@gmail.com", "03115555555", 16);

        reservationManager.addPassengerToReservation(reservation, p1);
        reservationManager.addPassengerToReservation(reservation, p2);
        reservationManager.addPassengerToReservation(reservation, p3);

        assertEquals(3, reservation.getNumberOfPassengers());
        assertEquals(15000, reservation.getTotalPrice()); // 3 * 5000

        Payment payment = paymentProcessor.processPayment(
                reservation.getReservationId(),
                reservation.getTotalPrice(),
                "4532015112830366", "123", "12/25"
        );

        reservationManager.confirmReservation(reservation, payment.getPaymentId());
        assertTrue(reservation.isConfirmed());
        assertEquals(97, selectedFlight.getAvailableSeats()); // 100 - 3
    }
}
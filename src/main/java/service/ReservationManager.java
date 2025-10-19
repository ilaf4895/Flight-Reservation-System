package service;

import model.Flight;
import model.Passenger;
import model.Reservation;
import java.util.ArrayList;
import java.util.List;

public class ReservationManager {
    private List<Reservation> reservations;
    private int reservationCounter = 1000;

    public ReservationManager() {
        this.reservations = new ArrayList<>();
    }

    public Reservation createReservation(Flight flight) {
        if (flight == null) {
            throw new IllegalArgumentException("Flight cannot be null");
        }
        if (flight.getAvailableSeats() <= 0) {
            throw new IllegalStateException("No available seats on this flight");
        }

        String reservationId = "RES" + (++reservationCounter);
        Reservation reservation = new Reservation(reservationId, flight);
        reservations.add(reservation);
        return reservation;
    }

    public void addPassengerToReservation(Reservation reservation, Passenger passenger) {
        if (reservation == null) {
            throw new IllegalArgumentException("Reservation cannot be null");
        }
        if (passenger == null) {
            throw new IllegalArgumentException("Passenger cannot be null");
        }
        if (reservation.isConfirmed()) {
            throw new IllegalStateException("Cannot modify confirmed reservation");
        }

        reservation.addPassenger(passenger);
    }

    public void removePassengerFromReservation(Reservation reservation, Passenger passenger) {
        if (reservation == null) {
            throw new IllegalArgumentException("Reservation cannot be null");
        }
        if (passenger == null) {
            throw new IllegalArgumentException("Passenger cannot be null");
        }
        if (reservation.isConfirmed()) {
            throw new IllegalStateException("Cannot modify confirmed reservation");
        }

        reservation.removePassenger(passenger);
    }

    public boolean confirmReservation(Reservation reservation, String paymentId) {
        if (reservation == null) {
            throw new IllegalArgumentException("Reservation cannot be null");
        }
        if (paymentId == null || paymentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment ID cannot be empty");
        }
        if (reservation.getNumberOfPassengers() == 0) {
            throw new IllegalStateException("Cannot confirm reservation without passengers");
        }

        Flight flight = reservation.getFlight();
        int numberOfPassengers = reservation.getNumberOfPassengers();

        if (flight.bookSeats(numberOfPassengers)) {
            reservation.confirm(paymentId);
            return true;
        }
        return false;
    }

    public boolean cancelReservation(String reservationId) {
        if (reservationId == null || reservationId.trim().isEmpty()) {
            throw new IllegalArgumentException("Reservation ID cannot be empty");
        }

        Reservation reservation = findReservationById(reservationId);
        if (reservation == null) {
            return false;
        }
        if (!reservation.isConfirmed()) {
            throw new IllegalStateException("Only confirmed reservations can be cancelled");
        }

        reservation.getFlight().cancelSeats(reservation.getNumberOfPassengers());
        reservation.cancel();
        return true;
    }

    public Reservation findReservationById(String reservationId) {
        if (reservationId == null || reservationId.trim().isEmpty()) {
            throw new IllegalArgumentException("Reservation ID cannot be empty");
        }

        return reservations.stream()
                .filter(r -> r.getReservationId().equals(reservationId))
                .findFirst()
                .orElse(null);
    }

    public List<Reservation> getReservationsByPassenger(String passengerEmail) {
        if (passengerEmail == null || passengerEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Passenger email cannot be empty");
        }

        List<Reservation> result = new ArrayList<>();
        for (Reservation reservation : reservations) {
            for (Passenger passenger : reservation.getPassengers()) {
                if (passenger.getEmail().equalsIgnoreCase(passengerEmail)) {
                    result.add(reservation);
                    break;
                }
            }
        }
        return result;
    }

    public int getTotalReservations() {
        return reservations.size();
    }

    public int getConfirmedReservations() {
        return (int) reservations.stream()
                .filter(Reservation::isConfirmed)
                .count();
    }
}
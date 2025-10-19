package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Reservation {
    private String reservationId;
    private Flight flight;
    private List<Passenger> passengers;
    private double totalPrice;
    private String status; // PENDING, CONFIRMED, CANCELLED
    private LocalDateTime bookingDate;
    private String paymentId;

    public Reservation(String reservationId, Flight flight) {
        this.reservationId = reservationId;
        this.flight = flight;
        this.passengers = new ArrayList<>();
        this.status = "PENDING";
        this.bookingDate = LocalDateTime.now();
    }

    public String getReservationId() { return reservationId; }
    public Flight getFlight() { return flight; }
    public List<Passenger> getPassengers() { return passengers; }
    public double getTotalPrice() { return totalPrice; }
    public String getStatus() { return status; }
    public LocalDateTime getBookingDate() { return bookingDate; }
    public String getPaymentId() { return paymentId; }

    public void addPassenger(Passenger passenger) {
        if (passenger == null) {
            throw new IllegalArgumentException("Passenger cannot be null");
        }
        passengers.add(passenger);
        calculateTotalPrice();
    }

    public void removePassenger(Passenger passenger) {
        passengers.remove(passenger);
        calculateTotalPrice();
    }

    public int getNumberOfPassengers() {
        return passengers.size();
    }

    private void calculateTotalPrice() {
        this.totalPrice = flight.getPricePerSeat() * passengers.size();
    }

    public void confirm(String paymentId) {
        if (paymentId == null || paymentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment ID cannot be empty");
        }
        this.status = "CONFIRMED";
        this.paymentId = paymentId;
    }

    public void cancel() {
        this.status = "CANCELLED";
    }

    public boolean isConfirmed() {
        return "CONFIRMED".equals(status);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        return Objects.equals(reservationId, that.reservationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reservationId);
    }
}
package model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Flight {
    private String flightId;
    private String sourceCity;
    private String destinationCity;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private int totalSeats;
    private int availableSeats;
    private double pricePerSeat;
    private String airline;

    public Flight(String flightId, String sourceCity, String destinationCity,
                  LocalDateTime departureTime, LocalDateTime arrivalTime,
                  int totalSeats, double pricePerSeat, String airline) {
        this.flightId = flightId;
        this.sourceCity = sourceCity;
        this.destinationCity = destinationCity;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.totalSeats = totalSeats;
        this.availableSeats = totalSeats;
        this.pricePerSeat = pricePerSeat;
        this.airline = airline;
    }

    public String getFlightId() { return flightId; }
    public String getSourceCity() { return sourceCity; }
    public String getDestinationCity() { return destinationCity; }
    public LocalDateTime getDepartureTime() { return departureTime; }
    public LocalDateTime getArrivalTime() { return arrivalTime; }
    public int getAvailableSeats() { return availableSeats; }
    public int getTotalSeats() { return totalSeats; }
    public double getPricePerSeat() { return pricePerSeat; }
    public String getAirline() { return airline; }

    public boolean bookSeats(int numberOfSeats) {
        if (numberOfSeats <= 0) {
            throw new IllegalArgumentException("Number of seats must be positive");
        }
        if (numberOfSeats > availableSeats) {
            return false;
        }
        availableSeats -= numberOfSeats;
        return true;
    }

    public void cancelSeats(int numberOfSeats) {
        if (numberOfSeats <= 0) {
            throw new IllegalArgumentException("Number of seats must be positive");
        }
        if (availableSeats + numberOfSeats > totalSeats) {
            throw new IllegalArgumentException("Cannot cancel more seats than booked");
        }
        availableSeats += numberOfSeats;
    }

    public boolean isFull() {
        return availableSeats == 0;
    }

    public double getFlightDuration() {
        return java.time.temporal.ChronoUnit.MINUTES.between(departureTime, arrivalTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Flight flight = (Flight) o;
        return Objects.equals(flightId, flight.flightId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flightId);
    }
}
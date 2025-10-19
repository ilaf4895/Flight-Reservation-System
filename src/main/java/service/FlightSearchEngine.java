package service;

import model.Flight;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FlightSearchEngine {
    private List<Flight> flightDatabase;

    public FlightSearchEngine() {
        this.flightDatabase = new ArrayList<>();
    }

    public void addFlight(Flight flight) {
        if (flight == null) {
            throw new IllegalArgumentException("Flight cannot be null");
        }
        if (flightDatabase.contains(flight)) {
            throw new IllegalArgumentException("Flight already exists");
        }
        flightDatabase.add(flight);
    }

    public List<Flight> searchFlights(String source, String destination, LocalDateTime date) {
        if (source == null || source.trim().isEmpty()) {
            throw new IllegalArgumentException("Source city cannot be empty");
        }
        if (destination == null || destination.trim().isEmpty()) {
            throw new IllegalArgumentException("Destination city cannot be empty");
        }
        if (date == null) {
            throw new IllegalArgumentException("Travel date cannot be null");
        }
        if (source.equalsIgnoreCase(destination)) {
            throw new IllegalArgumentException("Source and destination cannot be same");
        }

        return flightDatabase.stream()
                .filter(f -> f.getSourceCity().equalsIgnoreCase(source) &&
                           f.getDestinationCity().equalsIgnoreCase(destination) &&
                           isSameDay(f.getDepartureTime(), date) &&
                           f.getAvailableSeats() > 0)
                .collect(Collectors.toList());
    }

    public List<Flight> searchFlightsByAirline(String source, String destination,
                                               LocalDateTime date, String airline) {
        List<Flight> flights = searchFlights(source, destination, date);
        return flights.stream()
                .filter(f -> f.getAirline().equalsIgnoreCase(airline))
                .collect(Collectors.toList());
    }

    public List<Flight> searchFlightsByPriceRange(String source, String destination,
                                                   LocalDateTime date, double minPrice, double maxPrice) {
        if (minPrice < 0 || maxPrice < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        if (minPrice > maxPrice) {
            throw new IllegalArgumentException("Min price cannot be greater than max price");
        }

        List<Flight> flights = searchFlights(source, destination, date);
        return flights.stream()
                .filter(f -> f.getPricePerSeat() >= minPrice && f.getPricePerSeat() <= maxPrice)
                .collect(Collectors.toList());
    }

    public List<Flight> searchFlightsBySeatsAvailable(String source, String destination,
                                                       LocalDateTime date, int requiredSeats) {
        if (requiredSeats <= 0) {
            throw new IllegalArgumentException("Required seats must be positive");
        }

        List<Flight> flights = searchFlights(source, destination, date);
        return flights.stream()
                .filter(f -> f.getAvailableSeats() >= requiredSeats)
                .collect(Collectors.toList());
    }

    public Flight findFlightById(String flightId) {
        if (flightId == null || flightId.trim().isEmpty()) {
            throw new IllegalArgumentException("Flight ID cannot be empty");
        }

        return flightDatabase.stream()
                .filter(f -> f.getFlightId().equalsIgnoreCase(flightId))
                .findFirst()
                .orElse(null);
    }

    public int getTotalFlights() {
        return flightDatabase.size();
    }

    private boolean isSameDay(LocalDateTime dateTime1, LocalDateTime dateTime2) {
        return dateTime1.toLocalDate().equals(dateTime2.toLocalDate());
    }
}
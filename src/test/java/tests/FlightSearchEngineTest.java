package tests;

import model.Flight;
import service.FlightSearchEngine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Flight Search Engine Test Suite")
public class FlightSearchEngineTest {
    
    private FlightSearchEngine searchEngine;
    private LocalDateTime baseDate;
    
    @BeforeEach
    void setUp() {
        searchEngine = new FlightSearchEngine();
        baseDate = LocalDateTime.of(2025, 12, 15, 10, 0);
    }
    
    // Helper method to create test flights
    private Flight createFlight(String id, String source, String dest, 
                               LocalDateTime departure, int seats, double price, String airline) {
        LocalDateTime arrival = departure.plusHours(2);
        return new Flight(id, source, dest, departure, arrival, seats, price, airline);
    }
    
    // ==================== ADD FLIGHT TESTS ====================
    
    @Test
    @DisplayName("Add Flight - Valid Flight")
    void testAddFlight_Valid() {
        Flight flight = createFlight("FL001", "NYC", "LAX", baseDate, 100, 200.0, "Delta");
        
        assertDoesNotThrow(() -> searchEngine.addFlight(flight));
        assertEquals(1, searchEngine.getTotalFlights());
    }
    
    @Test
    @DisplayName("Add Flight - Null Flight")
    void testAddFlight_Null() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> searchEngine.addFlight(null)
        );
        assertEquals("Flight cannot be null", exception.getMessage());
    }
    
    @Test
    @DisplayName("Add Flight - Duplicate Flight")
    void testAddFlight_Duplicate() {
        Flight flight = createFlight("FL001", "NYC", "LAX", baseDate, 100, 200.0, "Delta");
        searchEngine.addFlight(flight);
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> searchEngine.addFlight(flight)
        );
        assertEquals("Flight already exists", exception.getMessage());
    }
    
    @Test
    @DisplayName("Add Multiple Flights")
    void testAddMultipleFlights() {
        Flight flight1 = createFlight("FL001", "NYC", "LAX", baseDate, 100, 200.0, "Delta");
        Flight flight2 = createFlight("FL002", "NYC", "SFO", baseDate, 150, 250.0, "United");
        Flight flight3 = createFlight("FL003", "LAX", "NYC", baseDate, 120, 220.0, "American");
        
        searchEngine.addFlight(flight1);
        searchEngine.addFlight(flight2);
        searchEngine.addFlight(flight3);
        
        assertEquals(3, searchEngine.getTotalFlights());
    }
    
    // ==================== SEARCH FLIGHTS - ECP TESTS ====================
    
    @Test
    @DisplayName("ECP - Search Flights with Valid Criteria")
    void testSearchFlights_ValidCriteria() {
        Flight flight1 = createFlight("FL001", "NYC", "LAX", baseDate, 100, 200.0, "Delta");
        Flight flight2 = createFlight("FL002", "NYC", "LAX", baseDate, 150, 250.0, "United");
        Flight flight3 = createFlight("FL003", "NYC", "SFO", baseDate, 120, 220.0, "American");
        
        searchEngine.addFlight(flight1);
        searchEngine.addFlight(flight2);
        searchEngine.addFlight(flight3);
        
        List<Flight> results = searchEngine.searchFlights("NYC", "LAX", baseDate);
        
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(f -> f.getSourceCity().equalsIgnoreCase("NYC")));
        assertTrue(results.stream().allMatch(f -> f.getDestinationCity().equalsIgnoreCase("LAX")));
    }
    
    @Test
    @DisplayName("ECP - Search Flights No Results")
    void testSearchFlights_NoResults() {
        Flight flight = createFlight("FL001", "NYC", "LAX", baseDate, 100, 200.0, "Delta");
        searchEngine.addFlight(flight);
        
        List<Flight> results = searchEngine.searchFlights("NYC", "MIA", baseDate);
        
        assertTrue(results.isEmpty());
    }
    
    @Test
    @DisplayName("ECP - Search Flights Case Insensitive")
    void testSearchFlights_CaseInsensitive() {
        Flight flight = createFlight("FL001", "NYC", "LAX", baseDate, 100, 200.0, "Delta");
        searchEngine.addFlight(flight);
        
        List<Flight> results = searchEngine.searchFlights("nyc", "lax", baseDate);
        
        assertEquals(1, results.size());
    }
    
    @Test
    @DisplayName("ECP - Search Flights Null Source")
    void testSearchFlights_NullSource() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> searchEngine.searchFlights(null, "LAX", baseDate)
        );
        assertEquals("Source city cannot be empty", exception.getMessage());
    }
    
    @Test
    @DisplayName("ECP - Search Flights Empty Source")
    void testSearchFlights_EmptySource() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> searchEngine.searchFlights("   ", "LAX", baseDate)
        );
        assertEquals("Source city cannot be empty", exception.getMessage());
    }
    
    @Test
    @DisplayName("ECP - Search Flights Null Destination")
    void testSearchFlights_NullDestination() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> searchEngine.searchFlights("NYC", null, baseDate)
        );
        assertEquals("Destination city cannot be empty", exception.getMessage());
    }
    
    @Test
    @DisplayName("ECP - Search Flights Empty Destination")
    void testSearchFlights_EmptyDestination() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> searchEngine.searchFlights("NYC", "  ", baseDate)
        );
        assertEquals("Destination city cannot be empty", exception.getMessage());
    }
    
    @Test
    @DisplayName("ECP - Search Flights Null Date")
    void testSearchFlights_NullDate() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> searchEngine.searchFlights("NYC", "LAX", null)
        );
        assertEquals("Travel date cannot be null", exception.getMessage());
    }
    
    @Test
    @DisplayName("ECP - Search Flights Same Source and Destination")
    void testSearchFlights_SameSourceDestination() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> searchEngine.searchFlights("NYC", "NYC", baseDate)
        );
        assertEquals("Source and destination cannot be same", exception.getMessage());
    }
    
    @Test
    @DisplayName("ECP - Search Flights Only Available Seats")
    void testSearchFlights_OnlyAvailableSeats() {
        Flight flight1 = createFlight("FL001", "NYC", "LAX", baseDate, 100, 200.0, "Delta");
        Flight flight2 = createFlight("FL002", "NYC", "LAX", baseDate, 150, 250.0, "United");
        
        searchEngine.addFlight(flight1);
        searchEngine.addFlight(flight2);
        
        // Book all seats on flight1
        flight1.bookSeats(100);
        
        List<Flight> results = searchEngine.searchFlights("NYC", "LAX", baseDate);
        
        assertEquals(1, results.size());
        assertEquals("FL002", results.get(0).getFlightId());
    }
    
    @Test
    @DisplayName("ECP - Search Flights Different Date")
    void testSearchFlights_DifferentDate() {
        Flight flight = createFlight("FL001", "NYC", "LAX", baseDate, 100, 200.0, "Delta");
        searchEngine.addFlight(flight);
        
        LocalDateTime differentDate = baseDate.plusDays(1);
        List<Flight> results = searchEngine.searchFlights("NYC", "LAX", differentDate);
        
        assertTrue(results.isEmpty());
    }
    
    @Test
    @DisplayName("ECP - Search Flights Same Day Different Time")
    void testSearchFlights_SameDayDifferentTime() {
        Flight flight = createFlight("FL001", "NYC", "LAX", baseDate, 100, 200.0, "Delta");
        searchEngine.addFlight(flight);
        
        LocalDateTime sameDayDifferentTime = baseDate.withHour(18);
        List<Flight> results = searchEngine.searchFlights("NYC", "LAX", sameDayDifferentTime);
        
        assertEquals(1, results.size());
    }
    
    // ==================== SEARCH BY AIRLINE TESTS ====================
    
    @Test
    @DisplayName("Search By Airline - Valid")
    void testSearchFlightsByAirline_Valid() {
        Flight flight1 = createFlight("FL001", "NYC", "LAX", baseDate, 100, 200.0, "Delta");
        Flight flight2 = createFlight("FL002", "NYC", "LAX", baseDate, 150, 250.0, "United");
        Flight flight3 = createFlight("FL003", "NYC", "LAX", baseDate, 120, 220.0, "Delta");
        
        searchEngine.addFlight(flight1);
        searchEngine.addFlight(flight2);
        searchEngine.addFlight(flight3);
        
        List<Flight> results = searchEngine.searchFlightsByAirline("NYC", "LAX", baseDate, "Delta");
        
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(f -> f.getAirline().equalsIgnoreCase("Delta")));
    }
    
    @Test
    @DisplayName("Search By Airline - No Results")
    void testSearchFlightsByAirline_NoResults() {
        Flight flight = createFlight("FL001", "NYC", "LAX", baseDate, 100, 200.0, "Delta");
        searchEngine.addFlight(flight);
        
        List<Flight> results = searchEngine.searchFlightsByAirline("NYC", "LAX", baseDate, "United");
        
        assertTrue(results.isEmpty());
    }
    
    @Test
    @DisplayName("Search By Airline - Case Insensitive")
    void testSearchFlightsByAirline_CaseInsensitive() {
        Flight flight = createFlight("FL001", "NYC", "LAX", baseDate, 100, 200.0, "Delta");
        searchEngine.addFlight(flight);
        
        List<Flight> results = searchEngine.searchFlightsByAirline("NYC", "LAX", baseDate, "DELTA");
        
        assertEquals(1, results.size());
    }
    
    // ==================== SEARCH BY PRICE RANGE - BVA TESTS ====================
    
    @Test
    @DisplayName("BVA - Price Range Valid Range")
    void testSearchFlightsByPriceRange_ValidRange() {
        Flight flight1 = createFlight("FL001", "NYC", "LAX", baseDate, 100, 150.0, "Delta");
        Flight flight2 = createFlight("FL002", "NYC", "LAX", baseDate, 150, 250.0, "United");
        Flight flight3 = createFlight("FL003", "NYC", "LAX", baseDate, 120, 350.0, "American");
        
        searchEngine.addFlight(flight1);
        searchEngine.addFlight(flight2);
        searchEngine.addFlight(flight3);
        
        List<Flight> results = searchEngine.searchFlightsByPriceRange("NYC", "LAX", baseDate, 100.0, 300.0);
        
        assertEquals(2, results.size());
    }
    
    @Test
    @DisplayName("BVA - Price Range Minimum Boundary")
    void testSearchFlightsByPriceRange_MinBoundary() {
        Flight flight = createFlight("FL001", "NYC", "LAX", baseDate, 100, 100.0, "Delta");
        searchEngine.addFlight(flight);
        
        List<Flight> results = searchEngine.searchFlightsByPriceRange("NYC", "LAX", baseDate, 100.0, 200.0);
        
        assertEquals(1, results.size());
    }
    
    @Test
    @DisplayName("BVA - Price Range Maximum Boundary")
    void testSearchFlightsByPriceRange_MaxBoundary() {
        Flight flight = createFlight("FL001", "NYC", "LAX", baseDate, 100, 200.0, "Delta");
        searchEngine.addFlight(flight);
        
        List<Flight> results = searchEngine.searchFlightsByPriceRange("NYC", "LAX", baseDate, 100.0, 200.0);
        
        assertEquals(1, results.size());
    }
    
    @Test
    @DisplayName("BVA - Price Range Zero Minimum")
    void testSearchFlightsByPriceRange_ZeroMin() {
        Flight flight = createFlight("FL001", "NYC", "LAX", baseDate, 100, 150.0, "Delta");
        searchEngine.addFlight(flight);
        
        List<Flight> results = searchEngine.searchFlightsByPriceRange("NYC", "LAX", baseDate, 0.0, 200.0);
        
        assertEquals(1, results.size());
    }
    
    @Test
    @DisplayName("BVA - Price Range Negative Minimum")
    void testSearchFlightsByPriceRange_NegativeMin() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> searchEngine.searchFlightsByPriceRange("NYC", "LAX", baseDate, -100.0, 200.0)
        );
        assertEquals("Price cannot be negative", exception.getMessage());
    }
    
    @Test
    @DisplayName("BVA - Price Range Negative Maximum")
    void testSearchFlightsByPriceRange_NegativeMax() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> searchEngine.searchFlightsByPriceRange("NYC", "LAX", baseDate, 100.0, -200.0)
        );
        assertEquals("Price cannot be negative", exception.getMessage());
    }
    
    @Test
    @DisplayName("BVA - Price Range Min Greater Than Max")
    void testSearchFlightsByPriceRange_MinGreaterThanMax() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> searchEngine.searchFlightsByPriceRange("NYC", "LAX", baseDate, 300.0, 200.0)
        );
        assertEquals("Min price cannot be greater than max price", exception.getMessage());
    }
    
    @Test
    @DisplayName("BVA - Price Range Equal Min and Max")
    void testSearchFlightsByPriceRange_EqualMinMax() {
        Flight flight = createFlight("FL001", "NYC", "LAX", baseDate, 100, 200.0, "Delta");
        searchEngine.addFlight(flight);
        
        List<Flight> results = searchEngine.searchFlightsByPriceRange("NYC", "LAX", baseDate, 200.0, 200.0);
        
        assertEquals(1, results.size());
    }
    
    // ==================== SEARCH BY SEATS AVAILABLE - BVA TESTS ====================
    
    @Test
    @DisplayName("BVA - Seats Available Valid Request")
    void testSearchFlightsBySeatsAvailable_Valid() {
        Flight flight1 = createFlight("FL001", "NYC", "LAX", baseDate, 100, 200.0, "Delta");
        Flight flight2 = createFlight("FL002", "NYC", "LAX", baseDate, 50, 250.0, "United");
        
        searchEngine.addFlight(flight1);
        searchEngine.addFlight(flight2);
        
        List<Flight> results = searchEngine.searchFlightsBySeatsAvailable("NYC", "LAX", baseDate, 75);
        
        assertEquals(1, results.size());
        assertEquals("FL001", results.get(0).getFlightId());
    }
    
    @Test
    @DisplayName("BVA - Seats Available Minimum Boundary (1 seat)")
    void testSearchFlightsBySeatsAvailable_OneSeats() {
        Flight flight = createFlight("FL001", "NYC", "LAX", baseDate, 100, 200.0, "Delta");
        searchEngine.addFlight(flight);
        
        List<Flight> results = searchEngine.searchFlightsBySeatsAvailable("NYC", "LAX", baseDate, 1);
        
        assertEquals(1, results.size());
    }
    
    @Test
    @DisplayName("BVA - Seats Available Zero Seats")
    void testSearchFlightsBySeatsAvailable_ZeroSeats() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> searchEngine.searchFlightsBySeatsAvailable("NYC", "LAX", baseDate, 0)
        );
        assertEquals("Required seats must be positive", exception.getMessage());
    }
    
    @Test
    @DisplayName("BVA - Seats Available Negative Seats")
    void testSearchFlightsBySeatsAvailable_NegativeSeats() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> searchEngine.searchFlightsBySeatsAvailable("NYC", "LAX", baseDate, -5)
        );
        assertEquals("Required seats must be positive", exception.getMessage());
    }
    
    @Test
    @DisplayName("BVA - Seats Available Exact Match")
    void testSearchFlightsBySeatsAvailable_ExactMatch() {
        Flight flight = createFlight("FL001", "NYC", "LAX", baseDate, 100, 200.0, "Delta");
        searchEngine.addFlight(flight);
        
        List<Flight> results = searchEngine.searchFlightsBySeatsAvailable("NYC", "LAX", baseDate, 100);
        
        assertEquals(1, results.size());
    }
    
    @Test
    @DisplayName("BVA - Seats Available More Than Available")
    void testSearchFlightsBySeatsAvailable_MoreThanAvailable() {
        Flight flight = createFlight("FL001", "NYC", "LAX", baseDate, 100, 200.0, "Delta");
        searchEngine.addFlight(flight);
        
        List<Flight> results = searchEngine.searchFlightsBySeatsAvailable("NYC", "LAX", baseDate, 101);
        
        assertTrue(results.isEmpty());
    }
    
    // ==================== FIND FLIGHT BY ID TESTS ====================
    
    @Test
    @DisplayName("Find Flight By ID - Valid")
    void testFindFlightById_Valid() {
        Flight flight = createFlight("FL001", "NYC", "LAX", baseDate, 100, 200.0, "Delta");
        searchEngine.addFlight(flight);
        
        Flight found = searchEngine.findFlightById("FL001");
        
        assertNotNull(found);
        assertEquals("FL001", found.getFlightId());
    }
    
    @Test
    @DisplayName("Find Flight By ID - Case Insensitive")
    void testFindFlightById_CaseInsensitive() {
        Flight flight = createFlight("FL001", "NYC", "LAX", baseDate, 100, 200.0, "Delta");
        searchEngine.addFlight(flight);
        
        Flight found = searchEngine.findFlightById("fl001");
        
        assertNotNull(found);
        assertEquals("FL001", found.getFlightId());
    }
    
    @Test
    @DisplayName("Find Flight By ID - Not Found")
    void testFindFlightById_NotFound() {
        Flight flight = createFlight("FL001", "NYC", "LAX", baseDate, 100, 200.0, "Delta");
        searchEngine.addFlight(flight);
        
        Flight found = searchEngine.findFlightById("FL999");
        
        assertNull(found);
    }
    
    @Test
    @DisplayName("Find Flight By ID - Null ID")
    void testFindFlightById_NullId() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> searchEngine.findFlightById(null)
        );
        assertEquals("Flight ID cannot be empty", exception.getMessage());
    }
    
    @Test
    @DisplayName("Find Flight By ID - Empty ID")
    void testFindFlightById_EmptyId() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> searchEngine.findFlightById("   ")
        );
        assertEquals("Flight ID cannot be empty", exception.getMessage());
    }
    
    // ==================== PARAMETERIZED TESTS ====================
    
    @ParameterizedTest(name = "Test {index}: Source={0}, Destination={1}")
    @CsvSource({
        "NYC, LAX, NYC to LAX",
        "SFO, MIA, SFO to MIA",
        "CHI, BOS, CHI to BOS",
        "LAX, NYC, LAX to NYC",
        "MIA, SEA, MIA to SEA"
    })
    @DisplayName("Parameterized - Valid Flight Searches")
    void testSearchFlights_ValidRoutes(String source, String destination, String description) {
        Flight flight = createFlight("FL001", source, destination, baseDate, 100, 200.0, "Delta");
        searchEngine.addFlight(flight);
        
        List<Flight> results = searchEngine.searchFlights(source, destination, baseDate);
        
        assertEquals(1, results.size(), description);
        assertEquals(source, results.get(0).getSourceCity());
        assertEquals(destination, results.get(0).getDestinationCity());
    }
    
    @ParameterizedTest(name = "Test {index}: Airline={0}")
    @ValueSource(strings = {"Delta", "United", "American", "Southwest", "JetBlue"})
    @DisplayName("Parameterized - Different Airlines")
    void testSearchFlightsByAirline_DifferentAirlines(String airline) {
        Flight flight = createFlight("FL001", "NYC", "LAX", baseDate, 100, 200.0, airline);
        searchEngine.addFlight(flight);
        
        List<Flight> results = searchEngine.searchFlightsByAirline("NYC", "LAX", baseDate, airline);
        
        assertEquals(1, results.size());
        assertEquals(airline, results.get(0).getAirline());
    }
    
    @ParameterizedTest(name = "Test {index}: Min={0}, Max={1}")
    @CsvSource({
        "0.0, 100.0",
        "50.0, 150.0",
        "100.0, 200.0",
        "150.0, 300.0",
        "200.0, 500.0"
    })
    @DisplayName("Parameterized - Different Price Ranges")
    void testSearchFlightsByPriceRange_DifferentRanges(double minPrice, double maxPrice) {
        Flight flight = createFlight("FL001", "NYC", "LAX", baseDate, 100, 175.0, "Delta");
        searchEngine.addFlight(flight);
        
        List<Flight> results = searchEngine.searchFlightsByPriceRange("NYC", "LAX", baseDate, minPrice, maxPrice);
        
        if (175.0 >= minPrice && 175.0 <= maxPrice) {
            assertEquals(1, results.size());
        } else {
            assertTrue(results.isEmpty());
        }
    }
    
    @ParameterizedTest(name = "Test {index}: Required Seats={0}")
    @ValueSource(ints = {1, 10, 25, 50, 75, 100})
    @DisplayName("Parameterized - Different Seat Requirements")
    void testSearchFlightsBySeatsAvailable_DifferentRequirements(int requiredSeats) {
        Flight flight = createFlight("FL001", "NYC", "LAX", baseDate, 100, 200.0, "Delta");
        searchEngine.addFlight(flight);
        
        List<Flight> results = searchEngine.searchFlightsBySeatsAvailable("NYC", "LAX", baseDate, requiredSeats);
        
        if (requiredSeats <= 100) {
            assertEquals(1, results.size());
        } else {
            assertTrue(results.isEmpty());
        }
    }
    
    @ParameterizedTest(name = "Test {index}: Invalid source or destination={0}")
    @ValueSource(strings = {"", "   ", "\t", "\n"})
    @DisplayName("Parameterized - Invalid Source/Destination Inputs")
    void testSearchFlights_InvalidInputs(String invalidInput) {
        assertThrows(IllegalArgumentException.class,
            () -> searchEngine.searchFlights(invalidInput, "LAX", baseDate)
        );
        
        assertThrows(IllegalArgumentException.class,
            () -> searchEngine.searchFlights("NYC", invalidInput, baseDate)
        );
    }
    
    // ==================== INTEGRATION TESTS ====================
    
    @Test
    @DisplayName("Integration - Complete Flight Search Workflow")
    void testCompleteSearchWorkflow() {
        // Add multiple flights
        Flight flight1 = createFlight("FL001", "NYC", "LAX", baseDate, 100, 150.0, "Delta");
        Flight flight2 = createFlight("FL002", "NYC", "LAX", baseDate, 150, 250.0, "United");
        Flight flight3 = createFlight("FL003", "NYC", "LAX", baseDate, 120, 200.0, "Delta");
        Flight flight4 = createFlight("FL004", "NYC", "SFO", baseDate, 100, 180.0, "American");
        
        searchEngine.addFlight(flight1);
        searchEngine.addFlight(flight2);
        searchEngine.addFlight(flight3);
        searchEngine.addFlight(flight4);
        
        assertEquals(4, searchEngine.getTotalFlights());
        
        // Basic search
        List<Flight> nycToLax = searchEngine.searchFlights("NYC", "LAX", baseDate);
        assertEquals(3, nycToLax.size());
        
        // Search by airline
        List<Flight> deltaFlights = searchEngine.searchFlightsByAirline("NYC", "LAX", baseDate, "Delta");
        assertEquals(2, deltaFlights.size());
        
        // Search by price range
        List<Flight> affordableFlights = searchEngine.searchFlightsByPriceRange("NYC", "LAX", baseDate, 100.0, 200.0);
        assertEquals(2, affordableFlights.size());
        
        // Search by seats
        List<Flight> largeFlights = searchEngine.searchFlightsBySeatsAvailable("NYC", "LAX", baseDate, 130);
        assertEquals(1, largeFlights.size());
        assertEquals("FL002", largeFlights.get(0).getFlightId());
        
        // Find by ID
        Flight foundFlight = searchEngine.findFlightById("FL001");
        assertNotNull(foundFlight);
        assertEquals("Delta", foundFlight.getAirline());
    }
    
    @Test
    @DisplayName("Integration - Search After Booking")
    void testSearchAfterBooking() {
        Flight flight1 = createFlight("FL001", "NYC", "LAX", baseDate, 10, 200.0, "Delta");
        Flight flight2 = createFlight("FL002", "NYC", "LAX", baseDate, 20, 250.0, "United");
        
        searchEngine.addFlight(flight1);
        searchEngine.addFlight(flight2);
        
        // Initially both available
        List<Flight> results = searchEngine.searchFlights("NYC", "LAX", baseDate);
        assertEquals(2, results.size());
        
        // Book all seats on flight1
        flight1.bookSeats(10);
        
        // Now only flight2 should appear
        results = searchEngine.searchFlights("NYC", "LAX", baseDate);
        assertEquals(1, results.size());
        assertEquals("FL002", results.get(0).getFlightId());
    }
    
    @Test
    @DisplayName("Integration - Multiple Search Criteria Combined")
    void testMultipleSearchCriteriaCombined() {
        // Add flights with different characteristics
        Flight flight1 = createFlight("FL001", "NYC", "LAX", baseDate, 200, 150.0, "Delta");
        Flight flight2 = createFlight("FL002", "NYC", "LAX", baseDate, 50, 180.0, "Delta");
        Flight flight3 = createFlight("FL003", "NYC", "LAX", baseDate, 150, 220.0, "United");
        
        searchEngine.addFlight(flight1);
        searchEngine.addFlight(flight2);
        searchEngine.addFlight(flight3);
        
        // Search for Delta flights in price range 100-200 with at least 100 seats
        List<Flight> deltaFlights = searchEngine.searchFlightsByAirline("NYC", "LAX", baseDate, "Delta");
        List<Flight> priceFiltered = searchEngine.searchFlightsByPriceRange("NYC", "LAX", baseDate, 100.0, 200.0);
        List<Flight> seatFiltered = searchEngine.searchFlightsBySeatsAvailable("NYC", "LAX", baseDate, 100);
        
        assertEquals(2, deltaFlights.size());
        assertEquals(2, priceFiltered.size());
        assertEquals(2, seatFiltered.size());
        
        // Only FL001 meets all criteria
        Flight fl001 = searchEngine.findFlightById("FL001");
        assertTrue(deltaFlights.contains(fl001));
        assertTrue(priceFiltered.contains(fl001));
        assertTrue(seatFiltered.contains(fl001));
    }
    
    @Test
    @DisplayName("Get Total Flights - Empty Database")
    void testGetTotalFlights_Empty() {
        assertEquals(0, searchEngine.getTotalFlights());
    }
    
    @Test
    @DisplayName("Get Total Flights - After Adding")
    void testGetTotalFlights_AfterAdding() {
        assertEquals(0, searchEngine.getTotalFlights());
        
        Flight flight1 = createFlight("FL001", "NYC", "LAX", baseDate, 100, 200.0, "Delta");
        Flight flight2 = createFlight("FL002", "SFO", "MIA", baseDate, 150, 250.0, "United");
        
        searchEngine.addFlight(flight1);
        assertEquals(1, searchEngine.getTotalFlights());
        
        searchEngine.addFlight(flight2);
        assertEquals(2, searchEngine.getTotalFlights());
    }
}
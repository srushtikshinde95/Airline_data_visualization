package com.airdata.test;

import com.airdata.controller.AppController;
import com.airdata.model.*;
import com.airdata.service.*;
import com.airdata.util.ValidationException;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * Automated Verification Test Suite to validate core logic, services, and OOP behaviors.
 */
public class AppVerificationTest {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("  STARTING AIRLINE DATA VISUALIZATION TEST SUITE  ");
        System.out.println("==================================================");

        int passed = 0;
        int failed = 0;

        try {
            testCsvDataLoader();
            System.out.println("[PASS] CSV Data Loader & Parser Test");
            passed++;
        } catch (Exception e) {
            System.err.println("[FAIL] CSV Data Loader Test: " + e.getMessage());
            e.printStackTrace();
            failed++;
        }

        try {
            testStatisticsCalculator();
            System.out.println("[PASS] Statistics Calculator & KPI Metrics Test");
            passed++;
        } catch (Exception e) {
            System.err.println("[FAIL] Statistics Calculator Test: " + e.getMessage());
            e.printStackTrace();
            failed++;
        }

        try {
            testDataFiltering();
            System.out.println("[PASS] Multi-Criteria Data Filtering Test");
            passed++;
        } catch (Exception e) {
            System.err.println("[FAIL] Data Filtering Test: " + e.getMessage());
            e.printStackTrace();
            failed++;
        }

        try {
            testCrudOperations();
            System.out.println("[PASS] Controller CRUD Operations Test");
            passed++;
        } catch (Exception e) {
            System.err.println("[FAIL] Controller CRUD Test: " + e.getMessage());
            e.printStackTrace();
            failed++;
        }

        try {
            testExportAndReimport();
            System.out.println("[PASS] CSV Export and Re-Import Integrity Test");
            passed++;
        } catch (Exception e) {
            System.err.println("[FAIL] CSV Export Test: " + e.getMessage());
            e.printStackTrace();
            failed++;
        }

        try {
            testOopPolymorphismAndInheritance();
            System.out.println("[PASS] OOP Polymorphism & Inheritance Test");
            passed++;
        } catch (Exception e) {
            System.err.println("[FAIL] OOP Polymorphism Test: " + e.getMessage());
            e.printStackTrace();
            failed++;
        }

        System.out.println("==================================================");
        System.out.println("  TEST RUN SUMMARY: " + passed + " PASSED, " + failed + " FAILED");
        System.out.println("==================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void testCsvDataLoader() throws Exception {
        CSVDataLoader loader = new CSVDataLoader();
        File file = new File("data/sample_airline_data.csv");
        AirlineDataset dataset = loader.loadFromFile(file);

        if (dataset.size() < 50) {
            throw new AssertionError("Expected at least 50 flights, found: " + dataset.size());
        }

        Flight first = dataset.getFlights().get(0);
        if (!"FL-1001".equals(first.getFlightId())) {
            throw new AssertionError("Expected first flight FL-1001, found: " + first.getFlightId());
        }
        if (!"Delta Air Lines".equals(first.getAirline().getName())) {
            throw new AssertionError("Expected Delta Air Lines, found: " + first.getAirline().getName());
        }
        if (!"JFK".equals(first.getOrigin().getIataCode()) || !"LAX".equals(first.getDestination().getIataCode())) {
            throw new AssertionError("Expected JFK -> LAX route");
        }
    }

    private static void testStatisticsCalculator() throws Exception {
        CSVDataLoader loader = new CSVDataLoader();
        AirlineDataset dataset = loader.loadFromFile(new File("data/sample_airline_data.csv"));
        StatisticsCalculator calc = new StatisticsCalculator();

        DatasetSummary summary = calc.calculateSummary(dataset.getFlights());
        if (summary.getTotalFlights() != 60) {
            throw new AssertionError("Expected 60 total flights, got: " + summary.getTotalFlights());
        }
        if (summary.getTotalPassengers() <= 0) {
            throw new AssertionError("Total passengers should be > 0");
        }
        if (summary.getTotalRevenue() <= 0) {
            throw new AssertionError("Total revenue should be > 0");
        }
        if (summary.getUniqueAirlinesCount() < 5) {
            throw new AssertionError("Expected at least 5 unique airlines");
        }
        if (summary.getOnTimePercentage() <= 0.0 || summary.getOnTimePercentage() > 100.0) {
            throw new AssertionError("Invalid on-time percentage: " + summary.getOnTimePercentage());
        }

        Map<String, Integer> byAirline = calc.getFlightCountByAirline(dataset.getFlights());
        if (byAirline.isEmpty() || !byAirline.containsKey("Delta Air Lines")) {
            throw new AssertionError("Flight count map missing Delta Air Lines");
        }

        Map<FlightStatus, Integer> byStatus = calc.getStatusDistribution(dataset.getFlights());
        if (!byStatus.containsKey(FlightStatus.ON_TIME) || !byStatus.containsKey(FlightStatus.CANCELLED)) {
            throw new AssertionError("Status distribution map missing standard status keys");
        }
    }

    private static void testDataFiltering() throws Exception {
        CSVDataLoader loader = new CSVDataLoader();
        AirlineDataset dataset = loader.loadFromFile(new File("data/sample_airline_data.csv"));
        DataFilter filter = new DataFilter();

        // 1. Filter by Airline
        IDataFilter.FilterCriteria crit1 = new IDataFilter.FilterCriteria();
        crit1.setAirline("Emirates");
        List<Flight> emiratesFlights = filter.applyFilter(dataset.getFlights(), crit1);
        if (emiratesFlights.isEmpty()) {
            throw new AssertionError("Expected Emirates flights to be found");
        }
        for (Flight f : emiratesFlights) {
            if (!"Emirates".equalsIgnoreCase(f.getAirline().getName())) {
                throw new AssertionError("Non-Emirates flight found in filtered list: " + f.getAirline().getName());
            }
        }

        // 2. Filter by Status
        IDataFilter.FilterCriteria crit2 = new IDataFilter.FilterCriteria();
        crit2.setStatus(FlightStatus.CANCELLED);
        List<Flight> cancelled = filter.applyFilter(dataset.getFlights(), crit2);
        if (cancelled.isEmpty()) {
            throw new AssertionError("Expected cancelled flights in sample dataset");
        }
        for (Flight f : cancelled) {
            if (f.getStatus() != FlightStatus.CANCELLED) {
                throw new AssertionError("Non-cancelled flight in cancelled filter");
            }
        }

        // 3. Search Query
        IDataFilter.FilterCriteria crit3 = new IDataFilter.FilterCriteria();
        crit3.setSearchQuery("LHR");
        List<Flight> lhrFlights = filter.applyFilter(dataset.getFlights(), crit3);
        if (lhrFlights.isEmpty()) {
            throw new AssertionError("Expected search results for 'LHR'");
        }
    }

    private static void testCrudOperations() throws Exception {
        AppController controller = new AppController();
        controller.loadDataset(new File("data/sample_airline_data.csv"));

        int initialCount = controller.getDataset().size();

        // Create new Flight
        Flight newFlight = new CommercialFlight(
                "FL-9999", new Airline("Test Air"), "TA-100",
                new Route(new Airport("SFO"), new Airport("HND"), 5160),
                LocalDate.of(2026, 3, 10), LocalTime.of(10, 0), LocalTime.of(14, 30),
                630, 250, 800.0, 200000.0, 0, FlightStatus.ON_TIME, "", "Boeing 787", 300
        );

        controller.addFlight(newFlight);
        if (controller.getDataset().size() != initialCount + 1) {
            throw new AssertionError("Failed to add new flight");
        }

        // Update Flight
        newFlight.setPassengerCount(280);
        controller.updateFlight(newFlight);
        Flight fetched = controller.getDataset().findById("FL-9999").orElse(null);
        if (fetched == null || fetched.getPassengerCount() != 280) {
            throw new AssertionError("Failed to update flight");
        }

        // Delete Flight
        boolean deleted = controller.deleteFlight("FL-9999");
        if (!deleted || controller.getDataset().size() != initialCount) {
            throw new AssertionError("Failed to delete flight");
        }
    }

    private static void testExportAndReimport() throws Exception {
        AppController controller = new AppController();
        controller.loadDataset(new File("data/sample_airline_data.csv"));

        File tempExport = File.createTempFile("test_export_", ".csv");
        tempExport.deleteOnExit();

        controller.exportData(tempExport, false);

        CSVDataLoader loader = new CSVDataLoader();
        AirlineDataset reimported = loader.loadFromFile(tempExport);

        if (reimported.size() != controller.getDataset().size()) {
            throw new AssertionError("Export/Re-import record count mismatch: " + reimported.size() + " vs " + controller.getDataset().size());
        }
    }

    private static void testOopPolymorphismAndInheritance() throws Exception {
        Flight standard = new Flight("FL-T1", new Airline("Test"), "T1", new Route(new Airport("A"), new Airport("B"), 100),
                LocalDate.now(), LocalTime.now(), LocalTime.now(), 60, 100, 200.0, 0, 0, FlightStatus.ON_TIME, "");
        
        CommercialFlight comm = new CommercialFlight("FL-T2", new Airline("Test"), "T2", new Route(new Airport("A"), new Airport("B"), 100),
                LocalDate.now(), LocalTime.now(), LocalTime.now(), 60, 100, 200.0, 0, 0, FlightStatus.ON_TIME, "", "Jet", 150);
        comm.setAncillaryRevenuePerPax(30.0);

        // Demonstrating Polymorphism: calculateRevenue()
        double standardRev = standard.calculateRevenue(); // 100 * 200 = 20,000
        double commRev = comm.calculateRevenue(); // 100 * 200 + 100 * 30 = 23,000

        if (standardRev != 20000.0) {
            throw new AssertionError("Standard revenue mismatch: " + standardRev);
        }
        if (commRev != 23000.0) {
            throw new AssertionError("Polymorphic Commercial revenue mismatch: " + commRev);
        }
        if (comm.getLoadFactorPercentage() < 66.6 || comm.getLoadFactorPercentage() > 66.7) {
            throw new AssertionError("Load factor calculation mismatch: " + comm.getLoadFactorPercentage());
        }
    }
}
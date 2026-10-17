package com.airdata.service;

import com.airdata.model.DatasetSummary;
import com.airdata.model.Flight;
import com.airdata.model.FlightStatus;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Concrete implementation of IStatisticsCalculator providing statistical computations.
 * Demonstrates:
 * 1. Method Overriding and Overloading
 * 2. Java Streams Aggregations (Collectors.groupingBy, summingDouble, averagingDouble)
 * 3. Handling empty/edge-case datasets safely
 */
public class StatisticsCalculator implements IStatisticsCalculator {

    @Override
    public DatasetSummary calculateSummary(List<Flight> flights) {
        if (flights == null || flights.isEmpty()) {
            return DatasetSummary.empty();
        }

        int totalFlights = flights.size();
        long totalPassengers = flights.stream().mapToLong(Flight::getPassengerCount).sum();
        double totalRevenue = flights.stream().mapToDouble(Flight::getTotalRevenue).sum();

        int uniqueAirlines = (int) flights.stream().map(f -> f.getAirline().getName()).distinct().count();
        
        Set<String> airports = new HashSet<>();
        flights.forEach(f -> {
            airports.add(f.getOrigin().getIataCode());
            airports.add(f.getDestination().getIataCode());
        });
        int uniqueAirports = airports.size();

        double avgDelay = flights.stream().mapToInt(Flight::getDelayMinutes).average().orElse(0.0);
        int maxDelay = flights.stream().mapToInt(Flight::getDelayMinutes).max().orElse(0);
        int minDelay = flights.stream().mapToInt(Flight::getDelayMinutes).min().orElse(0);

        int cancelled = (int) flights.stream().filter(Flight::isCancelled).count();
        int delayed = (int) flights.stream().filter(f -> f.getStatus() == FlightStatus.DELAYED).count();
        int onTime = (int) flights.stream().filter(Flight::isOnTime).count();

        double onTimePct = totalFlights > 0 ? ((double) onTime / totalFlights) * 100.0 : 0.0;
        double cancelPct = totalFlights > 0 ? ((double) cancelled / totalFlights) * 100.0 : 0.0;

        double avgTicketPrice = flights.stream().mapToDouble(Flight::getTicketPrice).average().orElse(0.0);
        double avgPassengers = totalFlights > 0 ? (double) totalPassengers / totalFlights : 0.0;

        String busiestAirline = getTopKey(flights.stream().collect(Collectors.groupingBy(f -> f.getAirline().getName(), Collectors.counting())));
        String busiestOrigin = getTopKey(flights.stream().collect(Collectors.groupingBy(f -> f.getOrigin().getIataCode(), Collectors.counting())));
        String busiestDest = getTopKey(flights.stream().collect(Collectors.groupingBy(f -> f.getDestination().getIataCode(), Collectors.counting())));

        return new DatasetSummary(
                totalFlights, totalPassengers, totalRevenue,
                uniqueAirlines, uniqueAirports,
                avgDelay, maxDelay, minDelay,
                cancelled, onTime, delayed,
                onTimePct, cancelPct,
                avgTicketPrice, avgPassengers,
                busiestAirline, busiestOrigin, busiestDest
        );
    }

    private <K> K getTopKey(Map<K, Long> map) {
        return map.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    @Override
    public Map<String, Integer> getFlightCountByAirline(List<Flight> flights) {
        if (flights == null || flights.isEmpty()) return Collections.emptyMap();
        return flights.stream()
                .collect(Collectors.groupingBy(f -> f.getAirline().getName(), Collectors.summingInt(f -> 1)))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    @Override
    public Map<String, Long> getPassengerCountByAirline(List<Flight> flights) {
        if (flights == null || flights.isEmpty()) return Collections.emptyMap();
        return flights.stream()
                .collect(Collectors.groupingBy(f -> f.getAirline().getName(), Collectors.summingLong(Flight::getPassengerCount)))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    @Override
    public Map<String, Double> getRevenueByAirline(List<Flight> flights) {
        if (flights == null || flights.isEmpty()) return Collections.emptyMap();
        return flights.stream()
                .collect(Collectors.groupingBy(f -> f.getAirline().getName(), Collectors.summingDouble(Flight::getTotalRevenue)))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    @Override
    public Map<FlightStatus, Integer> getStatusDistribution(List<Flight> flights) {
        Map<FlightStatus, Integer> map = new LinkedHashMap<>();
        for (FlightStatus s : FlightStatus.values()) {
            map.put(s, 0);
        }
        if (flights != null) {
            for (Flight f : flights) {
                map.put(f.getStatus(), map.getOrDefault(f.getStatus(), 0) + 1);
            }
        }
        return map;
    }

    @Override
    public Map<String, Integer> getFlightsOverTime(List<Flight> flights) {
        if (flights == null || flights.isEmpty()) return Collections.emptyMap();
        Map<LocalDate, Long> byDate = flights.stream()
                .filter(f -> f.getDepartureDate() != null)
                .collect(Collectors.groupingBy(Flight::getDepartureDate, TreeMap::new, Collectors.counting()));
        
        Map<String, Integer> result = new LinkedHashMap<>();
        byDate.forEach((date, count) -> result.put(date.toString(), count.intValue()));
        return result;
    }

    @Override
    public Map<String, Double> getRevenueOverTime(List<Flight> flights) {
        if (flights == null || flights.isEmpty()) return Collections.emptyMap();
        Map<LocalDate, Double> byDate = flights.stream()
                .filter(f -> f.getDepartureDate() != null)
                .collect(Collectors.groupingBy(Flight::getDepartureDate, TreeMap::new, Collectors.summingDouble(Flight::getTotalRevenue)));
        
        Map<String, Double> result = new LinkedHashMap<>();
        byDate.forEach((date, rev) -> result.put(date.toString(), rev));
        return result;
    }

    @Override
    public Map<String, Double> getAverageDelayOverTime(List<Flight> flights) {
        if (flights == null || flights.isEmpty()) return Collections.emptyMap();
        Map<LocalDate, Double> byDate = flights.stream()
                .filter(f -> f.getDepartureDate() != null)
                .collect(Collectors.groupingBy(Flight::getDepartureDate, TreeMap::new, Collectors.averagingInt(Flight::getDelayMinutes)));
        
        Map<String, Double> result = new LinkedHashMap<>();
        byDate.forEach((date, avg) -> result.put(date.toString(), avg));
        return result;
    }

    @Override
    public Map<String, Double> getAverageDelayByAirline(List<Flight> flights) {
        if (flights == null || flights.isEmpty()) return Collections.emptyMap();
        return flights.stream()
                .collect(Collectors.groupingBy(f -> f.getAirline().getName(), Collectors.averagingInt(Flight::getDelayMinutes)))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    @Override
    public Map<String, Integer> getTopBusiestRoutes(List<Flight> flights, int limit) {
        if (flights == null || flights.isEmpty()) return Collections.emptyMap();
        return flights.stream()
                .collect(Collectors.groupingBy(f -> f.getRoute().getRouteCode(), Collectors.summingInt(f -> 1)))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit > 0 ? limit : 10)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }
}

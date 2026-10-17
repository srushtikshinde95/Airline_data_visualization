package com.airdata.service;

import com.airdata.model.Flight;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service dedicated to deep route and airport traffic analytics.
 */
public class RouteAnalysisService {

    public static class AirlineScorecard {
        public String airlineName;
        public int totalFlights;
        public long totalPassengers;
        public double totalRevenue;
        public double onTimePercentage;
        public double avgDelayMinutes;
        public double revenuePerPax;
    }

    public List<AirlineScorecard> generateScorecards(List<Flight> flights) {
        if (flights == null || flights.isEmpty()) return Collections.emptyList();

        Map<String, List<Flight>> grouped = flights.stream()
                .collect(Collectors.groupingBy(f -> f.getAirline().getName()));

        List<AirlineScorecard> scorecards = new ArrayList<>();

        grouped.forEach((name, airlineFlights) -> {
            AirlineScorecard sc = new AirlineScorecard();
            sc.airlineName = name;
            sc.totalFlights = airlineFlights.size();
            sc.totalPassengers = airlineFlights.stream().mapToLong(Flight::getPassengerCount).sum();
            sc.totalRevenue = airlineFlights.stream().mapToDouble(Flight::getTotalRevenue).sum();
            
            long onTimeCount = airlineFlights.stream().filter(Flight::isOnTime).count();
            sc.onTimePercentage = sc.totalFlights > 0 ? ((double) onTimeCount / sc.totalFlights) * 100.0 : 0.0;
            
            sc.avgDelayMinutes = airlineFlights.stream().mapToInt(Flight::getDelayMinutes).average().orElse(0.0);
            sc.revenuePerPax = sc.totalPassengers > 0 ? sc.totalRevenue / sc.totalPassengers : 0.0;
            
            scorecards.add(sc);
        });

        scorecards.sort((a, b) -> Double.compare(b.totalRevenue, a.totalRevenue));
        return scorecards;
    }
}

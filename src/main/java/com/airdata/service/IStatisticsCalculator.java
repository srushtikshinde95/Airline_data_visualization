package com.airdata.service;

import com.airdata.model.DatasetSummary;
import com.airdata.model.Flight;
import com.airdata.model.FlightStatus;

import java.util.List;
import java.util.Map;

/**
 * Interface defining contract for computing statistical aggregations, KPIs, and distributions.
 * Demonstrates Abstraction and Method Overloading.
 */
public interface IStatisticsCalculator {
    DatasetSummary calculateSummary(List<Flight> flights);

    Map<String, Integer> getFlightCountByAirline(List<Flight> flights);
    Map<String, Long> getPassengerCountByAirline(List<Flight> flights);
    Map<String, Double> getRevenueByAirline(List<Flight> flights);
    Map<FlightStatus, Integer> getStatusDistribution(List<Flight> flights);
    
    Map<String, Integer> getFlightsOverTime(List<Flight> flights);
    Map<String, Double> getRevenueOverTime(List<Flight> flights);
    Map<String, Double> getAverageDelayOverTime(List<Flight> flights);
    
    Map<String, Double> getAverageDelayByAirline(List<Flight> flights);
    Map<String, Integer> getTopBusiestRoutes(List<Flight> flights, int limit);
}

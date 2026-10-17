package com.airdata.model;

import java.io.Serializable;

/**
 * Immutable DTO representing statistical summary and KPIs computed over an airline dataset.
 */
public class DatasetSummary implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int totalFlights;
    private final long totalPassengers;
    private final double totalRevenue;
    private final int uniqueAirlinesCount;
    private final int uniqueAirportsCount;
    private final double averageDelayMinutes;
    private final int maxDelayMinutes;
    private final int minDelayMinutes;
    private final int cancelledFlightsCount;
    private final int onTimeFlightsCount;
    private final int delayedFlightsCount;
    private final double onTimePercentage;
    private final double cancellationRate;
    private final double averageTicketPrice;
    private final double averagePassengersPerFlight;
    private final String busiestAirline;
    private final String busiestOrigin;
    private final String busiestDestination;

    public DatasetSummary(int totalFlights, long totalPassengers, double totalRevenue,
                          int uniqueAirlinesCount, int uniqueAirportsCount,
                          double averageDelayMinutes, int maxDelayMinutes, int minDelayMinutes,
                          int cancelledFlightsCount, int onTimeFlightsCount, int delayedFlightsCount,
                          double onTimePercentage, double cancellationRate,
                          double averageTicketPrice, double averagePassengersPerFlight,
                          String busiestAirline, String busiestOrigin, String busiestDestination) {
        this.totalFlights = totalFlights;
        this.totalPassengers = totalPassengers;
        this.totalRevenue = totalRevenue;
        this.uniqueAirlinesCount = uniqueAirlinesCount;
        this.uniqueAirportsCount = uniqueAirportsCount;
        this.averageDelayMinutes = averageDelayMinutes;
        this.maxDelayMinutes = maxDelayMinutes;
        this.minDelayMinutes = minDelayMinutes;
        this.cancelledFlightsCount = cancelledFlightsCount;
        this.onTimeFlightsCount = onTimeFlightsCount;
        this.delayedFlightsCount = delayedFlightsCount;
        this.onTimePercentage = onTimePercentage;
        this.cancellationRate = cancellationRate;
        this.averageTicketPrice = averageTicketPrice;
        this.averagePassengersPerFlight = averagePassengersPerFlight;
        this.busiestAirline = (busiestAirline != null) ? busiestAirline : "N/A";
        this.busiestOrigin = (busiestOrigin != null) ? busiestOrigin : "N/A";
        this.busiestDestination = (busiestDestination != null) ? busiestDestination : "N/A";
    }

    public static DatasetSummary empty() {
        return new DatasetSummary(0, 0, 0.0, 0, 0, 0.0, 0, 0, 0, 0, 0, 0.0, 0.0, 0.0, 0.0, "N/A", "N/A", "N/A");
    }

    public int getTotalFlights() { return totalFlights; }
    public long getTotalPassengers() { return totalPassengers; }
    public double getTotalRevenue() { return totalRevenue; }
    public int getUniqueAirlinesCount() { return uniqueAirlinesCount; }
    public int getUniqueAirportsCount() { return uniqueAirportsCount; }
    public double getAverageDelayMinutes() { return averageDelayMinutes; }
    public int getMaxDelayMinutes() { return maxDelayMinutes; }
    public int getMinDelayMinutes() { return minDelayMinutes; }
    public int getCancelledFlightsCount() { return cancelledFlightsCount; }
    public int getOnTimeFlightsCount() { return onTimeFlightsCount; }
    public int getDelayedFlightsCount() { return delayedFlightsCount; }
    public double getOnTimePercentage() { return onTimePercentage; }
    public double getCancellationRate() { return cancellationRate; }
    public double getAverageTicketPrice() { return averageTicketPrice; }
    public double getAveragePassengersPerFlight() { return averagePassengersPerFlight; }
    public String getBusiestAirline() { return busiestAirline; }
    public String getBusiestOrigin() { return busiestOrigin; }
    public String getBusiestDestination() { return busiestDestination; }
}

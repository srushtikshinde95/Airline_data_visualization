package com.airdata.model;

import com.airdata.util.ValidationException;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Specialized subclass representing commercial passenger flights.
 * Demonstrates:
 * 1. Inheritance (extends Flight)
 * 2. Polymorphism (Overriding calculateRevenue() and toString())
 * 3. Specific business attributes (Aircraft type, Max Seating Capacity, Load Factor)
 */
public class CommercialFlight extends Flight {
    private static final long serialVersionUID = 1L;

    private String aircraftType;
    private int maxSeatCapacity;
    private double ancillaryRevenuePerPax; // Baggage, seat selection, meals

    public CommercialFlight() {
        super();
        this.aircraftType = "Boeing 737 / Airbus A320";
        this.maxSeatCapacity = 180;
        this.ancillaryRevenuePerPax = 25.0;
    }

    public CommercialFlight(String flightId, Airline airline, String flightNumber, Route route,
                            LocalDate departureDate, LocalTime departureTime, LocalTime arrivalTime,
                            int durationMinutes, int passengerCount, double ticketPrice,
                            double totalRevenue, int delayMinutes, FlightStatus status,
                            String cancellationReason, String aircraftType, int maxSeatCapacity)
            throws ValidationException {
        super(flightId, airline, flightNumber, route, departureDate, departureTime, arrivalTime,
              durationMinutes, passengerCount, ticketPrice, totalRevenue, delayMinutes, status,
              cancellationReason);
        this.aircraftType = (aircraftType != null) ? aircraftType : "Commercial Jet";
        this.maxSeatCapacity = Math.max(passengerCount, maxSeatCapacity);
        this.ancillaryRevenuePerPax = 25.0;
    }

    public String getAircraftType() {
        return aircraftType;
    }

    public void setAircraftType(String aircraftType) {
        this.aircraftType = aircraftType;
    }

    public int getMaxSeatCapacity() {
        return maxSeatCapacity;
    }

    public void setMaxSeatCapacity(int maxSeatCapacity) {
        this.maxSeatCapacity = Math.max(1, maxSeatCapacity);
    }

    public double getAncillaryRevenuePerPax() {
        return ancillaryRevenuePerPax;
    }

    public void setAncillaryRevenuePerPax(double ancillaryRevenuePerPax) {
        this.ancillaryRevenuePerPax = Math.max(0.0, ancillaryRevenuePerPax);
    }

    /**
     * Calculates passenger load factor (Percentage of seats occupied).
     */
    public double getLoadFactorPercentage() {
        if (maxSeatCapacity <= 0) return 0.0;
        return ((double) getPassengerCount() / maxSeatCapacity) * 100.0;
    }

    /**
     * Polymorphic Overriding: Total commercial revenue incorporates ticket sales + ancillary fees.
     */
    @Override
    public double calculateRevenue() {
        double baseTicketRevenue = getPassengerCount() * getTicketPrice();
        double ancillaryTotal = getPassengerCount() * ancillaryRevenuePerPax;
        return baseTicketRevenue + ancillaryTotal;
    }

    @Override
    public String toString() {
        return "CommercialFlight[" + getFlightId() + ", " + getAirline().getName() + " " +
                getFlightNumber() + ", Aircraft=" + aircraftType + ", Load=" +
                String.format("%.1f%%", getLoadFactorPercentage()) + "]";
    }
}

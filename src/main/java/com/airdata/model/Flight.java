package com.airdata.model;

import com.airdata.util.ValidationException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Core domain entity representing a scheduled flight.
 * Demonstrates:
 * 1. Encapsulation (Private fields with validated getters and setters)
 * 2. Inheritance (Extends BaseEntity)
 * 3. Composition / Aggregation (Has-a Airline, Airport, Route, and FlightStatus)
 * 4. Method Overloading & Overriding (Constructors, calculations, toString, equals)
 */
public class Flight extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Airline airline;
    private String flightNumber;
    private Route route;
    private LocalDate departureDate;
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    private int durationMinutes;
    private int passengerCount;
    private double ticketPrice;
    private double totalRevenue;
    private int delayMinutes;
    private FlightStatus status;
    private String cancellationReason;

    public Flight() {
        super();
        this.status = FlightStatus.ON_TIME;
        this.airline = new Airline("Unknown");
        this.route = new Route(new Airport("UNK"), new Airport("UNK"), 0);
    }

    /**
     * Primary Parameterized Constructor.
     */
    public Flight(String flightId, Airline airline, String flightNumber, Route route,
                  LocalDate departureDate, LocalTime departureTime, LocalTime arrivalTime,
                  int durationMinutes, int passengerCount, double ticketPrice,
                  double totalRevenue, int delayMinutes, FlightStatus status,
                  String cancellationReason) throws ValidationException {
        super(flightId);
        validateFlightId(flightId);
        this.airline = (airline != null) ? airline : new Airline("Unknown");
        this.flightNumber = (flightNumber != null) ? flightNumber.trim() : "N/A";
        this.route = (route != null) ? route : new Route(new Airport("UNK"), new Airport("UNK"), 0);
        this.departureDate = departureDate;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.durationMinutes = Math.max(0, durationMinutes);
        this.passengerCount = Math.max(0, passengerCount);
        this.ticketPrice = Math.max(0.0, ticketPrice);
        
        // Auto-calculate revenue if not explicitly provided
        if (totalRevenue > 0) {
            this.totalRevenue = totalRevenue;
        } else {
            this.totalRevenue = calculateRevenue();
        }
        
        this.delayMinutes = Math.max(0, delayMinutes);
        this.status = (status != null) ? status : (delayMinutes > 0 ? FlightStatus.DELAYED : FlightStatus.ON_TIME);
        this.cancellationReason = (cancellationReason != null) ? cancellationReason.trim() : "";
    }

    /**
     * Validation helper (Encapsulation and defensive programming).
     */
    private void validateFlightId(String id) throws ValidationException {
        if (id == null || id.trim().isEmpty()) {
            throw new ValidationException("Flight ID cannot be null or empty");
        }
    }

    /**
     * Calculates base revenue. Can be overridden by subclasses (Polymorphism).
     */
    public double calculateRevenue() {
        return this.passengerCount * this.ticketPrice;
    }

    // --- Getters and Setters with Encapsulation ---

    public String getFlightId() {
        return getId();
    }

    public void setFlightId(String flightId) {
        setId(flightId);
    }

    public Airline getAirline() {
        return airline;
    }

    public void setAirline(Airline airline) {
        this.airline = (airline != null) ? airline : new Airline("Unknown");
        markUpdated();
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
        markUpdated();
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
        markUpdated();
    }

    public Airport getOrigin() {
        return route != null ? route.getOrigin() : new Airport("UNK");
    }

    public Airport getDestination() {
        return route != null ? route.getDestination() : new Airport("UNK");
    }

    public double getDistanceMiles() {
        return route != null ? route.getDistanceMiles() : 0.0;
    }

    public LocalDate getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(LocalDate departureDate) {
        this.departureDate = departureDate;
        markUpdated();
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalTime departureTime) {
        this.departureTime = departureTime;
        markUpdated();
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalTime arrivalTime) {
        this.arrivalTime = arrivalTime;
        markUpdated();
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = Math.max(0, durationMinutes);
        markUpdated();
    }

    public int getPassengerCount() {
        return passengerCount;
    }

    public void setPassengerCount(int passengerCount) {
        this.passengerCount = Math.max(0, passengerCount);
        this.totalRevenue = calculateRevenue();
        markUpdated();
    }

    public double getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(double ticketPrice) {
        this.ticketPrice = Math.max(0.0, ticketPrice);
        this.totalRevenue = calculateRevenue();
        markUpdated();
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
        markUpdated();
    }

    public int getDelayMinutes() {
        return delayMinutes;
    }

    public void setDelayMinutes(int delayMinutes) {
        this.delayMinutes = Math.max(0, delayMinutes);
        if (this.delayMinutes > 0 && this.status != FlightStatus.CANCELLED) {
            this.status = FlightStatus.DELAYED;
        }
        markUpdated();
    }

    public FlightStatus getStatus() {
        return status;
    }

    public void setStatus(FlightStatus status) {
        this.status = (status != null) ? status : FlightStatus.ON_TIME;
        markUpdated();
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = (cancellationReason != null) ? cancellationReason.trim() : "";
        if (!this.cancellationReason.isEmpty()) {
            this.status = FlightStatus.CANCELLED;
        }
        markUpdated();
    }

    public boolean isCancelled() {
        return this.status == FlightStatus.CANCELLED;
    }

    public boolean isDelayed() {
        return this.status == FlightStatus.DELAYED || this.delayMinutes > 0;
    }

    public boolean isOnTime() {
        return this.status == FlightStatus.ON_TIME && this.delayMinutes == 0;
    }

    @Override
    public String getDisplayName() {
        return flightNumber + " (" + getOrigin().getIataCode() + " -> " + getDestination().getIataCode() + ")";
    }

    @Override
    public String toString() {
        return "Flight[" + getId() + ", " + airline.getName() + " " + flightNumber + ", " +
                getOrigin().getIataCode() + "->" + getDestination().getIataCode() + ", " + status + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Flight flight = (Flight) o;
        return Objects.equals(flightNumber, flight.flightNumber) &&
               Objects.equals(departureDate, flight.departureDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), flightNumber, departureDate);
    }
}

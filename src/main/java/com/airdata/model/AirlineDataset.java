package com.airdata.model;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Aggregate Root model representing the collection of flight records.
 * Demonstrates:
 * 1. Aggregation / Composition (Encapsulates collection of Flight domain entities)
 * 2. Defensive Copying (Prevents external corruption of internal dataset)
 * 3. Thread-safe operations
 */
public class AirlineDataset implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<Flight> flights;
    private String datasetName;
    private String sourceFilePath;

    public AirlineDataset() {
        this("Airline Dataset", "");
    }

    public AirlineDataset(String datasetName, String sourceFilePath) {
        this.datasetName = datasetName;
        this.sourceFilePath = sourceFilePath;
        this.flights = new CopyOnWriteArrayList<>();
    }

    public void addFlight(Flight flight) {
        if (flight != null) {
            flights.add(flight);
        }
    }

    public void addAll(Collection<? extends Flight> newFlights) {
        if (newFlights != null) {
            flights.addAll(newFlights);
        }
    }

    public boolean removeFlight(Flight flight) {
        return flights.remove(flight);
    }

    public boolean removeFlightById(String flightId) {
        return flights.removeIf(f -> f.getFlightId().equalsIgnoreCase(flightId));
    }

    public void clear() {
        flights.clear();
    }

    public int size() {
        return flights.size();
    }

    public boolean isEmpty() {
        return flights.isEmpty();
    }

    /**
     * Returns an unmodifiable defensive copy of the flights list (Encapsulation).
     */
    public List<Flight> getFlights() {
        return Collections.unmodifiableList(new ArrayList<>(flights));
    }

    public Optional<Flight> findById(String flightId) {
        if (flightId == null) return Optional.empty();
        return flights.stream()
                .filter(f -> f.getFlightId().equalsIgnoreCase(flightId.trim()))
                .findFirst();
    }

    public Set<String> getUniqueAirlines() {
        return flights.stream()
                .map(f -> f.getAirline().getName())
                .filter(name -> !name.isEmpty())
                .collect(Collectors.toCollection(TreeSet::new));
    }

    public Set<String> getUniqueOrigins() {
        return flights.stream()
                .map(f -> f.getOrigin().getIataCode())
                .filter(code -> !code.isEmpty())
                .collect(Collectors.toCollection(TreeSet::new));
    }

    public Set<String> getUniqueDestinations() {
        return flights.stream()
                .map(f -> f.getDestination().getIataCode())
                .filter(code -> !code.isEmpty())
                .collect(Collectors.toCollection(TreeSet::new));
    }

    public String getDatasetName() {
        return datasetName;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    public String getSourceFilePath() {
        return sourceFilePath;
    }

    public void setSourceFilePath(String sourceFilePath) {
        this.sourceFilePath = sourceFilePath;
    }
}

package com.airdata.controller;

import com.airdata.model.AirlineDataset;
import com.airdata.model.DatasetSummary;
import com.airdata.model.Flight;
import com.airdata.service.*;
import com.airdata.util.ValidationException;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Main Application Controller implementing the Controller in MVC architecture.
 * Manages active dataset state, coordinates services, and notifies view observers.
 */
public class AppController {

    private final IDataLoader dataLoader;
    private final IDataExporter dataExporter;
    private final IDataFilter dataFilter;
    private final IStatisticsCalculator statsCalculator;
    private final RouteAnalysisService routeAnalysisService;

    private AirlineDataset dataset;
    private List<Flight> filteredFlights;
    private DatasetSummary currentSummary;
    private IDataFilter.FilterCriteria activeCriteria;

    private final List<IDatasetListener> listeners;

    public AppController() {
        this(new CSVDataLoader(), new CSVDataExporter(), new DataFilter(), new StatisticsCalculator(), new RouteAnalysisService());
    }

    /**
     * Dependency Injection constructor (Loose coupling & Testability).
     */
    public AppController(IDataLoader dataLoader, IDataExporter dataExporter, IDataFilter dataFilter,
                         IStatisticsCalculator statsCalculator, RouteAnalysisService routeAnalysisService) {
        this.dataLoader = dataLoader;
        this.dataExporter = dataExporter;
        this.dataFilter = dataFilter;
        this.statsCalculator = statsCalculator;
        this.routeAnalysisService = routeAnalysisService;

        this.dataset = new AirlineDataset("Empty Dataset", "");
        this.filteredFlights = new ArrayList<>();
        this.currentSummary = DatasetSummary.empty();
        this.activeCriteria = new IDataFilter.FilterCriteria();
        this.listeners = new ArrayList<>();
    }

    public void addListener(IDatasetListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
            if (dataset != null && currentSummary != null && !dataset.isEmpty()) {
                listener.onDatasetChanged(dataset, Collections.unmodifiableList(filteredFlights), currentSummary);
            }
        }
    }

    public void removeListener(IDatasetListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (IDatasetListener listener : listeners) {
            listener.onDatasetChanged(dataset, Collections.unmodifiableList(filteredFlights), currentSummary);
        }
    }

    /**
     * Load dataset from a user-specified CSV file.
     */
    public void loadDataset(File csvFile) throws ValidationException {
        this.dataset = dataLoader.loadFromFile(csvFile);
        this.activeCriteria.reset();
        refreshFilteredData();
    }

    /**
     * Load default bundled dataset from classpath resources.
     */
    public void loadDefaultDataset() {
        try {
            InputStream is = getClass().getResourceAsStream("/sample_airline_data.csv");
            if (is != null) {
                this.dataset = dataLoader.loadFromInputStream(is, "Sample Dataset (Built-in)");
            } else {
                File fallback = new File("data/sample_airline_data.csv");
                if (fallback.exists()) {
                    this.dataset = dataLoader.loadFromFile(fallback);
                }
            }
        } catch (Exception e) {
            System.err.println("Could not load default dataset: " + e.getMessage());
        }
        this.activeCriteria.reset();
        refreshFilteredData();
    }

    /**
     * Apply multi-criteria filters.
     */
    public void applyFilter(IDataFilter.FilterCriteria criteria) {
        this.activeCriteria = (criteria != null) ? criteria : new IDataFilter.FilterCriteria();
        refreshFilteredData();
    }

    /**
     * Reset all active filters.
     */
    public void resetFilters() {
        this.activeCriteria.reset();
        refreshFilteredData();
    }

    /**
     * Adds a new flight record to the dataset.
     */
    public void addFlight(Flight flight) throws ValidationException {
        if (flight == null) {
            throw new ValidationException("Flight record cannot be null.");
        }
        dataset.addFlight(flight);
        refreshFilteredData();
    }

    /**
     * Updates an existing flight record.
     */
    public void updateFlight(Flight flight) throws ValidationException {
        if (flight == null) {
            throw new ValidationException("Flight record cannot be null.");
        }
        dataset.removeFlightById(flight.getFlightId());
        dataset.addFlight(flight);
        refreshFilteredData();
    }

    /**
     * Deletes a flight record by ID.
     */
    public boolean deleteFlight(String flightId) {
        boolean removed = dataset.removeFlightById(flightId);
        if (removed) {
            refreshFilteredData();
        }
        return removed;
    }

    /**
     * Exports current view or entire dataset to a CSV file.
     */
    public void exportData(File targetFile, boolean onlyFiltered) throws ValidationException {
        List<Flight> toExport = onlyFiltered ? filteredFlights : dataset.getFlights();
        dataExporter.exportToFile(toExport, targetFile);
    }

    private void refreshFilteredData() {
        this.filteredFlights = dataFilter.applyFilter(dataset.getFlights(), activeCriteria);
        this.currentSummary = statsCalculator.calculateSummary(filteredFlights);
        notifyListeners();
    }

    // --- Accessors ---
    public AirlineDataset getDataset() { return dataset; }
    public List<Flight> getFilteredFlights() { return Collections.unmodifiableList(filteredFlights); }
    public DatasetSummary getCurrentSummary() { return currentSummary; }
    public IDataFilter.FilterCriteria getActiveCriteria() { return activeCriteria; }
    public IStatisticsCalculator getStatsCalculator() { return statsCalculator; }
    public RouteAnalysisService getRouteAnalysisService() { return routeAnalysisService; }
}

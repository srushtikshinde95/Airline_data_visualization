package com.airdata.controller;

import com.airdata.model.AirlineDataset;
import com.airdata.model.DatasetSummary;
import com.airdata.model.Flight;
import java.util.List;

/**
 * Observer pattern interface for listening to dataset lifecycle and filter events.
 * Demonstrates:
 * 1. Observer Design Pattern
 * 2. Loose Coupling between Model and View
 */
public interface IDatasetListener {
    void onDatasetChanged(AirlineDataset fullDataset, List<Flight> currentViewFlights, DatasetSummary currentSummary);
}

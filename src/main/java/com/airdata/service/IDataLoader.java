package com.airdata.service;

import com.airdata.model.AirlineDataset;
import com.airdata.util.ValidationException;
import java.io.File;
import java.io.InputStream;

/**
 * Interface defining contract for dataset loading services.
 * Demonstrates:
 * 1. Abstraction (Defines methods without implementation)
 * 2. Polymorphism (Allows alternative loaders e.g., JSON, Database, Mock)
 */
public interface IDataLoader {
    AirlineDataset loadFromFile(File file) throws ValidationException;
    AirlineDataset loadFromInputStream(InputStream inputStream, String sourceName) throws ValidationException;
}

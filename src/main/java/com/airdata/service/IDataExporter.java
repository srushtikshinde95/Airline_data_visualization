package com.airdata.service;

import com.airdata.model.Flight;
import com.airdata.util.ValidationException;
import java.io.File;
import java.util.List;

/**
 * Interface defining contract for exporting flight records to external storage.
 */
public interface IDataExporter {
    void exportToFile(List<Flight> flights, File targetFile) throws ValidationException;
}

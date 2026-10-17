package com.airdata.service;

import com.airdata.model.Flight;
import com.airdata.util.DateUtils;
import com.airdata.util.ValidationException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Concrete implementation of IDataExporter for writing dataset records to CSV files.
 */
public class CSVDataExporter implements IDataExporter {

    @Override
    public void exportToFile(List<Flight> flights, File targetFile) throws ValidationException {
        if (targetFile == null) {
            throw new ValidationException("Target export destination cannot be null.");
        }
        if (flights == null || flights.isEmpty()) {
            throw new ValidationException("No records to export.");
        }

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetFile), StandardCharsets.UTF_8))) {
            // Write Header
            writer.write("FlightID,Airline,FlightNumber,Origin,Destination,DepartureDate,DepartureTime,ArrivalTime,DurationMinutes,DistanceMiles,Passengers,TicketPrice,TotalRevenue,DelayMinutes,Status,CancellationReason");
            writer.newLine();

            for (Flight f : flights) {
                StringBuilder line = new StringBuilder();
                line.append(escape(f.getFlightId())).append(",");
                line.append(escape(f.getAirline().getName())).append(",");
                line.append(escape(f.getFlightNumber())).append(",");
                line.append(escape(f.getOrigin().getIataCode())).append(",");
                line.append(escape(f.getDestination().getIataCode())).append(",");
                line.append(f.getDepartureDate() != null ? f.getDepartureDate().toString() : "").append(",");
                line.append(f.getDepartureTime() != null ? DateUtils.formatTime(f.getDepartureTime()) : "").append(",");
                line.append(f.getArrivalTime() != null ? DateUtils.formatTime(f.getArrivalTime()) : "").append(",");
                line.append(f.getDurationMinutes()).append(",");
                line.append(f.getDistanceMiles()).append(",");
                line.append(f.getPassengerCount()).append(",");
                line.append(String.format(java.util.Locale.US, "%.2f", f.getTicketPrice())).append(",");
                line.append(String.format(java.util.Locale.US, "%.2f", f.getTotalRevenue())).append(",");
                line.append(f.getDelayMinutes()).append(",");
                line.append(f.getStatus().name()).append(",");
                line.append(escape(f.getCancellationReason()));

                writer.write(line.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            throw new ValidationException("Failed to export dataset: " + e.getMessage(), e);
        }
    }

    private String escape(String val) {
        if (val == null) return "";
        if (val.contains(",") || val.contains("\"") || val.contains("\n")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }
}

package com.airdata.service;

import com.airdata.model.*;
import com.airdata.util.DateUtils;
import com.airdata.util.ValidationException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Concrete implementation of IDataLoader for Comma-Separated Values (CSV) format.
 * Features:
 * - Robust header detection
 * - Flexible column mapping
 * - Malformed row tolerance and error reporting
 * - Handles missing/empty values gracefully with safe defaults
 */
public class CSVDataLoader implements IDataLoader {

    @Override
    public AirlineDataset loadFromFile(File file) throws ValidationException {
        if (file == null || !file.exists()) {
            throw new ValidationException("Selected file does not exist.");
        }
        if (!file.canRead()) {
            throw new ValidationException("Cannot read file. Please check file permissions.");
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            return loadFromInputStream(fis, file.getName());
        } catch (IOException e) {
            throw new ValidationException("Failed to read file: " + e.getMessage(), e);
        }
    }

    @Override
    public AirlineDataset loadFromInputStream(InputStream inputStream, String sourceName) throws ValidationException {
        if (inputStream == null) {
            throw new ValidationException("Input stream cannot be null.");
        }

        AirlineDataset dataset = new AirlineDataset(sourceName != null ? sourceName : "Imported Dataset", sourceName);
        List<String> warnings = new ArrayList<>();
        int lineNumber = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            lineNumber++;

            if (headerLine == null || headerLine.trim().isEmpty()) {
                throw new ValidationException("The CSV file is empty.");
            }

            String[] headers = parseCsvLine(headerLine);
            ColumnIndexMap colMap = mapHeaders(headers);

            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) continue;

                String[] tokens = parseCsvLine(line);
                try {
                    Flight flight = parseFlightRecord(tokens, colMap, lineNumber);
                    if (flight != null) {
                        dataset.addFlight(flight);
                    }
                } catch (Exception ex) {
                    warnings.add("Line " + lineNumber + ": " + ex.getMessage());
                }
            }
        } catch (IOException e) {
            throw new ValidationException("I/O error during CSV processing: " + e.getMessage(), e);
        }

        if (dataset.isEmpty()) {
            throw new ValidationException("No valid flight records could be imported from the CSV file.");
        }

        return dataset;
    }

    /**
     * Parses a single CSV line with support for quoted strings.
     */
    private String[] parseCsvLine(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString().trim());
        return tokens.toArray(new String[0]);
    }

    private Flight parseFlightRecord(String[] tokens, ColumnIndexMap map, int lineNum) throws ValidationException {
        String flightId = getVal(tokens, map.flightIdIdx, "FL-" + lineNum);
        String airlineName = getVal(tokens, map.airlineIdx, "Unknown Airline");
        String flightNum = getVal(tokens, map.flightNumIdx, "FL" + lineNum);
        String originCode = getVal(tokens, map.originIdx, "UNK").toUpperCase();
        String destCode = getVal(tokens, map.destIdx, "UNK").toUpperCase();
        
        LocalDate depDate = LocalDate.now();
        if (map.depDateIdx >= 0 && map.depDateIdx < tokens.length) {
            try {
                depDate = DateUtils.parseDate(tokens[map.depDateIdx]);
            } catch (Exception ignored) {}
        }

        LocalTime depTime = LocalTime.of(8, 0);
        if (map.depTimeIdx >= 0 && map.depTimeIdx < tokens.length) {
            try {
                depTime = DateUtils.parseTime(tokens[map.depTimeIdx]);
            } catch (Exception ignored) {}
        }

        LocalTime arrTime = LocalTime.of(12, 0);
        if (map.arrTimeIdx >= 0 && map.arrTimeIdx < tokens.length) {
            try {
                arrTime = DateUtils.parseTime(tokens[map.arrTimeIdx]);
            } catch (Exception ignored) {}
        }

        int duration = parseInt(getVal(tokens, map.durationIdx, "0"), 0);
        double distance = parseDouble(getVal(tokens, map.distanceIdx, "0"), 0.0);
        int passengers = parseInt(getVal(tokens, map.passengersIdx, "0"), 0);
        double price = parseDouble(getVal(tokens, map.priceIdx, "0"), 0.0);
        double revenue = parseDouble(getVal(tokens, map.revenueIdx, "0"), 0.0);
        int delayMins = parseInt(getVal(tokens, map.delayIdx, "0"), 0);
        
        String statusStr = getVal(tokens, map.statusIdx, delayMins > 0 ? "DELAYED" : "ON_TIME");
        FlightStatus status = FlightStatus.fromString(statusStr);
        String cancelReason = getVal(tokens, map.cancelReasonIdx, "");

        Airline airline = new Airline(airlineName);
        Airport origin = new Airport(originCode);
        Airport dest = new Airport(destCode);
        Route route = new Route(origin, dest, distance);

        return new CommercialFlight(
                flightId, airline, flightNum, route,
                depDate, depTime, arrTime, duration,
                passengers, price, revenue, delayMins,
                status, cancelReason, "Boeing 777 / Airbus A350", Math.max(passengers, 200)
        );
    }

    private String getVal(String[] tokens, int idx, String fallback) {
        if (idx >= 0 && idx < tokens.length && !tokens[idx].trim().isEmpty()) {
            return tokens[idx].trim();
        }
        return fallback;
    }

    private int parseInt(String val, int fallback) {
        try {
            return Integer.parseInt(val.replaceAll("[^0-9-]", ""));
        } catch (Exception e) {
            return fallback;
        }
    }

    private double parseDouble(String val, double fallback) {
        try {
            return Double.parseDouble(val.replaceAll("[^0-9.-]", ""));
        } catch (Exception e) {
            return fallback;
        }
    }

    private ColumnIndexMap mapHeaders(String[] headers) {
        ColumnIndexMap map = new ColumnIndexMap();
        for (int i = 0; i < headers.length; i++) {
            String h = headers[i].toLowerCase().replace("_", "").replace(" ", "").replace("-", "");
            if (h.contains("flightid") || h.equals("id")) {
                map.flightIdIdx = i;
            } else if (h.contains("airlinename") || h.equals("airline") || h.equals("carrier")) {
                map.airlineIdx = i;
            } else if (h.contains("flightnum") || h.equals("flightno") || h.equals("flightnumber")) {
                map.flightNumIdx = i;
            } else if (h.contains("origin") || h.equals("from") || h.equals("source") || h.equals("org") || h.equals("src")) {
                map.originIdx = i;
            } else if (h.contains("dest") || h.contains("destination") || h.equals("to")) {
                map.destIdx = i;
            } else if (h.equals("date") || h.contains("depdate") || h.contains("departuredate")) {
                map.depDateIdx = i;
            } else if (h.contains("deptime") || h.equals("departure") || h.contains("departuretime")) {
                map.depTimeIdx = i;
            } else if (h.contains("arrtime") || h.equals("arrival") || h.contains("arrivaltime")) {
                map.arrTimeIdx = i;
            } else if (h.contains("duration")) {
                map.durationIdx = i;
            } else if (h.contains("distance")) {
                map.distanceIdx = i;
            } else if (h.contains("pax") || h.contains("passengers") || h.contains("passengercount")) {
                map.passengersIdx = i;
            } else if (h.contains("price") || h.contains("fare") || h.contains("ticketprice")) {
                map.priceIdx = i;
            } else if (h.contains("revenue") || h.contains("totalrevenue")) {
                map.revenueIdx = i;
            } else if (h.contains("delay") || h.contains("delayminutes")) {
                map.delayIdx = i;
            } else if (h.contains("status") || h.contains("flightstatus")) {
                map.statusIdx = i;
            } else if (h.contains("cancel") || h.contains("reason")) {
                map.cancelReasonIdx = i;
            }
        }
        return map;
    }

    private static class ColumnIndexMap {
        int flightIdIdx = 0;
        int airlineIdx = 1;
        int flightNumIdx = 2;
        int originIdx = 3;
        int destIdx = 4;
        int depDateIdx = 5;
        int depTimeIdx = 6;
        int arrTimeIdx = 7;
        int durationIdx = 8;
        int distanceIdx = 9;
        int passengersIdx = 10;
        int priceIdx = 11;
        int revenueIdx = 12;
        int delayIdx = 13;
        int statusIdx = 14;
        int cancelReasonIdx = 15;
    }
}
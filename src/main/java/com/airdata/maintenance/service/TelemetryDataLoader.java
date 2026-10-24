package com.airdata.maintenance.service;

import com.airdata.maintenance.model.AircraftComponentType;
import com.airdata.maintenance.model.SensorReading;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Robust CSV loader for aircraft sensor telemetry.
 * Implements validation, range checks, deduplication, and missing value imputation.
 */
public class TelemetryDataLoader {

    public List<SensorReading> loadFromFile(File file) throws IOException {
        if (!file.exists()) {
            throw new FileNotFoundException("Sensor telemetry file not found: " + file.getAbsolutePath());
        }

        List<SensorReading> readings = new ArrayList<>();
        Set<String> seenKeys = new HashSet<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IOException("Sensor telemetry file is empty: " + file.getName());
            }

            String[] headers = headerLine.split(",");
            Map<String, Integer> colMap = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                colMap.put(headers[i].trim().toLowerCase(), i);
            }

            // Verify essential columns
            String[] required = {"aircraft_id", "component", "cycle_number", "egt_celsius", "core_vibration_ips"};
            for (String req : required) {
                if (!colMap.containsKey(req)) {
                    throw new IOException("Missing required column in telemetry CSV: " + req);
                }
            }

            String line;
            int lineNum = 1;

            while ((line = reader.readLine()) != null) {
                lineNum++;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] tokens = line.split(",", -1);
                try {
                    String acId = getStr(tokens, colMap, "aircraft_id", "UNKNOWN");
                    String compStr = getStr(tokens, colMap, "component", "ENGINE_1");
                    int cycle = getInt(tokens, colMap, "cycle_number", 1);

                    // Deduplication check
                    String dedupeKey = acId + "|" + compStr + "|" + cycle;
                    if (seenKeys.contains(dedupeKey)) {
                        continue; // Skip duplicate cycle entry
                    }
                    seenKeys.add(dedupeKey);

                    SensorReading r = new SensorReading();
                    r.setRecordId(getStr(tokens, colMap, "record_id", "REC-" + lineNum));
                    r.setAircraftId(acId);
                    r.setAircraftModel(getStr(tokens, colMap, "aircraft_model", "Boeing 737"));
                    r.setComponent(AircraftComponentType.fromString(compStr));
                    r.setFlightId(getStr(tokens, colMap, "flight_id", "FL-1001"));
                    r.setCycleNumber(cycle);
                    r.setTotalOperatingHours(getDouble(tokens, colMap, "total_operating_hours", 4000.0));
                    r.setCyclesSinceLastMaint(getInt(tokens, colMap, "cycles_since_last_maint", cycle % 100));

                    // Sensors with physics boundary validation
                    r.setEgtCelsius(clamp(getDouble(tokens, colMap, "egt_celsius", 670.0), 200.0, 1300.0));
                    r.setCoreVibrationIps(clamp(getDouble(tokens, colMap, "core_vibration_ips", 1.0), 0.0, 15.0));
                    r.setFanVibrationIps(clamp(getDouble(tokens, colMap, "fan_vibration_ips", 0.8), 0.0, 12.0));
                    r.setOilPressurePsi(clamp(getDouble(tokens, colMap, "oil_pressure_psi", 55.0), 5.0, 120.0));
                    r.setOilTempCelsius(clamp(getDouble(tokens, colMap, "oil_temp_celsius", 100.0), 0.0, 250.0));
                    r.setFuelFlowPph(clamp(getDouble(tokens, colMap, "fuel_flow_pph", 2100.0), 500.0, 6000.0));
                    r.setHydraulicPressurePsi(clamp(getDouble(tokens, colMap, "hydraulic_pressure_psi", 3000.0), 500.0, 4500.0));
                    r.setHydraulicTempCelsius(clamp(getDouble(tokens, colMap, "hydraulic_temp_celsius", 60.0), -20.0, 180.0));
                    r.setBrakeTempCelsius(clamp(getDouble(tokens, colMap, "brake_temp_celsius", 200.0), -20.0, 750.0));
                    r.setApuEgtCelsius(clamp(getDouble(tokens, colMap, "apu_egt_celsius", 600.0), 100.0, 1000.0));
                    r.setBleedPressurePsi(clamp(getDouble(tokens, colMap, "bleed_pressure_psi", 40.0), 10.0, 90.0));
                    r.setN1SpeedPct(clamp(getDouble(tokens, colMap, "n1_speed_pct", 94.0), 20.0, 120.0));
                    r.setN2SpeedPct(clamp(getDouble(tokens, colMap, "n2_speed_pct", 97.0), 20.0, 120.0));
                    r.setAmbientTempCelsius(getDouble(tokens, colMap, "ambient_temp_celsius", -20.0));
                    r.setAltitudeFeet(getDouble(tokens, colMap, "altitude_feet", 32000.0));

                    r.setActualRulCycles(getInt(tokens, colMap, "actual_rul_cycles", 100));
                    r.setFailureWithin30Cycles(getInt(tokens, colMap, "failure_within_30_cycles", 0));
                    r.setAnomalyFlag(getInt(tokens, colMap, "anomaly_flag", 0));
                    r.setFaultMode(getStr(tokens, colMap, "fault_mode", "NONE"));
                    r.setTimestamp(LocalDateTime.now().minusHours(lineNum % 48));

                    readings.add(r);
                } catch (Exception e) {
                    System.err.println("Warning: Skipping malformed row " + lineNum + ": " + e.getMessage());
                }
            }
        }

        return readings;
    }

    private String getStr(String[] tokens, Map<String, Integer> map, String key, String def) {
        Integer idx = map.get(key);
        if (idx != null && idx < tokens.length) {
            String val = tokens[idx].trim();
            if (!val.isEmpty()) return val;
        }
        return def;
    }

    private int getInt(String[] tokens, Map<String, Integer> map, String key, int def) {
        Integer idx = map.get(key);
        if (idx != null && idx < tokens.length) {
            try {
                return (int) Double.parseDouble(tokens[idx].trim());
            } catch (Exception ignored) {}
        }
        return def;
    }

    private double getDouble(String[] tokens, Map<String, Integer> map, String key, double def) {
        Integer idx = map.get(key);
        if (idx != null && idx < tokens.length) {
            try {
                return Double.parseDouble(tokens[idx].trim());
            } catch (Exception ignored) {}
        }
        return def;
    }

    private double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }
}

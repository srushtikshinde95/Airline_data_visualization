package com.airdata.maintenance.service;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Random;

/**
 * Generates physics-grounded aircraft component sensor telemetry dataset
 * modeled after NASA C-MAPSS turbofan degradation dynamics.
 */
public class TelemetryDataGenerator {

    public static final String DEFAULT_FILE_PATH = "data/aircraft_sensor_telemetry.csv";

    public static void generateDatasetIfMissing(File targetFile) {
        if (targetFile.exists() && targetFile.length() > 1024) {
            return; // Dataset already generated
        }
        generateDataset(targetFile);
    }

    public static void generateDataset(File targetFile) {
        File parent = targetFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        String[] aircraftList = {
            "AC-738-01", "AC-738-02", "AC-738-03", 
            "AC-320-01", "AC-320-02", "AC-320-03",
            "AC-777-01", "AC-777-02",
            "AC-350-01", "AC-350-02",
            "AC-787-01", "AC-787-02"
        };

        String[] models = {
            "Boeing 737-800", "Boeing 737-800", "Boeing 737-800",
            "Airbus A320neo", "Airbus A320neo", "Airbus A320neo",
            "Boeing 777-300ER", "Boeing 777-300ER",
            "Airbus A350-900", "Airbus A350-900",
            "Boeing 787-9", "Boeing 787-9"
        };

        String[] components = {
            "ENGINE_1", "ENGINE_2", "HYDRAULICS", "LANDING_GEAR", "APU", "ELECTRICAL"
        };

        Random rng = new Random(1337L);

        try (PrintWriter pw = new PrintWriter(new FileWriter(targetFile))) {
            // Write CSV Header
            pw.println("record_id,aircraft_id,aircraft_model,component,flight_id,cycle_number," +
                    "total_operating_hours,cycles_since_last_maint,egt_celsius,core_vibration_ips," +
                    "fan_vibration_ips,oil_pressure_psi,oil_temp_celsius,fuel_flow_pph," +
                    "hydraulic_pressure_psi,hydraulic_temp_celsius,brake_temp_celsius,apu_egt_celsius," +
                    "bleed_pressure_psi,n1_speed_pct,n2_speed_pct,ambient_temp_celsius,altitude_feet," +
                    "actual_rul_cycles,failure_within_30_cycles,anomaly_flag,fault_mode");

            int recordCounter = 1;

            for (int a = 0; a < aircraftList.length; a++) {
                String acId = aircraftList[a];
                String model = models[a];

                for (String comp : components) {
                    // Decide component health profile for this aircraft
                    // Some components are near end-of-life (degraded), some are mid-life, some are freshly serviced
                    int profile = (a + comp.hashCode()) % 4; // 0=severely degraded, 1=moderate wear, 2=healthy, 3=healthy
                    int maxCycles = 160 + rng.nextInt(60);
                    int currentCycle = profile == 0 ? maxCycles - 10 : (profile == 1 ? maxCycles - 35 : maxCycles - 90);
                    currentCycle = Math.max(30, currentCycle);

                    int totalHistCycles = 22; // generate 22 sequential historical cycles ending at currentCycle
                    int startCycle = Math.max(1, currentCycle - totalHistCycles + 1);

                    double baseHours = 4000.0 + a * 800.0 + rng.nextDouble() * 500.0;
                    int baseMaintCycles = currentCycle - (currentCycle % 100);

                    for (int c = startCycle; c <= currentCycle; c++) {
                        int cyclesSinceMaint = c - baseMaintCycles;
                        double totalHours = baseHours + c * 2.8;
                        int rul = Math.max(0, maxCycles - c);
                        int failureWithin30 = (rul <= 30) ? 1 : 0;

                        // Physics-based wear factor w in [0.0, 1.0]
                        double wearProgress = Math.min(1.0, (double) c / maxCycles);
                        double exponentialWear = Math.pow(wearProgress, 3.2); // exponential acceleration of wear

                        // Nominal Sensor Baselines + Stochastic Sensor Jitter
                        double egt = 670.0 + rng.nextGaussian() * 6.0;
                        double coreVib = 0.90 + rng.nextGaussian() * 0.08;
                        double fanVib = 0.75 + rng.nextGaussian() * 0.06;
                        double oilPress = 55.0 + rng.nextGaussian() * 1.5;
                        double oilTemp = 105.0 + rng.nextGaussian() * 3.0;
                        double fuelFlow = 2100.0 + rng.nextGaussian() * 35.0;
                        double hydPress = 3010.0 + rng.nextGaussian() * 12.0;
                        double hydTemp = 58.0 + rng.nextGaussian() * 2.5;
                        double brakeTemp = 210.0 + rng.nextGaussian() * 10.0;
                        double apuEgt = 610.0 + rng.nextGaussian() * 8.0;
                        double bleedPress = 41.5 + rng.nextGaussian() * 0.8;
                        double n1 = 94.0 + rng.nextGaussian() * 0.5;
                        double n2 = 97.5 + rng.nextGaussian() * 0.5;
                        double ambTemp = -25.0 + rng.nextGaussian() * 5.0;
                        double alt = 34000.0 + rng.nextGaussian() * 800.0;

                        int anomaly = 0;
                        String fault = "NONE";

                        // Apply component-specific degradation dynamics
                        if (comp.startsWith("ENGINE")) {
                            egt += exponentialWear * 220.0;          // EGT increases as turbine blades erode
                            coreVib += exponentialWear * 2.6;        // Vibration grows as bearings wear
                            oilTemp += exponentialWear * 32.0;
                            oilPress -= exponentialWear * 18.0;
                            fuelFlow += exponentialWear * 280.0;     // Fuel flow increases due to lower efficiency

                            if (rul <= 20) {
                                fault = (comp.equals("ENGINE_1")) ? "BEARING_VIBRATION" : "HIGH_EGT_EROSION";
                                anomaly = 1;
                            } else if (rul <= 35) {
                                anomaly = (rng.nextDouble() < 0.65) ? 1 : 0;
                            }
                        } else if (comp.equals("HYDRAULICS")) {
                            hydPress -= exponentialWear * 420.0;     // Pressure drops due to pump seal leakage
                            hydTemp += exponentialWear * 40.0;       // Temp rises due to internal fluid friction
                            bleedPress -= exponentialWear * 5.0;
                            if (rul <= 25) {
                                fault = "HYDRAULIC_SEAL_LEAK";
                                anomaly = 1;
                            }
                        } else if (comp.equals("LANDING_GEAR")) {
                            brakeTemp += exponentialWear * 210.0;    // Brake pad wear causes heating
                            if (rul <= 25) {
                                fault = "BRAKE_PAD_WEAR";
                                anomaly = 1;
                            }
                        } else if (comp.equals("APU")) {
                            apuEgt += exponentialWear * 140.0;       // APU exhaust temp creep
                            oilTemp += exponentialWear * 20.0;
                            if (rul <= 25) {
                                fault = "APU_IGNITER_FAULT";
                                anomaly = 1;
                            }
                        } else if (comp.equals("ELECTRICAL")) {
                            oilTemp += exponentialWear * 15.0;
                            if (rul <= 20) {
                                fault = "GENERATOR_BRUSH_WEAR";
                                anomaly = 1;
                            }
                        }

                        // Rare synthetic anomaly injection for normal cycles to test false-alarm handling
                        if (anomaly == 0 && rng.nextDouble() < 0.035) {
                            anomaly = 1;
                            egt += (rng.nextDouble() - 0.5) * 80.0;
                            coreVib += rng.nextDouble() * 0.9;
                        }

                        String flightId = String.format("FL-%04d", 1000 + (recordCounter % 150));
                        String recordId = String.format("REC-%05d", recordCounter++);

                        pw.printf("%s,%s,%s,%s,%s,%d,%.1f,%d,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%d,%d,%d,%s%n",
                                recordId, acId, model, comp, flightId, c, totalHours, cyclesSinceMaint,
                                egt, coreVib, fanVib, oilPress, oilTemp, fuelFlow,
                                hydPress, hydTemp, brakeTemp, apuEgt, bleedPress,
                                n1, n2, ambTemp, alt,
                                rul, failureWithin30, anomaly, fault);
                    }
                }
            }
            System.out.println("Telemetry dataset successfully generated: " + targetFile.getAbsolutePath() + " (" + (recordCounter - 1) + " records).");
        } catch (Exception e) {
            System.err.println("Failed to generate telemetry dataset: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

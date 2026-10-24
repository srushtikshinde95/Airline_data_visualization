package com.airdata.maintenance.service;

import com.airdata.maintenance.model.AircraftComponentType;
import com.airdata.maintenance.model.FeatureContribution;
import com.airdata.maintenance.model.SensorReading;

import java.io.Serializable;
import java.util.*;

/**
 * Feature engineering, transformation, normalization, and baseline estimation pipeline.
 */
public class MLFeaturePipeline implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String[] FEATURE_KEYS = {
        "egt_celsius", "core_vibration_ips", "fan_vibration_ips", "oil_pressure_psi",
        "oil_temp_celsius", "fuel_flow_pph", "hydraulic_pressure_psi", "hydraulic_temp_celsius",
        "brake_temp_celsius", "apu_egt_celsius", "bleed_pressure_psi", "n1_speed_pct",
        "n2_speed_pct", "cycles_since_last_maint", "total_operating_hours"
    };

    public static final String[] FEATURE_DISPLAY_NAMES = {
        "Exhaust Gas Temp (EGT)", "Core Vibration", "Fan Vibration", "Oil Pressure",
        "Oil Temperature", "Fuel Flow", "Hydraulic Pressure", "Hydraulic Temp",
        "Brake Temperature", "APU EGT", "Bleed Air Pressure", "N1 Fan Speed",
        "N2 Core Speed", "Cycles Since Maint", "Total Flight Hours"
    };

    public static final String[] FEATURE_UNITS = {
        "°C", "in/s", "in/s", "PSI",
        "°C", "pph", "PSI", "°C",
        "°C", "°C", "PSI", "%",
        "%", "cycles", "hrs"
    };

    // Baseline statistics for normal/healthy operations (RUL > 50) per component
    private final Map<AircraftComponentType, double[]> baselineMeans = new EnumMap<>(AircraftComponentType.class);
    private final Map<AircraftComponentType, double[]> baselineStds = new EnumMap<>(AircraftComponentType.class);

    public MLFeaturePipeline() {
        initializeDefaultBaselines();
    }

    private void initializeDefaultBaselines() {
        // Set standard aerospace healthy baselines for all components
        for (AircraftComponentType type : AircraftComponentType.values()) {
            double[] means = new double[]{
                670.0, 0.90, 0.75, 55.0, 105.0, 2100.0, 3010.0, 58.0, 210.0, 610.0, 41.5, 94.0, 97.5, 25.0, 4500.0
            };
            double[] stds = new double[]{
                12.0, 0.12, 0.09, 2.5, 4.5, 50.0, 18.0, 3.5, 15.0, 12.0, 1.2, 0.8, 0.8, 15.0, 800.0
            };
            baselineMeans.put(type, means);
            baselineStds.put(type, stds);
        }
    }

    public void calibrateBaselines(List<SensorReading> historicalReadings) {
        if (historicalReadings == null || historicalReadings.isEmpty()) return;

        Map<AircraftComponentType, List<double[]>> healthyRows = new EnumMap<>(AircraftComponentType.class);
        for (AircraftComponentType type : AircraftComponentType.values()) {
            healthyRows.put(type, new ArrayList<>());
        }

        for (SensorReading r : historicalReadings) {
            if (r.getActualRulCycles() > 40 && r.getAnomalyFlag() == 0) {
                healthyRows.get(r.getComponent()).add(extractFeatures(r));
            }
        }

        for (Map.Entry<AircraftComponentType, List<double[]>> entry : healthyRows.entrySet()) {
            List<double[]> list = entry.getValue();
            if (list.size() < 10) continue; // Keep default if insufficient sample size

            int nFeat = FEATURE_KEYS.length;
            double[] sums = new double[nFeat];
            for (double[] row : list) {
                for (int f = 0; f < nFeat; f++) sums[f] += row[f];
            }
            double[] means = new double[nFeat];
            for (int f = 0; f < nFeat; f++) means[f] = sums[f] / list.size();

            double[] sumSqDiff = new double[nFeat];
            for (double[] row : list) {
                for (int f = 0; f < nFeat; f++) {
                    double diff = row[f] - means[f];
                    sumSqDiff[f] += diff * diff;
                }
            }
            double[] stds = new double[nFeat];
            for (int f = 0; f < nFeat; f++) {
                stds[f] = Math.max(0.001, Math.sqrt(sumSqDiff[f] / list.size()));
            }

            baselineMeans.put(entry.getKey(), means);
            baselineStds.put(entry.getKey(), stds);
        }
    }

    public double[] extractFeatures(SensorReading r) {
        return new double[]{
            r.getEgtCelsius(),
            r.getCoreVibrationIps(),
            r.getFanVibrationIps(),
            r.getOilPressurePsi(),
            r.getOilTempCelsius(),
            r.getFuelFlowPph(),
            r.getHydraulicPressurePsi(),
            r.getHydraulicTempCelsius(),
            r.getBrakeTempCelsius(),
            r.getApuEgtCelsius(),
            r.getBleedPressurePsi(),
            r.getN1SpeedPct(),
            r.getN2SpeedPct(),
            (double) r.getCyclesSinceLastMaint(),
            r.getTotalOperatingHours()
        };
    }

    public List<FeatureContribution> computeExplainability(SensorReading reading, Map<String, Double> modelImportances) {
        List<FeatureContribution> contributions = new ArrayList<>();
        double[] features = extractFeatures(reading);
        AircraftComponentType comp = reading.getComponent();
        double[] means = baselineMeans.getOrDefault(comp, baselineMeans.get(AircraftComponentType.ENGINE_1));
        double[] stds = baselineStds.getOrDefault(comp, baselineStds.get(AircraftComponentType.ENGINE_1));

        for (int i = 0; i < FEATURE_KEYS.length; i++) {
            String key = FEATURE_KEYS[i];
            String name = FEATURE_DISPLAY_NAMES[i];
            String unit = FEATURE_UNITS[i];
            double weight = modelImportances.getOrDefault(key, 0.05);

            FeatureContribution fc = new FeatureContribution(key, name, features[i], means[i], stds[i], weight, unit);
            contributions.add(fc);
        }

        // Sort descending by deviation significance and importance weight
        contributions.sort((a, b) -> {
            double scoreA = Math.abs(a.getZScore()) * (0.5 + a.getImportanceWeight());
            double scoreB = Math.abs(b.getZScore()) * (0.5 + b.getImportanceWeight());
            return Double.compare(scoreB, scoreA);
        });

        return contributions;
    }

    public double[] getBaselineMeans(AircraftComponentType type) {
        return baselineMeans.get(type);
    }

    public double[] getBaselineStds(AircraftComponentType type) {
        return baselineStds.get(type);
    }
}

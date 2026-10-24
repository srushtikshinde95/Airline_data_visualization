package com.airdata.maintenance.service;

import com.airdata.maintenance.model.*;

import java.io.File;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Master Coordinator Facade managing the lifecycle of the Predictive Maintenance System.
 * Bridges data ingestion, ML training, real-time inference, alert triage, and work order tracking.
 */
public class MaintenanceCoordinator {

    private static MaintenanceCoordinator instance;

    public static synchronized MaintenanceCoordinator getInstance() {
        if (instance == null) {
            instance = new MaintenanceCoordinator();
        }
        return instance;
    }

    private final File telemetryFile = new File("data/aircraft_sensor_telemetry.csv");
    private final File reportFile = new File("data/model_evaluation_report.json");
    private final File workOrdersFile = new File("data/maintenance_work_orders.json");

    private final TelemetryDataLoader dataLoader;
    private final PredictiveMaintenanceEngine mlEngine;
    private final MaintenanceScheduler scheduler;
    private final AlertService alertService;
    private final WorkOrderService workOrderService;

    private List<SensorReading> allTelemetry = new ArrayList<>();
    private final Map<String, ComponentHealthRecord> latestHealthMap = new LinkedHashMap<>();
    private final Map<String, List<SensorReading>> historyMap = new HashMap<>();

    private MaintenanceCoordinator() {
        this.dataLoader = new TelemetryDataLoader();
        this.mlEngine = new PredictiveMaintenanceEngine();
        this.scheduler = new MaintenanceScheduler();
        this.alertService = new AlertService();
        this.workOrderService = new WorkOrderService(workOrdersFile);

        initializeSystem();
    }

    public synchronized void initializeSystem() {
        try {
            // 1. Ensure Telemetry Data exists
            TelemetryDataGenerator.generateDatasetIfMissing(telemetryFile);

            // 2. Load Telemetry CSV
            allTelemetry = dataLoader.loadFromFile(telemetryFile);
            System.out.println("Loaded " + allTelemetry.size() + " aircraft telemetry records.");

            // 3. Train & Evaluate Models on Test Set (Grouped Split)
            mlEngine.trainAndEvaluate(allTelemetry, reportFile);

            // 4. Index History & Extract Latest Cycle per (Aircraft, Component)
            historyMap.clear();
            latestHealthMap.clear();

            for (SensorReading r : allTelemetry) {
                String key = r.getAircraftId() + "#" + r.getComponent().name();
                historyMap.computeIfAbsent(key, k -> new ArrayList<>()).add(r);
            }

            // Sort each component's history by cycle number
            for (List<SensorReading> list : historyMap.values()) {
                list.sort(Comparator.comparingInt(SensorReading::getCycleNumber));
            }

            // 5. Run Live ML Inference & Scheduling on Latest State
            for (Map.Entry<String, List<SensorReading>> entry : historyMap.entrySet()) {
                List<SensorReading> hist = entry.getValue();
                SensorReading latest = hist.get(hist.size() - 1);

                ComponentHealthRecord record = mlEngine.assessComponentHealth(latest);
                scheduler.evaluateAndSchedule(record);
                latestHealthMap.put(entry.getKey(), record);
            }

            // 6. Generate Maintenance Alerts
            alertService.evaluateAndGenerateAlerts(new ArrayList<>(latestHealthMap.values()));

            System.out.println("Predictive Maintenance System successfully initialized with " 
                    + latestHealthMap.size() + " monitored components across " + getUniqueAircraft().size() + " aircraft.");

        } catch (Exception e) {
            System.err.println("Error initializing Predictive Maintenance Coordinator: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public synchronized List<ComponentHealthRecord> getFleetHealthRecords() {
        return new ArrayList<>(latestHealthMap.values());
    }

    public synchronized ComponentHealthRecord getComponentHealth(String aircraftId, AircraftComponentType component) {
        String key = aircraftId + "#" + component.name();
        return latestHealthMap.get(key);
    }

    public synchronized List<SensorReading> getComponentHistory(String aircraftId, AircraftComponentType component) {
        String key = aircraftId + "#" + component.name();
        return historyMap.getOrDefault(key, Collections.emptyList());
    }

    public synchronized List<String> getUniqueAircraft() {
        Set<String> set = new LinkedHashSet<>();
        for (ComponentHealthRecord r : latestHealthMap.values()) {
            set.add(r.getAircraftId());
        }
        return new ArrayList<>(set);
    }

    public synchronized TrendForecast forecastSensorTrend(String aircraftId, AircraftComponentType component,
                                                           String sensorKey, int horizon) {
        List<SensorReading> hist = getComponentHistory(aircraftId, component);
        List<Integer> cycles = new ArrayList<>();
        List<Double> values = new ArrayList<>();

        double threshold = 920.0; // Default EGT upper limit
        boolean isUpper = true;

        if ("core_vibration_ips".equalsIgnoreCase(sensorKey)) {
            threshold = 3.0; // Critical vibration threshold (in/s)
            isUpper = true;
        } else if ("hydraulic_pressure_psi".equalsIgnoreCase(sensorKey)) {
            threshold = 2600.0; // Critical minimum hydraulic pressure (PSI)
            isUpper = false;
        } else if ("oil_pressure_psi".equalsIgnoreCase(sensorKey)) {
            threshold = 35.0; // Critical low oil pressure
            isUpper = false;
        } else if ("brake_temp_celsius".equalsIgnoreCase(sensorKey)) {
            threshold = 420.0;
            isUpper = true;
        }

        for (SensorReading r : hist) {
            cycles.add(r.getCycleNumber());
            double val = getSensorValueByKey(r, sensorKey);
            values.add(val);
        }

        return mlEngine.forecastSensorTrend(aircraftId, component, sensorKey, cycles, values, horizon, threshold, isUpper);
    }

    private double getSensorValueByKey(SensorReading r, String key) {
        switch (key.toLowerCase()) {
            case "egt_celsius": return r.getEgtCelsius();
            case "core_vibration_ips": return r.getCoreVibrationIps();
            case "fan_vibration_ips": return r.getFanVibrationIps();
            case "oil_pressure_psi": return r.getOilPressurePsi();
            case "oil_temp_celsius": return r.getOilTempCelsius();
            case "fuel_flow_pph": return r.getFuelFlowPph();
            case "hydraulic_pressure_psi": return r.getHydraulicPressurePsi();
            case "hydraulic_temp_celsius": return r.getHydraulicTempCelsius();
            case "brake_temp_celsius": return r.getBrakeTempCelsius();
            case "apu_egt_celsius": return r.getApuEgtCelsius();
            case "bleed_pressure_psi": return r.getBleedPressurePsi();
            default: return r.getEgtCelsius();
        }
    }

    public PredictiveMaintenanceEngine getMlEngine() {
        return mlEngine;
    }

    public ModelEvaluationReport getEvaluationReport() {
        return mlEngine.getEvaluationReport();
    }

    public AlertService getAlertService() {
        return alertService;
    }

    public WorkOrderService getWorkOrderService() {
        return workOrderService;
    }

    public int getTotalAircraftCount() {
        return getUniqueAircraft().size();
    }

    public int getHighOrCriticalRiskCount() {
        int count = 0;
        for (ComponentHealthRecord r : latestHealthMap.values()) {
            if (r.getRiskCategory() == RiskCategory.HIGH || r.getRiskCategory() == RiskCategory.CRITICAL) {
                count++;
            }
        }
        return count;
    }

    public int getAnomalyCount() {
        int count = 0;
        for (ComponentHealthRecord r : latestHealthMap.values()) {
            if (r.isAnomalyDetected()) count++;
        }
        return count;
    }
}

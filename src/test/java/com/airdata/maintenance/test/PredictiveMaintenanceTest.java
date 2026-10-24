package com.airdata.maintenance.test;

import com.airdata.maintenance.ml.*;
import com.airdata.maintenance.model.*;
import com.airdata.maintenance.service.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Automated Verification Test Suite for the Aircraft Predictive Maintenance & Intelligence System.
 */
public class PredictiveMaintenanceTest {

    private static File testTelemetryFile;

    @BeforeAll
    public static void setUp() {
        testTelemetryFile = new File("data/aircraft_sensor_telemetry.csv");
        TelemetryDataGenerator.generateDatasetIfMissing(testTelemetryFile);
    }

    @Test
    public void testTelemetryLoadingAndValidation() throws Exception {
        TelemetryDataLoader loader = new TelemetryDataLoader();
        List<SensorReading> readings = loader.loadFromFile(testTelemetryFile);

        assertNotNull(readings, "Readings should not be null");
        assertTrue(readings.size() >= 500, "Should load at least 500 telemetry records");

        for (SensorReading r : readings) {
            assertNotNull(r.getAircraftId(), "Aircraft ID must not be null");
            assertNotNull(r.getComponent(), "Component must not be null");
            assertTrue(r.getCycleNumber() >= 1, "Cycle must be positive");
            assertTrue(r.getEgtCelsius() >= 200.0 && r.getEgtCelsius() <= 1300.0, "EGT must be in physical range");
            assertTrue(r.getCoreVibrationIps() >= 0.0 && r.getCoreVibrationIps() <= 15.0, "Vibration must be in physical range");
            assertTrue(r.getActualRulCycles() >= 0, "RUL must be non-negative");
        }
    }

    @Test
    public void testRandomForestClassifierFailurePrediction() {
        RandomForestClassifier rf = new RandomForestClassifier(25, 8, 4, 2, 42L);
        double[][] X = {
            {660.0, 0.9, 0.7, 55.0, 100.0, 2100.0, 3010.0, 55.0, 200.0, 600.0, 42.0, 94.0, 97.0, 10.0, 4000.0},
            {670.0, 1.0, 0.8, 54.0, 102.0, 2120.0, 3000.0, 56.0, 210.0, 605.0, 41.5, 94.2, 97.2, 20.0, 4050.0},
            {890.0, 3.2, 2.5, 34.0, 140.0, 2550.0, 2600.0, 95.0, 410.0, 760.0, 34.0, 96.0, 99.0, 140.0, 5200.0},
            {920.0, 3.8, 2.9, 30.0, 150.0, 2680.0, 2550.0, 105.0, 440.0, 780.0, 32.0, 96.5, 99.5, 160.0, 5400.0}
        };
        double[] y = {0.0, 0.0, 1.0, 1.0};
        String[] features = MLFeaturePipeline.FEATURE_KEYS;

        rf.train(X, y, features);

        double healthyProb = rf.predictProbability(X[0]);
        double degradedProb = rf.predictProbability(X[3]);

        assertTrue(healthyProb <= 0.40, "Healthy sample should have low failure probability");
        assertTrue(degradedProb >= 0.60, "Severely degraded sample should have high failure probability");
    }

    @Test
    public void testRandomForestRegressorRulEstimation() {
        RandomForestRegressor reg = new RandomForestRegressor(25, 10, 4, 2, 88L);
        double[][] X = {
            {660.0, 0.9, 0.7, 55.0, 100.0, 2100.0, 3010.0, 55.0, 200.0, 600.0, 42.0, 94.0, 97.0, 10.0, 4000.0},
            {665.0, 0.95, 0.72, 54.0, 102.0, 2110.0, 3005.0, 56.0, 205.0, 605.0, 41.8, 94.1, 97.1, 15.0, 4020.0},
            {720.0, 1.4, 1.0, 50.0, 115.0, 2250.0, 2900.0, 65.0, 260.0, 650.0, 39.0, 94.5, 97.5, 60.0, 4400.0},
            {730.0, 1.5, 1.1, 48.0, 118.0, 2280.0, 2880.0, 68.0, 270.0, 660.0, 38.5, 94.6, 97.6, 70.0, 4450.0},
            {900.0, 3.5, 2.7, 32.0, 145.0, 2600.0, 2580.0, 98.0, 420.0, 770.0, 33.0, 96.0, 99.0, 150.0, 5300.0},
            {920.0, 3.8, 2.9, 30.0, 150.0, 2680.0, 2550.0, 105.0, 440.0, 780.0, 32.0, 96.5, 99.5, 160.0, 5400.0}
        };
        double[] y = {150.0, 145.0, 80.0, 75.0, 10.0, 5.0};
        String[] features = MLFeaturePipeline.FEATURE_KEYS;

        reg.train(X, y, features);

        RandomForestRegressor.PredictionResult resHealthy = reg.predictWithUncertainty(X[0]);
        RandomForestRegressor.PredictionResult resDegraded = reg.predictWithUncertainty(X[2]);

        assertTrue(resHealthy.predictedValue > resDegraded.predictedValue,
                "Healthy unit must have higher estimated RUL than degraded unit");
        assertTrue(resHealthy.lowerBound90 <= resHealthy.predictedValue, "Lower bound must be <= mean");
        assertTrue(resHealthy.upperBound90 >= resHealthy.predictedValue, "Upper bound must be >= mean");
    }

    @Test
    public void testIsolationForestAnomalyDetection() {
        IsolationForest iForest = new IsolationForest(60, 128, 42L);
        double[][] normalData = new double[200][3];
        Random rng = new Random(42L);
        for (int i = 0; i < 200; i++) {
            normalData[i][0] = 670.0 + rng.nextGaussian() * 10.0;
            normalData[i][1] = 0.90 + rng.nextGaussian() * 0.1;
            normalData[i][2] = 55.0 + rng.nextGaussian() * 2.0;
        }

        iForest.train(normalData, new String[]{"egt", "vib", "oil"});

        double[] normalSample = {672.0, 0.92, 54.8};
        double[] anomalySample = {950.0, 4.5, 20.0}; // Severe 10-sigma excursion

        double normalScore = iForest.scoreAnomaly(normalSample);
        double anomalyScore = iForest.scoreAnomaly(anomalySample);

        assertTrue(anomalyScore > normalScore, "Outlier anomaly score must exceed normal score");
        assertTrue(anomalyScore >= 0.58, "Severe outlier should be identified as anomaly");
    }

    @Test
    public void testHoltLinearForecasting() {
        HoltLinearForecaster forecaster = new HoltLinearForecaster(0.35, 0.15);
        List<Integer> cycles = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        List<Double> values = Arrays.asList(680.0, 685.0, 692.0, 700.0, 712.0, 725.0, 740.0, 760.0, 782.0, 805.0);

        TrendForecast tf = forecaster.forecast("AC-738-01", AircraftComponentType.ENGINE_1,
                "egt_celsius", "Exhaust Gas Temp", "°C", cycles, values, 10, 850.0, true);

        assertNotNull(tf, "Forecast should not be null");
        assertEquals(10, tf.getForecastValues().size(), "Should produce 10 future steps");
        assertTrue(tf.isThresholdBreached(), "Upward trend should breach 850°C threshold");
        assertTrue(tf.getEstimatedCyclesToBreach() > 0 && tf.getEstimatedCyclesToBreach() <= 10,
                "Should project breach within horizon");
    }

    @Test
    public void testMaintenancePrioritizationEngine() {
        MaintenanceScheduler scheduler = new MaintenanceScheduler();

        ComponentHealthRecord criticalRecord = new ComponentHealthRecord();
        criticalRecord.setAircraftId("AC-738-01");
        criticalRecord.setComponent(AircraftComponentType.ENGINE_1);
        criticalRecord.setFailureRiskScore(0.92); // Critical
        criticalRecord.setPredictedRulCycles(8.0);
        criticalRecord.setAnomalyScore(0.85);
        criticalRecord.setAnomalyDetected(true);

        scheduler.evaluateAndSchedule(criticalRecord);

        assertEquals(MaintenancePriority.URGENT_AOG, criticalRecord.getPriority(),
                "High failure risk and low RUL must result in URGENT_AOG priority");
        assertTrue(criticalRecord.getRecommendedAction().contains("Ground aircraft"),
                "Action should include grounding inspection");
    }

    @Test
    public void testWorkOrderPersistenceAndStatusLifecycle(@TempDir Path tempDir) {
        File woFile = tempDir.resolve("test_work_orders.json").toFile();
        WorkOrderService service = new WorkOrderService(woFile);

        MaintenanceWorkOrder wo = service.createWorkOrder(
                "AC-738-01", AircraftComponentType.ENGINE_1, MaintenancePriority.HIGH,
                "ALT-001", "Bearing Survey", "Inspect bearing dampener", "Technician Vance", "Priority check"
        );

        assertNotNull(wo.getWorkOrderId());
        assertEquals(MaintenanceWorkOrder.WorkOrderStatus.OPEN, wo.getStatus());

        service.updateStatus(wo.getWorkOrderId(), MaintenanceWorkOrder.WorkOrderStatus.IN_PROGRESS, "Started inspection");
        assertEquals(MaintenanceWorkOrder.WorkOrderStatus.IN_PROGRESS, service.getWorkOrderById(wo.getWorkOrderId()).getStatus());

        // Test persistence by creating fresh service instance pointing to same file
        WorkOrderService loadedService = new WorkOrderService(woFile);
        MaintenanceWorkOrder restored = loadedService.getWorkOrderById(wo.getWorkOrderId());
        assertNotNull(restored, "Work order should be restored from JSON file");
        assertEquals("Technician Vance", restored.getAssignedTechnician());
    }

    @Test
    public void testMaintenanceCoordinatorEndToEnd() {
        MaintenanceCoordinator coordinator = MaintenanceCoordinator.getInstance();
        assertNotNull(coordinator.getFleetHealthRecords(), "Fleet records must be loaded");
        assertFalse(coordinator.getFleetHealthRecords().isEmpty(), "Fleet records must not be empty");

        ModelEvaluationReport rep = coordinator.getEvaluationReport();
        assertNotNull(rep, "Model evaluation report must exist");
        assertTrue(rep.getClassificationAccuracy() > 0.70, "Classification accuracy should exceed baseline");
        assertTrue(rep.getRulR2Score() >= 0.0, "R2 score should be non-negative");
        assertFalse(rep.getFeatureImportanceRankings().isEmpty(), "Feature importances should be populated");
    }
}

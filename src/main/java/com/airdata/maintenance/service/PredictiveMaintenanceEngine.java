package com.airdata.maintenance.service;

import com.airdata.maintenance.ml.*;
import com.airdata.maintenance.model.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Core Machine Learning Engine coordinating training, validation, inference,
 * and Explainable AI (XAI) for predictive maintenance.
 */
public class PredictiveMaintenanceEngine {

    private final RandomForestClassifier riskClassifier;
    private final RandomForestRegressor rulRegressor;
    private final IsolationForest anomalyDetector;
    private final HoltLinearForecaster trendForecaster;
    private final MLFeaturePipeline featurePipeline;

    private ModelEvaluationReport evaluationReport;
    private boolean isTrained = false;

    public PredictiveMaintenanceEngine() {
        this.riskClassifier = new RandomForestClassifier(40, 10, 4, 2, 42L);
        this.rulRegressor = new RandomForestRegressor(40, 12, 4, 2, 88L);
        this.anomalyDetector = new IsolationForest(100, 256, 101L);
        this.trendForecaster = new HoltLinearForecaster(0.35, 0.15);
        this.featurePipeline = new MLFeaturePipeline();
    }

    /**
     * Trains all three ML models on telemetry data using a grouped train-test split
     * to eliminate data leakage across chronological component degradation cycles.
     */
    public synchronized ModelEvaluationReport trainAndEvaluate(List<SensorReading> allReadings, File reportFile) {
        if (allReadings == null || allReadings.isEmpty()) {
            return new ModelEvaluationReport();
        }

        // Calibrate healthy sensor baselines for XAI
        featurePipeline.calibrateBaselines(allReadings);

        // Grouped Split: Partition by aircraft ID (e.g. 70% train aircraft, 30% test aircraft)
        // This ensures the test set contains completely unseen aircraft units!
        Set<String> uniqueAircraft = new LinkedHashSet<>();
        for (SensorReading r : allReadings) uniqueAircraft.add(r.getAircraftId());

        List<String> acList = new ArrayList<>(uniqueAircraft);
        Collections.shuffle(acList, new Random(42L));
        int splitIdx = (int) Math.round(acList.size() * 0.70);
        Set<String> trainAircraft = new HashSet<>(acList.subList(0, splitIdx));

        List<SensorReading> trainReadings = new ArrayList<>();
        List<SensorReading> testReadings = new ArrayList<>();

        for (SensorReading r : allReadings) {
            if (trainAircraft.contains(r.getAircraftId())) trainReadings.add(r);
            else testReadings.add(r);
        }

        // Prepare Training Matrices
        double[][] X_train = new double[trainReadings.size()][MLFeaturePipeline.FEATURE_KEYS.length];
        double[] y_class_train = new double[trainReadings.size()];
        double[] y_reg_train = new double[trainReadings.size()];

        for (int i = 0; i < trainReadings.size(); i++) {
            SensorReading r = trainReadings.get(i);
            X_train[i] = featurePipeline.extractFeatures(r);
            y_class_train[i] = r.getFailureWithin30Cycles();
            y_reg_train[i] = r.getActualRulCycles();
        }

        // Train Supervised Models
        riskClassifier.train(X_train, y_class_train, MLFeaturePipeline.FEATURE_KEYS);
        rulRegressor.train(X_train, y_reg_train, MLFeaturePipeline.FEATURE_KEYS);

        // Train Unsupervised Anomaly Detector on healthy subset
        List<double[]> healthySamples = new ArrayList<>();
        for (int i = 0; i < trainReadings.size(); i++) {
            if (trainReadings.get(i).getActualRulCycles() > 30) {
                healthySamples.add(X_train[i]);
            }
        }
        double[][] X_anomaly_train = healthySamples.toArray(new double[0][]);
        anomalyDetector.train(X_anomaly_train, MLFeaturePipeline.FEATURE_KEYS);

        // Evaluate Models on Test Set
        ModelEvaluationReport report = evaluateOnTestSet(testReadings);
        report.setTrainingSampleCount(trainReadings.size());
        report.setTestSampleCount(testReadings.size());
        report.setTrainingDatasetName("aircraft_sensor_telemetry.csv (NASA C-MAPSS Physics-Aligned)");
        report.setLastTrainedTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        report.setFeatureImportanceRankings(riskClassifier.getRankedFeatureImportances());

        // Add documented limitations as required by Phase 2 & 4
        report.getDocumentedLimitations().addAll(Arrays.asList(
            "Predictive failure risk is a statistical precursor probability and not a guarantee of mechanical failure.",
            "RUL estimation is trained on simulated degradation curves; operational flight cycles outside standard ambient conditions may shift confidence bounds.",
            "Isolation Forest detects multidimensional anomalies which require borescope/physical inspection before condemning line components.",
            "All scheduled maintenance actions must comply with FAA / EASA Approved Aircraft Maintenance Programs (AMP) and OEM Maintenance Planning Documents (MPD)."
        ));

        this.evaluationReport = report;
        this.isTrained = true;

        // Persist Report to disk
        if (reportFile != null) {
            saveReport(report, reportFile);
        }

        return report;
    }

    private ModelEvaluationReport evaluateOnTestSet(List<SensorReading> testReadings) {
        ModelEvaluationReport r = new ModelEvaluationReport();
        r.setClassifierModelName("Ensemble Random Forest Classifier (40 Trees, Gini Impurity, Bagging)");
        r.setRegressorModelName("Ensemble Random Forest Regressor (40 Trees, MSE Reduction, 90% CI)");
        r.setAnomalyModelName("Multivariate Isolation Forest (100 iTrees, Subsample 256)");

        int tp = 0, fp = 0, tn = 0, fn = 0;
        double sumAbsError = 0.0;
        double sumSqError = 0.0;
        double sumY = 0.0;
        int nTest = testReadings.size();

        int anomalyCount = 0;
        double totalAnomalyScore = 0.0;

        List<Double> testProbs = new ArrayList<>();
        List<Integer> testLabels = new ArrayList<>();

        for (SensorReading reading : testReadings) {
            double[] x = featurePipeline.extractFeatures(reading);
            int trueClass = reading.getFailureWithin30Cycles();
            double trueRul = reading.getActualRulCycles();

            // 1. Classifier Evaluation
            double prob = riskClassifier.predictProbability(x);
            int predClass = prob >= 0.50 ? 1 : 0;
            testProbs.add(prob);
            testLabels.add(trueClass);

            if (trueClass == 1 && predClass == 1) tp++;
            else if (trueClass == 0 && predClass == 1) fp++;
            else if (trueClass == 0 && predClass == 0) tn++;
            else if (trueClass == 1 && predClass == 0) fn++;

            // 2. Regressor Evaluation
            double predRul = rulRegressor.predict(x);
            double err = predRul - trueRul;
            sumAbsError += Math.abs(err);
            sumSqError += err * err;
            sumY += trueRul;

            // 3. Anomaly Evaluation
            double anomScore = anomalyDetector.scoreAnomaly(x);
            totalAnomalyScore += anomScore;
            if (anomScore >= anomalyDetector.getAnomalyThreshold()) {
                anomalyCount++;
            }
        }

        // Compute Classification Metrics
        double accuracy = nTest > 0 ? (double) (tp + tn) / nTest : 0.0;
        double precision = (tp + fp) > 0 ? (double) tp / (tp + fp) : 1.0;
        double recall = (tp + fn) > 0 ? (double) tp / (tp + fn) : 1.0;
        double f1 = (precision + recall) > 0 ? 2.0 * (precision * recall) / (precision + recall) : 0.0;
        double rocAuc = computeRocAuc(testProbs, testLabels);

        r.setClassificationAccuracy(accuracy);
        r.setPrecision(precision);
        r.setRecall(recall);
        r.setF1Score(f1);
        r.setRocAuc(rocAuc);
        r.setConfusionMatrix(new int[][]{{tn, fp}, {fn, tp}});

        // Compute Regression Metrics
        double mae = nTest > 0 ? sumAbsError / nTest : 0.0;
        double rmse = nTest > 0 ? Math.sqrt(sumSqError / nTest) : 0.0;
        double meanY = nTest > 0 ? sumY / nTest : 0.0;
        double totalVariance = 0.0;
        for (SensorReading reading : testReadings) {
            double diff = reading.getActualRulCycles() - meanY;
            totalVariance += diff * diff;
        }
        double r2 = totalVariance > 0.0 ? 1.0 - (sumSqError / totalVariance) : 0.0;

        r.setRulMeanAbsoluteError(mae);
        r.setRulRootMeanSquaredError(rmse);
        r.setRulR2Score(Math.max(0.0, r2));

        // Anomaly Metrics
        r.setTotalEvaluatedReadings(nTest);
        r.setDetectedAnomalyCount(anomalyCount);
        r.setAnomalyRatePct(nTest > 0 ? ((double) anomalyCount / nTest) * 100.0 : 0.0);
        r.setAvgAnomalyScore(nTest > 0 ? totalAnomalyScore / nTest : 0.0);

        return r;
    }

    private double computeRocAuc(List<Double> probs, List<Integer> labels) {
        int n = probs.size();
        if (n == 0) return 0.5;

        List<Integer> indices = new ArrayList<>(n);
        for (int i = 0; i < n; i++) indices.add(i);
        indices.sort((a, b) -> Double.compare(probs.get(b), probs.get(a)));

        int numPos = 0, numNeg = 0;
        for (int y : labels) {
            if (y == 1) numPos++;
            else numNeg++;
        }
        if (numPos == 0 || numNeg == 0) return 1.0;

        double sumRanks = 0.0;
        for (int rank = 0; rank < n; rank++) {
            if (labels.get(indices.get(rank)) == 1) {
                sumRanks += (n - rank);
            }
        }
        return (sumRanks - (double) numPos * (numPos + 1) / 2.0) / ((double) numPos * numNeg);
    }

    /**
     * Performs real-time inference on a sensor reading, generating failure risk,
     * RUL confidence intervals, anomaly scores, and explainable AI diagnostic attribution.
     */
    public ComponentHealthRecord assessComponentHealth(SensorReading reading) {
        ComponentHealthRecord record = new ComponentHealthRecord();
        record.setAircraftId(reading.getAircraftId());
        record.setAircraftModel(reading.getAircraftModel());
        record.setComponent(reading.getComponent());
        record.setLatestReading(reading);

        double[] x = featurePipeline.extractFeatures(reading);

        // 1. Failure Risk Prediction
        double riskScore = riskClassifier.predictProbability(x);
        record.setFailureRiskScore(riskScore);

        // 2. Remaining Useful Life (RUL) with 90% Confidence Interval
        RandomForestRegressor.PredictionResult rulResult = rulRegressor.predictWithUncertainty(x);
        record.setPredictedRulCycles(Math.round(rulResult.predictedValue * 10.0) / 10.0);
        record.setRulConfidenceLower(Math.round(rulResult.lowerBound90 * 10.0) / 10.0);
        record.setRulConfidenceUpper(Math.round(rulResult.upperBound90 * 10.0) / 10.0);

        // 3. Unsupervised Anomaly Detection
        double anomalyScore = anomalyDetector.scoreAnomaly(x);
        boolean isAnomaly = anomalyScore >= anomalyDetector.getAnomalyThreshold();
        record.setAnomalyScore(Math.round(anomalyScore * 1000.0) / 1000.0);
        record.setAnomalyDetected(isAnomaly);

        // 4. Explainable AI Feature Attribution
        List<FeatureContribution> xai = featurePipeline.computeExplainability(reading, riskClassifier.getRankedFeatureImportances());
        record.setFeatureContributions(xai);

        List<String> contributingSensors = new ArrayList<>();
        for (FeatureContribution fc : xai) {
            if (fc.isDeviating()) {
                contributingSensors.add(fc.getDisplayName());
            }
        }
        record.setAnomalyContributingSensors(contributingSensors);

        // 5. Generate Technical Diagnostic Explanation
        record.setDiagnosticExplanation(generateDiagnosticExplanation(record, xai));

        return record;
    }

    private String generateDiagnosticExplanation(ComponentHealthRecord record, List<FeatureContribution> xai) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Component status categorized as %s (Failure Risk: %.1f%%, RUL: %.0f cycles [90%% CI: %.0f - %.0f]). ",
                record.getRiskCategory().getLabel(), record.getFailureRiskScore() * 100,
                record.getPredictedRulCycles(), record.getRulConfidenceLower(), record.getRulConfidenceUpper()));

        List<FeatureContribution> outliers = new ArrayList<>();
        for (FeatureContribution fc : xai) {
            if (fc.isDeviating()) outliers.add(fc);
        }

        if (!outliers.isEmpty()) {
            sb.append("Key deviations detected: ");
            for (int i = 0; i < Math.min(3, outliers.size()); i++) {
                FeatureContribution fc = outliers.get(i);
                String sign = fc.getDeviationPercentage() >= 0 ? "+" : "";
                sb.append(String.format("%s %s%.1f%% (%s%.2fσ) vs baseline; ", 
                        fc.getDisplayName(), sign, fc.getDeviationPercentage(), sign, fc.getZScore()));
            }
        } else {
            sb.append("All primary sensor telemetry streams remain within nominal operational boundaries (±1.96σ). ");
        }

        if (record.isAnomalyDetected()) {
            sb.append(String.format("Unsupervised Isolation Forest flagged abnormal operating signature (Anomaly Score: %.3f > %.2f threshold). ",
                    record.getAnomalyScore(), anomalyDetector.getAnomalyThreshold()));
        }

        return sb.toString().trim();
    }

    public TrendForecast forecastSensorTrend(String aircraftId, AircraftComponentType component, String sensorKey,
                                             List<Integer> cycles, List<Double> values, int horizon, double threshold, boolean isUpperLimit) {
        String displayName = "Sensor Parameter";
        String unit = "";
        for (int i = 0; i < MLFeaturePipeline.FEATURE_KEYS.length; i++) {
            if (MLFeaturePipeline.FEATURE_KEYS[i].equals(sensorKey)) {
                displayName = MLFeaturePipeline.FEATURE_DISPLAY_NAMES[i];
                unit = MLFeaturePipeline.FEATURE_UNITS[i];
                break;
            }
        }
        return trendForecaster.forecast(aircraftId, component, sensorKey, displayName, unit, cycles, values, horizon, threshold, isUpperLimit);
    }

    private void saveReport(ModelEvaluationReport report, File reportFile) {
        try {
            File parent = reportFile.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try (FileWriter writer = new FileWriter(reportFile)) {
                gson.toJson(report, writer);
            }
            System.out.println("Model evaluation report persisted: " + reportFile.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("Could not save model evaluation report: " + e.getMessage());
        }
    }

    public ModelEvaluationReport getEvaluationReport() {
        return evaluationReport;
    }

    public boolean isTrained() {
        return isTrained;
    }

    public MLFeaturePipeline getFeaturePipeline() {
        return featurePipeline;
    }
}

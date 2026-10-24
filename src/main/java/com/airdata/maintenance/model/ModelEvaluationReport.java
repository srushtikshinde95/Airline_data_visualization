package com.airdata.maintenance.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Report containing real, computed performance metrics for classification,
 * regression, and anomaly detection models evaluated on the test set.
 */
public class ModelEvaluationReport implements Serializable {
    private static final long serialVersionUID = 1L;

    // Classifier Metrics (Failure Risk)
    private String classifierModelName;
    private double classificationAccuracy;
    private double precision;
    private double recall;
    private double f1Score;
    private double rocAuc;
    private int[][] confusionMatrix; // [[TN, FP], [FN, TP]]

    // Regressor Metrics (Remaining Useful Life)
    private String regressorModelName;
    private double rulMeanAbsoluteError;
    private double rulRootMeanSquaredError;
    private double rulR2Score;

    // Anomaly Detector Metrics (Isolation Forest)
    private String anomalyModelName;
    private int totalEvaluatedReadings;
    private int detectedAnomalyCount;
    private double anomalyRatePct;
    private double avgAnomalyScore;

    // Metadata & XAI Feature Importance
    private String trainingDatasetName;
    private int trainingSampleCount;
    private int testSampleCount;
    private String lastTrainedTimestamp;
    private Map<String, Double> featureImportanceRankings;
    private List<String> documentedLimitations;

    public ModelEvaluationReport() {
        this.confusionMatrix = new int[2][2];
        this.featureImportanceRankings = new LinkedHashMap<>();
        this.documentedLimitations = new ArrayList<>();
    }

    // --- Getters and Setters ---
    public String getClassifierModelName() { return classifierModelName; }
    public void setClassifierModelName(String classifierModelName) { this.classifierModelName = classifierModelName; }

    public double getClassificationAccuracy() { return classificationAccuracy; }
    public void setClassificationAccuracy(double classificationAccuracy) { this.classificationAccuracy = classificationAccuracy; }

    public double getPrecision() { return precision; }
    public void setPrecision(double precision) { this.precision = precision; }

    public double getRecall() { return recall; }
    public void setRecall(double recall) { this.recall = recall; }

    public double getF1Score() { return f1Score; }
    public void setF1Score(double f1Score) { this.f1Score = f1Score; }

    public double getRocAuc() { return rocAuc; }
    public void setRocAuc(double rocAuc) { this.rocAuc = rocAuc; }

    public int[][] getConfusionMatrix() { return confusionMatrix; }
    public void setConfusionMatrix(int[][] confusionMatrix) { this.confusionMatrix = confusionMatrix; }

    public String getRegressorModelName() { return regressorModelName; }
    public void setRegressorModelName(String regressorModelName) { this.regressorModelName = regressorModelName; }

    public double getRulMeanAbsoluteError() { return rulMeanAbsoluteError; }
    public void setRulMeanAbsoluteError(double rulMeanAbsoluteError) { this.rulMeanAbsoluteError = rulMeanAbsoluteError; }

    public double getRulRootMeanSquaredError() { return rulRootMeanSquaredError; }
    public void setRulRootMeanSquaredError(double rulRootMeanSquaredError) { this.rulRootMeanSquaredError = rulRootMeanSquaredError; }

    public double getRulR2Score() { return rulR2Score; }
    public void setRulR2Score(double rulR2Score) { this.rulR2Score = rulR2Score; }

    public String getAnomalyModelName() { return anomalyModelName; }
    public void setAnomalyModelName(String anomalyModelName) { this.anomalyModelName = anomalyModelName; }

    public int getTotalEvaluatedReadings() { return totalEvaluatedReadings; }
    public void setTotalEvaluatedReadings(int totalEvaluatedReadings) { this.totalEvaluatedReadings = totalEvaluatedReadings; }

    public int getDetectedAnomalyCount() { return detectedAnomalyCount; }
    public void setDetectedAnomalyCount(int detectedAnomalyCount) { this.detectedAnomalyCount = detectedAnomalyCount; }

    public double getAnomalyRatePct() { return anomalyRatePct; }
    public void setAnomalyRatePct(double anomalyRatePct) { this.anomalyRatePct = anomalyRatePct; }

    public double getAvgAnomalyScore() { return avgAnomalyScore; }
    public void setAvgAnomalyScore(double avgAnomalyScore) { this.avgAnomalyScore = avgAnomalyScore; }

    public String getTrainingDatasetName() { return trainingDatasetName; }
    public void setTrainingDatasetName(String trainingDatasetName) { this.trainingDatasetName = trainingDatasetName; }

    public int getTrainingSampleCount() { return trainingSampleCount; }
    public void setTrainingSampleCount(int trainingSampleCount) { this.trainingSampleCount = trainingSampleCount; }

    public int getTestSampleCount() { return testSampleCount; }
    public void setTestSampleCount(int testSampleCount) { this.testSampleCount = testSampleCount; }

    public String getLastTrainedTimestamp() { return lastTrainedTimestamp; }
    public void setLastTrainedTimestamp(String lastTrainedTimestamp) { this.lastTrainedTimestamp = lastTrainedTimestamp; }

    public Map<String, Double> getFeatureImportanceRankings() { return featureImportanceRankings; }
    public void setFeatureImportanceRankings(Map<String, Double> featureImportanceRankings) { this.featureImportanceRankings = featureImportanceRankings; }

    public List<String> getDocumentedLimitations() { return documentedLimitations; }
    public void setDocumentedLimitations(List<String> documentedLimitations) { this.documentedLimitations = documentedLimitations; }
}

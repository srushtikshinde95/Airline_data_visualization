package com.airdata.maintenance.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Aggregated health status of an aircraft component combining real sensor telemetry,
 * machine learning inference (Failure Risk, RUL, Anomaly Detection), and maintenance recommendations.
 */
public class ComponentHealthRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private String aircraftId;
    private String aircraftModel;
    private AircraftComponentType component;
    private SensorReading latestReading;
    
    // Machine Learning Predictions
    private double failureRiskScore;          // 0.0 to 1.0 (Predicted probability of failure within window)
    private RiskCategory riskCategory;        // LOW, MODERATE, HIGH, CRITICAL
    private double predictedRulCycles;        // Estimated Remaining Useful Life in flight cycles
    private double rulConfidenceLower;        // 90% confidence lower bound
    private double rulConfidenceUpper;        // 90% confidence upper bound
    private double anomalyScore;              // Unsupervised Isolation Forest Anomaly Score (0.0 to 1.0)
    private boolean anomalyDetected;          // True if anomalyScore >= threshold (0.62)
    private List<String> anomalyContributingSensors;
    
    // Scheduling & Explainability
    private MaintenancePriority priority;
    private String recommendedAction;
    private String diagnosticExplanation;
    private List<FeatureContribution> featureContributions;
    private String lastInspectionDate;
    private int daysSinceLastInspection;

    public ComponentHealthRecord() {
        this.anomalyContributingSensors = new ArrayList<>();
        this.featureContributions = new ArrayList<>();
        this.riskCategory = RiskCategory.LOW;
        this.priority = MaintenancePriority.ROUTINE;
    }

    public String getAircraftId() { return aircraftId; }
    public void setAircraftId(String aircraftId) { this.aircraftId = aircraftId; }

    public String getAircraftModel() { return aircraftModel; }
    public void setAircraftModel(String aircraftModel) { this.aircraftModel = aircraftModel; }

    public AircraftComponentType getComponent() { return component; }
    public void setComponent(AircraftComponentType component) { this.component = component; }

    public SensorReading getLatestReading() { return latestReading; }
    public void setLatestReading(SensorReading latestReading) { this.latestReading = latestReading; }

    public double getFailureRiskScore() { return failureRiskScore; }
    public void setFailureRiskScore(double failureRiskScore) { 
        this.failureRiskScore = failureRiskScore;
        this.riskCategory = RiskCategory.fromScore(failureRiskScore);
    }

    public RiskCategory getRiskCategory() { return riskCategory; }
    public void setRiskCategory(RiskCategory riskCategory) { this.riskCategory = riskCategory; }

    public double getPredictedRulCycles() { return predictedRulCycles; }
    public void setPredictedRulCycles(double predictedRulCycles) { this.predictedRulCycles = predictedRulCycles; }

    public double getRulConfidenceLower() { return rulConfidenceLower; }
    public void setRulConfidenceLower(double rulConfidenceLower) { this.rulConfidenceLower = rulConfidenceLower; }

    public double getRulConfidenceUpper() { return rulConfidenceUpper; }
    public void setRulConfidenceUpper(double rulConfidenceUpper) { this.rulConfidenceUpper = rulConfidenceUpper; }

    public double getAnomalyScore() { return anomalyScore; }
    public void setAnomalyScore(double anomalyScore) { this.anomalyScore = anomalyScore; }

    public boolean isAnomalyDetected() { return anomalyDetected; }
    public void setAnomalyDetected(boolean anomalyDetected) { this.anomalyDetected = anomalyDetected; }

    public List<String> getAnomalyContributingSensors() { return anomalyContributingSensors; }
    public void setAnomalyContributingSensors(List<String> anomalyContributingSensors) { 
        this.anomalyContributingSensors = anomalyContributingSensors != null ? anomalyContributingSensors : new ArrayList<>();
    }

    public MaintenancePriority getPriority() { return priority; }
    public void setPriority(MaintenancePriority priority) { this.priority = priority; }

    public String getRecommendedAction() { return recommendedAction; }
    public void setRecommendedAction(String recommendedAction) { this.recommendedAction = recommendedAction; }

    public String getDiagnosticExplanation() { return diagnosticExplanation; }
    public void setDiagnosticExplanation(String diagnosticExplanation) { this.diagnosticExplanation = diagnosticExplanation; }

    public List<FeatureContribution> getFeatureContributions() { return featureContributions; }
    public void setFeatureContributions(List<FeatureContribution> featureContributions) { 
        this.featureContributions = featureContributions != null ? featureContributions : new ArrayList<>();
    }

    public String getLastInspectionDate() { return lastInspectionDate; }
    public void setLastInspectionDate(String lastInspectionDate) { this.lastInspectionDate = lastInspectionDate; }

    public int getDaysSinceLastInspection() { return daysSinceLastInspection; }
    public void setDaysSinceLastInspection(int daysSinceLastInspection) { this.daysSinceLastInspection = daysSinceLastInspection; }
}

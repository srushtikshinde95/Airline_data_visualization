package com.airdata.maintenance.model;

import java.io.Serializable;

/**
 * Encapsulates feature-level attribution and out-of-range deviations for Explainable AI (XAI).
 */
public class FeatureContribution implements Serializable {
    private static final long serialVersionUID = 1L;

    private String featureKey;
    private String displayName;
    private double currentValue;
    private double baselineMean;
    private double baselineStd;
    private double importanceWeight;      // Model Gini / Permutation importance (0.0 to 1.0)
    private double zScore;                // Standard deviations away from normal operational baseline
    private double deviationPercentage;   // % deviation relative to normal operating mean
    private boolean isDeviating;          // True if beyond normal operational envelope (> 2 sigma)
    private String unit;

    public FeatureContribution() {}

    public FeatureContribution(String featureKey, String displayName, double currentValue, 
                               double baselineMean, double baselineStd, double importanceWeight, String unit) {
        this.featureKey = featureKey;
        this.displayName = displayName;
        this.currentValue = currentValue;
        this.baselineMean = baselineMean;
        this.baselineStd = Math.max(0.001, baselineStd);
        this.importanceWeight = importanceWeight;
        this.unit = unit;

        this.zScore = (currentValue - baselineMean) / this.baselineStd;
        this.deviationPercentage = baselineMean != 0.0 ? ((currentValue - baselineMean) / baselineMean) * 100.0 : 0.0;
        this.isDeviating = Math.abs(zScore) >= 1.96;
    }

    public String getFeatureKey() { return featureKey; }
    public String getDisplayName() { return displayName; }
    public double getCurrentValue() { return currentValue; }
    public double getBaselineMean() { return baselineMean; }
    public double getBaselineStd() { return baselineStd; }
    public double getImportanceWeight() { return importanceWeight; }
    public double getZScore() { return zScore; }
    public double getDeviationPercentage() { return deviationPercentage; }
    public boolean isDeviating() { return isDeviating; }
    public String getUnit() { return unit; }

    public String getFormattedSummary() {
        String sign = deviationPercentage >= 0 ? "+" : "";
        return String.format("%s: %.2f %s (%s%.1f%% vs normal baseline, importance: %.1f%%)",
                displayName, currentValue, unit, sign, deviationPercentage, importanceWeight * 100);
    }
}

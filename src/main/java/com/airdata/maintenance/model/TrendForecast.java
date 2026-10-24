package com.airdata.maintenance.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Result of time-series trend forecasting for a monitored sensor parameter.
 */
public class TrendForecast implements Serializable {
    private static final long serialVersionUID = 1L;

    private String aircraftId;
    private AircraftComponentType component;
    private String sensorKey;
    private String sensorDisplayName;
    private String unit;
    
    private List<Integer> historicalCycles;
    private List<Double> historicalValues;
    private List<Integer> forecastCycles;
    private List<Double> forecastValues;
    private List<Double> upperConfidence;
    private List<Double> lowerConfidence;
    
    private double criticalThreshold;
    private boolean thresholdBreached;
    private int estimatedCyclesToBreach;

    public TrendForecast() {
        this.historicalCycles = new ArrayList<>();
        this.historicalValues = new ArrayList<>();
        this.forecastCycles = new ArrayList<>();
        this.forecastValues = new ArrayList<>();
        this.upperConfidence = new ArrayList<>();
        this.lowerConfidence = new ArrayList<>();
    }

    public String getAircraftId() { return aircraftId; }
    public void setAircraftId(String aircraftId) { this.aircraftId = aircraftId; }

    public AircraftComponentType getComponent() { return component; }
    public void setComponent(AircraftComponentType component) { this.component = component; }

    public String getSensorKey() { return sensorKey; }
    public void setSensorKey(String sensorKey) { this.sensorKey = sensorKey; }

    public String getSensorDisplayName() { return sensorDisplayName; }
    public void setSensorDisplayName(String sensorDisplayName) { this.sensorDisplayName = sensorDisplayName; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public List<Integer> getHistoricalCycles() { return historicalCycles; }
    public void setHistoricalCycles(List<Integer> historicalCycles) { this.historicalCycles = historicalCycles; }

    public List<Double> getHistoricalValues() { return historicalValues; }
    public void setHistoricalValues(List<Double> historicalValues) { this.historicalValues = historicalValues; }

    public List<Integer> getForecastCycles() { return forecastCycles; }
    public void setForecastCycles(List<Integer> forecastCycles) { this.forecastCycles = forecastCycles; }

    public List<Double> getForecastValues() { return forecastValues; }
    public void setForecastValues(List<Double> forecastValues) { this.forecastValues = forecastValues; }

    public List<Double> getUpperConfidence() { return upperConfidence; }
    public void setUpperConfidence(List<Double> upperConfidence) { this.upperConfidence = upperConfidence; }

    public List<Double> getLowerConfidence() { return lowerConfidence; }
    public void setLowerConfidence(List<Double> lowerConfidence) { this.lowerConfidence = lowerConfidence; }

    public double getCriticalThreshold() { return criticalThreshold; }
    public void setCriticalThreshold(double criticalThreshold) { this.criticalThreshold = criticalThreshold; }

    public boolean isThresholdBreached() { return thresholdBreached; }
    public void setThresholdBreached(boolean thresholdBreached) { this.thresholdBreached = thresholdBreached; }

    public int getEstimatedCyclesToBreach() { return estimatedCyclesToBreach; }
    public void setEstimatedCyclesToBreach(int estimatedCyclesToBreach) { this.estimatedCyclesToBreach = estimatedCyclesToBreach; }
}

package com.airdata.maintenance.ml;

import com.airdata.maintenance.model.TrendForecast;
import com.airdata.maintenance.model.AircraftComponentType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Time-series forecaster implementing Holt's Linear Exponential Smoothing with additive trend.
 * Models degradation trajectories and projects threshold crossing horizons with prediction uncertainty.
 */
public class HoltLinearForecaster implements Serializable {
    private static final long serialVersionUID = 1L;

    private final double alpha; // Level smoothing coefficient (0.0 to 1.0)
    private final double beta;  // Trend smoothing coefficient (0.0 to 1.0)

    public HoltLinearForecaster() {
        this(0.35, 0.15);
    }

    public HoltLinearForecaster(double alpha, double beta) {
        this.alpha = alpha;
        this.beta = beta;
    }

    /**
     * Forecasts future steps based on historical series.
     *
     * @param historicalCycles List of cycle numbers
     * @param historicalValues List of historical measurements
     * @param horizon          Number of future cycles to project
     * @param threshold        Critical safety limit (upper or lower depending on direction)
     * @param isUpperLimit     True if exceeding threshold is failure, false if dropping below
     */
    public TrendForecast forecast(String aircraftId, AircraftComponentType component, String sensorKey,
                                  String sensorDisplayName, String unit,
                                  List<Integer> historicalCycles, List<Double> historicalValues,
                                  int horizon, double threshold, boolean isUpperLimit) {
        TrendForecast tf = new TrendForecast();
        tf.setAircraftId(aircraftId);
        tf.setComponent(component);
        tf.setSensorKey(sensorKey);
        tf.setSensorDisplayName(sensorDisplayName);
        tf.setUnit(unit);
        tf.setHistoricalCycles(new ArrayList<>(historicalCycles));
        tf.setHistoricalValues(new ArrayList<>(historicalValues));
        tf.setCriticalThreshold(threshold);

        int n = historicalValues.size();
        if (n < 2) {
            // Not enough points to establish trend
            tf.setThresholdBreached(false);
            tf.setEstimatedCyclesToBreach(-1);
            return tf;
        }

        // Initialize Level and Trend
        double level = historicalValues.get(0);
        double trend = historicalValues.get(1) - historicalValues.get(0);
        double sumSqErrors = 0.0;

        for (int t = 1; t < n; t++) {
            double actual = historicalValues.get(t);
            double prevLevel = level;
            double prevTrend = trend;
            double oneStepPred = prevLevel + prevTrend;
            double error = actual - oneStepPred;
            sumSqErrors += error * error;

            level = alpha * actual + (1.0 - alpha) * (prevLevel + prevTrend);
            trend = beta * (level - prevLevel) + (1.0 - beta) * prevTrend;
        }

        double residualStd = Math.sqrt(sumSqErrors / Math.max(1, n - 2));
        int lastCycle = historicalCycles.get(n - 1);

        List<Integer> forecastCycles = new ArrayList<>(horizon);
        List<Double> forecastValues = new ArrayList<>(horizon);
        List<Double> upperBounds = new ArrayList<>(horizon);
        List<Double> lowerBounds = new ArrayList<>(horizon);

        boolean breached = false;
        int cyclesToBreach = -1;

        for (int h = 1; h <= horizon; h++) {
            int cycle = lastCycle + h;
            double pointForecast = level + h * trend;
            double horizonStd = residualStd * Math.sqrt(1.0 + (h - 1) * alpha * alpha);
            double upper = pointForecast + 1.96 * horizonStd;
            double lower = pointForecast - 1.96 * horizonStd;

            forecastCycles.add(cycle);
            forecastValues.add(pointForecast);
            upperBounds.add(upper);
            lowerBounds.add(lower);

            if (!breached) {
                if (isUpperLimit && pointForecast >= threshold) {
                    breached = true;
                    cyclesToBreach = h;
                } else if (!isUpperLimit && pointForecast <= threshold) {
                    breached = true;
                    cyclesToBreach = h;
                }
            }
        }

        tf.setForecastCycles(forecastCycles);
        tf.setForecastValues(forecastValues);
        tf.setUpperConfidence(upperBounds);
        tf.setLowerConfidence(lowerBounds);
        tf.setThresholdBreached(breached);
        tf.setEstimatedCyclesToBreach(cyclesToBreach);

        return tf;
    }
}

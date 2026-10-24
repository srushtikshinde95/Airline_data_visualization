package com.airdata.maintenance.ml;

import java.io.Serializable;
import java.util.*;

/**
 * Supervised Random Forest Regressor for Remaining Useful Life (RUL) estimation.
 * Provides point predictions along with empirical confidence intervals calculated from ensemble variance.
 */
public class RandomForestRegressor implements Serializable {
    private static final long serialVersionUID = 1L;

    public static class PredictionResult implements Serializable {
        private static final long serialVersionUID = 1L;
        public double predictedValue;
        public double standardDeviation;
        public double lowerBound90;
        public double upperBound90;

        public PredictionResult(double predictedValue, double standardDeviation, double lowerBound90, double upperBound90) {
            this.predictedValue = predictedValue;
            this.standardDeviation = standardDeviation;
            this.lowerBound90 = lowerBound90;
            this.upperBound90 = upperBound90;
        }
    }

    private final int numTrees;
    private final int maxDepth;
    private final int minSamplesSplit;
    private final int minSamplesLeaf;
    private final long randomSeed;

    private List<DecisionTree> trees;
    private double[] featureImportances;
    private String[] featureNames;

    public RandomForestRegressor() {
        this(35, 12, 4, 2, 88L);
    }

    public RandomForestRegressor(int numTrees, int maxDepth, int minSamplesSplit, int minSamplesLeaf, long randomSeed) {
        this.numTrees = numTrees;
        this.maxDepth = maxDepth;
        this.minSamplesSplit = minSamplesSplit;
        this.minSamplesLeaf = minSamplesLeaf;
        this.randomSeed = randomSeed;
        this.trees = new ArrayList<>();
    }

    public void train(double[][] X, double[] y, String[] featureNames) {
        if (X == null || X.length == 0) return;
        this.featureNames = featureNames;
        int nSamples = X.length;
        int nFeatures = X[0].length;
        int featuresPerSplit = Math.max(1, nFeatures / 3);

        Random rng = new Random(randomSeed);
        this.trees = new ArrayList<>(numTrees);
        double[] totalImportances = new double[nFeatures];

        for (int t = 0; t < numTrees; t++) {
            double[][] bootX = new double[nSamples][nFeatures];
            double[] bootY = new double[nSamples];
            for (int i = 0; i < nSamples; i++) {
                int pick = rng.nextInt(nSamples);
                bootX[i] = X[pick];
                bootY[i] = y[pick];
            }

            DecisionTree tree = new DecisionTree(DecisionTree.TreeType.REGRESSION,
                    maxDepth, minSamplesSplit, minSamplesLeaf, featuresPerSplit);
            tree.train(bootX, bootY, rng);
            trees.add(tree);

            double[] treeImportances = tree.getFeatureImportances();
            if (treeImportances != null) {
                for (int f = 0; f < nFeatures; f++) {
                    totalImportances[f] += treeImportances[f];
                }
            }
        }

        double sum = 0.0;
        for (double v : totalImportances) sum += v;
        this.featureImportances = new double[nFeatures];
        if (sum > 0.0) {
            for (int f = 0; f < nFeatures; f++) {
                this.featureImportances[f] = totalImportances[f] / sum;
            }
        }
    }

    public PredictionResult predictWithUncertainty(double[] sample) {
        if (trees.isEmpty()) return new PredictionResult(0.0, 0.0, 0.0, 0.0);

        double[] treePredictions = new double[trees.size()];
        double sum = 0.0;
        for (int i = 0; i < trees.size(); i++) {
            treePredictions[i] = trees.get(i).predict(sample);
            sum += treePredictions[i];
        }
        double mean = sum / trees.size();

        double var = 0.0;
        for (double pred : treePredictions) {
            double diff = pred - mean;
            var += diff * diff;
        }
        double std = Math.sqrt(var / trees.size());

        // 90% confidence interval (z = 1.645)
        double lower = Math.max(0.0, mean - 1.645 * std);
        double upper = mean + 1.645 * std;

        return new PredictionResult(Math.max(0.0, mean), std, lower, upper);
    }

    public double predict(double[] sample) {
        return predictWithUncertainty(sample).predictedValue;
    }

    public double[] getFeatureImportances() {
        return featureImportances;
    }

    public String[] getFeatureNames() {
        return featureNames;
    }
}

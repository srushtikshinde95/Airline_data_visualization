package com.airdata.maintenance.ml;

import java.io.Serializable;
import java.util.*;

/**
 * Supervised Random Forest Classifier for Failure Risk Prediction.
 * Combines bootstrap aggregation (bagging) with random subspace feature sampling.
 */
public class RandomForestClassifier implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int numTrees;
    private final int maxDepth;
    private final int minSamplesSplit;
    private final int minSamplesLeaf;
    private final long randomSeed;

    private List<DecisionTree> trees;
    private double[] featureImportances;
    private String[] featureNames;
    private double threshold = 0.50;

    public RandomForestClassifier() {
        this(35, 10, 4, 2, 42L);
    }

    public RandomForestClassifier(int numTrees, int maxDepth, int minSamplesSplit, int minSamplesLeaf, long randomSeed) {
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
        int featuresPerSplit = Math.max(1, (int) Math.sqrt(nFeatures));

        Random rng = new Random(randomSeed);
        this.trees = new ArrayList<>(numTrees);
        double[] totalImportances = new double[nFeatures];

        for (int t = 0; t < numTrees; t++) {
            // Bootstrap sampling with replacement
            double[][] bootX = new double[nSamples][nFeatures];
            double[] bootY = new double[nSamples];
            for (int i = 0; i < nSamples; i++) {
                int pick = rng.nextInt(nSamples);
                bootX[i] = X[pick];
                bootY[i] = y[pick];
            }

            DecisionTree tree = new DecisionTree(DecisionTree.TreeType.CLASSIFICATION,
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

        // Normalize feature importances
        double sum = 0.0;
        for (double v : totalImportances) sum += v;
        this.featureImportances = new double[nFeatures];
        if (sum > 0.0) {
            for (int f = 0; f < nFeatures; f++) {
                this.featureImportances[f] = totalImportances[f] / sum;
            }
        }
    }

    /**
     * Estimates failure probability in range [0.0, 1.0].
     */
    public double predictProbability(double[] sample) {
        if (trees.isEmpty()) return 0.0;
        double sum = 0.0;
        for (DecisionTree tree : trees) {
            sum += tree.predict(sample);
        }
        return Math.max(0.0, Math.min(1.0, sum / trees.size()));
    }

    public int predict(double[] sample) {
        return predictProbability(sample) >= threshold ? 1 : 0;
    }

    public Map<String, Double> getRankedFeatureImportances() {
        Map<String, Double> map = new LinkedHashMap<>();
        if (featureNames == null || featureImportances == null) return map;

        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < featureImportances.length; i++) order.add(i);
        order.sort((a, b) -> Double.compare(featureImportances[b], featureImportances[a]));

        for (int idx : order) {
            map.put(featureNames[idx], featureImportances[idx]);
        }
        return map;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public double getThreshold() {
        return threshold;
    }

    public double[] getFeatureImportances() {
        return featureImportances;
    }
}

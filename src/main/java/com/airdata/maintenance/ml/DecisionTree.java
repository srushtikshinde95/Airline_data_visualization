package com.airdata.maintenance.ml;

import java.io.Serializable;
import java.util.*;

/**
 * High-performance Binary Decision Tree supporting both Classification (Gini Impurity)
 * and Regression (Variance Reduction / Mean Squared Error).
 */
public class DecisionTree implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum TreeType { CLASSIFICATION, REGRESSION }

    public static class Node implements Serializable {
        private static final long serialVersionUID = 1L;

        public boolean isLeaf;
        public int featureIndex = -1;
        public double threshold;
        public double prediction;          // Class probability or regression value
        public Node left;
        public Node right;
        public double impurity;
        public int sampleCount;
    }

    private final TreeType type;
    private final int maxDepth;
    private final int minSamplesSplit;
    private final int minSamplesLeaf;
    private final int featuresPerSplit;     // Number of random features to evaluate (random subspace)
    private Node root;
    private double[] featureImportances;

    public DecisionTree(TreeType type, int maxDepth, int minSamplesSplit, int minSamplesLeaf, int featuresPerSplit) {
        this.type = type;
        this.maxDepth = maxDepth;
        this.minSamplesSplit = minSamplesSplit;
        this.minSamplesLeaf = minSamplesLeaf;
        this.featuresPerSplit = featuresPerSplit;
    }

    public void train(double[][] X, double[] y, Random rng) {
        int numFeatures = X[0].length;
        featureImportances = new double[numFeatures];
        int[] sampleIndices = new int[X.length];
        for (int i = 0; i < X.length; i++) sampleIndices[i] = i;

        root = buildTree(X, y, sampleIndices, 0, rng);
    }

    private Node buildTree(double[][] X, double[] y, int[] indices, int currentDepth, Random rng) {
        Node node = new Node();
        node.sampleCount = indices.length;

        if (type == TreeType.CLASSIFICATION) {
            double sum = 0.0;
            for (int idx : indices) sum += y[idx];
            node.prediction = indices.length > 0 ? sum / indices.length : 0.0;
            node.impurity = computeGini(y, indices);
        } else {
            double sum = 0.0;
            for (int idx : indices) sum += y[idx];
            node.prediction = indices.length > 0 ? sum / indices.length : 0.0;
            node.impurity = computeVariance(y, indices);
        }

        // Stopping criteria
        if (currentDepth >= maxDepth || indices.length < 2 || (indices.length < minSamplesSplit && currentDepth > 0) || node.impurity == 0.0) {
            node.isLeaf = true;
            return node;
        }

        int numFeatures = X[0].length;
        int m = Math.min(numFeatures, Math.max(1, featuresPerSplit));
        List<Integer> featurePool = new ArrayList<>(numFeatures);
        for (int f = 0; f < numFeatures; f++) featurePool.add(f);
        Collections.shuffle(featurePool, rng);
        List<Integer> selectedFeatures = featurePool.subList(0, m);

        int bestFeature = -1;
        double bestThreshold = 0.0;
        double bestGain = -1.0;
        int[] bestLeftIndices = null;
        int[] bestRightIndices = null;

        int effectiveMinLeaf = Math.min(minSamplesLeaf, Math.max(1, indices.length / 4));

        for (int f : selectedFeatures) {
            // Collect unique split candidates (percentiles or sampled values)
            double[] values = new double[indices.length];
            for (int i = 0; i < indices.length; i++) values[i] = X[indices[i]][f];
            Arrays.sort(values);

            int step = Math.max(1, values.length / 20); // Check up to 20 candidate thresholds
            for (int v = 0; v < values.length - 1; v += step) {
                if (values[v] == values[v + 1]) continue;
                double threshold = (values[v] + values[v + 1]) / 2.0;

                int leftCount = 0;
                for (int idx : indices) {
                    if (X[idx][f] <= threshold) leftCount++;
                }
                int rightCount = indices.length - leftCount;

                if (leftCount < effectiveMinLeaf || rightCount < effectiveMinLeaf) continue;

                int[] left = new int[leftCount];
                int[] right = new int[rightCount];
                int li = 0, ri = 0;
                for (int idx : indices) {
                    if (X[idx][f] <= threshold) left[li++] = idx;
                    else right[ri++] = idx;
                }

                double leftImpurity = (type == TreeType.CLASSIFICATION) ? computeGini(y, left) : computeVariance(y, left);
                double rightImpurity = (type == TreeType.CLASSIFICATION) ? computeGini(y, right) : computeVariance(y, right);
                double weightedChildImpurity = ((double) leftCount / indices.length) * leftImpurity 
                                             + ((double) rightCount / indices.length) * rightImpurity;
                double gain = node.impurity - weightedChildImpurity;

                if (gain > bestGain) {
                    bestGain = gain;
                    bestFeature = f;
                    bestThreshold = threshold;
                    bestLeftIndices = left;
                    bestRightIndices = right;
                }
            }
        }

        if (bestGain <= 0.0 || bestLeftIndices == null || bestRightIndices == null) {
            node.isLeaf = true;
            return node;
        }

        node.isLeaf = false;
        node.featureIndex = bestFeature;
        node.threshold = bestThreshold;
        featureImportances[bestFeature] += bestGain * indices.length;

        node.left = buildTree(X, y, bestLeftIndices, currentDepth + 1, rng);
        node.right = buildTree(X, y, bestRightIndices, currentDepth + 1, rng);

        return node;
    }

    public double predict(double[] sample) {
        if (root == null) return 0.0;
        Node curr = root;
        while (!curr.isLeaf) {
            if (sample[curr.featureIndex] <= curr.threshold) {
                curr = curr.left != null ? curr.left : curr;
                if (curr.isLeaf) break;
            } else {
                curr = curr.right != null ? curr.right : curr;
                if (curr.isLeaf) break;
            }
        }
        return curr.prediction;
    }

    private double computeGini(double[] y, int[] indices) {
        if (indices.length == 0) return 0.0;
        int count1 = 0;
        for (int idx : indices) {
            if (y[idx] >= 0.5) count1++;
        }
        double p1 = (double) count1 / indices.length;
        double p0 = 1.0 - p1;
        return 1.0 - (p0 * p0 + p1 * p1);
    }

    private double computeVariance(double[] y, int[] indices) {
        if (indices.length <= 1) return 0.0;
        double mean = 0.0;
        for (int idx : indices) mean += y[idx];
        mean /= indices.length;
        double sse = 0.0;
        for (int idx : indices) {
            double diff = y[idx] - mean;
            sse += diff * diff;
        }
        return sse / indices.length;
    }

    public double[] getFeatureImportances() {
        return featureImportances;
    }
}

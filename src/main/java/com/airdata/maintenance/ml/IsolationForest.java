package com.airdata.maintenance.ml;

import java.io.Serializable;
import java.util.*;

/**
 * Unsupervised Isolation Forest for aircraft sensor anomaly detection.
 * Identifies abnormal multidimensional operating conditions without requiring labeled failure events.
 */
public class IsolationForest implements Serializable {
    private static final long serialVersionUID = 1L;

    public static class ITreeNode implements Serializable {
        private static final long serialVersionUID = 1L;
        public boolean isLeaf;
        public int size;
        public int splitFeature = -1;
        public double splitValue;
        public ITreeNode left;
        public ITreeNode right;
    }

    private final int numTrees;
    private final int subSampleSize;
    private final int maxTreeHeight;
    private final long randomSeed;
    private final List<ITreeNode> trees;
    private double anomalyThreshold = 0.62;
    private String[] featureNames;

    public IsolationForest() {
        this(100, 256, 101L);
    }

    public IsolationForest(int numTrees, int subSampleSize, long randomSeed) {
        this.numTrees = numTrees;
        this.subSampleSize = subSampleSize;
        this.randomSeed = randomSeed;
        this.maxTreeHeight = (int) Math.ceil(Math.log(Math.max(2, subSampleSize)) / Math.log(2));
        this.trees = new ArrayList<>();
    }

    public void train(double[][] X, String[] featureNames) {
        if (X == null || X.length == 0) return;
        this.featureNames = featureNames;
        int nSamples = X.length;
        int nFeatures = X[0].length;
        int sampleSize = Math.min(nSamples, subSampleSize);

        Random rng = new Random(randomSeed);
        trees.clear();

        for (int t = 0; t < numTrees; t++) {
            // Subsampling without replacement
            int[] indices = new int[nSamples];
            for (int i = 0; i < nSamples; i++) indices[i] = i;
            // Fisher-Yates partial shuffle
            for (int i = 0; i < sampleSize; i++) {
                int r = i + rng.nextInt(nSamples - i);
                int tmp = indices[i];
                indices[i] = indices[r];
                indices[r] = tmp;
            }

            double[][] subX = new double[sampleSize][nFeatures];
            for (int i = 0; i < sampleSize; i++) {
                subX[i] = X[indices[i]];
            }

            ITreeNode tree = buildITree(subX, 0, maxTreeHeight, rng);
            trees.add(tree);
        }
    }

    private ITreeNode buildITree(double[][] X, int currentHeight, int heightLimit, Random rng) {
        ITreeNode node = new ITreeNode();
        node.size = X.length;

        if (currentHeight >= heightLimit || X.length <= 1) {
            node.isLeaf = true;
            return node;
        }

        int nFeatures = X[0].length;
        // Find min and max for all features to ensure non-degenerate splits
        List<Integer> validFeatures = new ArrayList<>();
        double[] mins = new double[nFeatures];
        double[] maxs = new double[nFeatures];

        for (int f = 0; f < nFeatures; f++) {
            double min = Double.MAX_VALUE;
            double max = -Double.MAX_VALUE;
            for (double[] row : X) {
                if (row[f] < min) min = row[f];
                if (row[f] > max) max = row[f];
            }
            mins[f] = min;
            maxs[f] = max;
            if (max > min) validFeatures.add(f);
        }

        if (validFeatures.isEmpty()) {
            node.isLeaf = true;
            return node;
        }

        int selectedFeature = validFeatures.get(rng.nextInt(validFeatures.size()));
        double min = mins[selectedFeature];
        double max = maxs[selectedFeature];
        double splitValue = min + rng.nextDouble() * (max - min);

        int leftCount = 0;
        for (double[] row : X) {
            if (row[selectedFeature] < splitValue) leftCount++;
        }
        int rightCount = X.length - leftCount;

        if (leftCount == 0 || rightCount == 0) {
            node.isLeaf = true;
            return node;
        }

        double[][] leftX = new double[leftCount][nFeatures];
        double[][] rightX = new double[rightCount][nFeatures];
        int li = 0, ri = 0;
        for (double[] row : X) {
            if (row[selectedFeature] < splitValue) leftX[li++] = row;
            else rightX[ri++] = row;
        }

        node.isLeaf = false;
        node.splitFeature = selectedFeature;
        node.splitValue = splitValue;
        node.left = buildITree(leftX, currentHeight + 1, heightLimit, rng);
        node.right = buildITree(rightX, currentHeight + 1, heightLimit, rng);

        return node;
    }

    /**
     * Calculates the anomaly score s(x, n) in [0.0, 1.0].
     * Scores close to 1 indicate strong anomalies; scores below 0.5 indicate normal instances.
     */
    public double scoreAnomaly(double[] sample) {
        if (trees.isEmpty()) return 0.0;
        double totalPathLength = 0.0;
        for (ITreeNode root : trees) {
            totalPathLength += pathLength(sample, root, 0);
        }
        double avgPathLength = totalPathLength / trees.size();
        double cN = averagePathLengthAdjustment(subSampleSize);
        if (cN <= 0.0) return 0.0;

        return Math.pow(2.0, - (avgPathLength / cN));
    }

    public boolean isAnomaly(double[] sample) {
        return scoreAnomaly(sample) >= anomalyThreshold;
    }

    private double pathLength(double[] sample, ITreeNode node, int currentDepth) {
        if (node == null) return currentDepth;
        if (node.isLeaf) {
            return currentDepth + averagePathLengthAdjustment(node.size);
        }
        if (sample[node.splitFeature] < node.splitValue) {
            return pathLength(sample, node.left, currentDepth + 1);
        } else {
            return pathLength(sample, node.right, currentDepth + 1);
        }
    }

    /**
     * Average path length of unsuccessful search in BST: c(n) = 2*ln(n-1) + 0.5772156649 - (2*(n-1)/n)
     */
    private double averagePathLengthAdjustment(int n) {
        if (n <= 1) return 0.0;
        if (n == 2) return 1.0;
        double harmonic = Math.log(n - 1) + 0.5772156649; // Euler-Mascheroni constant
        return 2.0 * harmonic - (2.0 * (n - 1.0) / n);
    }

    public void setAnomalyThreshold(double threshold) {
        this.anomalyThreshold = threshold;
    }

    public double getAnomalyThreshold() {
        return anomalyThreshold;
    }

    public String[] getFeatureNames() {
        return featureNames;
    }
}

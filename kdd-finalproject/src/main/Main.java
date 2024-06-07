package main;


import java.util.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;


public class Main {
    public static void main(String[] args) {
        Processor processor = new CSVProcessor();
        List<String[]> records = processor.readData("./files/iris.data");
        records.remove(0);
//        processor.processData(records);
        double[][] data = convertToDoubleArray(records);

        int[] minPointsRange = {2, 3, 4, 5, 6, 7, 8, 9, 10};
        double[] epsilonRange = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0};
        double bestEvaluation = 1000;   //arbitrary large number
        List<List<double[]>> bestClusters = new ArrayList<>();
        int bestMinPoints = minPointsRange[0];
        double bestEpsilon = 0.5;

        for (int minPoints : minPointsRange) {
            for (double epsilon : epsilonRange) {
                DBSCAN dbscan = new DBSCAN(epsilon, minPoints);
                List<List<double[]>> clusters = dbscan.fit(data);
                double evaluation = evaluateCluster(clusters);
                System.out.println("MinPoints, Epsilon: [" + minPoints + ", " + epsilon+ "], Evaluation: " + evaluation);
                if (evaluation < bestEvaluation) {  //we want a low value, since evaluation is average gap size
                    bestEvaluation = evaluation;
                    bestClusters = clusters;
                    bestMinPoints = minPoints;
                    bestEpsilon = epsilon;
                }
            }
        }
      
        System.out.println("Best MinPoints, Epsilon: [" + bestMinPoints + ", " + bestEpsilon + "]");
        System.out.println("Clusters:");
        for (int i = 0; i < bestClusters.size(); i++) {
            System.out.println("Cluster " + (i + 1) + ":");
            for (double[] point : bestClusters.get(i)) {
                System.out.print("(");
                for (double val : point) {
                    System.out.print(val + " ");
                }
                System.out.println(")");
            }
        }

        String cluster = "cluster1";
        writeClustersToFile(bestClusters, cluster);

//        testCalculateClusterEntropy();
//        testCalculateTotalEntropy();

//        DBSCAN dbscan = new DBSCAN(0.5, 3);
//        List<List<double[]>> clusters = dbscan.fit(data);
//
//        System.out.println("Clusters:");
//        for (int i = 0; i < clusters.size(); i++) {
//            System.out.println("Cluster " + (i + 1) + ":");
//            for (double[] point : clusters.get(i)) {
//                System.out.print("(");
//                for (double val : point) {
//                    System.out.print(val + " ");
//                }
//                System.out.println(")");
//            }
//        }

    }

    private static double[][] convertToDoubleArray(List<String[]> records) {
        double[][] data = new double[records.size()][records.get(0).length];
        for (int i = 0; i < records.size(); i++) {
            for (int j = 0; j < records.get(0).length; j++) {
                data[i][j] = Double.parseDouble(records.get(i)[j]);
            }
        }
        return data;
    }

    public static double evaluateCluster(List<List<double[]>> clusters) {
        /*
        Pick random minPoints and epsilon
        generate clusters
        Pick a random point in a every cluster
            In that cluster, calculate distance of that point from every other points (e.g 5, 10, 25)
            Find the biggest gap between distance (e.g. 15)
        average all the gaps for each cluster
        We want to minimize this gap
         */
        //silhouette score?
        //score - (#noise/#totalPoints)
        //plotly
        if (clusters.isEmpty()) { //if no clusters were generated
            return 1000.0; //large numbers are valued less
        }
        double totalSize = 0;   //else, find the avg gap for all clusters
        double gapSum = 0;
        for (List<double[]> cluster : clusters) {
            if(cluster.size() > 1) {
                Random randomizer = new Random();
                double[] randPoint = cluster.get(randomizer.nextInt(cluster.size())); //choose random point
                List<double[]> tempCluster = new ArrayList<>(cluster); //make a copy
                tempCluster.remove(randPoint);  //remove the random point
                List<Double> distList = new ArrayList<>(); //distance list
                for (double[] point : tempCluster) {
                    distList.add(findDistance(randPoint, point));
                }
                Collections.sort(distList);
                Collections.reverse(distList); //descending order
                gapSum += findGap(distList); //Find the largest gap in cluster
            }

        }
        return gapSum / clusters.size(); // Average the gap of all clusters
    }

    private static double findDistance(double[] point1, double[] point2) {
        double sum = 0;
        for (int i = 0; i < point1.length; i++) {
            sum += Math.pow(point1[i] - point2[i], 2);
        }
        return Math.sqrt(sum);
    }

    /**
     * finds the largest gap in a list sorted in descending order.
     * @param myList    A list in descending order
     * @return  The largest gap between values in the list
     */
    private static double findGap(List<Double> myList) {
        if(myList.size() > 1) {
            double maxGap = 0;
            double gap = 0;
            double max = myList.get(0);
            double cur;
            for (int i = 0; i < myList.size(); i++) {
                cur = myList.get(i);
                gap = max - cur;
                if (gap > maxGap) {
                    maxGap = gap;
                }
            }
            return maxGap;
        } else {return myList.get(0);} //if size is 1, then cluster size is 2. distance is already the maxGap
    }

    public static void writeClustersToFile(List<List<double[]>> bestClusters, String fileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (int i = 0; i < bestClusters.size(); i++) {
                for (double[] point : bestClusters.get(i)) {

                    for (int j = 0; j < point.length; j++) {
                        writer.write(point[j] + (j < point.length - 1 ? ", " : ""));
                    }
                    writer.write(", " + (i + 1) + "\n");
                }
            }
            System.out.println("Written to file " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static double calculateClusterEntropy(List<double[]> cluster) {
        Map<String, Integer> labelCounts = new HashMap<>();
        for (double[] point : cluster) {
            String label = String.valueOf(point[point.length - 1]);
            labelCounts.put(label, labelCounts.getOrDefault(label, 0) + 1);
        }
        int totalInstances = cluster.size();
        double entropy = 0.0;
        for (Map.Entry<String, Integer> entry : labelCounts.entrySet()) {
            double probability = (double) entry.getValue() / totalInstances;
            entropy -= probability * (Math.log(probability) / Math.log(2));
        }
        return entropy;
    }

    public static double calculateTotalEntropy(List<List<double[]>> clusters) {
        double totalEntropy = 0.0;
        int totalInstances = 0;

        for (List<double[]> cluster : clusters) {
            int clusterSize = cluster.size();
            totalInstances += clusterSize;
            totalEntropy += calculateClusterEntropy(cluster) * clusterSize;
        }
        if (totalInstances==0){
            return 0;
        }else {
            return totalEntropy / totalInstances;
        }
    }

    public static double calculateClusterPurity(List<double[]> cluster) {
        return (double) getMaxLabelCount(getLabelCounts(cluster)) / cluster.size();
    }

    public static double calculateTotalPurity(List<List<double[]>> clusters) {
        int totalInstances = 0;
        int sumMaxLabelCounts = 0;

        for (List<double[]> cluster : clusters) {
            Map<String, Integer> labelCounts = getLabelCounts(cluster);
            int maxLabelCount = getMaxLabelCount(labelCounts);
            sumMaxLabelCounts += maxLabelCount;
            totalInstances += cluster.size();
        }
        if (totalInstances==0){
            return 0;
        }else {
            return (double) sumMaxLabelCounts / totalInstances;
        }
    }

    private static int getMaxLabelCount(Map<String, Integer> labelCounts){
        return labelCounts.values().stream().max(Integer::compare).orElse(0);
    }

    public static Map<String, Integer> getLabelCounts(List<double[]> cluster) {
        Map<String, Integer> labelCounts = new HashMap<>();
        for (double[] point : cluster) {
            String label = String.valueOf(point[point.length - 1]);
            labelCounts.put(label, labelCounts.getOrDefault(label, 0) + 1);
        }
        return labelCounts;
    }

    private static void testCalculateClusterEntropy() {
        // Test case 1: Single class cluster
        List<double[]> cluster1 = new ArrayList<>();
        cluster1.add(new double[]{1.0, 1.0, 1.0, 1.0, 1.0});
        cluster1.add(new double[]{1.0, 1.0, 1.0, 1.0, 1.0});
        cluster1.add(new double[]{1.0, 1.0, 1.0, 1.0, 1.0});
        double entropy1 = calculateClusterEntropy(cluster1);
        System.out.println(entropy1 + " testCalculateClusterEntropy - Single class 0.0");

        // Test case 2: Two classes with equal distribution
        List<double[]> cluster2 = new ArrayList<>();
        cluster2.add(new double[]{1.0, 1.0, 1.0, 1.0, 0.0});
        cluster2.add(new double[]{1.0, 1.0, 1.0, 1.0, 0.0});
        cluster2.add(new double[]{1.0, 1.0, 1.0, 1.0, 1.0});
        cluster2.add(new double[]{1.0, 1.0, 1.0, 1.0, 1.0});
        double entropy2 = calculateClusterEntropy(cluster2);
        System.out.println(entropy2 + " testCalculateClusterEntropy - Two classes equal distribution 1.0");

        // Test case 3: Two classes with unequal distribution
        List<double[]> cluster3 = new ArrayList<>();
        cluster3.add(new double[]{1.0, 1.0, 1.0, 1.0, 0.0});
        cluster3.add(new double[]{1.0, 1.0, 1.0, 1.0, 0.0});
        cluster3.add(new double[]{1.0, 1.0, 1.0, 1.0, 0.0});
        cluster3.add(new double[]{1.0, 1.0, 1.0, 1.0, 1.0});
        double entropy3 = calculateClusterEntropy(cluster3);
        System.out.println(entropy3 + " testCalculateClusterEntropy - Two classes unequal distribution 0.811");
    }

    private static void testCalculateTotalEntropy() {
        // Test case 1: Single cluster
        List<List<double[]>> clusters1 = new ArrayList<>();
        List<double[]> cluster1 = new ArrayList<>();
        cluster1.add(new double[]{1.0, 1.0, 1.0, 1.0, 1.0});
        cluster1.add(new double[]{1.0, 1.0, 1.0, 1.0, 1.0});
        clusters1.add(cluster1);
        double entropy1 = calculateTotalEntropy(clusters1);
        System.out.println(entropy1 + " testCalculateTotalEntropy - Single cluster 0.0");

        // Test case 2: Multiple clusters with equal distribution
        List<List<double[]>> clusters2 = new ArrayList<>();
        List<double[]> cluster2a = new ArrayList<>();
        cluster2a.add(new double[]{1.0, 1.0, 1.0, 1.0, 0.0});
        cluster2a.add(new double[]{1.0, 1.0, 1.0, 1.0, 1.0});
        List<double[]> cluster2b = new ArrayList<>();
        cluster2b.add(new double[]{1.0, 1.0, 1.0, 1.0, 0.0});
        cluster2b.add(new double[]{1.0, 1.0, 1.0, 1.0, 1.0});
        clusters2.add(cluster2a);
        clusters2.add(cluster2b);
        double entropy2 = calculateTotalEntropy(clusters2);
        System.out.println(entropy2 + " testCalculateTotalEntropy - Multiple clusters equal distribution 1.0");
    }
}

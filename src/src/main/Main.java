package src.main;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Processor processor = new CSVProcessor();
        List<String[]> records = processor.readData("./files/tsne scores.csv");
        records.remove(0);
        // processor.processData(records);
        double[][] data = convertToDoubleArray(records);

        int[] minPointsRange = {2, 3, 4, 5, 6, 7, 8, 9, 10};
        double bestEvaluation = 0;
        List<List<double[]>> bestClusters = new ArrayList<>();
        int bestMinPoints = minPointsRange[0];

        for (int minPoints : minPointsRange) {
            DBSCAN dbscan = new DBSCAN(0.5, minPoints);
            List<List<double[]>> clusters = dbscan.fit(data);
            double evaluation = evaluateCluster(clusters);
            System.out.println("MinPoints: " + minPoints + ", Evaluation: " + evaluation);
            if (evaluation > bestEvaluation) {
                bestEvaluation = evaluation;
                bestClusters = clusters;
                bestMinPoints = minPoints;
            }
        }

        System.out.println("Best MinPoints: " + bestMinPoints);
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
        if (clusters.isEmpty()) {
            return 0.0;
        }

        double totalSize = 0;
        for (List<double[]> cluster : clusters) {
            totalSize += cluster.size();
        }

        return totalSize / clusters.size(); // Average cluster size
    }
}

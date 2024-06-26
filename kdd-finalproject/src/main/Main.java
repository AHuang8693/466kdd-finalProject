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
//        records.remove(0);
//        processor.processData(records);
        double[][] data = convertToDoubleArray(records);
        List<DataPoint> dataList = convertToDataPoint(data);
        //default: 5, 0.5
        int[] minPointsRange = {2, 3, 4, 5, 6, 7, 8, 9, 10};
        double[] epsilonRange = {0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0, 1.1, 1.2, 1.3, 1.4, 1.5};
        double bestEvaluation = -1;   //worst possible silhouette score
        List<List<DataPoint>> bestClusters = new ArrayList<>();
        int bestMinPoints = minPointsRange[0];
        double bestEpsilon = 0.5;

        for (int minPoints : minPointsRange) {
            for (double epsilon : epsilonRange) {
                DBSCAN dbscan = new DBSCAN(epsilon, minPoints);
                List<List<DataPoint>> clusters = dbscan.fit(dataList);
                double evaluation = evaluateSilhouette(clusters);
                System.out.println("MinPoints, Epsilon: [" + minPoints + ", " + epsilon+ "], Evaluation: " + evaluation);
                if (evaluation > bestEvaluation) {  //we want a high value for silhouette
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
            for (DataPoint point : bestClusters.get(i)) {
                System.out.print("(");
                for (double val : point.getData()) {
                    System.out.print(val + " ");
                }
                System.out.println(")");
            }
        }

        String cluster = "cluster1";
        writeClustersToFile(bestClusters, cluster);

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

    //score - (#noise/#totalPoints) ?
    public static double evaluateSilhouette(List<List<DataPoint>> clusters) {
        if (clusters.isEmpty() || clusters.size() < 2) { //can't compare with zero or less than two clusters
            return -1.0; //ranges -1 to 1, with -1 being the worst
        }

        double totalSilScore = 0;
        int totalPoints = 0;
        for (List<DataPoint> someCluster : clusters) {
            for(DataPoint somePoint : someCluster) {
                List<DataPoint> tempCluster = new ArrayList<>(someCluster); //make a copy
                tempCluster.remove(somePoint);  //remove the random point
                List<Double> distList = new ArrayList<>(); //distance list

                //Calculate a - intra-cluster distance, somePoint to all other points in cluster
                double intraDist = 0;
                for (DataPoint point : tempCluster) {
                    intraDist += findDistance(somePoint, point);
                }
                intraDist = intraDist / tempCluster.size(); //dont want to include dist(somePoint, somePoint)

                //Calculate b - nearest cluster dist ()
                double nearestClusterDist = Double.MAX_VALUE;
                for (List<DataPoint> otherCluster : clusters) {
                    if (otherCluster != someCluster) {
                        double otherClusterDist = 0;
                        for (DataPoint point : otherCluster) {
                            otherClusterDist += findDistance(somePoint, point);
                        }
                        otherClusterDist = otherClusterDist/otherCluster.size();
                        if (otherClusterDist < nearestClusterDist) {
                            nearestClusterDist = otherClusterDist;
                        }
                    }
                }
                double silhouetteScore = (nearestClusterDist - intraDist) / Math.max(intraDist, nearestClusterDist);
                totalSilScore += silhouetteScore;
                totalPoints++;
            }
        }
        return totalSilScore / totalPoints;
    }

    //used for the gap statistic
    public static double evaluateCluster(List<List<DataPoint>> clusters) {
        if (clusters.isEmpty()) { //if no clusters were generated
            return 1000.0; //large numbers are valued less
        }
        double totalSize = 0;   //else, find the avg gap for all clusters
        double gapSum = 0;
        for (List<DataPoint> cluster : clusters) {
            if(cluster.size() > 1) {
                Random randomizer = new Random();
                DataPoint randPoint = cluster.get(randomizer.nextInt(cluster.size())); //choose random point
                List<DataPoint> tempCluster = new ArrayList<>(cluster); //make a copy
                tempCluster.remove(randPoint);  //remove the random point
                List<Double> distList = new ArrayList<>(); //distance list
                for (DataPoint point : tempCluster) {
                    distList.add(findDistance(randPoint, point));
                }
                Collections.sort(distList);
                Collections.reverse(distList); //descending order
                gapSum += findGap(distList); //Find the largest gap in cluster
            }

        }
        return gapSum / clusters.size(); // Average the gap of all clusters
    }

    private static double findDistance(DataPoint point1, DataPoint point2) {
        double sum = 0;
        for (int i = 0; i < point1.getData().length; i++) {
            sum += Math.pow(point1.getData()[i] - point2.getData()[i], 2);
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

    public static void writeClustersToFile(List<List<DataPoint>> bestClusters, String fileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (int i = 0; i < bestClusters.size(); i++) {
                for (DataPoint point : bestClusters.get(i)) {

                    for (int j = 0; j < point.getData().length; j++) {
                        writer.write(point.getData()[j] + (j < point.getData().length - 1 ? ", " : ""));
                    }
                    writer.write(", " + (i + 1) + "\n");
                }
            }
            System.out.println("Written to file " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<DataPoint> convertToDataPoint(double[][] data) {
        List<DataPoint> dataList = new ArrayList<>();
        int count = 0;
        for (double[] point : data) {
                DataPoint p = new DataPoint(count, point);
                dataList.add(p);
                count++;
        }
        return dataList;
    }
}

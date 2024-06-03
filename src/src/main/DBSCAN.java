package src.main;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DBSCAN {
    private double epsilon;
    private int minPoints;

    public DBSCAN(double epsilon, int minPoints) {
        this.epsilon = epsilon;
        this.minPoints = minPoints;
    }

    public List<List<double[]>> fit(double[][] data) {
        List<List<double[]>> clusters = new ArrayList<>();
        Set<double[]> visited = new HashSet<>();
        Set<double[]> noise = new HashSet<>();

        for (double[] point : data) {
            if (!visited.contains(point)) {
                visited.add(point);
                List<double[]> neighbors = getNeighbors(point, data);
                if (neighbors.size() >= minPoints) {
                    List<double[]> cluster = new ArrayList<>();
                    clusters.add(expandCluster(point, neighbors, cluster, visited, data));
                } else {
                    noise.add(point);
                }
            }
        }
        return clusters;
    }

    private List<double[]> expandCluster(double[] point,
                                         List<double[]> neighbors,
                                         List<double[]> cluster,
                                         Set<double[]> visited,
                                         double[][] data) {
        cluster.add(point);

        int index = 0;
        while (index < neighbors.size()) {
            double[] neighbor = neighbors.get(index);

            if (!visited.contains(neighbor)) {
                visited.add(neighbor);
                List<double[]> neighborNeighbors = getNeighbors(neighbor, data);
                if (neighborNeighbors.size() >= minPoints) {
                    neighbors.addAll(neighborNeighbors);
                }
            }

            if (!isInCluster(neighbor, cluster)) {
                cluster.add(neighbor);
            }
            index++;
        }
        return cluster;
    }

    private List<double[]> getNeighbors(double[] point, double[][] data) {
        List<double[]> neighbors = new ArrayList<>();
        for (double[] candidate : data) {
            if (distance(point, candidate) <= epsilon) {
                neighbors.add(candidate);
            }
        }
        return neighbors;
    }

    private double distance(double[] point1, double[] point2) {
        double sum = 0;
        for (int i = 0; i < point1.length; i++) {
            sum += Math.pow(point1[i] - point2[i], 2);
        }
        return Math.sqrt(sum);
    }

    private boolean isInCluster(double[] point, List<double[]> cluster) {
        for (double[] clusterPoint : cluster) {
            if (clusterPoint == point) {
                return true;
            }
        }
        return false;
    }
}

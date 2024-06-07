package main;

import java.util.*;

public class DBSCAN {
    private double epsilon;
    private int minPoints;

    public DBSCAN(double epsilon, int minPoints) {
        this.epsilon = epsilon;
        this.minPoints = minPoints;
    }

    public List<List<DataPoint>> fit(List<DataPoint> data) {
        List<List<DataPoint>> clusters = new ArrayList<>();
        HashMap<Integer, DataPoint> visited = new HashMap<>();
        Set<DataPoint> noise = new HashSet<>();

        for (DataPoint dPoint : data) {
            int id = dPoint.getId();
            if (visited.get(id) == null) { //if point is not visited
                visited.put(id, dPoint);
                List<DataPoint> neighbors = getNeighbors(dPoint, data);
                if (neighbors.size() >= minPoints) {
                    List<DataPoint> cluster = new ArrayList<>();
                    clusters.add(expandCluster(dPoint, neighbors, cluster, visited, data));
                } else {
                    noise.add(dPoint);
                }
            }
        }
        return clusters;
    }

    private List<DataPoint> expandCluster(DataPoint point,
                                         List<DataPoint> neighbors,
                                         List<DataPoint> cluster,
                                          HashMap<Integer, DataPoint> visited,
                                         List<DataPoint> data) {
        cluster.add(point);

        int index = 0;
        while (index < neighbors.size()) {
            DataPoint neighbor = neighbors.get(index);
            int id = neighbor.getId();
            if (visited.get(id) == null) {  //doesn't have datapoint in map
                visited.put(id, neighbor);
                List<DataPoint> neighborNeighbors = getNeighbors(neighbor, data);
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

    private List<DataPoint> getNeighbors(DataPoint point, List<DataPoint> data) {
        List<DataPoint> neighbors = new ArrayList<>();
        for (DataPoint candidate : data) {
            if (distance(point, candidate) <= epsilon) {
                neighbors.add(candidate);
            }
        }
        return neighbors;
    }

    private double distance(DataPoint point1, DataPoint point2) {
        double sum = 0;
        for (int i = 0; i < point1.getData().length; i++) {
            sum += Math.pow(point1.getData()[i] - point2.getData()[i], 2);
        }
        return Math.sqrt(sum);
    }

    private boolean isInCluster(DataPoint point, List<DataPoint> cluster) {
        for (DataPoint clusterPoint : cluster) {
            if (clusterPoint == point) {
                return true;
            }
        }
        return false;
    }
}

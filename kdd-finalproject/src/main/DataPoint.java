package main;

import java.util.Arrays;

public class DataPoint {
    private final int id;
    private final double[] data;

    public DataPoint(int id, double[] data) {
        this.id = id;
        this.data = data;
    }

    public int getId() {return id;}
    public double[] getData() {return data;}

    @Override
    public String toString() {
        return "DataPoint{" +
                "id=" + id +
                ", data=" + Arrays.toString(data) +
                '}';
    }

    @Override
    public boolean equals(Object o) {   //equal if id is the same
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataPoint dataPoint = (DataPoint) o;
        return id == dataPoint.id;
    }

}

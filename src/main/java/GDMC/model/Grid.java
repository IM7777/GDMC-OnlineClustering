package GDMC.model;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by jxm on 2021/7/17.
 */
public class Grid implements Serializable {
    private static final long serialVersionUID = 4939831091940554949L;
    private Point centroid;
    private int[] vector;
    private double density;
    private long updateTime;
    private int dim;
    private double lambda;
    private double delta;
    private double centerDistance;
    private Grid nearestNeighbor;
    private int label;

    public Grid(double lambda, Point point, int[] vector) {
        this.density = 1;
        this.dim = point.getDim();
        this.lambda = lambda;
        this.centroid = point;
        this.updateTime = point.getId();
        this.vector = new int[dim];
        this.vector = vector;
        this.delta = Double.MAX_VALUE;
        this.centerDistance = Double.MAX_VALUE;
        this.label = -1;

    }

    public double getUpdateDensity(long time) {
        if (time > updateTime) {
            this.density = this.density * Math.pow(lambda, time - this.updateTime);
            this.updateTime = time;
        }
        return this.density;
    }


    public void updateGrid(Point point) {
        double updateDensity = getUpdateDensity(point.getId());
        for (int i = 0; i < this.dim; i++) {
            this.centroid.getAttr()[i] = (this.centroid.getAttr()[i] * updateDensity + point.getAttr()[i]) / (updateDensity + 1);
        }
        this.density = updateDensity + 1;
    }

    public double getCenterDistance() {
        return centerDistance;
    }

    public void setCenterDistance(double centerDistance) {
        this.centerDistance = centerDistance;
    }

    public Point getCentroid() {
        return centroid;
    }

    public int[] getVector() {
        return vector;
    }

    public double getDensity() {
        return density;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public int getDim() {
        return dim;
    }

    public double getLambda() {
        return lambda;
    }

    public double getDelta() {
        return delta;
    }

    public int getLabel() {
        return label;
    }

    public void setLabel(int label) {
        this.label = label;
    }

    public Grid getNearestNeighbor() {
        return nearestNeighbor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Grid grid = (Grid) o;

        return Arrays.equals(vector, grid.vector);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(vector);
    }

    public double calDistance(Grid grid) {
        double distance = 0.0;
        for (int i = 0; i < dim; i++) {
            distance += Math.pow(this.centroid.getAttr()[i] - grid.centroid.getAttr()[i], 2);
        }
        return Math.sqrt(distance);
    }

    public double calDoubleDistance(Grid grid) {
        double doubleDistance = 0.0;
        for (int i = 0; i < dim; i++) {
            doubleDistance += Math.pow(this.centroid.getAttr()[i] - grid.centroid.getAttr()[i], 2);
        }
        return doubleDistance;
    }

    public void setCentroid(Point centroid) {
        this.centroid = centroid;
    }

    public void setDensity(double density) {
        this.density = density;
    }

    public void setDelta(double delta) {
        this.delta = delta;
    }

    public void setNearestNeighbor(Grid nearestNeighbor) {
        this.nearestNeighbor = nearestNeighbor;
    }

    @Override
    public String toString() {
        return "Grid{" +
                "centroid=" + Arrays.toString(centroid.getAttr()) +
                ", vector=" + Arrays.toString(vector) +
                ", density=" + density +
                ", delta=" + delta +
                ", label=" + label +
                '}';
    }
}



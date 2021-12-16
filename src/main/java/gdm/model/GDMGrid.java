package gdm.model;

import common.model.Grid;
import common.model.Point;

public class GDMGrid extends Grid {

    private static final long serialVersionUID = -4612747013329737360L;
    private double delta;
    private GDMGrid nearestNeighbor;
    private double centerDistance;


    public GDMGrid(int[] vector, double lambda, Point point) {
        super(vector, lambda, point);
        this.centerDistance = Double.MAX_VALUE;
        this.delta = Double.MAX_VALUE;
    }

    public double getDelta() {
        return delta;
    }

    public void setDelta(double delta) {
        this.delta = delta;
    }

    public GDMGrid getNearestNeighbor() {
        return nearestNeighbor;
    }

    public void setNearestNeighbor(GDMGrid nearestNeighbor) {
        this.nearestNeighbor = nearestNeighbor;
    }

    public double getCenterDistance() {
        return centerDistance;
    }

    public void setCenterDistance(double centerDistance) {
        this.centerDistance = centerDistance;
    }

    @Override
    public String toString() {
        return "GDMGrid{" +
                "centroid=" + centroid +
                ", delta=" + delta +
                ", density=" + density +
                ", label=" + label +
                '}';
    }
}

package gdm.model;

import common.model.Grid;
import common.model.Point;

import java.util.Arrays;

public class GDMGrid extends Grid {

    private static final long serialVersionUID = -4612747013329737360L;
    private int partition;
    private double delta;
    private GDMGrid nearestNeighbor;
    private double centerDistance;


    public GDMGrid(int[] vector, double lambda, Point point) {
        super(vector, lambda, point);
        this.centerDistance = Double.MAX_VALUE;
        this.delta = Double.MAX_VALUE;
    }

    public GDMGrid(int parallelism, int[] vector, double lambda, Point point) {
        super(vector, lambda, point);
        this.centerDistance = Double.MAX_VALUE;
        this.delta = Double.MAX_VALUE;
        setPartition(parallelism, point);
    }

    public int getPartition() {
        return partition;
    }

    public void setPartition(int parallelism, Point point) {
        if (parallelism == 2) {
            double step_y = 0.5;
            if (point.getAttr()[1] >= step_y)
                partition = 1;
            else
                partition = 0;
        } else if (parallelism == 3) {
            double step_x = 0.5;
            double step_y = 0.5;
            if (point.getAttr()[1] < step_y) {
                partition = 0;
            }else if (point.getAttr()[0] < step_x)
                partition = 1;
            else
                partition = 2;
        } else if (parallelism == 4) {
            double step_x = 0.5;
            double step_y = 0.5;
            if (point.getAttr()[1] < step_y) {
                if (point.getAttr()[0] < step_x)
                    partition = 0;
                else
                    partition = 1;
            }else {
                if (point.getAttr()[0] < step_x)
                    partition = 2;
                else
                    partition = 3;
            }
        } else if (parallelism == 5) {
            double step_x = 0.3;
            double step_y = 0.5;
            if (point.getAttr()[1] < step_y) {
                if (point.getAttr()[0] < step_x)
                    partition = 0;
                else
                    partition = 1;
            }else {
                if (point.getAttr()[0] < step_x)
                    partition = 2;
                else if (point.getAttr()[0] < step_x*2)
                    partition = 3;
                else
                    partition = 4;
            }

        } else {
            double step_x = 0.3;
            double step_y = 0.5;
            if (point.getAttr()[1] < step_y) {
                if (point.getAttr()[0] < step_x) {
                    partition = 0;
                } else if (point.getAttr()[0] < step_x * 2) {
                    partition = 1;
                }else
                    partition = 2;
            }else {
                if (point.getAttr()[0] < step_x) {
                    partition = 3;
                } else if (point.getAttr()[0] < step_x * 2) {
                    partition = 4;
                }else
                    partition = 5;
            }
        }
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
                "vector=" + Arrays.toString(vector) +
                "centroid=" + centroid +
                ", delta=" + delta +
                ", density=" + density +
                ", label=" + label +
                '}';
    }
}

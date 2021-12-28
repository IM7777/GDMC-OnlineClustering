package common.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Grid implements Serializable {
    private static final long serialVersionUID = 1365063496956976255L;
    // 网格向量，类似于id
    public int[] vector;
    // 密度
    public double density;
    // 最近一次的更新时间
    public int updateTime;
    // 衰减因子
    public double lambda;
    // 质心
    public Point centroid;
    // 所属标签
    public int label;


    public Grid(int[] vector, double lambda, Point point) {
        this.vector = vector;
        this.lambda = lambda;
        this.density = 1.0;
        this.label = -1;
        this.centroid = point;
        this.updateTime = point.getId();
    }

    public Grid(int[] vector) {
        this.vector = vector;
    }

    public void updateGrid(Point point) {
        updateDensity(point.getId());
        updateCentroid(point);
        density += 1;
    }

    private void updateCentroid(Point point) {
        int dim = point.getDim();
        for (int i = 0; i < dim; i++) {
            centroid.getAttr()[i] = (point.getAttr()[i] + density * centroid.getAttr()[i]) / (1 + density);
        }
    }

    //欧几里得距离
    public double calDistance(Grid grid) {
        double distance = 0.0;
        int dim = vector.length;
        for (int i = 0; i < dim; i++) {
            distance += Math.pow(centroid.getAttr()[i] - grid.centroid.getAttr()[i], 2);
        }
        return Math.sqrt(distance);
    }

    public double calDoubleDistance(Grid grid) {
        double doubleDistance = 0.0;
        int dim = vector.length;
        for (int i = 0; i < dim; i++) {
            doubleDistance += Math.pow(centroid.getAttr()[i] - grid.centroid.getAttr()[i], 2);
        }
        return doubleDistance;
    }

    public Point getCentroid() {
        return centroid;
    }


    public void updateDensity(int currentTime) {
        if (currentTime > updateTime) {
            density = Math.pow(lambda, (currentTime - updateTime))*density;
            updateTime = currentTime;
        }
    }

    public double getDensity() {
        return density;
    }

    public int getLabel() {
        return label;
    }

    public void setLabel(int label) {
        this.label = label;
    }

    public int[] getVector() {
        return vector;
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
}

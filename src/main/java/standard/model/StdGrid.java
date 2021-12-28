package standard.model;

import common.model.Grid;
import common.model.Point;

import java.util.Arrays;
import java.util.HashMap;

public class StdGrid extends Grid {

    private static final long serialVersionUID = -117623122460004950L;

    public StdGrid(int[] vector, double lambda, Point point) {
        super(vector, lambda, point);
        this.label = point.getLabel();
    }

    @Override
    public void updateGrid(Point point) {
        this.label = point.getLabel();
        updateDensity(point.getId());
        density += 1;
    }


    @Override
    public String toString() {
        return "StdGrid{" +
                "density=" + density +
                ", vector=" + Arrays.toString(vector) +
                ", label=" + label +
                '}';
    }
}

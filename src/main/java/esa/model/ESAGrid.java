package esa.model;

import common.model.Grid;
import common.model.Point;

import java.util.ArrayList;

public class ESAGrid extends Grid {

    private static final long serialVersionUID = -1817448441886097415L;

    public ESAGrid(int[] vector, double lambda, Point point) {
        super(vector, lambda, point);
    }

    public ESAGrid(int[] vector) {
        super(vector);
    }

    // 切比雪夫距离
    @Override
    public double calDistance(Grid grid) {
        double distance = Double.MIN_VALUE;
        int dim = this.vector.length;
        for (int i = 0; i < dim; i++) {
            distance = Math.max(distance, Math.abs(grid.centroid.getAttr()[i] - this.centroid.getAttr()[i]));
        }
        return distance;
    }

    public ArrayList<ESAGrid> getNeighbors() {
        ArrayList<ESAGrid> neighbors = new ArrayList<>();
        for (int i = 0; i < vector.length; i++) {
            int[] neighborL = vector.clone();
            int[] neighborR = vector.clone();
            neighborL[i] = vector[i] - 1;
            neighborR[i] = vector[i] + 1;
            neighbors.add(new ESAGrid(neighborL));
            neighbors.add(new ESAGrid(neighborR));
        }
        return neighbors;
    }



    public static void main(String[] args) {
        int[] vec = new int[]{1, 1};
        double[] attr = new double[]{1.2, 1.4};
        Point point = new Point(attr, 1);
        ESAGrid grid = new ESAGrid(vec, 1, point);
        ArrayList<ESAGrid> neighbors = grid.getNeighbors();
        for (ESAGrid neighbor : neighbors) {
            System.out.println(neighbor);
        }
    }

}

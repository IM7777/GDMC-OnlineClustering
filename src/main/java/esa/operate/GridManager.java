package esa.operate;
import common.model.Point;
import esa.model.ESAGrid;

import java.util.ArrayList;

public class GridManager {
    // lower thresholds
    public double Dl;
    // upper thresholds
    public double Du;

    public long gap;

    // decay factor
    private double lambda;
    // grid unit size
    public double len;
    private ArrayList<ESAGrid> grids;


    public GridManager(double lambda, double len) {
        this.lambda = lambda;
        this.len = len;
        this.grids = new ArrayList<>();
    }


    public void map(Point point) {
        int[] vec = point.mapToGrid(len);
        ESAGrid grid = new ESAGrid(vec, lambda, point);
        int index = grids.indexOf(grid);
        if (index == -1) {
            grids.add(grid);
        } else {
            grids.get(index).updateGrid(point);
        }
    }

    public void updateAllGrids(long time) {
        for (ESAGrid grid : grids) {
            grid.updateDensity(time);
            grid.setLabel(-1);
        }
        int Mt = grids.size();
        Dl = 2.0 / 3 * Mt * (1 - lambda);
        double totalDenseDensity = 0.0;
        int denseNum = 0;
        for (ESAGrid grid : grids) {
            if (grid.getDensity() > Dl) {
                totalDenseDensity += grid.getDensity();
                denseNum++;
            }
        }
        Du = totalDenseDensity / denseNum;
        gap = (long) Math.floor(
                Math.log(Math.min(Dl / Du, (1 - Du * Mt * (1 - lambda)) / (1 - Dl * Mt * (1 - lambda))))/Math.log(lambda));
        grids.removeIf(grid -> grid.getDensity() < Dl);
    }

    public ArrayList<ESAGrid> getGrids() {
        return grids;
    }
}

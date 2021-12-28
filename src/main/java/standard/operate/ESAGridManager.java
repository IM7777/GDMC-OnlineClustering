package standard.operate;

import common.model.Point;
import esa.ESA;
import esa.model.ESAGrid;
import esa.operate.GridManager;
import standard.model.StdGrid;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import static common.util.Functions.log;

public class ESAGridManager{
    // lower thresholds
    public double Dl;
    // upper thresholds
    public double Du;

    // decay factor
    public double lambda;
    // grid unit size
    public double len;
    public ArrayList<StdGrid> grids;

    public ESAGridManager(double lambda, double len) {
        this.lambda = lambda;
        this.len = len;
        this.grids = new ArrayList<>();
    }

    public void map(Point point) {
        int[] vec = point.mapToGrid(len);
        StdGrid grid = new StdGrid(vec, lambda, point);
        int index = grids.indexOf(grid);
        if (index == -1) {
            grids.add(grid);
        } else {
            grids.get(index).updateGrid(point);
        }
    }

    public void updateAllGrids(int time) {
        for (StdGrid grid : grids) {
            grid.updateDensity(time);
        }
        int Mt = grids.size();
        Dl = 2.0 / (3 * Mt * (1 - lambda));
        double totalNonSparseWeight = 0.0;
        int nonSparseNum = 0;
        for (StdGrid grid : grids) {
            double density = grid.getDensity();
            if (density > Dl) {
                totalNonSparseWeight += density;
                nonSparseNum++;
            }
        }
        Du = totalNonSparseWeight / nonSparseNum;
        grids.removeIf(grid -> grid.getDensity() < Dl);
    }

    public ArrayList<StdGrid> getGrids() {
        return grids;
    }
}

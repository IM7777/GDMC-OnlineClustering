package standard.operate;

import common.model.Point;
import standard.model.StdGrid;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class GDMCGridManager {
    // 衰减函数
    private double lambda;

    // 网格单位长度
    private double len;

    // 网格密度阈值
    public double Dh;
    public double Dl;
    public ArrayList<StdGrid> grids;

    public GDMCGridManager(double lambda, double len) {
        this.lambda = lambda;
        this.len = len;
        grids = new ArrayList<>();
    }

    public void mapToGrid(Point point) {
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
        double totalDensity = 0.0;
        for (StdGrid grid : grids) {
            grid.updateDensity(time);
            totalDensity += grid.getDensity();
        }
        int Mt = grids.size();
        double Davg = totalDensity / Mt;

        int denseNum=0, sparseNum = 0;
        double totalDense = 0.0, totalSparse = 0.0;
        for (StdGrid grid : grids) {
            double density = grid.getDensity();
            if (density >= Davg) {
                totalDense += density;
                denseNum++;
            } else {
                totalSparse += density;
                sparseNum++;
            }
        }
        Dh = totalDense / denseNum;
        Dl = totalSparse / sparseNum;
        grids.removeIf(grid -> grid.getDensity() < Dl);
    }

    public ArrayList<StdGrid> getGrids() {
        return grids;
    }
}

package gdm.operate;

import common.model.Point;
import gdm.model.GDMGrid;

import java.util.ArrayList;

import static common.util.Functions.log;

public class ParallelGridManager {
    // 网格列表
    private ArrayList<GDMGrid> grids;

    // 并行数
    int parallelism;

    // 衰减函数
    private double lambda;

    // 网格单位长度
    private double len;

    // 网格密度平均值
    public double avg;
    // 网格密度阈值
    public double Dh;
    public double Dl;
    // 检测间隔时间
    public int gap;

    public ParallelGridManager(int parallelism, double lambda, double len) {
        this.parallelism = parallelism;
        this.lambda = lambda;
        this.len = len;
        this.grids = new ArrayList<>();
        this.Dh = 0.0;
        this.Dl = 0.0;
        this.avg = 0.0;
    }

    public void mapToGrid(Point point) {
        int[] vector = new int[2];
        vector = point.mapToGrid(len);
        GDMGrid grid = new GDMGrid(vector, lambda, point);
        int index = grids.indexOf(grid);
        if (index == -1) {
            grids.add(grid);
        } else{
            grids.get(index).updateGrid(point);
        }
    }

    public void updateGrids(int time) {
        double totalDensity = 0.0;
        for (GDMGrid grid : grids) {
            grid.updateDensity(time);
            totalDensity += grid.getDensity();
        }
        avg = totalDensity / grids.size();
        int denseNum=0, sparseNum = 0;
        double totalDense = 0.0, totalSparse = 0.0;
        ArrayList<GDMGrid> sparseGrids = new ArrayList<>();
        for (GDMGrid grid : grids) {
            double curDensity = grid.getDensity();
            if (curDensity >= avg) {
                totalDense += curDensity;
                denseNum++;
            } else {
                totalSparse += curDensity;
                sparseNum++;
            }
        }
        Dh = totalDense / denseNum;
        Dl = totalSparse / sparseNum;
        int Mt = grids.size();
        gap = (int) Math.floor(log(lambda, Math.min(Dl / Dh, (1 - Dh * Mt * (1 - lambda)) / (1 - Dl * Mt * (1 - lambda)))));
        gap = Math.max(gap, 1);
        grids.removeIf(grid -> grid.getDensity() < Dl);
    }

    public void mapToGrid(Point point, ArrayList<GDMGrid> centers) {
        int[] vector = new int[2];
        vector = point.mapToGrid(len);
        GDMGrid grid = new GDMGrid(vector, lambda, point);
        int index = grids.indexOf(grid);
        // 如果是新增网格，计算距离最近的聚类中心，并将其分配给它
        if (index == -1) {
            double minDistance = Double.MAX_VALUE;
            GDMGrid nearestCenter = null;
            for (GDMGrid center : centers) {
                double curDistance = grid.calDistance(center);
                if (curDistance < minDistance) {
                    nearestCenter = center;
                    minDistance = curDistance;
                }
            }
            if (nearestCenter != null) {
                grid.setCenterDistance(minDistance);
                grid.setLabel(nearestCenter.getLabel());
            }
            grids.add(grid);
        } else {
            grids.get(index).updateGrid(point);
        }
    }

    public void gridsInfo() {
        for (GDMGrid grid : grids) {
            System.out.println(grid);
        }
    }

    public ArrayList<GDMGrid> getGrids() {
        return grids;
    }

}

package GDMC.operate;

import GDMC.model.Cluster;
import GDMC.model.Grid;
import GDMC.model.Point;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by jxm on 2021/7/21.
 */
public class GridManager {
    // 网格列表
    private ArrayList<Grid> grids;

    // 衰减函数
    private double lambda;

    // 网格单位长度
    private double len;

    public GridManager(double lambda, double len) {
        this.lambda = lambda;
        this.len = len;
        this.grids = new ArrayList<>();
    }

    public void mapToGrid(Point point) {
        int[] vector = new int[2];
        vector = point.mapToGrid(len);
        Grid grid = new Grid(lambda, point, vector);
        int index = grids.indexOf(grid);
        if (index == -1) {
            grids.add(grid);
        } else{
            grids.get(index).updateGrid(point);
        }
    }

    public void mapToGrid(Point point, ArrayList<Grid> centers) {
        int[] vector = new int[2];
        vector = point.mapToGrid(len);
        Grid grid = new Grid(lambda, point, vector);
        int index = grids.indexOf(grid);
        // 如果是新增网格，计算距离最近的聚类中心，并将其分配给它
        if (index == -1) {
            double minDistance = Double.MAX_VALUE;
            Grid nearestCenter = null;
            for (Grid center : centers) {
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
        int count = 0;
        for (int i = 0; i < grids.size(); i++) {
            Grid grid = grids.get(i);
            System.out.println(grid);
            count += grid.getDensity();
        }
        System.out.println("包含点数：" + count);
    }

    public ArrayList<Grid> getGrids() {
        return grids;
    }
}

package GDMC.operate;

import GDMC.model.Grid;
import GDMC.model.Point;

import java.util.ArrayList;

/**
 * Created by jxm on 2021/7/21.
 */
public class GridManager {
    private ArrayList<Grid> grids;
    private double lambda;
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

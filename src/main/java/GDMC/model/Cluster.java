package GDMC.model;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by jxm on 2021/7/22.
 */
public class Cluster {
    private int label;
    private ArrayList<Grid> grids;
    private Grid center;

    public Cluster(int label, Grid center) {
        this.label = label;
        this.center = center;
        grids = new ArrayList<>();
        grids.add(center);
    }

    public void addGrid(Grid grid) {
        grids.add(grid);
    }

    public ArrayList<Grid> getGrids() {
        return grids;
    }
}

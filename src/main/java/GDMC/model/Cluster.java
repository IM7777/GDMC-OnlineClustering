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

    public Cluster() {
        this.label = -1;
        grids = new ArrayList<>();
    }

    public void copy(Cluster cluster) {
        this.label = cluster.getLabel();
        this.grids = cluster.getGrids();
        this.center = cluster.getCenter();

    }

    public void merge(Cluster cluster) {
        this.grids.addAll(cluster.getGrids());
    }

    public void addGrid(Grid grid) {
        grids.add(grid);
    }

    public ArrayList<Grid> getGrids() {
        return grids;
    }

    public int getLabel() {
        return label;
    }

    public Grid getCenter() {
        return center;
    }

    public boolean isEmpty() {
        if (this.grids.size() == 0) {
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cluster)) return false;

        Cluster cluster = (Cluster) o;

        return grids.equals(cluster.grids);
    }

    @Override
    public int hashCode() {
        return grids.hashCode();
    }
}

package common.model;


import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by jxm on 2021/7/22.
public class OldCluster implements Serializable {

    private static final long serialVersionUID = -2143986170675141982L;
    private int label;
    private ArrayList<Grid> grids;
    private Grid center;

    public OldCluster(int label, Grid center) {
        this.label = label;
        this.center = center;
        grids = new ArrayList<>();
        grids.add(center);
    }

    public OldCluster() {
        this.label = -1;
        grids = new ArrayList<>();
    }

    public void copy(OldCluster cluster) {
        this.label = cluster.getLabel();
        this.grids = cluster.getGrids();
        this.center = cluster.getCenter();

    }

    public void merge(OldCluster cluster) {
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

    public double getDensity() {
        double density = 0.0;
        for (Grid grid : grids) {
            density += grid.getDensity();
        }
        return density;
    }

    public double getStandardDeviation() {
        double density = getDensity();
        double sum = 0.0;
        for (Grid grid : grids) {
            sum += grid.calDoubleDistance(center);
        }
        return Math.sqrt(sum * 1.0 / density);
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OldCluster)) return false;

        OldCluster cluster = (OldCluster) o;

        return grids.equals(cluster.grids);
    }

    @Override
    public int hashCode() {
        return grids.hashCode();
    }
}
*/
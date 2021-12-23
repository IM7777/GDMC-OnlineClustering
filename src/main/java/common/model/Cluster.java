package common.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class Cluster<T> implements Serializable {

    private static final long serialVersionUID = -1192800104757420547L;
    public int label;
    public ArrayList<T> grids;

    public Cluster(int label) {
        this.label = label;
        grids = new ArrayList<>();
    }

    public void addGrid(T grid) {
        grids.add(grid);
    }

    public boolean isEmpty() {
        return grids.size() == 0;
    }

    public int getLabel() {
        return label;
    }

    public void setLabel(int label) {
        this.label = label;
    }

    public ArrayList<T> getGrids() {
        return grids;
    }

    public void merge(Cluster<T> cluster) {
        grids.addAll(cluster.getGrids());
    }

}

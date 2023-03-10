package esa.model;

import common.model.Cluster;

public class ESACluster extends Cluster<ESAGrid> {

    private static final long serialVersionUID = 6701562292507175146L;

    public ESACluster(int label) {
        super(label);
    }


    public void merge(ESACluster tempCluster) {
        for (ESAGrid tempGrid : tempCluster.getGrids()) {
            tempGrid.setLabel(label);
            grids.add(tempGrid);
        }
    }


}

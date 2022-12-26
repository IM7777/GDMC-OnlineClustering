package gdm.model;

import common.model.Cluster;

public class GDMCluster extends Cluster<GDMGrid> {

    private static final long serialVersionUID = -3749885220758032045L;
    private GDMGrid center;

    public GDMCluster(int label) {
        super(label);
    }

    public GDMCluster(int label, GDMGrid center) {
        super(label);
        this.center = center;
        grids.add(center);
    }

    public double getDensity() {
        double totalDensity = 0.0;
        for (GDMGrid grid : grids) {
            totalDensity += grid.getDensity();
        }
        return totalDensity;
    }

    public GDMGrid getCenter() {
        return center;
    }

    public double getStandardDeviation() {
        double density = getDensity();
        double sum = 0.0;
        for (GDMGrid grid : grids) {
            sum += grid.calDoubleDistance(center);
        }
        return Math.sqrt(sum * 1.0 / density);
    }
}

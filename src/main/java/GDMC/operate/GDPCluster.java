package GDMC.operate;

import GDMC.model.Cluster;
import GDMC.model.Grid;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jxm on 2021/7/17.
 */
public class GDPCluster {
    private ArrayList<Grid> grids;
    private double rhoThreshold;
    private double deltaThreshold;
    private ArrayList<Grid> centers;
    private Map<Integer, Cluster> clusters;

    public GDPCluster(ArrayList<Grid> grids, double rhoThreshold, double deltaThreshold) {
        this.grids = grids;
        this.rhoThreshold = rhoThreshold;
        this.deltaThreshold = deltaThreshold;
        this.centers = new ArrayList<>();
    }

    public void calDelta(){
        grids.sort(new Comparator<Grid>() {
            @Override
            public int compare(Grid o1, Grid o2) {
                return (int) (o2.getDensity() - o1.getDensity());
            }
        });
        double maxDistance = Double.MIN_VALUE;
        Grid peakGrid = grids.get(0);
        for (int i = 1; i < grids.size();i++) {
            Grid curGrid = grids.get(i);
            double minDistance = curGrid.calDistance(peakGrid);
            Grid nearestNeighbor = peakGrid;
            for (int j = 1; j < i; j++) {
                Grid tempGrid = grids.get(j);
                if (curGrid.getDensity() == tempGrid.getDensity())
                    break;
                double distance = curGrid.calDistance(tempGrid);
                if (distance < minDistance) {
                    minDistance = distance;
                    nearestNeighbor = tempGrid;
                }
                if (distance > maxDistance)
                    maxDistance = distance;
            }
            curGrid.setDelta(minDistance);
            curGrid.setNearestNeighbor(nearestNeighbor);
        }
        peakGrid.setDelta(maxDistance);
    }

    public void findCenters() {
        int lable = 0;
        for (int i = 0; i<grids.size();i++) {
            Grid curGrid = grids.get(i);
            if (curGrid.getDensity() >= rhoThreshold) {
                if (curGrid.getDelta() >= deltaThreshold) {
                    centers.add(curGrid);
                    curGrid.setLabel(lable);
                    lable++;
                }
            }
            else
                break;
        }
    }

    public void assignLabel() {
        for (int i = 0; i < grids.size(); i++) {
            Grid curGrid = grids.get(i);
            if (!centers.contains(curGrid)) {
                Grid nearestNeighbor = curGrid.getNearestNeighbor();
                curGrid.setLabel(nearestNeighbor.getLabel());
            }
        }
    }

    public void info() {
        System.out.println("聚类中心");
        for(int i=0;i<centers.size();i++) {
            System.out.println(centers.get(i));
        }
        System.out.println("聚类结果");
        for (int i = 0; i< grids.size();i++) {
            System.out.println(grids.get(i));
        }
    }

    public Map<Integer, Cluster> getClusters() {
        clusters = new HashMap<>();
        for (Grid center : centers) {
            Cluster cluster = new Cluster(center.getLabel(), center);
            clusters.put(center.getLabel(), cluster);
        }
        for (Grid grid : grids) {
            if (!centers.contains(grid)) {
                Cluster cluster = clusters.get(grid.getLabel());
                cluster.addGrid(grid);
            }
        }
        return clusters;
    }
}

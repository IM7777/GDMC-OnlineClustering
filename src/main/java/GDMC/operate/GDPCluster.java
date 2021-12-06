package GDMC.operate;

import GDMC.model.Cluster;
import GDMC.model.Grid;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jxm on 2021/7/17.
 */
public class GDPCluster implements Serializable {
    private ArrayList<Grid> grids;
    private double deltaThreshold;
    //对应的下标就是中心的聚类标签
    private ArrayList<Grid> centers;
    private double Dh;
    private double Dl;


    public GDPCluster(ArrayList<Grid> grids, double deltaThreshold) {
        this.grids = grids;
        this.deltaThreshold = deltaThreshold;
    }

    private void updateGridsDensity(long time) {
        double totalDensity = 0.0;
        for (Grid grid : grids) {
            totalDensity += grid.getUpdateDensity(time);
        }
        double averageDensity = totalDensity / grids.size();
        int denseNum=0, sparseNum = 0;
        double totalDense = 0.0, totalSparse = 0.0;
        for (Grid grid : grids) {
            double curDensity = grid.getDensity();
            if (curDensity >= averageDensity) {
                totalDense += curDensity;
                denseNum++;
            } else {
                totalSparse += curDensity;
                sparseNum++;
            }
        }
        Dh = totalDense / denseNum;
        Dl = totalSparse / sparseNum;
    }

    private void calDelta() {
        grids.sort(new Comparator<Grid>() {
            @Override
            public int compare(Grid o1, Grid o2) {
                return Double.compare(o2.getDensity(), o1.getDensity());
            }
        });
        double maxDistance = Double.MIN_VALUE;
        Grid peakGrid = grids.get(0);
        for (int i = 1; i < grids.size(); i++) {
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

    private void findCenters() {
        this.centers = new ArrayList<>();
        int lable = 0;
        for (Grid curGrid : grids) {
            if (curGrid.getDensity() >= Dh) {
                if (curGrid.getDelta() >= deltaThreshold) {
                    centers.add(curGrid);
                    curGrid.setLabel(lable);
                    lable++;
                }
            } else
                break;
        }
    }

    private void assignLabel() {
        for (Grid curGrid : grids) {
            if (!centers.contains(curGrid)) {
                Grid nearestNeighbor = curGrid.getNearestNeighbor();
                int curLabel = nearestNeighbor.getLabel();
                curGrid.setLabel(curLabel);
                curGrid.setCenterDistance(curGrid.calDistance(centers.get(curLabel)));
            }
        }
    }

    public void info() {
        System.out.println("聚类中心");
        for (Grid center : centers) {
            System.out.println(center);
        }
        System.out.println("聚类结果");
        for (Grid grid : grids) {
            System.out.println(grid);
        }
    }

    public HashMap<Integer, Cluster> getClusters() {
        HashMap<Integer, Cluster> clusters = new HashMap<>();
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

    public ArrayList<Grid> getCenters() {
        return centers;
    }

    public void process(long time) {
        updateGridsDensity(time);
        calDelta();
        findCenters();
        assignLabel();
    }
}

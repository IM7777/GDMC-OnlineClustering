package gdm.operate;

import gdm.model.GDMCluster;
import gdm.model.GDMGrid;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Created by jxm on 2021/7/17.
 */
public class GDPCluster {
    private ArrayList<GDMGrid> grids;
    private double deltaThreshold;
    //对应的下标就是中心的聚类标签
    private ArrayList<GDMGrid> centers;

    public GDPCluster(ArrayList<GDMGrid> grids, double deltaThreshold) {
        this.grids = grids;
        this.deltaThreshold = deltaThreshold;
    }


    private void calDelta() {
        grids.sort(new Comparator<GDMGrid>() {
            @Override
            public int compare(GDMGrid o1, GDMGrid o2) {
                return Double.compare(o2.getDensity(), o1.getDensity());
            }
        });
        double maxDistance = Double.MIN_VALUE;
        GDMGrid peakGrid = grids.get(0);
        for (int i = 1; i < grids.size(); i++) {
            GDMGrid curGrid = grids.get(i);
            double minDistance = curGrid.calDistance(peakGrid);
            GDMGrid nearestNeighbor = peakGrid;
            for (int j = 1; j < i; j++) {
                GDMGrid tempGrid = grids.get(j);
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

    private void findCenters(double Dh) {
        this.centers = new ArrayList<>();
        int lable = 0;
        for (GDMGrid curGrid : grids) {
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
        for (GDMGrid curGrid : grids) {
            if (!centers.contains(curGrid)) {
                GDMGrid nearestNeighbor = curGrid.getNearestNeighbor();
                int curLabel = nearestNeighbor.getLabel();
                curGrid.setLabel(curLabel);
                curGrid.setCenterDistance(curGrid.calDistance(centers.get(curLabel)));
            }
        }
    }

    public void info() {
        System.out.println("聚类中心");
        for (GDMGrid center : centers) {
            System.out.println(center);
        }
        System.out.println("聚类结果");
        for (GDMGrid grid : grids) {
            System.out.println(grid);
        }
    }

    public HashMap<Integer, GDMCluster> getClusters() {
        HashMap<Integer, GDMCluster> clusters = new HashMap<>();
        for (GDMGrid center : centers) {
            GDMCluster cluster = new GDMCluster(center.getLabel(), center);
            clusters.put(center.getLabel(), cluster);
        }
        for (GDMGrid grid : grids) {
            if (!centers.contains(grid)) {
                GDMCluster cluster = clusters.get(grid.getLabel());
                cluster.addGrid(grid);
            }
        }
        return clusters;
    }

    public ArrayList<GDMGrid> getCenters() {
        return centers;
    }


    public void process(long time, double Dh, double Dl) {
        //updateGridsDensity(time);
        calDelta();
        findCenters(Dh);
        assignLabel();
    }
}

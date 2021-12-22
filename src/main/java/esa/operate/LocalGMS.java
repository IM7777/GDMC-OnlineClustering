package esa.operate;

import esa.model.ESACluster;
import esa.model.ESAGrid;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class LocalGMS extends Thread {
    private ArrayList<ESAGrid> grids;
    private ArrayList<Double> borders;
    private int partitionId;
    private ConcurrentHashMap<Integer, ArrayList<ESAGrid>> globalGrids;
    private CountDownLatch countDownLatch;
    private double Du;
    private double Dl;
    private double len;

    public LocalGMS(ArrayList<ESAGrid> grids, ArrayList<Double> borders, int partitionId,
                    ConcurrentHashMap<Integer, ArrayList<ESAGrid>> globalGrids,
                    CountDownLatch countDownLatch, double Du, double Dl, double len) {
        this.grids = grids;
        this.borders = borders;
        this.partitionId = partitionId;
        this.globalGrids = globalGrids;
        this.countDownLatch = countDownLatch;
        this.Du = Du;
        this.Dl = Dl;
        this.len = len;
    }

    @Override
    public void run() {
        int label = partitionId * 1000;
        for (ESAGrid grid : grids) {
            if (grid.getDensity() >= Du && grid.getLabel() == -1) {
                grid.setLabel(label);
                dfs(grid, label);
                label++;
            }
        }
        ArrayList<ESAGrid> globalGridsList = globalGrids.get(partitionId);
        for (ESAGrid grid : grids) {
            double minBorderDistance = getMinBorderDistance(grid);
            if (minBorderDistance < len) {
                globalGridsList.add(grid);
            }
        }
        countDownLatch.countDown();
    }

    public void dfs(ESAGrid grid, int label) {
        ArrayList<ESAGrid> neighbors = grid.getNeighbors();
        for (ESAGrid neighbor : neighbors) {
            int index = grids.indexOf(neighbor);
            if (index != -1) {
                neighbor = grids.get(index);
                if (neighbor.getLabel() == -1 && isMerge(grid, neighbor)) {
                    neighbor.setLabel(label);
                    dfs(neighbor, label);
                }
            }
        }
    }

    private boolean isMerge(ESAGrid g1, ESAGrid g2) {
        double den1 = g1.getDensity();
        double den2 = g2.getDensity();
        double distance = g1.calDistance(g2);
        if (den1 >= Du && den2 >= Du && distance <= 4.0 / 3 * len)
            return true;
        else if (distance <= len) {
            if (den1 >= Du && den2 >= Dl)
                return true;
            else if (den1 >= Dl && den2 >= Du)
                return true;
        }
        else if (distance <= 2.0 / 3 * len && den1 >= Dl && den2 >= Dl && den1 + den2 >= Du)
            return true;
        return false;
    }

    public double getMinBorderDistance(ESAGrid grid) {
        double minBorderDistance = Double.MAX_VALUE;
        double distance = 0;
        for (int i = 0; i < borders.size(); i++) {
            if (i < 2) {
                distance = Math.abs(grid.centroid.getAttr()[0] - borders.get(i));
            } else {
                distance = Math.abs(grid.centroid.getAttr()[1] - borders.get(i));
            }
            minBorderDistance = Math.min(minBorderDistance, distance);
        }
        return minBorderDistance;
    }
}

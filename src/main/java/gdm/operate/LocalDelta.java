package gdm.operate;

import gdm.model.GDMGrid;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

class LocalDelta extends Thread {
    ArrayList<GDMGrid> grids;
    ArrayList<Double> borders;
    ConcurrentHashMap<Integer, ArrayList<GDMGrid>> globalGrids;
    int threadId;
    CountDownLatch countDownLatch;

    public LocalDelta(ArrayList<GDMGrid> grids, ArrayList<Double> borders,
                      ConcurrentHashMap<Integer, ArrayList<GDMGrid>> globalGrids,
                      int nodeId, CountDownLatch countDownLatch) {
        this.grids = grids;
        this.borders = borders;
        this.globalGrids = globalGrids;
        this.threadId = nodeId;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void run() {
        if (!grids.isEmpty()) {
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
                }
                curGrid.setDelta(minDistance);
                curGrid.setNearestNeighbor(nearestNeighbor);
            }
            peakGrid.setDelta(Double.MAX_VALUE);
            ArrayList<GDMGrid> globalGridsList = globalGrids.get(threadId);
            for (GDMGrid grid : grids) {
                double minBorderDistance = getMinBorderDistance(grid);
                if (minBorderDistance < grid.getDelta()) {
                    globalGridsList.add(grid);
                }
            }
        }
        countDownLatch.countDown();
    }


    public double getMinBorderDistance(GDMGrid grid) {
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

package esa.operate;

import common.model.Point;
import common.operate.PointManager;
import esa.model.ESACluster;
import esa.model.ESAGrid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ECSCluster {
    private ArrayList<ESAGrid> grids;
    private double Du;
    private double Dl;
    private double len;

    public ECSCluster(ArrayList<ESAGrid> grids, double len) {
        this.grids = grids;
        this.len = len;
    }

    public void process(double du, double dl) {
        Du = du;
        Dl = dl;
        int label = 0;
        for (ESAGrid grid : grids) {
            if (grid.getDensity() >= Du && grid.getLabel() == -1) {
                grid.setLabel(label);
                dfs(grid, label);
                label++;
            }
        }
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


    public HashMap<Integer, ESACluster> getClusters() {
        HashMap<Integer, ESACluster> clusters = new HashMap<>();
        for (ESAGrid grid : grids) {
            int label = grid.getLabel();
            if (clusters.containsKey(label)) {
                ESACluster cluster = clusters.get(label);
                cluster.addGrid(grid);
            }else{
                ESACluster newCluster = new ESACluster(label);
                newCluster.addGrid(grid);
                clusters.put(label, newCluster);
            }
        }
        return clusters;
    }

    public static void main(String[] args) throws IOException {
        String filePath = "C:\\Users\\Celeste\\Desktop\\data\\overview.txt";
        PointManager pointManager = new PointManager();
        pointManager.readPointsWithLabel(filePath);

        ArrayList<Point> points = pointManager.getPoints();

        GridManager gridManager = new GridManager(0.999, 0.1);

        for (Point point : points)
            gridManager.map(point);
        gridManager.updateAllGrids(3000);

        ArrayList<ESAGrid> grids = gridManager.getGrids();
        ECSCluster ESAClusteirng = new ECSCluster(grids, gridManager.len);
        long st = System.currentTimeMillis();
        ESAClusteirng.process(gridManager.Du/2, gridManager.Dl);
        long ed = System.currentTimeMillis();
        ResultViewer resultViewer = new ResultViewer();
        //resultViewer.showChart(ESAClusteirng.getClusters());
        System.out.println("ECS:" + (ed - st));


    }

}

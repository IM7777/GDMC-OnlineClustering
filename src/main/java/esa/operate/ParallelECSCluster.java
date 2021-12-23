package esa.operate;

import common.model.Point;
import common.operate.PointManager;
import esa.model.ESACluster;
import esa.model.ESAGrid;
import sun.misc.ASCIICaseInsensitiveComparator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class ParallelECSCluster {
    private double len;
    private ArrayList<ESAGrid> grids;
    private ConcurrentHashMap<Integer, ArrayList<ESAGrid>> localGrids;
    private ConcurrentHashMap<Integer, ArrayList<Double>> localBorders;
    private ConcurrentHashMap<Integer, ArrayList<ESAGrid>> globalGrids;
    /* key: partitionId
       value: {key: label, value: cluster}
     */
    private ConcurrentHashMap<Integer, ESACluster> globalClusters;
    private int parallelism;
    private double Du;
    private double Dl;

    public ParallelECSCluster(double len, ArrayList<ESAGrid> grids, int parallelism) {
        this.len = len;
        this.grids = grids;
        this.parallelism = parallelism;
        partition();
    }

    private void partition() {
        localBorders = new ConcurrentHashMap<>();
        localGrids = new ConcurrentHashMap<>();
        globalClusters = new ConcurrentHashMap<>();
        globalGrids = new ConcurrentHashMap<>();

        if (parallelism == 2) {
            double step_y = 5.0;
            for (int i = 0; i < parallelism; i++) {
                ArrayList<Double> border = new ArrayList<>();
                border.add(0.0);
                border.add(10.0);
                border.add(i * step_y);
                border.add((i + 1) * step_y);
                localBorders.put(i, border);
                localGrids.put(i, new ArrayList<>());
                globalGrids.put(i, new ArrayList<>());
            }

            for (ESAGrid grid : grids) {
                if (grid.centroid.getAttr()[1] >= 5) {
                    ArrayList<ESAGrid> lgrids = localGrids.get(1);
                    lgrids.add(grid);
                } else {
                    ArrayList<ESAGrid> lgrids = localGrids.get(0);
                    lgrids.add(grid);
                }
            }
        } else if (parallelism == 3) {
            double step_x = 5.0;
            for (int i = 0; i < parallelism; i++) {
                ArrayList<Double> border = new ArrayList<>();
                if (i == 0) {
                    border.add(0.0);
                    border.add(10.0);
                    border.add(0.0);
                    border.add(5.0);
                } else {
                    border.add((i - 1) * step_x);
                    border.add(i * step_x);
                    border.add(5.0);
                    border.add(10.0);
                }
                localBorders.put(i, border);
                localGrids.put(i, new ArrayList<>());
                globalGrids.put(i, new ArrayList<>());
            }

            for (ESAGrid grid : grids) {
                if (grid.centroid.getAttr()[1] < 5) {
                    ArrayList<ESAGrid> lgrids = localGrids.get(0);
                    lgrids.add(grid);
                } else if (grid.centroid.getAttr()[0] < 5) {
                    ArrayList<ESAGrid> lgrids = localGrids.get(1);
                    lgrids.add(grid);
                } else {
                    ArrayList<ESAGrid> lgrids = localGrids.get(2);
                    lgrids.add(grid);
                }
            }

        } else if (parallelism == 4) {
            double step_x = 5.0;
            double step_y = 5.0;
            for (int i = 0; i < parallelism; i++) {
                ArrayList<Double> border = new ArrayList<>();
                if (i <= 1) {
                    border.add(i * step_x);
                    border.add((i + 1) * step_x);
                    border.add(0.0);
                    border.add(5.0);
                } else {
                    border.add((i - 2) * step_x);
                    border.add((i - 1) * step_x);
                    border.add(5.0);
                    border.add(10.0);
                }
                localGrids.put(i, new ArrayList<>());
                localBorders.put(i, border);
                globalGrids.put(i, new ArrayList<>());
            }

            for (ESAGrid grid : grids) {
                if (grid.centroid.getAttr()[1] < step_y) {
                    if (grid.centroid.getAttr()[0] < step_x) {
                        ArrayList<ESAGrid> lgrids = localGrids.get(0);
                        lgrids.add(grid);
                    } else {
                        ArrayList<ESAGrid> lgrids = localGrids.get(1);
                        lgrids.add(grid);
                    }
                } else {
                    if (grid.centroid.getAttr()[0] < step_x) {
                        ArrayList<ESAGrid> lgrids = localGrids.get(2);
                        lgrids.add(grid);
                    } else {
                        ArrayList<ESAGrid> lgrids = localGrids.get(3);
                        lgrids.add(grid);
                    }
                }
            }

        } else if (parallelism == 5) {
            double step_x = 3.0;
            double step_y = 5.0;
            for (int i = 0; i < parallelism; i++) {
                ArrayList<Double> border = new ArrayList<>();
                if (i <= 1) {
                    if (i == 0) {
                        border.add(0.0);
                        border.add(step_x);
                    } else {
                        border.add(step_x);
                        border.add(10.0);
                    }
                    border.add(0.0);
                    border.add(5.0);
                } else {
                    border.add((i - 2) * step_x);
                    if (i == 4) {
                        border.add(10.0);
                    } else {
                        border.add((i - 1) * step_x);
                    }
                    border.add(5.0);
                    border.add(10.0);
                }
                localBorders.put(i, border);
                localGrids.put(i, new ArrayList<>());
                globalGrids.put(i, new ArrayList<>());
            }
            for (ESAGrid grid : grids) {
                if (grid.centroid.getAttr()[1] < 5.0) {
                    if (grid.centroid.getAttr()[0] < step_x) {
                        ArrayList<ESAGrid> lgrids = localGrids.get(0);
                        lgrids.add(grid);
                    } else {
                        ArrayList<ESAGrid> lgrids = localGrids.get(1);
                        lgrids.add(grid);
                    }
                } else {
                    if (grid.centroid.getAttr()[0] < step_x) {
                        ArrayList<ESAGrid> lgrids = localGrids.get(2);
                        lgrids.add(grid);
                    } else if (grid.centroid.getAttr()[0] < step_x * 2) {
                        ArrayList<ESAGrid> lgrids = localGrids.get(3);
                        lgrids.add(grid);
                    } else {
                        ArrayList<ESAGrid> lgrids = localGrids.get(4);
                        lgrids.add(grid);
                    }
                }
            }
        } else if (parallelism == 6) {
            double step_x = 3.0;
            for (int i = 0; i < parallelism; i++) {
                ArrayList<Double> border = new ArrayList<>();
                if (i < 3) {
                    border.add(i * step_x);
                    if (i == 2) {
                        border.add(10.0);
                    } else {
                        border.add((i + 1) * step_x);
                    }
                    border.add(0.0);
                    border.add(5.0);
                } else {
                    border.add((i - 3) * step_x);
                    if (i == 5) {
                        border.add(10.0);
                    } else {
                        border.add((i - 2) * step_x);
                    }
                    border.add(5.0);
                    border.add(10.0);
                }
                localGrids.put(i, new ArrayList<>());
                localBorders.put(i, border);
                globalGrids.put(i, new ArrayList<>());
            }
            for (ESAGrid grid : grids) {
                if (grid.centroid.getAttr()[1] < 5) {
                    if (grid.centroid.getAttr()[0] < step_x) {
                        ArrayList<ESAGrid> lgrids = localGrids.get(0);
                        lgrids.add(grid);
                    } else if (grid.centroid.getAttr()[0] < step_x * 2) {
                        ArrayList<ESAGrid> lgrids = localGrids.get(1);
                        lgrids.add(grid);
                    } else {
                        ArrayList<ESAGrid> lgrids = localGrids.get(2);
                        lgrids.add(grid);
                    }
                } else {
                    if (grid.centroid.getAttr()[0] < step_x) {
                        ArrayList<ESAGrid> lgrids = localGrids.get(3);
                        lgrids.add(grid);
                    } else if (grid.centroid.getAttr()[0] < step_x * 2) {
                        ArrayList<ESAGrid> lgrids = localGrids.get(4);
                        lgrids.add(grid);
                    } else {
                        ArrayList<ESAGrid> lgrids = localGrids.get(5);
                        lgrids.add(grid);
                    }
                }
            }
        }
    }

    public void process(double Du, double Dl) throws InterruptedException {
        this.Du = Du;
        this.Dl = Dl;

        // 局部聚类
        LocalGMS[] localGMSThread = new LocalGMS[parallelism];
        CountDownLatch countDownLatch = new CountDownLatch(parallelism);
        for (int i = 0; i < parallelism; i++) {
            localGMSThread[i] = new LocalGMS(localGrids.get(i), localBorders.get(i), globalClusters,
                    i, globalGrids, countDownLatch, Du, Dl, len);
            localGMSThread[i].start();
        }
        countDownLatch.await();

        // 全局聚类
        globalGMS();
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


    private void globalGMS() {
        for (Integer partitionId1 : globalGrids.keySet()) {
            if (!globalGrids.get(partitionId1).isEmpty()) {
                ArrayList<ESAGrid> currentGlobalGridList = globalGrids.get(partitionId1);
                for (ESAGrid grid : currentGlobalGridList) {
                    ArrayList<ESAGrid> neighbors = grid.getNeighbors();
                    int label1 = grid.getLabel();
                    for (ESAGrid neighbor : neighbors) {
                        // 判断该邻居是否在globalGrids中，若在则返回所属的partitionId
                        int partitionId2 = getNeighborPartitionId(neighbor, partitionId1);
                        if (partitionId2 != -1) {
                            // 获取邻居网格在globalGrids的index
                            int neighborIndex = globalGrids.get(partitionId2).indexOf(neighbor);
                            // 获取真正的邻居网格
                            neighbor = globalGrids.get(partitionId2).get(neighborIndex);
                            int label2 = neighbor.getLabel();
                            if (label1 != label2) {
                                ESACluster neighborCluster = globalClusters.get(label2);
                                globalClusters.get(label1).merge(neighborCluster);
                                globalClusters.remove(label2);
                            }
                        }
                    }
                }
            }
        }
    }

    private int getNeighborPartitionId(ESAGrid neighbor, int partitionId1) {
        for (Integer partitionId2 : globalGrids.keySet()) {
            if (partitionId2 != partitionId1 && !globalGrids.get(partitionId2).isEmpty()) {
                ArrayList<ESAGrid> currentGlobalGridList = globalGrids.get(partitionId2);
                if (currentGlobalGridList.contains(neighbor))
                    return partitionId2;
            }
        }
        return -1;
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

    public static void main(String[] args) throws IOException, InterruptedException {
        String filePath = "C:\\Users\\Celeste\\Desktop\\data\\overview.txt";
        PointManager pointManager = new PointManager();
        pointManager.readPointsWithLabel(filePath);

        ArrayList<Point> points = pointManager.getPoints();

        GridManager gridManager = new GridManager(0.999, 0.1);

        for (Point point : points)
            gridManager.map(point);

        gridManager.updateAllGrids(3000);
        ArrayList<ESAGrid> grids = gridManager.getGrids();
        long st = System.currentTimeMillis();
        ParallelECSCluster pesa = new ParallelECSCluster(gridManager.len, grids, 2);

        pesa.process(gridManager.Du/2, gridManager.Dl);
        long ed = System.currentTimeMillis();
        ResultViewer resultViewer = new ResultViewer();
        //resultViewer.showChart(pesa.getClusters());
        System.out.println("ParallelECS:" + (ed - st));
    }

}

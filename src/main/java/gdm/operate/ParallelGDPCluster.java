package gdm.operate;

import common.model.Point;
import common.operate.PointManager;
import gdm.model.GDMCluster;
import gdm.model.GDMGrid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class ParallelGDPCluster {
    private int parallelism;
    private double deltaThreshold;
    private ArrayList<GDMGrid> grids;
    private ArrayList<GDMGrid> centers;
    private ConcurrentHashMap<Integer, ArrayList<GDMGrid>> globalGrids;
    private ConcurrentHashMap<Integer, ArrayList<GDMGrid>> localGrids;
    //每一个ArrayList的大小是2^d, 下界和上界；对于二维数据即是，min_x, max_x, min_y, max_y
    private ConcurrentHashMap<Integer, ArrayList<Double>> localBorders;

    public ParallelGDPCluster(int parallelism, double deltaThreshold, ArrayList<GDMGrid> grids) {
        this.parallelism = parallelism;
        this.deltaThreshold = deltaThreshold;
        this.grids = grids;
        allocateGrids();
    }

    private void allocateGrids() {
        //先将grids进行并行排序
        grids.sort(new Comparator<GDMGrid>() {
            @Override
            public int compare(GDMGrid o1, GDMGrid o2) {
                return Double.compare(o2.getDensity(), o1.getDensity());
            }
        });
        localGrids = new ConcurrentHashMap<>();
        localBorders = new ConcurrentHashMap<>();
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

            for (GDMGrid grid : grids) {
                if (grid.centroid.getAttr()[1] >= 5) {
                    ArrayList<GDMGrid> lgrids = localGrids.get(1);
                    lgrids.add(grid);
                } else {
                    ArrayList<GDMGrid> lgrids = localGrids.get(0);
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

            for (GDMGrid grid : grids) {
                if (grid.centroid.getAttr()[1] < 5) {
                    ArrayList<GDMGrid> lgrids = localGrids.get(0);
                    lgrids.add(grid);
                } else if (grid.centroid.getAttr()[0] < 5) {
                    ArrayList<GDMGrid> lgrids = localGrids.get(1);
                    lgrids.add(grid);
                } else {
                    ArrayList<GDMGrid> lgrids = localGrids.get(2);
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

            for (GDMGrid grid : grids) {
                if (grid.centroid.getAttr()[1] < step_y) {
                    if (grid.centroid.getAttr()[0] < step_x) {
                        ArrayList<GDMGrid> lgrids = localGrids.get(0);
                        lgrids.add(grid);
                    } else {
                        ArrayList<GDMGrid> lgrids = localGrids.get(1);
                        lgrids.add(grid);
                    }
                } else {
                    if (grid.centroid.getAttr()[0] < step_x) {
                        ArrayList<GDMGrid> lgrids = localGrids.get(2);
                        lgrids.add(grid);
                    } else {
                        ArrayList<GDMGrid> lgrids = localGrids.get(3);
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
            for (GDMGrid grid : grids) {
                if (grid.centroid.getAttr()[1] < 5.0) {
                    if (grid.centroid.getAttr()[0] < step_x) {
                        ArrayList<GDMGrid> lgrids = localGrids.get(0);
                        lgrids.add(grid);
                    } else {
                        ArrayList<GDMGrid> lgrids = localGrids.get(1);
                        lgrids.add(grid);
                    }
                } else {
                    if (grid.centroid.getAttr()[0] < step_x) {
                        ArrayList<GDMGrid> lgrids = localGrids.get(2);
                        lgrids.add(grid);
                    } else if (grid.centroid.getAttr()[0] < step_x * 2) {
                        ArrayList<GDMGrid> lgrids = localGrids.get(3);
                        lgrids.add(grid);
                    } else {
                        ArrayList<GDMGrid> lgrids = localGrids.get(4);
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
            for (GDMGrid grid : grids) {
                if (grid.centroid.getAttr()[1] < 5) {
                    if (grid.centroid.getAttr()[0] < step_x) {
                        ArrayList<GDMGrid> lgrids = localGrids.get(0);
                        lgrids.add(grid);
                    } else if (grid.centroid.getAttr()[0] < step_x * 2) {
                        ArrayList<GDMGrid> lgrids = localGrids.get(1);
                        lgrids.add(grid);
                    } else {
                        ArrayList<GDMGrid> lgrids = localGrids.get(2);
                        lgrids.add(grid);
                    }
                } else {
                    if (grid.centroid.getAttr()[0] < step_x) {
                        ArrayList<GDMGrid> lgrids = localGrids.get(3);
                        lgrids.add(grid);
                    } else if (grid.centroid.getAttr()[0] < step_x * 2) {
                        ArrayList<GDMGrid> lgrids = localGrids.get(4);
                        lgrids.add(grid);
                    } else {
                        ArrayList<GDMGrid> lgrids = localGrids.get(5);
                        lgrids.add(grid);
                    }
                }
            }
        }
    }

    public ConcurrentHashMap<Integer, ArrayList<GDMGrid>> getLocalGrids() {
        return localGrids;
    }

    public void calGlobalDelta() {
        for (Integer partitionId1 : globalGrids.keySet()) {
            if (!globalGrids.get(partitionId1).isEmpty()) {
                ArrayList<GDMGrid> currentGlobalGridsList = globalGrids.get(partitionId1);
                for (GDMGrid grid : currentGlobalGridsList) {
                    double minDistance = grid.getDelta();
                    GDMGrid nearestNeighbor = grid.getNearestNeighbor();
                    for (Integer partitionId2 : localGrids.keySet()) {
                        if (partitionId2 != partitionId1 && !localGrids.get(partitionId2).isEmpty()) {
                            ArrayList<GDMGrid> tempLocalGridsList = localGrids.get(partitionId2);
                            for (GDMGrid tempGrid : tempLocalGridsList) {
                                if (tempGrid.getDensity()<= grid.getDensity())
                                    break;
                                double distance = grid.calDistance(tempGrid);
                                if (distance < minDistance) {
                                    minDistance = distance;
                                    nearestNeighbor = tempGrid;
                                }
                            }
                        }
                    }
                    grid.setDelta(minDistance);
                    grid.setNearestNeighbor(nearestNeighbor);
                }
            }
        }
    }


    public void newCalGlobalDelta() {
        for (Integer partitionId1 : globalGrids.keySet()) {
            if (!globalGrids.get(partitionId1).isEmpty()) {
                ArrayList<GDMGrid> currentGlobalGridsList = globalGrids.get(partitionId1);
                for (GDMGrid grid : currentGlobalGridsList) {
                    double minDistance = grid.getDelta();
                    GDMGrid nearestNeighbor = grid.getNearestNeighbor();
                    for (GDMGrid tempGrid : grids) {
                        if (grid.getDensity() == tempGrid.getDensity())
                            break;
                        double distance = grid.calDistance(tempGrid);
                        if (distance < minDistance) {
                            minDistance = distance;
                            nearestNeighbor = tempGrid;
                        }
                    }
                    grid.setDelta(minDistance);
                    grid.setNearestNeighbor(nearestNeighbor);
                }
            }
        }
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

    private void process(double Dh) throws InterruptedException {
        // 并行计算局部最短截距
        LocalDelta[] localDeltaThread = new LocalDelta[parallelism];
        CountDownLatch countDownLatch = new CountDownLatch(parallelism);
        for (int i = 0; i < parallelism; i++) {
            localDeltaThread[i] = new LocalDelta(localGrids.get(i), localBorders.get(i), globalGrids, i, countDownLatch);
            localDeltaThread[i].start();
        }
        countDownLatch.await();
        // 合并计算最短截距
        newCalGlobalDelta();

        // 查找聚类中心
        findCenters(Dh);

        // 分配标签
        assignLabel();
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

    public static void main(String[] args) throws IOException, InterruptedException {
        String filePath = "C:\\Users\\Celeste\\Desktop\\data\\overview.txt";
        PointManager pointManager = new PointManager();
        pointManager.readPointsWithLabel(filePath);

        ArrayList<Point> points = pointManager.getPoints();

        GridManager gridManager = new GridManager(1, 0.1);
        for (Point point : points)
            gridManager.mapToGrid(point);

        ArrayList<GDMGrid> grids = gridManager.getGrids();

        ParallelGDPCluster parallelCluster = new ParallelGDPCluster(3, 2.5, grids);
        long st = System.currentTimeMillis();
        parallelCluster.process(gridManager.Dh);
        long ed = System.currentTimeMillis();
        System.out.println("ParallelGDP:" + (ed - st));
        //聚类结果显示模块
        ResultViewer resultViewer = new ResultViewer();
        //resultViewer.showChart(parallelCluster.getClusters());
    }

}


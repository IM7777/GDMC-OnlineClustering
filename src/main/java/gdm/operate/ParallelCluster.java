package gdm.operate;

import common.model.Point;
import common.operate.PointManager;
import gdm.model.GDMGrid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ParallelCluster {
    private int parallelism;
    private ArrayList<GDMGrid> grids;
    private ArrayList<GDMGrid> centers;
    private HashMap<Integer, ArrayList<GDMGrid>> localGrids;
    //每一个ArrayList的大小是2^d, 下界和上界；对于二维数据即是，min_x, max_x, min_y, max_y
    private HashMap<Integer, ArrayList<Double>> localBorders;

    public ParallelCluster(int parallelism, ArrayList<GDMGrid> grids) {
        this.parallelism = parallelism;
        this.grids = grids;
        allocateGrids();
    }

    private void allocateGrids() {
        localGrids = new HashMap<>();
        localBorders = new HashMap<>();
        if (parallelism == 2) {
            double step_y = 5.0;
            for (int i = 0; i < parallelism; i++) {
                ArrayList<Double> border = new ArrayList<>();
                border.add(0.0);
                border.add(10.0);
                border.add(i * step_y);
                border.add((i + 1) * step_y);
                localBorders.put(i, border);
                localGrids.put(i, new ArrayList<GDMGrid>());
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

    public HashMap<Integer, ArrayList<GDMGrid>> getLocalGrids() {
        return localGrids;
    }

    public static void main(String[] args) throws IOException {
        String filePath = "C:\\Users\\Celeste\\Desktop\\data\\overview.txt";
        PointManager pointManager = new PointManager();
        pointManager.readPointsWithLabel(filePath);

        ArrayList<Point> points = pointManager.getPoints();

        GridManager gridManager = new GridManager(1, 1);
        for (Point point : points)
            gridManager.mapToGrid(point);

        ArrayList<GDMGrid> grids = gridManager.getGrids();

        ParallelCluster parallelCluster = new ParallelCluster(6, grids);
        HashMap<Integer, ArrayList<GDMGrid>> localGrids = parallelCluster.getLocalGrids();
        System.out.println(localGrids.size());

    }
}

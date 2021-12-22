package esa.operate;

import esa.model.ESAGrid;
import sun.misc.ASCIICaseInsensitiveComparator;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ParallelECSCluster {
    private double len;
    private ArrayList<ESAGrid> grids;
    private ConcurrentHashMap<Integer, ArrayList<ESAGrid>> localGrids;
    private ConcurrentHashMap<Integer, ArrayList<Double>> localBorders;
    private ConcurrentHashMap<Integer, ArrayList<ESAGrid>> globalGrids;
    private int parallelism;
    private double Du;
    private double Dl;

    public ParallelECSCluster(double len, ArrayList<ESAGrid> grids, int parallelism) {
        this.len = len;
        this.grids = grids;
        this.parallelism = parallelism;
    }

    private void partition() {
        localBorders = new ConcurrentHashMap<>();
        localGrids = new ConcurrentHashMap<>();
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

    private void process(double Du, double Dl) {
        this.Du = Du;
        this.Dl = Dl;


    }

}

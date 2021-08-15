package GDMC.operate;

import GDMC.model.Cluster;
import GDMC.model.Grid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jxm on 2021/7/23.
 */
public class EvolutionRecognizer {
    private ArrayList<Cluster> latestClusters;
    private ArrayList<Cluster> currentClusters;
    private ArrayList<Cluster> deadList = new ArrayList<>();
    private ArrayList<Cluster> survivalList = new ArrayList<>();
    private HashMap<Cluster, Integer> sizeTransitionList = new HashMap<>();
    private HashMap<Cluster, Integer> compactTransitionList = new HashMap<>();


    private Map<Cluster, ArrayList<Cluster>> splitList = new HashMap<>();
    private Map<Cluster, ArrayList<Cluster>> absorpList = new HashMap<>();

    // match
    private double tau = 0.5;
    private double tauSplit = 0.2;
    // size
    private double varepsilon = 50;
    // compact
    private double delta = 0.5;


    public EvolutionRecognizer(ArrayList<Cluster> latestClusters, ArrayList<Cluster> currentClusters) {
        this.latestClusters = latestClusters;
        this.currentClusters = currentClusters;
    }

    public double overlap(Cluster x, Cluster y) {
        ArrayList<Grid> grids_x = x.getGrids();
        ArrayList<Grid> grids_y = y.getGrids();
        ArrayList<Grid> jointGrids = new ArrayList<>();
        double jointDensity = 0.0;
        double gridsDensity_i = 0.0;
        for (Grid grid_i : grids_x) {
            int index = grids_y.indexOf(grid_i);
            if (index != -1) {
                Grid grid_j = grids_y.get(index);
                jointDensity += grid_j.getDensity();
            }
            gridsDensity_i += grid_i.getDensity();
        }
        return jointDensity / gridsDensity_i;
    }

    public void externalTransition(ArrayList<Cluster> c_i, ArrayList<Cluster> c_j) {
        Map<Cluster, Cluster> absorption_survival = new HashMap<>();
        for (Cluster x : c_i) {
            Cluster splitCandicate = new Cluster();
            ArrayList<Cluster> splitUnion = new ArrayList<>();
            Cluster survivalCandicate = new Cluster();
            double overlap_max = 0;

            for (Cluster y : c_j) {
                double mcell = overlap(x, y);
                if (mcell >= tau) {
                    if (mcell > overlap_max)
                        survivalCandicate.copy(y);
                }
                else if (mcell >= tauSplit) {
                    splitCandicate.merge(y);
                    splitUnion.add(y);
                }
            }

            if (splitCandicate.isEmpty() && survivalCandicate.isEmpty())
                this.deadList.add(x);
            else if (!splitCandicate.isEmpty()) {
                if (overlap(x, splitCandicate) >= tau) {
                    ArrayList<Cluster> split_x = new ArrayList<>();
                    split_x.addAll(splitUnion);
                    this.splitList.put(x, split_x);
                }
                else
                    this.deadList.add(x);
            }
            else
                absorption_survival.put(x, survivalCandicate);
        }

        for (Cluster y : c_j) {
            ArrayList<Cluster> absorptionUnion = new ArrayList<>();
            for (Map.Entry<Cluster, Cluster> entry : absorption_survival.entrySet()) {
                if (entry.getValue() == y) {
                    absorptionUnion.add(entry.getKey());
                }
            }
            if (absorptionUnion.size() > 1) {
                ArrayList<Cluster> absorption_y = new ArrayList<>();
                absorption_y.addAll(absorptionUnion);
                this.absorpList.put(y, absorption_y);
            } else if (absorptionUnion.size() == 1) {
                this.survivalList.add(absorptionUnion.get(0));
            }
        }
    }

    public void sizeTransition(Cluster x, Cluster y) {
        double density_x = x.getDensity();
        double density_y = y.getDensity();
        double deltaDensity = Math.abs(density_x - density_y);
        if (deltaDensity >= varepsilon) {
            if (density_x - density_y > 0)
                sizeTransitionList.put(x, -1);
            else
                sizeTransitionList.put(x, 1);
        }
    }

    public void compactTransition(Cluster x, Cluster y) {
        double standardDeviation_x = x.getStandardDeviation();
        double standardDeviation_y = y.getStandardDeviation();
        double deltaStandardDeviation = Math.abs(standardDeviation_x - standardDeviation_y);
        if (deltaStandardDeviation >= delta) {
            if (standardDeviation_x - standardDeviation_y > 0)
                compactTransitionList.put(x, 1);
            else
                compactTransitionList.put(x, -1);
        }
    }

    


}

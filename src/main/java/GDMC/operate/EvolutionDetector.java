package GDMC.operate;
import GDMC.model.Cluster;
import GDMC.model.Grid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static GDMC.util.Functions.EuclideanDistance;

/**
 * Created by jxm on 2021/7/23.
 */
public class EvolutionDetector {
    private int dim;
    private double shiftThreshold;//判定是否发生漂移的阈值
    private int kenalBandwidth;
    private HashMap<Integer, double[]> latestShiftAttrs = new HashMap<>();

    public EvolutionDetector(int dim, double shiftThreshold, int kenalBandwidth) {
        this.dim = dim;
        this.shiftThreshold = shiftThreshold;
        this.kenalBandwidth = kenalBandwidth;
    }

    private HashMap<Integer, double[]> calShiftAttrs(HashMap<Integer, Cluster> clusters) {
        HashMap<Integer, double[]> shiftAttrs = new HashMap<>();
        for (Map.Entry<Integer, Cluster> entry : clusters.entrySet()) {
            double[] shiftAttr = getMeanShift(entry.getValue());
            shiftAttrs.put(entry.getKey(), shiftAttr);
        }
        return shiftAttrs;
    }

    public boolean isShift(HashMap<Integer, Cluster> clusters) {
        if (latestShiftAttrs.isEmpty()) {
            latestShiftAttrs = calShiftAttrs(clusters);
            return true;
        } else {
            HashMap<Integer, double[]> currentShiftAttrs = calShiftAttrs(clusters);
            for (Integer label : latestShiftAttrs.keySet()) {
                double[] latestShiftAttr = latestShiftAttrs.get(label);
                double[] currentShiftAttr = currentShiftAttrs.get(label);
                double shiftDistance = EuclideanDistance(latestShiftAttr, currentShiftAttr);
                if (shiftDistance > shiftThreshold) {
                    latestShiftAttrs.clear();
                    return true;
                }
            }
        }
        return false;
    }


/*
    private ArrayList<Double> calGussianKernel(ArrayList<Double> distances) {
        ArrayList<Double> gussainValues = new ArrayList<>();
        double left = 1 / (kenalBandwidth * Math.sqrt(2 * Math.PI));
        for (int i = 0; i < distances.size(); i++) {
            double right = (-0.5 * Math.pow(distances.get(i), 2)) / (Math.pow(kenalBandwidth, 2));
            right = Math.exp(right);
            gussainValues.add(left * right);
        }
        return gussainValues;
    }

*/

    private ArrayList<Double> calGussianKernel(Cluster cluster) {
        ArrayList<Double> gussainValues = new ArrayList<>();
        double left = 1 / (kenalBandwidth * Math.sqrt(2 * Math.PI));
        for (int i = 0; i < cluster.getGrids().size(); i++) {
            Grid curGrid = cluster.getGrids().get(i);
            double right = (-0.5 * Math.pow(curGrid.getCenterDistance(), 2)) / (Math.pow(kenalBandwidth, 2));
            right = Math.exp(right);
            gussainValues.add(left * right);
        }
        return gussainValues;
    }

    private double[] getMeanShift(Cluster cluster) {
        ArrayList<Double> gussainValues = new ArrayList<>();
        gussainValues = calGussianKernel(cluster);
        //求分母 高斯值*密度值的累加和
        double allSum = 0.0;
        for (int i = 0; i < gussainValues.size(); i++) {
            allSum += gussainValues.get(i) * cluster.getGrids().get(i).getDensity();
        }

        double[] shiftAttr = new double[dim];
        for (int i = 0; i< shiftAttr.length; i++) {
            shiftAttr[i] = 0.0;
            int index = 0;
            for (Grid grid : cluster.getGrids()) {
                shiftAttr[i] += gussainValues.get(index) * grid.getDensity() * grid.getCentroid().getAttr()[i];
                index++;
            }
            shiftAttr[i] /= allSum;
        }

        return shiftAttr;
    }
/*
    private double[] getMeanShift(Grid center, ArrayList<Grid> grids) {
        ArrayList<Double> distances = new ArrayList<>();
        ArrayList<Double> gussainValues = new ArrayList<>();
        for (Grid grid : grids) {
            double distance = center.calDistance(grid);
            distances.add(distance);
        }
        gussainValues = calGussianKernel(distances);

        //求分母 高斯值*密度值的累加和
        double allSum = 0.0;
        for (int i = 0; i < gussainValues.size(); i++) {
            allSum += gussainValues.get(i) * grids.get(i).getDensity();
        }


        double[] shiftAttr = new double[center.getDim()];
        for (int i = 0; i< shiftAttr.length; i++) {
            shiftAttr[i] = 0.0;
            int index = 0;
            for (Grid grid : grids) {
                shiftAttr[i] += gussainValues.get(index) * grid.getDensity() * grid.getCentroid().getAttr()[i];
                index++;
            }
            shiftAttr[i] /= allSum;
        }

        return shiftAttr;
    }
 */

}

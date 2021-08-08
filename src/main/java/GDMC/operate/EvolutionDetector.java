package GDMC.operate;
import GDMC.model.Grid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static GDMC.utl.Functions.EuclideanDistance;

/**
 * Created by jxm on 2021/7/23.
 */
public class EvolutionDetector {
    private ArrayList<Grid> centers;
    private double shiftThreshold;//判定是否发生漂移的阈值
    private int kenalBandwidth;
    private Map<Integer, Boolean> result;

    public EvolutionDetector(ArrayList<Grid> centers, double shiftThreshold, int kenalBandwidth) {
        this.centers = centers;
        this.shiftThreshold = shiftThreshold;
        this.kenalBandwidth = kenalBandwidth;
    }

    public ArrayList<Double> calGussianKernel(ArrayList<Double> distances) {
        ArrayList<Double> gussainValues = new ArrayList<>();
        double left = 1 / (kenalBandwidth * Math.sqrt(2 * Math.PI));
        for (int i = 0; i < distances.size(); i++) {
            double right = (-0.5 * Math.pow(distances.get(i), 2)) / (Math.pow(kenalBandwidth, 2));
            right = Math.exp(right);
            gussainValues.add(left * right);
        }
        return gussainValues;
    }

    public double[] getMeanShift(Grid center, ArrayList<Grid> grids) {
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

    public Map<Integer, double[]> getShiftAttrs(ArrayList<Grid> grids) {
        Map<Integer, double[]> shiftDistances = new HashMap<>();
        for (Grid center : centers) {
            double[] shifAttr = getMeanShift(center, grids);
            shiftDistances.put(center.getLabel(), shifAttr);
        }
        return shiftDistances;
    }


    public Map<Integer, Boolean> process(Map<Integer, double[]> latestShiftAttrs, Map<Integer, double[]> currentShiftAttrs) {
        result = new HashMap<>();
        for (Grid center : centers) {
            int key = center.getLabel();
            double[] latestShiftDistance = latestShiftAttrs.get(key);
            double[] currentShiftDistace = currentShiftAttrs.get(key);
            double shiftDistance = EuclideanDistance(latestShiftDistance, currentShiftDistace);
            if (shiftDistance > shiftThreshold) {
                result.put(key, Boolean.TRUE);
            } else {
                result.put(key, Boolean.FALSE);
            }
        }
        return result;
    }
}

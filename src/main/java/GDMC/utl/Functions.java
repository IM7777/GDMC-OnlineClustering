package GDMC.utl;

/**
 * Created by jxm on 2021/8/1.
 */
public class Functions {
    public static double EuclideanDistance(double[] attr1, double[] attr2) {
        double distance = 0.0;
        for (int i = 0; i < attr1.length; i++) {
            distance += Math.pow(attr1[i] - attr2[i], 2);
        }
        return Math.sqrt(distance);
    }
}

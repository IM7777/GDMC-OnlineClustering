package common.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

    public static HashMap<Integer, ArrayList<Integer>> swapKV(HashMap<Integer, Integer> origin) {
        HashMap<Integer, ArrayList<Integer>> res = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : origin.entrySet()) {
            int key = entry.getKey();
            int value = entry.getValue();
            ArrayList<Integer> list = res.getOrDefault(value, new ArrayList<Integer>());
            list.add(key);
            res.put(value, list);
        }
        return res;
    }

    public static double log(double a, double x) {
        if (a <= 0 || a == 1 || x <= 0) {
            return -1;
        }
        return Math.log(x) / Math.log(a);
    }


    public static void main(String[] args) {
        double a = 0.998;
        double x = 0.31;
        System.out.println(log(a, x));

    }

}

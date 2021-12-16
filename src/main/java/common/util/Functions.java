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

    public static void main(String[] args) {
        HashMap<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) {
                map.put(i, 0);
            } else {
                map.put(i, 1);
            }
        }
        HashMap<Integer, ArrayList<Integer>> res = swapKV(map);
        System.out.println(map);

    }

}

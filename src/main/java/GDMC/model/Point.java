package GDMC.model;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by jxm on 2021/7/17.
 */
public class Point implements Serializable {

    private static final long serialVersionUID = 8459785729573034889L;
    private double[] attr;
    private int id;
    private int dim;

    public Point(double[] attr, int id) {
        this.attr = attr;
        this.id = id;
        this.dim = attr.length;
    }

    public int[] mapToGrid(double len) {
        int[] vec = new int[dim];
        for (int i=0;i<vec.length;i++) {
            if (attr[i] == 1.0) {
                attr[i] = 0.999999;
            }
            vec[i] = (int) Math.floor(attr[i] * 1.0 / len);
        }
        return vec;
    }

    public double[] getAttr() {
        return attr;
    }

    public int getId() {
        return id;
    }

    public int getDim() {
        return dim;
    }

    @Override
    public String toString() {
        return "Point{" +
                "attr=" + Arrays.toString(attr) +
                ", id=" + id +
                '}';
    }
}

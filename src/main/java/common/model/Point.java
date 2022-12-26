package common.model;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by jxm on 2021/7/17.
 */
public class Point implements Serializable {

    private static final long serialVersionUID = 8588843159689457355L;
    private double[] attr;
    private int id;
    private int dim;
    private int label;

    public Point(double[] attr, int id) {
        this.attr = attr;
        this.id = id;
        this.dim = attr.length;
    }

    public Point(double[] attr, int id, int label) {
        this.attr = attr;
        this.id = id;
        this.dim = attr.length;
        this.label = label;
    }

    public void standard(){
        for (int i =0; i<dim; i++){
            if (attr[i] == 0.0)
                break;
            if (attr[i] == 10.0) {
                attr[i] = 1.0;
                break;
            }
            attr[i] = (10.0-attr[i])/10;
        }
    }

    public int[] mapToGrid(double len) {
        int[] vec = new int[dim];
        for (int i=0;i<vec.length;i++) {
            if (attr[i] == 10.0) {
                attr[i] = 9.999999;
            }
            if (attr[i] == 1.0)
                attr[i] = 0.999999;
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

    public int getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return "Point{" +
                "attr=" + Arrays.toString(attr) +
                ", id=" + id +
                '}';
    }
}

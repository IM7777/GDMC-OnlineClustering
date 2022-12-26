package common.operate;

import common.model.Point;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by jxm on 2021/7/21.
 */
public class PointManager {

    private ArrayList<Point> points;

    public void readPoints(int dim, String filePath) throws IOException {
        points = new ArrayList<>();
        File file = new File(filePath);

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = null;
            int id = 0;
            while ((line = br.readLine()) != null) {
                if (line.length()==0) continue;
                String[] seg = line.split(",");
                double[] attr = new double[dim];
                for (int i = 0; i < dim; i++) {
                    attr[i] = Double.parseDouble(seg[i]);
                }
                points.add(new Point(attr, id));
                id++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void readPointsWithLabel(int dim, String filePath) throws IOException {
        points = new ArrayList<>();
        File file = new File(filePath);
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = null;
            int id = 0;
            while ((line = br.readLine()) != null) {
                if (line.length()==0) continue;
                String[] seg = line.split(",");
                double[] attr = new double[dim];
                for (int i = 0; i < dim; i++) {
                    attr[i] = Double.parseDouble(seg[i]);
                }
                int label = Integer.parseInt(seg[dim]);
                points.add(new Point(attr, id, label));
                id++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    public void pointsInfo() {
        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            point.standard();
            //System.out.println(point);
        }
    }

    public ArrayList<Point> getPoints() {
        return points;
    }



    public static void main(String[] args) throws IOException {
        //格式：x, y, label
        String filePath = "/Users/jxm/Downloads/data/synthetic/syn.txt";
        PointManager pointProcess = new PointManager();
        long st = System.currentTimeMillis();
        pointProcess.readPointsWithLabel(2, filePath);
        pointProcess.pointsInfo();
        long ed = System.currentTimeMillis();
        System.out.println("标准化时间是："+(ed-st));
    }
}

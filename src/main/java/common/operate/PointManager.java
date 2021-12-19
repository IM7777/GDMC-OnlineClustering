package common.operate;

import common.model.Point;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by jxm on 2021/7/21.
 */
public class PointManager {

    private ArrayList<Point> points;

    public void readPoints(String filePath) throws IOException {
        points = new ArrayList<>();
        File file = new File(filePath);

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = null;
            int id = 0;
            while ((line = br.readLine()) != null) {
                if (line.length()==0) continue;
                String[] seg = line.split(",");
                double[] attr = new double[2];
                for (int i = 0; i < attr.length; i++) {
                    attr[i] = Double.parseDouble(seg[i]);
                }
                points.add(new Point(attr, id));
                id++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void readPointsWithLabel(String filePath) throws IOException {
        points = new ArrayList<>();
        File file = new File(filePath);
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = null;
            int id = 0;
            while ((line = br.readLine()) != null) {
                if (line.length()==0) continue;
                String[] seg = line.split(",");
                double[] attr = new double[2];
                for (int i = 0; i < attr.length; i++) {
                    attr[i] = Double.parseDouble(seg[i]);
                }
                int label = Integer.parseInt(seg[2]);
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
            System.out.println(point);
        }
    }

    public ArrayList<Point> getPoints() {
        return points;
    }



    public static void main(String[] args) throws IOException {
        //格式：x, y, label
        String filePath = "E:\\我的坚果云\\2 数据集\\aggregation\\aggregationNormal.txt";
        PointManager pointProcess = new PointManager();
        pointProcess.readPoints(filePath);
        pointProcess.pointsInfo();
    }
}

package esa;

import common.model.Point;
import common.operate.PointManager;
import esa.model.ESACluster;
import esa.model.ESAGrid;
import esa.operate.ECSCluster;
import esa.operate.GridManager;
import esa.operate.ResultViewer;
import org.apache.commons.lang3.SerializationUtils;
import org.omg.Messaging.SYNC_WITH_TRANSPORT;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Supplier;

public class ESA {

    public String dataPath;
    public String outputPath;
    public int dim;
    public int initalNum;
    public double len;
    public double lambda;
    public boolean isWithLabel;
    public boolean isWrite;

    public ArrayList<Point> points;
    public ArrayList<ESAGrid> grids;
    public GridManager gridManager;
    public ResultViewer resultViewer;

    public ESA(String dataPath, String outputPath, int dim, int initalNum, double len, double lambda,
               boolean isWithLabel, boolean isWrite) {
        this.dataPath = dataPath;
        this.outputPath = outputPath;
        this.dim = dim;
        this.initalNum = initalNum;
        this.len = len;
        this.lambda = lambda;
        this.isWithLabel = isWithLabel;
        this.isWrite = isWrite;
        gridManager = new GridManager(lambda, len);
        resultViewer = new ResultViewer();
    }

    public void pointAcceptor() throws IOException {
        PointManager pointManager = new PointManager();
        if (isWithLabel)
            pointManager.readPointsWithLabel(dim, dataPath);
        else
            pointManager.readPoints(dim, dataPath);
        points = pointManager.getPoints();
    }

    public ArrayList<Integer> process() throws IOException {
        ArrayList<Integer> timestamps = new ArrayList<>();
        pointAcceptor();
        long st = System.currentTimeMillis();
        int t = 0;

        // 初始聚类
        while (t < initalNum) {
            gridManager.map(points.get(t));
            t++;
        }
        gridManager.updateAllGrids(t);
        timestamps.add(t);

        int interval = gridManager.gap;
        grids = gridManager.getGrids();
        ECSCluster ecsCluster = new ECSCluster(grids, gridManager.len);
        ecsCluster.process(gridManager.Du, gridManager.Dl);
        HashMap<Integer, ESACluster> currentClusters = SerializationUtils.clone(ecsCluster.getClusters());
        System.out.println("t=" + t + ", 初始聚类！" + "聚类个数：" + currentClusters.size() + ",Du:" + gridManager.Du + ",Dl:" + gridManager.Dl + ",gap:" + gridManager.gap);
        //resultViewer.showChart(currentClusters);
        if (isWrite)
            writeToFile(t);

        while (t < points.size()) {
            for (int i = 0; i < interval && t < points.size(); i++) {
                gridManager.map(points.get(t));
                t++;
            }
            gridManager.updateAllGrids(t);
            timestamps.add(t);
            interval = gridManager.gap;
            ecsCluster.process(gridManager.Du, gridManager.Dl);
            HashMap<Integer, ESACluster> latestClusters = SerializationUtils.clone(currentClusters);
            currentClusters = SerializationUtils.clone(ecsCluster.getClusters());
            System.out.println("t=" + t + ", 发生聚类！聚类个数：" + currentClusters.size() + ",Du:" + gridManager.Du + ",Dl:" + gridManager.Dl + ",gap:" + gridManager.gap);
            //resultViewer.showChart(currentClusters);
            if (isWrite)
                writeToFile(t);
        }
        long ed = System.currentTimeMillis();
        System.out.println("ESA的时间开销是：" + (ed - st) + "毫秒");
        return timestamps;
    }

    public void writeToFile(int time) throws IOException {
        String file = outputPath + lambda + "_" + len + "_" + time + ".txt";
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        for (ESAGrid grid : grids) {
            int label = grid.getLabel();
            if (label != -1) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < dim; i++) {
                    sb.append(grid.getVector()[i]);
                    sb.append(",");
                }
                sb.append(label);
                sb.append("\n");
                bw.write(sb.toString());
            }
        }
        bw.close();
    }

    public void writeToFile(ArrayList<Integer> timestamps) throws IOException {
        String file = outputPath + lambda + "_" + len + "_time.txt";
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        for (int timestamp : timestamps) {
            bw.write(timestamp + "\n");
        }
        bw.close();
    }


    public static void main(String[] args) throws IOException {
        String dataPath = "C:\\Users\\Celeste\\Desktop\\data\\synthetic\\syn.txt";
        String outputPath = "C:\\Users\\Celeste\\Desktop\\data\\result\\ESA\\synthetic\\";
        long start = System.currentTimeMillis();
        ESA esa = new ESA(dataPath, outputPath, 2, 1000, 0.5, 0.998, true, true);
        ArrayList<Integer> timestamps = esa.process();
        esa.writeToFile(timestamps);
        long end = System.currentTimeMillis();
        System.out.println("总的运行时间是：" + (end - start) + "毫秒");
    }
}

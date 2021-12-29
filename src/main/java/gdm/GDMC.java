package gdm;

import gdm.model.GDMCluster;
import gdm.model.GDMGrid;
//import gdm.model.Grid;
import common.model.Point;
import common.operate.PointManager;
import gdm.operate.ResultViewer;
import gdm.operate.*;
import org.apache.commons.lang3.SerializationUtils;
import org.omg.Messaging.SYNC_WITH_TRANSPORT;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by jxm on 2021/7/21.
 */
public class GDMC {
    public static String filePath = "C:\\Users\\Celeste\\Desktop\\data\\mergeWithLabel.txt";
    public static int initNum = 1000;

    public String dataPath;
    public String outputPath;
    public int initalNum;
    public double len;
    public double lambda;

    public ArrayList<Point> points;
    public ArrayList<GDMGrid> grids;
    public GridManager gridManager;
    public ResultViewer resultViewer;
    public EvolutionDetector evolutionDetector;

    public GDMC(String dataPath, int initalNum, String outputPath, double lambda, double len) {
        this.dataPath = dataPath;
        this.initalNum = initalNum;
        this.outputPath = outputPath;
        this.len = len;
        this.lambda = lambda;
        points = new ArrayList<>();
        grids = new ArrayList<>();
        resultViewer = new ResultViewer();
        gridManager = new GridManager(lambda, len);
        evolutionDetector = new EvolutionDetector(2, 0.4, 2);

    }

    public ArrayList<Integer> process() throws IOException {
        ArrayList<Integer> timestamps = new ArrayList<>();
        pointAcceptor();
        int t = 0;

        // 初始聚类
        while (t < initalNum) {
            Point curPoint = points.get(t);
            gridManager.mapToGrid(curPoint);
            t++;
        }
        gridManager.updateGrids(t);
        int interval = gridManager.gap;
        grids = gridManager.getGrids();

        //分裂的delta为2.2
        GDPCluster currentGDPC = new GDPCluster(grids, 2.5);
        System.out.println("t=" + t + ", 初始聚类！");
        timestamps.add(t);
        currentGDPC.process(gridManager.Dh);
        //currentGDPC.info();
        HashMap<Integer, GDMCluster> currentClusters = SerializationUtils.clone(currentGDPC.getClusters());
        resultViewer.showChart(currentClusters);
        writeToFile(t);

        //第一次计算中心点偏移值
        evolutionDetector.setLatestShiftAttrs(currentClusters);

        while (t < points.size()) {
            // 映射数据至网格，对于新增的网格为其直接分配标签，否则就是更新旧网格
            for(int i=0; i<interval && t<points.size(); i++){
                Point curPoint = points.get((int) t);
                t++;
                gridManager.mapToGrid(curPoint, currentGDPC.getCenters());
            }
            //更新网格密度等
            gridManager.updateGrids(t);
            interval = gridManager.gap;
            // 均值漂移检测
            if (evolutionDetector.isShift(currentGDPC.getClusters(), gridManager.avg)) {
                System.out.println("t=" + t + ", 发生聚类！");
                timestamps.add(t);
                currentGDPC.process(gridManager.Dh);
                //currentGDPC.info();
                HashMap<Integer, GDMCluster> latestClusters = SerializationUtils.clone(currentClusters);
                currentClusters = SerializationUtils.clone(currentGDPC.getClusters());
                evolutionDetector.setLatestShiftAttrs(currentClusters);
                resultViewer.showChart(currentClusters);
                writeToFile(t);
                //演化分析
                /*
                EvolutionRecognizer evolutionRecognizer = new EvolutionRecognizer(latestClusters, currentClusters);
                evolutionRecognizer.process();
                 */
            }
        }
        return timestamps;

    }

    public void pointAcceptor() throws IOException {
        PointManager pointManager = new PointManager();
        pointManager.readPointsWithLabel(dataPath);
        points = pointManager.getPoints();
    }

    public void writeToFile(int time) throws IOException {
        String file = outputPath + lambda + "_" + len + "_" + time + ".txt";
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        for (GDMGrid grid : grids) {
            int label = grid.getLabel();
            if (label != -1) {
                String line = grid.getVector()[0] + "," + grid.getVector()[1] + "," + label + "\n";
                bw.write(line);
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
        String dataPath = "C:\\Users\\Celeste\\Desktop\\data\\mergeWithLabel.txt";
        String outputPath = "C:\\Users\\Celeste\\Desktop\\data\\result\\GDMC\\";
        long start = System.currentTimeMillis();
        GDMC gdmc = new GDMC(dataPath, 1000, outputPath, 0.998, 0.1);
        ArrayList<Integer> timestamps = gdmc.process();
        gdmc.writeToFile(timestamps);
        long end = System.currentTimeMillis();
        System.out.println("GDMC运行时间：" + (end - start));

    }
}

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
    public String dataPath;
    public String outputPath;
    public int initalNum;
    public int dim;
    public double len;
    public double lambda;
    public double deltaThreshold;
    public boolean isWithLabel;
    public boolean isWrite;

    public ArrayList<Point> points;
    public ArrayList<GDMGrid> grids;
    public GridManager gridManager;
    public ResultViewer resultViewer;
    public EvolutionDetector evolutionDetector;

    public GDMC(String dataPath, int initalNum, int dim, String outputPath, double lambda, double len, double deltaThreshold,
                double shiftThreshold, boolean isWithLabel, boolean isWrite) {
        this.dataPath = dataPath;
        this.initalNum = initalNum;
        this.dim = dim;
        this.outputPath = outputPath;
        this.len = len;
        this.lambda = lambda;
        this.deltaThreshold = deltaThreshold;
        this.isWithLabel = isWithLabel;
        this.isWrite = isWrite;

        points = new ArrayList<>();
        grids = new ArrayList<>();
        resultViewer = new ResultViewer();
        gridManager = new GridManager(lambda, len);
        evolutionDetector = new EvolutionDetector(dim, shiftThreshold, 2);

    }

    public ArrayList<Integer> process() throws IOException {
        ArrayList<Integer> timestamps = new ArrayList<>();
        pointAcceptor();
        int t = 0;
        long st = System.currentTimeMillis();
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
        GDPCluster currentGDPC = new GDPCluster(grids, deltaThreshold);

        timestamps.add(t);
        currentGDPC.process(gridManager.Dh);
        HashMap<Integer, GDMCluster> currentClusters = SerializationUtils.clone(currentGDPC.getClusters());
        System.out.println("t=" + t + ", 初始聚类！" + "聚类个数：" + currentClusters.size() + "  Dh：" + gridManager.Dh + ", Dl：" + gridManager.Dl + ", gap：" + gridManager.gap);
        resultViewer.showChart(currentClusters);
        long firstEnd = System.currentTimeMillis();
        System.out.println("第一次聚类的时间开销是：" + (firstEnd - st) + "ms");
        if (isWrite)
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

                timestamps.add(t);
                currentGDPC.process(gridManager.Dh);
                HashMap<Integer, GDMCluster> latestClusters = SerializationUtils.clone(currentClusters);
                currentClusters = SerializationUtils.clone(currentGDPC.getClusters());
                System.out.println("t=" + t + ", 发生聚类！" + "聚类个数：" + currentClusters.size()+ "  Dh：" + gridManager.Dh + ", Dl：" + gridManager.Dl + ", gap：" + gridManager.gap);
                evolutionDetector.setLatestShiftAttrs(currentClusters);
                resultViewer.showChart(currentClusters);
                if (isWrite)
                    writeToFile(t);

                //演化分析
                long est = System.currentTimeMillis();
                EvolutionRecognizer evolutionRecognizer = new EvolutionRecognizer(latestClusters, currentClusters);
                evolutionRecognizer.process();
                long eed = System.currentTimeMillis();
                System.out.println("演化分析执行时间是："+(eed-est)+"ms");
            }
        }
        long ed = System.currentTimeMillis();
        System.out.println("GDMC的时间开销是：" + (ed - st) + "ms");
        return timestamps;

    }

    public void pointAcceptor() throws IOException {
        PointManager pointManager = new PointManager();
        if (isWithLabel)
            pointManager.readPointsWithLabel(dim, dataPath);
        else
            pointManager.readPoints(dim, dataPath);
        points = pointManager.getPoints();
    }

    public void writeToFile(int time) throws IOException {
        String file = outputPath + lambda + "_" + len + "_" + time + ".txt";
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        for (GDMGrid grid : grids) {
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

    /*
    kdd.txt 34个属性，有标签
     */

    public static void main(String[] args) throws IOException {
        String dataPath = "/Users/jxm/Downloads/data/synthetic/syn.txt";
        String outputPath = "C:\\Users\\Celeste\\Desktop\\data\\result\\GDMC\\synthetic\\";
        long st = System.currentTimeMillis();
        GDMC gdmc = new GDMC(dataPath, 1000, 2, outputPath, 0.998, 0.2,
                2.0 , 0.4, true, false);
        ArrayList<Integer> timestamps = gdmc.process();
        long ed = System.currentTimeMillis();
        //gdmc.writeToFile(timestamps);
        System.out.println("总的运行时间是：" + (ed - st) + "ms");
    }
}

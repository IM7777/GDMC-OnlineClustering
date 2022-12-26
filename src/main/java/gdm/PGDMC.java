package gdm;

import common.model.Point;
import common.operate.PointManager;
import gdm.model.GDMCluster;
import gdm.model.GDMGrid;
import gdm.operate.*;
import org.apache.commons.lang3.SerializationUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class PGDMC {
    public String dataPath;
    public int initalNum;
    public int dim;
    public int parallelism;
    public double len;
    public double lambda;
    public double deltaThreshold;
    public boolean isWithLabel;

    public ArrayList<Point> points;
    public ArrayList<GDMGrid> grids;
    public ParallelGridManager parallelGridManager;
    public EvolutionDetector evolutionDetector;

    public PGDMC(String dataPath, int initalNum, int dim, int parallelism, double lambda, double len, double deltaThreshold,
                double shiftThreshold, boolean isWithLabel) {
        this.dataPath = dataPath;
        this.initalNum = initalNum;
        this.dim = dim;
        this.parallelism = parallelism;
        this.len = len;
        this.lambda = lambda;
        this.deltaThreshold = deltaThreshold;
        this.isWithLabel = isWithLabel;


        points = new ArrayList<>();
        grids = new ArrayList<>();
        parallelGridManager = new ParallelGridManager(parallelism, lambda, len);
        evolutionDetector = new EvolutionDetector(dim, shiftThreshold, 2);

    }

    public ArrayList<Integer> process() throws IOException, InterruptedException {
        ArrayList<Integer> timestamps = new ArrayList<>();
        pointAcceptor();
        int t = 0;
        long st = System.currentTimeMillis();
        // 初始聚类
        while (t < initalNum) {
            Point curPoint = points.get(t);
            parallelGridManager.mapToGrid(curPoint);
            t++;
        }
        parallelGridManager.updateGrids(t);
        int interval = parallelGridManager.gap;
        grids = parallelGridManager.getGrids();

        //分裂的delta为2.2
        ParallelGDPCluster parallelCluster = new ParallelGDPCluster(parallelism, deltaThreshold, grids);
        timestamps.add(t);
        parallelCluster.process(parallelGridManager.Dh);
        HashMap<Integer, GDMCluster> currentClusters = SerializationUtils.clone(parallelCluster.getClusters());
        System.out.println("t=" + t + ", 初始聚类！" + "聚类个数：" + currentClusters.size());
        //resultViewer.showChart(currentClusters);

        //第一次计算中心点偏移值
        evolutionDetector.setLatestShiftAttrs(currentClusters);

        while (t < points.size()) {
            // 映射数据至网格，对于新增的网格为其直接分配标签，否则就是更新旧网格
            for(int i=0; i<interval && t<points.size(); i++){
                Point curPoint = points.get((int) t);
                t++;
                parallelGridManager.mapToGrid(curPoint, parallelCluster.getCenters());
            }
            //更新网格密度等
            parallelGridManager.updateGrids(t);
            interval = parallelGridManager.gap;
            // 均值漂移检测
            if (evolutionDetector.isShift(parallelCluster.getClusters(), parallelGridManager.avg)) {

                timestamps.add(t);
                parallelCluster.process(parallelGridManager.Dh);
                HashMap<Integer, GDMCluster> latestClusters = SerializationUtils.clone(currentClusters);
                currentClusters = SerializationUtils.clone(parallelCluster.getClusters());
                System.out.println("t=" + t + ", 发生聚类！" + "聚类个数：" + currentClusters.size());
                evolutionDetector.setLatestShiftAttrs(currentClusters);
                //resultViewer.showChart(currentClusters);

                //演化分析
                /*
                EvolutionRecognizer evolutionRecognizer = new EvolutionRecognizer(latestClusters, currentClusters);
                evolutionRecognizer.process();
                 */
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



    public static void main(String[] args) throws IOException, InterruptedException {
        String dataPath = "C:\\Users\\Celeste\\Desktop\\data\\TDrive.txt";
        long st = System.currentTimeMillis();
        PGDMC pgdmc = new PGDMC(dataPath, 1000, 2, 4, 0.998, 0.01,
                0.25, 0.04, false);
        ArrayList<Integer> timestamps = pgdmc.process();
        long ed = System.currentTimeMillis();
        System.out.println("总的运行时间是：" + (ed - st) + "ms");
    }
}

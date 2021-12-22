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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by jxm on 2021/7/21.
 */
public class GDMClustering {
    public static String filePath = "C:\\Users\\Celeste\\Desktop\\data\\merge.txt";
    public static int initNum = 1000;


    public static void main(String[] args) throws IOException {
        long start = System.currentTimeMillis();

        ArrayList<Point> points = new ArrayList<>();
        ArrayList<GDMGrid> grids = new ArrayList<>();

        PointManager pointManager = new PointManager();
        pointManager.readPoints(filePath);
        points = pointManager.getPoints();

        int t = 0;
        // 网格管理模块，设置
        GridManager gridManager = new GridManager(0.997, 0.1);

        // 初始聚类
        while (t < initNum) {
            Point curPoint = points.get(t);
            gridManager.mapToGrid(curPoint);
            t++;
        }
        gridManager.updateGrids(t);
        grids = gridManager.getGrids();

        //分裂的delta为2.2
        GDPCluster currentGDPC = new GDPCluster(grids, 2.5);
        currentGDPC.process(gridManager.Dh);
        //currentGDPC.info();
        HashMap<Integer, GDMCluster> currentClusters = SerializationUtils.clone(currentGDPC.getClusters());

        //聚类结果显示模块
        ResultViewer resultViewer = new ResultViewer();
        resultViewer.showChart(currentClusters);

        EvolutionDetector ed = new EvolutionDetector(2, 0.4, 2);
        //第一次计算中心点偏移值
        ed.setLatestShiftAttrs(currentClusters);

        while (t < points.size()) {
            // 映射数据至网格，对于新增的网格为其直接分配标签，否则就是更新旧网格
            for(int i=0; i<initNum && t<points.size(); i++){
                Point curPoint = points.get((int) t);
                t++;
                gridManager.mapToGrid(curPoint, currentGDPC.getCenters());
            }
            //更新网格密度等
            gridManager.updateGrids(t);
            // 均值漂移检测
            if (ed.isShift(currentGDPC.getClusters(), gridManager.avg)) {
                System.out.println("t=" + t + "，检测可能有发生！");
                currentGDPC.process(gridManager.Dh);
                //currentGDPC.info();
                HashMap<Integer, GDMCluster> latestClusters = SerializationUtils.clone(currentClusters);
                currentClusters = SerializationUtils.clone(currentGDPC.getClusters());
                ed.setLatestShiftAttrs(currentClusters);
                resultViewer.showChart(currentClusters);
                //演化分析
                /*
                EvolutionRecognizer evolutionRecognizer = new EvolutionRecognizer(latestClusters, currentClusters);
                evolutionRecognizer.process();
                 */
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("运行时间：" + (end - start));

    }
}

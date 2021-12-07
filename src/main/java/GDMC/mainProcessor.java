package GDMC;

import GDMC.model.Cluster;
import GDMC.model.Grid;
import GDMC.model.Point;
import GDMC.operate.*;
import org.jfree.chart.util.CloneUtils;
import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by jxm on 2021/7/21.
 */
public class mainProcessor {
    public static String filePath = "C:\\Users\\Celeste\\Desktop\\data\\merge.txt";
    public static int initNum = 1000;


    public static void main(String[] args) throws IOException {
        ArrayList<Point> points = new ArrayList<>();
        ArrayList<Grid> grids = new ArrayList<>();

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
        grids = gridManager.getGrids();

        GDPCluster currentGDPC = new GDPCluster(grids, 2.5);
        currentGDPC.process(t);
        //currentGDPC.info();
        HashMap<Integer, Cluster> currentClusters = SerializationUtils.clone(currentGDPC.getClusters());

        //聚类结果显示模块
        ResultViewer resultViewer = new ResultViewer();
        resultViewer.showChart(currentClusters);

        EvolutionDetector ed = new EvolutionDetector(2, 0.4, 2);
        System.out.println("计算第一次中心点漂移值");
        ed.isShift(currentClusters);

        while (t < points.size()) {
            // 映射数据至网格，对于新增的网格为其直接分配标签，否则就是更新旧网格
            for(int i=0; i<initNum && t<points.size(); i++){
                Point curPoint = points.get((int) t);
                t++;
                gridManager.mapToGrid(curPoint, currentGDPC.getCenters());
            }
            // 均值漂移检测
            if (ed.isShift(currentGDPC.getClusters())) {
                System.out.println("t=" + t + "，发生漂移啦啊！");
                currentGDPC.process(t);
                //currentGDPC.info();
                HashMap<Integer, Cluster> latestClusters = SerializationUtils.clone(currentClusters);
                currentClusters = SerializationUtils.clone(currentGDPC.getClusters());
                resultViewer.showChart(currentClusters);
                EvolutionRecognizer evolutionRecognizer = new EvolutionRecognizer(latestClusters, currentClusters);
                evolutionRecognizer.process();
            }
        }

    }
}

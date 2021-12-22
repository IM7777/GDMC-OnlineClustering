package esa;

import common.model.Point;
import common.operate.PointManager;
import esa.model.ESACluster;
import esa.model.ESAGrid;
import esa.operate.ECSCluster;
import esa.operate.GridManager;
import esa.operate.ResultViewer;
import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ESAStream {
    public static String filePath = "C:\\Users\\Celeste\\Desktop\\data\\merge.txt";
    public static int initNum = 1000;


    public static void main(String[] args) throws IOException {
        long start = System.currentTimeMillis();
        ArrayList<Point> points = new ArrayList<>();
        ArrayList<ESAGrid> grids = new ArrayList<>();

        PointManager pointManager = new PointManager();
        pointManager.readPoints(filePath);
        points = pointManager.getPoints();

        int t = 0;
        // 网格管理模块，设置
        GridManager gridManager = new GridManager(0.997, 0.1);

        // 初始聚类
        while (t < initNum) {
            Point curPoint = points.get(t);
            gridManager.map(curPoint);
            t++;
        }
        gridManager.updateAllGrids(t);
        int nextTime = (int) (t + gridManager.gap);
        grids = gridManager.getGrids();

        //分裂的delta为2.2
        ECSCluster currentESA = new ECSCluster(grids, gridManager.len);
        currentESA.process(gridManager.Du, gridManager.Dl);
        //currentGDPC.info();
        HashMap<Integer, ESACluster> currentClusters = SerializationUtils.clone(currentESA.getClusters());

        //聚类结果显示模块
        ResultViewer resultViewer = new ResultViewer();
        resultViewer.showChart(currentClusters);


        while (t < points.size()) {
            // 映射数据至网格，对于新增的网格为其直接分配标签，否则就是更新旧网格
            for(int i=0; i<nextTime && t<points.size(); i++){
                Point curPoint = points.get((int) t);
                t++;
                gridManager.map(curPoint);
            }
            //更新网格密度等
            gridManager.updateAllGrids(t);
            nextTime = (int) gridManager.gap;
            // 均值漂移检测

            currentESA.process(gridManager.Du, gridManager.Dl);
            //currentGDPC.info();
            HashMap<Integer, ESACluster> latestClusters = SerializationUtils.clone(currentClusters);
            currentClusters = SerializationUtils.clone(currentESA.getClusters());
            resultViewer.showChart(currentClusters);
        }

        long end = System.currentTimeMillis();
        System.out.println("运行时间:" + (end - start));
    }

}

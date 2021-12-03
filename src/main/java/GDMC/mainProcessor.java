package GDMC;

import GDMC.model.Cluster;
import GDMC.model.Grid;
import GDMC.model.Point;
import GDMC.operate.*;
import org.jfree.chart.util.CloneUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static GDMC.util.Functions.deepCloneObject;

/**
 * Created by jxm on 2021/7/21.
 */
public class mainProcessor {
    public static String filePath = "C:\\Users\\Celeste\\Desktop\\data\\merge.txt";
    public static int initNum = 1000;


    public static void main(String[] args) throws IOException {
        ArrayList<Point> points = new ArrayList<>();
        ArrayList<Grid> grids = new ArrayList<>();

        PointManager pm = new PointManager();
        pm.readPoints(filePath);
        points = pm.getPoints();

        int t = 0;
        // 网格管理模块，设置
        GridManager gm = new GridManager(1.0, 0.1);

        // 初始聚类
        while (t < initNum) {
            Point curPoint = points.get(t);
            gm.mapToGrid(curPoint);
            t++;
        }
        grids = gm.getGrids();

        GDPCluster currentGDPC = new GDPCluster(grids, 13, 1.0);
        currentGDPC.calDelta();
        currentGDPC.findCenters();
        currentGDPC.assignLabel();
        currentGDPC.info();
        ArrayList<Grid> currentCenters = currentGDPC.getCenters();
        HashMap<Integer, Cluster> currentClusters = currentGDPC.getClusters();
        EvolutionDetector ed = new EvolutionDetector(currentGDPC.getCenters(), 0.5, 2);

        while (t < points.size()) {
            // 映射数据至网格，对于新增的网格为其直接分配标签，否则就是更新旧网格
            for(int i=0; i<initNum && t<points.size(); i++){
                Point curPoint = points.get(t);
                t++;
                gm.mapToGrid(curPoint, currentGDPC.getCenters());
            }
            // 均值漂移检测





        }

        GDPCluster latestGDPC = deepCloneObject(currentGDPC);
        ResultViewer rv = new ResultViewer(currentGDPC.getClusters());
        rv.showChart();



    }
}

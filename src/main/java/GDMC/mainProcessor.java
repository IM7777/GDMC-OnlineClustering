package GDMC;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by jxm on 2021/7/21.
 */
public class mainProcessor {
    public static String filePath = "/Users/jxm/NutstoreCloudBridge/我的坚果云/2 数据集/aggregation/aggregationNormal.txt";
    public static int initNum = 788;


    public static void main(String[] args) throws IOException {
        ArrayList<Point> points = new ArrayList<>();
        ArrayList<Grid> grids = new ArrayList<>();

        PointManager pm = new PointManager();
        pm.readPoints(filePath);
        points = pm.getPoints();

        int t = 0;
        GridManager gm = new GridManager(1.0, 0.1);
        while (t < initNum) {
            Point curPoint = points.get(t);
            gm.mapToGrid(curPoint);
            t++;
        }
        grids = gm.getGrids();

        GDPCluster gdpCluster = new GDPCluster(grids, 5.0, 0.25);
        gdpCluster.calDelta();
        gdpCluster.findCenters();
        gdpCluster.assignLabel();
        gdpCluster.info();


    }
}
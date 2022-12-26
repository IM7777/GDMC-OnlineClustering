package standard;

import common.model.Point;
import common.operate.PointManager;
import standard.model.StdCluster;
import standard.model.StdGrid;
import standard.operate.ClusterProcessor;
import standard.operate.ESAGridManager;
import standard.operate.GDMCGridManager;
import standard.operate.ResultViewer;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;


public class Standard {
    public String outputPath;
    public int dim;
    public double lambda;
    public double len;
    public ArrayList<Integer> timestamps;
    public ArrayList<Point> points;
    public ArrayList<StdGrid> grids;
    public ResultViewer resultViewer;

    public Standard(String inputPath, String outputPath, String timePath, int dim, double lambda, double len)
            throws IOException {
        this.outputPath = outputPath;
        this.dim = dim;
        this.lambda = lambda;
        this.len = len;
        this.timestamps = readTimestamps(timePath);
        PointManager pointManager = new PointManager();
        pointManager.readPointsWithLabel(dim, inputPath);
        points = pointManager.getPoints();
        grids = new ArrayList<>();
        resultViewer = new ResultViewer();
    }

    public void ESAProcess() throws IOException {
        ESAGridManager gridManager = new ESAGridManager(lambda, len);
        grids = gridManager.getGrids();
        int t = 0;
        for (int timestamp : timestamps) {
            while (t < timestamp) {
                Point point = points.get(t);
                gridManager.map(point);
                t++;
            }
            gridManager.updateAllGrids(t);
            writeToFile(t);
            ClusterProcessor cp = new ClusterProcessor(grids);
            //resultViewer.showChart(cp.getClusters());
        }

    }

    public void GDMCProcess() throws IOException {
        GDMCGridManager gridManager = new GDMCGridManager(lambda, len);
        grids = gridManager.getGrids();
        int t = 0;
        for (int timestamp : timestamps) {
            while (t < timestamp) {
                Point point = points.get(t);
                gridManager.mapToGrid(point);
                t++;
            }
            gridManager.updateAllGrids(t);
            writeToFile(t);
            ClusterProcessor cp = new ClusterProcessor(grids);
            HashMap<Integer, StdCluster> clusters = cp.getClusters();
            //resultViewer.showChart(clusters);
        }
    }

    public void writeToFile(int t) throws IOException {
        String file = outputPath + lambda + "_" + len + "_" + t + ".txt";
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        for (StdGrid grid : grids) {
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

    public ArrayList<Integer> readTimestamps(String timePath) throws IOException {
        timePath += lambda + "_" + len + "_time.txt";
        BufferedReader br = new BufferedReader(new FileReader(timePath));
        ArrayList<Integer> timestamps = new ArrayList<>();
        String line = null;
        while ((line = br.readLine()) != null) {
            timestamps.add(Integer.parseInt(line));
        }
        return timestamps;
    }

    public static void main(String[] args) throws IOException {
        String dataPath = "C:\\Users\\Celeste\\Desktop\\data\\synthetic\\syn.txt";

        String timePath = "C:\\Users\\Celeste\\Desktop\\data\\result\\GDMC\\synthetic\\";
        String outputPath = "C:\\Users\\Celeste\\Desktop\\data\\result\\Standard\\gdmc\\synthetic\\";

        //String timePath = "C:\\Users\\Celeste\\Desktop\\data\\result\\ESA\\synthetic\\";
        //String outputPath = "C:\\Users\\Celeste\\Desktop\\data\\result\\Standard\\esa\\synthetic\\";
        Standard std = new Standard(dataPath, outputPath, timePath, 2,0.998, 0.3);
        std.GDMCProcess();
        //std.ESAProcess();
    }

}

package common.operate;

import common.model.Cluster;
import common.model.Grid;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static common.util.Functions.Cmn;

/*
聚类纯度 Purity
正确聚类数占总数的比例
-------------------------------------------------------------------------------------------
兰德系数 RandIndex
对于任意一个聚类来说：
1.如果从里面任取两个样本出来均是同一个类别，这就表示这个不带中的所有样本都算作是聚类正确的
2.如果取出来发现存在两个样本不是同一类别，就说明存在聚类错误的情况
对于从任意两个聚类中各取一个的情况来书：
3.如果两个样本均是不同类别，就表明两个聚类中的样本都被聚类正确了
4.如果两个样本是相同的，则说明当前聚类存在聚类错误的情况
因此有如下定义：
TP：表示两个同类样本点在同一个聚类中的情况数量（正确）
FP：表示两个非同类样本点在同一个聚类中的情况数量
TN：表示两个非同类样本点分别在两个聚类中的情况数量（正确）
FN：表示两个同类样本点分别在两个聚类中的情况数量

RI = (TP + FN)/(TP + FP + TN + FN)
----------------------------------------------------------------------------------------------
轮廓系数 Silhouette Coefficient

 */
public class Metric {
    public String sourceFilePath;
    public String standardFilePath;
    public String metric;
    public int dim;
    public double lambda;
    public double len;
    public ArrayList<Integer> timestamps;

    public Metric() {
    }

    private Metric(String sourceFilePath, String standardFilePath, String metric, int dim, double lambda, double len) throws IOException {
        this.sourceFilePath = sourceFilePath;
        this.standardFilePath = standardFilePath;
        this.metric = metric;
        this.dim = dim;
        this.lambda = lambda;
        this.len = len;
        this.timestamps = readTimestamps();
    }

    private ArrayList<Integer> readTimestamps() throws IOException {
        String timePath = sourceFilePath + lambda + "_" + len + "_time.txt";
        BufferedReader br = new BufferedReader(new FileReader(timePath));
        ArrayList<Integer> timestamps = new ArrayList<>();
        String line = null;
        while ((line = br.readLine()) != null) {
            timestamps.add(Integer.parseInt(line));
        }
        return timestamps;
    }

    private double getPurity(HashMap<Integer, Cluster<Grid>> sourceClusters, HashMap<Integer, Cluster<Grid>> standardClusters) {
        int totalOverlap = 0;
        // 参与聚类的样本总数
        int num = 0;
        for (Cluster<Grid> cluster : sourceClusters.values()) {
            int maxOverlap = 0;
            for (Cluster<Grid> standard : standardClusters.values()) {
                int overlap = cluster.getOverlap(standard.getGrids());
                maxOverlap = Math.max(overlap, maxOverlap);
            }
            num += cluster.getGrids().size();
            totalOverlap += maxOverlap;
        }
        return totalOverlap * 1.0 / num;
    }

    private double getRandIndex(HashMap<Integer, Cluster<Grid>> sourceClusters, HashMap<Integer, Cluster<Grid>> standardClusters) {
        double ans = 0.0;
        int TP = computeTP(sourceClusters, standardClusters);
        int TP_FP = computeTFP(sourceClusters);
        int TP_FN = computeTPFN(sourceClusters, standardClusters);
        int all = computeAll(sourceClusters);
        if (all == 0) {
            return 0;
        }
        int FP = TP_FP - TP;
        int FN = TP_FN - TP;
        int TN = all - TP - FN - FP;
        ans = (TP + TN) * 1.0 / all;
        return ans;
    }

    private int computeTP(HashMap<Integer, Cluster<Grid>> sourceClusters, HashMap<Integer, Cluster<Grid>> standardClusters) {
        int TP = 0;
        for (Cluster<Grid> source : sourceClusters.values()) {
            HashMap<Integer, Integer> truthLabels = new HashMap<>();
            for (Grid grid : source.getGrids()) {
                int truthLabel = findTruthLabel(grid, standardClusters);
                if (truthLabel != -1) {
                    int count = truthLabels.getOrDefault(truthLabel, 0);
                    count++;
                    truthLabels.put(truthLabel, count);
                }
            }
            for (Integer count : truthLabels.values()) {
                if (count >= 2) {
                    TP += Cmn(2, count);
                }
            }
        }
        return TP;
    }

    private int computeTFP(HashMap<Integer, Cluster<Grid>> sourceClusters) {
        int TFP = 0;
        for (Cluster<Grid> cluster : sourceClusters.values()) {
            int n = cluster.getGrids().size();
            if (n >= 2) {
                TFP += Cmn(2, n);
            }
        }
        return TFP;
    }

    private int computeTPFN(HashMap<Integer, Cluster<Grid>> sourceClusters, HashMap<Integer, Cluster<Grid>> standardClusters) {
        int TPFN = 0;
        HashMap<Integer, Integer> truthLabels = new HashMap<>();
        for (Cluster<Grid> source : sourceClusters.values()) {
            ArrayList<Grid> grids = source.getGrids();
            for (Grid grid : grids) {
                int truthLabel = findTruthLabel(grid, standardClusters);
                if (truthLabel != -1) {
                    int count = truthLabels.getOrDefault(truthLabel, 0);
                    count++;
                    truthLabels.put(truthLabel, count);
                }
            }
        }
        for (Integer n : truthLabels.values()) {
            if (n >= 2) {
                TPFN += Cmn(2, n);
            }
        }
        return TPFN;
    }

    private int computeAll(HashMap<Integer, Cluster<Grid>> sourceClusters) {
        int n = 0;
        for (Cluster<Grid> cluster : sourceClusters.values()) {
            n += cluster.getGrids().size();
        }
        return Cmn(2, n);
    }

    private int findTruthLabel(Grid grid, HashMap<Integer, Cluster<Grid>> standardClusters) {
        int truthLabel = -1;
        for (Integer label : standardClusters.keySet()) {
            ArrayList<Grid> grids = standardClusters.get(label).getGrids();
            if (grids.indexOf(grid) != -1) {
                truthLabel = label;
                break;
            }
        }
        return truthLabel;
    }

    private double getSilhouetteCoefficient(HashMap<Integer, Cluster<Grid>> sourceClusters) {
        double totalSi = 0.0;
        int count=0;
        for (Integer label1 : sourceClusters.keySet()) {
            Cluster<Grid> cluster1 = sourceClusters.get(label1);
            for (Grid grid : cluster1.getGrids()) {
                double ai = getAi(grid, cluster1);
                double bi = Double.MAX_VALUE;
                for (Integer label2 : sourceClusters.keySet()) {
                    if (label2 != label1) {
                        Cluster<Grid> cluster2 = sourceClusters.get(label2);
                        double currentBi = getBi(grid, cluster2);
                        bi = Math.min(currentBi, bi);
                    }
                }
                double si = (bi - ai) / Math.max(ai, bi);
                totalSi += si;
                count++;
            }
        }
        return totalSi / count;
    }

    private double getAi(Grid grid, Cluster<Grid> cluster) {
        ArrayList<Grid> grids = cluster.getGrids();
        double totalDistance = 0.0;
        for (Grid temp : grids) {
            totalDistance += grid.calVecDistance(temp);
        }
        if (grids.size()==1)
            return 1;
        return totalDistance / (grids.size() - 1);
    }


    private double getBi(Grid grid, Cluster<Grid> cluster) {
        ArrayList<Grid> grids = cluster.getGrids();
        double totalDistance = 0.0;
        for (Grid temp : grids) {
            totalDistance += grid.calVecDistance(temp);
        }
        return totalDistance / grids.size();
    }

    private HashMap<Integer, Cluster<Grid>> getClusters(String sourceFilePath) throws IOException {
        HashMap<Integer, Cluster<Grid>> clusters = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(sourceFilePath));
        String line = null;
        while ((line = br.readLine()) != null) {
            String[] seg = line.split(",");
            int[] vec = new int[dim];
            for (int i = 0; i < dim; i++)
                vec[i] = Integer.parseInt(seg[i]);
            Grid grid = new Grid(vec);
            int label = Integer.parseInt(seg[dim]);
            Cluster<Grid> cluster = clusters.getOrDefault(label, new Cluster<>(label));
            cluster.addGrid(grid);
            clusters.put(label, cluster);
        }
        return clusters;
    }

    public void process() throws IOException {
        ArrayList<Integer> timestamps = readTimestamps();
        HashMap<Integer, Double> results = new HashMap<>();
        double sum = 0;
        for (int time : timestamps) {
            String sourceFile = sourceFilePath + lambda + "_" + len + "_" + time + ".txt";
            HashMap<Integer, Cluster<Grid>> sourceClusters = getClusters(sourceFile);
            double res = 0.0;
            if (!standardFilePath.equals("")) {
                String standardFile = standardFilePath + lambda + "_" + len + "_" + time + ".txt";
                HashMap<Integer, Cluster<Grid>> standardClusters = getClusters(standardFile);
                if (metric.equals("Purity"))
                    res = getPurity(sourceClusters, standardClusters);
                else if (metric.equals("RandIndex"))
                    res = getRandIndex(sourceClusters, standardClusters);
            }
            else
                res = getSilhouetteCoefficient(sourceClusters);
            System.out.println("时刻" + time + "的"+metric+"：" +res);
            sum += res;
            results.put(time, res);
        }
        System.out.println("平均"+metric + "是" + (sum / results.size()));
        //writeToFile(results, metric);

    }

    private void writeToFile(HashMap<Integer, Double> results, String metric) throws IOException {
        String purityPath = sourceFilePath + lambda + "_" + len + "_" + metric + ".txt";
        BufferedWriter bw = new BufferedWriter(new FileWriter(purityPath));
        for (Map.Entry<Integer, Double> entry : results.entrySet()) {
            String line = entry.getKey() + "," + entry.getValue() + "\n";
            bw.write(line);
        }
        bw.close();

    }

    private void randIndexTestDemo() {
        HashMap<Integer, Cluster<Grid>> sourceClusters = new HashMap<>();
        for (int i = 0; i < 3; i++) {
            Cluster<Grid> cluster = new Cluster<>(0);
            if (i < 2) {
                for (int j = 0; j < 6; j++) {
                    int[] vec = new int[]{i, j};
                    Grid grid = new Grid(vec);
                    grid.setLabel(i);
                    cluster.addGrid(grid);
                }
            } else {
                for (int j = 0; j < 5; j++) {
                    int[] vec = new int[]{i, j};
                    Grid grid = new Grid(vec);
                    grid.setLabel(i);
                    cluster.addGrid(grid);
                }
            }
            sourceClusters.put(i, cluster);
        }

        HashMap<Integer, Cluster<Grid>> standardClusters = new HashMap<>();
        for (int i = 0; i < 3; i++) {
            Cluster<Grid> cluster = new Cluster<>(i);
            if (i == 0) {
                for (int j = 0; j < 5; j++) {
                    int[] vec = new int[]{i, j};
                    Grid grid = new Grid(vec);
                    grid.setLabel(i);
                    cluster.addGrid(grid);
                }
                int[] vec1 = new int[]{1, 0};
                int[] vec2 = new int[]{2, 0};
                int[] vec3 = new int[]{2, 1};
                Grid grid1 = new Grid(vec1);
                grid1.setLabel(i);
                cluster.addGrid(grid1);
                Grid grid2 = new Grid(vec2);
                grid2.setLabel(i);
                cluster.addGrid(grid2);
                Grid grid3 = new Grid(vec3);
                grid3.setLabel(i);
                cluster.addGrid(grid3);
            } else if (i == 1) {
                for (int j = 2; j < 6; j++) {
                    int[] vec = new int[]{i, j};
                    Grid grid = new Grid(vec);
                    grid.setLabel(i);
                    cluster.addGrid(grid);
                }
                int[] vec1 = new int[]{0, 5};
                Grid grid1 = new Grid(vec1);
                grid1.setLabel(i);
                cluster.addGrid(grid1);
            } else {
                for (int j = 2; j < 5; j++) {
                    int[] vec = new int[]{i, j};
                    Grid grid = new Grid(vec);
                    grid.setLabel(i);
                    cluster.addGrid(grid);
                }
                int[] vec1 = new int[]{1, 1};
                Grid grid1 = new Grid(vec1);
                grid1.setLabel(i);
                cluster.addGrid(grid1);
            }
            standardClusters.put(i, cluster);
        }

    }


    public static void main(String[] args) throws IOException {
        String source = "C:\\Users\\Celeste\\Desktop\\data\\result\\GDMC\\synthetic\\";
        String standard = "C:\\Users\\Celeste\\Desktop\\data\\result\\Standard\\gdmc\\synthetic\\";

        //String source = "C:\\Users\\Celeste\\Desktop\\data\\result\\ESA\\synthetic\\";
        //String standard = "C:\\Users\\Celeste\\Desktop\\data\\result\\Standard\\esa\\synthetic\\";
        //String standard = "";

        /*
        Purity
        RandIndex
        SilhouetteCoefficient
         */
        Metric pc = new Metric(source, standard, "Purity", 2, 0.998, 0.3);
        pc.process();



    }
}

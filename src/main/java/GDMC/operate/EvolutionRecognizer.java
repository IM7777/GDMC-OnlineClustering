package GDMC.operate;

import GDMC.model.Cluster;
import GDMC.model.Grid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static GDMC.util.Functions.swapKV;

/**
 * Created by jxm on 2021/7/23.
 */
public class EvolutionRecognizer {
    private HashMap<Integer, Cluster> latestClusters;
    private HashMap<Integer, Cluster> currentClusters;
    private HashSet<Integer> currentSet;
    //存储的是已消失的latestCluster的label
    private ArrayList<Integer> deadList;

    //存储的是新增的currentCluster的label
    private ArrayList<Integer> birthList;

    //存储的是仍活着的currentCluster的label，表明其在latesteCluster里也有存在过
    private ArrayList<Integer> survivalList;

    private HashMap<Cluster, Integer> sizeTransitionList = new HashMap<>();
    private HashMap<Cluster, Integer> compactTransitionList = new HashMap<>();

    /*
    key: latestCluster的label
    value: currentCluster的label列表
    表示的是latestCluster分裂成了哪些currentClusters
     */
    private Map<Integer, ArrayList<Integer>> splitList;
    /*
    key: currentCluster的label
    value: latestCluster的label列表
    表示的是currentCluster是由哪些latestClusters合并而来的
     */
    private Map<Integer, ArrayList<Integer>> mergeList;

    // match
    private double tau = 0.5;
    private double tauSplit = 0.2;
    // size
    private double varepsilon = 50;
    // compact
    private double delta = 0.5;


    public EvolutionRecognizer(HashMap<Integer, Cluster> latestClusters, HashMap<Integer, Cluster> currentClusters) {
        this.latestClusters = latestClusters;
        this.currentClusters = currentClusters;
    }

    private void initialoze() {
        currentSet = new HashSet<>();
        birthList = new ArrayList<>();
        deadList = new ArrayList<>();
        survivalList = new ArrayList<>();
        splitList = new HashMap<>();
        mergeList = new HashMap<>();
    }
    
    public void process() {
        initialoze();
        externalTransition();
        StringBuilder sb = new StringBuilder();
        sb.append("数据演化形式有：");
        if (birthList.size() > 0) {
            sb.append("新增").append(birthList.size()).append("个聚类 ");
        }
        if (deadList.size() > 0) {
            sb.append("消失").append(deadList.size()).append("个聚类 ");
        }
        if (splitList.size() > 0) {
            sb.append("分裂 ");
        }
        if (mergeList.size() > 0) {
            sb.append("合并 ");
        }
        if (survivalList.size() > 0) {
            sb.append(survivalList.size()).append("个聚类仍在");
        }
        System.out.println(sb.toString());
    }

    private double getOverlap(Cluster x, Cluster y) {
        ArrayList<Grid> xGrids = x.getGrids();
        ArrayList<Grid> yGrids = y.getGrids();
        ArrayList<Grid> jointGrids = new ArrayList<>();
        double jointDensity = 0.0;
        double xDensity = 0.0;
        for (Grid xGrid : xGrids) {
            int index = yGrids.indexOf(xGrid);
            if (index != -1) {
                Grid yGrid = yGrids.get(index);
                jointDensity += yGrid.getDensity();
            }
            xDensity += xGrid.getDensity();
        }
        return jointDensity / xDensity;
    }

    /*
    分裂分析
    返回HashMap<Integer, Integer> mergeOrSurvival
    key: latestCluster的label
    value: 最佳候选的currentCluster的label
     */
    private HashMap<Integer, Integer> splitAnalyze() {
        HashMap<Integer, Integer> survivals = new HashMap<>();
        for (Map.Entry<Integer, Cluster> latestEntry : latestClusters.entrySet()) {
            int xLabel = latestEntry.getKey();
            Cluster x = latestEntry.getValue();
            ArrayList<Integer> splitUnion = new ArrayList<>();
            int survivalCandicate = -1;
            double overlapMax = 0.0;

            for (Map.Entry<Integer, Cluster> currentEntry : currentClusters.entrySet()) {
                Cluster y = currentEntry.getValue();
                int yLabel = currentEntry.getKey();
                double overlap = getOverlap(x, y);
                if (overlap >= tau) {
                    if (overlap > overlapMax) {
                        overlapMax = overlap;
                        survivalCandicate = yLabel;
                    }
                } else if (overlap >= tauSplit) {
                    splitUnion.add(yLabel);
                }
            }

            if (splitUnion.isEmpty() && survivalCandicate == -1) {
                this.deadList.add(xLabel);
            }
            else if (!splitUnion.isEmpty()) {
                Cluster splitCluster = combineClusters(splitUnion, currentClusters);
                if (getOverlap(x, splitCluster) >= tau) {
                    this.splitList.put(xLabel, splitUnion);
                    currentSet.addAll(splitUnion);
                }
                //这里需要注意一下，和它匹配的那个currentCluster需要如何进行分析
                else {
                    this.deadList.add(xLabel);
                }
            }
            //分裂集合为空，且存在survivalCandicate
            else {
                survivals.put(xLabel, survivalCandicate);
            }
        }
        return survivals;
    }

    /*
    合并分析
     */
    private void mergeAnalyze(HashMap<Integer, ArrayList<Integer>> mergeOrSurvival) {
        for (Map.Entry<Integer, Cluster> currentEntry : currentClusters.entrySet()) {
            Cluster y = currentEntry.getValue();
            int yLabel = currentEntry.getKey();
            currentSet.add(yLabel);
            if (mergeOrSurvival.containsKey(yLabel)) {
                ArrayList<Integer> list = mergeOrSurvival.get(yLabel);
                if (list.size() == 1) {
                    this.survivalList.add(yLabel);
                } else if (list.size() > 1) {
                    this.mergeList.put(yLabel, list);
                }
            }
        }
    }

    private void externalTransition() {
        //分裂+消失分析
        HashMap<Integer, Integer> survivals = splitAnalyze();

        //合并+存活分析
        HashMap<Integer, ArrayList<Integer>> mergeOrSurvival = swapKV(survivals);
        if (mergeOrSurvival.size()>0)
            mergeAnalyze(mergeOrSurvival);

        //新增分析
        birthAnalyze();

    }

    private void birthAnalyze() {
        for (Integer yLabel : currentClusters.keySet()) {
            if (!currentSet.contains(yLabel)) {
                birthList.add(yLabel);
            }
        }
    }

    private Cluster combineClusters(ArrayList<Integer> list, HashMap<Integer, Cluster> clusters) {
        Cluster cluster = new Cluster();
        for (Integer integer : list) {
            cluster.merge(clusters.get(integer));
        }
        return cluster;
    }

    public void sizeTransition(Cluster x, Cluster y) {
        double density_x = x.getDensity();
        double density_y = y.getDensity();
        double deltaDensity = Math.abs(density_x - density_y);
        if (deltaDensity >= varepsilon) {
            if (density_x - density_y > 0)
                sizeTransitionList.put(x, -1);
            else
                sizeTransitionList.put(x, 1);
        }
    }

    public void compactTransition(Cluster x, Cluster y) {
        double standardDeviation_x = x.getStandardDeviation();
        double standardDeviation_y = y.getStandardDeviation();
        double deltaStandardDeviation = Math.abs(standardDeviation_x - standardDeviation_y);
        if (deltaStandardDeviation >= delta) {
            if (standardDeviation_x - standardDeviation_y > 0)
                compactTransitionList.put(x, 1);
            else
                compactTransitionList.put(x, -1);
        }
    }

    


}

package standard.operate;

import standard.model.StdCluster;
import standard.model.StdGrid;

import java.util.ArrayList;
import java.util.HashMap;

public class ClusterProcessor {
    public ArrayList<StdGrid> grids;

    public ClusterProcessor(ArrayList<StdGrid> grids) {
        this.grids = grids;
    }

    public HashMap<Integer, StdCluster> getClusters() {
        HashMap<Integer, StdCluster> clusters = new HashMap<>();
        for (StdGrid grid : grids) {
            int label = grid.getLabel();
            if (label != -1) {
                StdCluster cluster = clusters.getOrDefault(label, new StdCluster(label));
                cluster.addGrid(grid);
                clusters.put(label, cluster);
            }
        }
        return clusters;
    }


}

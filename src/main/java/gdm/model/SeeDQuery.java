import gdm.*;

public class SeeD {

    // --- Data Structures ---

    // Trajectory point
    static class TrajPoint {
        int tid;
        long timestamp;
        double x, y;
        TrajPoint(int tid, long timestamp, double x, double y) {
            this.tid = tid;
            this.timestamp = timestamp;
            this.x = x;
            this.y = y;
        }
        double distance(TrajPoint other) {
            return Math.hypot(x - other.x, y - other.y);
        }
    }

    // Trajectory
    static class Trajectory {
        int tid;
        List<TrajPoint> points = new ArrayList<>();
        int cluster = -1;
        Trajectory(int tid) { this.tid = tid; }
        double avgX() { return points.stream().mapToDouble(p->p.x).average().orElse(0); }
        double avgY() { return points.stream().mapToDouble(p->p.y).average().orElse(0); }
        double euclidean(Trajectory other) {
            double sum = 0;
            int len = Math.min(points.size(), other.points.size());
            for (int i = 0; i < len; i++) {
                sum += points.get(i).distance(other.points.get(i));
            }
            return len > 0 ? sum / len : Double.MAX_VALUE;
        }
        @Override
        public String toString() {
            return "TID " + tid + " [C" + cluster + "] " + points.size() + "pts";
        }
    }

    // --- Grid-based Clustering ---

    static class GridClusterer {
        List<Trajectory> trajs;
        double gridSize;
        int minPoints;
        Map<String, List<Trajectory>> grid = new HashMap<>();
        Map<Integer, Integer> clusterHistory = new HashMap<>(); // tid -> cluster at last epoch

        GridClusterer(List<Trajectory> trajs, double gridSize, int minPoints) {
            this.trajs = trajs;
            this.gridSize = gridSize;
            this.minPoints = minPoints;
        }

        String gridKey(Trajectory t) {
            int gx = (int)Math.floor(t.avgX()/gridSize);
            int gy = (int)Math.floor(t.avgY()/gridSize);
            return gx+","+gy;
        }

        void assignToGrid() {
            grid.clear();
            for (Trajectory t : trajs) {
                String key = gridKey(t);
                grid.computeIfAbsent(key, k->new ArrayList<>()).add(t);
            }
        }

        void cluster() {
            assignToGrid();
            int cid = 0;
            for (String key : grid.keySet()) {
                List<Trajectory> cell = grid.get(key);
                if (cell.size() >= minPoints) {
                    for (Trajectory t : cell) t.cluster = cid;
                    cid++;
                } else {
                    for (Trajectory t : cell) t.cluster = -1;
                }
            }
        }

        // Call this per epoch (e.g., per time window) to analyze evolution
        void evolutionAnalysis(int epoch) {
            System.out.println("Epoch " + epoch + " evolution:");
            int unchanged = 0, changed = 0, out = 0;
            for (Trajectory t : trajs) {
                int prev = clusterHistory.getOrDefault(t.tid, -2);
                if (t.cluster == prev) {
                    unchanged++;
                } else if (prev == -2) {
                    out++;
                } else {
                    changed++;
                }
                clusterHistory.put(t.tid, t.cluster);
            }
            System.out.printf("   Unchanged: %d, Changed: %d, New/Out: %d%n", unchanged, changed, out);
        }
    }

    // --- Similarity Query ---

    static class SimilarityQuery {
        List<Trajectory> trajs;

        SimilarityQuery(List<Trajectory> trajs) {
            this.trajs = trajs;
        }

        // Top-k Euclidean similarity in same cluster
        List<Trajectory> topK(Trajectory query, int k) {
            return trajs.stream()
                .filter(t -> t.cluster == query.cluster && t.tid != query.tid)
                .sorted(Comparator.comparingDouble(query::euclidean))
                .limit(k)
                .toList();
        }
    }

    // --- Data Loader ---

    static List<Trajectory> loadTraj(String filename) throws IOException {
        Map<Integer, Trajectory> map = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line=br.readLine())!=null) {
                if (line.trim().isEmpty() || line.startsWith("#")) continue;
                String[] sp = line.split("[,\\s]+");
                int tid = Integer.parseInt(sp[0]);
                long time = Long.parseLong(sp[1]);
                double x = Double.parseDouble(sp[2]);
                double y = Double.parseDouble(sp[3]);
                map.computeIfAbsent(tid, k->new Trajectory(tid))
                    .points.add(new TrajPoint(tid, time, x, y));
            }
        }
        return new ArrayList<>(map.values());
    }

    // --- Main Process ---

    public static void main(String[] args) throws Exception {
        // 1. Load trajectory data
        List<Trajectory> trajs = loadTraj("trajectories.csv");

        // 2. Clustering and evolution analysis over 3 epochs (simulate time evolution)
        double gridSize = 10.0;
        int minPoints = 2;
        GridClusterer clusterer = new GridClusterer(trajs, gridSize, minPoints);

        for (int epoch=1; epoch<=3; epoch++) {
            // (Simulate: use all data; in real use, filter by time window)
            clusterer.cluster();
            clusterer.evolutionAnalysis(epoch);
        }

        // 3. Similarity query
        SimilarityQuery sq = new SimilarityQuery(trajs);
        Trajectory query = trajs.get(0);
        List<Trajectory> sim = sq.topK(query, 3);
        System.out.println("\nTop-3 similar trajectories to " + query + ":");
        for (Trajectory t : sim) System.out.println(t);
    }
}

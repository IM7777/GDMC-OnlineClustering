import java.util.*;
import java.io.*;

public class CSQTrajectoryDemo {

    // Trajectory point: id, time, x, y
    static class TrajPoint {
        int tid; // trajectory id
        long timestamp; // unix ms
        double x, y;
        TrajPoint(int tid, long timestamp, double x, double y) {
            this.tid = tid;
            this.timestamp = timestamp;
            this.x = x;
            this.y = y;
        }
        double distance(TrajPoint other) {
            return Math.sqrt((x-other.x)*(x-other.x)+(y-other.y)*(y-other.y));
        }
    }

    // Trajectory: list of points
    static class Trajectory {
        int tid;
        List<TrajPoint> points = new ArrayList<>();
        int cluster = -1;
        Trajectory(int tid) { this.tid = tid; }
        // For grid clustering, use average x, y
        double avgX() { return points.stream().mapToDouble(p->p.x).average().orElse(0); }
        double avgY() { return points.stream().mapToDouble(p->p.y).average().orElse(0); }
        // For Euclidean: simple Hausdorff distance (max-min of all points)
        double euclidean(Trajectory other) {
            double sum = 0;
            int len = Math.min(points.size(), other.points.size());
            for (int i = 0; i < len; i++) {
                sum += points.get(i).distance(other.points.get(i));
            }
            return sum / len;
        }
        // LCSS
        int lcss(Trajectory other, double epsilon, long delta) {
            int m = points.size(), n = other.points.size();
            int[][] dp = new int[m+1][n+1];
            for (int i=1; i<=m; i++) {
                for (int j=1; j<=n; j++) {
                    TrajPoint a = points.get(i-1), b = other.points.get(j-1);
                    if (Math.abs(a.x-b.x)<=epsilon && Math.abs(a.y-b.y)<=epsilon
                        && Math.abs(a.timestamp-b.timestamp)<=delta)
                        dp[i][j]=dp[i-1][j-1]+1;
                    else
                        dp[i][j]=Math.max(dp[i-1][j], dp[i][j-1]);
                }
            }
            return dp[m][n];
        }
        // DTW
        double dtw(Trajectory other) {
            int m = points.size(), n = other.points.size();
            double[][] dp = new double[m+1][n+1];
            for (int i=0; i<=m; i++) Arrays.fill(dp[i], Double.POSITIVE_INFINITY);
            dp[0][0]=0;
            for (int i=1; i<=m; i++)
                for (int j=1; j<=n; j++) {
                    double cost = points.get(i-1).distance(other.points.get(j-1));
                    dp[i][j] = cost + Math.min(Math.min(dp[i-1][j], dp[i][j-1]), dp[i-1][j-1]);
                }
            return dp[m][n];
        }
        @Override
        public String toString() {
            return "TID "+tid+" [C"+cluster+"] "+points.size()+"pts";
        }
    }

    // CSQ for trajectory
    static class CSQ {
        List<Trajectory> trajs;
        double gridSize;
        int minPoints;
        Map<String, List<Trajectory>> grid = new HashMap<>();

        CSQ(List<Trajectory> trajs, double gridSize, int minPoints) {
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
            for (Trajectory t : trajs) {
                String key = gridKey(t);
                grid.computeIfAbsent(key, k->new ArrayList<>()).add(t);
            }
        }

        void performClustering() {
            assignToGrid();
            int cid = 0;
            Set<String> visited = new HashSet<>();
            for (String key : grid.keySet()) {
                List<Trajectory> cell = grid.get(key);
                if (cell.size()>=minPoints && !visited.contains(key)) {
                    for (Trajectory t : cell) t.cluster = cid;
                    visited.add(key);
                    cid++;
                }
            }
        }

        // Top-k query
        List<Trajectory> topKQuery(Trajectory q, int k, String distType, double lcssEps, long lcssDelta) {
            Comparator<Trajectory> cmp;
            if ("euclidean".equalsIgnoreCase(distType)) {
                cmp = Comparator.comparingDouble(t->q.euclidean(t));
            } else if ("lcss".equalsIgnoreCase(distType)) {
                cmp = Comparator.comparingInt((Trajectory t)->-q.lcss(t, lcssEps, lcssDelta));
            } else if ("dtw".equalsIgnoreCase(distType)) {
                cmp = Comparator.comparingDouble(q::dtw);
            } else throw new IllegalArgumentException("Unknown distType");
            return trajs.stream()
                .filter(t->t.cluster==q.cluster && t!=q)
                .sorted(cmp)
                .limit(k)
                .toList();
        }
    }

    // Load trajectories from CSV: tid,timestamp,x,y
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

    public static void main(String[] args) throws IOException {
        // Example: Use "trajectories.csv" as dataset, format: tid,timestamp,x,y
        List<Trajectory> trajs = loadTraj("trajectories.csv");

        CSQ csq = new CSQ(trajs, 10.0, 2); // grid size=10, minPoints=2
        csq.performClustering();

        System.out.println("Loaded "+trajs.size()+" trajectories, clusters:");
        for (Trajectory t : trajs)
            System.out.println(t);

        // Query: pick any trajectory
        Trajectory query = trajs.get(0);

        System.out.println("\nTop-3 Euclidean:");
        for (Trajectory t : csq.topKQuery(query, 3, "euclidean", 0, 0))
            System.out.println(t);

        System.out.println("\nTop-3 LCSS (eps=2, delta=1000ms):");
        for (Trajectory t : csq.topKQuery(query, 3, "lcss", 2.0, 1000))
            System.out.println(t);

        System.out.println("\nTop-3 DTW:");
        for (Trajectory t : csq.topKQuery(query, 3, "dtw", 0, 0))
            System.out.println(t);
    }
}

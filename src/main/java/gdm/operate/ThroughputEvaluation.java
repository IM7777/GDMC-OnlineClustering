import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class ThroughputEvaluation {

    // Trajectory point structure
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
    }

    // Trajectory object (for streaming, points arrive in order)
    static class Trajectory {
        int tid;
        List<TrajPoint> points = new ArrayList<>();
        int cluster = -1;
        Trajectory(int tid) { this.tid = tid; }
        double avgX() { return points.stream().mapToDouble(p->p.x).average().orElse(0); }
        double avgY() { return points.stream().mapToDouble(p->p.y).average().orElse(0); }
    }

    // Streaming clusterer with grid density
    static class StreamingGridClusterer {
        double gridSize;
        int minPoints;
        Map<String, List<Trajectory>> grid = new ConcurrentHashMap<>();
        Map<Integer, Trajectory> activeTrajs = new ConcurrentHashMap<>();
        AtomicLong clusterCounter = new AtomicLong(0);

        StreamingGridClusterer(double gridSize, int minPoints) {
            this.gridSize = gridSize;
            this.minPoints = minPoints;
        }

        String gridKey(double x, double y) {
            int gx = (int) Math.floor(x / gridSize);
            int gy = (int) Math.floor(y / gridSize);
            return gx + "," + gy;
        }

        void processPoint(TrajPoint p) {
            Trajectory traj = activeTrajs.computeIfAbsent(p.tid, Trajectory::new);
            traj.points.add(p);
            String key = gridKey(p.x, p.y);
            grid.computeIfAbsent(key, k -> Collections.synchronizedList(new ArrayList<>()));
            // If this is the first point, cluster assignment
            if (traj.points.size() == 1) {
                List<Trajectory> cell = grid.get(key);
                cell.add(traj);
                if (cell.size() >= minPoints && traj.cluster == -1) {
                    long cid = clusterCounter.getAndIncrement();
                    for (Trajectory t : cell) {
                        if (t.cluster == -1) t.cluster = (int) cid;
                    }
                }
            }
        }

        int getCurrentClusterCount() {
            Set<Integer> clusters = new HashSet<>();
            for (List<Trajectory> cell : grid.values()) {
                for (Trajectory t : cell) {
                    if (t.cluster != -1) clusters.add(t.cluster);
                }
            }
            return clusters.size();
        }
    }

    // Synthetic streaming data generator
    static Iterator<TrajPoint> syntheticStream(int totalTrajs, int pointsPerTraj, double spread, long startTimestamp, long timeStep) {
        List<TrajPoint> all = new ArrayList<>();
        Random rand = new Random(42);
        for (int tid = 0; tid < totalTrajs; tid++) {
            double baseX = rand.nextDouble() * spread, baseY = rand.nextDouble() * spread;
            for (int i = 0; i < pointsPerTraj; i++) {
                double x = baseX + rand.nextGaussian();
                double y = baseY + rand.nextGaussian();
                long ts = startTimestamp + i * timeStep;
                all.add(new TrajPoint(tid, ts, x, y));
            }
        }
        Collections.shuffle(all, rand);
        return all.iterator();
    }

    public static void main(String[] args) {
        int totalTrajs = 10000;       // number of trajectories
        int pointsPerTraj = 10;       // points per trajectory
        double gridSize = 5.0;        // grid cell size
        int minPoints = 3;            // min points for dense cell
        double spread = 100.0;        // spatial spread
        long startTimestamp = 1_000_000L;
        long timeStep = 1000L;

        StreamingGridClusterer clusterer = new StreamingGridClusterer(gridSize, minPoints);
        Iterator<TrajPoint> stream = syntheticStream(totalTrajs, pointsPerTraj, spread, startTimestamp, timeStep);

        long totalPoints = totalTrajs * pointsPerTraj;
        long reportEvery = 100_000;
        long processed = 0;

        long startTime = System.currentTimeMillis();

        while (stream.hasNext()) {
            TrajPoint p = stream.next();
            clusterer.processPoint(p);
            processed++;
            if (processed % reportEvery == 0) {
                long now = System.currentTimeMillis();
                double elapsed = (now - startTime) / 1000.0;
                double throughput = processed / elapsed;
                System.out.printf("Processed %,d points in %.2f sec (%.2f points/sec); Current cluster count: %d%n",
                        processed, elapsed, throughput, clusterer.getCurrentClusterCount());
            }
        }
        long endTime = System.currentTimeMillis();
        double totalSec = (endTime - startTime) / 1000.0;
        double throughput = processed / totalSec;

        System.out.printf("Final: Processed %,d points in %.2f sec (%.2f points/sec); Total clusters: %d%n",
                processed, totalSec, throughput, clusterer.getCurrentClusterCount());
    }
}

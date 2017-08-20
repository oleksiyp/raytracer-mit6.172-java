package raytracer;

public class StopWatch {
    private static long total = 0;
    private static long t;

    public static void reset() {
        total = 0;
    }

    public static long get() {
        return total;
    }

    public static void start() {
        t = System.nanoTime();
    }

    public static void end() {
        total += System.nanoTime() - t;
    }
}

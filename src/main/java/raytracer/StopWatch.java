package raytracer;

import static java.lang.ThreadLocal.withInitial;

public class StopWatch {
    private static final ThreadLocal<StopWatch> STOP_WATCH_THREAD_LOCAL = withInitial(StopWatch::new);

    private long total = 0;
    private int n;
    private long t;

    public static StopWatch tl() {
        return STOP_WATCH_THREAD_LOCAL.get();
    }

    public static void swReset() {
        tl().reset();
    }

    public static long swTotalT() {
        return tl().totalT();
    }

    public static int swTotalN() {
        return tl().totalN();
    }

    public static void swStart() {
        tl().start();
    }

    public static void swEnd() {
        tl().end();
    }

    public void reset() {
        total = 0;
        n = 0;
    }

    public long totalT() {
        return total;
    }

    public int totalN() {
        return n;
    }

    public void start() {
        t = System.nanoTime();
    }

    public void end() {
        total += System.nanoTime() - t;
        n++;
    }
}

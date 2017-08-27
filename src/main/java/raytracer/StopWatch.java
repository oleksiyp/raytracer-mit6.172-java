package raytracer;

import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static java.lang.ThreadLocal.withInitial;

public class StopWatch {
    private static final ThreadLocal<Map<String, StopWatch>> STOP_WATCH_THREAD_LOCAL = withInitial(TreeMap::new);

    private long total = 0;
    private int n;
    private long t;
    private String name;
    private int lvl;

    public StopWatch(String name) {
        this.name = name;
    }

    public static StopWatch sw(String name) {
        return STOP_WATCH_THREAD_LOCAL.get().computeIfAbsent(name, StopWatch::new);
    }

    public static void swReset() {
        STOP_WATCH_THREAD_LOCAL.get().forEach((k, v) -> v.reset());
    }

    public static long swTotalT() {
        return sw("general").totalT();
    }

    public static int swTotalN() {
        return sw("general").totalN();
    }

    public static void swStart() {
        sw("general").start();
    }

    public static void swEnd() {
        sw("general").end();
    }

    public void reset() {
        total = 0;
        n = 0;
        lvl = 0;
    }

    public long totalT() {
        return total;
    }

    public int totalN() {
        return n;
    }

    public void start() {
        if (lvl == 0) {
            t = System.nanoTime();
        }
        lvl++;
    }

    public void end() {
        lvl--;
        if (lvl == 0) {
            total += System.nanoTime() - t;
            n++;
        }
    }

    public void tick() {
        if (lvl == 0) {
            n++;
        }
    }

    @Override
    public String toString() {
        return String.format("%s{n=%d, total=%d}", name, n, total);
    }

    public static void report(Formatter fmt, double all, String ...names) {
        Map<String, StopWatch> hm = STOP_WATCH_THREAD_LOCAL.get();
        if (names.length == 0) {
            names = hm.keySet().toArray(new String[0]);
        }
        for (String name : names) {
            StopWatch sw = hm.get(name);
            if (sw == null) {
                fmt.format("%s-", name);
                continue;
            }
            double tf = sw.total / 1e9;
            fmt.format("%s{%6.3f %5.1f%% %6d} ", name, tf, tf / all * 100.0, sw.n);
        }
    }
}

package raytracer;

import java.util.Formatter;
import java.util.Map;
import java.util.TreeMap;

import static java.lang.ThreadLocal.withInitial;
import static raytracer.StopWatch.TL.MAP;

public interface StopWatch {
    DummyStopWatch DUMMY_STOP_WATCH = new DummyStopWatch();

    class TL {
        static ThreadLocal<Map<String, StopWatch>> MAP = withInitial(TreeMap::new);
    }

    static void off() {
        MAP = null;
    }

    static StopWatch sw(String name) {
        if (MAP == null) {
            return DUMMY_STOP_WATCH;
        }
        return MAP.get().computeIfAbsent(name, StopWatchImpl::new);
    }

    static void swReset() {
        if (MAP == null) {
            return;
        }
        MAP.get().forEach((k, v) -> v.reset());
    }

    static long swTotalT() {
        return sw("general").totalT();
    }

    static int swTotalN() {
        return sw("general").totalN();
    }

    static void swStart() {
        sw("general").start();
    }

    static void swEnd() {
        sw("general").end();
    }

    static void report(Formatter fmt, double all, String... names) {
        if (MAP == null) {
            return;
        }
        Map<String, StopWatch> hm = MAP.get();
        if (names.length == 0) {
            names = hm.keySet().toArray(new String[0]);
        }
        for (String name : names) {
            StopWatch sw = hm.get(name);
            if (sw == null) {
                fmt.format("%s-", name);
                continue;
            }
            double tf = sw.getTotal() / 1e9;
            fmt.format("%s{%6.3f %5.1f%% %6d} ", name, tf, tf / all * 100.0, sw.getN());
        }
    }

    void reset();

    long totalT();

    int totalN();

    void start();

    void end();

    void tick();

    long getTotal();

    int getN();

}


class StopWatchImpl implements StopWatch {
    private final String name;
    private long total;
    private int n;
    private long t;
    private int lvl;

    public StopWatchImpl(String name) {
        this.name = name;
        total = 0;
    }

    @Override
    public void reset() {
        total = 0;
        n = 0;
        lvl = 0;
    }

    @Override
    public long totalT() {
        return total;
    }

    @Override
    public int totalN() {
        return n;
    }

    @Override
    public void start() {
        if (lvl == 0) {
            t = System.nanoTime();
        }
        lvl++;
    }

    @Override
    public void end() {
        lvl--;
        if (lvl == 0) {
            total += System.nanoTime() - t;
            n++;
        }
    }

    @Override
    public void tick() {
        if (lvl == 0) {
            n++;
        }
    }

    @Override
    public long getTotal() {
        return total;
    }

    @Override
    public int getN() {
        return n;
    }

    @Override
    public String toString() {
        return String.format("%s{n=%d, total=%d}", name, n, total);
    }

}

class DummyStopWatch implements StopWatch {
    @Override
    public void reset() {

    }

    @Override
    public long totalT() {
        return 0;
    }

    @Override
    public int totalN() {
        return 0;
    }

    @Override
    public void start() {

    }

    @Override
    public void end() {

    }

    @Override
    public void tick() {

    }

    @Override
    public long getTotal() {
        return 0;
    }

    @Override
    public int getN() {
        return 0;
    }
}

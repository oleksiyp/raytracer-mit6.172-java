package raytracer;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.lang.Math.*;
import static raytracer.Fixed.*;
import static raytracer.TripleI.zero;

public class IrradianceCache {
    private static final double ICACHE_MAX_SPACING_RATIO = 100.0;

    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    final int tolerance;
    final double minSpacing;
    final int invTolerance;
    final double maxSpacing;
    final int[] vals;
    int freeSample = 0;
    private Lock writeLock = rwLock.writeLock();
    private Lock readLock = rwLock.readLock();

    public IrradianceCache(double tolerance, double minSpacing) {
        this.tolerance = toFixed(tolerance);
        this.minSpacing = minSpacing;
        invTolerance = toFixed(1.0 / tolerance);
        maxSpacing = ICACHE_MAX_SPACING_RATIO * minSpacing;

        vals = new int[10 * 100000];
    }

    public void insert(Point3D pos, Vector3D norm, double r0, Colour irr) {
        r0 = clamp(r0 * tolerance, minSpacing, maxSpacing);

        writeLock.lock();
        try {
            int i = freeSample;
            freeSample += 10;
            vals[i] = toFixed(pos.x);
            vals[i + 1] = toFixed(pos.y);
            vals[i + 2] = toFixed(pos.z);
            vals[i + 3] = toFixed(norm.x);
            vals[i + 4] = toFixed(norm.y);
            vals[i + 5] = toFixed(norm.z);
            vals[i + 6] = toFixed(r0);
            vals[i + 7] = toFixed(irr.r);
            vals[i + 8] = toFixed(irr.g);
            vals[i + 9] = toFixed(irr.b);
        } finally {
            writeLock.unlock();
        }
    }

    private double clamp(double val, double low, double high) {
        return min(high, max(val, low));
    }

    TripleI pt = zero();
    TripleI vec = zero();
    TripleI col = zero();

    public boolean getIrradiance(Point3D pos, Vector3D norm, Colour col) {
        boolean hasIrr = false;
        int weight = 0;
        int r = 0, g = 0, b = 0;

        pt.p(pos);
        vec.v(norm);

        readLock.lock();
        try {
            for (int i = 0; i < freeSample; i += 10) {
                int wi = weight2(i, pt, vec);
                if (wi > invTolerance) {
                    wi = (int) sqrt(wi << DENOM_BITS);
                    r += mul(vals[i + 7], wi);
                    g += mul(vals[i + 8], wi);
                    b += mul(vals[i + 9], wi);
                    hasIrr = true;
                    weight += wi;
                }
            }
        } finally {
            readLock.unlock();
        }

        if (!hasIrr) {
            return false;
        }

        col.c(r, g, b);
        col.multiply(1.0 / weight);

        return true;
    }

    public int weight2(int idx, TripleI xPos, TripleI xNorm) {
        int vx = vals[idx] - xPos.a;
        int vy = vals[idx + 1] - xPos.b;
        int vz = vals[idx + 2] - xPos.c;

        int r0 = vals[6];
        int r = mul(r0, tolerance);
        if (abs(vx) < r && abs(vy) < r && abs(vz) < r) {
            int a = dot(xNorm.a, vals[idx + 3], xNorm.b, vals[idx + 4], xNorm.c, vals[idx + 5]);
            int d2 = dot(vx, vx, vy, vy, vz, vz);
            int rr = div((int) sqrt(d2 << DENOM_BITS), r0);
            rr += (int)sqrt((DENOM - a) << DENOM_BITS);
            if (rr == 0) {
                return DENOM_SQ;
            }
            return DENOM_SQ / rr;
        } else {
            return 0;
        }
    }

}

package raytracer;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.lang.Math.*;
import static raytracer.Fixed.*;

public class IrradianceCache {
    private static final double ICACHE_MAX_SPACING_RATIO = 100.0;

    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    final int tolerance;
    final double minSpacing;
    final int invTolerance;
    final double maxSpacing;
    final int[] vals;
    int freeSample = 0;

    public IrradianceCache(double tolerance, double minSpacing) {
        this.tolerance = toFixed(tolerance);
        this.minSpacing = minSpacing;
        invTolerance = toFixed(1.0 / tolerance);
        maxSpacing = ICACHE_MAX_SPACING_RATIO * minSpacing;

        vals = new int[10 * 100000];
    }

    public void insert(Point3D pos, Vector3D norm, double r0, Colour irr) {
        r0 = clamp(r0 * tolerance, minSpacing, maxSpacing);

        if (freeSample == vals.length) {

        } else {
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
        }
    }

    private double clamp(double val, double low, double high) {
        return min(high, max(val, low));
    }

    Point3Di pt = new Point3Di(0, 0, 0);
    Vector3Di vec = new Vector3Di(0, 0, 0);
    public boolean getIrradiance(Point3D pos, Vector3D norm, Colour col) {
        boolean hasIrr = false;
        int weight = 0;
        int r = 0, g = 0, b = 0;

        Fixed.point(pos, pt);
        Fixed.vector(norm, vec);

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

        if (!hasIrr) {
            return false;
        }

        col.c(r, g, b);
        col.multiply(1.0 / weight);

        return true;
    }

    public int weight2(int idx, Point3Di xPos, Vector3Di xNorm) {
        int vx = vals[idx] - xPos.x;
        int vy = vals[idx + 1] - xPos.y;
        int vz = vals[idx + 2] - xPos.z;

        int r0 = vals[6];
        int r = mul(r0, tolerance);
        if (abs(vx) < r && abs(vy) < r && abs(vz) < r) {
            int a = dot(xNorm.x, vals[idx + 3], xNorm.y, vals[idx + 4], xNorm.z, vals[idx + 5]);
            int d2 = dot(vx, vx, vy, vy, vz, vz);
            int s1 = (d2 << Fixed.DENOM_BITS) / mul(r0, r0);
            int s2 = DENOM - a;
            int sq12 = (int) sqrt(((d2 * (DENOM - a)) << DENOM_BITS) / (r0 * r0));
            int dd = s1 + 2 * sq12 + s2;
            if (dd == 0) {
                return DENOM * DENOM;
            }
            return (DENOM * DENOM) / dd;
        } else {
            return 0;
        }
    }
}

package raytracer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.lang.Math.*;

public class IrradianceCache {
    private static final double ICACHE_MAX_SPACING_RATIO = 100.0;

    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();

    final List<Sample> samples;
    final double tolerance;
    final double minSpacing;
    final double invTolerance;
    final double maxSpacing;

    public IrradianceCache(double tolerance, double minSpacing) {
        this.tolerance = tolerance;
        this.minSpacing = minSpacing;
        invTolerance = 1.0 / tolerance;
        maxSpacing = ICACHE_MAX_SPACING_RATIO * minSpacing;

        samples = new ArrayList<>();
    }

    public void insert(Point3D pos, Vector3D norm, double r0, Colour irr) {
        r0 = clamp(r0 * tolerance, minSpacing, maxSpacing);

        writeLock.lock();
        try {
            samples.add(new Sample(pos, norm, r0, r0 * tolerance, irr));
        } finally {
            writeLock.unlock();
        }
    }

    private double clamp(double val, double low, double high) {
        return min(high, max(val, low));
    }

    public Colour getIrradiance(Point3D pos, Vector3D norm) {
        double weight = 0.0;
        boolean hasIrr = false;
        double r = 0, g = 0, b = 0;

        readLock.lock();
        try {
            for (int i = 0; i < samples.size(); i++) {
                Sample s = samples.get(i);
                double wi = s.weight2(pos, norm);
                wi = min(1e20, wi);
                if (wi > invTolerance) {
                    wi = sqrt(wi);
                    r += s.irr.r * wi;
                    g += s.irr.g * wi;
                    b += s.irr.b * wi;
                    hasIrr = true;
                    weight += wi;
                }
            }
        } finally {
            readLock.unlock();
        }

        if (!hasIrr) {
            return null;
        }

        return new Colour(r / weight, g / weight, b / weight);
    }

    @AllArgsConstructor
    @Getter
    @ToString
    private class Sample {
        final Point3D pos;
        final Vector3D norm;
        final double r0;
        final double r;
        final Colour irr;

        public double weight2(Point3D xPos, Vector3D xNorm) {
            double vx = pos.x - xPos.x;
            double vy = pos.y - xPos.y;
            double vz = pos.z - xPos.z;

            if (abs(vx) < r && abs(vy) < r && abs(vz) < r) {
                double a = xNorm.dot(norm);
                double d2 = vx * vx + vy * vy + vz * vz;
                double s1 = d2 / (r0 * r0);
                double s2 = 1.0 - a;
                double sq12 = sqrt(d2 * (1.0 - a) / (r0 * r0));
                return 1.0 / (s1 + 2 * sq12 + s2);
            } else {
                return 0;
            }
        }
    }
}

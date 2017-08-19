package raytracer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.*;

public class IrradianceCache {
    private static final double ICACHE_MAX_SPACING_RATIO = 100.0;

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

        samples = new ArrayList<Sample>();
    }

    public void insert(Point3D pos, Vector3D norm, double r0, Colour irr) {
        r0 = clamp(r0 * tolerance, minSpacing, maxSpacing);

        samples.add(new Sample(pos, norm, r0, irr));
    }

    private double clamp(double val, double low, double high) {
        return min(high, max(val, low));
    }

    public Colour getIrradiance(Point3D pos, Vector3D norm) {
        double weight = 0.0;
        boolean hasIrr = false;
        Colour irr = new Colour(0, 0, 0);

        for (Sample s : samples) {
            double wi = s.weight(pos, norm);
            wi = min(1e10, wi);
            if (wi > invTolerance) {
                irr = irr.add(s.irr.multiply(wi));
                hasIrr = true;
                weight += wi;
            }
        }

        if (!hasIrr) {
            return null;
        }

        return irr.divide(weight);
    }

    @AllArgsConstructor
    @Getter
    @ToString
    private class Sample {
        final Point3D pos;
        final Vector3D norm;
        final double r0;
        final Colour irr;

        public double weight(Point3D xPos, Vector3D xNorm) {
            Vector3D v = pos.subtract(xPos);

            if (abs(v.x) < (r0 * tolerance) &&
                    abs(v.y) < (r0 * tolerance) &&
                    abs(v.z) < (r0 * tolerance)) {
                double a = xNorm.dot(norm);
                double d = v.mag();
                double w = 1.0 / ((d * 1.0 / r0) + sqrt(1.0 - a));
                return w;
            } else
                return 0;
        }
    }
}

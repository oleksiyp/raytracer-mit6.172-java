package raytracer;

import static java.lang.Math.abs;

public class LinearPhotons {
    double[] vals;
    byte[] plane;

    public void init(Photon[] photons, int nStoredPhotons) {
        if (vals == null) {
            vals = new double[(nStoredPhotons + 1) * 9];
            plane = new byte[(nStoredPhotons + 1) * 9];
            for (int i = 1; i <= nStoredPhotons; i++) {
                vals[i * 9] = photons[i].pos.x;
                vals[i * 9 + 1] = photons[i].pos.y;
                vals[i * 9 + 2] = photons[i].pos.z;
                vals[i * 9 + 3] = photons[i].dir.x;
                vals[i * 9 + 4] = photons[i].dir.y;
                vals[i * 9 + 5] = photons[i].dir.z;
                vals[i * 9 + 6] = photons[i].power.r;
                vals[i * 9 + 7] = photons[i].power.g;
                vals[i * 9 + 8] = photons[i].power.b;
                plane[i] = (byte) photons[i].plane;
            }
        }
    }

    double dist(int idx, Vector3D normal, Point3D pos) {
        double distx1 = vals[idx * 9] - pos.x;
        double disty1 = vals[idx * 9 + 1] - pos.y;
        double distz1 = vals[idx * 9 + 2] - pos.z;

        double dist2 = distx1 * distx1 + disty1 * disty1 + distz1 * distz1;
        double discFix = normal.x * distx1 + normal.y * disty1 + normal.z * distz1;

        discFix = abs(discFix);
        dist2 *= discFix  * 0.010 + 1;
        return dist2;
    }

    public double dirDot(int idx, Vector3D normal) {
        return normal.x * vals[idx * 9 + 3] + normal.y * vals[idx * 9 + 4] + normal.z * vals[idx * 9 + 5];
    }

    public double powerR(int idx) {
        return vals[idx * 9 + 6];
    }
    public double powerG(int idx) {
        return vals[idx * 9 + 7];
    }
    public double powerB(int idx) {
        return vals[idx * 9 + 8];
    }

    public double coord(int idx, int plane) {
        return vals[idx * 9 + plane];
    }

    public int plane(int idx) {
        return plane[idx];
    }
}

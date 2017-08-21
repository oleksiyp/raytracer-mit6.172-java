package raytracer;

import static java.lang.Math.abs;

public class LinearPhotons {
    float[] vals;
    byte[] plane;

    public void init(Photon[] photons, int nStoredPhotons) {
        if (vals == null) {
            vals = new float[(nStoredPhotons + 1) * 9];
            plane = new byte[(nStoredPhotons + 1) * 9];
            for (int i = 1; i <= nStoredPhotons; i++) {
                vals[i * 9] = (float) photons[i].pos.x;
                vals[i * 9 + 1] = (float) photons[i].pos.y;
                vals[i * 9 + 2] = (float) photons[i].pos.z;
                vals[i * 9 + 3] = (float) photons[i].dir.x;
                vals[i * 9 + 4] = (float) photons[i].dir.y;
                vals[i * 9 + 5] = (float) photons[i].dir.z;
                vals[i * 9 + 6] = (float) photons[i].power.r;
                vals[i * 9 + 7] = (float) photons[i].power.g;
                vals[i * 9 + 8] = (float) photons[i].power.b;
                plane[i] = (byte) photons[i].plane;
            }
        }
    }

    float dist(int idx, Vector3Df normal, Point3Df pos) {
        float distx1 = vals[idx * 9] - pos.x;
        float disty1 = vals[idx * 9 + 1] - pos.y;
        float distz1 = vals[idx * 9 + 2] - pos.z;

        float dist2 = distx1 * distx1 + disty1 * disty1 + distz1 * distz1;
        float discFix = normal.x * distx1 + normal.y * disty1 + normal.z * distz1;

        discFix = abs(discFix);
        dist2 *= discFix  * 0.010 + 1;
        return dist2;
    }

    public float dirDot(int idx, Vector3Df normal) {
        return normal.x * vals[idx * 9 + 3] + normal.y * vals[idx * 9 + 4] + normal.z * vals[idx * 9 + 5];
    }

    public float powerR(int idx) {
        return vals[idx * 9 + 6];
    }
    public float powerG(int idx) {
        return vals[idx * 9 + 7];
    }
    public float powerB(int idx) {
        return vals[idx * 9 + 8];
    }

    public float coord(int idx, int plane) {
        return vals[idx * 9 + plane];
    }

    public int plane(int idx) {
        return plane[idx];
    }
}

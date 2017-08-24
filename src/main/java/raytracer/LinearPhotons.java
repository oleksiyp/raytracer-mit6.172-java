package raytracer;

import static java.lang.Float.max;
import static java.lang.Float.min;
import static java.lang.Math.abs;

public class LinearPhotons {
    public static final int DENOM = 255;
    public static final short DENOM_BITS = 8;
    int[] vals;
    byte[] plane;

    public void init(Photon[] photons, int nStoredPhotons) {
        if (vals == null) {
            vals = new int[(nStoredPhotons + 1) * 9];
            plane = new byte[(nStoredPhotons + 1) * 9];
            for (int i = 1; i <= nStoredPhotons; i++) {
                plane[i] = (byte) photons[i].plane;
                float[] f = {
                        (float) photons[i].pos.x,
                        (float) photons[i].pos.y,
                        (float) photons[i].pos.z,
                        (float) photons[i].dir.x,
                        (float) photons[i].dir.y,
                        (float) photons[i].dir.z,
                        (float) photons[i].power.r,
                        (float) photons[i].power.g,
                        (float) photons[i].power.b
                };
                for (int j = 0; j < 9; j++) {
                    vals[i * 9 + j] = toFixed(f[j]);
                }
            }
        }
    }

    public Point3Di pos(Point3D pt) {
        return new Point3Di(
                toFixed(pt.x),
                toFixed(pt.y),
                toFixed(pt.z));
    }

    public Vector3Di normal(Vector3D vec) {
        return new Vector3Di(
                toFixed(vec.x),
                toFixed(vec.y),
                toFixed(vec.z));
    }

    public ColourI colour(Colour col) {
        return new ColourI(
                toFixed(col.r),
                toFixed(col.g),
                toFixed(col.b));
    }

    private int toFixed(double val) {
        return (int) (val * DENOM);
    }

    int dist(int idx, Vector3Di normal, Point3Di pos) {
        int distx1 = vals[idx * 9] - pos.x;
        int disty1 = vals[idx * 9 + 1] - pos.y;
        int distz1 = vals[idx * 9 + 2] - pos.z;

        int dist2 = (distx1 * distx1 + disty1 * disty1 + distz1 * distz1) >> DENOM_BITS;
        int discFix = (normal.x * distx1 + normal.y * disty1 + normal.z * distz1) >> DENOM_BITS;

        discFix = FastMath.abs(discFix);
        dist2 = dist2 + ((dist2 * discFix) >> DENOM_BITS) / 100;
        return dist2;
    }

    public int dirDot(int idx, Vector3Di normal) {
        return (normal.x * vals[idx * 9 + 3] + normal.y * vals[idx * 9 + 4] + normal.z * vals[idx * 9 + 5]) >> DENOM_BITS;
    }

    public int powerR(int idx) {
        return vals[idx * 9 + 6];
    }

    public int powerG(int idx) {
        return vals[idx * 9 + 7];
    }

    public int powerB(int idx) {
        return vals[idx * 9 + 8];
    }

    public int coord(int idx, int plane) {
        return vals[idx * 9 + plane];
    }

    public int plane(int idx) {
        return plane[idx];
    }
}

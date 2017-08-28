package raytracer;

import static raytracer.Fixed.*;

public class LinearPhotons {
    int[] vals;
    byte[] plane;

    public void init(Photon[] photons, int nStoredPhotons) {
        if (vals == null) {
            vals = new int[(nStoredPhotons + 1) * 9];
            plane = new byte[(nStoredPhotons + 1) * 9];
            for (int i = 1; i <= nStoredPhotons; i++) {
                plane[i] = (byte) photons[i].plane;
                vals[i * 9] = toFixed(photons[i].pos.x);
                vals[i * 9 + 1] = toFixed(photons[i].pos.y);
                vals[i * 9 + 2] = toFixed(photons[i].pos.z);
                vals[i * 9 + 3] = toFixed(photons[i].dir.x);
                vals[i * 9 + 4] = toFixed(photons[i].dir.y);
                vals[i * 9 + 5] = toFixed(photons[i].dir.z);
                vals[i * 9 + 6] = toFixed(photons[i].power.r);
                vals[i * 9 + 7] = toFixed(photons[i].power.g);
                vals[i * 9 + 8] = toFixed(photons[i].power.b);
            }
        }
    }

    int dist(int idx, TripleI normal, TripleI pos) {
        int distx1 = vals[idx * 9] - pos.a;
        int disty1 = vals[idx * 9 + 1] - pos.b;
        int distz1 = vals[idx * 9 + 2] - pos.c;

        int dist2 = dot(distx1, distx1, disty1, disty1, distz1, distz1);
        int discFix = dot(normal.a, distx1, normal.b, disty1, normal.c, distz1);

        discFix = FastMath.abs(discFix);
        int i = mul(dist2, discFix);
        dist2 = dist2 + i / 100;
        return dist2;
    }

    public int dirDot(int idx, TripleI normal) {
        return dot(normal.a, vals[idx * 9 + 3], normal.b, vals[idx * 9 + 4], normal.c, vals[idx * 9 + 5]);
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

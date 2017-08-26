package raytracer;

import java.util.Arrays;

import static java.lang.Math.abs;
import static java.lang.Math.min;
import static java.util.Arrays.sort;
import static raytracer.Fixed.DENOM;

public class BalancedPhotonMap {
    Photon[] photons;
    int nStoredPhotons;
    int half_stored_photons;

    long[] idxs;
    int lastIdx;

    NearestPhotons np = new NearestPhotons();
    LinearPhotons lp = new LinearPhotons();

    StopWatch lpSw, dstSw, sumSw, srtSw;

    public BalancedPhotonMap(Photon[] photons, int nStoredPhotons) {
        this.photons = Arrays.copyOf(photons, nStoredPhotons);
        this.nStoredPhotons = nStoredPhotons;
        half_stored_photons = nStoredPhotons / 2 - 1;

        this.idxs = new long[nStoredPhotons];
        lp.init(photons, this.nStoredPhotons);

    }

    public boolean irradianceEstimate(
            Point3D pos,
            Vector3D normal,
            double max_dist,
            int nphotons,
            Colour col) {

        if (nStoredPhotons <= 1) {
            return false;
        }
        int r = 0, g = 0, b = 0;

        Fixed.vector(normal, np.normal);
        Fixed.point(pos, np.pos);
        int dist = (int) (max_dist * DENOM);
        np.init(dist, nphotons);

        lastIdx = 0;

        if (lpSw == null) {
            lpSw = StopWatch.sw("lp");
        }
        lpSw.start();
        try {
            // Locate the nearest photons
            locatePhotons(1);
        } finally {
            lpSw.end();
        }

        int sz = 0;

        if (dstSw == null) {
            dstSw = StopWatch.sw("d");
        }
        dstSw.start();
        try {
            for (int i = 0; i < lastIdx; i++) {
                long index = (int) idxs[i];
                // Adjust the distance for photons that are not on the same plane as this
                // point.
                int dist2 = lp.dist((int) index, np.normal, np.pos);

                if (dist2 >= np.maxDist2) {
                    continue;
                }

                idxs[sz++] = index | ((long) dist2 << 32);
            }

        } finally {
            dstSw.end();
        }

        if (srtSw == null) {
            srtSw = StopWatch.sw("srt");
        }
        if (sz > nphotons) {
            srtSw.start();
            try {
                sort(idxs, 0, sz);
            } finally {
                srtSw.end();
            }
        }

        np.found = min(sz, nphotons);

//        System.out.println(sz + " " + nphotons + " " + (nphotons - np.found));

        // If less than 2 photons return
        if (np.found < 2) {
            return false;
        }

        if (sumSw == null) {
            sumSw = StopWatch.sw("sum");
        }
        sumSw.start();
        try {
            // Sum irradiance from all photons
            for (int i = 0; i < np.found; i++) {
                int idx = (int) idxs[i];
                if (lp.dirDot(idx, np.normal) < 0) {
                    r += lp.powerR(idx);
                    g += lp.powerG(idx);
                    b += lp.powerB(idx);
                }
            }

            float rr = r, gg = g, bb = b;
            rr /= DENOM;
            gg /= DENOM;
            bb /= DENOM;

            col.c(rr, gg, bb);
            // Take into account (estiate of) density
            col.multiply((DENOM / Math.PI) / (np.maxDist2));
        } finally {
            sumSw.end();
        }
        return true;
    }

    private void locatePhotons(int index) {
        idxs[lastIdx++] = index;
        while (index < half_stored_photons) {
            int plane = lp.plane(index);
            int dist1 = np.pos.coord(plane) - lp.coord(index, plane);

            // If dist1 is positive search right plane
            // Else, dist1 is negative search left first
            int dist1sq = Fixed.mul(dist1, dist1);
            int off = dist1 >>> 31;
            if (dist1sq < np.maxDist2) {
                locatePhotons(2 * index + off);
            }

            index = 2 * index + 1 - off;
            idxs[lastIdx++] = index;
        }
    }

    public BalancedPhotonMap copy() {
        return new BalancedPhotonMap(this);
    }

    private BalancedPhotonMap(BalancedPhotonMap bpm) {
        this.photons = Arrays.copyOf(bpm.photons, nStoredPhotons);
        this.nStoredPhotons = bpm.nStoredPhotons;
        half_stored_photons = nStoredPhotons / 2 - 1;

        this.idxs = new long[nStoredPhotons];
        this.lp = bpm.lp;
    }
}

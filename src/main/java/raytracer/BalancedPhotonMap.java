package raytracer;

import java.util.Arrays;

import static java.lang.Math.abs;
import static java.lang.Math.min;
import static java.util.Arrays.sort;
import static raytracer.Fixed.DENOM;
import static raytracer.Fixed.mul;

public class BalancedPhotonMap {
    Photon[] photons;
    int nStoredPhotons;
    int half_stored_photons;

    long[] idxs;
    int lastIdx;

    NearestPhotons np = new NearestPhotons();
    LinearPhotons lp = new LinearPhotons();

    StopWatch ieSw;

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

        np.normal.v(normal);
        np.pos.p(pos);
        int dist = (int) (max_dist * DENOM);
        np.init(dist, nphotons);

        lastIdx = 0;

        if (ieSw == null) {
            ieSw = StopWatch.sw("irrEst");
        }
        ieSw.start();
        try {
            // Locate the nearest photons
            locatePhotons(1);

            int sz = 0;

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

            if (sz > nphotons) {
                sort(idxs, 0, sz);
                sz = nphotons;
            }


            // If less than 2 photons return
            if (sz < 2) {
                return false;
            }

            // Sum irradiance from all photons
            for (int i = 0; i < sz; i++) {
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
            ieSw.end();
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
            int dist1sq = mul(dist1, dist1);
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

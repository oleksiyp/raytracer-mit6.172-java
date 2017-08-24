package raytracer;

import java.util.Arrays;

import static java.lang.Math.abs;
import static raytracer.LinearPhotons.DENOM;

public class BalancedPhotonMap {
    int nStoredPhotons;
    int half_stored_photons;
    Photon[] photons;

    NearestPhotons np = new NearestPhotons();
    LinearPhotons lp = new LinearPhotons();

    public BalancedPhotonMap(Photon[] photons, int nStoredPhotons) {
        this.nStoredPhotons = nStoredPhotons;
        half_stored_photons = nStoredPhotons / 2 - 1;
        this.photons = photons;

        lp.init(photons, this.nStoredPhotons);

    }

    public Colour irradianceEstimate(
            Point3D pos,
            Vector3D normal,
            double max_dist,
            int nphotons) {

        int r = 0, g = 0, b = 0;

        Vector3Di iNorm = lp.normal(normal);
        Point3Di iPos = lp.pos(pos);
        int dist = (int) (max_dist * DENOM);
        np.init(iPos, iNorm, dist, nphotons);

        // Locate the nearest photons
        locatePhotons(1);

        // If less than 2 photons return
        if (np.found < 2) {
            return null;
        }

        // Sum irradiance from all photons
        for (int i = 1; i <= np.found; i++) {
            int idx = np.index[i];
            if (lp.dirDot(idx, iNorm) < 0) {
                r += lp.powerR(idx);
                g += lp.powerG(idx);
                b += lp.powerB(idx);
            }
        }

        float rr = r, gg = g, bb = b;
        rr /= DENOM;
        gg /= DENOM;
        bb /= DENOM;
        // Take into account (estiate of) density
        return new Colour(rr, gg, bb).multiply((DENOM / Math.PI) / (np.maxDist2));
    }

    private void locatePhotons(int index) {
        int dist1;
        int dist2;

        Vector3Di normal = np.normal;

        if (index < half_stored_photons) {
            int plane = lp.plane(index);
            dist1 = np.pos.coord(plane) - lp.coord(index, plane);

            // If dist1 is positive search right plane
            // Else, dist1 is negative search left first
            int dist1sq = (dist1 * dist1) >> LinearPhotons.DENOM_BITS;
            int off = dist1 >>> 31;
            locatePhotons(2 * index + 1 - off);
            if (dist1sq < np.maxDist2) {
                locatePhotons(2 * index + off);
            }
        }

        // Adjust the distance for photons that are not on the same plane as this
        // point.
//        tl.start();
        dist2 = lp.dist(index, normal, np.pos);
//        tl.end();

        if (dist2 < np.maxDist2) {
            // We found a photon :) Insert it in the candidate list
            if (np.found < np.max) {
                // Array not full
                add(index, dist2);
            } else {
                // Array full.  Have to remove the furthest photon.
                addFull(index, dist2);
            }
        }
    }

    private void add(int index, int dist2) {
        np.found++;
        np.dist2[np.found] = dist2;
        np.index[np.found] = index;
    }

    private void addFull(int index, int dist2) {
        int j;
        float maxDist = -1;
        int maxIndex = -1;
        j = 1;
        while (j <= np.found) {
            if (np.dist2[j] > maxDist) {
                maxDist = np.dist2[j];
                maxIndex = j;
            }
            j++;
        }
        if (maxIndex != -1) {
            np.dist2[maxIndex] = dist2;
            np.index[maxIndex] = index;
        }
    }

    public BalancedPhotonMap copy() {
        return new BalancedPhotonMap(
                Arrays.copyOf(photons, photons.length),
                nStoredPhotons);
    }
}

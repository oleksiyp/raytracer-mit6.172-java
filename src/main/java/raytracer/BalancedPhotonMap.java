package raytracer;

import java.util.Arrays;

import static java.lang.Math.abs;

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

        float r = 0, g = 0, b = 0;

        np.init(pos.toFloat(), normal.toFloat(), (float) max_dist, nphotons);

        // Locate the nearest photons
        locatePhotons(1);

        // If less than 2 photons return
        if (np.found < 2) {
            return null;
        }

        // Sum irradiance from all photons
        for (int i = 1; i <= np.found; i++) {
            int idx = np.index[i];
            if (lp.dirDot(idx, normal.toFloat()) < 0.0) {
                r += lp.powerR(idx);
                g += lp.powerG(idx);
                b += lp.powerB(idx);
            }
        }

        // Take into account (estimate of) density
        return new Colour(r, g, b).multiply((1.0f / Math.PI) / (np.dist2[0]));
    }


    private void locatePhotons(int index) {
        float dist1;
        float dist2;

        Vector3Df normal = np.normal;

        if (index < half_stored_photons) {
            int plane = lp.plane(index);
            dist1 = np.pos.coord(plane) - lp.coord(index, plane);

            // If dist1 is positive search right plane
            if (dist1 > 0.0) {
                locatePhotons(2 * index + 1);
                if (dist1 * dist1 < np.dist2[0]) {
                    locatePhotons(2 * index);
                }
                // Else, dist1 is negative search left first
            } else {
                locatePhotons(2 * index);
                if (dist1 * dist1 < np.dist2[0]) {
                    locatePhotons(2 * index + 1);
                }
            }
        }

        // Adjust the distance for photons that are not on the same plane as this
        // point.
        dist2 = lp.dist(index, normal, np.pos);

        if (dist2 < np.dist2[0]) {
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

    private void add(int index, float dist2) {
        np.found++;
        np.dist2[np.found] = dist2;
        np.index[np.found] = index;
    }

    private void addFull(int index, float dist2) {
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

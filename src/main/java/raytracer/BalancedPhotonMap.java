package raytracer;

import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class BalancedPhotonMap {
    int stored_photons;
    int half_stored_photons;
    Photon[] photons;

    NearestPhotons np = new NearestPhotons();

    public Colour irradianceEstimate(
            Point3D pos,
            Vector3D normal,
            double max_dist,
            int nphotons) {

        double r = 0, g = 0, b = 0;

        if (np.dist2 == null || np.dist2.length < nphotons + 1) {
            np.dist2 = new double[nphotons + 1];
        }
        if (np.index == null || np.index.length < nphotons + 1) {
            np.index = new int[nphotons + 1];
        }
        np.pos = pos;
        np.max = nphotons;
        np.found = 0;
        np.dist2[0] = max_dist * max_dist;
        np.index[0] = 0;

        // Locate the nearest photons
        locatePhotons(np, 1, normal);

        // If less than 2 photons return
        if (np.found < 2) {
            return null;
        }

        // Sum irradiance from all photons
        for (int i = 1; i <= np.found; i++) {
            Photon p = photons[np.index[i]];
            if (p.dir.dot(normal) < 0.0) {
                r += p.power.r;
                g += p.power.g;
                b += p.power.b;
            }
        }

        // Take into account (estimate of) density
        return new Colour(r, g, b).multiply((1.0f / Math.PI) / (np.dist2[0]));
    }

    private void locatePhotons(NearestPhotons np,
                               int index,
                               Vector3D normal) {

        Photon p = photons[index];
        double dist1;
        double distx1;
        double disty1;
        double distz1;
        double dist2;
        double discFix;

        if (index < half_stored_photons) {
            dist1 = np.pos.coord(p.plane) - p.pos.coord(p.plane);

            // If dist1 is positive search right plane
            if (dist1 > 0.0) {
                locatePhotons(np, 2 * index + 1, normal);
                if (dist1 * dist1 < np.dist2[0]) {
                    locatePhotons(np, 2 * index, normal);
                }
                // Else, dist1 is negative search left first
            } else {
                locatePhotons(np, 2 * index, normal);
                if (dist1 * dist1 < np.dist2[0]) {
                    locatePhotons(np, 2 * index + 1, normal);
                }
            }
        }

        // Compute squared distance between current photon and np.pos
        dist2 = p.pos.dist2To(np.pos);

        // Adjust the distance for photons that are not on the same plane as this
        // point.
        distx1 = p.pos.x - np.pos.x;
        disty1 = p.pos.y - np.pos.y;
        distz1 = p.pos.z - np.pos.z;
        discFix = normal.x * distx1 + normal.y * disty1 + normal.z * distz1;
        discFix = abs(discFix);
        dist2 += discFix * dist2 * 0.010;

        if (dist2 < np.dist2[0]) {
            // We found a photon :) Insert it in the candidate list
            if (np.found < np.max) {
                // Array not full
                np.found++;
                np.dist2[np.found] = dist2;
                np.index[np.found] = index;
            } else {
                // Array full.  Have to remove the furthest photon.
                int j;
                double max_dist = -1;
                int max_index = -1;
                j = 1;
                while (j <= np.found) {
                    if (np.dist2[j] > max_dist) {
                        max_dist = np.dist2[j];
                        max_index = j;
                    }
                    j++;
                }
                if (max_index != -1) {
                    np.dist2[max_index] = dist2;
                    np.index[max_index] = index;
                }
            }
        }
    }
}

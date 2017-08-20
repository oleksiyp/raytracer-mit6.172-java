package raytracer;

import static java.lang.Math.abs;

public class BalancedPhotonMap {
    int nStoredPhotons;
    int half_stored_photons;
    Photon[] photons;

    NearestPhotons np = new NearestPhotons();
    LinearPhotons lp = new LinearPhotons();

    public Colour irradianceEstimate(
            Point3D pos,
            Vector3D normal,
            double max_dist,
            int nphotons) {

        double r = 0, g = 0, b = 0;

        np.init(pos, normal, max_dist, nphotons);
        lp.init(photons, nStoredPhotons);

        // Locate the nearest photons
        locatePhotons(1);

        // If less than 2 photons return
        if (np.found < 2) {
            return null;
        }

        // Sum irradiance from all photons
        for (int i = 1; i <= np.found; i++) {
            int idx = np.index[i];
            if (lp.dirDot(idx, normal) < 0.0) {
                r += lp.powerR(idx);
                g += lp.powerG(idx);
                b += lp.powerB(idx);
            }
        }

        // Take into account (estimate of) density
        return new Colour(r, g, b).multiply((1.0f / Math.PI) / (np.dist2[0]));
    }

    public Colour irradianceEstimate2(
            Point3D pos,
            Vector3D normal,
            double maxDist) {

        double r = 0, g = 0, b = 0;
        int found = 0;
        double maxDist2 = maxDist * maxDist;


        // Sum irradiance from all photons
        for (int i = 1; i <= nStoredPhotons; i++) {
            double dist = lp.dist(i, normal, pos);
            if (dist * dist < maxDist2) {
                if (lp.dirDot(i, normal) < 0.0) {
                    r += lp.powerR(i);
                    g += lp.powerG(i);
                    b += lp.powerB(i);
                }
                found++;
            }
        }

        // If less than 2 photons return
        if (found < 2) {
            return null;
        }

        // Take into account (estimate of) density
        return new Colour(r, g, b).multiply((1.0f / Math.PI) / maxDist2);
    }


    private void locatePhotons(int index) {
        double dist1;
        double dist2;

        Vector3D normal = np.normal;

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

    private void add(int index, double dist2) {
        np.found++;
        np.dist2[np.found] = dist2;
        np.index[np.found] = index;
    }

    private void addFull(int index, double dist2) {
        int j;
        double maxDist = -1;
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

    private double dist(Photon p, Vector3D normal, Point3D pos) {
        double distx1 = p.pos.x - pos.x;
        double disty1 = p.pos.y - pos.y;
        double distz1 = p.pos.z - pos.z;

        double dist2 = distx1 * distx1 + disty1 * disty1 + distz1 * distz1;
        double discFix = normal.x * distx1 + normal.y * disty1 + normal.z * distz1;

        discFix = abs(discFix);
        dist2 *= discFix  * 0.010 + 1;
        return dist2;
    }
}

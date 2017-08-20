package raytracer;

import java.util.Arrays;

import static java.lang.Math.*;

public class PhotonMap {
    int stored_photons;
    int prev_scale;
    int max_photons;
    Photon[] photons;

    double[] bbox_min;
    double[] bbox_max;
    int half_stored_photons;

    public PhotonMap(int maxPhotons) {
        stored_photons = 0;
        prev_scale = 1;
        this.max_photons = maxPhotons;

        photons = new Photon[max_photons + 1];

        bbox_min = new double[3];
        bbox_max = new double[3];

        bbox_min[0] = bbox_min[1] = bbox_min[2] = 1e8f;
        bbox_max[0] = bbox_max[1] = bbox_max[2] = -1e8f;
    }

    public BalancedPhotonMap balance() {
        if (stored_photons > 1) {
            int i;
            int d, j, foo;
            Photon foo_photon;

            // Allocate two temporary arrays for the balancing procedure
            int[] pa1 = new int[stored_photons + 1];
            int[] pa2 = new int[stored_photons + 1];

            for (i = 0; i <= stored_photons; i++) {
                pa2[i] = i;
            }

            balanceSegment(pa1, pa2, 1, 1, stored_photons);

            // Reorganize balanced kd-tree (make a heap)
            j = 1;
            foo = 1;
            foo_photon = photons[j];

            for (i = 1; i <= stored_photons; i++) {
                d = pa1[j];
                pa1[j] = -1;
                if (d != foo) {
                    photons[j] = photons[d];
                } else {
                    photons[j] = foo_photon;

                    if (i < stored_photons) {
                        for (; foo <= stored_photons; foo++) {
                            if (pa1[foo] != -1) {
                                break;
                            }
                        }
                        foo_photon = photons[foo];
                        j = foo;
                    }
                    continue;
                }
                j = d;
            }
        }

        BalancedPhotonMap bmap = new BalancedPhotonMap();
        bmap.stored_photons = stored_photons;
        bmap.half_stored_photons = stored_photons / 2 - 1;
        bmap.photons = photons;

        return bmap;
    }

    private void balanceSegment(int[] pbal,
                                int[] porg,
                                int index,
                                int start,
                                int end) {
        // Compute new median
        int axis;
        int median = 1;

        while ((4 * median) <= (end - start + 1)) {
            median += median;
        }

        if ((3 * median) <= (end - start + 1)) {
            median += median;
            median += start - 1;
        } else {
            median = end - median + 1;
        }

        // Find axis to split along
        axis = 2;
        if ((bbox_max[0] - bbox_min[0]) >
                (bbox_max[1] - bbox_min[1]) &&
                (bbox_max[0] - bbox_min[0]) >
                        (bbox_max[2] - bbox_min[2])) {
            axis = 0;
        } else if ((bbox_max[1] - bbox_min[1]) >
                (bbox_max[2] - bbox_min[2])) {
            axis = 1;
        }

        // Partition photon block around the median
        medianSplit(porg, start, end, median, axis);

        pbal[index] = porg[median];
        photons[pbal[index]].plane = axis;

        // Recursively balance the left and right block
        if (median > start) {
            // Balance left segment
            if (start < median - 1) {
                double tmp = bbox_max[axis];
                bbox_max[axis] = photons[pbal[index]].pos.coord(axis);
                balanceSegment(pbal, porg, 2 * index, start, median - 1);
                bbox_max[axis] = tmp;
            } else {
                pbal[2 * index] = porg[start];
            }
        }

        if (median < end) {
            // Balance right segment
            if (median + 1 < end) {
                double tmp = bbox_min[axis];
                bbox_min[axis] = photons[pbal[index]].pos.coord(axis);
                balanceSegment(pbal, porg, 2 * index + 1, median + 1, end);
                bbox_min[axis] = tmp;
            } else {
                pbal[2 * index + 1] = porg[end];
            }
        }
    }

    private void medianSplit(
            int[] p,
            int start,
            int end,
            int median,
            int axis) {
        int left = start;
        int right = end;

        while (right > left) {
            double v = photons[p[right]].pos.coord(axis);
            int i = left - 1;
            int j = right;
            for (; ; ) {

                while (photons[p[++i]].pos.coord(axis) < v) ;
                while (photons[p[--j]].pos.coord(axis) > v && j > left) ;

                if (i >= j) {
                    break;
                }
                int tmp = p[i];
                p[i] = p[j];
                p[j] = tmp;
            }

            int tmp = p[i];
            p[i] = p[right];
            p[right] = tmp;

            if (i >= median) {
                right = i - 1;
            }
            if (i <= median) {
                left = i + 1;
            }
        }
    }

    public void scalePhotonPower(double scale) {
        for (int i = prev_scale; i <= stored_photons; i++) {
            photons[i].power = photons[i].power.multiply(scale);
        }
        prev_scale = stored_photons;
    }

    public void storePhoton(Colour power, Point3D pos, Vector3D dir) {
        Photon photon = new Photon();

        if (stored_photons >= max_photons) {
            photons = Arrays.copyOf(photons, 2 * max_photons + 1);
            max_photons *= 2;
        }

        stored_photons++;

        for (int i = 0; i < 3; i++) {
            photon.pos = pos;
            if (photon.pos.coord(i) < bbox_min[i]) {
                bbox_min[i] = photon.pos.coord(i);
            }
            if (photon.pos.coord(i) > bbox_max[i]) {
                bbox_max[i] = photon.pos.coord(i);
            }
        }
        photon.power = power;
        photon.dir = dir;
        photons[stored_photons] = photon;
    }

}

package raytracer;

public class NearestPhotons {
    int[] dist2;
    int[] index;
    Point3Di pos;
    int max;
    int found;
    Vector3Di normal;
    int maxDist2;

    public void init(Point3Di pos, Vector3Di normal, int max_dist, int nphotons) {
        if (dist2 == null || dist2.length < nphotons + 1) {
            dist2 = new int[nphotons + 1];
        }
        if (index == null || index.length < nphotons + 1) {
            index = new int[nphotons + 1];
        }
        this.pos = pos;
        max = nphotons;
        found = 0;
        maxDist2 = (max_dist * max_dist) >> LinearPhotons.DENOM_BITS;
        index[0] = 0;
        this.normal = normal;
    }
}

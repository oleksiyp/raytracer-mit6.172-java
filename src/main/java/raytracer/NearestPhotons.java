package raytracer;

public class NearestPhotons {
    float[] dist2;
    int[] index;
    Point3Df pos;
    int max;
    int found;
    Vector3Df normal;

    public void init(Point3Df pos, Vector3Df normal, float max_dist, int nphotons) {
        if (dist2 == null || dist2.length < nphotons + 1) {
            dist2 = new float[nphotons + 1];
        }
        if (index == null || index.length < nphotons + 1) {
            index = new int[nphotons + 1];
        }
        this.pos = pos;
        max = nphotons;
        found = 0;
        dist2[0] = max_dist * max_dist;
        index[0] = 0;
        this.normal = normal;
    }
}

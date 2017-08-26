package raytracer;

public class NearestPhotons {
//    int[] dist2;
//    int[] index;
    int max;
    int found;
    Point3Di pos = new Point3Di(0, 0, 0);
    Vector3Di normal = new Vector3Di(0, 0, 0);
    int maxDist2;

    public void init(int max_dist, int nphotons) {
//        if (dist2 == null || dist2.length < nphotons + 1) {
//            dist2 = new int[nphotons + 1];
//        }
//        if (index == null || index.length < nphotons + 1) {
//            index = new int[nphotons + 1];
//        }
        max = nphotons;
        found = 0;
        maxDist2 = Fixed.mul(max_dist, max_dist);
//        index[0] = 0;
    }
}

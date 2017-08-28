package raytracer;

import static raytracer.TripleI.zero;

public class NearestPhotons {
    int max;
    TripleI pos = zero();
    TripleI normal = zero();
    int maxDist2;

    public void init(int max_dist, int nphotons) {
        max = nphotons;
        maxDist2 = Fixed.mul(max_dist, max_dist);
    }
}

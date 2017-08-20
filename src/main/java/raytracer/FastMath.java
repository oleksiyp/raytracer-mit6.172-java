package raytracer;

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

public class FastMath {
    public static double acos(double x) {
        double negate = x < 0 ? 1 : 0;
        x = abs(x);
        double ret = -0.0187293;
        ret = ret * x;
        ret = ret + 0.0742610;
        ret = ret * x;
        ret = ret - 0.2121144;
        ret = ret * x;
        ret = ret + 1.5707288;
        ret = ret * sqrt(1.0-x);
        ret = ret - 2 * negate * ret;
        return negate * 3.14159265358979 + ret;
    }
}

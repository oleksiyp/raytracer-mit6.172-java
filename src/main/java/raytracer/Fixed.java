package raytracer;

public class Fixed {
    public static final short DENOM_BITS = 12;
    public static final int DENOM = 1 << DENOM_BITS;

    public static void point(Point3D pt, Point3Di pos) {
        pos.x = toFixed(pt.x);
        pos.y = toFixed(pt.y);
        pos.z = toFixed(pt.z);
    }

    public static void vector(Vector3D vec, Vector3Di vecR) {
        vecR.x = toFixed(vec.x);
        vecR.y = toFixed(vec.y);
        vecR.z =  toFixed(vec.z);
    }

    public static void colour(Colour col, ColourI ci) {
        ci.r = toFixed(col.r);
        ci.g = toFixed(col.g);
        ci.b = toFixed(col.b);
    }

    public static int toFixed(double val) {
        return (int) (val * DENOM);
    }

    public static int mul(int f1, int f2) {
        long l = f1;
        l *= f2;
        l >>= DENOM_BITS;
        return (int) l;
    }

    public static int dot(int x1, int x2, int y1, int y2, int z1, int z2) {
        long l = ((long)x1) * x2;
        l += ((long)y1) * y2;
        l += ((long)z1) * z2;
        l >>= DENOM_BITS;
        return (int) l;
    }
}

package raytracer;

public class Fixed {
    public static final short DENOM_BITS = 11;
    public static final int DENOM = 1 << DENOM_BITS;
    public static final int DENOM_SQ = DENOM * DENOM;

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

    public static int div(int a, int b) {
        long l = a;
        l <<= DENOM_BITS;
        l /= b;
        return (int) l;
    }
}

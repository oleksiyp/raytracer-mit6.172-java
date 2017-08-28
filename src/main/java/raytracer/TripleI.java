package raytracer;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@ToString
@Getter
@EqualsAndHashCode
public class TripleI {
    public static final int BITS = 20;

    int a, b, c;
    int d;
    public static final int MASK = (1 << BITS) - 1;

    public int coord(int plane) {
        if (plane == 0) return a;
        if (plane == 1) return b;
        return c;
    }

    public void p(Point3D pt) {
        a = Fixed.toFixed(pt.x);
        b = Fixed.toFixed(pt.y);
        c = Fixed.toFixed(pt.z);
    }

    public void v(Vector3D vec) {
        a = Fixed.toFixed(vec.x);
        b = Fixed.toFixed(vec.y);
        c = Fixed.toFixed(vec.z);
    }

    public void colour(Colour col) {
        a = Fixed.toFixed(col.r);
        b = Fixed.toFixed(col.g);
        c = Fixed.toFixed(col.b);
    }

    public static TripleI zero() {
        return new TripleI(0, 0, 0, 0);
    }

    public void unpack(long val) {
        int shift = 32 - BITS;
        c = (int) (val & MASK) << shift >> shift;
        val >>>= BITS;
        b = (int) (val & MASK) << shift >> shift;
        val >>>= BITS;
        a = (int) (val & MASK) << shift >> shift;
        val >>>= BITS;
        d = (int) (val & 3);
    }

    public static long pack(int a, int b, int c) {
        return pack(a, b, c, 0);
    }

    public static long pack(int a, int b, int c, int d) {
        long l = d;
        l <<= BITS;
        l |= a & MASK;
        l <<= BITS;
        l |= b & MASK;
        l <<= BITS;
        l |= c & MASK;
        return l;
    }

}

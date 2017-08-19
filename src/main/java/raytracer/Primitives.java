package raytracer;

public class Primitives {
    static final double EPSILON = 1e-6;

    public static Point3D p(double x, double y, double z) {
        return new Point3D(x, y, z);
    }

    static Vector3D v(double x, double y, double z) {
        return new Vector3D(x, y, z);
    }

    static Colour c(double r, double g, double b) {
        return new Colour(r, g, b);
    }
}

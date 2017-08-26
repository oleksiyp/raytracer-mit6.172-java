package raytracer;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import static java.lang.Math.sqrt;

@AllArgsConstructor
@ToString
@Getter
@EqualsAndHashCode
public class Vector3D {
    public static final Vector3D ZERO = new Vector3D(0, 0, 0);
    double x, y, z;

    public Vector3D() {
    }

    public void assign(Vector3D v) {
        x = v.x;
        y = v.y;
        z = v.z;
    }

    public void negate() {
        x = -x;
        y = -y;
        z = -z;
    }

    public void normalize() {
        double l = mag();
        x /= l;
        y /= l;
        z /= l;
    }

    public double dot(Vector3D vec) {
        return x*vec.x + y*vec.y + z*vec.z;
    }

    public void multiply(double s) {
        x *= s;
        y *= s;
        z *= s;
    }

    public void subtract(Vector3D vec) {
        double xx = x - vec.x;
        double yy = y - vec.y;
        double zz = z - vec.z;

        this.x = xx;
        this.y = yy;
        this.z = zz;
    }

    public void cross(Vector3D vec) {
        double xx = y * vec.z - z * vec.y;
        double yy = z * vec.x - x * vec.z;
        double zz = x * vec.y - y * vec.x;

        this.x = xx;
        this.y = yy;
        this.z = zz;
    }

    public void transform(Matrix4D mat) {
        if (mat.ident) {
            return;
        }

        double xx = x * mat.values[0][0] + y * mat.values[0][1] + z * mat.values[0][2];
        double yy = x * mat.values[1][0] + y * mat.values[1][1] + z * mat.values[1][2];
        double zz = x * mat.values[2][0] + y * mat.values[2][1] + z * mat.values[2][2];

        this.x = xx;
        this.y = yy;
        this.z = zz;
    }

    public double mag() {
        return sqrt(mag2());
    }

    private double mag2() {
        return x * x + y * y + z * z;
    }

    public Vector3D copy() {
        Vector3D v = zero();
        v.assign(this);
        return v;
    }

    public static Vector3D zero() {
        return new Vector3D(0 , 0 ,0);
    }

    public void v(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}

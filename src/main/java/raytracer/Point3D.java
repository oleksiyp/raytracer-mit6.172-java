package raytracer;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
public class Point3D {
    double x, y, z;

    public Point3D() {
    }

    public void subtract(Point3D pt, Vector3D res) {
        res.x = x - pt.x;
        res.y = y - pt.y;
        res.z = z - pt.z;
    }

    public void add(Vector3D vec) {
        x += vec.x;
        y += vec.y;
        z += vec.z;
    }

    public void transform(Matrix4D mat) {
        if (mat.ident) {
            return;
        }
        double xx = x * mat.values[0][0] + y * mat.values[0][1] + z * mat.values[0][2] + mat.values[0][3];
        double yy = x * mat.values[1][0] + y * mat.values[1][1] + z * mat.values[1][2] + mat.values[1][3];
        double zz = x * mat.values[2][0] + y * mat.values[2][1] + z * mat.values[2][2] + mat.values[2][3];

        x = xx;
        y = yy;
        z = zz;
    }

    public double dot(Vector3D vec) {
        return x*vec.x + y*vec.y + z*vec.z;
    }

    public double dot(Point3D pt) {
        return x*pt.x + y*pt.y + z*pt.z;
    }

    public double coord(int axis) {
        if (axis == 0) {
            return x;
        }
        if (axis == 1) {
            return y;
        }
        return z;
    }

    public static Point3D origin() {
        return new Point3D();
    }

    public void assign(Point3D vec) {
        x = vec.x;
        y = vec.y;
        z = vec.z;
    }

    public void p(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Point3D copy() {
        return new Point3D(x, y, z);
    }
}

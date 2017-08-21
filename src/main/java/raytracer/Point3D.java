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
    final double x, y, z;

    public Vector3D subtract(Point3D pt) {
        return new Vector3D(x - pt.x, y - pt.y, z - pt.z);
    }

    public Point3D add(Vector3D vec) {
        return new Point3D(x + vec.x, y +  vec.y, z + vec.z);
    }

    public Point3D transform(Matrix4D mat) {
        if (mat.ident) {
            return this;
        }
        return new Point3D(
                x * mat.values[0][0] + y * mat.values[0][1] + z * mat.values[0][2] + mat.values[0][3],
                x * mat.values[1][0] + y * mat.values[1][1] + z * mat.values[1][2] + mat.values[1][3],
                x * mat.values[2][0] + y * mat.values[2][1] + z * mat.values[2][2] + mat.values[2][3]);
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

    public Point3Df toFloat() {
        return new Point3Df((float) x, (float) y, (float) z);
    }
}

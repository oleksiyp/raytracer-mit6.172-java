package raytracer;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
public class Point3Df {
    final float x, y, z;

    public Vector3D subtract(Point3Df pt) {
        return new Vector3D(x - pt.x, y - pt.y, z - pt.z);
    }

    public Point3Df add(Vector3D vec) {
        return new Point3Df(x + (float) vec.x, y +  (float) vec.y, z + (float) vec.z);
    }

    public Point3Df transform(Matrix4D mat) {
        if (mat.ident) {
            return this;
        }
        return new Point3Df(
                (float) (x * mat.values[0][0] + y * mat.values[0][1] + z * mat.values[0][2] + mat.values[0][3]),
                (float) (x * mat.values[1][0] + y * mat.values[1][1] + z * mat.values[1][2] + mat.values[1][3]),
                (float) (x * mat.values[2][0] + y * mat.values[2][1] + z * mat.values[2][2] + mat.values[2][3]));
    }

    public float dot(Vector3D vec) {
        return (float) (x*vec.x + y*vec.y + z*vec.z);
    }

    public float dot(Point3Df pt) {
        return x*pt.x + y*pt.y + z*pt.z;
    }

    public float coord(int axis) {
        if (axis == 0) {
            return x;
        }
        if (axis == 1) {
            return y;
        }
        return z;
    }
}

package raytracer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import static java.lang.Math.sqrt;

@AllArgsConstructor
@ToString
@Getter
public class Vector3D {
    final double x, y, z;

    public Vector3D negate() {
        return new Vector3D(-x, -y, -z);
    }

    public Vector3D normalize() {
        double l = sqrt(x * x + y * y + z * z);
        return new Vector3D(x / l, y / l, z / l);
    }

    public double dot(Vector3D vec) {
        return x*vec.x + y*vec.y + z*vec.z;
    }

    public Vector3D multiply(double s) {
        return new Vector3D(x * s, y * s, z * s);
    }

    public Vector3D subtract(Vector3D vec) {
        return new Vector3D(x - vec.x, y - vec.y, z - vec.z);
    }

    public Vector3D cross(Vector3D vec) {
        return new Vector3D(
                y * vec.z - z * vec.y,
                z * vec.x - x * vec.z,
                x * vec.y - y * vec.x);
    }

    public Vector3D transform(Matrix4D mat) {
        return new Vector3D(
                x * mat.values[0][0] + y * mat.values[0][1] + z * mat.values[0][2],
                x * mat.values[1][0] + y * mat.values[1][1] + z * mat.values[1][2],
                x * mat.values[2][0] + y * mat.values[2][1] + z * mat.values[2][2]);
    }

    public Point3D toPoint() {
        return new Point3D(x, y, z);
    }
}

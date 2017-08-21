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
public class Vector3Df {
    final float x, y, z;

    public Vector3Df negate() {
        return new Vector3Df(-x, -y, -z);
    }

    public Vector3Df normalize() {
        float l = mag();
        return new Vector3Df(x / l, y / l, z / l);
    }

    public float dot(Vector3Df vec) {
        return x*vec.x + y*vec.y + z*vec.z;
    }

    public Vector3Df multiply(float s) {
        return new Vector3Df(x * s, y * s, z * s);
    }

    public Vector3Df subtract(Vector3Df vec) {
        return new Vector3Df(x - vec.x, y - vec.y, z - vec.z);
    }

    public Vector3Df cross(Vector3Df vec) {
        return new Vector3Df(
                y * vec.z - z * vec.y,
                z * vec.x - x * vec.z,
                x * vec.y - y * vec.x);
    }

    public Vector3Df transform(Matrix4D mat) {
        if (mat.ident) {
            return this;
        }
        return new Vector3Df(
                (float) (x * mat.values[0][0] + y * mat.values[0][1] + z * mat.values[0][2]),
                (float) (x * mat.values[1][0] + y * mat.values[1][1] + z * mat.values[1][2]),
                (float) (x * mat.values[2][0] + y * mat.values[2][1] + z * mat.values[2][2]));
    }

    public Point3D toPoint() {
        return new Point3D(x, y, z);
    }

    public float mag() {
        return (float) sqrt(x * x + y * y + z * z);
    }
}

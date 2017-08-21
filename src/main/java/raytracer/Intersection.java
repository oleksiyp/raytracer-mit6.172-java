package raytracer;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
public class Intersection {
    boolean set;
    Point3D point;
    Vector3D normal;
    double tValue;
    Material mat;

    public Intersection() {
        set = false;
        tValue = Double.MAX_VALUE;
    }

    public double getTValue() {
        return tValue;
    }

    public void set(Point3D point,
                    Vector3D normal,
                    double t,
                    Material mat) {
        this.set = true;
        this.point = point;
        this.normal = normal;
        this.tValue = t;
        this.mat = mat;
    }

    public void transformBack(Matrix4D modelToWorld) {
        point = point.transform(modelToWorld);
        normal = normal.transform(modelToWorld);
    }
}

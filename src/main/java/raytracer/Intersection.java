package raytracer;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import static raytracer.Point3D.origin;
import static raytracer.Vector3D.zero;

@AllArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
public class Intersection {
    boolean set;
    Point3D point = origin();
    Vector3D normal = zero();
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
        this.point.assign(point);
        this.normal.assign(normal);
        this.tValue = t;
        this.mat = mat;
    }

    public void transformBack(Matrix4D modelToWorld) {
        point.transform(modelToWorld);
        normal.transform(modelToWorld);
    }

    public void clear() {
        set = false;
        tValue = Double.MAX_VALUE;
    }
}

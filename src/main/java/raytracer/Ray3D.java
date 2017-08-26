package raytracer;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import static raytracer.Colour.black;
import static raytracer.Point3D.origin;
import static raytracer.Primitives.EPSILON;
import static raytracer.Vector3D.zero;

@Getter
@ToString
public class Ray3D {
    final Point3D origin;
    final Vector3D dir;
    final Intersection intersection = new Intersection();
    Colour colour;

    public Ray3D() {
        this.origin = origin();
        this.dir = zero();
        colour = black();
    }

    public Ray3D(Ray3D ray) {
        this.origin = ray.getOrigin();
        this.dir = ray.getDir();
        this.colour = black();
        if (intersection.isSet()) {
            this.setIntersection(intersection.getPoint(), intersection.getNormal(), intersection.getTValue(), intersection.getMat());
        }
    }


    public Intersection getIntersection() {
        return intersection;
    }

    public void setIntersection(Point3D pt, Vector3D normal, double t, Material mat) {
        intersection.set(pt, normal, t, mat);
    }

    public boolean lessDistant(double lambda) {
        return lambda > EPSILON && lambda < getIntersection().getTValue();
    }

    public void set(Point3D origin, Vector3D dir) {
        this.origin.assign(origin);
        this.dir.assign(dir);
        colour.c(0, 0, 0);
        intersection.clear();
    }

    public void clampColour() {
        colour.clamp();
    }

    public void setColour(Colour col) {
        colour.assign(col);
    }

    public void addColour(Colour col) {
        colour.add(col);
    }

    public void setColour(double r, double g, double b) {
        colour.c(r, g, b);
    }
}

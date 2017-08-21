package raytracer;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import static raytracer.Primitives.EPSILON;

@Getter
@ToString
public class Ray3D {
    final Point3D origin;
    final Vector3D dir;
    final Intersection intersection = new Intersection();

    @Setter
    Colour colour;

    public Ray3D(Point3D origin, Vector3D dir) {
        this.origin = origin;
        this.dir = dir;
        colour = new Colour(0, 0, 0);
    }

    public Ray3D(Ray3D ray) {
        this.origin = ray.getOrigin();
        this.dir = ray.getDir();
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

    public void clampColour() {
        colour = colour.clamp();
    }

    public boolean lessDistant(double lambda) {
        return lambda > EPSILON && lambda < getIntersection().getTValue();
    }
}

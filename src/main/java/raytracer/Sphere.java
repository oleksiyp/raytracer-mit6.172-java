package raytracer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import static java.lang.Math.sqrt;

@AllArgsConstructor
@Getter
@ToString
public class Sphere extends SceneObject {
    final Material mat;

    public boolean intersect(Ray3D ray, Matrix4D worldToModel, Matrix4D modelToWorld) {
        Point3D p;
        Vector3D n;
        double lambda;
        double A, B, C, D;
        Point3D origin = ray.getOrigin().transform(worldToModel);
        Vector3D dir = ray.getDir().transform(worldToModel);

        double radiusSq = 20 * 20;
        A = dir.dot(dir);
        B = 2 * origin.dot(dir);
        C = origin.dot(origin) - radiusSq;
        D = B * B - (4 * A * C);

        if (D < 0) {
            return false;
        }

        if (D > 0) {
            double sq = sqrt(D);

            lambda = (-B - sq) / (2 * A);

            if (ray.lessDistant(lambda)) {
                p = origin.add(dir.multiply(lambda));
                ray.setIntersection(p, new Vector3D(p.x, p.y, p.z), lambda, mat);
                ray.intersection.transformBack(modelToWorld);
                return true;
            }

            lambda = (-B + sq) / (2 * A);

            if (ray.lessDistant(lambda)) {
                p = origin.add(dir.multiply(lambda));
                ray.setIntersection(p, new Vector3D(p.x, p.y, p.z), lambda, mat);
                ray.intersection.transformBack(modelToWorld);
                return true;
            }
        } else {
            lambda = -B / (2 * A);

            if (ray.lessDistant(lambda)) {
                p = origin.add(dir.multiply(lambda));
                ray.setIntersection(p, new Vector3D(p.x, p.y, p.z), lambda, mat);
                ray.intersection.transformBack(modelToWorld);
                return true;
            }
        }

        return false;
    }

}

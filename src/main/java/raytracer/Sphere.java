package raytracer;

import lombok.Getter;
import lombok.ToString;

import static java.lang.Math.sqrt;
import static raytracer.Point3D.origin;
import static raytracer.Vector3D.zero;

@Getter
@ToString
public class Sphere extends SceneObject {
    final Material mat;

    Point3D origin = origin();
    Vector3D dir = zero();
    Point3D sphPt = origin();
    Vector3D sphVec = zero();

    public Sphere(Material mat) {
        this.mat = mat;
    }

    public boolean intersect(Ray3D ray, Matrix4D worldToModel, Matrix4D modelToWorld) {
        Point3D p;
        Vector3D n;
        double lambda;
        double A, B, C, D;

        origin.assign(ray.getOrigin());
        dir.assign(ray.getDir());

        origin.transform(worldToModel);
        dir.transform(worldToModel);

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
                sphPt.assign(origin);
                sphVec.assign(dir);
                sphVec.multiply(lambda);
                sphPt.add(sphVec);
                sphVec.v(sphPt.x, sphPt.y, sphPt.z);
                sphVec.normalize();
                ray.setIntersection(sphPt, sphVec, lambda, mat);
                ray.intersection.transformBack(modelToWorld);
                return true;
            }

            lambda = (-B + sq) / (2 * A);

            if (ray.lessDistant(lambda)) {
                sphPt.assign(origin);
                sphVec.assign(dir);
                sphVec.multiply(lambda);
                sphPt.add(sphVec);
                sphVec.v(sphPt.x, sphPt.y, sphPt.z);
                sphVec.normalize();
                ray.setIntersection(sphPt, sphVec, lambda, mat);
                ray.intersection.transformBack(modelToWorld);
                return true;
            }
        } else {
            lambda = -B / (2 * A);

            if (ray.lessDistant(lambda)) {
                sphPt.assign(origin);
                sphVec.assign(dir);
                sphVec.multiply(lambda);
                sphPt.add(sphVec);
                sphVec.v(sphPt.x, sphPt.y, sphPt.z);
                sphVec.normalize();
                ray.setIntersection(sphPt, sphVec, lambda, mat);
                ray.intersection.transformBack(modelToWorld);
                return true;
            }
        }

        return false;
    }

}

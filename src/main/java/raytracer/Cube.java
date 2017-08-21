package raytracer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;


@AllArgsConstructor
@Getter
@ToString
public class Cube extends SceneObject {
    final Direction dir;
    final Material mat1;
    final Material mat2;
    final Material mat3;
    final Material mat4;
    final Material mat5;
    final Material mat6;

    @Override
    public boolean intersect(Ray3D ray, Matrix4D worldToModel, Matrix4D modelToWorld) {
        Point3D p;
        double lambda;
        Point3D origin = ray.getOrigin().transform(worldToModel);
        Vector3D dir = ray.getDir().transform(worldToModel);

        lambda = (50 - origin.getZ()) / dir.getZ();
        if (ray.lessDistant(lambda)) {
            p = origin.add(dir.multiply(lambda));
            if (p.x <= 50 && p.x >= -50 && p.y <= 50 && p.y >= -50) {
                ray.setIntersection(p, new Vector3D(0, 0, d()), lambda, mat1);

                return true;
            }
        }

        lambda = (-50 - origin.getZ()) / dir.getZ();
        if (ray.lessDistant(lambda)) {
            p = origin.add(dir.multiply(lambda));
            if (p.x <= 50 && p.x >= -50 && p.y <= 50 && p.y >= -50) {
                ray.setIntersection(p, new Vector3D(0, 0, -d()), lambda, mat2);

                return true;
            }
        }

        lambda = (50 - origin.getY()) / dir.getY();
        if (ray.lessDistant(lambda)) {
            p = origin.add(dir.multiply(lambda));
            if (p.x <= 50 && p.x >= -50 && p.z <= 50 && p.z >= -50) {
                ray.setIntersection(p, new Vector3D(0, d(),0), lambda, mat3);
                ray.intersection.transformBack(modelToWorld);
                return true;
            }
        }

        lambda = (-50 - origin.getY()) / dir.getY();
        if (ray.lessDistant(lambda)) {
            p = origin.add(dir.multiply(lambda));
            if (p.x <= 50 && p.x >= -50 && p.z <= 50 && p.z >= -50) {
                ray.setIntersection(p, new Vector3D(0, -d(), 0), lambda, mat4);
                ray.intersection.transformBack(modelToWorld);
                return true;
            }
        }

        lambda = (50 - origin.getX()) / dir.getX();
        if (ray.lessDistant(lambda)) {
            p = origin.add(dir.multiply(lambda));
            if (p.y <= 50 && p.y >= -50 && p.z <= 50 && p.z >= -50) {
                ray.setIntersection(p, new Vector3D(d(), 0, 0), lambda, mat5);
                ray.intersection.transformBack(modelToWorld);
                return true;
            }
        }

        lambda = (-50 - origin.getX()) / dir.getX();
        if (ray.lessDistant(lambda)) {
            p = origin.add(dir.multiply(lambda));
            if (p.y <= 50 && p.y >= -50 && p.z <= 50 && p.z >= -50) {
                ray.setIntersection(p, new Vector3D(-d(), 0, 0), lambda, mat6);
                ray.intersection.transformBack(modelToWorld);
                return true;
            }
        }

        return false;

    }

    private double d() {
        return dir == Direction.IN ? -1 : 1;
    }
}

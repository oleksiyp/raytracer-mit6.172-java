package raytracer;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Square extends SceneObject {
    final Material material;

    public boolean intersect(Ray3D ray, Matrix4D worldToModel, Matrix4D modelToWorld) {
        Point3D origin = ray.origin.transform(worldToModel);
        Vector3D dir = ray.dir.transform(worldToModel);

        double lambda = (49.99 - origin.y) / dir.y;
        if (ray.lessDistant(lambda)) {
            Point3D p = origin.add(dir.multiply(lambda));
            if (p.x <= 16 && p.x >= -16 && p.z <= 16 && p.z >= -16) {
                ray.setIntersection(p, new Vector3D(0, -1, 0), lambda, material);
                ray.intersection.transformBack(modelToWorld);
                return true;
            }
        }

        return false;
    }
}

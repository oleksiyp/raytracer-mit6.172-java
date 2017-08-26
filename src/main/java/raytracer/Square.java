package raytracer;

import static raytracer.Point3D.origin;
import static raytracer.Vector3D.zero;

public class Square extends SceneObject {
    public static final Vector3D YM = new Vector3D(0, -1, 0);
    final Material material;

    public Square(Material material) {
        this.material = material;
    }

    Point3D origin = origin();
    Vector3D dir = zero();
    Point3D sqPt = origin();
    Vector3D sqVec = zero();

    public boolean intersect(Ray3D ray, Matrix4D worldToModel, Matrix4D modelToWorld) {
        origin.assign(ray.getOrigin());
        dir.assign(ray.getDir());

        origin.transform(worldToModel);
        dir.transform(worldToModel);

        double lambda = (49.99 - origin.y) / dir.y;
        if (ray.lessDistant(lambda)) {
            sqPt.assign(origin);
            sqVec.assign(dir);
            sqVec.multiply(lambda);
            sqPt.add(sqVec);
            if (sqPt.x <= 16 && sqPt.x >= -16 && sqPt.z <= 16 && sqPt.z >= -16) {
                ray.setIntersection(sqPt, YM, lambda, material);
                ray.intersection.transformBack(modelToWorld);
                return true;
            }
        }

        return false;
    }
}

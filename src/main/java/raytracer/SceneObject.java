package raytracer;

public abstract class SceneObject {

    public abstract boolean intersect(Ray3D ray,
                                      Matrix4D worldToModel,
                                      Matrix4D modelToWorld);
}

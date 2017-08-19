package raytracer;

public interface Raytracer {
    void traverseEntireScene(Ray3D ray, boolean casting);

    void computeShading(Ray3D ray, int depth, boolean getDirectly);
}

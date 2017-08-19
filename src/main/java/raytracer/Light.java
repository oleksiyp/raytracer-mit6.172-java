package raytracer;

public interface Light {
    void shade(Ray3D ray, Raytracer raytracer, boolean getDirectly);
}

package raytracer;

public interface Scene {
    void setCamera(Camera camera);

    SceneNode add(SceneObject sceneObject);
}

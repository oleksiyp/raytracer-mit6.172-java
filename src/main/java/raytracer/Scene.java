package raytracer;

public interface Scene {
    void setWH(int width, int height);

    void setCamera(Camera camera);

    void setRenderingOptions(RenderingOptions options);

    SceneNode add(SceneObject sceneObject);
}

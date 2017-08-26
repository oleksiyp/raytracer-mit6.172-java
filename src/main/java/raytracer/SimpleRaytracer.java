package raytracer;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static raytracer.Matrix4D.identity;

public class SimpleRaytracer implements Scene, Renderer {
    final SceneNode root;
    Camera camera;
    Matrix4D viewToWorld;
    PixBuf pixBuf;


    public SimpleRaytracer() {
        root = new SceneNode();
    }

    public SceneNode add(SceneObject sceneObject) {
        return new SceneNode(root, sceneObject);
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    public void render(RenderingOptions options) {
        int width = options.getWidth();
        int height = options.getHeight();

        pixBuf = new PixBuf(width, height);
        pixBuf.fill(options.getDefaultColour());

        initViewMatrix();

        int concurrency = 2;

        AtomicInteger j = new AtomicInteger(0);
        List<Thread> threads = new ArrayList<>();
        RaytracerTask firstTask = null;
        for (int i = 0; i < concurrency; i++) {
            RaytracerTask task = new RaytracerTask(j, pixBuf, viewToWorld, options);
            if (i == 0) {
                task.initObjects(root);
                firstTask = task;
            } else {
                task.initFrom(firstTask);
            }
            Thread thread = new Thread(task);
            threads.add(thread);
            thread.start();
        }

        try {
            for (Thread t : threads) {
                t.join();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void initViewMatrix() {
        Vector3D view = camera.getView().copy();
        view.normalize();

        Vector3D up = camera.getUp();
        Vector3D viewL = view.copy();
        viewL.multiply(up.dot(view));
        up.subtract(viewL);
        up.normalize();
        Vector3D w = view.copy();
        w.cross(up);
        Point3D eye = camera.getEye();

        viewToWorld = identity();

        viewToWorld.setElement(0, 0, w.x);
        viewToWorld.setElement(1, 0, w.y);
        viewToWorld.setElement(2, 0, w.z);

        viewToWorld.setElement(0, 1, up.x);
        viewToWorld.setElement(1, 1, up.y);
        viewToWorld.setElement(2, 1, up.z);

        viewToWorld.setElement(0, 2, view.x);
        viewToWorld.setElement(1, 2, view.y);
        viewToWorld.setElement(2, 2, view.z);

        viewToWorld.setElement(0, 3, eye.x);
        viewToWorld.setElement(1, 3, eye.y);
        viewToWorld.setElement(2, 3, eye.z);
    }


    public BufferedImage pixBufAsImage() {
        BufferedImage img = new BufferedImage(pixBuf.width, pixBuf.height, TYPE_INT_RGB);
        for (int j = 0; j < pixBuf.height; j++) {
            for (int i = 0; i < pixBuf.width; i++) {
                img.setRGB(i, j, this.pixBuf.getRGB(i, j));
            }
        }
        return img;
    }

}

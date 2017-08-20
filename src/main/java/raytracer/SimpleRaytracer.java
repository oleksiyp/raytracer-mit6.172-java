package raytracer;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static java.lang.Math.sqrt;
import static raytracer.Matrix4D.identity;
import static raytracer.Primitives.p;

public class SimpleRaytracer implements Scene, Raytracer, Renderer {
    final SceneNode root;

    final Matrix4D modelToWorld;
    final Matrix4D worldToModel;

    Camera camera;
    RenderingOptions options;

    Matrix4D viewToWorld;
    PixBuf pixBuf;

    private List<Light> lights;

    public SimpleRaytracer() {
        root = new SceneNode();

        modelToWorld = identity();
        worldToModel = identity();

        lights = new ArrayList<Light>();
    }

    public SceneNode add(SceneObject sceneObject) {
        return new SceneNode(root, sceneObject);
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    public void render(RenderingOptions options) {
        this.options = options;

        int width = options.getWidth();
        int height = options.getHeight();

        pixBuf = new PixBuf(width, height);
        pixBuf.fill(options.getDefaultColour());

        initObjects();
        initViewMatrix();

        Point3D origin = p(0, 0, 150);

        for (int j = 0; j < height; j++) {
            double y = (49.5 - 99 * j / (double) (height));
            for (int i = 0; i < width; i++) {
                double x = (99 * i / (double) (width) - 49.5);
                Point3D imagePlane = p(x, y, 49.9);
                Vector3D dir = imagePlane.subtract(origin).normalize();

                Ray3D ray = new Ray3D(imagePlane, dir);

                traverseEntireScene(ray, false);
                if (ray.getIntersection().isSet()) {
                    computeShading(ray, 6, false);
                    pixBuf.setPixel(i, j, ray.getColour());
                }
                System.out.print(i + " ");
            }
            System.out.println();
            System.out.println(j);
        }
    }

    private void initViewMatrix() {
        Vector3D view = camera.getView().normalize();
        Vector3D up = camera.getUp();
        up = up.subtract(view.multiply(up.dot(view)));
        up = up.normalize();
        Vector3D w = view.cross(up);
        Point3D eye = camera.getEye();

        viewToWorld = identity();

        viewToWorld.setElement(0, 0, w.x);
        viewToWorld.setElement(1, 0, w.y);
        viewToWorld.setElement(2, 0, w.z);

        viewToWorld.setElement(0, 1, up.x);
        viewToWorld.setElement(1, 1, up.y);
        viewToWorld.setElement(2, 1, up.z);

        viewToWorld.setElement(0, 2, -view.x);
        viewToWorld.setElement(1, 2, -view.y);
        viewToWorld.setElement(2, 2, -view.z);

        viewToWorld.setElement(0, 3, eye.x);
        viewToWorld.setElement(1, 3, eye.y);
        viewToWorld.setElement(2, 3, eye.z);
    }

    private void initObjects() {
        lights.clear();
        initObjects(root);
    }

    private void initObjects(SceneNode node) {
        if (node.sceneObject != null) {
            node.sceneObject.init(this);
        }

        if (node.sceneObject instanceof Light) {
            lights.add((Light) node.sceneObject);
        }

        for (SceneNode child : node.getChilds()) {
            initObjects(child);
        }
    }

    public void computeShading(Ray3D ray, int depth, boolean getDirectly) {
        if (depth <= 0) {
            ray.setColour(options.getDefaultColour());
            return;
        }

        Intersection ints = ray.intersection;
        Material mat = ints.mat;

        ints.normal = ints.normal.normalize();

        for (Light light : lights) {
            light.shade(ray, this, getDirectly);
        }

        if (mat.isDiffuse) {
            ray.clampColour();
            return;
        }

        if (mat.refractive.maxComponent() > 1e-2) {
            double n;
            if (ray.dir.dot(ints.normal) < 0) {
                n = 1.0 / mat.refractionIndex;
            } else {
                ints.normal = ints.normal.negate();
                n = mat.refractionIndex;
            }

            double cosI = ints.normal.dot(ray.dir);
            double sinT2 = n * n * (1.0 - cosI * cosI);

            Vector3D T;
            if (sinT2 < 1.0) {
                Vector3D lhs = ray.dir.multiply(n);
                Vector3D rhs = ints.normal
                        .multiply(n * cosI + sqrt(1.0 - sinT2));

                T = lhs.subtract(rhs);
            } else {
                Vector3D rhs = ints.normal.multiply(2 * ray.dir.dot(ints.normal));
                T = ray.dir.subtract(rhs);
            }

            Ray3D newRay = new Ray3D(ints.point, T);
            traverseEntireScene(newRay, true);

            if (newRay.intersection.isSet()) {
                computeShading(newRay, depth - 1, getDirectly);
            } else {
                newRay.setColour(options.getDefaultColour());
            }

            ray.setColour(ray.colour
                    .add(mat.refractive
                            .multiply(newRay.getColour())));

        }

        if (ray.dir.dot(ints.normal) < 0) {
            Vector3D R = ray.dir.subtract(
                    ints.normal.multiply((2 * ray.dir.dot(ints.normal))));

            Ray3D newRay = new Ray3D(ints.point, R);
            traverseEntireScene(newRay, false);

            if (newRay.intersection.isSet()) {
                computeShading(newRay, depth - 1, getDirectly);
            } else {
                newRay.setColour(options.getDefaultColour());
            }

            ray.setColour(ray.colour
                    .add(mat.specular
                            .multiply(newRay.getColour())));
        }

        ray.clampColour();

    }

    public void traverseEntireScene(Ray3D ray, boolean casting) {
        traverseScene(root, ray, casting);
    }

    private void traverseScene(SceneNode node, Ray3D ray, boolean casting) {
        SceneObject sceneObj = node.getSceneObject();
        if (sceneObj != null) {
            if (!node.isIdentity()) {
                modelToWorld.mulRight(node.getTransform());
                worldToModel.mulLeft(node.getInvTransform());
            }

            if (casting) {
                if (!(sceneObj instanceof Light)) {
                    sceneObj.intersect(ray, worldToModel, modelToWorld);
                }
            } else {
                sceneObj.intersect(ray, worldToModel, modelToWorld);
            }
        }
        for (SceneNode child : node.getChilds()) {
            traverseScene(child, ray, casting);
        }

        if (sceneObj != null) {
            if (!node.isIdentity()) {
                worldToModel.mulLeft(node.getTransform());
                modelToWorld.mulRight(node.getInvTransform());
            }
        }
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

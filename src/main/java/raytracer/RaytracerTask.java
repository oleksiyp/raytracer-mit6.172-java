package raytracer;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Math.sqrt;
import static java.lang.System.currentTimeMillis;
import static raytracer.Matrix4D.identity;
import static raytracer.Point3D.origin;
import static raytracer.StopWatch.*;
import static raytracer.Vector3D.zero;

public class RaytracerTask implements Runnable, Raytracer {
    private static final int MAX_DEPTH = 30;
    private final AtomicInteger row;
    private PixBuf pixBuf;
    private SceneNode root;
    private Matrix4D viewToWorld;

    final Matrix4D modelToWorld;
    final Matrix4D worldToModel;

    Point3D rayOrigin = origin();
    Vector3D rayDir = zero();

    private RenderingOptions options;

    public RaytracerTask(AtomicInteger row,
                         PixBuf pixBuf,
                         Matrix4D viewToWorld,
                         RenderingOptions options) {

        this.row = row;
        this.pixBuf = pixBuf;
        this.viewToWorld = viewToWorld;
        this.options = options;

        modelToWorld = identity();
        worldToModel = identity();

        lights = new ArrayList<>();
    }

    private List<Light> lights = new ArrayList<>();

    public void initObjects(SceneNode root) {
        lights.clear();
        this.root = root;
        initObjs(root, false);
    }

    public void initFrom(RaytracerTask otherTask) {
        root = otherTask.root.copy();
        lights.clear();
        initObjs(root, true);
    }


    private void initObjs(SceneNode node, boolean copy) {
        if (node.sceneObject != null) {
            node.sceneObject.init(this, copy);
        }

        if (node.sceneObject instanceof Light) {
            lights.add((Light) node.sceneObject);
        }

        if (!copy) {
            modelToWorld.mulRight(node.getTransform());
            worldToModel.mulLeft(node.getInvTransform());

            node.modelToWorld = new Matrix4D(modelToWorld);
            node.worldToModel = new Matrix4D(worldToModel);
        }

        for (SceneNode child : node.getChilds()) {
            initObjs(child, copy);
        }

        if (!copy) {
            worldToModel.mulLeft(node.getTransform());
            modelToWorld.mulRight(node.getInvTransform());
        }
    }

    @Override
    public void run() {
        int j;
        int height = pixBuf.getHeight();
        int width = pixBuf.getWidth();

        Ray3D ray = new Ray3D();
        Formatter fmt = new Formatter(System.out);

        while ((j = row.incrementAndGet()) < height) {
            double y = (49.5 - 99 * j / (double) (height));

            long t = currentTimeMillis();
            swReset();
            for (int i = 0; i < width; i++) {
                double x = (99 * i / (double) (width) - 49.5);
                rayDir.v(x, y, 100.1);
                rayDir.normalize();
                rayOrigin.p(x, y, 0);

                rayOrigin.transform(viewToWorld);
                rayDir.transform(viewToWorld);

                ray.set(rayOrigin, rayDir);
                traverseEntireScene(ray, false);
                if (ray.getIntersection().isSet()) {
                    computeShading(ray, 6, false);
                    pixBuf.setPixel(i, j, ray.getColour());
                }
            }
            long tt = currentTimeMillis();
            double total = (tt - t) / 1e3;
            fmt.format("%d %.3f ", j, total);
            StopWatch.report(fmt, total);
            fmt.format("%n");
        }
    }

    public void traverseEntireScene(Ray3D ray, boolean casting) {
        traverseScene(root, ray, casting);
    }

    private void traverseScene(SceneNode node, Ray3D ray, boolean casting) {
        SceneObject sceneObj = node.getSceneObject();
        if (sceneObj != null) {
            if (casting) {
                if (!(sceneObj instanceof Light)) {
                    sceneObj.intersect(ray, node.worldToModel, node.modelToWorld);
                }
            } else {
                sceneObj.intersect(ray, node.worldToModel, node.modelToWorld);
            }
        }

        List<SceneNode> childs = node.getChilds();
        for (int i = 0; i < childs.size(); i++) {
            SceneNode child = childs.get(i);
            traverseScene(child, ray, casting);
        }
    }

    StackItem []stack = new StackItem[MAX_DEPTH];
    int sp;
    {
        for (int i = 0; i < stack.length; i++) {
            stack[i] = new StackItem();
        }
    }

    @Override
    public void computeShading(Ray3D ray, int depth, boolean getDirectly) {
        StackItem item = stack[sp++];
            item.computeShading(ray, depth, getDirectly);
        sp--;
    }

    class StackItem {

        Vector3D dir = zero();
        Vector3D norm = zero();
        Ray3D newRay = new Ray3D();
        Colour csCol = Colour.black();

        public void computeShading(Ray3D ray, int depth, boolean getDirectly) {
            if (depth <= 0) {
                ray.setColour(options.getDefaultColour());
                return;
            }

            Intersection ints = ray.intersection;
            Material mat = ints.mat;

            ints.normal.normalize();

            for (int i = 0; i < lights.size(); i++) {
                Light light = lights.get(i);
                light.shade(ray, RaytracerTask.this, getDirectly);
            }

            if (mat.isDiffuse) {
                ray.clampColour();
                return;
            }

            norm.assign(ints.normal);
            if (mat.refractive.maxComponent() > 1e-2) {
                double n;
                if (ray.dir.dot(norm) < 0) {
                    n = 1.0 / mat.refractionIndex;
                } else {
                    norm.negate();
                    n = mat.refractionIndex;
                }

                double cosI = norm.dot(ray.dir);
                double sinT2 = n * n * (1.0 - cosI * cosI);

                if (sinT2 < 1.0) {
                    dir.assign(ray.dir);
                    dir.multiply(n);
                    norm.multiply(n * cosI + sqrt(1.0 - sinT2));
                    dir.subtract(norm);
                } else {
                    // Total internal reflection
                    dir.assign(ray.dir);
                    norm.multiply(2 * ray.dir.dot(norm));
                    dir.subtract(norm);
                }

                newRay.set(ints.point, dir);
                traverseEntireScene(newRay, true);

                if (newRay.intersection.isSet()) {
                    RaytracerTask.this.computeShading(newRay, depth - 1, getDirectly);
                } else {
                    newRay.setColour(options.getDefaultColour());
                }

                csCol.assign(mat.refractive);
                csCol.multiply(newRay.getColour());
                ray.addColour(csCol);

            }

            norm.assign(ints.normal);
            if (ray.dir.dot(norm) < 0) {
                dir.assign(ray.dir);
                norm.multiply(2 * ray.dir.dot(norm));
                dir.subtract(norm);

                newRay.set(ints.point, dir);

                traverseEntireScene(newRay, false);

                if (newRay.intersection.isSet()) {
                    RaytracerTask.this.computeShading(newRay, depth - 1, getDirectly);
                } else {
                    newRay.setColour(options.getDefaultColour());
                }

                csCol.assign(mat.specular);
                csCol.multiply(newRay.getColour());
                ray.addColour(csCol);
            }

            ray.clampColour();

        }
    }
}

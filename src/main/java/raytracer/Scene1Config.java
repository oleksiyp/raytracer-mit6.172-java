package raytracer;

import lombok.Getter;

import static raytracer.Primitives.c;
import static raytracer.Primitives.v;

@Getter
public class Scene1Config {
    // Camera parameters.
    Point3D eye = Primitives.p(0, 20, 100);
    Vector3D gaze = v(0, 0, -1);
    Vector3D up = v(0, 1, 0);
    double fov = 20;

    // Define materials for shading.
    Material red = new Material(
            c(0.92941176, 0.52156863, 0.14117647),
            c(0.0, 0.0, 0.0),
            4,
            c(0.0, 0.0, 0.0),
            1,
            true,
            false,
            false);

    Material green = new Material(
            c((double) 0, 0.8, (double) 0),
            c(0.0, 0.0, 0.0),
            4,
            c(0.0, 0.0, 0.0),
            1,
            true,
            false,
            false);

    Material blue = new Material(
            c(0.14117647, 0.52156863, 0.92941176),
            c(0.0, 0.0, 0.0),
            4,
            c(0.0, 0.0, 0.0),
            1,
            true,
            false,
            false);

    Material grey = new Material(
            c(0.7, 0.7, 0.7),
            c(0.0, 0.0, 0.0),
            4,
            c(0.0, 0.0, 0.0),
            1,
            true,
            false,
            false);

    Material yellow = new Material(
            c(0xEE / 255., 0xF7 / 255., 0x45 / 255.),
            c(0.0, 0.0, 0.0),
            4,
            c(0.0, 0.0, 0.0),
            1,
            true,
            false,
            false);

    Material magenta = new Material(
            c(0xE0 / 255., 0x1B / 255., 0x4C / 255.),
            c(0.0, 0.0, 0.0),
            4,
            c(0.0, 0.0, 0.0),
            1,
            true,
            false,
            false);

    Material glass = new Material(
            c(0.0, 0.0, 0.0),
            c(0.07, 0.07, 0.07),
            5,
            c(0.89, 0.89, 0.89),
            1.8,
            false,
            true,
            false);

    Material water = new Material(
            c(0.0, 0.00, 0.0),
            c(0.07, 0.07, 0.07),
            5,
            c(0.93, 0.93, 0.93),
            1.3,
            false,
            true,
            false);

    Material mirror = new Material(
            c(0.0, 0.0, 0.0),
            c(0.7, 0.7, 0.7),
            25,
            c(0.0, 0.0, 0.0),
            1.7,
            false,
            true,
            false);

    Material lightmat = new Material(
            c(1.0, 1.0, 1.0),
            c(0.0, 0.0, 0.0),
            55,
            c(0.0, 0.0, 0.0),
            1.0,
            false,
            true,
            true);

    SceneNode mirrorSphere;
    SceneNode glassSphere;
    SceneNode photonLight;
    SceneNode sceneCube;

    public void config(Scene scene) {
        sceneCube = scene.add(new Cube(Direction.IN, grey, grey, grey, grey, blue, red));

        // Mirror surface sphere
        mirrorSphere = scene.add(new Sphere(mirror));

        // Solid glass sphere
        glassSphere = scene.add(new Sphere(glass));

        photonLight = scene.add(new SquarePhotonLight(
                c(15000.0, 15000.0, 15000.0),
                1000,
                10000,
                lightmat,
                new LightOptions(
                        13,
                        true,
                        true,
                        4,
                        false)));
        scene.setCamera(new Camera(eye, gaze, up, fov));
    }

}

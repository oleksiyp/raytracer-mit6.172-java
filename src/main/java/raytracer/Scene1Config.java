package raytracer;

import lombok.Getter;

import static raytracer.Primitives.c;
import static raytracer.Primitives.v;

@Getter
public class Scene1Config extends CommonConfig {

    SceneNode sceneCube;
    SceneNode mirrorSphere;
    SceneNode glassSphere;
    SceneNode photonLight;

    public void config(Scene scene) {
        sceneCube = scene.add(new Cube(Direction.IN, grey, grey, grey, grey, blue, red));

        // Mirror surface sphere
        mirrorSphere = scene.add(new Sphere(mirror));

        // Solid glass sphere
        glassSphere = scene.add(new Sphere(glass));

        photonLight = scene.add(new SquarePhotonLight(
                c(15000.0, 15000.0, 15000.0),
                lightmat,
                new LightOptions(
                        1000,
                        10000,
                        28,
                        13,
                        true,
                        true,
                        4,
                        1,
                        1,
                        30,
                        200)));
        scene.setCamera(new Camera(eye, gaze, up, fov));
    }

}

package raytracer;

import lombok.Getter;

import static raytracer.Primitives.c;

@Getter
public class Scene2Config extends CommonConfig {

    SceneNode sceneCube;
    SceneNode mirrorSphere;
    SceneNode glassSphere;
    SceneNode photonLight;

    public void config(Scene scene) {
        sceneCube = scene.add(new Cube(Direction.IN, grey, grey, grey, grey, green, blue));

        // Mirror surface sphere
        mirrorSphere = scene.add(new Sphere(mirror));
        mirrorSphere.translate(new Vector3D(-27, -30, -30));

        // Water surface
        scene.add(new DisplacedSurface(water,
                22.0,
                499,
                499,
                new PerlinNoise(1, 0.14, 1.0, 3)));


        photonLight = scene.add(new SquarePhotonLight(
                c(15000.0, 15000.0, 15000.0),
                lightmat,
                new LightOptions(
                        1000,
                        2000000,
                        28,
                        13,
                        true,
                        true,
                        1,
                        1,
                        1,
                        30,
                        200)));
        scene.setCamera(new Camera(eye, gaze, up, fov));
    }

}

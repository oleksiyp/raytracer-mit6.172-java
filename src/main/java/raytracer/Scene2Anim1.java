package raytracer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static raytracer.Primitives.EPSILON;
import static raytracer.Primitives.p;
import static raytracer.Primitives.v;

public class Scene2Anim1 {
    SimpleRaytracer raytracer = new SimpleRaytracer();
    Scene2Config cfg = new Scene2Config();

    public static void main(String[] args) throws IOException {
        new Scene2Anim1()
                .run();
    }

    void run() throws IOException {
        cfg.config(raytracer);

        int frame = 0;
        long timer = System.currentTimeMillis();
        DisplacedSurface displacedSurface = (DisplacedSurface) cfg.waterSurface.sceneObject;

//        for (int i = 0; i < 120; i++) {
        int i = 120;
        {
            Point3D eye = p(0, 10, 40);
            Point3D l = p(0, 50, 0);
            Vector3D gaze = (l.subtract(eye)).normalize();
            Vector3D up = gaze.cross(cfg.up.cross(gaze)).normalize();

            raytracer.setCamera(new Camera(eye, gaze, up, cfg.fov));

            displacedSurface.makeSurface(i / 2.0);
            raytracer.render(new RenderingOptions(
                    600,
                    600,
                    new Colour(0.4, 0.4, 0.4)));

            BufferedImage img = raytracer.pixBufAsImage();
            ImageIO.write(img, "PNG", new File("out" + frame + ".png"));
            long ttimer = System.currentTimeMillis();
            System.out.printf("%6.2f Frame %d%n", (ttimer - timer) / 1000., frame);
            timer = ttimer;
            frame++;
        }

    }

}

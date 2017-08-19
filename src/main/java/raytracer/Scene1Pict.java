package raytracer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static raytracer.Primitives.EPSILON;
import static raytracer.Primitives.v;

public class Scene1Pict {
    SimpleRaytracer raytracer = new SimpleRaytracer();
    Scene1Config cfg = new Scene1Config();

    public static void main(String[] args) throws IOException {
        new Scene1Pict()
                .run();
    }

    void run() throws IOException {
        cfg.config(raytracer);
        cfg.mirrorSphere.translate(v(-27, -30, -30));
        cfg.glassSphere.translate(v(29, -30, -5));

        long timer = System.currentTimeMillis();
        raytracer.render(new RenderingOptions(
                600,
                600,
                new Colour(0.4, 0.4, 0.4)));

        BufferedImage img = raytracer.pixBufAsImage();
        ImageIO.write(img, "PNG", new File("out.png"));
        long ttimer = System.currentTimeMillis();
        System.out.printf("%6.2f%n", (ttimer - timer) / 1000.);
    }

}

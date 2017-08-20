package raytracer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static raytracer.Primitives.v;

public class Scene2Pict {
    SimpleRaytracer raytracer = new SimpleRaytracer();
    Scene2Config cfg = new Scene2Config();

    public static void main(String[] args) throws IOException {
        new Scene2Pict()
                .run();
    }

    void run() throws IOException {
        cfg.config(raytracer);

        long timer = System.currentTimeMillis();
        raytracer.render(new RenderingOptions(
                600,
                600,
                new Colour(0.4, 0.4, 0.4)));

        BufferedImage img = raytracer.pixBufAsImage();
        ImageIO.write(img, "PNG", new File("scene2.png"));
        long ttimer = System.currentTimeMillis();
        System.out.printf("%6.2f%n", (ttimer - timer) / 1000.);
    }

}

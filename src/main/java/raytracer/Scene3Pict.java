package raytracer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Scene3Pict {
    SimpleRaytracer raytracer = new SimpleRaytracer();
    Scene3Config cfg = new Scene3Config();

    public static void main(String[] args) throws IOException {
        new Scene3Pict()
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
        ImageIO.write(img, "PNG", new File("scene3.png"));
        long ttimer = System.currentTimeMillis();
        System.out.printf("%6.2f%n", (ttimer - timer) / 1000.);
    }

}

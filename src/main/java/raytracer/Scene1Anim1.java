package raytracer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static raytracer.Primitives.EPSILON;
import static raytracer.Primitives.v;

public class Scene1Anim1 {
    SimpleRaytracer raytracer = new SimpleRaytracer();
    Scene1Config cfg = new Scene1Config();

    public static void main(String[] args) throws IOException {
        new Scene1Anim1()
                .run();
    }

    void run() throws IOException {
        cfg.config(raytracer);

        double y = 30;
        double v = 0, g = -1;
        int st = 0;
        double py = 30;

        double dv = 0;

        double gx = 30;
        double px = 30;

        cfg.mirrorSphere.translate(v(-27, y, -30));
        cfg.glassSphere.translate(v(30, -30, 30));
        int frame = 0;
        long timer = System.currentTimeMillis();
        for (int i = 0; i < 120; i++) {
            raytracer.render(new RenderingOptions(
                    600,
                    600,
                    new Colour(0.4, 0.4, 0.4)));
            int t = i - st;
            double d = y + v * t + g * 0.5 * t * t;
            if (d < -30) {
                y = -30 - (d + 30);
                if (dv <= EPSILON) {
                    dv = -g * t;
                }
                v = dv;
                st = i;
            }
            gx = 30 - i / 2;
            cfg.mirrorSphere.translate(v(0, d - py, 0));
            cfg.glassSphere.translate(v(gx - px, 0, 0));
            py = d;
            px = gx;

            BufferedImage img = raytracer.pixBufAsImage();
            ImageIO.write(img, "PNG", new File("out" + frame + ".png"));
            long ttimer = System.currentTimeMillis();
            System.out.printf("%6.2f Frame %d%n", (ttimer - timer) / 1000., frame);
            timer = ttimer;
            frame++;
        }

    }

}

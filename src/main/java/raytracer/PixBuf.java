package raytracer;

import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;

@Getter
@ToString
public class PixBuf {
    final int width;
    final int height;
    final byte[] r;
    final byte[] g;
    final byte[] b;

    public PixBuf(int width, int height) {
        this.width = width;
        this.height = height;
        r = new byte[width * height];
        g = new byte[width * height];
        b = new byte[width * height];
    }

    public void fill(Colour colour) {
        Arrays.fill(r, toByte(colour.r));
        Arrays.fill(g, toByte(colour.g));
        Arrays.fill(b, toByte(colour.b));
    }

    private byte toByte(double val) {
        return (byte)(int)(val * 255);
    }

    public void setPixel(int i, int j, Colour colour) {
        r[j * width + i] = toByte(colour.r);
        g[j * width + i] = toByte(colour.g);
        b[j * width + i] = toByte(colour.b);
    }

    public int getRGB(int i, int j) {
        int rr = r[j * width + i];
        rr &= 0xFF;
        rr <<= 16;
        int gg = g[j * width + i];
        gg &= 0xFF;
        gg <<= 8;
        int bb = b[j * width + i];
        bb &= 0xFF;

        return rr | gg | bb;
    }
}

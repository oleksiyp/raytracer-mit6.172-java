package raytracer;

import lombok.Getter;
import lombok.ToString;

import static java.lang.Math.sqrt;

@Getter
@ToString
public class PerlinNoise {
    public static final int SAMPLE_SIZE = 1024;

    private static final int B = SAMPLE_SIZE;
    private static final int BM = (SAMPLE_SIZE - 1);

    private static final int N = 0x1000;
    private static final int NP = 12;
    private static final int NM = 0xfff;

    boolean start = false;

    final int octaves;
    final double freq;
    final double amp;
    final int seed;

    int []p = new int[SAMPLE_SIZE + SAMPLE_SIZE + 2];
    double [][]g3 = new double[SAMPLE_SIZE + SAMPLE_SIZE + 2][3];
    double [][]g2 = new double[SAMPLE_SIZE + SAMPLE_SIZE + 2][2];
    double []g1 = new double[SAMPLE_SIZE + SAMPLE_SIZE + 2];


    public PerlinNoise(int octaves, double freq, double amp, int seed) {
        this.octaves = octaves;
        this.freq = freq;
        this.amp = amp;
        this.seed = seed;
    }

    public double get(double x, double y, double time) {
        double[] vec = {x, y, time};
        return perlin_noise_3D(vec);
    }

    private double perlin_noise_3D(double[] vec) {
        int terms = octaves;
        double result = 0.0f;
        double amp = this.amp;

        vec[0] *= freq;
        vec[1] *= freq;
        vec[2] *= freq;

        for (int i = 0; i < terms; i++) {
            result += noise3(vec) * amp;
            vec[0] *= 2.0f;
            vec[1] *= 2.0f;
            vec[2] *= 2.0f;
            amp *= 0.5f;
        }

        return result;
    }


    private double noise3(double[] vec) {
        int bx0, bx1, by0, by1, bz0, bz1, b00, b10, b01, b11;
        double rx0, rx1, ry0, ry1, rz0, rz1, sy, sz, a, b, c, d, t, u, v;
        int i, j;
        double []q;

        if (start) {
//            srand(mSeed);
            start = false;
            init();
        }

        t = vec[0] + N;
        bx0 = ((int)t) & BM;
        bx1 = (bx0 + 1) & BM;
        rx0 = t - (int) t;
        rx1 = rx0 - 1.0f;

        t = vec[1] + N;
        by0 = ((int)t) & BM;
        by1 = (by0 + 1) & BM;
        ry0 = t - (int) t;
        ry1 = ry0 - 1.0f;

        t = vec[2] + N;
        bz0 = ((int)t) & BM;
        bz1 = (bz0 + 1) & BM;
        rz0 = t - (int) t;
        rz1 = rz0 - 1.0f;

        i = p[bx0];
        j = p[bx1];

        b00 = p[i + by0];
        b10 = p[j + by0];
        b01 = p[i + by1];
        b11 = p[j + by1];

        t = s_curve(rx0);
        sy = s_curve(ry0);
        sz = s_curve(rz0);

        q = g3[b00 + bz0];
        u = at3(rx0, ry0, rz0, q);
        q = g3[b10 + bz0];
        v = at3(rx1, ry0, rz0, q);
        a = lerp(t, u, v);

        q = g3[b01 + bz0];
        u = at3(rx0, ry1, rz0, q);
        q = g3[b11 + bz0];
        v = at3(rx1, ry1, rz0, q);
        b = lerp(t, u, v);

        c = lerp(sy, a, b);

        q = g3[b00 + bz1];
        u = at3(rx0, ry0, rz1, q);
        q = g3[b10 + bz1];
        v = at3(rx1, ry0, rz1, q);
        a = lerp(t, u, v);

        q = g3[b01 + bz1];
        u = at3(rx0, ry1, rz1, q);
        q = g3[b11 + bz1];
        v = at3(rx1, ry1, rz1, q);
        b = lerp(t, u, v);

        d = lerp(sy, a, b);

        return lerp(sz, c, d);
    }


    private double s_curve(double t) {
        return (t * t * (3.0f - 2.0f * t));
    }

    private double lerp(double t, double a, double b) {
        return (a + t * (b - a));
    }

    private double at3(double rx, double ry, double rz, double []q) {
        return rx * q[0] + ry * q[1] + rz * q[2];
    }

    void init() {
        int i, j, k;

        for (i = 0; i < B; i++) {
            p[i] = i;
            g1[i] = (double) ((rand() % (B + B)) - B) / B;
            for (j = 0; j < 2; j++)
                g2[i][j] = (double) ((rand() % (B + B)) - B) / B;
            normalize2(g2[i]);
            for (j = 0; j < 3; j++)
                g3[i][j] = (double) ((rand() % (B + B)) - B) / B;
            normalize3(g3[i]);
        }

        while (--i > 0) {
            k = p[i];
            p[i] = p[j = rand() % B];
            p[j] = k;
        }

        for (i = 0; i < B + 2; i++) {
            p[B + i] = p[i];
            g1[B + i] = g1[i];
            for (j = 0; j < 2; j++)
                g2[B + i][j] = g2[i][j];
            for (j = 0; j < 3; j++)
                g3[B + i][j] = g3[i][j];
        }
    }

    private int rand() {
        return 0;
    }

    void normalize2(double []v)
    {
        double s;

        s = sqrt(v[0] * v[0] + v[1] * v[1]);
        s = 1.0f / s;
        v[0] = v[0] * s;
        v[1] = v[1] * s;
    }

    void normalize3(double []v)
    {
        double s;

        s = sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
        s = 1.0f / s;

        v[0] = v[0] * s;
        v[1] = v[1] * s;
        v[2] = v[2] * s;
    }

}

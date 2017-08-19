package raytracer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class PerlinNoise {
    final int octaves;
    final double freq;
    final double amp;
    final int seed;

    public double get(double v, double zc, double time) {
        return 0;
    }
}

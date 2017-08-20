package raytracer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import static java.lang.Math.max;
import static java.lang.Math.min;

@AllArgsConstructor
@Getter
@ToString
public class Colour {
    final double r, g, b;

    public Colour clamp() {
        return new Colour(min(1.0, r), min(1.0, g), min(1.0, b));
    }

    public double maxComponent() {
        return max(r, max(g, b));
    }

    public Colour multiply(Colour c) {
        return new Colour(r * c.r,
                g * c.g,
                b * c.b);
    }

    public Colour add(Colour c) {
        return new Colour(r + c.r,
                g + c.g,
                b + c.b);
    }

    public Colour divide(double s) {
        return new Colour(r / s, g / s, b / s);
    }

    public Colour multiply(double s) {
        return new Colour(r * s, g * s, b * s);
    }
}

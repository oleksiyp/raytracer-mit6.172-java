package raytracer;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import static java.lang.Math.max;
import static java.lang.Math.min;

@AllArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
public class Colour {
    double r, g, b;

    public void clamp() {
        r = min(1.0, r);
        g = min(1.0, g);
        b = min(1.0, b);
    }

    public double maxComponent() {
        return max(r, max(g, b));
    }

    public void multiply(Colour c) {
        r *= c.r;
        g *= c.g;
        b *= c.b;
    }

    public void add(Colour c) {
        r += c.r;
        g += c.g;
        b += c.b;
    }

    public void multiply(double s) {
        r *= s;
        g *= s;
        b *= s;
    }

    public static Colour black() {
        return new Colour(0, 0, 0);
    }

    public void assign(Colour col) {
        r = col.r;
        g = col.g;
        b = col.b;
    }

    public void c(double r, double g, double b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public Colour copy() {
        return new Colour(r, g, b);
    }
}

package raytracer;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@ToString
@Getter
@EqualsAndHashCode
public class Point3Di {
    int x, y, z;

    public int coord(int plane) {
        if (plane == 0) return x;
        if (plane == 1) return y;
        return z;
    }
}

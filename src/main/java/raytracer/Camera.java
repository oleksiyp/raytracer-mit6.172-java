package raytracer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class Camera {
    final Point3D eye;
    final Vector3D view;
    final Vector3D up;
    final double fov;
}

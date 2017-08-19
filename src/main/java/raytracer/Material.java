package raytracer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class Material {
    final Colour diffuse;
    final Colour specular;
    final double specularExp;
    final Colour refractive;
    final double refractionIndex;
    final boolean isDiffuse;
    final boolean isSpecular;
    final boolean isLight;
}

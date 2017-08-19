package raytracer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class RenderingOptions {
    final int width;
    final int height;
    final Colour defaultColour;
}

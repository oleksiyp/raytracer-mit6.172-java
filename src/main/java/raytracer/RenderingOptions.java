package raytracer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class RenderingOptions {
    final boolean softShadows;
    final boolean directIllumination;
    final boolean globalIllumination;
    final boolean caustics;
    final Colour defaultColour;
}

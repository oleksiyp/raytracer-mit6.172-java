package raytracer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class LightOptions {
    final int nGlobalIllumination;
    final boolean caustics;
    final boolean directIllumination;
    final int nSoftShadows;
    final boolean showPhotonMap;
}

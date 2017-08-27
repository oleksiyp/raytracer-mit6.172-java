package raytracer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class LightOptions {
    final int numPhotons;
    final int numCausticPhotons;

    final int nGlobalIlluminationN;
    final int nGlobalIlluminationM;
    final boolean simpleDiffuse;
    final boolean caustics;
    final boolean directIllumination;

    final int nSoftShadows;
    final double irradianceCacheTolerance;

    final double irradianceCacheSpacing;
    final int indirectMaxDistance;
    final int indirectMaxPhotons;
}

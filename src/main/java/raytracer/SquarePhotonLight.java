package raytracer;

import lombok.Getter;
import lombok.ToString;

import static java.lang.Math.abs;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

@Getter
@ToString
public class SquarePhotonLight extends Square implements Light {
    final Colour colour;
    final int numPhotons;
    final int numCausticPhotons;
    final LightOptions lightOpts;
    private IrradianceCache icache;

    public SquarePhotonLight(
            Colour colour,
            int numPhotons,
            int numCausticPhotons,
            Material material,
            LightOptions lightOpts) {
        super(material);
        this.colour = colour;
        this.numPhotons = numPhotons;
        this.numCausticPhotons = numCausticPhotons;
        this.lightOpts = lightOpts;
        icache = new IrradianceCache();
    }

    public void shade(Ray3D ray, Raytracer raytracer) {
        Material mat = ray.intersection.mat;
        if (mat.isLight) {
            ray.setColour(mat.diffuse);
            return;
        }

        if (lightOpts.showPhotonMap) {
            return;
        }

        if (mat.isDiffuse) {
            if (lightOpts.nGlobalIllumination > 0) {
                globalIllumination(ray);
            }
            if (lightOpts.caustics) {
                causticsIllumination(ray);
            }
        }

        if (lightOpts.directIllumination) {
            directIllumination(ray, raytracer);
        }

    }

    private void causticsIllumination(Ray3D ray) {

    }

    private void globalIllumination(Ray3D ray) {
//        // If we're not already in the irradiance cache, compute the irradiance via
//        // monte carlo methods.
//        Intersection ints = ray.intersection;
//
//        if (!icache.getIrradiance(ints.point, ints.normal)) {
//            Colour c;
//
//            ray.setColour(new Colour(0, 0, 0));
//
//            int N = lightOpts.nGlobalIllumination;
//            int M = lightOpts.nGlobalIllumination;
//            int hits = 0;
//            double r0 = 0;
//
//            Matrix4D basis = initTransformMatrix(ints.normal);
//
//            // Stratification
//            for (int i = 0; i < N; i++) {
//                double phi = 2 * M_PI * ((double) i + r.Random1()) / N;
//                double sinPhi = sin(phi);
//                double cosPhi = cos(phi);
//
//                for (int j = 0; j < M; j++) {
//                    double cosTheta = sqrt(1 - (((double) j + r.Random1()) / M));
//                    double theta = acos(cosTheta);
//                    double sinTheta = sin(theta);
//
//                    Vector3D v = Vector3D(cosPhi * sinTheta, sinPhi * sinTheta, cosTheta);
//                    v = v.transform(basis);
//                    v.normalize();
//
//                    Ray3D new_ray = Ray3D(ints.point, v);
//                    raytracer->traverseEntireScene(new_ray, true);
//
//                    if (!new_ray.intersection.none) {
//                        raytracer->computeShading(new_ray, 3, true);
//                        ray.col += new_ray.col;
//
//                        r0 += 1 / new_ray.intersection.t_value;
//                        hits++;
//                    }
//                }
//            }
//
//            ray.col *= 1.0 / hits;
//            r0 = 1 / r0;
//
//            if (hits == N * M) {
//                icache.insert(ints.point, ints.normal, r0, ray.col);
//            }
//        }
//        ray.col *= ints.mat->diffuse;

    }

    private Matrix4D initTransformMatrix(Vector3D w) {
        Vector3D u, v;
        double vx, vy, vz;

        if ((abs(w.x) < abs(w.y)) && (abs(w.x) < abs(w.z))) {
            vx = 0;
            vy = w.z;
            vz = -w.y;
        } else if (abs(w.y) < abs(w.z)) {
            vx = w.z;
            vy = 0;
            vz = -w.x;
        } else {
            vx = w.y;
            vy = -w.x;
            vz = 0;
        }
        v = new Vector3D(vx, vy, vz);
        v = v.normalize();
        u = v.cross(w);

        Matrix4D mat = new Matrix4D();
        mat.values[0][0] = u.x;
        mat.values[1][0] = u.y;
        mat.values[2][0] = u.z;
        mat.values[0][1] = v.x;
        mat.values[1][1] = v.y;
        mat.values[2][1] = v.z;
        mat.values[0][2] = w.x;
        mat.values[1][2] = w.y;
        mat.values[2][2] = w.z;
        return mat;
    }

    private void directIllumination(Ray3D ray, Raytracer raytracer) {
        // Direct illumination
        int N = lightOpts.nSoftShadows;

        Colour directCol = new Colour(0, 0, 0);
        double dx = 1.0 / (N + 1);
        double dz = 1.0 / (N + 1);

        // Loop for soft shadows
        for (int i = 1; i <= N; i++) {
            double x = i * dx * 30 - 15;

            for (int j = 1; j <= N; j++) {
                double z = i * dz * 30 - 15;

                Vector3D L = new Point3D(x, 50, z)
                        .subtract(ray.intersection.point);
                double l = sqrt(L.x * L.x + L.z * L.z + L.y * L.y);
                L = L.normalize();
                Vector3D LN = new Vector3D(0, -1, 0);

                double scale = -LN.dot(L);
                scale = scale < 0 ? 0 : scale;
                scale /= l * l * 1.5 * Math.PI;

                Ray3D newRay = new Ray3D(ray.intersection.point, L);
                raytracer.traverseEntireScene(newRay, false);

                if (newRay.intersection.isSet() && newRay.intersection.mat.isLight){
                    Vector3D R =  ray.intersection.normal.multiply(2.0 * ray.intersection.normal.dot(L)).subtract(L);

                    double NdotL = L.dot(ray.intersection.normal);
                    double RdotV = -(R.dot(ray.dir));
                    NdotL = NdotL < 0 ? 0 : NdotL;
                    RdotV = RdotV < 0 ? 0 : RdotV;
                    if (ray.dir.dot(ray.intersection.normal) > 0) {
                        RdotV = 0;
                    }

                    Material mat = ray.intersection.mat;

                    Colour diffuse = mat.diffuse.multiply(NdotL);
                    double specPow = pow(RdotV, mat.specularExp);
                    Colour specular = mat.specular.multiply(specPow);
                    Colour col = colour.multiply(scale).multiply(diffuse.add(specular));
                    directCol = directCol.add(col);
                }
            }
        }
        directCol = directCol.divide(N * N);
        ray.setColour(ray.colour.add(directCol));
    }
}

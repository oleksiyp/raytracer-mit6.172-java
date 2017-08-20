package raytracer;

import lombok.Getter;
import lombok.ToString;

import static java.lang.Math.*;

@Getter
@ToString
public class SquarePhotonLight extends Square implements Light {
    final Colour colour;
    final LightOptions lightOpts;
    IrradianceCache icache;
    IrradianceCache directCache;

    final Random rnd;

    BalancedPhotonMap bmap;
    BalancedPhotonMap cmap;

    public SquarePhotonLight(
            Colour colour,
            Material material,
            LightOptions lightOpts) {
        super(material);
        this.colour = colour;
        this.lightOpts = lightOpts;
        rnd = new Random();
    }

    @Override
    public void init(Raytracer raytracer) {
        icache = new IrradianceCache(
                lightOpts.irradianceCacheTolerance,
                lightOpts.irradianceCacheSpacing);

        directCache = new IrradianceCache(
                lightOpts.irradianceCacheTolerance,
                lightOpts.irradianceCacheSpacing);

        cmap = bmap = null;

        tracePhotonMap(lightOpts.numPhotons, lightOpts.numCausticPhotons, raytracer);
    }

    public void shade(Ray3D ray, Raytracer raytracer, boolean getDirectly) {
        Material mat = ray.intersection.mat;
        if (mat.isLight) {
            ray.setColour(mat.diffuse);
            return;
        }

        if (getDirectly) {
            Vector3D normal = ray.intersection.normal;
            normal = normal.normalize();

            Colour irr = bmap.irradianceEstimate(
                    ray.intersection.point,
                    normal,
                    lightOpts.indirectMaxDistance,
                    lightOpts.indirectMaxPhotons);

            if (irr == null) {
                irr = ray.colour;
            }

            ray.setColour(irr.multiply(mat.diffuse));
            return;
        }

        if (mat.isDiffuse) {
            if (lightOpts.nGlobalIlluminationN > 0 && lightOpts.nGlobalIlluminationM > 0) {
                globalIllumination(ray, raytracer);
            }
            if (lightOpts.caustics) {
                causticsIllumination(ray);
            }
        }

        if (lightOpts.directIllumination) {
            directIllumination(ray, raytracer);
        }

    }

    private void tracePhotonMap(int num, int causticsNum, Raytracer raytracer) {
        PhotonMap map = new PhotonMap(num * 4);

        int i = 0;
        while (i < num) {
            // Calculate the start pos and direction of the photon
            Point3D p = new Point3D(rnd.random2() * 16, 49.99, rnd.random2() * 16);
            Vector3D v = new Vector3D(0, -1, 0);
            v = getRandLambertianDir(v);
            Ray3D ray = new Ray3D(p, v);
            ray.colour = colour;

            int count = 0;
            while (ray.colour.maxComponent() > 0.1 && count++ < 100) {
                raytracer.traverseEntireScene(ray, true);

                Intersection ints = ray.intersection;
                if (!ints.isSet()) {
                    i--;
                    break;
                }

                Vector3D dir_norm = ray.dir;
                dir_norm = dir_norm.normalize();
                if (ints.mat.isDiffuse) {
                    map.storePhoton(
                            ray.colour,
                            ints.point,
                            dir_norm);
                }

                double ran = rnd.random1();

                Colour c = ray.colour.multiply(ints.mat.diffuse);
                double P = c.maxComponent() / ray.colour.maxComponent();
                if (ran < P) {
                    // Diffuse reflection
                    v = getRandLambertianDir(ints.normal);
                } else {
                    ran -= P;
                    c = ray.colour.multiply(ints.mat.specular);
                    P = c.maxComponent() / ray.colour.maxComponent();
                    if (ran < P) {
                        // Specular reflection
                        v = ray.dir.subtract(
                                ints.normal.multiply(2 * ray.dir.dot(ints.normal)));
                    } else {
                        ran -= P;
                        c = ray.colour.multiply(ints.mat.refractive);
                        P = c.maxComponent() / ray.colour.maxComponent();
                        if (ran < P) {
                            // Refraction
                            double n;

                            if (ray.dir.dot(ints.normal) < 0) {
                                n = 1 / ints.mat.refractionIndex;
                            } else {
                                ints.normal = ints.normal.negate();
                                n = ints.mat.refractionIndex;
                            }

                            double cosI = ints.normal.dot(ray.dir);
                            double sinT2 = n * n * (1.0 - cosI * cosI);

                            if (sinT2 < 1.0) {
                                Vector3D lhs = ray.dir.multiply(n);
                                Vector3D rhs = ints.normal.multiply(n * cosI + sqrt(1.0 - sinT2));
                                v = lhs.subtract(rhs);
                            } else {
                                // Total internal reflection
                                Vector3D rhs = ints.normal.multiply(2 * ray.dir.dot(ints.normal));
                                v = ray.dir.subtract(rhs);
                            }
                        } else {
                            // Absorption
                            break;
                        }

                    }
                }
                ray = new Ray3D(ints.point, v);
                ray.setColour(c.divide(P));
            }
            i++;
        }
        map.scalePhotonPower(1.0 / i);
        bmap = map.balance();

        // Caustics
        map = new PhotonMap(causticsNum);

        int emitted = 0;
        i = 0;

        while (i < causticsNum) {
            // Calculate the start pos and direction of the photon
            Point3D p;

            if (lightOpts.nSoftShadows > 1) {
                p = new Point3D(rnd.random2() * 18, 49.99, rnd.random2() * 18);
            } else {
                p = new Point3D(0, 49.99, 0);
            }
            Vector3D v = new Vector3D(0, -1, 0);
            v = getRandLambertianDir(v);
            Ray3D ray = new Ray3D(p, v);
            ray.colour = colour;
            emitted++;

            raytracer.traverseEntireScene(ray, true);

            if (ray.intersection.isSet() && ray.intersection.mat.isSpecular) {
                while (true) {
                    raytracer.traverseEntireScene(ray, true);

                    if (!ray.intersection.isSet()) {
                        break;
                    }

                    Vector3D dirNorm = ray.dir;
                    dirNorm = dirNorm.normalize();

                    if (ray.intersection.mat.isDiffuse){
                        map.storePhoton(
                                ray.colour,
                                ray.intersection.point,
                                dirNorm);

                        i++;
                        break;
                    }

                    double ran = rnd.random1();
                    Colour c = ray.colour.multiply(ray.intersection.mat.specular);
                    double P = c.maxComponent() / ray.colour.maxComponent();
                    Intersection ints = ray.intersection;

                    // Specular reflection
                    if (ran < P) {
                        ray.intersection.normal = ray.intersection.normal.negate();

                        v = ray.dir.subtract(
                                ints.normal.multiply(2 * ray.dir.dot(ints.normal)));
                    } else {
                        ran -= P;
                        c = ray.colour.multiply(ints.mat.refractive);
                        P = c.maxComponent() / ray.colour.maxComponent();
                        if (ran < P) {
                            // Refraction
                            double n;

                            if (ray.dir.dot(ints.normal) < 0) {
                                n = 1 / ints.mat.refractionIndex;
                            } else {
                                ints.normal = ints.normal.negate();
                                n = ints.mat.refractionIndex;
                            }

                            double cosI = ints.normal.dot(ray.dir);
                            double sinT2 = n * n * (1.0 - cosI * cosI);

                            if (sinT2 < 1.0) {
                                Vector3D lhs = ray.dir.multiply(n);
                                Vector3D rhs = ints.normal.multiply(n * cosI + sqrt(1.0 - sinT2));
                                v = lhs.subtract(rhs);
                            } else {
                                // Total internal reflection
                                Vector3D rhs = ints.normal.multiply(2 * ray.dir.dot(ints.normal));
                                v = ray.dir.subtract(rhs);
                            }
                        } else {
                            // Absorption
                            i++;
                            break;
                        }
                    }

                    ray = new Ray3D(ints.point, v);
                    ray.setColour(c.divide(P));
                }

            }
        }
        map.scalePhotonPower(1.0 / emitted);
        cmap = map.balance();
    }

    private Vector3D getRandLambertianDir(Vector3D normal) {
        Vector3D v;

        double phi = 2 * PI * rnd.random1();
        double sinPhi = sin(phi);
        double cosPhi = cos(phi);

        double cosTheta = sqrt(rnd.random1());
        double theta = FastMath.acos(cosTheta);
        double sinTheta = sin(theta);

        Matrix4D basis = initTransformMatrix(normal);

        v = new Vector3D(cosPhi * sinTheta, sinPhi * sinTheta, cosTheta);
        v = v.transform(basis);
        v = v.normalize();

        return v;
    }


    private void causticsIllumination(Ray3D ray) {
        // Caustics
        Colour caus_col;

        Vector3D normal = ray.intersection.normal;
        normal.normalize();

        if (lightOpts.nSoftShadows > 1) {
            caus_col = cmap.irradianceEstimate(
                    ray.intersection.point,
                    normal,
                    4,
                    1000);
        } else {
            caus_col = cmap.irradianceEstimate(
                    ray.intersection.point,
                    normal,
                    1.1,
                    1000);
        }
        if (caus_col == null)  {
            caus_col = new Colour(0, 0, 0);
        }
        double cosTheta12 = sqrt(-(ray.dir.dot(ray.intersection.normal)));
        caus_col = caus_col.multiply(ray.intersection.mat.diffuse);

        ray.setColour(ray.colour.add(caus_col.multiply(cosTheta12)));
    }

    private void globalIllumination(Ray3D ray, Raytracer raytracer) {
        // If we're not already in the irradiance cache, compute the irradiance via
        // monte carlo methods.
        Intersection ints = ray.intersection;

        Colour irr = icache.getIrradiance(ints.point, ints.normal);
        if (irr != null) {
            ray.setColour(irr.multiply(ints.mat.diffuse));
            return;
        }

        ray.setColour(new Colour(0, 0, 0));

        int N = lightOpts.nGlobalIlluminationN;
        int M = lightOpts.nGlobalIlluminationM;
        int hits = 0;
        double r0 = 0;

        Matrix4D basis = initTransformMatrix(ints.normal);

        // Stratification
        for (int i = 0; i < N; i++) {
            double phi = 2 * PI * ((double) i + rnd.random1()) / N;
            double sinPhi = sin(phi);
            double cosPhi = cos(phi);

            for (int j = 0; j < M; j++) {
                double cosTheta = sqrt(1 - (((double) j + rnd.random1()) / M));
                double theta = FastMath.acos(cosTheta);
                double sinTheta = sin(theta);

                Vector3D v = new Vector3D(cosPhi * sinTheta, sinPhi * sinTheta, cosTheta);
                v = v.transform(basis);

                Ray3D newRay = new Ray3D(ints.point, v);
                raytracer.traverseEntireScene(newRay, true);

                if (newRay.intersection.isSet()) {
                    raytracer.computeShading(newRay, 3, true);
                    ray.setColour(ray.colour.add(newRay.colour));

                    r0 += 1 / newRay.intersection.tValue;
                    hits++;
                }
            }
        }

        ray.colour = ray.colour.multiply(1.0 / hits);
        r0 = 1 / r0;

        if (hits == N * M) {
            icache.insert(ints.point, ints.normal, r0, ray.colour);
        }

        ray.setColour(ray.colour.multiply(ints.mat.diffuse));

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
            double x;
            if (N > 1) {
                double rand = rnd.random1();
                double dxRand = rand - floor(rand);
                x = ((i + dxRand) * dx) * 30 - 15;
            } else {
                x = i * dx * 30 - 15;
            }

            for (int j = 1; j <= N; j++) {
                double z;
                if (N > 1) {
                    double rand = rnd.random1();
                    double dzRand = rand - floor(rand);
                    z = (((double) j + dzRand) * dz) * 30 - 15;
                } else {
                    z = i * dz * 30 - 15;
                }

                Vector3D L = new Point3D(x, 50, z)
                        .subtract(ray.intersection.point);
                double l = sqrt(L.x * L.x + L.z * L.z + L.y * L.y);
                L = L.normalize();
                Vector3D LN = new Vector3D(0, -1, 0);

                double scale = -LN.dot(L);
                scale = scale < 0 ? 0 : scale;
                scale /= l * l * 1.5 * PI;

                Ray3D newRay = new Ray3D(ray.intersection.point, L);
                raytracer.traverseEntireScene(newRay, false);

                if (newRay.intersection.isSet() && newRay.intersection.mat.isLight) {
                    Vector3D R = ray.intersection.normal.multiply(2.0 * ray.intersection.normal.dot(L)).subtract(L);

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

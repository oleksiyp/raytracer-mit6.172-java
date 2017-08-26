package raytracer;

import lombok.Getter;
import lombok.ToString;

import static java.lang.Math.*;
import static raytracer.Colour.black;
import static raytracer.Point3D.origin;
import static raytracer.Vector3D.zero;

@Getter
@ToString
public class SquarePhotonLight extends Square implements Light {
    private final static int STACK_DEPTH = 30;
    final Colour colour;

    final LightOptions lightOpts;
    BalancedPhotonMap bmap;
    BalancedPhotonMap cmap;
    IrradianceCache icache;

    Random rnd;

    public SquarePhotonLight(
            Colour colour,
            Material material,
            LightOptions lightOpts) {
        super(material);
        this.colour = colour;
        this.lightOpts = lightOpts;
    }



    @Override
    public void init(Raytracer raytracer, boolean copy) {
        rnd = new Random();

        if (!copy) {
            icache = new IrradianceCache(
                    lightOpts.irradianceCacheTolerance,
                    lightOpts.irradianceCacheSpacing);

            tracePhotonMap(lightOpts.numPhotons, lightOpts.numCausticPhotons, raytracer);
        }
    }

    Point3D tpmPt = origin();
    Vector3D tpmVec = zero();
    Vector3D tpmNorm = zero();
    Colour tpmCol = black();

    Ray3D ray = new Ray3D();
    private void tracePhotonMap(int num, int causticsNum, Raytracer raytracer) {
        PhotonMap map = new PhotonMap(num * 4);

        int i = 0;
        while (i < num) {
            // Calculate the start point and direction of the photon
            tpmPt.p(rnd.random2() * 16, 49.99, rnd.random2() * 16);
            tpmVec.v(0, -1, 0);
            getRandLambertianDir(tpmVec);
            ray.set(tpmPt, tpmVec);
            ray.setColour(colour);

            int count = 0;
            while (ray.colour.maxComponent() > 0.1 && count++ < 100) {
                raytracer.traverseEntireScene(ray, true);

                Intersection ints = ray.intersection;
                if (!ints.isSet()) {
                    i--;
                    break;
                }

                ray.dir.normalize();
                if (ints.mat.isDiffuse) {
                    map.storePhoton(
                            ray.colour,
                            ints.point,
                            ray.dir);
                }

                double ran = rnd.random1();

                tpmCol.assign(ray.colour);
                tpmCol.multiply(ints.mat.diffuse);
                double P = tpmCol.maxComponent() / ray.colour.maxComponent();
                if (ran < P) {
                    // Diffuse reflection
                    tpmVec.assign(ints.normal);
                    getRandLambertianDir(tpmVec);
                } else {
                    ran -= P;
                    tpmCol.assign(ray.colour);
                    tpmCol.multiply(ints.mat.specular);
                    P = tpmCol.maxComponent() / ray.colour.maxComponent();
                    if (ran < P) {
                        // Specular reflection
                        tpmVec.assign(ints.normal);
                        tpmVec.multiply(2 * ray.dir.dot(ints.normal));
                        tpmVec.subtract(ray.dir);
                        tpmVec.negate();
                    } else {
                        ran -= P;
                        tpmCol.assign(ray.colour);
                        tpmCol.multiply(ints.mat.refractive);
                        P = tpmCol.maxComponent() / ray.colour.maxComponent();
                        if (ran < P) {
                            // Refraction

                            double n;
                            if (ray.dir.dot(tpmNorm) < 0) {
                                n = 1.0 / ints.mat.refractionIndex;
                            } else {
                                ints.normal.negate();
                                n = ints.mat.refractionIndex;
                            }

                            tpmNorm.assign(ints.normal);

                            double cosI = tpmNorm.dot(ray.dir);
                            double sinT2 = n * n * (1.0 - cosI * cosI);

                            if (sinT2 < 1.0) {
                                tpmVec.assign(ray.dir);
                                tpmVec.multiply(n);
                                tpmNorm.multiply(n * cosI + sqrt(1.0 - sinT2));
                                tpmVec.subtract(ints.normal);
                            } else {
                                // Total internal reflection
                                tpmVec.assign(ray.dir);
                                tpmNorm.multiply(2 * ray.dir.dot(ints.normal));
                                tpmVec.subtract(ints.normal);
                            }


                        } else {
                            // Absorption
                            break;
                        }

                    }
                }
                ray.set(ints.point, tpmVec);
                tpmCol.multiply( 1 / P);
                ray.setColour(tpmCol);
            }
            i++;
        }
        map.scalePhotonPower(1.0 / i);
        System.out.println("bmap");
        bmap = map.balance();

        // Caustics
        map = new PhotonMap(causticsNum);

        int emitted = 0;
        i = 0;

        Point3D p = origin();

        while (i < causticsNum) {
            // Calculate the start point and direction of the photon

            if (lightOpts.nSoftShadows > 1) {
                p.p(rnd.random2() * 18, 49.99, rnd.random2() * 18);
            } else {
                p.p(0, 49.99, 0);
            }
            tpmVec.v(0, -1, 0);
            getRandLambertianDir(tpmVec);
            ray.set(p, tpmVec);
            ray.setColour(colour);
            emitted++;

            raytracer.traverseEntireScene(ray, true);

            if (ray.intersection.isSet() && ray.intersection.mat.isSpecular) {
                while (true) {
                    raytracer.traverseEntireScene(ray, true);

                    if (!ray.intersection.isSet()) {
                        break;
                    }

                    tpmVec.assign(ray.dir);
                    tpmVec.normalize();

                    if (ray.intersection.mat.isDiffuse) {
                        map.storePhoton(
                                ray.colour,
                                ray.intersection.point,
                                tpmVec);

                        i++;
                        break;
                    }

                    double ran = rnd.random1();
                    tpmCol.assign(ray.colour);
                    tpmCol.multiply(ray.intersection.mat.specular);
                    double P = tpmCol.maxComponent() / ray.colour.maxComponent();
                    Intersection ints = ray.intersection;

                    // Specular reflection
                    if (ran < P) {
                        ray.intersection.normal.negate();

                        tpmVec.assign(ints.normal);
                        tpmVec.multiply(2 * ray.dir.dot(ints.normal));
                        tpmVec.subtract(ray.dir);
                        tpmVec.negate();
                    } else {
                        ran -= P;
                        tpmCol.assign(ray.colour);
                        tpmCol.multiply(ints.mat.refractive);
                        P = tpmCol.maxComponent() / ray.colour.maxComponent();
                        if (ran < P) {
                            // Refraction
                            tpmNorm.assign(ints.normal);
                            double n;
                            if (ray.dir.dot(tpmNorm) < 0) {
                                n = 1.0 / ints.mat.refractionIndex;
                            } else {
                                tpmNorm.negate();
                                n = ints.mat.refractionIndex;
                            }

                            double cosI = ints.normal.dot(ray.dir);
                            double sinT2 = n * n * (1.0 - cosI * cosI);

                            if (sinT2 < 1.0) {
                                tpmVec.assign(ray.dir);
                                tpmVec.multiply(n);
                                tpmNorm.multiply(n * cosI + sqrt(1.0 - sinT2));
                                tpmVec.subtract(ints.normal);
                            } else {
                                // Total internal reflection
                                tpmVec.assign(ray.dir);
                                tpmNorm.multiply(2 * ray.dir.dot(tpmNorm));
                                tpmVec.subtract(ints.normal);
                            }

                        } else {
                            // Absorption
                            i++;
                            break;
                        }
                    }

                    ray.set(ints.point, tpmVec);
                    tpmCol.multiply(1 / P);
                    ray.setColour(tpmCol);
                }

            }
        }
        map.scalePhotonPower(1.0 / emitted);
        System.out.println("cmap");
        cmap = map.balance();
    }



    private void getRandLambertianDir(Vector3D vec) {
        double phi = 2 * PI * rnd.random1();
        double sinPhi = sin(phi);
        double cosPhi = cos(phi);

        double cosTheta = sqrt(rnd.random1());
        double theta = FastMath.acos(cosTheta);
        double sinTheta = sin(theta);

        initTransformMatrix(vec, itmMat);

        vec.v(cosPhi * sinTheta, sinPhi * sinTheta, cosTheta);
        vec.transform(itmMat);
        vec.normalize();
    }

    Vector3D itmU = zero(), itmV = zero();
    Matrix4D itmMat = new Matrix4D();

    private void initTransformMatrix(Vector3D w, Matrix4D mat) {
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

        itmV.v(vx, vy, vz);
        itmV.normalize();
        itmU.assign(itmV);
        itmU.cross(w);

        mat.setElement(0, 0, itmU.x);
        mat.setElement(1, 0, itmU.y);
        mat.setElement(2, 0, itmU.z);
        mat.setElement(3, 0, 0);
        mat.setElement(0, 1, itmV.x);
        mat.setElement(1, 1, itmV.y);
        mat.setElement(2, 1, itmV.z);
        mat.setElement(3, 1, 0);
        mat.setElement(0, 2, w.x);
        mat.setElement(1, 2, w.y);
        mat.setElement(2, 2, w.z);
        mat.setElement(3, 2, 0);
        mat.setElement(0, 3, 0);
        mat.setElement(1, 3, 0);
        mat.setElement(2, 3, 0);
        mat.setElement(3, 3, 0);
    }


    StackItem []stack = new StackItem[STACK_DEPTH];
    int sp = 0;
    {
        for (int i = 0; i < stack.length; i++) {
            stack[i] = new StackItem();
        }
    }

    @Override
    public void shade(Ray3D ray, Raytracer raytracer, boolean getDirectly) {
        StackItem stackItem = stack[sp++];
        stackItem.shade(ray, raytracer, getDirectly);
        sp--;
    }

    class StackItem {

        Colour col = black();
        Colour col2 = black();

        public void shade(Ray3D ray, Raytracer raytracer, boolean getDirectly) {
            Material mat = ray.intersection.mat;
            if (mat.isLight) {
                ray.setColour(mat.diffuse);
                return;
            }

            if (getDirectly) {
                ray.intersection.normal.normalize();

                if (!bmap.irradianceEstimate(
                        ray.intersection.point,
                        ray.intersection.normal,
                        lightOpts.indirectMaxDistance,
                        lightOpts.indirectMaxPhotons,
                        col)) {
                    col.assign(ray.colour);
                }

                col.multiply(mat.diffuse);
                ray.setColour(col);
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

        private void causticsIllumination(Ray3D ray) {
            // Caustics
            Vector3D normal = ray.intersection.normal;
            normal.normalize();

            boolean ret;
            if (lightOpts.nSoftShadows > 1) {
                ret = cmap.irradianceEstimate(
                        ray.intersection.point,
                        normal,
                        4,
                        1000, col);
            } else {
                ret = cmap.irradianceEstimate(
                        ray.intersection.point,
                        normal,
                        1.1,
                        1000, col);
            }
            if (!ret) {
                col.c(0, 0, 0);
            }
            double cosTheta12 = sqrt(-(ray.dir.dot(ray.intersection.normal)));
            col.multiply(ray.intersection.mat.diffuse);
            col.multiply(cosTheta12);
            ray.addColour(col);
        }

        Vector3D giVec = zero();

        Ray3D newRay = new Ray3D();
        Matrix4D basis = new Matrix4D();

        private void globalIllumination(Ray3D ray, Raytracer raytracer) {
            // If we're not already in the irradiance cache, compute the irradiance via
            // monte carlo methods.
            Intersection ints = ray.intersection;

            if (icache.getIrradiance(ints.point, ints.normal, col)) {
                col.multiply(ints.mat.diffuse);
                ray.setColour(col);
                return;
            }
            ray.setColour(0, 0, 0);

            int N = lightOpts.nGlobalIlluminationN;
            int M = lightOpts.nGlobalIlluminationM;
            int hits = 0;
            double r0 = 0;

            initTransformMatrix(ints.normal, basis);

            // Stratification
            for (int i = 0; i < N; i++) {
                double phi = 2 * PI * ((double) i + rnd.random1()) / N;
                double sinPhi = sin(phi);
                double cosPhi = cos(phi);

                for (int j = 0; j < M; j++) {
                    double cosTheta = sqrt(1 - (((double) j + rnd.random1()) / M));
                    double theta = FastMath.acos(cosTheta);
                    double sinTheta = sin(theta);

                    giVec.v(cosPhi * sinTheta, sinPhi * sinTheta, cosTheta);
                    giVec.transform(basis);

                    newRay.set(ints.point, giVec);
                    raytracer.traverseEntireScene(newRay, true);

                    if (newRay.intersection.isSet()) {
                        raytracer.computeShading(newRay, 3, true);
                        ray.addColour(newRay.colour);

                        r0 += 1 / newRay.intersection.tValue;
                        hits++;
                    }
                }
            }

            ray.colour.multiply(1.0 / hits);
            r0 = 1 / r0;

            if (hits == N * M) {
                icache.insert(ints.point, ints.normal, r0, ray.colour);
            }

            ray.colour.multiply(ints.mat.diffuse);
        }

        Point3D diPt = origin();
        Vector3D diVec = zero();
        Vector3D diNorm = zero();
        Colour directCol = black();

        private void directIllumination(Ray3D ray, Raytracer raytracer) {
            // Direct illumination
            int N = lightOpts.nSoftShadows;

            double dx = 1.0 / (N + 1);
            double dz = 1.0 / (N + 1);

            directCol.c(0, 0, 0);

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

                    diPt.p(x, 50, z);
                    diPt.subtract(ray.intersection.point, diVec);
                    double l = diVec.mag();
                    diVec.multiply(1 / l);
                    diNorm.v(0, -1, 0);

                    double scale = -diNorm.dot(diVec);
                    scale = scale < 0 ? 0 : scale;
                    scale /= l * l * 1.5 * PI;

                    newRay.set(ray.intersection.point, diVec);
                    raytracer.traverseEntireScene(newRay, false);

                    if (newRay.intersection.isSet() && newRay.intersection.mat.isLight) {
                        diNorm.assign(ray.intersection.normal);
                        diNorm.multiply(2.0 * ray.intersection.normal.dot(diVec));
                        diNorm.subtract(diVec);

                        double NdotL = diVec.dot(ray.intersection.normal);
                        double RdotV = -(diNorm.dot(ray.dir));
                        NdotL = NdotL < 0 ? 0 : NdotL;
                        RdotV = RdotV < 0 ? 0 : RdotV;
                        if (ray.dir.dot(ray.intersection.normal) > 0) {
                            RdotV = 0;
                        }

                        Material mat = ray.intersection.mat;

                        col.assign(mat.diffuse);
                        col.multiply(NdotL);
                        double specPow = pow(RdotV, mat.specularExp);
                        col2.assign(mat.specular);
                        col2.multiply(specPow);
                        col.add(col2);
                        col2.assign(colour);
                        col2.multiply(scale);
                        col2.multiply(col);
                        directCol.add(col2);
                    }
                }
            }
            directCol.multiply(1.0 / (N * N));
            ray.addColour(directCol);
        }
    }

    @Override
    public SceneObject copy() {
        return new SquarePhotonLight(this);
    }

    private SquarePhotonLight(SquarePhotonLight photonLight) {
        super(photonLight.material);
        this.colour = photonLight.colour;
        this.lightOpts = photonLight.lightOpts;
        this.icache = photonLight.icache;
        this.cmap = photonLight.cmap.copy();
        this.bmap = photonLight.bmap.copy();
    }
}

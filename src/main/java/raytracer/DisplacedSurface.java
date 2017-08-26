package raytracer;

import static raytracer.Point3D.origin;
import static raytracer.Primitives.EPSILON;
import static raytracer.Vector3D.ZERO;
import static raytracer.Vector3D.zero;

public class DisplacedSurface extends SceneObject {
    public static final Vector3D ZM = new Vector3D(0, 0, -1);
    public static final Vector3D ZP = new Vector3D(0, 0, 1);
    public static final Vector3D YM = new Vector3D(0, -1, 0);
    public static final Vector3D YP = new Vector3D(0, 1, 0);
    public static final Vector3D XM = new Vector3D(-1, 0, 0);
    public static final Vector3D XP = new Vector3D(1, 0, 0);

    final int xcoords;
    final int zcoords;
    final PerlinNoise perlinNoise;
    final Material material;
    final double maxDisp;
    final Point3D[][] points;

    Vector3D aco = zero();

    Vector3D ba = zero();
    Vector3D dc = zero();
    Vector3D cb = zero();
    Vector3D bc = zero();
    Vector3D da = zero();
    Vector3D ad = zero();
    Vector3D db = zero();
    Vector3D bd = zero();

    Point3D x1 = origin();
    Point3D x2 = origin();

    Vector3D n1 = zero();
    Vector3D n2 = zero();

    Vector3D off = zero();

    Vector3D x12ac = zero();
    Vector3D x12b = zero();
    Vector3D x12d = zero();

    public DisplacedSurface(Material material, int n, int m, PerlinNoise noise) {
        perlinNoise = noise;
        this.material = material;

        xcoords = n;
        zcoords = m;
        maxDisp = 1.0;

        points = new Point3D[n + 1][m + 1];
        for (int i = 0; i < n + 1; i++) {
            for (int j = 0; j < m + 1; j++) {
                points[i][j] = origin();
            }
        }

        makeSurface(0);
    }

    private boolean checkIntersectionGrid(
            int xcoord,
            int zcoord,
            Point3D origin,
            Vector3D dir,
            Ray3D ray) {

        // Each square grid is made up of to triages (A and B).  This code checks
        // to see if the ray intersections with either of them.  If the ray
        // intersects with both triangles, we determine which is closer.

   /*     a         d
    *       + -----+
    *       |     /|
    *       | A  / |
    *       |   /  |
    *       |  /   |
    *       | /  B |
    *       |/     |
    *       +------+
    *     b         c
    */

        int i = -1;
        double t1, t2;
        Point3D a, b, c, d;

        a = points[xcoord + 1][zcoord];
        b = points[xcoord][zcoord];
        c = points[xcoord][zcoord + 1];
        d = points[xcoord + 1][zcoord + 1];

        b.subtract(a, ba);
        d.subtract(c, dc);
        c.subtract(b, cb);
        b.subtract(c, bc);
        d.subtract(a, da);
        a.subtract(d, ad);
        d.subtract(b, db);
        b.subtract(d, bd);

        n1.assign(ba);
        n1.cross(da);

        a.subtract(origin, aco);
        t1 = aco.dot(n1) / (dir.dot(n1));
        off.assign(dir);
        off.multiply(t1);
        x1.assign(origin);
        x1.add(off);

        x1.subtract(a, x12ac);
        x1.subtract(b, x12b);
        x1.subtract(d, x12d);

        ba.cross(x12ac);
        db.cross(x12b);
        ad.cross(x12d);

        if (ba.dot(n1) >= 0 &&
                db.dot(n1) >= 0 &&
                ad.dot(n1) >= 0) {
            i = 1;
        }

        n2.assign(dc);
        n2.cross(bc);

        c.subtract(origin, aco);
        t2 = aco.dot(n2) / dir.dot(n2);
        if (i != 1 || t2 < t1) {
            off.assign(dir);
            off.multiply(t2);
            x2.assign(origin);
            x2.add(off);

            x2.subtract(b, x12b);
            x2.subtract(d, x12d);
            x2.subtract(c, x12ac);

            dc.cross(x12ac);
            bd.cross(x12b);
            cb.cross(x12d);

            if (dc.dot(n2) >= 0 &&
                    bd.dot(n2) >= 0 &&
                    cb.dot(n2) >= 0)
                i = 2;

        }

        if (i == 1 && ray.lessDistant(t1)) {
            n1.normalize();
            ray.setIntersection(x1, n1, t1, material);
            return true;
        }
        if (i == 2 && ray.lessDistant(t2)) {
            n2.normalize();
            ray.setIntersection(x2, n2, t2, material);
            return true;
        }

        return false;
    }

    Point3D intsOrigin = origin();
    Vector3D intsDir = zero();
    Point3D intsGridPt = origin();
    Vector3D intsGridDir = zero();
    Ray3D mRay = new Ray3D();

    @Override
    public boolean intersect(Ray3D ray, Matrix4D worldToModel, Matrix4D modelToWorld) {
        intsOrigin.assign(ray.getOrigin());
        intsDir.assign(ray.getDir());

        intsOrigin.transform(worldToModel);
        intsDir.transform(worldToModel);

        mRay.set(intsOrigin, intsDir);
        intersectBBox(mRay);
        if (!mRay.intersection.isSet()) {
            return false;
        }

        if (!ray.lessDistant(mRay.getIntersection().getTValue())) {
            return false;
        }

        double n = xcoords + 1 + zcoords + 1;

        intsGridDir.assign(intsDir);
        intsGridDir.normalize();
        intsGridDir.multiply(100 * 1.41421356237 / n);

        intsGridPt.assign(mRay.intersection.point);
        intsGridPt.add(intsGridDir);

        int px1 = -1, pz1 = -1;
        for (int i = 0; i < n; i++) {
            int x1 = (int) Math.floor(xcoords * (intsGridPt.x + 50) / 100);
            int z1 = (int) Math.floor(zcoords * (intsGridPt.z + 50) / 100);

            if (x1 < 0 || z1 < 0 || x1 >= xcoords || z1 >= zcoords) {
                return false;
            }

            if (x1 != px1 || z1 != pz1) {
                if (checkIntersectionGrid(x1, z1, intsOrigin, intsDir, ray)) {
                    ray.intersection.transformBack(modelToWorld);
                    return true;
                }
                if (x1 + 1 < xcoords && checkIntersectionGrid(x1 + 1, z1, intsOrigin, intsDir, ray)) {
                    ray.intersection.transformBack(modelToWorld);
                    return true;
                }
                if (z1 + 1 < zcoords && checkIntersectionGrid(x1, z1 + 1, intsOrigin, intsDir, ray)) {
                    ray.intersection.transformBack(modelToWorld);
                    return true;
                }
            }

            px1 = x1;
            pz1 = z1;

            intsGridPt.add(intsGridDir);
        }
        return false;
    }

    Point3D bboxPt = origin();
    Vector3D bboxDir = zero();

    private void intersectBBox(Ray3D ray) {
        bboxPt.assign(ray.origin);
        bboxDir.assign(ray.dir);

        double lambda;

        double xl = -50, xh = 50;
        double yl = -maxDisp, yh = maxDisp;
        double zl = -50, zh = 50;

        if (xl <= intsOrigin.x && intsOrigin.x <= xh &&
                yl <= intsOrigin.y && intsOrigin.y <= yh &&
                zl <= intsOrigin.y && intsOrigin.y <= zh) {
            lambda = 2 * EPSILON;
            if (ray.lessDistant(lambda)) {
                bboxDir.multiply(lambda);
                bboxPt.add(bboxDir);
                ray.setIntersection(bboxPt, ZERO, lambda, material);
                return;
            }
        }

        lambda = (zh - intsOrigin.getZ()) / intsDir.getZ();
        if (ray.lessDistant(lambda)) {
            bboxPt.assign(ray.origin);
            bboxDir.assign(ray.dir);
            bboxDir.multiply(lambda);
            bboxPt.add(bboxDir);
            if (bboxPt.x <= xh && bboxPt.x >= xl && bboxPt.y <= yh && bboxPt.y >= yl) {
                ray.setIntersection(bboxPt, ZM, lambda, material);
            }
        }

        lambda = (zl - intsOrigin.getZ()) / intsDir.getZ();
        if (ray.lessDistant(lambda)) {
            bboxPt.assign(ray.origin);
            bboxDir.assign(ray.dir);
            bboxDir.multiply(lambda);
            bboxPt.add(bboxDir);
            if (bboxPt.x <= xh && bboxPt.x >= xl && bboxPt.y <= yh && bboxPt.y >= yl) {
                ray.setIntersection(bboxPt, ZP, lambda, material);
            }
        }

        lambda = (yh - intsOrigin.getY()) / intsDir.getY();
        if (ray.lessDistant(lambda)) {
            bboxPt.assign(ray.origin);
            bboxDir.assign(ray.dir);
            bboxDir.multiply(lambda);
            bboxPt.add(bboxDir);
            if (bboxPt.x <= xh && bboxPt.x >= xl && bboxPt.z <= zh && bboxPt.z >= zl) {
                ray.setIntersection(bboxPt, YM, lambda, material);
            }
        }

        lambda = (yl - intsOrigin.getY()) / intsDir.getY();
        if (ray.lessDistant(lambda)) {
            bboxPt.assign(ray.origin);
            bboxDir.assign(ray.dir);
            bboxDir.multiply(lambda);
            bboxPt.add(bboxDir);
            if (bboxPt.x <= xh && bboxPt.x >= xl && bboxPt.z <= zh && bboxPt.z >= zl) {
                ray.setIntersection(bboxPt, YP, lambda, material);
            }
        }

        lambda = (xh - intsOrigin.getX()) / intsDir.getX();
        if (ray.lessDistant(lambda)) {
            bboxPt.assign(ray.origin);
            bboxDir.assign(ray.dir);
            bboxDir.multiply(lambda);
            bboxPt.add(bboxDir);
            if (bboxPt.y <= yh && bboxPt.y >= yl && bboxPt.z <= zh && bboxPt.z >= zl) {
                ray.setIntersection(bboxPt, XM, lambda, material);
            }
        }

        lambda = (xl - intsOrigin.getX()) / intsDir.getX();
        if (ray.lessDistant(lambda)) {
            bboxPt.assign(ray.origin);
            bboxDir.assign(ray.dir);
            bboxDir.multiply(lambda);
            bboxPt.add(bboxDir);
            if (bboxPt.y <= yh && bboxPt.y >= yl && bboxPt.z <= zh && bboxPt.z >= zl) {
                ray.setIntersection(bboxPt, XP, lambda, material);
            }
        }
    }

    public void makeSurface(double time) {
        /* Extents are -50 to +50 in x-z plane, offset by height */
        for (int x = 0; x < xcoords + 1; x++) {
            for (int z = 0; z < zcoords + 1; z++) {

                double xc = (x * 100.0) / xcoords - 50.0;
                double zc = (z * 100.0) / zcoords - 50.0;
                double y = perlinNoise.get(xc / 1.2, zc, time);

                points[x][z].x = xc;
                points[x][z].y = y;
                points[x][z].z = zc;
            }
        }

    }

}

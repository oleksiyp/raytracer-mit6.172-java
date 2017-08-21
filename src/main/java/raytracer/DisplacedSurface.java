package raytracer;

import static raytracer.Primitives.EPSILON;

public class DisplacedSurface extends SceneObject {
    final int xcoords;
    final int zcoords;
    final PerlinNoise perlinNoise;
    final Material material;
    final double maxDisp;
    final Point3D[][] points;

    public DisplacedSurface(Material material, int n, int m, PerlinNoise noise) {
        perlinNoise = noise;
        this.material = material;

        xcoords = n;
        zcoords = m;
        maxDisp = 1.0;

        points = new Point3D[n + 1][m + 1];

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
        Point3D x1, x2 = null;
        Vector3D n1, n2;
        double t1, t2;
        Point3D a, b, c, d;

        a = points[xcoord + 1][zcoord];
        b = points[xcoord][zcoord];
        c = points[xcoord][zcoord + 1];
        d = points[xcoord + 1][zcoord + 1];

        Vector3D ba = b.subtract(a);
        Vector3D da = d.subtract(a);
        Vector3D db = d.subtract(b);
        Vector3D ad = a.subtract(d);
        Vector3D bd = b.subtract(d);
        Vector3D cb = c.subtract(b);
        Vector3D dc = d.subtract(c);
        n1 = ba.cross(da);

        Vector3D ao = a.subtract(origin);
        t1 = ao.dot(n1) / (dir.dot(n1));
        x1 = origin.add(dir.multiply(t1));

        Vector3D x1a = x1.subtract(a);
        Vector3D x1b = x1.subtract(b);
        Vector3D x1d = x1.subtract(d);
        if ((ba.cross(x1a)).dot(n1) >= 0 &&
                (db.cross(x1b)).dot(n1) >= 0 &&
                (ad.cross(x1d)).dot(n1) >= 0) {
            i = 1;
        }

        n2 = dc.cross(b.subtract(c));

        Vector3D co = c.subtract(origin);
        t2 = co.dot(n2) / (dir.dot(n2));
        if (i != 1 || t2 < t1) {
            x2 = origin.add(dir.multiply(t2));
            Vector3D x2b = x2.subtract(b);
            Vector3D x2d = x2.subtract(d);
            Vector3D x2c = x2.subtract(c);

            if ((dc.cross(x2c)).dot(n2) >= 0 &&
                    (bd.cross(x2d)).dot(n2) >= 0 &&
                    (cb.cross(x2b)).dot(n2) >= 0)
                i = 2;

        }

        if (i == 1 && ray.lessDistant(t1)) {
            n1 = n1.normalize();
            ray.setIntersection(x1, n1, t1, material);
            return true;
        }
        if (i == 2 && ray.lessDistant(t2)) {
            n2 = n2.normalize();
            ray.setIntersection(x2, n2, t2, material);
            return true;
        }

        return false;
    }

    @Override
    public boolean intersect(Ray3D ray, Matrix4D worldToModel, Matrix4D modelToWorld) {
        Point3D origin = ray.getOrigin().transform(worldToModel);
        Vector3D dir = ray.getDir().transform(worldToModel);

        Ray3D mRay = new Ray3D(origin, dir);
        intersectBBox(mRay);
        if (!mRay.intersection.isSet()) {
            return false;
        }

        if (!ray.lessDistant(mRay.getIntersection().getTValue())) {
            return false;
        }

        double n = xcoords + 1 + zcoords + 1;
        Vector3D d = dir.normalize().multiply(100 * 1.41421356237 / n);
        Point3D pt = mRay.intersection.point.add(d.multiply(1 / n));

        int px1 = -1, pz1 = -1;
        for (int i = 0; i < n; i++) {
            int x1 = (int) Math.floor(xcoords * (pt.x + 50) / 100);
            int z1 = (int) Math.floor(zcoords  * (pt.z + 50) / 100);

            if (x1 < 0 || z1 < 0 || x1 >= xcoords || z1 >= zcoords) {
                return false;
            }

            if (x1 != px1 || z1 != pz1) {
                if (checkIntersectionGrid(x1, z1, origin, dir, ray)) {
                    ray.intersection.transformBack(modelToWorld);
                    return true;
                }
                if (x1 + 1 < xcoords && checkIntersectionGrid(x1 + 1, z1, origin, dir, ray)) {
                    ray.intersection.transformBack(modelToWorld);
                    return true;
                }
                if (z1 + 1 < zcoords && checkIntersectionGrid(x1, z1 + 1, origin, dir, ray)) {
                    ray.intersection.transformBack(modelToWorld);
                    return true;
                }
            }

            px1 = x1;
            pz1 = z1;
            pt = pt.add(d);
        }
        return false;
    }

    private void intersectBBox(Ray3D ray) {
        Point3D origin = ray.origin;
        Vector3D dir = ray.dir;
        double lambda;
        Point3D p;

        double xl = -50, xh = 50;
        double yl = -maxDisp, yh = maxDisp;
        double zl = -50, zh = 50;

        if (xl <= origin.x && origin.x <= xh &&
                yl <= origin.y && origin.y <= yh &&
                zl <= origin.y && origin.y <= zh) {
            lambda = 2 * EPSILON;
            if (ray.lessDistant(lambda)) {
                p = origin.add(dir.multiply(lambda));
                ray.setIntersection(p, new Vector3D(0, 0, 0), lambda, material);
                return;
            }
        }

        lambda = (zh - origin.getZ()) / dir.getZ();
        if (ray.lessDistant(lambda)) {
            p = origin.add(dir.multiply(lambda));
            if (p.x <= xh && p.x >= xl && p.y <= yh && p.y >= yl) {
                ray.setIntersection(p, new Vector3D(0, 0, -1), lambda, material);
            }
        }

        lambda = (zl - origin.getZ()) / dir.getZ();
        if (ray.lessDistant(lambda)) {
            p = origin.add(dir.multiply(lambda));
            if (p.x <= xh && p.x >= xl && p.y <= yh && p.y >= yl) {
                ray.setIntersection(p, new Vector3D(0, 0, 1), lambda, material);
            }
        }

        lambda = (yh - origin.getY()) / dir.getY();
        if (ray.lessDistant(lambda)) {
            p = origin.add(dir.multiply(lambda));
            if (p.x <= xh && p.x >= xl && p.z <= zh && p.z >= zl) {
                ray.setIntersection(p, new Vector3D(0, -1,0), lambda, material);
            }
        }

        lambda = (yl - origin.getY()) / dir.getY();
        if (ray.lessDistant(lambda)) {
            p = origin.add(dir.multiply(lambda));
            if (p.x <= xh && p.x >= xl && p.z <= zh && p.z >= zl) {
                ray.setIntersection(p, new Vector3D(0, 1, 0), lambda, material);
            }
        }

        lambda = (xh - origin.getX()) / dir.getX();
        if (ray.lessDistant(lambda)) {
            p = origin.add(dir.multiply(lambda));
            if (p.y <= yh && p.y >= yl && p.z <= zh && p.z >= zl) {
                ray.setIntersection(p, new Vector3D(-1, 0, 0), lambda, material);
            }
        }

        lambda = (xl - origin.getX()) / dir.getX();
        if (ray.lessDistant(lambda)) {
            p = origin.add(dir.multiply(lambda));
            if (p.y <= yh && p.y >= yl && p.z <= zh && p.z >= zl) {
                ray.setIntersection(p, new Vector3D(1, 0, 0), lambda, material);
            }
        }
    }

    public void makeSurface(double time) {
        /* Extents are -50 to +50 in x-z plane, offset by height */
        int z = 0;
        int x = 0;

        for (x = 0; x < xcoords + 1; x++) {
            for (z = 0; z < zcoords + 1; z++) {

                double xc = (x * 100.0) / xcoords - 50.0;
                double zc = (z * 100.0) / zcoords - 50.0;
                double y = perlinNoise.get(xc / 1.2, zc, time);

                points[x][z] = new Point3D(xc, y, zc);
            }
        }

    }

}

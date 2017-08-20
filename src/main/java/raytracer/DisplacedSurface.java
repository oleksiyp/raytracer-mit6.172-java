package raytracer;

public class DisplacedSurface extends SceneObject {
    final int xcoords;
    final int zcoords;
    final double middle;
    final PerlinNoise perlinNoise;
    final Material material;
    final double maxDisp;
    final Point3D[][] points;
    final double topBound;
    final double bottomBound;

    public DisplacedSurface(Material material, double middle, int n, int m, PerlinNoise noise) {
        perlinNoise = noise;
        this.material = material;

        xcoords = n;
        zcoords = m;
        this.middle = middle;
        maxDisp = 1.0;

        points = new Point3D[n + 1][m + 1];

        topBound = middle + maxDisp;
        bottomBound = middle - maxDisp;

        makeSurface(0);
    }

    private boolean checkIntersectionGrid(
            int xcoord, int zcoord,
            Point3D origin, Vector3D dir,
            Ray3D ray,
            Matrix4D modelToWorld) {

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

        n1 = (b.subtract(a)).cross(d.subtract(a));

        t1 = (a.subtract(origin)).dot(n1) / (dir.dot(n1));
        x1 = origin.add(dir.multiply(t1));
        if (((b.subtract(a)).cross(x1.subtract(a))).dot(n1) >= 0 &&
                ((d.subtract(b)).cross(x1.subtract(b))).dot(n1) >= 0 &&
                ((a.subtract(d)).cross(x1.subtract(d))).dot(n1) >= 0) {
            i = 1;
        }

        n2 = (d.subtract(c)).cross(b.subtract(c));

        t2 = (c.subtract(origin)).dot(n2) / (dir.dot(n2));
        if (i != 1 || t2 < t1) {
            x2 = origin.add(dir.multiply(t2));
            if (((d.subtract(c)).cross(x2.subtract(c))).dot(n2) >= 0 &&
                    ((b.subtract(d)).cross(x2.subtract(d))).dot(n2) >= 0 &&
                    ((c.subtract(b)).cross(x2.subtract(b))).dot(n2) >= 0)
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
        Point3D origin = ray.origin;
        Vector3D dir = ray.dir;

        boolean intersected = false;

        // Check each triangle in grid to see if it intersects with this ray
        for (int x1 = 0; x1 < xcoords; x1++) {
            for (int z1 = 0; z1 < zcoords; z1++) {
                intersected |= checkIntersectionGrid(x1, z1, origin, dir, ray,
                        modelToWorld);
            }
        }

        return intersected;
    }

    public void makeSurface(double time) {
   /* Extents are -50 to +50 in x-z plane, offset by height */
        int z = 0;
        int x = 0;

        for (x = 0; x < xcoords + 1; x++) {
            for (z = 0; z < zcoords + 1; z++) {

                double xc = (x * 100.0) / xcoords - 50.0;
                double zc = (z * 100.0) / zcoords - 50.0;
                double y = perlinNoise.get(xc / 1.2, zc, time) + middle;

                points[x][z] = new Point3D(xc, y, zc);
            }
        }

    }

}

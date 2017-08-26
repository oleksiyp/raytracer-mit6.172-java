package raytracer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import static raytracer.Point3D.origin;
import static raytracer.Vector3D.zero;


@Getter
@ToString
public class Cube extends SceneObject {
    public static final Vector3D ZM = new Vector3D(0, 0, -1);
    public static final Vector3D ZP = new Vector3D(0, 0, 1);
    public static final Vector3D YM = new Vector3D(0, -1, 0);
    public static final Vector3D YP = new Vector3D(0, 1, 0);
    public static final Vector3D XM = new Vector3D(-1, 0, 0);
    public static final Vector3D XP = new Vector3D(1, 0, 0);

    final Direction normalDir;
    final Material mat1;
    final Material mat2;
    final Material mat3;
    final Material mat4;
    final Material mat5;
    final Material mat6;

    public Cube(Direction normalDir,
                Material mat1,
                Material mat2,
                Material mat3,
                Material mat4,
                Material mat5,
                Material mat6) {

        this.normalDir = normalDir;
        this.mat1 = mat1;
        this.mat2 = mat2;
        this.mat3 = mat3;
        this.mat4 = mat4;
        this.mat5 = mat5;
        this.mat6 = mat6;
    }

    Point3D origin = origin();
    Vector3D dir = zero();
    Point3D cubePt = origin();
    Vector3D cubeVec = zero();

    @Override
    public boolean intersect(Ray3D ray, Matrix4D worldToModel, Matrix4D modelToWorld) {
        double lambda;

        origin.assign(ray.getOrigin());
        dir.assign(ray.getDir());

        origin.transform(worldToModel);
        dir.transform(worldToModel);

        lambda = (50 - origin.getZ()) / dir.getZ();
        if (ray.lessDistant(lambda)) {
            cubePt.assign(origin);
            cubeVec.assign(dir);
            cubeVec.multiply(lambda);
            cubePt.add(cubeVec);
            if (cubePt.x <= 50 && cubePt.x >= -50 && cubePt.y <= 50 && cubePt.y >= -50) {
                ray.setIntersection(cubePt, in() ? ZM : ZP, lambda, mat1);
                ray.intersection.transformBack(modelToWorld);
                return true;
            }
        }

        lambda = (-50 - origin.getZ()) / dir.getZ();
        if (ray.lessDistant(lambda)) {
            cubePt.assign(origin);
            cubeVec.assign(dir);
            cubeVec.multiply(lambda);
            cubePt.add(cubeVec);
            if (cubePt.x <= 50 && cubePt.x >= -50 && cubePt.y <= 50 && cubePt.y >= -50) {
                ray.setIntersection(cubePt, in() ? ZP : ZM, lambda, mat2);
                ray.intersection.transformBack(modelToWorld);
                return true;
            }
        }

        lambda = (50 - origin.getY()) / dir.getY();
        if (ray.lessDistant(lambda)) {
            cubePt.assign(origin);
            cubeVec.assign(dir);
            cubeVec.multiply(lambda);
            cubePt.add(cubeVec);
            if (cubePt.x <= 50 && cubePt.x >= -50 && cubePt.z <= 50 && cubePt.z >= -50) {
                ray.setIntersection(cubePt, in() ? YM : YP, lambda, mat3);
                ray.intersection.transformBack(modelToWorld);
                return true;
            }
        }

        lambda = (-50 - origin.getY()) / dir.getY();
        if (ray.lessDistant(lambda)) {
            cubePt.assign(origin);
            cubeVec.assign(dir);
            cubeVec.multiply(lambda);
            cubePt.add(cubeVec);
            if (cubePt.x <= 50 && cubePt.x >= -50 && cubePt.z <= 50 && cubePt.z >= -50) {
                ray.setIntersection(cubePt, in() ? YP : YM, lambda, mat4);
                ray.intersection.transformBack(modelToWorld);
                return true;
            }
        }

        lambda = (50 - origin.getX()) / dir.getX();
        if (ray.lessDistant(lambda)) {
            cubePt.assign(origin);
            cubeVec.assign(dir);
            cubeVec.multiply(lambda);
            cubePt.add(cubeVec);
            if (cubePt.y <= 50 && cubePt.y >= -50 && cubePt.z <= 50 && cubePt.z >= -50) {
                ray.setIntersection(cubePt, in() ? XM : XP, lambda, mat5);
                ray.intersection.transformBack(modelToWorld);
                return true;
            }
        }

        lambda = (-50 - origin.getX()) / dir.getX();
        if (ray.lessDistant(lambda)) {
            cubePt.assign(origin);
            cubeVec.assign(dir);
            cubeVec.multiply(lambda);
            cubePt.add(cubeVec);
            if (cubePt.y <= 50 && cubePt.y >= -50 && cubePt.z <= 50 && cubePt.z >= -50) {
                ray.setIntersection(cubePt, in() ? XP : XM, lambda, mat6);
                ray.intersection.transformBack(modelToWorld);
                return true;
            }
        }

        return false;
    }

    private boolean in() {
        return normalDir == Direction.IN;
    }
}

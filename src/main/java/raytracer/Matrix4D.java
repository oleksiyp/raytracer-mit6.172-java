package raytracer;

import java.util.Arrays;

public class Matrix4D {
    double[][] values;

    public Matrix4D() {
        values = new double[4][4];
    }

    public Matrix4D(Matrix4D mat) {
        values = new double[][] {
                Arrays.copyOf(mat.values[0], 4),
                Arrays.copyOf(mat.values[1], 4),
                Arrays.copyOf(mat.values[2], 4),
                Arrays.copyOf(mat.values[3], 4)
        };
    }

    public static Matrix4D identity() {
        Matrix4D res = new Matrix4D();
        res.values[0][0] = 1;
        res.values[1][1] = 1;
        res.values[2][2] = 1;
        res.values[3][3] = 1;
        return res;
    }

    public static Matrix4D translation(Vector3D offset) {
        Matrix4D res = identity();
        res.values[0][3] = offset.x;
        res.values[1][3] = offset.y;
        res.values[2][3] = offset.z;
        return res;
    }

    private double[][] mul(Matrix4D mat1, Matrix4D mat2) {
        int i, j;
        double [][]r = new double[4][4];

        for (i = 0 ; i < 4 ; i++) {
            for (j = 0; j < 4; j++) {
                r[i][j] = mat1.values[i][0] * mat2.values[0][j] +
                        mat1.values[i][1] * mat2.values[1][j] +
                        mat1.values[i][2] * mat2.values[2][j] +
                        mat1.values[i][3] * mat2.values[3][j];
            }
        }


        return r;
    }

    public void mulRight(Matrix4D otherMat) {
        values = mul(this, otherMat);
    }

    public void mulLeft(Matrix4D otherMat) {
        values = mul(otherMat, this);
    }

    public void setElement(int i, int j, double val) {
        values[i][j] = val;
    }
}

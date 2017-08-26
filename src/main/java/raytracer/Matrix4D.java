package raytracer;

import java.util.Arrays;

import static java.lang.Math.abs;

public class Matrix4D {
    public boolean ident;
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
        checkIdent();
    }

    private void checkIdent() {
        ident = true;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (abs(values[i][j] - (i == j ? 1 : 0)) >= Primitives.EPSILON) {
                    ident = false;
                }
            }
        }
    }

    public static Matrix4D identity() {
        Matrix4D res = new Matrix4D();
        res.ident = true;
        res.values[0][0] = 1;
        res.values[1][1] = 1;
        res.values[2][2] = 1;
        res.values[3][3] = 1;
        return res;
    }

    public static Matrix4D translation(Vector3D offset) {
        Matrix4D res = identity();
        res.ident = false;
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
        checkIdent();
    }

    public void mulLeft(Matrix4D otherMat) {
        values = mul(otherMat, this);
        checkIdent();
    }

    public void setElement(int i, int j, double val) {
        values[i][j] = val;
        checkIdent();
    }

    public void assign(Matrix4D otherMat) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                values[i][j] = otherMat.values[i][j];
            }
        }
    }
}

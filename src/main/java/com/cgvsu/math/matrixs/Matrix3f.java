package com.cgvsu.math.matrixs;

import com.cgvsu.math.interfaces.Matrix;
import com.cgvsu.math.vectors.Vector3f;


public class Matrix3f implements Matrix {
    private float[][] matrix;

    public Matrix3f(float[][] matrix) throws IllegalArgumentException {
        if (matrix.length == 0) {
            throw new IllegalArgumentException("Matrix's length must be 3x3, but get void matrix");
        }
        if (matrix.length != 3) {
            throw new IllegalArgumentException(String.format("Matrix's length must be 3x3, but get %dx%d %n", matrix.length, matrix[0].length));
        }
        if (matrix[0].length != 3) {
            throw new IllegalArgumentException(String.format("Matrix's length must be 3x3, but get %dx%d %n", matrix.length, matrix[0].length));
        }
        this.matrix = matrix;
    }

    public Matrix3f() {
        this.matrix = new float[][] {
                {1, 0, 0},
                {0, 1, 0},
                {0, 0, 1}
        };
    }

    public float[][] getMatrix() {
        return matrix;
    }

    public void setValue(int row, int column, float value) {
        matrix[row][column] = value;
    }

    public float getValue(int row, int column) {
        return matrix[row][column];
    }

    public void plus(Matrix3f matrix3f) {
        for (int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {
                matrix[i][j] += matrix3f.getValue(i, j);
            }
        }
    }

    public void minus(Matrix3f matrix3f) {
        for (int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {
                matrix[i][j] -= matrix3f.getValue(i, j);
            }
        }
    }

    public Vector3f multiplyOnVector(Vector3f vector) {
        float x = matrix[0][0] * vector.getX() + matrix[0][1] * vector.getY() + matrix[0][2] * vector.getZ();
        float y = matrix[1][0] * vector.getX() + matrix[1][1] * vector.getY() + matrix[1][2] * vector.getZ();
        float z = matrix[2][0] * vector.getX() + matrix[2][1] * vector.getY() + matrix[2][2] * vector.getZ();

        return new Vector3f(x, y, z);
    }

    public Matrix3f multiplyOnMatrix(Matrix3f matrix3f) {
        Matrix3f result = new Matrix3f();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                float sum = 0;
                for (int k = 0; k < 3; k++) {
                    sum += matrix[i][k] * matrix3f.getValue(k, j);
                }
                result.setValue(i, j, sum);
            }
        }
        return result;
    }

    @Override
    public void transposition() {
        Matrix3f result = new Matrix3f();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                result.setValue(i, j, matrix[j][i]);
            }
        }
        matrix = result.getMatrix();
    }
}

package com.cgvsu.math.matrixs;

import com.cgvsu.math.interfaces.Matrix;
import com.cgvsu.math.vectors.Vector4f;


public class Matrix4f implements Matrix {
    private float[][] matrix = new float[4][4];

    public Matrix4f(float[][] matrix) throws IllegalArgumentException {
        if (matrix.length == 0) {
            throw new IllegalArgumentException("Matrix's length must be 4x4, but get void matrix");
        }
        if (matrix.length != 4) {
            throw new IllegalArgumentException(String.format("Matrix's length must be 4x4, but get %dx%d %n", matrix.length, matrix[0].length));
        }
        if (matrix[0].length != 4) {
            throw new IllegalArgumentException(String.format("Matrix's length must be 4x4, but get %dx%d %n", matrix.length, matrix[0].length));
        }
        this.matrix = matrix;
    }

    public Matrix4f() {
        this.matrix = new float[][] {
                {1, 0, 0, 0},
                {0, 1, 0, 0},
                {0, 0, 1, 0},
                {0, 0, 0, 1}
        };
    }

    public Matrix4f(float[] array) {
        for (int i = 0; i < 4; i++) {
            System.arraycopy(array, i * 4, matrix[i], 0, 4);
        }
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

    public void add(Matrix4f matrix4F) {
        for (int i = 0; i < 4; i++) {
            for(int j = 0; j < 4; j++) {
                matrix[i][j] += matrix4F.getValue(i, j);
            }
        }
    }

    public void sub(Matrix4f matrix4F) {
        for (int i = 0; i < 4; i++) {
            for(int j = 0; j < 4; j++) {
                matrix[i][j] -= matrix4F.getValue(i, j);
            }
        }
    }

    public Vector4f multiplyOnVector(Vector4f vector) {
        float x = matrix[0][0] * vector.getX() + matrix[0][1] * vector.getY() + matrix[0][2] * vector.getZ() + matrix[0][3] * vector.getW();
        float y = matrix[1][0] * vector.getX() + matrix[1][1] * vector.getY() + matrix[1][2] * vector.getZ() + matrix[1][3] * vector.getW();
        float z = matrix[2][0] * vector.getX() + matrix[2][1] * vector.getY() + matrix[2][2] * vector.getZ() + matrix[2][3] * vector.getW();
        float w = matrix[3][0] * vector.getX() + matrix[3][1] * vector.getY() + matrix[3][2] * vector.getZ() + matrix[3][3] * vector.getW();
        return new Vector4f(x, y, z, w);
    }

    public void mul(Matrix4f matrix4F) {
        Matrix4f result = new Matrix4f();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                float sum = 0;
                for (int k = 0; k < 4; k++) {
                    sum += matrix[i][k] * matrix4F.getValue(k, j);
                }
                result.setValue(i, j, sum);
            }
        }
        this.matrix = result.getMatrix();
    }

    @Override
    public void transposition() {
        Matrix4f result = new Matrix4f();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result.setValue(i, j, matrix[j][i]);
            }
        }
        matrix = result.getMatrix();
    }
}

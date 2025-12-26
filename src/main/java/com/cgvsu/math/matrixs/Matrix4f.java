package com.cgvsu.math.matrixs;

import com.cgvsu.math.vectors.Vector3f;
import com.cgvsu.math.vectors.Vector4f;


public class Matrix4f extends AbstractMatrix<Matrix4f> {

    public Matrix4f() {
        super(4);
    }

    public Matrix4f(float[][] matrix) {
        super(validate(matrix));
    }

    public Matrix4f(float[] array) {
        super(validate(array));
    }

    @Override
    protected Matrix4f create(float[][] matrix) {
        return new Matrix4f(matrix);
    }

    private static float[][] validate(float[][] matrix) {
        if (matrix == null) {
            throw new NullPointerException("Matrix is null");
        }
        if (matrix.length != 4) {
            throw new IllegalArgumentException("Matrix must be 4x4");
        }
        for (float[] floats : matrix) {
            if (floats == null) {
                throw new NullPointerException("Row is null");
            }
            if (floats.length != 4) {
                throw new IllegalArgumentException("Matrix must be 4x4");
            }
        }
        return matrix;
    }

    private static float[] validate(float[] array) {
        if (array == null) {
            throw new NullPointerException("Array is null");
        }
        if (array.length != 16) {
            throw  new IllegalArgumentException("Length of array for Matrix4f must be 16");
        }
        return array;
    }

    public Vector4f multiplyOnVector(Vector4f vector) {
        return multiplyOnVector(this, vector);
    }

    public Vector3f multiplyOnVector(Vector3f vector) {
        return multiplyOnVector(this, vector);
    }

    public static Vector4f multiplyOnVector(Matrix4f matrix4f, Vector4f vector4f) {
        float x = matrix4f.getValue(0, 0) * vector4f.getX() + matrix4f.getValue(0, 1) * vector4f.getY() +
                matrix4f.getValue(0, 2) * vector4f.getZ() + matrix4f.getValue(0, 3) * vector4f.getW();
        float y = matrix4f.getValue(1, 0) * vector4f.getX() + matrix4f.getValue(1, 1) * vector4f.getY() +
                matrix4f.getValue(1, 2) * vector4f.getZ() + matrix4f.getValue(1, 3) * vector4f.getW();
        float z = matrix4f.getValue(2, 0) * vector4f.getX() + matrix4f.getValue(2, 1) * vector4f.getY() +
                matrix4f.getValue(2, 2) * vector4f.getZ() + matrix4f.getValue(2, 3) * vector4f.getW();
        float w = matrix4f.getValue(3, 0) * vector4f.getX() + matrix4f.getValue(3, 1) * vector4f.getY() +
                matrix4f.getValue(3, 2) * vector4f.getZ() + matrix4f.getValue(3, 3) * vector4f.getW();

        return new Vector4f(x, y, z, w);
    }

    public static Vector3f multiplyOnVector(Matrix4f matrix4f, Vector3f vector3f) {
        float x = vector3f.getX() * matrix4f.getValue(0, 0) +
                vector3f.getY() * matrix4f.getValue(0, 1) +
                vector3f.getZ() * matrix4f.getValue(0, 2) +
                matrix4f.getValue(0, 3);

        float y = vector3f.getX() * matrix4f.getValue(1, 0) +
                vector3f.getY() * matrix4f.getValue(1, 1) +
                vector3f.getZ() * matrix4f.getValue(1, 2) +
                matrix4f.getValue(1, 3);

        float z = vector3f.getX() * matrix4f.getValue(2, 0) +
                vector3f.getY() * matrix4f.getValue(2, 1) +
                vector3f.getZ() * matrix4f.getValue(2, 2) +
                matrix4f.getValue(2, 3);

        float w = vector3f.getX() * matrix4f.getValue(3, 0) +
                vector3f.getY() * matrix4f.getValue(3, 1) +
                vector3f.getZ() * matrix4f.getValue(3, 2) +
                matrix4f.getValue(3, 3);
        if (Math.abs(w) < EPS) {
            return new Vector3f(x, y, z);
        }
        return new Vector3f(x / w, y / w, z / w);
    }

    public Vector4f multipliedOnVector(Vector4f vector4f) {
        Matrix4f matrix4f = this.copy();
        return matrix4f.multiplyOnVector(vector4f);
    }

    public Vector3f multipliedOnVector(Vector3f vector3f) {
        Matrix4f matrix4f = this.copy();
        return matrix4f.multiplyOnVector(vector3f);
    }
}

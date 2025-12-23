package com.cgvsu.math.matrixs;

import com.cgvsu.math.vectors.Vector3f;
import com.cgvsu.math.vectors.Vector4f;


public class Matrix3f extends AbstractMatrix<Matrix3f> {
    
    
    public Matrix3f() {
        super(3);
    }

    public Matrix3f(float[][] matrix) {
        super(validate(matrix));
    }

    public Matrix3f(float[] array) {
        super(validate(array));

    }

    private static float[] validate(float[] array) {
        if (array == null) {
            throw new NullPointerException("Array is null");
        }
        if (array.length != 9) {
            throw new IllegalArgumentException("Length of array for Matrix3f must be 9");
        }
        return array;
    }

    private static float[][] validate(float[][] matrix) {
        if (matrix == null) {
            throw new NullPointerException("Matrix is null");
        }
        if (matrix.length != 3) {
            throw new IllegalArgumentException("Matrix must be 3x3");
        }
        for (float[] floats : matrix) {
            if (floats == null) {
                throw new NullPointerException("Row is null");
            }
            if (floats.length != 3) {
                throw new IllegalArgumentException("Matrix must be 3x3");
            }
        }
        return matrix;
    }

    public Vector3f multiplyOnVector(Vector3f vector) {
        return multiplyOnVector(this, vector);
    }

    public Vector4f multiplyOnVector(Vector4f vector) {
        return multiplyOnVector(this, vector);
    }

    public static Vector4f multiplyOnVector(Matrix3f matrix3f, Vector4f vector4f) {
        float x = matrix3f.getValue(0, 0) * vector4f.getX() + matrix3f.getValue(0, 1) * vector4f.getY() + matrix3f.getValue(0, 2) * vector4f.getZ();
        float y = matrix3f.getValue(1, 0) * vector4f.getX() + matrix3f.getValue(1, 1) * vector4f.getY() + matrix3f.getValue(1, 2) * vector4f.getZ();
        float z = matrix3f.getValue(2, 0) * vector4f.getX() + matrix3f.getValue(2, 1) * vector4f.getY() + matrix3f.getValue(2, 2) * vector4f.getZ();

        return new Vector4f(x, y, z, vector4f.getW());
    }

    public static Vector3f multiplyOnVector(Matrix3f matrix3f, Vector3f vector3f) {
        float x = matrix3f.getValue(0, 0) * vector3f.getX() + matrix3f.getValue(0, 1) * vector3f.getY() + matrix3f.getValue(0, 2) * vector3f.getZ();
        float y = matrix3f.getValue(1, 0) * vector3f.getX() + matrix3f.getValue(1, 1) * vector3f.getY() + matrix3f.getValue(1, 2) * vector3f.getZ();
        float z = matrix3f.getValue(2, 0) * vector3f.getX() + matrix3f.getValue(2, 1) * vector3f.getY() + matrix3f.getValue(2, 2) * vector3f.getZ();

        return new Vector3f(x, y, z);
    }

}

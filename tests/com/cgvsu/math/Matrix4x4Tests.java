package com.cgvsu.math;

import com.cgvsu.math.matrixs.Matrix4f;
import com.cgvsu.math.vectors.Vector4f;
import org.junit.Test;


import static org.junit.Assert.*;

public class Matrix4x4Tests {
    @Test
    public void testConstructorWithValidMatrix() {
        float[][] data = {
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}
        };
        Matrix4f matrix = new Matrix4f(data);
        assertArrayEquals(data, matrix.getMatrix());
    }

    @Test
    public void testConstructorWithInvalidMatrixSize() {
        float[][] data = {
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        };
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new Matrix4f(data);
        });
        assertTrue(exception.getMessage().contains("Matrix's length must be 4x4"));
    }

    @Test
    public void testSetValueAndGetValue() {
        Matrix4f matrix = new Matrix4f();
        matrix.setValue(2, 3, 42f);
        assertEquals(42f, matrix.getValue(2, 3), 10e-6);
    }

    @Test
    public void testPlus() {
        Matrix4f m1 = new Matrix4f(new float[][] {
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}
        });
        Matrix4f m2 = new Matrix4f(new float[][] {
                {16, 15, 14, 13},
                {12, 11, 10, 9},
                {8, 7, 6, 5},
                {4, 3, 2, 1}
        });
        m1.add(m2);
        float[][] expected = {
                {17, 17, 17, 17},
                {17, 17, 17, 17},
                {17, 17, 17, 17},
                {17, 17, 17, 17}
        };
        assertArrayEquals(expected, m1.getMatrix());
    }

    @Test
    public void testMinus() {
        Matrix4f m1 = new Matrix4f(new float[][] {
                {17, 17, 17, 17},
                {17, 17, 17, 17},
                {17, 17, 17, 17},
                {17, 17, 17, 17}
        });
        Matrix4f m2 = new Matrix4f(new float[][] {
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}
        });
        m1.sub(m2);
        float[][] expected = {
                {16, 15, 14, 13},
                {12, 11, 10, 9},
                {8, 7, 6, 5},
                {4, 3, 2, 1}
        };
        assertArrayEquals(expected, m1.getMatrix());
    }

    @Test
    public void testMultiplyOnVector() {
        Matrix4f matrix = new Matrix4f(); // единичная матрица
        Vector4f vector = new Vector4f(1, 2, 3, 4);
        Vector4f result = matrix.multiplyOnVector(vector);
        assertEquals(1, result.getX(), 10e-6);
        assertEquals(2, result.getY(), 10e-6);
        assertEquals(3, result.getZ(), 10e-6);
        assertEquals(4, result.getW(), 10e-6);
    }

    @Test
    public void testMultiplyOnMatrix() {
        Matrix4f m1 = new Matrix4f(new float[][] {
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}
        });
        Matrix4f m2 = new Matrix4f(new float[][] {
                {16, 15, 14, 13},
                {12, 11, 10, 9},
                {8, 7, 6, 5},
                {4, 3, 2, 1}
        });
        Matrix4f result = m1.multiply(m2);
        assertNotNull(result);
        assertEquals(4, result.getMatrix().length);
        assertEquals(4, result.getMatrix()[0].length);
    }

    @Test
    public void testTransposition() {
        Matrix4f matrix = new Matrix4f(new float[][] {
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}
        });
        matrix.transpose();
        float[][] expected = {
                {1, 5, 9, 13},
                {2, 6, 10, 14},
                {3, 7, 11, 15},
                {4, 8, 12, 16}
        };
        assertArrayEquals(expected, matrix.getMatrix());
    }
}

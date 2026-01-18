package com.cgvsu.math;

import com.cgvsu.math.matrixs.Matrix3f;
import com.cgvsu.math.vectors.Vector3f;
import org.junit.Test;


import static org.junit.Assert.*;

public class Matrix3x3Tests {
    @Test
    public void testConstructorWithValidMatrix() {
        float[][] data = {
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        };
        Matrix3f matrix = new Matrix3f(data);
        assertArrayEquals(data, matrix.getMatrix());
    }

    @Test
    public void testSetValueAndGetValue() {
        Matrix3f matrix = new Matrix3f(new float[][] {
                {0, 0, 0},
                {0, 0, 0},
                {0, 0, 0}
        });
        matrix.setValue(1, 1, 5f);
        assertEquals(5f, matrix.getValue(1, 1), 10e-6);
    }

    @Test
    public void testPlus() {
        Matrix3f m1 = new Matrix3f(new float[][] {
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        });
        Matrix3f m2 = new Matrix3f(new float[][] {
                {9, 8, 7},
                {6, 5, 4},
                {3, 2, 1}
        });
        m1.add(m2);
        float[][] expected = {
                {10, 10, 10},
                {10, 10, 10},
                {10, 10, 10}
        };
        assertArrayEquals(expected, m1.getMatrix());
    }

    @Test
    public void testMinus() {
        Matrix3f m1 = new Matrix3f(new float[][] {
                {10, 10, 10},
                {10, 10, 10},
                {10, 10, 10}
        });
        Matrix3f m2 = new Matrix3f(new float[][] {
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        });
        m1.sub(m2);
        float[][] expected = {
                {9, 8, 7},
                {6, 5, 4},
                {3, 2, 1}
        };
        assertArrayEquals(expected, m1.getMatrix());
    }

    @Test
    public void testTranspose() {
        Matrix3f matrix = new Matrix3f(new float[][] {
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        });
        matrix.transpose();
        float[][] expected = {
                {1, 4, 7},
                {2, 5, 8},
                {3, 6, 9}
        };
        assertArrayEquals(expected, matrix.getMatrix());
    }

    @Test
    public void testMultiplyOnVector() {
        Matrix3f matrix = new Matrix3f(new float[][] {
                {1, 0, 0},
                {0, 1, 0},
                {0, 0, 1}
        });
        Vector3f vector = new Vector3f(1, 2, 3);
        Vector3f result = matrix.multiplyOnVector(vector);
        assertEquals(1, result.getX(), 10e-6);
        assertEquals(2, result.getY(), 10e-6);
        assertEquals(3, result.getZ(), 10e-6);
    }

    @Test
    public void testMultiplyOnMatrix() {
        Matrix3f m1 = new Matrix3f(new float[][] {
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        });
        Matrix3f m2 = new Matrix3f(new float[][] {
                {9, 8, 7},
                {6, 5, 4},
                {3, 2, 1}
        });
        Matrix3f result = m1.multiply(m2);
        assertNotNull(result);
        assertEquals(3, result.getMatrix().length);
        assertEquals(3, result.getMatrix()[0].length);
    }
}

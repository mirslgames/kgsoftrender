package com.cgvsu.math;

import com.cgvsu.math.vectors.Vector4f;
import org.junit.Before;
import org.junit.Test;


import static org.junit.Assert.assertEquals;

public class Vector4fTests {
    Vector4f vector4f;
    @Before
    public void setup(){
        vector4f = new Vector4f(1, 2, 2, 0);
    }

    @Test
    public void testLen() {
        assertEquals(3, vector4f.len(), 10e-6);
    }

    @Test
    public void testPlus() {
        vector4f.add(new Vector4f(2, -2, 4, 2));
        Vector4f testVector4f = new Vector4f(3, 0, 6, 2);

        assertEquals(testVector4f, vector4f);
    }

    @Test
    public void testMinus() {
        vector4f.sub(new Vector4f(2, -2, 4, 2));
        Vector4f testVector3f = new Vector4f(-1, 4, -2, -2);
        assertEquals(testVector3f, vector4f);
    }

    @Test
    public void testNormalize() {
        vector4f.normalize();
        Vector4f testVector3f = new Vector4f(1/3F, 2/3F, 2/3F, 0);
        assertEquals(testVector3f, vector4f);
    }

    @Test
    public void testMultiplyByScalar() {
        Vector4f testVector3f = new Vector4f(10, 20, 20, 0);
        vector4f.multiply(10);
        assertEquals(testVector3f, vector4f);
    }

    @Test
    public void testDivideByScalar() {
        Vector4f testVector3f = new Vector4f(0.5F, 1, 1, 0);
        vector4f.divide(2);
        assertEquals(testVector3f, vector4f);
    }

    @Test
    public void testScalarMultiply() {
        Vector4f testVector3f = new Vector4f(5, -3, 8, 0);
        assertEquals(15, testVector3f.dot(vector4f), 10e-6f);
    }
}

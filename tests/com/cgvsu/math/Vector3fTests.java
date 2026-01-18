package com.cgvsu.math;

import com.cgvsu.math.vectors.Vector3f;
import org.junit.Before;
import org.junit.Test;


import static org.junit.Assert.assertEquals;


public class Vector3fTests {
    Vector3f vector3f;

    @Before
    public void setup(){
        vector3f = new Vector3f(1, 2, 2);
    }

    @Test
    public void testLen() {
        assertEquals(3, vector3f.len(), 10e-6);
    }

    @Test
    public void testPlus() {
        vector3f.add(new Vector3f(2, -2, 4));
        Vector3f testVector3f = new Vector3f(3, 0, 6);
        assertEquals(testVector3f, vector3f);
    }

    @Test
    public void testMinus() {
        vector3f.sub(new Vector3f(2, -2, 4));
        Vector3f testVector3f = new Vector3f(-1, 4, -2);
        assertEquals(testVector3f, vector3f);
    }

    @Test
    public void testNormalize() {
        vector3f.normalize();
        Vector3f testVector3f = new Vector3f(1/3F, 2/3F, 2/3F);
        assertEquals(testVector3f, vector3f);
    }

    @Test
    public void testMultiplyByScalar() {
        Vector3f testVector3f = new Vector3f(10, 20, 20);
        vector3f.multiply(10);
        assertEquals(testVector3f, vector3f);
    }

    @Test
    public void testDivideByScalar() {
        Vector3f testVector3f = new Vector3f(0.5F, 1, 1);
        vector3f.divide(2);
        assertEquals(testVector3f, vector3f);
    }

    @Test
    public void testScalarMultiply() {
        Vector3f testVector3f = new Vector3f(5, -3, 8);
        assertEquals(15, testVector3f.dot(vector3f), 10e-6f);
    }

    @Test
    public void testVectorMultiply() {
        Vector3f secondVector = new Vector3f(2, -3, 4);
        Vector3f resultVector = new Vector3f(14, 0, -7);
        Vector3f mult = vector3f.cross(secondVector);
        assertEquals(resultVector, mult);
    }
}

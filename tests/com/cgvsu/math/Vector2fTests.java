package com.cgvsu.math;

import com.cgvsu.math.vectors.Vector2f;
import org.junit.Before;
import org.junit.Test;


import static org.junit.Assert.*;


public class Vector2fTests {

    Vector2f vector2f;

    @Before
    public void setup(){
        vector2f = new Vector2f(8, 6);
    }

    @Test
    public void testLen() {
        assertEquals(10, vector2f.len(), 10e-6);
    }

    @Test
    public void testPlus() {
        vector2f.add(new Vector2f(2, -2));
        Vector2f testVector2f = new Vector2f(10, 4);
        assertEquals(testVector2f, vector2f);
    }

    @Test
    public void testMinus() {
        vector2f.sub(new Vector2f(2, -2));
        Vector2f testVector2f = new Vector2f(6, 8);
        assertEquals(testVector2f, vector2f);
    }

    @Test
    public void testNormalize() {
        vector2f.normalize();
        Vector2f testVector2f = new Vector2f(0.8F, 0.6F);
        assertEquals(testVector2f, vector2f);
    }

    @Test
    public void testMultiplyByScalar() {
        Vector2f testVector2f = new Vector2f(80, 60);
        vector2f.multiply(10);
        assertEquals(testVector2f, vector2f);
    }

    @Test
    public void testDivideByScalar() {
        Vector2f testVector2f = new Vector2f(4, 3);
        vector2f.divide(2);
        assertEquals(testVector2f, vector2f);
    }

    @Test
    public void testScalarMultiply() {
        Vector2f testVector2f = new Vector2f(3, -2);
        assertEquals(12, testVector2f.dot(vector2f), 10e-6f);
    }


}

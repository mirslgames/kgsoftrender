package com.cgvsu.affine;

import com.cgvsu.math.matrixs.Matrix4f;
import com.cgvsu.render_engine.GraphicConveyor;
import org.junit.jupiter.api.Test;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

public class AffineTests {
    @Test
    public void testRotateX() {
        Matrix4f test = GraphicConveyor.rotate(45, 0, 0);
        Matrix4f result = new Matrix4f(new float[] {
                1, 0, 0, 0,
                0, (float) Math.sqrt(2) / 2, (float) Math.sqrt(2) / 2, 0,
                0, (float) -Math.sqrt(2) / 2, (float) Math.sqrt(2) / 2, 0,
                0, 0, 0, 1
        });
        assertTrue(result.equalsEps(test));
    }

    @Test
    public void testRotateY() {
        Matrix4f test = GraphicConveyor.rotate(0, 45, 0);
        Matrix4f result = new Matrix4f(new float[] {
                (float) Math.sqrt(2) / 2, 0, (float) Math.sqrt(2) / 2, 0,
                0, 1, 0, 0,
                (float) -Math.sqrt(2) / 2, 0, (float) Math.sqrt(2) / 2, 0,
                0, 0, 0, 1
        });
        assertTrue(result.equalsEps(test));
    }

    @Test
    public void testRotateZ() {
        Matrix4f test = GraphicConveyor.rotate(0, 0, 45);
        Matrix4f result = new Matrix4f(new float[] {
                (float) Math.sqrt(2) / 2, (float) Math.sqrt(2) / 2, 0, 0,
                (float) -Math.sqrt(2) / 2, (float) Math.sqrt(2) / 2, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1
        });
        assertTrue(result.equalsEps(test));
    }

    @Test
    public void testRotateXY() {
        Matrix4f test = GraphicConveyor.rotate(30, 30, 0);
        Matrix4f result = new Matrix4f(new float[] {
                (float) Math.sqrt(3) / 2, -1F / 4, (float) Math.sqrt(3) / 4, 0,
                0, (float) Math.sqrt(3) / 2, 1F / 2, 0,
                -1F/2, (float) -Math.sqrt(3) / 4, 3F/4, 0,
                0, 0, 0, 1
        });
        assertTrue(result.equalsEps(test));
    }

    @Test
    public void testRotateXZ() {
        Matrix4f test = GraphicConveyor.rotate(30, 0, 30);
        Matrix4f result = new Matrix4f(new float[] {
                (float) Math.sqrt(3) / 2, (float) Math.sqrt(3) / 4, 1F/4, 0,
                -1F/2, 3F/4, (float) Math.sqrt(3) / 4, 0,
                0, -1F/2, (float) Math.sqrt(3) / 2, 0,
                0, 0, 0, 1
        });
        assertTrue(result.equalsEps(test));
    }

    @Test
    public void testRotateYZ() {
        Matrix4f test = GraphicConveyor.rotate(0, 30, 30);
        Matrix4f result = new Matrix4f(new float[] {
                3F/4, 1F/2, (float) Math.sqrt(3) / 4, 0,
                (float) -Math.sqrt(3) / 4, (float) Math.sqrt(3) / 2, -1F/4, 0,
                -1F/2, 0, (float) Math.sqrt(3) / 2, 0,
                0, 0, 0, 1
        });
        assertTrue(result.equalsEps(test));
    }

    @Test
    public void testRotateXYZ() {
        Matrix4f test = GraphicConveyor.rotate(30, 45, 60);
        Matrix4f result = new Matrix4f(new float[] {
                (float) Math.sqrt(2) / 4, 3F/4 - (float) Math.sqrt(2) / 8, (float) (Math.sqrt(6) + 2 * Math.sqrt(3)) / 8, 0,
                (float) -Math.sqrt(6) / 4, (float) (Math.sqrt(6) + 2 * Math.sqrt(3)) / 8, (float) (2 - 3 * Math.sqrt(2)) / 8, 0,
                (float) -Math.sqrt(2) / 2, (float) -Math.sqrt(2) / 4, (float) Math.sqrt(6) / 4, 0,
                0, 0, 0, 1
        });
        assertTrue(result.equalsEps(test));
    }

    @Test
    public void testScaleRotate() {
        Matrix4f test = GraphicConveyor.rotateScaleTranslate(2, 2, 2, 90, 90, 90, 0,0, 0);
        Matrix4f result = new Matrix4f(new float[] {
                        0, 0, 2, 0,
                        0, 2, 0, 0,
                        -2, 0, 0, 0,
                        0, 0, 0, 1
        });
        assertTrue(result.equalsEps(test));
    }

    @Test
    public void testScaleRotateTranslate() {
        Matrix4f test = GraphicConveyor.rotateScaleTranslate(2, 2, 2, 90, 90, 90, 10,10, 10);
        Matrix4f result = new Matrix4f(new float[] {
                0, 0, 2, 10,
                0, 2, 0, 10,
                -2, 0, 0, 10,
                0, 0, 0, 1
        });
        assertTrue(result.equalsEps(test));
    }



}

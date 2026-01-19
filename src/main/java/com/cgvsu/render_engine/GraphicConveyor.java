package com.cgvsu.render_engine;


import com.cgvsu.math.matrixs.Matrix4f;
import com.cgvsu.math.point.Point2f;
import com.cgvsu.math.vectors.Vector3f;

import java.util.Arrays;

public class GraphicConveyor {

    public static Matrix4f rotateScaleTranslate(
            float sx, float sy, float    sz,
            float dx, float dy, float dz,
            float tx, float ty, float tz
    ) {
        Matrix4f scaleMatrix = new Matrix4f(new float[] {
                sx, 0, 0, 0,
                0, sy, 0, 0,
                0, 0, sz, 0,
                0, 0, 0, 1
        });
        Matrix4f rotateMatrix = rotate(dx, dy, dz);

        Matrix4f translate = new Matrix4f(new float[] {
                1, 0, 0, tx,
                0, 1, 0, ty,
                0, 0, 1, tz,
                0, 0, 0, 1
        });

        translate.multiply(rotateMatrix);
        translate.multiply(scaleMatrix);

        return translate;
    }

    public static Matrix4f rotateScaleTranslate() {
        return  rotateScaleTranslate(1, 1, 1, 0, 0,0, 0,0,0);
    }

     public static Matrix4f rotate(float dx, float dy, float dz) {
        dx = (float) Math.toRadians(dx);
        dy = (float) Math.toRadians(dy);
        dz = (float) Math.toRadians(dz);

        Matrix4f rotateZMatrix = new Matrix4f(new float[] {
                (float) Math.cos(dz), (float) Math.sin(dz), 0, 0,
                (float) -Math.sin(dz), (float) Math.cos(dz), 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1
        });

        Matrix4f rotateYMatrix = new Matrix4f(new float[] {
                (float) Math.cos(dy), 0, (float) Math.sin(dy), 0,
                0, 1, 0, 0,
                (float) -Math.sin(dy), 0, (float) Math.cos(dy), 0,
                0, 0, 0, 1

        });

        Matrix4f rotateXMatrix = new Matrix4f(new float[] {
                1, 0, 0, 0,
                0, (float) Math.cos(dx), (float) Math.sin(dx), 0,
                0, (float) -Math.sin(dx), (float) Math.cos(dx), 0,
                0, 0, 0, 1

        });
        Matrix4f result = new Matrix4f(rotateZMatrix.getMatrix());
        result.multiply(rotateYMatrix);
        result.multiply(rotateXMatrix);
        return result;
    }

    public static Matrix4f lookAt(Vector3f eye, Vector3f target) {
        return lookAt(eye, target, new Vector3f(0F, 1.0F, 0F));
    }

    public static Matrix4f lookAt(Vector3f eye, Vector3f target, Vector3f up) {
        Vector3f resultX = new Vector3f();
        Vector3f resultY = new Vector3f();
        Vector3f resultZ = new Vector3f();

        resultZ.sub(target, eye);
        resultX.cross(up, resultZ);
        resultY.cross(resultZ, resultX);

        resultX.normalize();
        resultY.normalize();
        resultZ.normalize();

        float[] matrix = new float[]{
                resultX.getX(), resultX.getY(), resultX.getZ(), -resultX.dot(eye),
                resultY.getX(), resultY.getY(), resultY.getZ(), -resultY.dot(eye),
                resultZ.getX(), resultZ.getY(), resultZ.getZ(), -resultZ.dot(eye),
                0, 0, 0, 1
        };
        return new Matrix4f(matrix);
    }

    public static Matrix4f perspective(
            final float fov,
            final float aspectRatio,
            final float nearPlane,
            final float farPlane) {

        Matrix4f result = new Matrix4f();

        float f = (float) (1.0 / Math.tan(fov * 0.5f));

        result.setValue(0, 0, f / aspectRatio);
        result.setValue(1, 1, f);

        result.setValue(2, 2, (farPlane + nearPlane) / (farPlane - nearPlane));
        result.setValue(2, 3, 2 * (nearPlane * farPlane) / (nearPlane - farPlane));

        result.setValue(3, 2, 1.0F);
        result.setValue(3, 3, 0.0F);

        return result;
    }



    public static Point2f vertexToPoint(final Vector3f vertex, final int width, final int height) {
        return new Point2f(
                (width - 1) * vertex.getX() / 2 + (width - 1) / 2,
                (1 - height) * vertex.getY() / 2 + (height - 1) / 2
        );
    }
}

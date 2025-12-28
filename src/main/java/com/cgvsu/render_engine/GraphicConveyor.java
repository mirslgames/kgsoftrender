package com.cgvsu.render_engine;


import com.cgvsu.math.matrixs.Matrix4f;
import com.cgvsu.math.point.Point2f;
import com.cgvsu.math.vectors.Vector3f;

import java.util.Arrays;

public class GraphicConveyor {

    public static Matrix4f rotateScaleTranslate() {
        return new Matrix4f();
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
                0, 0, 0, 1};
        return new Matrix4f(matrix);
    }

    public static Matrix4f perspective(
            final float fov,
            final float aspectRatio,
            final float nearPlane,
            final float farPlane) {
        Matrix4f result = new Matrix4f();
        // float tangentMinusOnDegree = (float) (1.0F / (Math.tan(Math.toRadians(fov * 0.5F))));
        float tangentMinusOnDegree = (float) (1.0F / (Math.tan(fov * 0.5F)));
        result.setValue(0, 0, tangentMinusOnDegree);
        result.setValue(1, 1, tangentMinusOnDegree  / aspectRatio);
        result.setValue(2, 2, (farPlane + nearPlane) / (farPlane - nearPlane));
        result.setValue(2, 3, 2 * (nearPlane * farPlane) / (nearPlane - farPlane));
        result.setValue(3, 2, 1.0F);
        result.setValue(3,3, 0);
        return result;
    }

    public static Point2f vertexToPoint(final Vector3f vertex, final int width, final int height) {
        return new Point2f(
                (width - 1) * vertex.getX() / 2 + (width - 1) / 2,
                (1 - height) * vertex.getY() / 2 + (height - 1) / 2
        );
    }
}

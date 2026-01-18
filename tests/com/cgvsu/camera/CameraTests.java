package com.cgvsu.camera;


import com.cgvsu.math.matrixs.Matrix4f;
import com.cgvsu.math.vectors.Vector3f;
import com.cgvsu.render_engine.Camera;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

public class CameraTests {
    private static final float EPS = 1e-4f;

    private static void assertVecEquals(Vector3f expected, Vector3f actual, float eps) {
        assertEquals(expected.getX(), actual.getX(), eps, "X mismatch");
        assertEquals(expected.getY(), actual.getY(), eps, "Y mismatch");
        assertEquals(expected.getZ(), actual.getZ(), eps, "Z mismatch");
    }

    private static void assertVecFinite(Vector3f v) {
        assertTrue(Float.isFinite(v.getX()), "X is not finite");
        assertTrue(Float.isFinite(v.getY()), "Y is not finite");
        assertTrue(Float.isFinite(v.getZ()), "Z is not finite");
    }

    private static float dist(Vector3f a, Vector3f b) {
        return a.subbed(b).len();
    }

    private static Camera newDefaultCamera(float z) {
        float fov = (float) Math.toRadians(60.0);
        float aspect = 16f / 9f;
        return new Camera(
                new Vector3f(0, 0, z),
                new Vector3f(0, 0, 0),
                fov,
                aspect,
                0.1f,
                10_000f
        );
    }

    private static float getPrivateFloat(Object obj, String fieldName) {
        try {
            Field f = obj.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            return f.getFloat(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read private float field: " + fieldName, e);
        }
    }

    private static Vector3f getPrivateVector3f(Object obj, String fieldName) {
        try {
            Field f = obj.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            return (Vector3f) f.get(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read private Vector3f field: " + fieldName, e);
        }
    }

    private static void assertMatrixFinite(Matrix4f m) {
        assertNotNull(m);
        float[][] a = m.getMatrix(); // у тебя это уже используется в проекте
        assertNotNull(a);
        for (int r = 0; r < a.length; r++) {
            for (int c = 0; c < a[r].length; c++) {
                assertTrue(Float.isFinite(a[r][c]), "Matrix has non-finite at [" + r + "][" + c + "]");
            }
        }
    }

    @Test
    void constructor_setsRadiusEqualToDistance() {
        Camera cam = newDefaultCamera(50f);
        float radius = getPrivateFloat(cam, "radius");
        float d = dist(cam.getPosition(), cam.getTarget());
        assertEquals(d, radius, EPS);
    }

    @Test
    void rotateYaw_keepsTargetAndRadius() {
        Camera cam = newDefaultCamera(10f);

        Vector3f targetBefore = cam.getTarget();
        float radiusBefore = dist(cam.getPosition(), cam.getTarget());

        cam.rotateCamera(45f, 0f);

        assertVecEquals(targetBefore, cam.getTarget(), EPS);
        float radiusAfter = dist(cam.getPosition(), cam.getTarget());
        assertEquals(radiusBefore, radiusAfter, 1e-3f);

        // cameraUp должен быть нормализован и ортогонален forward
        Vector3f forward = cam.getTarget().subbed(cam.getPosition()).normalized();
        Vector3f up = getPrivateVector3f(cam, "cameraUp");
        assertEquals(1f, up.len(), 1e-3f);
        assertEquals(0f, forward.dot(up), 1e-3f);
    }

    @Test
    void rotatePitch_clampsNearPoles_doesNotFlipToVertical() {
        Camera cam = newDefaultCamera(50f);

        Vector3f posBefore = cam.getPosition();
        Vector3f targetBefore = cam.getTarget();

        cam.rotateCamera(0f, 90f);

        assertVecEquals(posBefore, cam.getPosition(), 1e-3f);
        assertVecEquals(targetBefore, cam.getTarget(), 1e-6f);
    }

    @Test
    void rotatePitch_reasonableAngle_changesPosition_keepsRadius() {
        Camera cam = newDefaultCamera(50f);
        float radiusBefore = dist(cam.getPosition(), cam.getTarget());

        cam.rotateCamera(0f, 30f);

        float radiusAfter = dist(cam.getPosition(), cam.getTarget());
        assertEquals(radiusBefore, radiusAfter, 1e-3f);
        assertTrue(dist(cam.getPosition(), new Vector3f(0, 0, 50f)) > 1e-3f, "Position should change");
    }

    @Test
    void moveCamera_translatesPositionAndTarget_equally_keepsOffsetAndRadius() {
        Camera cam = newDefaultCamera(10f);

        Vector3f offsetBefore = cam.getPosition().subbed(cam.getTarget());
        float radiusBefore = dist(cam.getPosition(), cam.getTarget());

        cam.moveCamera(120f, -80f, 1920, 1080);

        Vector3f offsetAfter = cam.getPosition().subbed(cam.getTarget());
        assertVecEquals(offsetBefore, offsetAfter, 1e-5f);

        float radiusAfter = dist(cam.getPosition(), cam.getTarget());
        assertEquals(radiusBefore, radiusAfter, 1e-3f);

        // sanity: должно реально сдвинуться
        assertTrue(dist(cam.getTarget(), new Vector3f(0, 0, 0)) > 1e-6f);
    }

    @Test
    void moveCamera_zeroDelta_doesNothing() {
        Camera cam = newDefaultCamera(10f);

        Vector3f posBefore = cam.getPosition();
        Vector3f tgtBefore = cam.getTarget();

        cam.moveCamera(0f, 0f, 800, 600);

        assertVecEquals(posBefore, cam.getPosition(), 0f);
        assertVecEquals(tgtBefore, cam.getTarget(), 0f);
    }

    @Test
    void zoomCamera_decreasesRadius_andClampsToMin() {
        Camera cam = newDefaultCamera(10f);

        // важно: в твоей реализации yaw/pitch обновляются в rotateCamera,
        // поэтому "инициализируем" их, чтобы zoom работал предсказуемо.
        cam.rotateCamera(0f, 0f);

        cam.zoomCamera(2f);
        assertEquals(8f, dist(cam.getPosition(), cam.getTarget()), 1e-3f);

        cam.zoomCamera(10_000f); // упрёмся в clamp(0.5)
        assertEquals(0.5f, dist(cam.getPosition(), cam.getTarget()), 1e-3f);
    }

    @Test
    void getRayToPoint_returnsNormalizedDirection_andHandlesInvalidInput() {
        Camera cam = newDefaultCamera(10f);

        Vector3f ray = cam.getRayToPoint(new Vector3f(0, 0, 0));
        assertVecFinite(ray);
        assertEquals(1f, ray.len(), 1e-3f);
        assertEquals(0f, ray.getX(), 1e-3f);
        assertEquals(0f, ray.getY(), 1e-3f);
        assertEquals(-1f, ray.getZ(), 1e-3f);

        Vector3f rayNull = cam.getRayToPoint(null);
        assertVecEquals(new Vector3f(0, 0, -1), rayNull, 0f);

        Vector3f rayNaN = cam.getRayToPoint(new Vector3f(Float.NaN, 0, 0));
        assertVecEquals(new Vector3f(0, 0, -1), rayNaN, 0f);

        Vector3f rayZero = cam.getRayToPoint(cam.getPosition());
        assertVecEquals(new Vector3f(0, 0, -1), rayZero, 0f);
    }

    @Test
    void returnToDefaultCamera_resetsState() {
        Camera cam = newDefaultCamera(10f);
        cam.rotateCamera(30f, 20f);
        cam.moveCamera(50f, 20f, 800, 600);
        cam.zoomCamera(3f);

        cam.returnToDefaultCamera();

        assertVecEquals(new Vector3f(0, 0, 50f), cam.getPosition(), 1e-6f);
        assertVecEquals(new Vector3f(0, 0, 0), cam.getTarget(), 1e-6f);

        float yaw = getPrivateFloat(cam, "yaw");
        float pitch = getPrivateFloat(cam, "pitch");
        float radius = getPrivateFloat(cam, "radius");
        Vector3f up = getPrivateVector3f(cam, "cameraUp");

        assertEquals(0f, yaw, 0f);
        assertEquals(0f, pitch, 0f);
        assertEquals(50f, radius, 1e-6f);
        assertVecEquals(new Vector3f(0, 1, 0), up, 1e-6f);
    }

    @Test
    void viewAndProjectionMatrices_areFinite() {
        Camera cam = newDefaultCamera(50f);

        Matrix4f view = cam.getViewMatrix();
        Matrix4f proj = cam.getProjectionMatrix();

        assertMatrixFinite(view);
        assertMatrixFinite(proj);
    }
}

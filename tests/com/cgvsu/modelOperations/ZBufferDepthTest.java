package com.cgvsu.modelOperations;

import com.cgvsu.math.matrixs.Matrix4f;
import com.cgvsu.math.point.Point2f;
import com.cgvsu.math.vectors.Vector3f;
import com.cgvsu.model.Vertex;
import com.cgvsu.render_engine.Camera;
import com.cgvsu.render_engine.GraphicConveyor;
import org.junit.jupiter.api.Test;

import static com.cgvsu.render_engine.GraphicConveyor.rotateScaleTranslate;
import static com.cgvsu.render_engine.GraphicConveyor.vertexToPoint;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для проверки правильности работы z-buffer.
 * Проверяет, что глубина правильно вычисляется и сравнивается для разных треугольников.
 */
public class ZBufferDepthTest {

//    @Test
//    public void testZBufferDepthOrdering() {
//        // Создаём простой треугольник
//        ZBuffer zBuffer = new ZBuffer(100, 100);
//        zBuffer.clear();
//
//        // Ближний пиксель (z = 0.1) должен перекрыть дальний (z = 0.9)
//        assertTrue(zBuffer.testAndSet(50, 50, 0.1f), "Ближний пиксель должен пройти проверку");
//        assertFalse(zBuffer.testAndSet(50, 50, 0.9f), "Дальний пиксель должен быть отклонён");
//        assertEquals(0.1f, zBuffer.getDepth(50, 50), 0.001f, "Глубина должна быть ближней");
//    }

    @Test
    public void testZBufferWithTriangleAtDifferentDepths() {
        // Тест: два треугольника на разной глубине
        // Ближний треугольник должен перекрыть дальний

        Camera camera = new Camera(
                new Vector3f(0, 0, 5),  // Камера смотрит вдоль Z
                new Vector3f(0, 0, 0),
                60, 1.0f, 0.1f, 100
        );

        ZBuffer zBuffer = new ZBuffer(200, 200);
        zBuffer.clear();

        // Ближний треугольник: вершины на z = 1 в world space
        Vertex v1 = new Vertex();
        v1.position = new Vector3f(-1, -1, 1);
        Vertex v2 = new Vertex();
        v2.position = new Vector3f(1, -1, 1);
        Vertex v3 = new Vertex();
        v3.position = new Vector3f(0, 1, 1);

        // Дальний треугольник: вершины на z = 2 в world space (за первым)
        Vertex v4 = new Vertex();
        v4.position = new Vector3f(-1, -1, 2);
        Vertex v5 = new Vertex();
        v5.position = new Vector3f(1, -1, 2);
        Vertex v6 = new Vertex();
        v6.position = new Vector3f(0, 1, 2);

        Matrix4f modelMatrix = rotateScaleTranslate(1, 1, 1, 0, 0, 0, 0, 0, 0);
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projMatrix = camera.getProjectionMatrix();

        Matrix4f modelViewMatrix = new Matrix4f(viewMatrix.getMatrix());
        modelViewMatrix.multiply(modelMatrix);

        // Преобразуем вершины ближнего треугольника
        Vector3f view1 = modelViewMatrix.multiplyOnVector(v1.position);
        Vector3f view2 = modelViewMatrix.multiplyOnVector(v2.position);
        Vector3f view3 = modelViewMatrix.multiplyOnVector(v3.position);

        Vector3f proj1 = projMatrix.multiplyOnVector(view1);
        Vector3f proj2 = projMatrix.multiplyOnVector(view2);
        Vector3f proj3 = projMatrix.multiplyOnVector(view3);

        Point2f p1 = vertexToPoint(proj1, 200, 200);
        Point2f p2 = vertexToPoint(proj2, 200, 200);
        Point2f p3 = vertexToPoint(proj3, 200, 200);

        float zProj1 = proj1.getZ();
        float zProj2 = proj2.getZ();
        float zProj3 = proj3.getZ();

        // Преобразуем для z-buffer: z в [0, 1], где меньшее = ближе
        float z1Near = 1.0f - (zProj1 + 1.0f) * 0.5f;
        float z2Near = 1.0f - (zProj2 + 1.0f) * 0.5f;
        float z3Near = 1.0f - (zProj3 + 1.0f) * 0.5f;

        // Преобразуем вершины дальнего треугольника
        Vector3f view4 = modelViewMatrix.multiplyOnVector(v4.position);
        Vector3f view5 = modelViewMatrix.multiplyOnVector(v5.position);
        Vector3f view6 = modelViewMatrix.multiplyOnVector(v6.position);

        Vector3f proj4 = projMatrix.multiplyOnVector(view4);
        Vector3f proj5 = projMatrix.multiplyOnVector(view5);
        Vector3f proj6 = projMatrix.multiplyOnVector(view6);

        float zProj4 = proj4.getZ();
        float zProj5 = proj5.getZ();
        float zProj6 = proj6.getZ();

        float z4Far = 1.0f - (zProj4 + 1.0f) * 0.5f;
        float z5Far = 1.0f - (zProj5 + 1.0f) * 0.5f;
        float z6Far = 1.0f - (zProj6 + 1.0f) * 0.5f;

        // Проверяем: ближние z должны быть меньше дальних (меньше = ближе)
        assertTrue(z1Near < z4Far, 
                String.format("Ближний z (%f) должен быть меньше дальнего (%f)", z1Near, z4Far));

        // Центр ближнего треугольника
        int centerX = (int)((p1.getX() + p2.getX() + p3.getX()) / 3);
        int centerY = (int)((p1.getY() + p2.getY() + p3.getY()) / 3);
        float centerZNear = (z1Near + z2Near + z3Near) / 3.0f;

        // Сначала рисуем дальний треугольник (должен пройти)
        assertTrue(zBuffer.testAndSet(centerX, centerY, z4Far), 
                "Дальний треугольник должен пройти проверку при первом рисовании");

        // Затем рисуем ближний треугольник (должен перекрыть дальний)
        boolean nearPassed = zBuffer.testAndSet(centerX, centerY, centerZNear);
        assertTrue(nearPassed, 
                "Ближний треугольник должен перекрыть дальний");

        // Проверяем, что глубина обновилась на ближнюю
        assertEquals(centerZNear, zBuffer.getDepth(centerX, centerY), 0.001f,
                "Глубина должна быть ближней");

        // Дальний треугольник не должен пройти проверку
        assertFalse(zBuffer.testAndSet(centerX, centerY, z4Far),
                "Дальний треугольник не должен пройти проверку после ближнего");
    }

    @Test
    public void testZInterpolationConsistency() {
        // Проверяем, что перспективная интерполяция z работает правильно

        // Простой треугольник с разными z
        Point2f p1 = new Point2f(10, 10);
        Point2f p2 = new Point2f(50, 10);
        Point2f p3 = new Point2f(30, 50);

        // z после проекции (в NDC [-1, 1])
        float zProj1 = 0.5f;  // Средняя глубина
        float zProj2 = 0.6f;  // Чуть дальше
        float zProj3 = 0.4f;  // Чуть ближе

        // z в view space для перспективной интерполяции
        float zView1 = -2.0f;
        float zView2 = -2.5f;
        float zView3 = -1.5f;

        // Центр треугольника (барицентрики 1/3, 1/3, 1/3)
        float[] center = {1.0f/3.0f, 1.0f/3.0f, 1.0f/3.0f};

        // Линейная интерполяция (неправильная)
        float zLinear = Rasterization.interpolate(zProj1, zProj2, zProj3, center);
        float expectedLinear = (zProj1 + zProj2 + zProj3) / 3.0f;
        assertEquals(expectedLinear, zLinear, 0.001f, "Линейная интерполяция должна быть средним");

        // Перспективная интерполяция
        float zPerspective = Rasterization.interpolateZWithPerspective(
                zProj1, zProj2, zProj3,
                zView1, zView2, zView3,
                center);

        // Перспективная интерполяция должна отличаться от линейной
        assertNotEquals(zLinear, zPerspective, 0.01f,
                "Перспективная интерполяция должна отличаться от линейной");

        // Проверяем, что результат в разумном диапазоне
        assertTrue(zPerspective >= -1.0f && zPerspective <= 1.0f,
                "Интерполированное z должно быть в диапазоне NDC [-1, 1]");
    }


    @Test
    public void testDepthCalculation() {
        Camera camera = new Camera(
                new Vector3f(0, 0, 5),
                new Vector3f(0, 0, 0),
                60, 1.0f, 0.1f, 100
        );

        Vector3f nearPoint = new Vector3f(0, 0, -0.1f);  // на near
        Vector3f midPoint = new Vector3f(0, 0, -50f);    // посередине
        Vector3f farPoint = new Vector3f(0, 0, -100f);   // на far

        float zNear = calculateDepthForZBuffer(nearPoint, camera);  // должно быть ~1.0
        float zMid = calculateDepthForZBuffer(midPoint, camera);    // должно быть ~0.5
        float zFar = calculateDepthForZBuffer(farPoint, camera);    // должно быть ~0.0

        System.out.printf("zNear=%f, zMid=%f, zFar=%f%n", zNear, zMid, zFar);

        // Если большее значение = ближе:
        assertTrue(zNear > zMid, String.format("%f > %f", zNear, zMid));
        assertTrue(zMid > zFar, String.format("%f > %f", zMid, zFar));

        // Или если меньшее значение = ближе:
        // assertTrue(zNear < zMid);
        // assertTrue(zMid < zFar);
    }
    public static float calculateDepthForZBuffer(Vector3f viewPos, Camera camera) {
        float near = camera.getNearPlane();
        float far = camera.getFarPlane();
        float zView = Math.abs(viewPos.getZ());  // расстояние от камеры

        // Линейное преобразование: 0.0 = near, 1.0 = far
        float zBufferValue = (zView - near) / (far - near);

        return zBufferValue;  // меньшее = ближе!
    }
    @Test
    public void testZBufferDepthOrdering() {
        ZBuffer zBuffer = new ZBuffer(100, 100);
        zBuffer.clear();

        // В вашем z-buffer: МЕНЬШЕЕ значение = БЛИЖЕ
        assertTrue(zBuffer.testAndSet(50, 50, 0.1f), "0.1 (ближе) должно пройти");
        assertFalse(zBuffer.testAndSet(50, 50, 0.9f), "0.9 (дальше) должно быть отклонено");
        assertTrue(zBuffer.testAndSet(50, 50, 0.05f), "0.05 (ещё ближе) должно пройти");

        assertEquals(0.05f, zBuffer.getDepth(50, 50), 0.001f);
    }
    @Test
    public void testZProjectionTransformation() {
        Camera camera = new Camera(
                new Vector3f(0, 0, 5),
                new Vector3f(0, 0, 0),
                60, 1.0f, 0.1f, 100
        );

        Vector3f nearPoint = new Vector3f(0, 0, -0.1f);   // на near plane
        Vector3f midPoint = new Vector3f(0, 0, -5.0f);    // посередине
        Vector3f farPoint = new Vector3f(0, 0, -100f);    // на far plane

        float zNear = calculateDepthForZBuffer(nearPoint, camera);  // должно быть ~0.0
        float zMid = calculateDepthForZBuffer(midPoint, camera);    // должно быть ~0.05
        float zFar = calculateDepthForZBuffer(farPoint, camera);    // должно быть ~1.0

        System.out.printf("zNear=%f, zMid=%f, zFar=%f%n", zNear, zMid, zFar);

        // Проверяем: меньшее значение = ближе
        assertTrue(zNear < zMid, String.format("%f < %f (near < mid)", zNear, zMid));
        assertTrue(zMid < zFar, String.format("%f < %f (mid < far)", zMid, zFar));

        // Диапазон [0, 1]
        assertTrue(zNear >= 0.0f && zNear <= 1.0f);
        assertTrue(zMid >= 0.0f && zMid <= 1.0f);
        assertTrue(zFar >= 0.0f && zFar <= 1.0f);
    }

}


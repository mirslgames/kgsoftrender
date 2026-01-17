package com.cgvsu.modelOperations;

import com.cgvsu.math.matrixs.Matrix4f;
import com.cgvsu.math.vectors.Vector3f;
import com.cgvsu.model.Vertex;
import com.cgvsu.render_engine.Camera;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ZBufferTests {
    @Test
    public void testZBufferBasicFunctionality() {
        ZBuffer zBuffer = new ZBuffer(100, 100);
        zBuffer.clear();

        // Ближний пиксель должен перекрыть дальний
        assertTrue(zBuffer.testAndSet(50, 50, 0.5f)); // Должен отрисоваться
        assertFalse(zBuffer.testAndSet(50, 50, 0.8f)); // Не должен отрисоваться (дальше)
        assertTrue(zBuffer.testAndSet(50, 50, 0.3f)); // Должен отрисоваться (ближе)

        assertEquals(0.3f, zBuffer.getDepth(50, 50), 0.001f);
    }
    @Test
    public void testZValuesInRender() {
        Camera camera = new Camera(
                new Vector3f(1, 1, 5),
                new Vector3f(1, 1, 0),
                60, 1, 0.1f, 100
        );

        Vertex v1 = new Vertex();
        v1.position = new Vector3f(0, 0, 0);
        v1.normal = new Vector3f(-1, -1, 1).normalized();

        Vector3f pos = v1.position;
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projMatrix = camera.getProjectionMatrix();

        // Преобразуем в view space
        Vector3f viewPos = viewMatrix.multiplyOnVector(pos);
        System.out.println("View позиция: " + viewPos + ", zView=" + viewPos.getZ());

        // Преобразуем в projection space
        Vector3f projPos = projMatrix.multiplyOnVector(viewPos);
        System.out.println("Projection позиция: " + projPos + ", zProj=" + projPos.getZ());

        // Важно: в какой системе координат z после проекции?
        // Обычно после проекции z в диапазоне [-1, 1] или [0, 1]

        // Проверяем
        assertTrue(
                viewPos.getZ() < 0);

        assertTrue(Math.abs(projPos.getZ()) <= 1.0f);
    }
    @Test
    public void testProjectionZValues() {
        Camera camera = new Camera(
                new Vector3f(1, 1, 5),
                new Vector3f(1, 1, 0),
                60, 800.0f/600.0f, 0.1f, 100
        );

        // Точка в центре треугольника
        Vector3f point = new Vector3f(1, 1, 0);

        Matrix4f viewMatrix = camera.getViewMatrix();
        Vector3f viewPos = viewMatrix.multiplyOnVector(point);

        System.out.println("View позиция: " + viewPos);
        System.out.println("z в view space: " + viewPos.getZ());

        Matrix4f projMatrix = camera.getProjectionMatrix();
        Vector3f projPos = projMatrix.multiplyOnVector(viewPos);

        System.out.println("Projection позиция: " + projPos);
        System.out.println("z после проекции: " + projPos.getZ());

        // Ожидаемый диапазон: [-1, 1] или [0, 1]
        // near plane (0.1) → z = -1 или 0
        // far plane (100) → z = 1
        // Ваше значение ≈ 1.0001 - чуть больше 1!
    }
    @Test
    public void testZValuesPipeline() {
        Camera camera = new Camera(
                new Vector3f(1, 1, 5),
                new Vector3f(1, 1, 0),
                60, 800.0f/600.0f, 0.1f, 100
        );

        // Точка треугольника
        Vector3f point = new Vector3f(0, 0, 0);

        Matrix4f viewMatrix = camera.getViewMatrix();
        Vector3f viewPos = viewMatrix.multiplyOnVector(point);

        System.out.println("1. View space:");
        System.out.println("   viewPos: " + viewPos);
        System.out.println("   zView: " + viewPos.getZ() + " (должно быть ~5.0)");

        Matrix4f projMatrix = camera.getProjectionMatrix();
        Vector3f projected = projMatrix.multiplyOnVector(viewPos);

        System.out.println("\n2. After projection (NDC):");
        System.out.println("   projected: " + projected);
        System.out.println("   zNDC: " + projected.getZ() + " (должно быть ~0.96, в [-1,1])");

        // Проверка диапазона
        if (projected.getZ() < -1.0f || projected.getZ() > 1.0f) {
            System.out.println("   ⚠️  z вне диапазона NDC [-1,1]!");
        }

        System.out.println("\n3. Для z-buffer ([0,1] диапазон):");
        float zBufferValue = (projected.getZ() + 1.0f) * 0.5f;
        System.out.println("   zBuffer: " + zBufferValue + " (должно быть ~0.98, в [0,1])");

        // Что вы сохраняете в z1, z2, z3?
        System.out.println("\n4. В рендерере нужно сохранять:");
        System.out.println("   zScreen = projected.getZ() = " + projected.getZ());
        System.out.println("   НЕ viewPos.getZ() = " + viewPos.getZ());
    }
}

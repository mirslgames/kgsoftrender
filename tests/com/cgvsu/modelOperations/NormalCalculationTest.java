package com.cgvsu.modelOperations;

import com.cgvsu.math.vectors.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Vertex;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class NormalCalculationTest {
    //Тут напишу тесты нормали рассчёта нормали с ручным вычислением на конкретной значении для треугольника и т.п.
    @Test
    public void testDegenerateTriangle() {
        Model model = new Model();

        // Три одинаковые вершины
        Vertex v1 = new Vertex();
        v1.position = new Vector3f(0, 0, 0);

        Vertex v2 = new Vertex();
        v2.position = new Vector3f(0, 0, 0);

        Vertex v3 = new Vertex();
        v3.position = new Vector3f(0, 0, 0);

        model.vertices.addAll(Arrays.asList(v1, v2, v3));
        model.polygonsBoundaries.add(0);
        model.polygons.addAll(Arrays.asList(0, 1, 2));

        // Вычисляем нормали
        MyVertexNormalCalc calculator = new MyVertexNormalCalc();
        calculator.calculateVertexNormals(model);

        // У вырожденного треугольника нормаль должна быть нулевой
        for (Vertex vertex : model.vertices) {
            assertNotNull(vertex.normal);
            assertEquals(0, vertex.normal.getX(), 0.001f);
            assertEquals(0, vertex.normal.getY(), 0.001f);
            assertEquals(0, vertex.normal.getZ(), 0.001f);
        }
    }
    private String vectorToString(Vector3f v) {
        if (v == null) return "null";
        return String.format("(%.2f, %.2f, %.2f)", v.getX(), v.getY(), v.getZ());
    }

    private String vertexToString(Vertex v) {
        if (v == null) return "null";
        return "pos=" + vectorToString(v.position) + ", normal=" + vectorToString(v.normal);
    }
    @Test
    public void testSharedVertexNormal() {
        Model model = new Model();

        // Вспомогательный метод для печати
        java.util.function.Function<Vector3f, String> vecStr = v ->
                v == null ? "null" : String.format("(%.2f, %.2f, %.2f)", v.getX(), v.getY(), v.getZ());

        // Создаем два треугольника в разных плоскостях
        Vertex v1 = new Vertex(); // (0,0,0) - общая вершина
        v1.position = new Vector3f(0, 0, 0);

        Vertex v2 = new Vertex();
        v2.position = new Vector3f(1, 0, 0);

        Vertex v3 = new Vertex();
        v3.position = new Vector3f(0, 1, 0); // для треугольника 1 (плоскость XY)

        Vertex v4 = new Vertex();
        v4.position = new Vector3f(0, 0, 1); // для треугольника 2 (плоскость XZ)

        model.vertices.addAll(Arrays.asList(v1, v2, v3, v4));

        // Треугольник 1: (0,0,0), (1,0,0), (0,1,0) - нормаль (0,0,1)
        model.polygonsBoundaries.add(0);
        model.polygons.addAll(Arrays.asList(0, 1, 2));

        // Треугольник 2: (0,0,0), (1,0,0), (0,0,1) - нормаль (0,-1,0)
        model.polygonsBoundaries.add(3);
        model.polygons.addAll(Arrays.asList(0, 1, 3));

        System.out.println("=== Начальные данные ===");
        System.out.println("Треугольник 1 вершины:");
        for (int i = 0; i < 3; i++) {
            int idx = model.polygons.get(i);
            Vertex v = model.vertices.get(idx);
            System.out.println("  [" + idx + "] " + vecStr.apply(v.position));
        }

        System.out.println("Треугольник 2 вершины:");
        for (int i = 3; i < 6; i++) {
            int idx = model.polygons.get(i);
            Vertex v = model.vertices.get(idx);
            System.out.println("  [" + idx + "] " + vecStr.apply(v.position));
        }

        // Вычисляем нормали
        MyVertexNormalCalc calculator = new MyVertexNormalCalc();
        calculator.calculateVertexNormals(model);

        System.out.println("\n=== Результат ===");
        for (int i = 0; i < model.vertices.size(); i++) {
            Vertex vertex = model.vertices.get(i);
            System.out.println("Вершина " + i + ":");
            System.out.println("  Позиция: " + vecStr.apply(vertex.position));
            System.out.println("  Нормаль: " + vecStr.apply(vertex.normal));

            if (vertex.normal != null) {
                float length = (float) Math.sqrt(
                        vertex.normal.getX() * vertex.normal.getX() +
                                vertex.normal.getY() * vertex.normal.getY() +
                                vertex.normal.getZ() * vertex.normal.getZ()
                );
                System.out.println("  Длина нормали: " + length);
            }
        }

        // Проверяем вершину 0 (общую)
        Vertex commonVertex = model.vertices.get(0);
        assertNotNull(commonVertex.normal, "Нормаль не должна быть null");

        float lengthSquared =
                commonVertex.normal.getX() * commonVertex.normal.getX() +
                        commonVertex.normal.getY() * commonVertex.normal.getY() +
                        commonVertex.normal.getZ() * commonVertex.normal.getZ();

        assertTrue(lengthSquared > 0.001f,
                "Длина нормали должна быть > 0. Получили: " + lengthSquared);
    }
    @Test
    public void testTriangleNormal() {
        Model model = new Model();

        // Простой треугольник в плоскости XY
        Vertex v1 = new Vertex();
        v1.position = new Vector3f(0, 0, 0);

        Vertex v2 = new Vertex();
        v2.position = new Vector3f(1, 0, 0);

        Vertex v3 = new Vertex();
        v3.position = new Vector3f(0, 1, 0);

        model.vertices.addAll(Arrays.asList(v1, v2, v3));

        // Один треугольник
        model.polygonsBoundaries.add(0);
        model.polygons.addAll(Arrays.asList(0, 1, 2));

        // Вычисляем нормали
        MyVertexNormalCalc calculator = new MyVertexNormalCalc();
        calculator.calculateVertexNormals(model);

        // Нормаль должна быть (0, 0, 1) или (0, 0, -1) в зависимости от порядка вершин
        Vector3f expected = new Vector3f(0, 0, 1);

        // Проверяем все вершины
        for (Vertex vertex : model.vertices) {
            assertNotNull(vertex.normal);
            assertEquals(expected.getX(), vertex.normal.getX(), 0.001f);
            assertEquals(expected.getY(), vertex.normal.getY(), 0.001f);
            assertEquals(expected.getZ(), vertex.normal.getZ(), 0.001f);
        }
    }

}

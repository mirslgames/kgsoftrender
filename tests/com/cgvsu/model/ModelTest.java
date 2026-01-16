package com.cgvsu.model;

import com.cgvsu.math.vectors.Vector2f;
import com.cgvsu.math.vectors.Vector3f;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ModelTextureTest {

    private static final float EPS = 1e-6f;

    @Test
    void testGetHasTextureVertexTrueWhenAllCornersHaveUv() {
        Model m = new Model();
        m.vertices = new ArrayList<>();
        m.polygons = new ArrayList<>();
        m.polygonsBoundaries = new ArrayList<>();
        m.polygonsTextureCoordinateIndices = new ArrayList<>();

        Vertex v0 = new Vertex(); v0.position = new Vector3f(0, 0, 0);
        Vertex v1 = new Vertex(); v1.position = new Vector3f(1, 0, 0);
        Vertex v2 = new Vertex(); v2.position = new Vector3f(0, 1, 0);

        v0.getOrAddTextureCoordinate(new Vector2f(0f, 0f));
        v1.getOrAddTextureCoordinate(new Vector2f(1f, 0f));
        v2.getOrAddTextureCoordinate(new Vector2f(0f, 1f));

        m.vertices.addAll(List.of(v0, v1, v2));

        m.polygonsBoundaries.add(0);
        m.polygons.addAll(List.of(0, 1, 2));
        m.polygonsTextureCoordinateIndices.addAll(List.of(0, 0, 0));

        assertTrue(m.getHasTextureVertex(),
                "Если у каждого угла полигона есть корректный UV-индекс, модель должна считаться имеющей текстурные координаты.");
    }

    @Test
    void testGetHasTextureVertexFalseWhenSomeCornerHasMinusOne() {
        Model m = new Model();
        m.vertices = new ArrayList<>();
        m.polygons = new ArrayList<>();
        m.polygonsBoundaries = new ArrayList<>();
        m.polygonsTextureCoordinateIndices = new ArrayList<>();

        Vertex v0 = new Vertex(); v0.position = new Vector3f(0, 0, 0);
        Vertex v1 = new Vertex(); v1.position = new Vector3f(1, 0, 0);
        Vertex v2 = new Vertex(); v2.position = new Vector3f(0, 1, 0);

        v0.getOrAddTextureCoordinate(new Vector2f(0f, 0f));
        v1.getOrAddTextureCoordinate(new Vector2f(1f, 0f));
        v2.getOrAddTextureCoordinate(new Vector2f(0f, 1f));

        m.vertices.addAll(List.of(v0, v1, v2));

        m.polygonsBoundaries.add(0);
        m.polygons.addAll(List.of(0, 1, 2));

        // Один угол "без UV"
        m.polygonsTextureCoordinateIndices.addAll(List.of(0, -1, 0));

        assertFalse(m.getHasTextureVertex(),
                "Если хотя бы один угол имеет UV-индекс < 0, модель должна считаться не имеющей корректных текстурных координат (частичный UV запрещён).");
    }

    @Test
    void testGetTextureCoordinateForPolygonVertexReturnsCorrectUv() {
        Model m = new Model();
        m.vertices = new ArrayList<>();
        m.polygons = new ArrayList<>();
        m.polygonsBoundaries = new ArrayList<>();
        m.polygonsTextureCoordinateIndices = new ArrayList<>();

        Vertex v0 = new Vertex(); v0.position = new Vector3f(0, 0, 0);
        Vertex v1 = new Vertex(); v1.position = new Vector3f(1, 0, 0);
        Vertex v2 = new Vertex(); v2.position = new Vector3f(0, 1, 0);

        v0.getOrAddTextureCoordinate(new Vector2f(0.1f, 0.2f));
        v1.getOrAddTextureCoordinate(new Vector2f(0.3f, 0.4f));
        v2.getOrAddTextureCoordinate(new Vector2f(0.5f, 0.6f));

        m.vertices.addAll(List.of(v0, v1, v2));

        m.polygonsBoundaries.add(0);
        m.polygons.addAll(List.of(0, 1, 2));
        m.polygonsTextureCoordinateIndices.addAll(List.of(0, 0, 0));

        Vector2f uvCorner1 = m.getTextureCoordinateForPolygonVertex(1);

        assertNotNull(uvCorner1,
                "Метод getTextureCoordinateForPolygonVertex должен вернуть UV, если индексы корректны и модель содержит UV.");

        assertEquals(0.3f, uvCorner1.getX(), EPS,
                "U у угла полигона (corner=1) должен соответствовать UV вершины v1.");

        assertEquals(0.4f, uvCorner1.getY(), EPS,
                "V у угла полигона (corner=1) должен соответствовать UV вершины v1.");
    }

    @Test
    void testUvSeamSameVertexTwoDifferentUvs() {
        Model m = new Model();
        m.vertices = new ArrayList<>();
        m.polygons = new ArrayList<>();
        m.polygonsBoundaries = new ArrayList<>();
        m.polygonsTextureCoordinateIndices = new ArrayList<>();

        //Вершины квадрата
        Vertex v0 = new Vertex(); v0.position = new Vector3f(0, 0, 0);
        Vertex v1 = new Vertex(); v1.position = new Vector3f(1, 0, 0);
        Vertex v2 = new Vertex(); v2.position = new Vector3f(1, 1, 0);
        Vertex v3 = new Vertex(); v3.position = new Vector3f(0, 1, 0);

        //Одна и та же геометрическая вершина v0 используется с двумя разными UV
        int v0uv0 = v0.getOrAddTextureCoordinate(new Vector2f(0f, 0f));
        int v0uv1 = v0.getOrAddTextureCoordinate(new Vector2f(0f, 0.5f));

        v1.getOrAddTextureCoordinate(new Vector2f(1f, 0f));
        v2.getOrAddTextureCoordinate(new Vector2f(1f, 1f));
        v3.getOrAddTextureCoordinate(new Vector2f(0f, 1f));

        m.vertices.addAll(List.of(v0, v1, v2, v3));

        //Треугольник 1: (0,1,2) использует для v0 UV=local 0
        m.polygonsBoundaries.add(0);
        m.polygons.addAll(List.of(0, 1, 2));
        m.polygonsTextureCoordinateIndices.addAll(List.of(v0uv0, 0, 0));

        //Треугольник 2: (0,2,3) использует для v0 UV=local 1
        m.polygonsBoundaries.add(3);
        m.polygons.addAll(List.of(0, 2, 3));
        m.polygonsTextureCoordinateIndices.addAll(List.of(v0uv1, 0, 0));

        assertTrue(m.getHasTextureVertex(),
                "Модель должна считаться текстурированной, если у каждого угла есть корректный локальный UV-индекс.");

        assertNotNull(v0.textureCoordinates,
                "У вершины v0 список textureCoordinates должен быть инициализирован.");

        assertEquals(2, v0.textureCoordinates.size(),
                "У вершины v0 должно быть 2 UV-варианта, так как она участвует в UV seam.");

        Vector2f uvFirstUse = m.getTextureCoordinateForPolygonVertex(0);
        Vector2f uvSecondUse = m.getTextureCoordinateForPolygonVertex(3);

        assertNotNull(uvFirstUse,
                "UV для первого использования вершины v0 должен быть не null.");

        assertNotNull(uvSecondUse,
                "UV для второго использования вершины v0 (на другом треугольнике) должен быть не null.");

        assertEquals(0f, uvFirstUse.getY(), EPS,
                "Первое использование вершины v0 должно иметь V=0.0 (local UV 0).");

        assertEquals(0.5f, uvSecondUse.getY(), EPS,
                "Второе использование вершины v0 должно иметь V=0.5 (local UV 1), что и демонстрирует UV seam.");
    }
}

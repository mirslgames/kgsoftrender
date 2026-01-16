package com.cgvsu.model;

import com.cgvsu.math.vectors.Vector2f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VertexTest {

    private static final float EPS = 1e-6f;

    @Test
    void testGetOrAddTextureCoordinateAddsFirstAndReturnsZero() {
        Vertex v = new Vertex();

        int idx = v.getOrAddTextureCoordinate(new Vector2f(0.1f, 0.2f));

        assertEquals(0, idx, "Первый добавленный UV должен получить локальный индекс 0.");

        assertNotNull(v.textureCoordinates,
                "Список textureCoordinates должен быть инициализирован и не должен быть null.");

        assertEquals(1, v.textureCoordinates.size(),
                "После добавления первого UV размер списка textureCoordinates должен быть равен 1.");

        assertEquals(0.1f, v.textureCoordinates.get(0).getX(), EPS,
                "U первой текстурной координаты должен совпадать с добавленным значением.");

        assertEquals(0.2f, v.textureCoordinates.get(0).getY(), EPS,
                "V первой текстурной координаты должен совпадать с добавленным значением.");
    }

    @Test
    void testGetOrAddTextureCoordinateSameUvReturnsSameIndex() {
        Vertex v = new Vertex();

        int idx1 = v.getOrAddTextureCoordinate(new Vector2f(0.5f, 0.5f));
        int idx2 = v.getOrAddTextureCoordinate(new Vector2f(0.5f, 0.5f));

        assertEquals(idx1, idx2,
                "Если UV одинаковый, метод должен вернуть тот же локальный индекс (без создания дубля).");

        assertNotNull(v.textureCoordinates,
                "Список textureCoordinates должен быть инициализирован и не должен быть null.");

        assertEquals(1, v.textureCoordinates.size(),
                "Если UV одинаковый, он не должен дублироваться внутри одной вершины.");
    }

    @Test
    void testGetTextureCoordinateOutOfRangeReturnsNull() {
        Vertex v = new Vertex();
        v.getOrAddTextureCoordinate(new Vector2f(0f, 0f));

        assertNull(v.getTextureCoordinate(-1),
                "При запросе UV по отрицательному индексу метод должен вернуть null.");

        assertNull(v.getTextureCoordinate(1),
                "При запросе UV по индексу, выходящему за границы списка, метод должен вернуть null.");
    }

    @Test
    void testGetOrAddTextureCoordinateNullReturnsMinusOneAndDoesNotAdd() {
        Vertex v = new Vertex();

        int idx = v.getOrAddTextureCoordinate(null);

        assertEquals(-1, idx,
                "Если передать null вместо UV, метод должен вернуть -1.");

        assertNotNull(v.textureCoordinates,
                "Список textureCoordinates должен быть инициализирован и не должен быть null.");

        assertEquals(0, v.textureCoordinates.size(),
                "Если передать null вместо UV, новый элемент не должен добавляться в список.");
    }
}

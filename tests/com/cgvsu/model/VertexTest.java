package com.cgvsu.model;

import com.cgvsu.math.vectors.Vector2f;
import com.cgvsu.math.vectors.Vector3f;
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

    @Test
    void testVertexDeepCopyCreatesIndependentCopy() {
        Vertex original = new Vertex();
        original.position = new Vector3f(1, 2, 3);
        original.normal = new Vector3f(0, 1, 0);

        original.getOrAddTextureCoordinate(new Vector2f(0.1f, 0.2f));
        original.getOrAddTextureCoordinate(new Vector2f(0.3f, 0.4f));

        Vertex copy = original.deepCopy();

        assertNotNull(copy, "Копия вершины не должна быть null.");
        assertNotSame(original, copy, "Копия вершины должна быть новым объектом, а не той же ссылкой.");

        assertNotNull(copy.position, "position в копии не должен быть null.");
        assertNotNull(copy.normal, "normal в копии не должен быть null.");
        assertNotSame(original.position, copy.position, "position должен быть скопирован глубоко");
        assertNotSame(original.normal, copy.normal, "normal должен быть скопирован глубоко");

        assertEquals(original.position.getX(), copy.position.getX(), EPS, "X координата position должна совпадать.");
        assertEquals(original.position.getY(), copy.position.getY(), EPS, "Y координата position должна совпадать.");
        assertEquals(original.position.getZ(), copy.position.getZ(), EPS, "Z координата position должна совпадать.");

        assertEquals(original.normal.getX(), copy.normal.getX(), EPS, "X координата normal должна совпадать.");
        assertEquals(original.normal.getY(), copy.normal.getY(), EPS, "Y координата normal должна совпадать.");
        assertEquals(original.normal.getZ(), copy.normal.getZ(), EPS, "Z координата normal должна совпадать.");

        assertNotNull(copy.textureCoordinates, "textureCoordinates в копии не должен быть null.");
        assertNotSame(original.textureCoordinates, copy.textureCoordinates,
                "Список textureCoordinates должен быть скопирован, а не разделяться по ссылке.");
        assertEquals(original.textureCoordinates.size(), copy.textureCoordinates.size(),
                "Размер textureCoordinates у оригинала и копии должен совпадать.");

        for (int i = 0; i < original.textureCoordinates.size(); i++) {
            Vector2f o = original.textureCoordinates.get(i);
            Vector2f c = copy.textureCoordinates.get(i);
            assertNotNull(c, "UV в копии не должен быть null.");
            assertNotSame(o, c, "UV-координата должна быть скопирована глубоко");

            assertEquals(o.getX(), c.getX(), EPS, "U координата UV должна совпадать.");
            assertEquals(o.getY(), c.getY(), EPS, "V координата UV должна совпадать.");
        }

        copy.textureCoordinates.add(new Vector2f(0.9f, 0.9f));
        assertEquals(2, original.textureCoordinates.size(),
                "При изменении textureCoordinates у копии, оригинал не должен изменяться.");
        assertEquals(3, copy.textureCoordinates.size(),
                "При добавлении UV в копию, размер списка в копии должен увеличиться.");
    }
}

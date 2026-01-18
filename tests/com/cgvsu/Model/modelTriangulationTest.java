package com.cgvsu.model;


import com.cgvsu.math.vectors.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Vertex;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.*;

import com.cgvsu.objreader.ObjReader;

import static org.junit.jupiter.api.Assertions.*;


public class modelTriangulationTest {
    //Тут написать тест с кубиком, насколько правильно он триангулируется с точки зрения модели, проверить нормали и
    // текстурные вершины
    @Test
    public void testSimpleObjParsing() throws Exception {

        String obj =
                "v 0 0 0\n" +
                        "v 1 0 0\n" +
                        "v 1 1 0\n" +
                        "v 0 1 0\n" +
                        "f 1 2 3 4\n";

        Model model = ObjReader.readModelFromFile(obj,"text_simple", new HashMap<>());
        System.out.println("=== Парсинг простого OBJ ===");
        System.out.println("Вершин: " + model.vertices.size()); // Должно быть 4
        System.out.println("polygons: " + model.polygons); // Должно быть [0, 1, 2, 3]
        System.out.println("polygons.size(): " + model.polygons.size()); // Должно быть 4
        System.out.println("polygonsBoundaries: " + model.polygonsBoundaries); // Должно быть [0]

        assertEquals(4, model.vertices.size());
        assertEquals(4, model.polygons.size());
        assertEquals(1, model.polygonsBoundaries.size());
        assertEquals(Arrays.asList(0, 1, 2, 3), model.polygons);
    }
    @Test
    public void testCubeTriangulation() throws Exception {
        String obj =
                "v -1 -1 -1\n" +  // 1
                        "v 1 -1 -1\n" +   // 2
                        "v 1 1 -1\n" +    // 3
                        "v -1 1 -1\n" +   // 4
                        "v -1 -1 1\n" +   // 5
                        "v 1 -1 1\n" +    // 6
                        "v 1 1 1\n" +     // 7
                        "v -1 1 1\n" +    // 8
                        "f 1 2 3 4\n" +   // задняя грань
                        "f 5 6 7 8\n" +   // передняя грань
                        "f 1 2 6 5\n" +   // нижняя грань
                        "f 3 4 8 7\n" +   // верхняя грань
                        "f 1 4 8 5\n" +   // левая грань
                        "f 2 3 7 6\n";
        Model cube = ObjReader.readModelFromFile(obj,"testCube", new HashMap<>());

        // 2. Сохраняем исходное состояние
        int originalVertexCount = cube.vertices.size();


        cube.triangulate();

        // ДЕБАГ: выведем что получилось
        System.out.println("После триангуляции:");
        System.out.println("polygonsBoundaries: " + cube.polygonsBoundaries);
        System.out.println("polygons size: " + cube.polygons.size());

        // ПРОВЕРЯЕМ ФАКТИЧЕСКИЙ РЕЗУЛЬТАТ
        // У нас должно быть 12 треугольников
        int triangleCount = cube.polygonsBoundaries.size();
        System.out.println("Фактическое количество треугольников: " + triangleCount);

        // Проверяем структуру
        for (int i = 0; i < triangleCount; i++) {
            int start = cube.polygonsBoundaries.get(i);
            int end = (i + 1 < triangleCount)
                    ? cube.polygonsBoundaries.get(i + 1)
                    : cube.polygons.size();

            // Каждый треугольник должен иметь ровно 3 вершины
            assertEquals(3, end - start,
                    "Треугольник " + i + " имеет неправильное количество вершин");
        }

        // Общее количество вершинных индексов
        assertEquals(36, cube.polygons.size()); // 12 треугольников × 3 вершины

        // ИЛИ ТАК: проверяем что polygonsBoundaries содержит индексы 0, 3, 6, 9, ... 33
        if (triangleCount == 12) {
            for (int i = 0; i < 12; i++) {
                assertEquals(i * 3, cube.polygonsBoundaries.get(i).intValue());
            }
        }
    }

    @Test
    public void testTriangulationPreservesTextureCoordinates() throws Exception {
        // 1. Загружаем модель
        String obj =
                "# comment\n" +
                        "v 0 0 0\n" +
                        "v 1 0 0\n" +
                        "v 0 1 0\n" +
                        "f 1 2 3\n";

        Model cube = ObjReader.readModelFromFile(obj,"src/test/resources/test_cube.obj", new HashMap<>());


        // Сохраняем маппинг "вершина -> текстурная координата" ДО триангуляции
        Map<Integer, List<Integer>> vertexToTextureBefore = new HashMap<>();
        for (int i = 0; i < cube.polygons.size(); i++) {
            int vertexIndex = cube.polygons.get(i);
            int textureIndex = cube.polygonsTextureCoordinateIndices.get(i);

            vertexToTextureBefore
                    .computeIfAbsent(vertexIndex, k -> new ArrayList<>())
                    .add(textureIndex);
        }


        cube.triangulate();

        // Проверяем, что после триангуляции маппинг сохранился
        for (int i = 0; i < cube.polygons.size(); i++) {
            int vertexIndex = cube.polygons.get(i);
            int textureIndex = cube.polygonsTextureCoordinateIndices.get(i);

            List<Integer> expectedTextures = vertexToTextureBefore.get(vertexIndex);
            assertNotNull(expectedTextures);
            assertTrue(expectedTextures.contains(textureIndex));
        }
    }

    @Test
    public void testTriangulationCreatesValidTriangles() throws Exception {
        // 1. Загружаем модель
        String obj =
                "# comment\n" +
                        "v 0 0 0\n" +
                        "v 1 0 0\n" +
                        "v 0 1 0\n" +
                        "f 1 2 3\n";
        String path = "/test_cube.obj";
        Model cube = ObjReader.readModelFromFile(obj,"src/test/resources/test_cube.obj", new HashMap<>());
        cube.triangulate();

        // Проверяем, что треугольники не вырождены (не лежат на одной прямой)
        for (int i = 0; i < cube.polygonsBoundaries.size(); i++) {
            int start = cube.polygonsBoundaries.get(i);

            // Получаем вершины треугольника
            int v1Idx = cube.polygons.get(start);
            int v2Idx = cube.polygons.get(start + 1);
            int v3Idx = cube.polygons.get(start + 2);

            Vertex v1 = cube.vertices.get(v1Idx);
            Vertex v2 = cube.vertices.get(v2Idx);
            Vertex v3 = cube.vertices.get(v3Idx);

            // Вычисляем нормаль треугольника
            Vector3f edge1 = new Vector3f(
                    v2.position.getX() - v1.position.getX(),
                    v2.position.getY() - v1.position.getY(),
                    v2.position.getZ() - v1.position.getZ()
            );
            Vector3f edge2 = new Vector3f(
                    v3.position.getX() - v1.position.getX(),
                    v3.position.getY() - v1.position.getY(),
                    v3.position.getZ() - v1.position.getZ()
            );

            // Векторное произведение
            Vector3f normal = new Vector3f(
                    edge1.getY() * edge2.getZ() - edge1.getZ() * edge2.getY(),
                    edge1.getZ() * edge2.getX() - edge1.getX() * edge2.getZ(),
                    edge1.getX() * edge2.getY() - edge1.getY() * edge2.getX()
            );

            // Длина нормали должна быть > 0 (не вырожденный треугольник)
            float length = (float) Math.sqrt(
                    normal.getX() * normal.getX() +
                            normal.getY() * normal.getY() +
                            normal.getZ() * normal.getZ()
            );

            assertTrue(length > 0.0001f, "Triangle " + i + " is degenerate");
        }
    }
}


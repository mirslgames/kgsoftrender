package com.cgvsu.integrations;

import com.cgvsu.math.vectors.Vector2f;
import com.cgvsu.math.vectors.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Transform;
import com.cgvsu.model.Vertex;
import com.cgvsu.modelOperations.Rasterization;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.cgvsu.modelOperations.Rasterization.interpolate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class testNormalsForLighting {
    public static Model createTestCube() {
        Model cube = new Model();
        cube.modelName = "TestCube";
        cube.currentTransform = new Transform(0,0,0,0,0,0,1,1,1);

        // Вершины куба (8 вершин)
        // Позиции
        Vector3f[] positions = {
                new Vector3f(-1, -1, -1), // 0: лево-низ-зад
                new Vector3f(1, -1, -1),  // 1: право-низ-зад
                new Vector3f(1, 1, -1),   // 2: право-верх-зад
                new Vector3f(-1, 1, -1),  // 3: лево-верх-зад
                new Vector3f(-1, -1, 1),  // 4: лево-низ-перед
                new Vector3f(1, -1, 1),   // 5: право-низ-перед
                new Vector3f(1, 1, 1),    // 6: право-верх-перед
                new Vector3f(-1, 1, 1)    // 7: лево-верх-перед
        };

        // Нормали для каждой грани (6 граней)
        Vector3f[] faceNormals = {
                new Vector3f(0, 0, -1),   // задняя грань
                new Vector3f(0, 0, 1),    // передняя грань
                new Vector3f(-1, 0, 0),   // левая грань
                new Vector3f(1, 0, 0),    // правая грань
                new Vector3f(0, -1, 0),   // нижняя грань
                new Vector3f(0, 1, 0)     // верхняя грань
        };

        // Текстурные координаты (простой UV квадрат)
        Vector2f[] texCoords = {
                new Vector2f(0, 0), // 0: лево-низ
                new Vector2f(1, 0), // 1: право-низ
                new Vector2f(1, 1), // 2: право-верх
                new Vector2f(0, 1)  // 3: лево-верх
        };

        // Индексы вершин для каждого треугольника (12 треугольников, 2 на грань)
        // Каждая грань: два треугольника
        int[][] triangles = {
                // Задняя грань (грань 0)
                {0, 1, 2}, {0, 2, 3},
                // Передняя грань (грань 1)
                {5, 4, 7}, {5, 7, 6},
                // Левая грань (грань 2)
                {4, 0, 3}, {4, 3, 7},
                // Правая грань (грань 3)
                {1, 5, 6}, {1, 6, 2},
                // Нижняя грань (грань 4)
                {4, 5, 1}, {4, 1, 0},
                // Верхняя грань (грань 5)
                {3, 2, 6}, {3, 6, 7}
        };

        // Нормали для каждой грани (индекс грани для каждого треугольника)
        int[] triangleToFace = {
                0, 0, // задняя грань (2 треугольника)
                1, 1, // передняя грань
                2, 2, // левая грань
                3, 3, // правая грань
                4, 4, // нижняя грань
                5, 5  // верхняя грань
        };

        // Текстурные координаты для каждого треугольника (индексы в texCoords)
        int[][] triangleTexCoords = {
                {0, 1, 2}, {0, 2, 3}, // задняя грань
                {1, 0, 3}, {1, 3, 2}, // передняя грань (зеркально)
                {0, 1, 2}, {0, 2, 3}, // левая грань
                {1, 0, 3}, {1, 3, 2}, // правая грань (зеркально)
                {0, 1, 2}, {0, 2, 3}, // нижняя грань
                {0, 1, 2}, {0, 2, 3}  // верхняя грань
        };

        // Создаём вершины
        for (Vector3f pos : positions) {
            Vertex vertex = new Vertex();
            vertex.position = pos;
            cube.vertices.add(vertex);
        }

        // Для каждой вершины создаём список нормалей (по одной на каждую грань, где используется вершина)
        // Сначала инициализируем списки нормалей для каждой вершины
        List<List<Vector3f>> vertexNormals = new ArrayList<>();
        for (int i = 0; i < cube.vertices.size(); i++) {
            vertexNormals.add(new ArrayList<>());
        }

        // Собираем нормали для каждой вершины
        for (int triIndex = 0; triIndex < triangles.length; triIndex++) {
            int faceIndex = triangleToFace[triIndex / 2]; // 2 треугольника на грань
            Vector3f faceNormal = faceNormals[faceIndex];

            int[] triVertices = triangles[triIndex];
            for (int i = 0; i < 3; i++) {
                int vertexIndex = triVertices[i];
                vertexNormals.get(vertexIndex).add(faceNormal);
            }
        }

        // Устанавливаем усреднённые нормали для вершин
        for (int i = 0; i < cube.vertices.size(); i++) {
            List<Vector3f> normals = vertexNormals.get(i);
            if (!normals.isEmpty()) {
                // Усредняем все нормали граней, которые используют эту вершину
                Vector3f avgNormal = new Vector3f(0, 0, 0);
                for (Vector3f n : normals) {
                    avgNormal = avgNormal.add(n);
                }
                cube.vertices.get(i).normal = avgNormal.normalize();
            }
        }

        // Создаём полигоны (треугольники)
        int polygonStartIndex = 0;

        for (int triIndex = 0; triIndex < triangles.length; triIndex++) {
            int[] triVertices = triangles[triIndex];
            int[] triTexIndices = triangleTexCoords[triIndex];

            // Добавляем границы полигонов
            cube.polygonsBoundaries.add(polygonStartIndex);

            // Добавляем вершины треугольника
            for (int i = 0; i < 3; i++) {
                int vertexIndex = triVertices[i];
                cube.polygons.add(vertexIndex);

                // Добавляем текстурные координаты
                cube.polygonsTextureCoordinateIndices.add(triTexIndices[i]);

                // Устанавливаем текстурные координаты для вершины
                Vertex vertex = cube.vertices.get(vertexIndex);
                vertex.textureCoordinates = new ArrayList<>();
                vertex.textureCoordinates.add(texCoords[triTexIndices[i]]);
            }

            polygonStartIndex += 3;
        }

        // Добавляем финальную границу
        cube.polygonsBoundaries.add(cube.polygons.size());

        // Устанавливаем флаг текстуры
        cube.hasTexture = true;

        return cube;
    }
    private Model loadTestModel() {
        // Загрузите простую тестовую модель (куб или сферу)
        // Или создайте програмно
        return createTestCube();
    }
    @Test
    public void testLightingAtTriangleCenter() {
        // Треугольник
        Vector3f v1 = new Vector3f(0, 0, 0);
        Vector3f v2 = new Vector3f(2, 0, 0);
        Vector3f v3 = new Vector3f(0, 2, 0);

        // Нормали (все смотрят вверх)
        Vector3f n1 = new Vector3f(0, 0, 1);
        Vector3f n2 = new Vector3f(0, 0, 1);
        Vector3f n3 = new Vector3f(0, 0, 1);
        // Центр треугольника
        float[] barycentric = new float[3];
        barycentric[0] = 1.0f/3;
        barycentric[1] = 1.0f/3;
        barycentric[2] = 1.0f/3;
        Vector3f position = Rasterization.interpolate(v1, v2, v3, barycentric);
        Vector3f normal = interpolate(n1, n2, n3, barycentric).normalize();

        // Камера сверху
        Vector3f cameraPos = new Vector3f(0, 0, 10);
        Vector3f lightDir = new Vector3f(0, 0, -1).normalize(); // Свет сверху

        // Вычисляем освещение
        float diffuse = Math.max(0, normal.dot(lightDir));

        // Нормаль смотрит вверх (0,0,1), свет сверху (0,0,-1)
        // Их скалярное произведение должно быть -1, но мы берём max(0, ...) = 0
        assertEquals(0.0f, diffuse, 0.001f,
                "При свете сверху и нормали вверх, диффузная составляющая должна быть 0");

        // Поворачиваем нормаль
        normal = new Vector3f(0, 0, -1).normalize(); // Смотрит вниз
        diffuse = Math.max(0, normal.dot(lightDir));

        // Теперь нормаль и свет в одном направлении
        assertEquals(1.0f, diffuse, 0.001f,
                "При совпадающих направлениях нормали и света, диффузная составляющая должна быть 1");
    }
    @Test
    public void testTriangleNormalConsistency() {
        // Загружаем модель и проверяем каждый полигон
        Model model = loadTestModel();

        for (int polyIndex = 0; polyIndex < model.polygonsBoundaries.size(); polyIndex++) {
            int start = model.polygonsBoundaries.get(polyIndex);
            int end = (polyIndex + 1 < model.polygonsBoundaries.size())
                    ? model.polygonsBoundaries.get(polyIndex + 1)
                    : model.polygons.size();

            if (end - start >= 3) { // Хотя бы треугольник
                // Получаем вершины треугольника
                List<Vector3f> positions = new ArrayList<>();
                List<Vector3f> normals = new ArrayList<>();

                for (int i = 0; i < 3; i++) {
                    int vertexIndex = model.polygons.get(start + i);
                    Vertex vertex = model.vertices.get(vertexIndex);
                    positions.add(vertex.position);
                    if (vertex.normal != null) {
                        normals.add(vertex.normal);
                    }
                }

                // Если есть все 3 нормали, проверяем их согласованность
                if (normals.size() == 3) {
                    // Вычисляем геометрическую нормаль
                    Vector3f geometricNormal = calculateGeometricNormal(
                            positions.get(0), positions.get(1), positions.get(2));

                    // Проверяем, что вершинные нормали примерно согласованы с геометрической
                    for (int i = 0; i < 3; i++) {
                        float dot = normals.get(i).dot(geometricNormal);
                        assertTrue(dot > 0.7f,
                                "Вершинная нормаль " + i + " в полигоне " + polyIndex +
                                        " должна быть согласована с геометрической. Dot: " + dot);
                    }
                }
            }
        }
    }
    private Vector3f calculateGeometricNormal(Vector3f v1, Vector3f v2, Vector3f v3) {
        Vector3f edge1 = v2.subbed(v1);
        Vector3f edge2 = v3.subbed(v1);
        return edge1.crossed(edge2).normalize();
    }



}

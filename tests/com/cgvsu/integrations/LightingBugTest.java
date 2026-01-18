package com.cgvsu.integrations;

import com.cgvsu.math.matrixs.Matrix4f;
import com.cgvsu.math.point.Point2f;
import com.cgvsu.math.vectors.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Vertex;
import com.cgvsu.modelOperations.MyVertexNormalCalc;
import com.cgvsu.modelOperations.Rasterization;
import com.cgvsu.modelOperations.TextureMapping;
import com.cgvsu.modelOperations.ZBuffer;
import com.cgvsu.objreader.ObjReader;
import com.cgvsu.render_engine.Camera;
import com.cgvsu.render_engine.GraphicConveyor;
import com.cgvsu.sceneview.SceneManager;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.util.HashMap;

import static com.cgvsu.modelOperations.Rasterization.*;
import static com.cgvsu.render_engine.GraphicConveyor.rotateScaleTranslate;
import static com.cgvsu.render_engine.GraphicConveyor.vertexToPoint;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LightingBugTest {


     @Test
     public void testNormalInterpolationConsistency() {
         // Создаем треугольник с "хорошими" нормалями
         Vertex v1 = new Vertex();
         v1.position = new Vector3f(0, 0, 0);
         v1.normal = new Vector3f(0, 0, 1).normalized();

         Vertex v2 = new Vertex();
         v2.position = new Vector3f(1, 0, 0);
         v2.normal = new Vector3f(0, 0, 1).normalized();

         Vertex v3 = new Vertex();
         v3.position = new Vector3f(0, 1, 0);
         v3.normal = new Vector3f(0, 0, 1).normalized();

         // Барицентрические координаты для центра треугольника
         float[] barycentric = {1.0f/3.0f, 1.0f/3.0f, 1.0f/3.0f};

         // Интерполируем нормаль
         Vector3f interpolatedNormal = Rasterization.interpolateNormalWithPerspective(
                 v1, v2, v3,
                 1.0f, 1.0f, 1.0f, // одинаковые z
                 barycentric
         );

         System.out.println("Интерполированная нормаль в центре: " +
                 String.format("(%.4f, %.4f, %.4f)",
                         interpolatedNormal.getX(),
                         interpolatedNormal.getY(),
                         interpolatedNormal.getZ()));

         // Нормаль должна быть нормализована
         float length = interpolatedNormal.len();
         System.out.println("Длина нормали: " + length);

         assertEquals(1.0f, length, 0.001f, "Нормаль должна быть нормализована");

         // Направление должно быть (0,0,1) ± погрешность
         assertEquals(0.0f, interpolatedNormal.getX(), 0.001f);
         assertEquals(0.0f, interpolatedNormal.getY(), 0.001f);
         assertEquals(1.0f, interpolatedNormal.getZ(), 0.001f);
     }

     @Test
     public void testNormalLengthAfterInterpolation() {
         // Тестируем интерполяцию нормалей в разных точках треугольника
         Vertex v1 = new Vertex();
         v1.position = new Vector3f(0, 0, 0);
         v1.normal = new Vector3f(0, 0, 1);

         Vertex v2 = new Vertex();
         v2.position = new Vector3f(1, 0, 0);
         v2.normal = new Vector3f(0, 1, 0); // Разные нормали!

         Vertex v3 = new Vertex();
         v3.position = new Vector3f(0, 1, 0);
         v3.normal = new Vector3f(1, 0, 0);

         // Тестируем несколько точек
         float[][] testPoints = {
                 {1.0f, 0.0f, 0.0f}, // вершина 1
                 {0.0f, 1.0f, 0.0f}, // вершина 2
                 {0.0f, 0.0f, 1.0f}, // вершина 3
                 {0.5f, 0.5f, 0.0f}, // середина ребра
                 {0.33f, 0.33f, 0.34f} // центр
         };

         for (float[] bary : testPoints) {
             Vector3f normal = Rasterization.interpolateNormalWithPerspective(
             v1, v2, v3,
                    1.0f, 1.0f, 1.0f,
                    bary
            );

            float length = normal.len();
            System.out.println(String.format(
                    "Барицентрики (%.2f, %.2f, %.2f): длина нормали = %.6f",
                    bary[0], bary[1], bary[2], length));

            // Нормаль должна быть нормализована (длина ≈ 1.0)
            assertEquals(1.0f, length, 0.01f,
                    String.format("Нормаль не нормализована в точке (%.2f, %.2f, %.2f)",
                            bary[0], bary[1], bary[2]));
        }
    }

     @Test
     public void testLightingCalculationEdgeCases() {
         // Проверяем освещение в проблемных случаях

         // 1. Нормаль, почти перпендикулярная направлению света
         Vector3f grazingNormal = new Vector3f(0.99f, 0, 0.01f).normalized();
         Vector3f cameraPos = new Vector3f(0, 0, 5);
         Vector3f worldPos = new Vector3f(0, 0, 0);

         Color baseColor = Color.WHITE;

         // Создаем камеру для теста
         Camera testCamera = new Camera(cameraPos, new Vector3f(0,0,0), 60, 1, 0.1f, 100);
         Camera originalCamera = SceneManager.activeCamera;
         SceneManager.activeCamera = testCamera;

         try {
             Color result = TextureMapping.getModifiedColorWithLighting(
                     grazingNormal, worldPos, baseColor, 1.0f);

             System.out.println("Цвет при grazing angle: " + result);

             // Цвет должен быть темным (но не черным)
             assertTrue(result.getRed() < 0.3f,
                     "При grazing angle цвет должен быть темным");
             assertTrue(result.getRed() > 0.0f,
                     "Но не черным!");

         } finally {
             SceneManager.activeCamera = originalCamera;
         }
     }

     @Test
     public void testDegenerateNormals() {
         // Проверяем обработку вырожденных нормалей

         Vertex v1 = new Vertex();
         v1.normal = new Vector3f(0, 0, 0); // Нулевая нормаль!

         Vertex v2 = new Vertex();
         v2.normal = new Vector3f(0, 0, 1);

         Vertex v3 = new Vertex();
         v3.normal = new Vector3f(0, 1, 0);

         float[] barycentric = {0.5f, 0.25f, 0.25f};

         Vector3f result = Rasterization.interpolateNormalWithPerspective(
                 v1, v2, v3,
                 1.0f, 1.0f, 1.0f,
                 barycentric
         );

         System.out.println("Результат с нулевой нормалью: " + result);

         // Должна вернуться дефолтная нормаль (0,1,0) или что-то ненулевое
         float length = result.len();
         assertTrue(length > 0.1f, "Результат не должен быть нулевым вектором");
     }
    @Test
    public void testZFightingIssue() {
        // Проверяем, не является ли проблема z-fighting'ом
        // (когда два полигона на одной глубине конкурируют за пиксель)

        Model model = new Model();

        // Два треугольника в одной плоскости (возможен z-fighting)
        Vertex v1 = new Vertex(); v1.position = new Vector3f(0,0,0);
        Vertex v2 = new Vertex(); v2.position = new Vector3f(1,0,0);
        Vertex v3 = new Vertex(); v3.position = new Vector3f(0,1,0);
        Vertex v4 = new Vertex(); v4.position = new Vector3f(1,0,0); // Та же точка что v2!

        // ... добавляем в модель ...

        // Рендерим и смотрим на границу
    }
    @Test
    public void testForBlackSpotsInTriangleCenters_FIXED() {
        // Этот тест специально ищет "чёрные точки" в центрах полигонов

        // 1. Создаём треугольник, который явно покажет проблему
        Vertex v1 = new Vertex();
        v1.position = new Vector3f(0, 0, 0);
        v1.normal = new Vector3f(-1, -1, 1).normalized();

        Vertex v2 = new Vertex();
        v2.position = new Vector3f(2, 0, 0);
        v2.normal = new Vector3f(1, -1, 1).normalized();

        Vertex v3 = new Vertex();
        v3.position = new Vector3f(0, 2, 0);
        v3.normal = new Vector3f(-1, 1, 1).normalized();

        // 2. Подготавливаем камеру
        Camera testCamera = new Camera(
                new Vector3f(1, 1, 5), // Камера смотрит примерно на центр
                new Vector3f(1, 1, 0),
                60, 1, 0.1f, 100
        );
        Camera original = SceneManager.activeCamera;
        SceneManager.activeCamera = testCamera;

        try {
            // 3. Получаем матрицу вида из камеры
            Matrix4f viewMatrix = testCamera.getViewMatrix();

            // 4. Преобразуем вершины в пространство камеры
            Vector3f v1View = viewMatrix.multiplyOnVector(v1.position);
            Vector3f v2View = viewMatrix.multiplyOnVector(v2.position);
            Vector3f v3View = viewMatrix.multiplyOnVector(v3.position);

            // 5. Получаем z-значения в пространстве камеры
            float z1 = v1View.getZ();
            float z2 = v2View.getZ();
            float z3 = v3View.getZ();

            System.out.println("Z в пространстве камеры:");
            System.out.println("  v1.z = " + z1);
            System.out.println("  v2.z = " + z2);
            System.out.println("  v3.z = " + z3);

            // 6. Тестируем несколько точек ВНУТРИ треугольника
            // Центр (барицентрики 1/3, 1/3, 1/3)
            float[] center = {1.0f/3.0f, 1.0f/3.0f, 1.0f/3.0f};

            // Ближе к центру
            float[] nearCenter = {0.4f, 0.3f, 0.3f};

            // Проверяем нормали в этих точках (используем правильные z-значения!)
            Vector3f normalCenter = Rasterization.interpolateNormalWithPerspective(
                    v1, v2, v3, z1, z2, z3, center);
            Vector3f normalNearCenter = Rasterization.interpolateNormalWithPerspective(
                    v1, v2, v3, z1, z2, z3, nearCenter);

            System.out.println("Нормаль в центре: " + normalCenter +
                    ", длина: " + normalCenter.len());
            System.out.println("Нормаль около центра: " + normalNearCenter +
                    ", длина: " + normalNearCenter.len());

            // 7. Проверяем ДЛИНУ нормали (главный индикатор!)
            assertTrue(normalCenter.len() > 0.95f,
                    "Нормаль в центре должна быть почти единичной длины. Фактически: " + normalCenter.len());
            assertTrue(normalNearCenter.len() > 0.95f,
                    "Нормаль около центра должна быть почти единичной длины. Фактически: " + normalNearCenter.len());

            // 8. Проверяем освещение в этих точках
            Vector3f centerPos = new Vector3f(
                    center[0] * v1.position.getX() + center[1] * v2.position.getX() + center[2] * v3.position.getX(),
                    center[0] * v1.position.getY() + center[1] * v2.position.getY() + center[2] * v3.position.getY(),
                    center[0] * v1.position.getZ() + center[1] * v2.position.getZ() + center[2] * v3.position.getZ()
            );

            Color centerColor = TextureMapping.getModifiedColorWithLighting(
                    normalCenter, centerPos, Color.WHITE, 1.0f);

            System.out.println("Цвет в центре: R=" + centerColor.getRed() +
                    ", G=" + centerColor.getGreen() + ", B=" + centerColor.getBlue());

            // 9. Цвет НЕ должен быть почти чёрным
            assertTrue(centerColor.getRed() > 0.1f,
                    "Красная компонента в центре должна быть > 0.1 (не чёрная). Фактически: " + centerColor.getRed());
            assertTrue(centerColor.getGreen() > 0.1f,
                    "Зелёная компонента в центре должна быть > 0.1 (не чёрная). Фактически: " + centerColor.getGreen());
            assertTrue(centerColor.getBlue() > 0.1f,
                    "Синяя компонента в центре должна быть > 0.1 (не чёрная). Фактически: " + centerColor.getBlue());

            // 10. Сравниваем с цветом в вершине (не должно быть РЕЗКОГО потемнения)
            Color vertexColor = TextureMapping.getModifiedColorWithLighting(
                    v1.normal, v1.position, Color.WHITE, 1.0f);

            System.out.println("Цвет в вершине: R=" + vertexColor.getRed() +
                    ", G=" + vertexColor.getGreen() + ", B=" + vertexColor.getBlue());

            float centerBrightness = (float)(centerColor.getRed() + centerColor.getGreen() + centerColor.getBlue()) / 3.0f;
            float vertexBrightness = (float)(vertexColor.getRed() + vertexColor.getGreen() + vertexColor.getBlue()) / 3.0f;

            System.out.println("Яркость в центре: " + centerBrightness);
            System.out.println("Яркость в вершине: " + vertexBrightness);
            System.out.println("Разница: " + Math.abs(centerBrightness - vertexBrightness));

            // Разница в яркости не должна быть слишком большой
            assertTrue(Math.abs(centerBrightness - vertexBrightness) < 0.3f,
                    "Слишком большая разница в яркости между центром и вершиной: " +
                            Math.abs(centerBrightness - vertexBrightness));

        } finally {
            SceneManager.activeCamera = original;
        }
    }
    @Test
    public void testViewMatrixOrientation() {
        Camera camera = new Camera(
                new Vector3f(0, 0, 5),   // Позиция камеры
                new Vector3f(0, 0, 0),   // Направление взгляда (target)
                60, 1, 0.1f, 100
        );

        Matrix4f viewMatrix = camera.getViewMatrix();

        // Проверим преобразование точки перед камерой
        Vector3f pointInFront = new Vector3f(0, 0, 0); // Прямо перед камерой
        Vector3f viewPos = viewMatrix.multiplyOnVector(pointInFront);

        System.out.println("Точка (0,0,0) в view space: " + viewPos);
        System.out.println("z=" + viewPos.getZ());

        // В правильной системе: точка перед камерой → z < 0
        // Если z > 0, значит система инвертирована

        // Также проверьте направляющие векторы камеры
        System.out.println("\nПараметры камеры:");
        System.out.println("Позиция: " + camera.getPosition());
        System.out.println("Target: " + camera.getTarget());
        System.out.println("Up вектор: " + camera.getCameraUp());

        // Вычислите направление вручную
        Vector3f direction = camera.getTarget().subbed(camera.getPosition()).normalized();
        System.out.println("Направление (target - position): " + direction);
    }
    @Test
    public void testProjectionMatrix() {
        Camera camera = new Camera(
                new Vector3f(0, 0, 5),
                new Vector3f(0, 0, 0),
                60, 1, 0.1f, 100
        );

        Matrix4f projMatrix = camera.getProjectionMatrix();

        // Точки на near и far plane
        Vector3f nearPoint = new Vector3f(0, 0, -camera.getNearPlane()); // В view space
        Vector3f farPoint = new Vector3f(0, 0, -camera.getFarPlane());   // В view space

        Vector3f projNear = projMatrix.multiplyOnVector(nearPoint);
        Vector3f projFar = projMatrix.multiplyOnVector(farPoint);

        System.out.println("Near plane в projection space: " + projNear + ", z=" + projNear.getZ());
        System.out.println("Far plane в projection space: " + projFar + ", z=" + projFar.getZ());

        // Обычно после проекции:
        // - near plane → z = -1 или 0
        // - far plane → z = 1
    }
    @Test
    public void testLightingInCenterDirectly() {
        // Камера в (1,1,5)
        Camera camera = new Camera(
                new Vector3f(1, 1, 5),
                new Vector3f(1, 1, 0),
                60, 1, 0.1f, 100
        );

        Camera original = SceneManager.activeCamera;
        SceneManager.activeCamera = camera;

        try {
            // Нормаль в центре треугольника из вашего теста
            // Вы сказали нормаль в центре: (-0.3, -0.3, 0.9)
            Vector3f centerNormal = new Vector3f(-0.3f, -0.3f, 0.9f).normalized();

            // Позиция в центре треугольника (1,1,0)?
            Vector3f centerPos = new Vector3f(2.0f/3.0f, 2.0f/3.0f, 0);

            // Вычисляем луч от точки к камере
            Vector3f ray = camera.getPosition().subbed(centerPos).normalized();
            System.out.println("Луч от центра к камере: " + ray);

            // Ваш расчёт освещения
            float l = centerNormal.dot(ray);
            System.out.println("l = n·ray = " + l);

            // Проверяем формулу
            float k = 1.0f;
            float intensity = (1f - k) + k * l;
            System.out.println("Интенсивность: " + intensity);

            // Должно быть ярко (l ≈ 0.9)
            assertTrue(l > 0.8f, "l должно быть большим: " + l);
            assertTrue(intensity > 0.8f, "Интенсивность должна быть большой: " + intensity);

        } finally {
            SceneManager.activeCamera = original;
        }
    }
    @Test
    public void testDarkSpotAnalysis() {
        // Камера
        Camera camera = new Camera(
                new Vector3f(1, 1, 5),
                new Vector3f(1, 1, 0),
                60, 1, 0.1f, 100
        );

        Camera original = SceneManager.activeCamera;
        SceneManager.activeCamera = camera;

        try {
            // Предположительные данные из тёмных точек
            System.out.println("=== АНАЛИЗ ТЁМНЫХ ТОЧЕК ===");

            // Возможная нормаль в тёмной точке
            // Если нормаль направлена ОТ камеры или перпендикулярно
            Vector3f[] problematicNormals = {
                    new Vector3f(0, 0, -1),   // От камеры
                    new Vector3f(0, 1, 0),    // Перпендикулярно
                    new Vector3f(1, 0, 0),    // Перпендикулярно
                    new Vector3f(0.3f, 0.3f, -0.9f).normalized(),  // Почти от камеры
            };

            Vector3f centerPos = new Vector3f(1, 1, 0);

            for (int i = 0; i < problematicNormals.length; i++) {
                System.out.println("\n--- Тест нормали " + i + ": " + problematicNormals[i] + " ---");

                Color color = TextureMapping.getModifiedColorWithLighting(
                        problematicNormals[i], centerPos, Color.WHITE, 1.0f);

                float brightness = (float) ((color.getRed() + color.getGreen() + color.getBlue()) / 3.0f);
                System.out.println("Итоговая яркость: " + brightness);

                if (brightness < 0.1f) {
                    System.out.println("✅ ЭТА нормаль даёт тёмную точку (как и должно быть)");
                } else {
                    System.out.println("❌ Эта нормаль должна давать тёмную точку, но даёт яркость: " + brightness);
                }
            }

        } finally {
            SceneManager.activeCamera = original;
        }
    }
    @Test
    public void testSimpleModelNormals() {
        // Создаём простую модель вручную
        Model testModel = new Model();

        // Создаём 4 вершины для квадрата
        for (int i = 0; i < 4; i++) {
            Vertex v = new Vertex();
            testModel.vertices.add(v);
        }

        // Вершины квадрата в плоскости XY
        testModel.vertices.get(0).position = new Vector3f(0, 0, 0);
        testModel.vertices.get(1).position = new Vector3f(1, 0, 0);
        testModel.vertices.get(2).position = new Vector3f(1, 1, 0);
        testModel.vertices.get(3).position = new Vector3f(0, 1, 0);

        // Добавляем 2 треугольника для квадрата
        // Треугольник 1: (0, 1, 2) - CCW
        testModel.polygons.add(0);
        testModel.polygons.add(1);
        testModel.polygons.add(2);
        testModel.polygonsBoundaries.add(0);

        // Треугольник 2: (0, 2, 3) - CCW
        testModel.polygons.add(0);
        testModel.polygons.add(2);
        testModel.polygons.add(3);
        testModel.polygonsBoundaries.add(3);

        // Вычисляем нормали
        MyVertexNormalCalc calculator = new MyVertexNormalCalc();
        calculator.calculateVertexNormals(testModel);

        System.out.println("=== ПРОСТАЯ МОДЕЛЬ КВАДРАТА ===");
        System.out.println("Позиции вершин:");
        for (int i = 0; i < testModel.vertices.size(); i++) {
            Vertex v = testModel.vertices.get(i);
            System.out.println("  v" + i + ": " + v.position);
        }

        System.out.println("\nВычисленные нормали:");
        for (int i = 0; i < testModel.vertices.size(); i++) {
            Vertex v = testModel.vertices.get(i);
            System.out.println("  v" + i + ": " + v.normal +
                    " (длина: " + v.normal.len() + ")");

            // Нормаль должна быть (0,0,1) или (0,0,-1)
            // Проверьте ЗНАК Z-компоненты!
            if (v.normal.getZ() > 0) {
                System.out.println("    ✅ Направлена ВВЕРХ (к камере, если камера сверху)");
            } else if (v.normal.getZ() < 0) {
                System.out.println("    ⚠️  Направлена ВНИЗ (от камеры, если камера сверху)");
            }
        }

        // Проверим с камерой
        Camera camera = new Camera(
                new Vector3f(0.5f, 0.5f, 3), // Камера сверху
                new Vector3f(0.5f, 0.5f, 0),
                60, 1, 0.1f, 100
        );

        Camera original = SceneManager.activeCamera;
        SceneManager.activeCamera = camera;

        try {
            System.out.println("\n=== ПРОВЕРКА ОСВЕЩЕНИЯ ===");
            System.out.println("Камера в: " + camera.getPosition());

            // Центр квадрата
            Vector3f centerPos = new Vector3f(0.5f, 0.5f, 0);
            Vector3f centerNormal = new Vector3f(0, 0, 1); // Предполагаемая нормаль

            // Проверим освещение
            Color color = TextureMapping.getModifiedColorWithLighting(
                    centerNormal, centerPos, Color.WHITE, 1.0f);

            float brightness = (float) ((color.getRed() + color.getGreen() + color.getBlue()) / 3.0f);
            System.out.println("Яркость в центре при нормали (0,0,1): " + brightness);

            if (brightness < 0.1f) {
                System.out.println("❌ Нормаль (0,0,1) даёт тёмную точку!");
                System.out.println("   Это значит камера смотрит не сверху!");
            }

        } finally {
            SceneManager.activeCamera = original;
        }
    }
    @Test
    public void reproduceRealRendererIssue() {
        // Воспроизводим условия реального рендерера

        // 1. Треугольник как в вашем тесте
        Vertex v1 = new Vertex();
        v1.position = new Vector3f(0, 0, 0);
        v1.normal = new Vector3f(-1, -1, 1).normalized();

        Vertex v2 = new Vertex();
        v2.position = new Vector3f(2, 0, 0);
        v2.normal = new Vector3f(1, -1, 1).normalized();

        Vertex v3 = new Vertex();
        v3.position = new Vector3f(0, 2, 0);
        v3.normal = new Vector3f(-1, 1, 1).normalized();

        // 2. Камера как в тесте
        Camera camera = new Camera(
                new Vector3f(1, 1, 5),
                new Vector3f(1, 1, 0),
                60, 800, 600, 0.1f
        );

        SceneManager.activeCamera = camera;

        // 3. Преобразуем как в реальном рендерере
        Matrix4f modelMatrix = new Matrix4f(); // Единичная
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f modelViewMatrix = viewMatrix.copy(); // view * model

        System.out.println("=== РЕАЛЬНЫЕ ПРЕОБРАЗОВАНИЯ ===");

        for (int i = 0; i < 3; i++) {
            Vertex v = (i == 0) ? v1 : (i == 1) ? v2 : v3;

            // Мировая позиция
            Vector3f worldPos = modelMatrix.multiplyOnVector(v.position);
            System.out.println("\nВершина " + i + ":");
            System.out.println("  Исходная позиция: " + v.position);
            System.out.println("  Мировая позиция: " + worldPos);
            System.out.println("  Исходная нормаль: " + v.normal);

            // View позиция
            Vector3f viewPos = modelViewMatrix.multiplyOnVector(v.position);
            System.out.println("  View позиция: " + viewPos);
            System.out.println("  zView: " + viewPos.getZ());

            // Проекция
            Matrix4f projMatrix = camera.getProjectionMatrix();
            Vector3f projPos = projMatrix.multiplyOnVector(viewPos);
            System.out.println("  Projection позиция: " + projPos);
            System.out.println("  zProj: " + projPos.getZ());

            // Преобразование нормали
            Vector3f worldNormal = modelMatrix.multiplyOnVector(v.normal);
            worldNormal.normalize();
            System.out.println("  Мировая нормаль: " + worldNormal);

            // Луч к камере
            Vector3f ray = camera.getPosition().subbed(worldPos).normalized();
            float l = worldNormal.dot(ray);
            System.out.println("  ray к камере: " + ray);
            System.out.println("  l = " + l);
            System.out.println("  Угол: " + Math.toDegrees(Math.acos(Math.max(-1, Math.min(1, l)))) + "°");
        }

        // 4. Центр треугольника
        System.out.println("\n=== ЦЕНТР ТРЕУГОЛЬНИКА ===");
        float[] center = {1.0f/3.0f, 1.0f/3.0f, 1.0f/3.0f};

        // Интерполируем вручную
        Vector3f centerWorldPos = new Vector3f(
                center[0] * v1.position.getX() + center[1] * v2.position.getX() + center[2] * v3.position.getX(),
                center[0] * v1.position.getY() + center[1] * v2.position.getY() + center[2] * v3.position.getY(),
                center[0] * v1.position.getZ() + center[1] * v2.position.getZ() + center[2] * v3.position.getZ()
        );

        Vector3f centerNormal = new Vector3f(
                center[0] * v1.normal.getX() + center[1] * v2.normal.getX() + center[2] * v3.normal.getX(),
                center[0] * v1.normal.getY() + center[1] * v2.normal.getY() + center[2] * v3.normal.getY(),
                center[0] * v1.normal.getZ() + center[1] * v2.normal.getZ() + center[2] * v3.normal.getZ()
        ).normalized();

        System.out.println("Центр мира: " + centerWorldPos);
        System.out.println("Центр нормаль: " + centerNormal);

        // Освещение
        Color color = TextureMapping.getModifiedColorWithLighting(
                centerNormal, centerWorldPos, Color.WHITE, 1.0f);

        float brightness = (float) ((color.getRed() + color.getGreen() + color.getBlue()) / 3.0f);
        System.out.println("Яркость в центре: " + brightness);

        // Вывод
        if (brightness < 0.1f) {
            System.out.println("\n❌ ПРОБЛЕМА ВОСПРОИЗВЕДЕНА: Центр тёмный!");
            System.out.println("Нужно посмотреть векторные данные выше.");
        } else {
            System.out.println("\n✅ Центр яркий - проблема не воспроизводится в этом тесте.");
            System.out.println("Значит проблема в чём-то другом в рендерере.");
        }
    }
    @Test
    public void testVertexToPointMethod() {
        // Ваш метод преобразования из projection space в экранные координаты
        // Это может быть источником проблемы!

        System.out.println("=== ТЕСТ METODA VERTEXTOPOINT ===");

        // Размеры экрана
        int width = 800;
        int height = 600;

        // Тестовые точки в NDC (Normalized Device Coordinates)
        // Обычно NDC: x,y в [-1,1], z в [0,1] или [-1,1]

        Vector3f[] testPoints = {
                new Vector3f(0, 0, 0.5f),     // Центр
                new Vector3f(1, 1, 0.5f),     // Правый верх
                new Vector3f(-1, -1, 0.5f),   // Левый нижний
                new Vector3f(0.5f, 0, 0.5f),  // Право от центра
                new Vector3f(0, 0.5f, 0.5f),  // Выше центра
        };

        for (int i = 0; i < testPoints.length; i++) {
            Vector3f ndc = testPoints[i];
            Point2f screen = vertexToPoint(ndc, width, height);

            System.out.println("NDC " + ndc + " → Screen " + screen);

            // Проверяем границы
            if (screen.getX() < 0 || screen.getX() >= width ||
                    screen.getY() < 0 || screen.getY() >= height) {
                System.out.println("  ⚠️  Выход за границы экрана!");
            }
        }

        // Особенно важно: что происходит с z?
        System.out.println("\n=== ПРОВЕРКА Z ===");
        Vector3f pointWithZ = new Vector3f(0, 0, 0.5f);
        Point2f screenPoint = vertexToPoint(pointWithZ, width, height);
        System.out.println("Точка с z=0.5: " + pointWithZ + " → " + screenPoint);
    }
    @Test
    public void compareTestVsRenderer() {
        // Те же данные что в тесте
        Vertex v1 = new Vertex();
        v1.position = new Vector3f(0, 0, 0);
        v1.normal = new Vector3f(-1, -1, 1).normalized();

        Vertex v2 = new Vertex();
        v2.position = new Vector3f(2, 0, 0);
        v2.normal = new Vector3f(1, -1, 1).normalized();

        Vertex v3 = new Vertex();
        v3.position = new Vector3f(0, 2, 0);
        v3.normal = new Vector3f(-1, 1, 1).normalized();

        float aspectRatio = 800.0f / 600.0f;
        Camera camera = new Camera(
                new Vector3f(1, 1, 5),
                new Vector3f(1, 1, 0),
                60, aspectRatio, 0.1f, 100
        );

        SceneManager.activeCamera = camera;

        // 1. Как делает ТЕСТ:
        System.out.println("=== КАК ДЕЛАЕТ ТЕСТ ===");

        Matrix4f modelMatrix = rotateScaleTranslate(1, 1, 1, 0, 0, 0, 0, 0, 0);
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f modelViewMatrix = new Matrix4f(viewMatrix.getMatrix());
        modelViewMatrix.multiply(modelMatrix);

        // Центр
        float[] center = {1.0f/3.0f, 1.0f/3.0f, 1.0f/3.0f};
        Vector3f centerWorldPos = new Vector3f(
                center[0] * v1.position.getX() + center[1] * v2.position.getX() + center[2] * v3.position.getX(),
                center[0] * v1.position.getY() + center[1] * v2.position.getY() + center[2] * v3.position.getY(),
                center[0] * v1.position.getZ() + center[1] * v2.position.getZ() + center[2] * v3.position.getZ()
        );

        // Простая интерполяция нормали
        Vector3f centerNormal = new Vector3f(
                center[0] * v1.normal.getX() + center[1] * v2.normal.getX() + center[2] * v3.normal.getX(),
                center[0] * v1.normal.getY() + center[1] * v2.normal.getY() + center[2] * v3.normal.getY(),
                center[0] * v1.normal.getZ() + center[1] * v2.normal.getZ() + center[2] * v3.normal.getZ()
        ).normalized();

        Color testColor = TextureMapping.getModifiedColorWithLighting(
                centerNormal, centerWorldPos, Color.WHITE, 1.0f);
        float testBrightness = (float) ((testColor.getRed() + testColor.getGreen() + testColor.getBlue()) / 3.0f);

        System.out.println("Центр мира: " + centerWorldPos);
        System.out.println("Нормаль центра: " + centerNormal);
        System.out.println("Яркость: " + testBrightness);

        // 2. Как делает РЕНДЕРЕР:
        System.out.println("\n=== КАК ДЕЛАЕТ РЕНДЕРЕР (симулируем) ===");

        // Получаем view позиции
        Vector3f v1View = modelViewMatrix.multiplyOnVector(v1.position);
        Vector3f v2View = modelViewMatrix.multiplyOnVector(v2.position);
        Vector3f v3View = modelViewMatrix.multiplyOnVector(v3.position);

        float zView1 = v1View.getZ();
        float zView2 = v2View.getZ();
        float zView3 = v3View.getZ();

        System.out.println("zView: " + zView1 + ", " + zView2 + ", " + zView3);

        // Перспективно-корректная интерполяция нормали
        Vector3f rendererNormal = Rasterization.interpolateNormalWithPerspective(
                v1, v2, v3, zView1, zView2, zView3, center);

        System.out.println("Нормаль (перспективно-корр.): " + rendererNormal);

        // Преобразуем нормаль модельной матрицей
        Vector3f worldNormalRenderer = modelMatrix.multiplyOnVector(rendererNormal);
        worldNormalRenderer.normalize();

        System.out.println("Мировая нормаль: " + worldNormalRenderer);

        Color rendererColor = TextureMapping.getModifiedColorWithLighting(
                worldNormalRenderer, centerWorldPos, Color.WHITE, 1.0f);
        float rendererBrightness = (float) ((rendererColor.getRed() + rendererColor.getGreen() + rendererColor.getBlue()) / 3.0f);

        System.out.println("Яркость: " + rendererBrightness);

        // Сравнение
        System.out.println("\n=== СРАВНЕНИЕ ===");
        System.out.println("Тест яркость: " + testBrightness);
        System.out.println("Рендерер яркость: " + rendererBrightness);
        System.out.println("Разница: " + Math.abs(testBrightness - rendererBrightness));

        // 3. Проверим что передаётся в растеризацию
        System.out.println("\n=== ЧТО ПЕРЕДАЁТСЯ В RASTERIZETRIANGLEWITHWORLDPOS ===");

        // Проекционные координаты
        Matrix4f projMatrix = camera.getProjectionMatrix();
        Vector3f v1Proj = projMatrix.multiplyOnVector(v1View);
        Vector3f v2Proj = projMatrix.multiplyOnVector(v2View);
        Vector3f v3Proj = projMatrix.multiplyOnVector(v3View);

        System.out.println("Projection координаты:");
        System.out.println("  v1: " + v1Proj + " (zProj=" + v1Proj.getZ() + ")");
        System.out.println("  v2: " + v2Proj + " (zProj=" + v2Proj.getZ() + ")");
        System.out.println("  v3: " + v3Proj + " (zProj=" + v3Proj.getZ() + ")");

        // Экранные координаты (ваш метод vertexToPoint)
        System.out.println("\nЭкранные координаты (через vertexToPoint):");
        System.out.println("  v1: " + vertexToPoint(v1Proj, 800, 600));
        System.out.println("  v2: " + vertexToPoint(v2Proj, 800, 600));
        System.out.println("  v3: " + vertexToPoint(v3Proj, 800, 600));
    }



}




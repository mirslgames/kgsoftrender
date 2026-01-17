package com.cgvsu.modelOperations;

import com.cgvsu.math.matrixs.Matrix4f;
import com.cgvsu.math.vectors.Vector2f;
import com.cgvsu.math.vectors.Vector3f;
import com.cgvsu.model.Vertex;
import com.cgvsu.render_engine.Camera;
import com.cgvsu.sceneview.SceneManager;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TextureMappingTest {
    @Test
    public void testGetTextureColor() {
        // Создаем простую текстуру 2x2 пикселя
        int width = 2;
        int height = 2;
        WritableImage texture = new WritableImage(width, height);
        PixelWriter writer = texture.getPixelWriter();

        // Заполняем разными цветами
        writer.setColor(0, 0, Color.RED);
        writer.setColor(1, 0, Color.GREEN);
        writer.setColor(0, 1, Color.BLUE);
        writer.setColor(1, 1, Color.YELLOW);

        // Тестируем углы текстуры
        Color color00 = TextureMapping.getTextureColor(texture, new Vector2f(0.0f, 0.0f));
        assertEquals(Color.RED, color00);

        Color color10 = TextureMapping.getTextureColor(texture, new Vector2f(1.0f, 0.0f));
        assertEquals(Color.GREEN, color10);

        Color color01 = TextureMapping.getTextureColor(texture, new Vector2f(0.0f, 1.0f));
        assertEquals(Color.BLUE, color01);

        Color color11 = TextureMapping.getTextureColor(texture, new Vector2f(1.0f, 1.0f));
        assertEquals(Color.YELLOW, color11);

        // Тестируем середину (интерполяция)
        Color colorMid = TextureMapping.getTextureColor(texture, new Vector2f(0.5f, 0.5f));
        // Середина между всеми 4 цветами
        assertNotNull(colorMid);
    }

    @Test
    public void testTextureClamping() {
        // Создаем простую текстуру
        WritableImage texture = new WritableImage(1, 1);
        texture.getPixelWriter().setColor(0, 0, Color.RED);

        // Координаты за пределами [0,1] должны обрабатываться (clamp или repeat)
        Color color = TextureMapping.getTextureColor(texture, new Vector2f(1.5f, -0.5f));
        assertNotNull(color);

        // В зависимости от реализации, может возвращаться RED (clamp) или что-то другое
    }
//
//    @Test
//    public void testLightingCalculation() {
//        // Тестируем простой расчет освещения
//        Vector3f normal = new Vector3f(0, 0, 1); // Смотрит прямо на камеру
//        Vector3f worldPosition = new Vector3f(0, 0, 0);
//        Color baseColor = Color.WHITE;
//        float lightIntensity = 1.0f;
//
//        Color result = TextureMapping.getModifiedColorWithLighting(
//                normal, worldPosition, baseColor, lightIntensity);
//
//        assertNotNull(result);
//        // Нормаль направлена прямо на свет → цвет должен быть светлее
//        // (зависит от реализации освещения)
//    }


    @Test
    public void testLightingWithDiffuseOnly_CameraLight() {
        // В вашей модели источник света В КАМЕРЕ!
        Camera camera = new Camera(
                new Vector3f(0, 0, 10), // Камера сзади по Z = источник света
                new Vector3f(0, 0, 0),  // Смотрит в начало координат
                60, 1, 0.1f, 100
        );

        Camera originalCamera = SceneManager.activeCamera;
        SceneManager.activeCamera = camera;

        try {
            Vector3f position = new Vector3f(0, 0, 0);
            Color baseColor = Color.WHITE;

            System.out.println("\nТест диффузного освещения (источник в камере):");
            System.out.println("Позиция камеры/источника: " + camera.getPosition());

            // Луч от точки к камере: (0, 0, 10) - (0, 0, 0) = (0, 0, 10) -> нормализовать
            Vector3f rayToCamera = new Vector3f(0, 0, 1).normalized(); // Камера на +Z

            // 1. Нормаль направлена К камере (0, 0, 1) - максимальная яркость
            Vector3f normalToCamera = new Vector3f(0, 0, 1);
            Color colorToCamera = TextureMapping.getModifiedColorWithLighting(
                    normalToCamera, position, baseColor, 1.0f);
            float brightToCamera = (float)((colorToCamera.getRed() +
                    colorToCamera.getGreen() +
                    colorToCamera.getBlue()) / 3.0f);
            System.out.println("Нормаль (0,0,1) к камере: яркость = " + brightToCamera);

            // 2. Нормаль под углом 45° к лучу (0, 0.707, 0.707)
            Vector3f normal45deg = new Vector3f(0, 0.707f, 0.707f).normalized();
            Color color45deg = TextureMapping.getModifiedColorWithLighting(
                    normal45deg, position, baseColor, 1.0f);
            float bright45deg = (float)((color45deg.getRed() +
                    color45deg.getGreen() +
                    color45deg.getBlue()) / 3.0f);
            System.out.println("Нормаль под 45° (0,0.707,0.707): яркость = " + bright45deg);

            // 3. Нормаль перпендикулярно лучу (0, 1, 0)
            Vector3f normalPerpendicular = new Vector3f(0, 1, 0);
            Color colorPerpendicular = TextureMapping.getModifiedColorWithLighting(
                    normalPerpendicular, position, baseColor, 1.0f);
            float brightPerpendicular = (float)((colorPerpendicular.getRed() +
                    colorPerpendicular.getGreen() +
                    colorPerpendicular.getBlue()) / 3.0f);
            System.out.println("Нормаль перпендикулярно (0,1,0): яркость = " + brightPerpendicular);

            // 4. Нормаль ОТ камеры (0, 0, -1)
            Vector3f normalAway = new Vector3f(0, 0, -1);
            Color colorAway = TextureMapping.getModifiedColorWithLighting(
                    normalAway, position, baseColor, 1.0f);
            float brightAway = (float)((colorAway.getRed() +
                    colorAway.getGreen() +
                    colorAway.getBlue()) / 3.0f);
            System.out.println("Нормаль от камеры (0,0,-1): яркость = " + brightAway);

            // Проверяем логику вашей модели:
            // l = -n·ray, где ray направлен от точки к камере

            // Для normalToCamera (0,0,1): n·ray = (0,0,1)·(0,0,1) = 1, l = -1 = -1 → обнуляется до 0?!
            // Ой! В вашей формуле l = -n·ray, но если n = ray, то n·ray = 1, l = -1 < 0 → l = 0!
            // Это ошибка в формуле или в моём понимании?

            // Проверяем фактическое поведение:
            assertTrue(brightToCamera > 0.9f,
                    "Нормаль к камере должна быть яркой: " + brightToCamera);

            assertTrue(bright45deg > 0.3f && bright45deg < 0.8f,
                    "Под 45° должна быть средняя яркость: " + bright45deg);

            assertEquals(0.0f, brightPerpendicular, 0.01f,
                    "Перпендикулярно лучу должно быть темно: " + brightPerpendicular);

            assertEquals(0.0f, brightAway, 0.01f,
                    "От камеры должно быть темно: " + brightAway);

            // Иерархия яркости
            assertTrue(brightToCamera > bright45deg,
                    "К камере должно быть ярче чем под 45°");
            assertTrue(bright45deg > brightPerpendicular,
                    "45° должно быть ярче чем перпендикулярно");

        } finally {
            SceneManager.activeCamera = originalCamera;
        }
    }

    @Test
    public void testAmbientEffect_CameraLight() {
        // Тестируем эффект (1-k) - фоновую яркость в вашей модели
        Camera camera = new Camera(
                new Vector3f(0, 0, 5),
                new Vector3f(0, 0, 0),
                60, 1, 0.1f, 100
        );

        Camera originalCamera = SceneManager.activeCamera;
        SceneManager.activeCamera = camera;

        try {
            Vector3f normal = new Vector3f(0, 1, 0); // Нормаль перпендикулярно лучу
            Vector3f position = new Vector3f(0, 0, 0);
            Color baseColor = Color.WHITE;

            System.out.println("\nТест фоновой яркости (1-k):");

            // При k=1.0: intensity = 0 + 1*l, где l=0 (перпендикулярно) → 0
            Color resultK1 = TextureMapping.getModifiedColorWithLighting(
                    normal, position, baseColor, 1.0f);
            float brightK1 = (float)((resultK1.getRed() +
                    resultK1.getGreen() +
                    resultK1.getBlue()) / 3.0f);
            System.out.println("k=1.0: " + brightK1);

            // При k=0.5: intensity = 0.5 + 0.5*l, где l=0 → 0.5
            Color resultK05 = TextureMapping.getModifiedColorWithLighting(
                    normal, position, baseColor, 0.5f);
            float brightK05 = (float)((resultK05.getRed() +
                    resultK05.getGreen() +
                    resultK05.getBlue()) / 3.0f);
            System.out.println("k=0.5: " + brightK05);

            // При k=0.0: intensity = 1.0 + 0*l → 1.0
            Color resultK0 = TextureMapping.getModifiedColorWithLighting(
                    normal, position, baseColor, 0.0f);
            float brightK0 = (float)((resultK0.getRed() +
                    resultK0.getGreen() +
                    resultK0.getBlue()) / 3.0f);
            System.out.println("k=0.0: " + brightK0);

            // Проверяем
            assertEquals(0.0f, brightK1, 0.01f,
                    "При k=1.0 и нормали перпендикулярно должно быть темно");
            assertEquals(0.5f, brightK05, 0.01f,
                    "При k=0.5 должно быть 0.5 (фоновое освещение)");
            assertEquals(1.0f, brightK0, 0.01f,
                    "При k=0.0 должно быть всегда ярко");

        } finally {
            SceneManager.activeCamera = originalCamera;
        }
    }

    @Test
    public void testTriangleWithUniformNormals_CameraLight() {
        Camera camera = new Camera(
                new Vector3f(1, 1, 5), // Камера/источник сбоку-сверху-сзади
                new Vector3f(1, 1, 0),
                60, 1, 0.1f, 100
        );

        Camera originalCamera = SceneManager.activeCamera;
        SceneManager.activeCamera = camera;

        try {
            // Создаём треугольник с одинаковыми нормалями
            // Нормаль смотрит в сторону камеры для равномерного освещения
            Vector3f cameraPos = camera.getPosition();
            Vector3f triangleCenter = new Vector3f(1, 1, 0); // Центр треугольника

            // Нормаль направлена от треугольника к камере
            Vector3f triangleNormal = cameraPos.subbed(triangleCenter).normalized();

            Vertex v1 = new Vertex();
            v1.position = new Vector3f(0, 0, 0);
            v1.normal = triangleNormal;

            Vertex v2 = new Vertex();
            v2.position = new Vector3f(2, 0, 0);
            v2.normal = triangleNormal;

            Vertex v3 = new Vertex();
            v3.position = new Vector3f(0, 2, 0);
            v3.normal = triangleNormal;

            // Тестируем точки
            float[] center = {1.0f/3.0f, 1.0f/3.0f, 1.0f/3.0f};
            float[] vertex = {1.0f, 0.0f, 0.0f}; // Точка в вершине v1

            // Преобразуем в пространство камеры для z-значений
            Matrix4f viewMatrix = camera.getViewMatrix();
            Vector3f v1View = viewMatrix.multiplyOnVector(v1.position);
            Vector3f v2View = viewMatrix.multiplyOnVector(v2.position);
            Vector3f v3View = viewMatrix.multiplyOnVector(v3.position);

            float z1 = v1View.getZ();
            float z2 = v2View.getZ();
            float z3 = v3View.getZ();

            // Интерполируем нормали
            Vector3f normalCenter = Rasterization.interpolateNormalWithPerspective(
                    v1, v2, v3, z1, z2, z3, center);
            Vector3f normalVertex = Rasterization.interpolateNormalWithPerspective(
                    v1, v2, v3, z1, z2, z3, vertex);

            System.out.println("\nТест треугольника с одинаковыми нормалями:");
            System.out.println("Камера в: " + camera.getPosition());
            System.out.println("Нормаль треугольника: " + triangleNormal);
            System.out.println("Нормаль в центре: " + normalCenter + ", длина: " + normalCenter.len());
            System.out.println("Нормаль в вершине: " + normalVertex + ", длина: " + normalVertex.len());

            // Позиции для освещения
            Vector3f centerPos = new Vector3f(
                    center[0] * v1.position.getX() + center[1] * v2.position.getX() + center[2] * v3.position.getX(),
                    center[0] * v1.position.getY() + center[1] * v2.position.getY() + center[2] * v3.position.getY(),
                    center[0] * v1.position.getZ() + center[1] * v2.position.getZ() + center[2] * v3.position.getZ()
            );

            // Цвета с k=0.7 (реалистичное значение)
            float k = 0.7f;
            Color centerColor = TextureMapping.getModifiedColorWithLighting(
                    normalCenter, centerPos, Color.WHITE, k);
            Color vertexColor = TextureMapping.getModifiedColorWithLighting(
                    normalVertex, v1.position, Color.WHITE, k);

            float centerBrightness = (float)((centerColor.getRed() +
                    centerColor.getGreen() +
                    centerColor.getBlue()) / 3.0f);
            float vertexBrightness = (float)((vertexColor.getRed() +
                    vertexColor.getGreen() +
                    vertexColor.getBlue()) / 3.0f);

            System.out.println("Яркость в центре: " + centerBrightness);
            System.out.println("Яркость в вершине: " + vertexBrightness);
            System.out.println("Разница: " + Math.abs(centerBrightness - vertexBrightness));

            // При одинаковых нормалях разница должна быть минимальной
            assertTrue(Math.abs(centerBrightness - vertexBrightness) < 0.05f,
                    "При одинаковых нормалях яркость должна быть почти одинаковой. Разница: " +
                            Math.abs(centerBrightness - vertexBrightness));

            // И не должно быть тёмных точек
            assertTrue(centerBrightness > 0.1f, "В центре не должно быть тёмных точек: " + centerBrightness);
            assertTrue(vertexBrightness > 0.1f, "В вершине не должно быть тёмных точек: " + vertexBrightness);

        } finally {
            SceneManager.activeCamera = originalCamera;
        }
    }

    @Test
    public void testOriginalBlackSpotsTest_CameraLight() {
        // Адаптация оригинального теста под вашу модель
        Vertex v1 = new Vertex();
        v1.position = new Vector3f(0, 0, 0);
        v1.normal = new Vector3f(-1, -1, 1).normalized();

        Vertex v2 = new Vertex();
        v2.position = new Vector3f(2, 0, 0);
        v2.normal = new Vector3f(1, -1, 1).normalized();

        Vertex v3 = new Vertex();
        v3.position = new Vector3f(0, 2, 0);
        v3.normal = new Vector3f(-1, 1, 1).normalized();

        // Камера/источник смотрит примерно на центр
        Camera testCamera = new Camera(
                new Vector3f(1, 1, 5),
                new Vector3f(1, 1, 0),
                60, 1, 0.1f, 100
        );
        Camera original = SceneManager.activeCamera;
        SceneManager.activeCamera = testCamera;

        try {
            // Преобразуем в пространство камеры
            Matrix4f viewMatrix = testCamera.getViewMatrix();
            Vector3f v1View = viewMatrix.multiplyOnVector(v1.position);
            Vector3f v2View = viewMatrix.multiplyOnVector(v2.position);
            Vector3f v3View = viewMatrix.multiplyOnVector(v3.position);

            float z1 = v1View.getZ();
            float z2 = v2View.getZ();
            float z3 = v3View.getZ();

            // Тестируем точки
            float[] center = {1.0f/3.0f, 1.0f/3.0f, 1.0f/3.0f};
            float[] nearCenter = {0.4f, 0.3f, 0.3f};

            Vector3f normalCenter = Rasterization.interpolateNormalWithPerspective(
                    v1, v2, v3, z1, z2, z3, center);
            Vector3f normalNearCenter = Rasterization.interpolateNormalWithPerspective(
                    v1, v2, v3, z1, z2, z3, nearCenter);

            System.out.println("\nОригинальный тест (адаптированный):");
            System.out.println("Нормаль в центре: длина = " + normalCenter.len());
            System.out.println("Нормаль около центра: длина = " + normalNearCenter.len());

            // Проверяем длину нормали
            assertTrue(normalCenter.len() > 0.95f,
                    "Нормаль в центре должна быть почти единичной: " + normalCenter.len());
            assertTrue(normalNearCenter.len() > 0.95f,
                    "Нормаль около центра должна быть почти единичной: " + normalNearCenter.len());

            // Позиция центра
            Vector3f centerPos = new Vector3f(
                    center[0] * v1.position.getX() + center[1] * v2.position.getX() + center[2] * v3.position.getX(),
                    center[0] * v1.position.getY() + center[1] * v2.position.getY() + center[2] * v3.position.getY(),
                    center[0] * v1.position.getZ() + center[1] * v2.position.getZ() + center[2] * v3.position.getZ()
            );

            // Используем k=0.7 как в реальном рендере
            float k = 0.7f;
            Color centerColor = TextureMapping.getModifiedColorWithLighting(
                    normalCenter, centerPos, Color.WHITE, k);
            Color vertexColor = TextureMapping.getModifiedColorWithLighting(
                    v1.normal, v1.position, Color.WHITE, k);

            System.out.println("Цвет в центре: R=" + centerColor.getRed() +
                    ", G=" + centerColor.getGreen() + ", B=" + centerColor.getBlue());
            System.out.println("Цвет в вершине: R=" + vertexColor.getRed() +
                    ", G=" + vertexColor.getGreen() + ", B=" + vertexColor.getBlue());

            float centerBrightness = (float)((centerColor.getRed() +
                    centerColor.getGreen() +
                    centerColor.getBlue()) / 3.0f);
            float vertexBrightness = (float)((vertexColor.getRed() +
                    vertexColor.getGreen() +
                    vertexColor.getBlue()) / 3.0f);

            System.out.println("Яркость в центре: " + centerBrightness);
            System.out.println("Яркость в вершине: " + vertexBrightness);
            System.out.println("Разница: " + Math.abs(centerBrightness - vertexBrightness));

            // Главное - не должно быть чёрных точек (brightness = 0)
            assertTrue(centerBrightness > 0.1f,
                    "В центре не должно быть чёрных точек. Яркость: " + centerBrightness);
            assertTrue(vertexBrightness > 0.1f,
                    "В вершине не должно быть чёрных точек. Яркость: " + vertexBrightness);

            // Разница может быть, но не катастрофическая
            assertTrue(Math.abs(centerBrightness - vertexBrightness) < 0.5f,
                    "Слишком большая разница в яркости: " +
                            Math.abs(centerBrightness - vertexBrightness));

        } finally {
            SceneManager.activeCamera = original;
        }
    }
    @Test
    public void quickSignTest() {
        Camera camera = new Camera(new Vector3f(0,0,10), new Vector3f(0,0,0), 60,1,0.1f,100);
        SceneManager.activeCamera = camera;

        Vector3f position = new Vector3f(0,0,0);
        Color white = Color.WHITE;

        // Нормаль К камере
        Vector3f n1 = new Vector3f(0,0,1);
        Color c1 = TextureMapping.getModifiedColorWithLighting(n1, position, white, 1.0f);
        float b1 = (float) ((c1.getRed() + c1.getGreen() + c1.getBlue()) / 3.0f);
        System.out.println("n=(0,0,1) к камере: " + b1);

        // Нормаль ОТ камеры
        Vector3f n2 = new Vector3f(0,0,-1);
        Color c2 = TextureMapping.getModifiedColorWithLighting(n2, position, white, 1.0f);
        float b2 = (float) ((c2.getRed() + c2.getGreen() + c2.getBlue()) / 3.0f);
        System.out.println("n=(0,0,-1) от камеры: " + b2);

        // Проверяем
        assertTrue(b1 > 0.9f, "К камере должно быть ярко: " + b1);
        assertEquals(0.0f, b2, 0.01f, "От камеры должно быть темно: " + b2);
    }
    @Test
    public void testWhichMultiplyOnVector() {
        Matrix4f matrix = new Matrix4f();
        Vector3f vector = new Vector3f(0, 0, -5);

        // 1. Какой метод вы используете?
        Vector3f result1 = matrix.multiplyOnVector(vector); // Метод объекта Matrix4f

        // 2. Или этот?
        Vector3f result2 = Matrix4f.multiplyOnVector(matrix, vector); // Статический метод

        System.out.println("Результат 1 (метод объекта): " + result1);
        System.out.println("Результат 2 (статический метод): " + result2);

        // Они должны быть разными если один делит на w, а другой нет!
    }


}

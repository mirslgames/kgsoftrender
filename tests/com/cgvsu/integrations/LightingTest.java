package com.cgvsu.integrations;

import com.cgvsu.math.matrixs.Matrix4f;
import com.cgvsu.math.point.Point2f;
import com.cgvsu.math.vectors.Vector2f;
import com.cgvsu.math.vectors.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Vertex;
import com.cgvsu.modelOperations.Rasterization;
import com.cgvsu.modelOperations.TextureMapping;
import com.cgvsu.modelOperations.ZBuffer;
import com.cgvsu.render_engine.Camera;
import com.cgvsu.sceneview.SceneManager;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class LightingTest {
    @Test
    public void testCompleteRenderingPipeline() {

        Vector3f cameraPos = new Vector3f(0, 0, 5); // Камера смотрит из точки (0,0,5)
        Vector3f cameraTarget = new Vector3f(0, 0, 0); // Смотрит в центр
        Camera testCamera = new Camera(cameraPos, cameraTarget, 60.0f, 1.0f, 0.1f, 100.0f);

        // Сохраняем старую камеру (если была) и устанавливаем тестовую
        Camera originalCamera = SceneManager.activeCamera;
        SceneManager.activeCamera = testCamera;
        // Создаем простую модель с нормалями
        Model model = new Model();

        // Вершины треугольника с нормалями
        Vertex v1 = new Vertex();
        v1.position = new Vector3f(0, 0, 0);
        v1.normal = new Vector3f(0, 0, 1);
        v1.getOrAddTextureCoordinate(new Vector2f(0, 0));

        Vertex v2 = new Vertex();
        v2.position = new Vector3f(1, 0, 0);
        v2.normal = new Vector3f(0, 0, 1);
        v2.getOrAddTextureCoordinate(new Vector2f(1, 0));

        Vertex v3 = new Vertex();
        v3.position = new Vector3f(0, 1, 0);
        v3.normal = new Vector3f(0, 0, 1);
        v3.getOrAddTextureCoordinate(new Vector2f(0, 1));

        model.vertices.addAll(Arrays.asList(v1, v2, v3));
        model.polygonsBoundaries.add(0);
        model.polygons.addAll(Arrays.asList(0, 1, 2));

        // Создаем текстуру
        WritableImage texture = new WritableImage(2, 2);
        texture.getPixelWriter().setColor(0, 0, Color.RED);
        texture.getPixelWriter().setColor(1, 0, Color.GREEN);
        texture.getPixelWriter().setColor(0, 1, Color.BLUE);
        texture.getPixelWriter().setColor(1, 1, Color.YELLOW);
        model.texture = texture;

        // Создаем Z-буфер
        ZBuffer zBuffer = new ZBuffer(100, 100);
        zBuffer.clear();

        // Коллекция для сбора нарисованных пикселей
        List<String> renderedPixels = new ArrayList<>();

        // Простая проекция (орфографическая)
        Point2f p1 = new Point2f(10, 10);
        Point2f p2 = new Point2f(50, 10);
        Point2f p3 = new Point2f(10, 50);

        float z1 = 1.0f, z2 = 1.0f, z3 = 1.0f;

        // Растеризуем треугольник
        Rasterization.PixelCallback callback = (x, y, z, barycentric, texCoord, worldNormal, worldPosition) -> {
            if (zBuffer.testAndSet(x, y, z)) {
                // Получаем цвет из текстуры
                Color texColor = TextureMapping.getTextureColor(texture, texCoord);

                // Применяем освещение
                Color finalColor = TextureMapping.getModifiedColorWithLighting(
                        worldNormal, worldPosition, texColor, 1.0f);

                renderedPixels.add(x + "," + y + ":" + finalColor);
            }
        };

        Rasterization.rasterizeTriangleWithWorldPos(
                p1, p2, p3,
                z1, z2, z3,
                v1, v2, v3,
                v1.getTextureCoordinate(0),
                v2.getTextureCoordinate(0),
                v3.getTextureCoordinate(0),
                v1.position, v2.position, v3.position,
                callback, new Matrix4f()); // Единичная матрица

        // Проверяем что хоть какие-то пиксели были нарисованы
        assertTrue(renderedPixels.size() > 0);

        // Проверяем что пиксели внутри треугольника были нарисованы
        // (например, центр треугольника)
        boolean foundCenter = false;
        for (String pixel : renderedPixels) {
            if (pixel.startsWith("20,20:")) {
                foundCenter = true;
                break;
            }
        }
        assertTrue(foundCenter, "Центр треугольника должен быть нарисован");
    }
}

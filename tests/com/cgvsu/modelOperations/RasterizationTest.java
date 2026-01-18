package com.cgvsu.modelOperations;

import com.cgvsu.math.matrixs.Matrix4f;
import com.cgvsu.math.point.Point2f;
import com.cgvsu.math.vectors.Vector2f;
import com.cgvsu.math.vectors.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Vertex;
import com.cgvsu.render_engine.Camera;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.cgvsu.modelOperations.Rasterization.*;
import static org.junit.jupiter.api.Assertions.*;

public class RasterizationTest {
    @Test
    public void testBarycentricCoordinates() {
        // Простой треугольник
        Point2f v1 = new Point2f(0, 0);
        Point2f v2 = new Point2f(2, 0);
        Point2f v3 = new Point2f(0, 2);

        // Центр треугольника (центроид)
        Point2f center = new Point2f((0+2+0)/3f, (0+0+2)/3f);

        float[] bary = Rasterization.calculateBarycentricCoordinates(center, v1, v2, v3);

        // В центроиде все координаты должны быть равны 1/3
        assertEquals(1.0f/3.0f, bary[0], 0.001f);
        assertEquals(1.0f/3.0f, bary[1], 0.001f);
        assertEquals(1.0f/3.0f, bary[2], 0.001f);

        // Проверяем точку в вершине
        float[] baryV1 = Rasterization.calculateBarycentricCoordinates(v1, v1, v2, v3);
        assertEquals(1.0f, baryV1[0], 0.001f);
        assertEquals(0.0f, baryV1[1], 0.001f);
        assertEquals(0.0f, baryV1[2], 0.001f);

        // Проверяем точку на ребре
        Point2f edgePoint = new Point2f(1, 0); // середина между v1 и v2
        float[] baryEdge = Rasterization.calculateBarycentricCoordinates(edgePoint, v1, v2, v3);
        assertEquals(0.5f, baryEdge[0], 0.001f);
        assertEquals(0.5f, baryEdge[1], 0.001f);
        assertEquals(0.0f, baryEdge[2], 0.001f);
    }

    @Test
    public void testIsInsideTriangle() {
        Point2f v1 = new Point2f(0, 0);
        Point2f v2 = new Point2f(2, 0);
        Point2f v3 = new Point2f(0, 2);

        // Точка внутри
        Point2f inside = new Point2f(0.5f, 0.5f);
        float[] baryInside = Rasterization.calculateBarycentricCoordinates(inside, v1, v2, v3);
        assertTrue(Rasterization.isInsideTriangle(baryInside));

        // Точка снаружи
        Point2f outside = new Point2f(2, 2);
        float[] baryOutside = Rasterization.calculateBarycentricCoordinates(outside, v1, v2, v3);
        assertFalse(Rasterization.isInsideTriangle(baryOutside));

        // Точка на границе (должна считаться внутри)
        Point2f boundary = new Point2f(1, 0);
        float[] baryBoundary = Rasterization.calculateBarycentricCoordinates(boundary, v1, v2, v3);
        assertTrue(Rasterization.isInsideTriangle(baryBoundary));
    }

    @Test
    public void testInterpolation() {
        // Проверяем интерполяцию скаляров
        float[] bary = {0.25f, 0.25f, 0.5f};

        float result = interpolate(1.0f, 2.0f, 3.0f, bary);
        // 0.25*1 + 0.25*2 + 0.5*3 = 0.25 + 0.5 + 1.5 = 2.25
        assertEquals(2.25f, result, 0.001f);

        // Проверяем интерполяцию Vector2f (текстурные координаты)
        Vector2f t1 = new Vector2f(0, 0);
        Vector2f t2 = new Vector2f(1, 0);
        Vector2f t3 = new Vector2f(0, 1);

        Vector2f texResult = interpolate(t1, t2, t3, bary);
        assertEquals(0.25f, texResult.getX(), 0.001f); // 0.25*0 + 0.25*1 + 0.5*0 = 0.25
        assertEquals(0.5f, texResult.getY(), 0.001f);  // 0.25*0 + 0.25*0 + 0.5*1 = 0.5

        // Проверяем интерполяцию Vector3f (нормали)
        Vector3f n1 = new Vector3f(1, 0, 0);
        Vector3f n2 = new Vector3f(0, 1, 0);
        Vector3f n3 = new Vector3f(0, 0, 1);

        Vector3f normalResult = interpolate(n1, n2, n3, bary);
        assertEquals(0.25f, normalResult.getX(), 0.001f); // 0.25*1 + 0.25*0 + 0.5*0 = 0.25
        assertEquals(0.25f, normalResult.getY(), 0.001f); // 0.25*0 + 0.25*1 + 0.5*0 = 0.25
        assertEquals(0.5f, normalResult.getZ(), 0.001f);  // 0.25*0 + 0.25*0 + 0.5*1 = 0.5
    }
    //Тут сделаю проверку реастеризации прямых и треугольников, через assert буду считать правильные цвета на простых
    // случаях
    @Test
    public void testDegenerateTriangle() {
        // Вырожденный треугольник (все точки на одной линии)
        Point2f v1 = new Point2f(0, 0);
        Point2f v2 = new Point2f(1, 1);
        Point2f v3 = new Point2f(2, 2); // На одной линии с v1 и v2

        // Барицентрические координаты для точки на линии
        float[] bary = Rasterization.calculateBarycentricCoordinates(new Point2f(1, 1), v1, v2, v3);

        // isInsideTriangle должен вернуть false для вырожденного треугольника
        assertFalse(Rasterization.isInsideTriangle(bary));
    }

    @Test
    public void testZeroAreaTriangle() {
        // Треугольник с нулевой площадью (все вершины совпадают)
        Point2f v1 = new Point2f(0, 0);
        Point2f v2 = new Point2f(0, 0);
        Point2f v3 = new Point2f(0, 0);

        int[] bbox = Rasterization.getBoundingBox(v1, v2, v3);
        assertEquals(0, bbox[0]);
        assertEquals(0, bbox[1]);
        assertEquals(0, bbox[2]);
        assertEquals(0, bbox[3]);
    }
    @Test
    public void testLineRasterizationHorizontal() {
        List<String> pixels = new ArrayList<>();

        Rasterization.LinePixelCallback callback = (x, y, z) -> {
            pixels.add(x + "," + y);
        };

        // Горизонтальная линия длиной 5 пикселей
        Rasterization.rasterizeLine(0, 0, 4, 0, 1.0f, 1.0f, callback);

        // Должно быть 5 пикселей: (0,0), (1,0), (2,0), (3,0), (4,0)
        assertEquals(5, pixels.size());
        assertTrue(pixels.contains("0,0"));
        assertTrue(pixels.contains("4,0"));
    }

    @Test
    public void testLineRasterizationVertical() {
        List<String> pixels = new ArrayList<>();

        Rasterization.LinePixelCallback callback = (x, y, z) -> {
            pixels.add(x + "," + y);
        };

        // Вертикальная линия
        Rasterization.rasterizeLine(0, 0, 0, 4, 1.0f, 1.0f, callback);

        assertEquals(5, pixels.size());
        assertTrue(pixels.contains("0,0"));
        assertTrue(pixels.contains("0,4"));
    }

    @Test
    public void testLineRasterizationDiagonal() {
        List<String> pixels = new ArrayList<>();

        Rasterization.LinePixelCallback callback = (x, y, z) -> {
            pixels.add(x + "," + y);
        };

        // Диагональ
        Rasterization.rasterizeLine(0, 0, 3, 3, 1.0f, 1.0f, callback);

        // Должно быть 4 пикселя: (0,0), (1,1), (2,2), (3,3)
        assertTrue(pixels.contains("0,0"));
        assertTrue(pixels.contains("1,1"));
        assertTrue(pixels.contains("2,2"));
        assertTrue(pixels.contains("3,3"));
    }

    @Test
    public void testLineZInterpolation() {
        List<Float> zValues = new ArrayList<>();

        Rasterization.LinePixelCallback callback = (x, y, z) -> {
            zValues.add(z);
        };

        // Линия с изменением глубины
        Rasterization.rasterizeLine(0, 0, 4, 0, 1.0f, 0.0f, callback);

        // Z должен линейно интерполироваться от 1.0 до 0.0
        assertEquals(1.0f, zValues.get(0), 0.001f);  // Начало
        assertEquals(0.0f, zValues.get(zValues.size()-1), 0.001f);  // Конец

        // Проверяем среднюю точку
        float middleZ = zValues.get(zValues.size()/2);
        assertEquals(0.5f, middleZ, 0.1f);
    }

}

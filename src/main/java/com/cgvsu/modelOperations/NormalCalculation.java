package com.cgvsu.modelOperations;

import com.cgvsu.math.vectors.Vector3f;
import com.cgvsu.model.Vertex;

import java.util.List;

public class NormalCalculation {
    /**
     *  Метод, который рассчитывает нормаль для конкретного полигона в модели.Конкретно в этом методе не важно, чтобы модель
     *  была триангулированной. Рассчёт основывавется на операциях векторного произведения
     * @param vertices
     * @param polygon
     * @param start
     * @param end
     * @return
     */
    public static Vector3f calculatePolygonNormal(List<Vertex> vertices, List<Integer> polygon, int start, int end) {
        if (end - start < 3) return new Vector3f(0, 0, 0);

        int i1 = polygon.get(start);
        int i2 = polygon.get(start + 1);
        int i3 = polygon.get(start + 2);

        if (i1 < 0 || i2 < 0 || i3 < 0 || i1 >= vertices.size() || i2 >= vertices.size() || i3 >= vertices.size()) {
            return new Vector3f(0, 0, 0);
        }

        Vector3f pos1 = vertices.get(i1).position;
        Vector3f pos2 = vertices.get(i2).position;
        Vector3f pos3 = vertices.get(i3).position;

        Vector3f normal = pos2.subbed(pos1).crossed(pos3.subbed(pos1));

        if (normal.len() < 1e-6f) {
            // Обрабатываем вырожденный случай, чтобы не было артефактов в освещении
            return new Vector3f(0, 0, 0);
        }

        normal.normalize();
        return normal;
    }

    /**
     * Метод, который рассчитывает площадь одного полигона(треугольника) в модели, используется для сравения по весам
     * в MyVertexNormalCalc
     * @param vertices список вершин модели
     * @param polygon список индексов на вершины в треугольнике (плоский массив)
     * @param start  первый индекс для навигации в плоском массиве polygon
     * @param end   второй индекс верши для навигации в плоском массиве polygon
     * @return
     */
    public static float computePolygonArea(List<Vertex> vertices, List<Integer> polygon, int start, int end) {
        if (end - start < 3) return 0f;

        int i1 = polygon.get(start);
        int i2 = polygon.get(start + 1);
        int i3 = polygon.get(start + 2);

        if (i1 < 0 || i2 < 0 || i3 < 0 || i1 >= vertices.size() || i2 >= vertices.size() || i3 >= vertices.size()) {
            return 0f;
        }

        Vector3f v1 = vertices.get(i1).position;
        Vector3f v2 = vertices.get(i2).position;
        Vector3f v3 = vertices.get(i3).position;

        return v2.subbed(v1).crossed(v3.subbed(v1)).len() * 0.5f;
    }
}

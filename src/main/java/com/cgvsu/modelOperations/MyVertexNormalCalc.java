package com.cgvsu.modelOperations;

import com.cgvsu.math.vectors.Vector3f;
import com.cgvsu.model.Model;

import java.util.ArrayList;
import java.util.List;

import static com.cgvsu.modelOperations.NormalCalculation.calculatePolygonNormal;
import static com.cgvsu.modelOperations.NormalCalculation.computePolygonArea;

public class MyVertexNormalCalc implements VertexNormals<Model> {
    /**
     * Метод, которые рассчитывает нормали к вершинам модели. Очень важно, чтобы модель на входе была триангулированная
     * Реализация использует рассчёт по весам, а не среднее, так как это точнее
     * @param model триангулированная модель, которой надо посчитать нормали
     */
    @Override
    public void calculateVertexNormals(Model model) {
        int vertexCount = model.vertices.size();

        List<Vector3f> vertexNormals = new ArrayList<>(vertexCount);
        List<Float> vertexWeights = new ArrayList<>(vertexCount);

        for (int i = 0; i < vertexCount; i++) {
            vertexNormals.add(new Vector3f(0, 0, 0));
            vertexWeights.add(0f);
        }

        int polyCount = model.polygonsBoundaries.size();

        for (int polyIdx = 0; polyIdx < polyCount; polyIdx++) {
            int startOfPolygon = model.polygonsBoundaries.get(polyIdx);
            int endOfPolygon = (polyIdx == polyCount - 1)
                    ? model.polygons.size()
                    : model.polygonsBoundaries.get(polyIdx + 1);

            if (endOfPolygon - startOfPolygon < 3) continue;

            Vector3f polygonNormal = calculatePolygonNormal(
                    model.vertices,
                    model.polygons,
                    startOfPolygon,
                    endOfPolygon
            );

            float area = computePolygonArea(
                    model.vertices,
                    model.polygons,
                    startOfPolygon,
                    endOfPolygon
            );

            // Скипаем мусорные/вырожденные полигоны, чтобы не засорять нормали
            if (area <= 1e-12f || polygonNormal.len() <= 1e-6f) continue;

            // ВАЖНО: multiply() мутирует, поэтому только multiplied()
            Vector3f weightedNormal = polygonNormal.multiplied(area);

            for (int j = startOfPolygon; j < endOfPolygon; j++) {
                int idx = model.polygons.get(j);
                if (idx < 0 || idx >= vertexCount) continue;

                Vector3f currentNormal = vertexNormals.get(idx);
                vertexNormals.set(idx, currentNormal.added(weightedNormal));

                vertexWeights.set(idx, vertexWeights.get(idx) + area);
            }
        }

        for (int i = 0; i < vertexCount; i++) {
            float weight = vertexWeights.get(i);
            if (weight > 0) {
                model.vertices.get(i).normal = vertexNormals.get(i).divided(weight).normalized();
            } else {
                model.vertices.get(i).normal = new Vector3f(0, 0, 0);
            }
        }
    }
}

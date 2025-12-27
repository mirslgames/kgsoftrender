package com.cgvsu.modelOperations;

import com.cgvsu.math.vectors.Vector3f;
import com.cgvsu.model.Model;

import java.util.ArrayList;
import java.util.List;

import static com.cgvsu.modelOperations.NormalCalculation.calculatePolygonNormal;
import static com.cgvsu.modelOperations.NormalCalculation.computePolygonArea;

public class MyVertexNormalCalc implements VertexNormals<Model> {

    @Override
    public void calculateVertexNormals(Model model) {
        int vertexCount = model.vertices.size();

        // Инициализация структур для накопления
        List<Vector3f> vertexNormals = new ArrayList<>(vertexCount);
        List<Float> vertexWeights = new ArrayList<>(vertexCount);

        for (int i = 0; i < vertexCount; i++) {
            vertexNormals.add(new Vector3f(0, 0, 0)); // Нулевой вектор
            vertexWeights.add(0f); // Нулевой вес
        }

        // 2. Обработка каждого полигона
        int polyCount = model.polygonsBoundaries.size();

        for (int polyIdx = 0; polyIdx < polyCount; polyIdx++) {
            int startOfPolygon = model.polygonsBoundaries.get(polyIdx);
            int endOfPolygon = (polyIdx == polyCount - 1)
                    ? model.polygons.size()
                    : model.polygonsBoundaries.get(polyIdx + 1);


            if (endOfPolygon - startOfPolygon < 3) {
                continue;
            }

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

            for (int j = startOfPolygon; j < endOfPolygon; j++) {
                int idx = model.polygons.get(j);


                if (idx < 0 || idx >= vertexCount) {
                    continue;
                }

                Vector3f weighted = polygonNormal.multiplied(area);
                Vector3f currentNormal = vertexNormals.get(idx);
                vertexNormals.set(idx, currentNormal.added(weighted));

                // Обновляем вес
                float currentWeight = vertexWeights.get(idx);
                vertexWeights.set(idx, currentWeight + area);
            }
        }


        for (int i = 0; i < vertexCount; i++) {
            float weight = vertexWeights.get(i);

            if (weight > 0) {
                Vector3f averaged = vertexNormals.get(i).divided(weight).normalized();
                // Сохраняем нормаль в вершину модели
                model.vertices.get(i).normal = averaged;
            } else {
                // Если вершина не участвовала ни в одном полигоне
                model.vertices.get(i).normal = (new Vector3f(0, 0, 0));
            }
        }
    }
}

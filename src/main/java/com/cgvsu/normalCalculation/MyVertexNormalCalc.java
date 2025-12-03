package com.cgvsu.normalCalculation;

import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

import java.util.ArrayList;
import java.util.List;

import static com.cgvsu.normalCalculation.VectorMultiply.calculatePolygonNormal;
import static com.cgvsu.normalCalculation.VectorMultiply.computePolygonArea;

public class MyVertexNormalCalc implements VertexNormals<Vector3f, Model> {

    @Override
    public  List<Vector3f> calculateVertexNormals(Model model) {
        int vertexCount = model.vertices.size();
        boolean isOneBased = model.polygons.get(0).getVertexIndices().get(0) == 1;

        List<Vector3f> vertexNormals = new ArrayList<>();
        List<Float> vertexWeights = new ArrayList<>();
        for (int i = 0; i < vertexCount; i++) {
            vertexNormals.add(new Vector3f(0, 0, 0));
            vertexWeights.add(0f);
        }

        for (Polygon polygon : model.polygons) {
            Vector3f polygonNormal = calculatePolygonNormal(polygon, model.vertices);
            float area = computePolygonArea(polygon, model.vertices);

            for (Integer vertexIndex : polygon.getVertexIndices()) {
                int idx = isOneBased ? vertexIndex - 1 : vertexIndex;
                if (idx < 0 || idx >= vertexCount) continue;

                Vector3f weighted = polygonNormal.multiply(area);
                vertexNormals.set(idx, vertexNormals.get(idx).add(weighted));
                vertexWeights.set(idx, vertexWeights.get(idx) + area);
            }
        }

        for (int i = 0; i < vertexCount; i++) {
            float weight = vertexWeights.get(i);
            if (weight > 0) {
                Vector3f averaged = vertexNormals.get(i).divide(weight).normalize();
                vertexNormals.set(i, averaged);
            }
        }

        return vertexNormals;
    }
}

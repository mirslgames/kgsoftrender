package com.cgvsu.model;



import com.cgvsu.math.vectors.Vector2f;
import com.cgvsu.modelOperations.TriangulationAlgorithm;
import javafx.scene.image.Image;


import java.util.*;

public class Model {

    public String modelName;
    public ArrayList<Vertex> vertices = new ArrayList<>(); //Вершины у модельки
    public ArrayList<Integer> polygons = new ArrayList<Integer>(); //Индексы на конкретные вершины из списка для полигонов
    public ArrayList<Integer> polygonsBoundaries = new ArrayList<>(); //Номер индекса с которого идут вершины для данного полигона (старт)
    public ArrayList<Integer> polygonsTextureCoordinateIndices = new ArrayList<>();
    public boolean hasTexture;
    public Image texture;

    //Положение модельки в сцене, todo: возможно переписать под векторы
    public float positionXValue;
    public float positionYValue;
    public float positionZValue;
    public float rotationXValue;
    public float rotationYValue;
    public float rotationZValue;
    public float scaleXValue;
    public float scaleYValue;
    public float scaleZValue;

    public Model(){
        positionXValue = 0;
        positionYValue = 0;
        positionZValue = 0;
        rotationXValue = 0;
        rotationYValue = 0;
        rotationZValue = 0;
        scaleXValue = 1;
        scaleYValue = 1;
        scaleZValue = 1;
    }
    /**
     * Оставил старое имя метода, потому что он используется в UI (GuiController).
     * По смыслу: есть ли в модели UV (vt), которые можно использовать.
     */
    public boolean getHasTextureVertex() {
        return polygonsTextureCoordinateIndices != null
                && polygonsTextureCoordinateIndices.size() == polygons.size();
    }

    public Vector2f getTextureCoordinateForPolygonVertex(final int polygonVertexGlobalIndex) {
        if (!getHasTextureVertex()) return null;
        if (polygonVertexGlobalIndex < 0 || polygonVertexGlobalIndex >= polygons.size()) return null;

        int vIndex = polygons.get(polygonVertexGlobalIndex);
        if (vIndex < 0 || vIndex >= vertices.size()) return null;

        int uvLocalIndex = polygonsTextureCoordinateIndices.get(polygonVertexGlobalIndex);
        return vertices.get(vIndex).getTextureCoordinate(uvLocalIndex);
    }
    /**
     * Триангулирует все полигоны в модели, сохраняя индексы UV (локальные) синхронно с вершинами.
     */
    public void triangulate() {
        if (polygonsBoundaries == null || polygonsBoundaries.isEmpty()) {
            return;
        }

        ArrayList<Integer> newPolygons = new ArrayList<>();
        ArrayList<Integer> newTextureLocalIndices = new ArrayList<>();
        ArrayList<Integer> newBoundaries = new ArrayList<>();

        int polygonCount = polygonsBoundaries.size();
        for (int polygonInd = 0; polygonInd < polygonCount; polygonInd++) {
            int start = polygonsBoundaries.get(polygonInd);
            int end = (polygonInd + 1 < polygonCount)
                    ? polygonsBoundaries.get(polygonInd + 1)
                    : polygons.size();

            int vertexCount = end - start;
            if (vertexCount < 3) {
                continue;
            }

            List<Integer> polygonVertices = new ArrayList<>(polygons.subList(start, end));
            List<Integer> polygonVtLocal = new ArrayList<>(polygonsTextureCoordinateIndices.subList(start, end));

            // Триангулируем по позициям 0..N-1 и применяем те же позиции к обоим спискам.
            List<List<Integer>> trianglesPos = TriangulationAlgorithm.triangulatePositions(vertexCount);
            for (List<Integer> triPos : trianglesPos) {
                newBoundaries.add(newPolygons.size());
                for (int pos : triPos) {
                    newPolygons.add(polygonVertices.get(pos));
                    newTextureLocalIndices.add(polygonVtLocal.get(pos));
                }
            }
        }

        polygons = newPolygons;
        polygonsTextureCoordinateIndices = newTextureLocalIndices;
        polygonsBoundaries = newBoundaries;
    }


}


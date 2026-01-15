package com.cgvsu.model;

import com.cgvsu.modelOperations.TriangulationAlgorithm;
import javafx.scene.image.Image;
import com.cgvsu.math.vectors.Vector2f;
import com.cgvsu.math.vectors.Vector3f;
import com.cgvsu.modelOperations.MyVertexNormalCalc;
import java.util.*;

public class Model {

    public String modelName;
    public ArrayList<Vertex> vertices = new ArrayList<>(); //Вершины у модельки
    public ArrayList<Integer> polygons = new ArrayList<Integer>(); //Индексы на конкретные вершины из списка для полигонов
    public ArrayList<Integer> polygonsBoundaries = new ArrayList<>(); //Номер индекса с которого идут вершины для данного полигона (старт)
    public boolean hasTexture;
    public Image texture;
    //todo: Добавить поле для самой текстуры + дефолтная текстура + подумать над режимами отрисовки

    //Положение модельки в сцене
    public Transform currentTransform;
    //История трансформаций где последняя должна совпадать с текущей
    public ArrayList<Transform> transformHistory;

    public Model() {

    }

    public static Model constructModelFromReadData(
            ArrayList<Vector3f> readVertices,
            ArrayList<Vector2f> readTextureVertices,
            ArrayList<Vector3f> readNormals,
            ArrayList<ArrayList<Integer>[]> readPolygonsIndices,
            String filename,
            boolean resultCheck
    ) {
        if (resultCheck) {
            try {
                Model result = new Model();
                result.modelName = filename;
                result.currentTransform = new Transform(0, 0, 0, 0, 0, 0, 1, 1, 1);

                result.vertices = new ArrayList<>();
                for (int i = 0; i < readVertices.size(); i++) {
                    Vertex vertex = new Vertex();
                    vertex.position = readVertices.get(i);
                    vertex.normal = null;
                    vertex.textureCoordinate = null;
                    result.vertices.add(vertex);
                }

                result.polygons = new ArrayList<>();
                result.polygonsBoundaries = new ArrayList<>();

                int polygonIndexCount = 0;
                for (ArrayList<Integer>[] polygon : readPolygonsIndices) {
                    result.polygonsBoundaries.add(polygonIndexCount);

                    ArrayList<Integer> vIdx = polygon[0];
                    for (int i = 0; i < vIdx.size(); i++) {
                        result.polygons.add(vIdx.get(i));
                        polygonIndexCount++;
                    }
                }

                //Считаем, что vt идут 1 в 1 с вершинами v по индексу
                if (readTextureVertices != null && !readTextureVertices.isEmpty()) {
                    for (int i = 0; i < result.vertices.size(); i++) {
                        result.vertices.get(i).textureCoordinate = readTextureVertices.get(i);
                    }
                    result.hasTexture = true;
                } else {
                    result.hasTexture = false;
                }

                MyVertexNormalCalc calc = new MyVertexNormalCalc();
                calc.calculateVertexNormals(result);

                return result;
            } catch (Exception exception) {
                throw new RuntimeException("Ошибка при построении модели на основе прочитанных данных: " + exception.getMessage());
            }
        }
        throw new RuntimeException("Прочитанные данные не корректны");
    }

    public void triangulate() {
        ArrayList<Integer> newPolygons = new ArrayList<>();
        ArrayList<Integer> newBoundaries = new ArrayList<>();

        for (int i = 0; i < polygonsBoundaries.size(); i++) {
            int start = polygonsBoundaries.get(i);
            int end = (i + 1 < polygonsBoundaries.size()) ? polygonsBoundaries.get(i + 1) : polygons.size();
            List<Integer> polygon = new ArrayList<>(polygons.subList(start, end));


            List<List<Integer>> triangles = TriangulationAlgorithm.triangulate(polygon);


            for (List<Integer> tri : triangles) {
                newBoundaries.add(newPolygons.size());
                newPolygons.addAll(tri);
            }
        }

        polygons = newPolygons;
        polygonsBoundaries = newBoundaries;
    }
}


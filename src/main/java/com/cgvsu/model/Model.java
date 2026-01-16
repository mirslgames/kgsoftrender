package com.cgvsu.model;



import com.cgvsu.modelOperations.TriangulationAlgorithm;
import javafx.scene.image.Image;


import java.util.*;

public class Model {

    public String modelName;
    public ArrayList<Vertex> vertices = new ArrayList<>(); //Вершины у модельки
    public ArrayList<Integer> polygons = new ArrayList<Integer>(); //Индексы на конкретные вершины из списка для полигонов
    public ArrayList<Integer> polygonsBoundaries = new ArrayList<>(); //Номер индекса с которого идут вершины для данного полигона (старт)
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


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

    public Model(){

    }
    public void triangulate() {
        ArrayList<Integer> newPolygons = new ArrayList<>();
        ArrayList<Integer> newBoundaries = new ArrayList<>();

        for (int i = 0; i < polygonsBoundaries.size(); i++) {
            int start = polygonsBoundaries.get(i);
            int end = (i + 1 < polygonsBoundaries.size()) ? polygonsBoundaries.get(i + 1) : polygons.size();
            List<Integer> polygon = new ArrayList<>(polygons.subList(start, end));

            System.out.println("Original polygon " + i + " indices: " + polygon);

            List<List<Integer>> triangles = TriangulationAlgorithm.triangulate(polygon);
            System.out.println("Triangles for polygon " + i + ": " + triangles);

            for (List<Integer> tri : triangles) {
                newBoundaries.add(newPolygons.size());
                newPolygons.addAll(tri);
            }
        }

        polygons = newPolygons;
        polygonsBoundaries = newBoundaries;

        System.out.println("After triangulation: total triangles = " + newBoundaries.size());
        System.out.println("Indices: " + newPolygons);
    }

}


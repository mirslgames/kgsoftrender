package com.cgvsu.model;

import java.util.*;

public class Model {

    public String modelName;
    public ArrayList<Vertex> vertices = new ArrayList<>(); //Вершины у модельки
    public ArrayList<Integer> polygons = new ArrayList<Integer>(); //Индексы на конкретные вершины из списка для полигонов
    public ArrayList<Integer> polygonsBoundaries = new ArrayList<>(); //Номер индекса с которого идут вершины для данного полигона (старт)
    public boolean hasTexture;
    //todo: Добавить поле для самой текстуры + подумать над режимами отрисовки

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

}


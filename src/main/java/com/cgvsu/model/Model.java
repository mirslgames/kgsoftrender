package com.cgvsu.model;

import java.util.*;

public class Model {

    public String modelName;
    public ArrayList<Vertex> vertices = new ArrayList<>(); //Вершины у модельки
    public ArrayList<Integer> polygons = new ArrayList<Integer>(); //Индексы на конкретные вершины из списка для полигонов
    public ArrayList<Integer> polygonsBoundaries = new ArrayList<>(); //Номер индекса с которого идут вершины для данного полигона (старт)
    public boolean hasTexture;
    //todo: Добавить поле для самой текстуры + подумать над режимами отрисовки

    public Model(){

    }

}


package com.cgvsu.model;


import com.cgvsu.math.vectors.Vector2f;
import com.cgvsu.math.vectors.Vector3f;

import java.util.*;

public class Model {
    public String modelName;
    public ArrayList<Vertex> vertices = new ArrayList<>();
    public ArrayList<Integer> polygons = new ArrayList<>();
    public ArrayList<Integer> polygonBoundaries = new ArrayList<>();

    public Model(){

    }

}


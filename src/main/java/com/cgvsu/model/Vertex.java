package com.cgvsu.model;

import com.cgvsu.math.vectors.Vector2f;
import com.cgvsu.math.vectors.Vector3f;

public class Vertex {
    public Vector3f position;
    public Vector3f normal;
    public Vector2f textureCoordinate;

    public boolean equals(Vertex vertex) {
        return vertex.position.equals(position);
    }

    public Vertex(float x, float y, float z){
        position = new Vector3f(x, y, z);
    }

    public Vertex(){
        position = new Vector3f(0,0,0);
    }
}

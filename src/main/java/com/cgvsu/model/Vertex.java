package com.cgvsu.model;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;

public class Vertex {
    public Vector3f position;
    public Vector3f normal;
    public Vector2f textureCoordinate;

    public boolean equals(Vertex vertex) {
        return vertex.position.equals(position);
    }
}

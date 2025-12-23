package com.cgvsu.model;

import com.cgvsu.math.vectors.Vector3f;
import com.cgvsu.math.vectors.Vector2f;

public class Vertex {
    public Vector3f position;
    protected Vector3f normal;
    public Vector2f textureCoordinate;

    public boolean equals(Vertex vertex) {
        return vertex.position.equals(position);
    }
    public void setNormal (Vector3f normal) {
        this.normal = normal;
    }
    public Vector3f getNormal (Vector3f out) {
        return normal;
    }
}

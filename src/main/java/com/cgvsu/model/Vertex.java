package com.cgvsu.model;

import com.cgvsu.math.vectors.Vector2f;
import com.cgvsu.math.vectors.Vector3f;

import java.util.ArrayList;

public class Vertex {
    public Vector3f position;
    public Vector3f normal;
    public ArrayList<Vector2f> textureCoordinates;

    public boolean equals(Vertex vertex) {
        return vertex.position.equals(position);
    }
    public int getOrAddTextureCoordinate(final Vector2f uv) {
        if (uv == null) return -1;

        for (int i = 0; i < textureCoordinates.size(); i++) {
            if (textureCoordinates.get(i).equals(uv)) {
                return i;
            }
        }

        textureCoordinates.add(new Vector2f(uv.getX(), uv.getY()));
        return textureCoordinates.size() - 1;
    }

    public Vector2f getTextureCoordinate(final int localIndex) {
        if (localIndex < 0 || localIndex >= textureCoordinates.size()) return null;
        return textureCoordinates.get(localIndex);
    }
}

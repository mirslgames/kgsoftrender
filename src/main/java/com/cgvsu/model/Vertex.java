package com.cgvsu.model;

import com.cgvsu.math.vectors.Vector2f;
import com.cgvsu.math.vectors.Vector3f;

import java.util.ArrayList;

public class Vertex {
    public Vector3f position;
    public Vector3f normal;
    public ArrayList<Vector2f> textureCoordinates = new ArrayList<>();; //UV, которые использовались с этой позицией вершины

    public boolean equals(Vertex vertex) {
        return vertex.position.equals(position);
    }
<<<<<<< Updated upstream

    public Vertex(float x, float y, float z){
        position = new Vector3f(x, y, z);
    }

    public Vertex(){
        position = new Vector3f(0,0,0);
    }

=======
    public Vertex(final Vector3f position) {
        this.position = position;
        this.normal = null;
    }

    public Vertex(final float x, final float y, final float z) {
        this(new Vector3f(x, y, z));
    }

    public Vertex() {
        this(new Vector3f(0, 0, 0));
    }
>>>>>>> Stashed changes
    public int getOrAddTextureCoordinate(final Vector2f uv) {
        if (uv == null) return -1;

        //Получаем индекс UV внутри textureCoordinates
        for (int i = 0; i < textureCoordinates.size(); i++){
            if (textureCoordinates.get(i).equals(uv)) {
                return i;
            }
        }
        //если такой не было то добавляем
        textureCoordinates.add(new Vector2f(uv.getX(), uv.getY()));
        return textureCoordinates.size() - 1;
    }

    public Vector2f getTextureCoordinate(final int localIndex) {
        if (localIndex < 0 || localIndex >= textureCoordinates.size()) return null;
        return textureCoordinates.get(localIndex);
    }

    public Vertex deepCopy() {
        Vertex copy = new Vertex();

        if (this.position != null) {
            copy.position = new Vector3f(this.position.getX(), this.position.getY(), this.position.getZ());
        }
        if (this.normal != null) {
            copy.normal = new Vector3f(this.normal.getX(), this.normal.getY(), this.normal.getZ());
        }

        copy.textureCoordinates = new ArrayList<>();
        if (this.textureCoordinates != null) {
            for (Vector2f uv : this.textureCoordinates) {
                if (uv == null) {
                    copy.textureCoordinates.add(null);
                } else {
                    copy.textureCoordinates.add(new Vector2f(uv.getX(), uv.getY()));
                }
            }
        }

        return copy;
    }
}

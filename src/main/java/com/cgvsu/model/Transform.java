package com.cgvsu.model;

public class Transform {
    public float positionX;
    public float positionY;
    public float positionZ;
    public float rotationX;
    public float rotationY;
    public float rotationZ;
    public float scaleX;
    public float scaleY;
    public float scaleZ;


    public Transform(float positionX, float positionY, float positionZ, float rotationX, float rotationY,
                     float rotationZ, float scaleX, float scaleY, float scaleZ) {
        this.positionX = positionX;
        this.positionY = positionY;
        this.positionZ = positionZ;
        this.rotationX = rotationX;
        this.rotationY = rotationY;
        this.rotationZ = rotationZ;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.scaleZ = scaleZ;
    }

    public Transform deepCopy() {
        return new Transform(
                positionX, positionY, positionZ,
                rotationX, rotationY, rotationZ,
                scaleX, scaleY, scaleZ
        );
    }

}

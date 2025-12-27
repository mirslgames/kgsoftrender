package com.cgvsu.math.vectors;


public class Vector4f extends AbstractVector<Vector4f> {

    public Vector4f() {
        super(4);
    }

    public Vector4f(float x, float y, float z, float w) {
        super(new float[]{x, y, z, w});
    }

    @Override
    protected Vector4f create(float[] coordinates) {
        return new Vector4f(coordinates[0], coordinates[1], coordinates[2], coordinates[3]);
    }

    public float getX() {
        return getValue(0);
    }

    public float getY() {
        return getValue(1);
    }

    public float getZ() {
        return getValue(2);
    }

    public float getW() {
        return getValue(3);
    }

    public void setX(float x) {
        setValue(0, x);
    }

    public void setY(float y) {
        setValue(1, y);
    }

    public void setZ(float z) {
        setValue(2, z);
    }

    public void setW(float w) {
        setValue(3, w);
    }

}

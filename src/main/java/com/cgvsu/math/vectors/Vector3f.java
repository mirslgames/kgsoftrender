package com.cgvsu.math.vectors;


public class Vector3f extends AbstractVector<Vector3f> {
    public Vector3f() {
        super(3);
    }

    public Vector3f(float x, float y, float z) {
        super(new float[]{x, y, z});
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

    public void setX(float x) {
        setValue(0, x);
    }

    public void setY(float y) {
        setValue(1, y);
    }

    public void setZ(float z) {
        setValue(2, z);
    }

    public Vector3f cross(Vector3f v) {
        cross(this, v);
        return this;
    }
    public Vector3f cross(Vector3f a, Vector3f b) {
        float x = a.getY() * b.getZ() - a.getZ() * b.getY();
        float y = a.getZ() * b.getX() - a.getX() * b.getZ();
        float z = a.getX() * b.getY() - a.getY() * b.getX();
        setX(x);
        setY(y);
        setZ(z);
        return this;
    }
}


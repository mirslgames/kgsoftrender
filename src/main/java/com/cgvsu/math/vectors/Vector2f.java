package com.cgvsu.math.vectors;


public class Vector2f extends AbstractVector<Vector2f> {

    public Vector2f() {
        super(2);
    }

    public Vector2f(float x, float y) {
        super(new float[]{x, y});
    }

    public float getX() {
        return getValue(0);
    }

    public float getY() {
        return getValue(1);
    }

    public void setX(float x) {
        setValue(0, x);
    }

    public void setY(float y) {
        setValue(1, y);
    }
}

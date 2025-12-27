package com.cgvsu.math.vectors;


public class Vector2f extends AbstractVector<Vector2f> {

    public Vector2f() {
        super(2);
    }

    public Vector2f(float x, float y) {
        super(new float[]{x, y});
    }

    @Override
    protected Vector2f create(float[] coordinates) {
        return new Vector2f(coordinates[0], coordinates[1]);
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
    public Vector2f added(Vector2f v) {
        float rx = x + v.getX();
        float ry = y + v.getY();
        return new Vector2f(rx, ry);
    }

    public Vector2f subtracted(Vector2f v) {
        float rx = x - v.getX();
        float ry = y - v.getY();
        return new Vector2f(rx, ry);
    }

    public Vector2f normalized() {
        float l = len();
        return new Vector2f(x / l, y / l);
    }

    public Vector2f multipliedByScalar(float number) {
        float rx = x * number;
        float ry = y * number;
        return new Vector2f(rx, ry);
    }

    public Vector2f dividedByScalar(float number) {
        float rx =  x / number;
        float ry =  y / number;
        return new Vector2f(rx, ry);
    }

}

package com.cgvsu.math.vectors;


import com.cgvsu.math.interfaces.Vector;

public class Vector2f implements Vector, Comparable<Vector2f> {

    private float x;
    private float y;

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public Vector2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void add(Vector2f v) {
        this.x += v.getX();
        this.y += v.getY();
    }

    public void sub(Vector2f v) {
        this.x -= v.getX();
        this.y -= v.getY();
    }

    @Override
    public float len() {
        return (float) Math.sqrt(x * x + y * y);
    }

    @Override
    public void normalize() {
        float l = len();
        if (l != 0) {
            x = x / l;
            y = y / l;
        }
    }


    public float dot(Vector2f v) {
        return x * v.getX() + y * v.getY();
    }



    @Override
    public void multiplyByScalar(float number) {
        x *= number;
        y *= number;
    }

    @Override
    public void divideByScalar(float number) {
        x /= number;
        y /= number;
    }

    @Override
    public int compareTo(Vector2f o) {
        float EPS = 10e-6F;
        if (-EPS <= x - o.getX() && x - o.getX() <= EPS &&
                -EPS <= y - o.getY() && y - o.getY() <= EPS) {
            return 0;
        };
        if (x > o.getX() && y > o.getY()) {
            return 1;
        }
        return -1;
    }
}

package com.cgvsu.math.vectors;

import com.cgvsu.math.interfaces.Vector;


public class Vector2f implements Vector {
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

    public Vector2f() {
        this.x = 1;
        this.y = 1;
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

    public void add(Vector2f v) {
        x += v.getX();
        y += v.getY();
    }

    public void sub(Vector2f v) {
        x -= v.getX();
        y -= v.getY();

    }

    public void sub(Vector2f v1, Vector2f v2) {
        x = v1.getX() - v2.getX();
        y = v1.getY() - v2.getY();
    }

    public float dot(Vector2f v) {
        return x * v.getX() + y * v.getY();
    }

    @Override
    public void divideByScalar(float number) {
        x /= number;
        y /= number;
    }

    @Override
    public void multiplyByScalar(float number) {
        x *= number;
        y *= number;

    }

    public boolean equals(Vector3f o) {
        float EPS = 10e-6F;
        return  -EPS <= x - o.getX() && x - o.getX() <= EPS &&
                -EPS <= y - o.getY() && y - o.getY() <= EPS;
    }

}

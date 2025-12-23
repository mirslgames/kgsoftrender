package com.cgvsu.math.vectors;

import com.cgvsu.math.interfaces.Vector;


public class Vector4f implements Vector, Comparable<Vector4f> {
    private float x;
    private float y;
    private float z;
    private float w;

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public float getW() {
        return w;
    }

    public Vector4f(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    @Override
    public float len() {
        return (float) Math.sqrt(x * x + y * y + z * z + w * w);
    }

    @Override
    public void normalize() {
        float l = len();
        if (l != 0) {
            x = x / l;
            y = y / l;
            z = z / l;
            w = w / l;
        }
    }

    public void add(Vector4f v) {
        x += v.getX();
        y += v.getY();
        z += v.getZ();
        w += v.getW();
    }

    public void sub(Vector4f v) {
        x -= v.getX();
        y -= v.getY();
        z -= v.getZ();
        w -= v.getW();
    }

    public float dot(Vector4f v) {
        return x * v.getX() + y * v.getY() + z * v.getZ() + w * v.getW();
    }



    @Override
    public void divideByScalar(float number) {
        x /= number;
        y /= number;
        z /= number;
        w /= number;
    }

    @Override
    public void multiplyByScalar(float number) {
        x *= number;
        y *= number;
        z *= number;
        w *= number;
    }

    @Override
    public int compareTo(Vector4f o) {
        float EPS = 10e-6F;
        if (-EPS <= x - o.getX() && x - o.getX() <= EPS &&
                -EPS <= y - o.getY() && y - o.getY() <= EPS &&
                -EPS <= z - o.getZ() && z - o.getZ() <= EPS &&
                -EPS <= w - o.getW() && w - o.getW() <= EPS) {
            return 0;
        };
        if (x > o.getX() && y > o.getY() && z > o.getZ() && w > o.getW()) {
            return 1;
        }
        return -1;
    }
    public Vector4f added(Vector4f v) {
        float rx = x + v.getX();
        float ry = y + v.getY();
        float rz = z + v.getZ();
        float rw = w + v.getW();
        return new Vector4f(rx, ry,rz, rw);
    }

    public Vector4f subtracted(Vector4f v) {
        float rx = x - v.getX();
        float ry = y - v.getY();
        float rz = z - v.getZ();
        float rw = w - v.getW();
        return new Vector4f(rx, ry, rz, rw);
    }

    public Vector4f normalized() {
        float l = len();
        if (l == 0) {
            return new Vector4f(0,0,0, 0);
        }
        return new Vector4f(x / l, y / l, z / l, w / l);
    }

    public Vector4f multipliedByScalar(float number) {
        float rx = x * number;
        float ry = y * number;
        float rz = z * number;
        float rw = w * number;
        return new Vector4f(rx, ry, rz, rw);
    }

    public Vector4f dividedByScalar(float number) {
        float rx =  x / number;
        float ry =  y / number;
        float rz = z / number;
        float rw = w / number;
        return new Vector4f(rx, ry, rz, rw);
    }
}

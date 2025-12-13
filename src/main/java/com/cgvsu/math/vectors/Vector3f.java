package com.cgvsu.math.vectors;

import com.cgvsu.math.interfaces.Vector;


public class Vector3f implements Vector {
    private float x;
    private float y;
    private float z;

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public Vector3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3f() {
        this.x = 1;
        this.y = 1;
        this.z = 1;
    }

    @Override
    public float len() {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    @Override
    public void normalize() {
        float l = len();
        if (l != 0) {
            x = x / l;
            y = y / l;
            z = z / l;
        }
    }

    public void add(Vector3f v) {
        x += v.getX();
        y += v.getY();
        z += v.getZ();
    }

    public void sub(Vector3f v) {
        x -= v.getX();
        y -= v.getY();
        z -= v.getZ();
    }

    public void sub(Vector3f v1, Vector3f v2) {
        x = v1.getX() - v2.getX();
        y = v1.getY() - v2.getY();
        z = v1.getZ() - v2.getZ();
    }

    public float dot(Vector3f v) {
        return x * v.getX() + y * v.getY() + z * v.getZ();
    }

    public void cross(Vector3f v) {
        this.x = y * v.getZ() - z * v.getY();
        this.y = z * v.getX() - x * v.getZ();
        this.z = x * v.getY() - y * v.getX();
    }

    public void cross(Vector3f v1, Vector3f v2) {
        this.x = v1.getY() * v2.getZ() - v1.getZ() * v2.getY();
        this.y = v1.getZ() * v2.getX() - v1.getX() * v2.getZ();
        this.z = v1.getX() * v2.getY() - v1.getY() * v2.getX();
    }

    /* @Override
    public float[][] toMatrix() {
        return new float[][] {
                {x, 0, 0},
                {0, y, 0},
                {0, 0, z}};
    } */


    @Override
    public void divideByScalar(float number) {
        x /= number;
        y /= number;
        z /= number;
    }

    @Override
    public void multiplyByScalar(float number) {
        x *= number;
        y *= number;
        z *= number;
    }

    public boolean equals(Vector3f o) {
        float EPS = 10e-6F;
        return  -EPS <= x - o.getX() && x - o.getX() <= EPS &&
                -EPS <= y - o.getY() && y - o.getY() <= EPS &&
                -EPS <= z - o.getZ() && z - o.getZ() <= EPS;
    }


/*  public int compareTo(Vector3f o) {
        float EPS = 10e-6F;
        if (-EPS <= x - o.getX() && x - o.getX() <= EPS &&
                -EPS <= y - o.getY() && y - o.getY() <= EPS &&
                -EPS <= z - o.getZ() && z - o.getZ() <= EPS) {
            return 0;
        };
        if (x > o.getX() && y > o.getY() && z > o.getX()) {
            return 1;
        }
        return -1;
    } */
}



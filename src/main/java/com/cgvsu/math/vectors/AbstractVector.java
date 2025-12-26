package com.cgvsu.math.vectors;

public abstract class AbstractVector <T extends AbstractVector<T>>{

    private final float[] coordinates;
    protected final int size;

    public static final float EPS = 1e-6f;

    public AbstractVector(int size) {
        this.size = size;
        this.coordinates = new float[size];
        /* for (int i = 0; i < size; i++) {
            this.coordinates[i] = 1;
        } */
    }

    protected AbstractVector(float[] coordinates) {
        this.size = coordinates.length;
        this.coordinates = coordinates.clone();
    }

    protected abstract T create(float[] coordinates);

    public T copy() {
        float[] coordinates = new float[size];
        System.arraycopy(this.coordinates, 0, coordinates, 0, size);
        return create(coordinates);

    }
    public float getValue(int i) {
        return coordinates[i];
    }

    public void setValue(int index, float value) {
        coordinates[index] = value;
    }

    public float len() {
        float sum = 0;
        for (int i = 0; i < coordinates.length; i++) {
            sum += coordinates[i] * coordinates[i];
        }
        return (float) Math.sqrt(sum);
    }

    public T normalize() {
        float l = len();
        if (Math.abs(l) < EPS) {
            for (int i = 0; i < coordinates.length; i++) {
                coordinates[i] = coordinates[i] / l;
            }
        }
        return (T) this;
    }

    public T add(T v) {

        if (v.size != this.size) {
            throw new IllegalArgumentException("Different vector sizes");
        }
        for (int i = 0; i < this.size; i++) {
            coordinates[i] += v.getValue(i);
        }
        return (T) this;
    }

    public T sub(T v) {
        if (v.size != this.size) {
            throw new IllegalArgumentException("Different vector sizes");
        }
        for (int i = 0; i < this.size; i++) {
            coordinates[i] -= v.getValue(i);
        }
        return (T) this;
    }

    public float dot(T v) {
        if (v.size != this.size) {
            throw new IllegalArgumentException("Different vector sizes");
        }
        float res = 0;
        for (int i = 0; i < this.size; i++) {
            res += v.getValue(i) * coordinates[i];
        }
        return res;
    }

    public T divide(float number) {
        if (Math.abs(number) < EPS) {
            throw new IllegalArgumentException("Divide by zero");
        }
        for (int i = 0; i < size; i++) {
            coordinates[i] /= number;
        }
        return (T) this;
    }

    public T multiply(float number) {
        for (int i = 0; i < size; i++) {
            coordinates[i] *= number;
        }
        return (T) this;

    }

    public T add(T a, T b) {
        if (a.size != b.size || this.size != a.size) {
            throw new IllegalArgumentException("Different vector sizes");
        }
        for (int i = 0; i < a.size; i++) {
            coordinates[i] = a.getValue(i) + b.getValue(i);
        }
        return (T) this;
    }

    public T sub(T a, T b) {
        if (a.size != b.size || this.size != a.size) {
            throw new IllegalArgumentException("Different vector sizes");
        }
        for (int i = 0; i < a.size; i++) {
            coordinates[i] = a.getValue(i) - b.getValue(i);
        }
        return (T) this;
    }

    public T normalized() {
        T vector = this.copy();
        return vector.normalize();
    }

    public T added(T elem) {
        T vector = this.copy();
        return vector.add(elem);
    }

    public T subbed(T elem) {
        T vector = this.copy();
        return vector.sub(elem);
    }

    public T divided(float number) {
        T vector = this.copy();
        return vector.divide(number);
    }

    public T multiplied(float number) {
        T vector = this.copy();
        return vector.multiply(number);
    }
}

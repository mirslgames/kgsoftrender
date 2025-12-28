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

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractVector<?> other = (AbstractVector<?>) o;

        if (this.size != other.size) return false;

        for (int i = 0; i < size; i++) {
            if (Float.compare(this.getValue(i), other.getValue(i)) != 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public final int hashCode() {
        int result = getClass().hashCode();
        result = 31 * result + size;
        for (int i = 0; i < size; i++) {
            result = 31 * result + Float.hashCode(getValue(i));
        }
        return result;
    }

    public final boolean equalsEps(AbstractVector<?> other, float eps) {
        if (other == null) return false;
        if (this.size != other.size) return false;
        if (!this.getClass().equals(other.getClass())) return false;

        for (int i = 0; i < size; i++) {
            if (Math.abs(this.getValue(i) - other.getValue(i)) > eps) {
                return false;
            }
        }
        return true;
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
        if (Math.abs(l) > EPS) {
            for (int i = 0; i < coordinates.length; i++) {
                coordinates[i] = coordinates[i] / l;
            }
        }
        return (T) this;
    }

    public T add(T v) {
        return add((T) this, v);
    }

    public T sub(T v) {
        return sub((T) this, v);
    }

    public float dot(T v) {
        return dot((T) this, v);
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

    public float dot(T a, T b) {
        if (a.size != b.size) {
            throw new IllegalArgumentException("Different vector sizes");
        }
        float res = 0;
        for (int i = 0; i < a.size; i++) {
            res += a.getValue(i) * b.getValue(i);
        }
        return res;
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

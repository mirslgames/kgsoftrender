package com.cgvsu.math.matrixs;

public abstract class AbstractMatrix<T extends AbstractMatrix<T>> {
    protected final int size;
    protected float[][] matrix;

    public static final float EPS = 1e-6f;

    public AbstractMatrix(int size) {
        this.size = size;
        float[][] matrix = new float[size][size];

        for (int i = 0; i < size; i++) {
            matrix[i][i] = 1;
        }
        this.matrix = matrix;
    }

    public AbstractMatrix(float[][] matrix) {
        if (matrix == null) {
            throw new NullPointerException("Matrix is null");
        }
        if (matrix.length == 0) {
            throw new IllegalArgumentException("Matrix is empty");
        }
        for (float[] floats : matrix) {
            if (floats == null) {
                throw new NullPointerException("Row is null");
            }
            if (floats.length != matrix.length) {
                throw new IllegalArgumentException("Matrix must be square");
            }
        }
        this.size = matrix.length;
        this.matrix = new float[size][size];
        for (int i = 0; i < size; i++) {
            System.arraycopy(matrix[i], 0, this.matrix[i], 0, size);
        }
    }

    public AbstractMatrix(float[] array) {
        if (array == null) {
            throw new NullPointerException("Array is null");
        }
        this.size = (int) Math.sqrt(array.length);
        if (size * size != array.length) {
            throw new IllegalArgumentException(String.format("Length of current array is not %d", size * size));
        }
        float[][] m = new float[size][size];
        for (int i = 0; i < size; i++) {
            System.arraycopy(array, i * size, m[i], 0, size);
        }
        this.matrix = m;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractMatrix<?> other = (AbstractMatrix<?>) o;
        if (this.size != other.size) return false;

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (Float.compare(this.matrix[i][j], other.matrix[i][j]) != 0) return false;
            }
        }
        return true;
    }

    @Override
    public final int hashCode() {
        int result = getClass().hashCode();
        result = 31 * result + size;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                result = 31 * result + Float.hashCode(matrix[i][j]);
            }
        }
        return result;
    }

    public final boolean equalsEps(AbstractMatrix<?> other, float eps) {
        if (other == null) return false;
        if (this.size != other.size) return false;
        if (!this.getClass().equals(other.getClass())) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (Math.abs(this.matrix[i][j] - other.matrix[i][j]) > eps) {
                    return false;
                }
            }
        }
        return true;
    }

    public final boolean equalsEps(AbstractMatrix<?> other) {
        return equalsEps(other, EPS);
    }

    protected abstract T create(float[][] matrix);

    public T copy() {
        return create(getMatrix());
    }

    public float[][] getMatrix() {
        float[][] m = new float[size][size];
        for (int i = 0; i < size; i++) {
            System.arraycopy(matrix[i], 0, m[i], 0, size);
        }
        return m;
    }

    public void setValue(int row, int column, float value) {
        matrix[row][column] = value;
    }

    public float getValue(int row, int column) {
        return matrix[row][column];
    }

    public T add(T elem) {
        return add((T) this, elem);
    }

    public T sub(T elem) {
        return sub((T) this, elem);
    }

    public T multiply(T elem) {
        return multiply((T) this, elem);
    }

    public T transpose() {
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                float tmp = matrix[i][j];
                matrix[i][j] = matrix[j][i];
                matrix[j][i] = tmp;
            }
        }
        return (T) this;
    }

    public T multiply(T a, T b) {
        if (a.size != b.size || this.size != a.size) {
            throw new IllegalArgumentException("Different matrix sizes");
        }
        float[][] res = new float[a.size][a.size];
        for (int i = 0; i < a.size; i++) {
            for (int j = 0; j < a.size; j++) {
                float sum = 0;
                for (int k = 0; k < a.size; k++) {
                    sum += a.getValue(i, k) * b.getValue(k, j);
                }
                res[i][j] = sum;
            }
        }
        this.matrix = res;
        return (T) this;
    }

    public T sub(T a, T b) {
        if (a.size != b.size || this.size != a.size) {
            throw new IllegalArgumentException("Different matrix sizes");
        }
        for (int i = 0; i < a.size; i++) {
            for (int j = 0; j < a.size; j++) {
                setValue(i, j, a.getValue(i, j) - b.getValue(i, j));
            }
        }
        return (T) this;
    }

    public T add(T a, T b) {
        if (a.size != b.size || this.size != a.size) {
            throw new IllegalArgumentException("Different matrix sizes");
        }
        for (int i = 0; i < a.size; i++) {
            for (int j = 0; j < a.size; j++) {
                setValue(i, j, a.getValue(i, j) + b.getValue(i, j));
            }
        }
        return (T) this;
    }

    public T transposed() {
        T matrix = this.copy();
        return matrix.transpose();
    }

    public T added(T elem) {
        T matrix = this.copy();
        return matrix.add(elem);
    }

    public T subbed(T elem) {
        T matrix = this.copy();
        return matrix.sub(elem);
    }

    public T multiplied(T elem) {
        T matrix = this.copy();
        return matrix.multiply(elem);
    }
}

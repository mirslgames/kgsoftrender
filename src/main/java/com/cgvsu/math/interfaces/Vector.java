package com.cgvsu.math.interfaces;

public interface Vector {


    default void divideByScalar(float number) {
        return;
    }

    default void multiplyByScalar(float number) {
        return;
    }


    default float len() {
        return 0;
    }

    default void normalize() {
        return;
    }
}

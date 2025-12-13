package com.cgvsu.math.interfaces;

public interface Vector {

    /*public default float[][] toMatrix() {
        return new float[0];
    }*/

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

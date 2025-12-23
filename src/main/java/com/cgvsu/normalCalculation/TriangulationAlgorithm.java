package com.cgvsu.normalCalculation;


import com.cgvsu.model.Vertex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class TriangulationAlgorithm {
    public static List<List<Integer>> triangulate( List<Integer> polygon){
        int n = polygon.size();

        if (n % 2 == 1) {
            // Для нечетного - дублируем последнюю вершину
            polygon.add(n - 1);
            return triangulateEven(polygon);
        } else {
            return triangulateEven( polygon);
        }
    }
    public static List<List<Integer>> triangulateEven(List<Integer> polygon) {
        List<List<Integer>> triangles = new ArrayList<>();

        int n = polygon.size();

        for (int step = 1; step < n; step *= 2) {
            for (int i = 0; i < n-2*step+1; i += step * 2) {
                if (i + step < n) {
                    int i1 = i;
                    int i2 = i + step;
                    int i3 = (i + 2 * step) % n;

                    if (!polygon.get(i1).equals(polygon.get(i2)) &&
                            !polygon.get(i2).equals(polygon.get(i3)) &&
                            !polygon.get(i1).equals(polygon.get(i3))) {

                        triangles.add(Arrays.asList(i1, i2, i3));
                    }
                }
            }
        }

        return triangles;
    }

}

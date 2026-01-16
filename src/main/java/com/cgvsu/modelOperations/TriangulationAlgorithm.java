package com.cgvsu.modelOperations;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TriangulationAlgorithm {

    public static List<List<Integer>> triangulate(List<Integer> polygon){
        List<Integer> copy = new ArrayList<>(polygon);
        int n = copy.size();
        if (n % 2 == 1) {
            copy.add(copy.get(n - 1)); // дублируем последний индекс вершины
        }
        return triangulateEven(copy);
    }
    public static List<List<Integer>> triangulateEven(List<Integer> polygon) {
        List<List<Integer>> triangles = new ArrayList<>();

        int n = polygon.size();
        for (int step = 1; step < n; step *= 2) {
            for (int i = 0; i < n-2*step+1; i += step * 2) {
                if (i + step < n) {
                    int i1 = polygon.get(i); // Берем индекс вершины из списка полигона
                    int i2 = polygon.get(i + step);
                    int i3 = polygon.get((i + 2 * step) % n);

                    if (i1 != i2 && i2 != i3 && i1 != i3) {
                        triangles.add(Arrays.asList(i1, i2, i3));
                    }
                }
            }
        }

        return triangles;
    }

    /**
     * НОВОЕ: триангуляция, которая возвращает позиции внутри полигона (0..N-1), а не "значения" индексов.
     *
     * Это нужно, когда у полигона несколько параллельных списков индексов (v и vt),
     * и мы хотим триангулировать их синхронно без рассинхронизации.
     */
    public static List<List<Integer>> triangulatePositions(final int polygonVertexCount) {
        List<Integer> positions = new ArrayList<>(polygonVertexCount + 1);
        for (int i = 0; i < polygonVertexCount; i++) {
            positions.add(i);
        }

        // Повторяем старую логику: если нечётное количество вершин — дублируем последнюю.
        if (polygonVertexCount % 2 == 1 && polygonVertexCount > 0) {
            positions.add(polygonVertexCount - 1);
        }

        return triangulateEven(positions);
    }

}

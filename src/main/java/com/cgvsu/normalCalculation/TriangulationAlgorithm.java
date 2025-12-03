package com.cgvsu.traingulation;

import com.cgvsu.model.Polygon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class TriangulationAlgorithm {
    public static List<Polygon> triangulate(Polygon polygon) {
        List<Integer> vertices = polygon.getVertexIndices();
        List<Integer> textureVertices = polygon.getTextureVertexIndices();
        List<Integer> normals = polygon.getNormalIndices();

        int n = vertices.size();

        if (n % 2 == 1) {
            // Для нечетного - дублируем последнюю вершину
            vertices.add(vertices.get(n - 1));
            return triangulateEven(new Polygon(vertices,textureVertices,normals));
        } else {
            return triangulateEven(polygon);
        }
    }
//    public List<Polygon> triangulate(Polygon polygon) {
//        List<Polygon> polygons = new ArrayList<>();
//        List<Integer> vertices = polygon.getVertexIndices();
//        List<Integer> textures = polygon.getTextureVertexIndices();
//        List<Integer> normals = polygon.getNormalIndices();
//
//        int size = vertices.size();
//        boolean hasTextures = textures != null && !textures.isEmpty();
//        boolean hasNormals = normals != null && !normals.isEmpty();
//
//
//        for (int i = 0; i < size - 2; i += 2) {
//            List<Integer> vertexIndices = Arrays.asList(
//                    vertices.get(i),
//                    vertices.get(i + 1),
//                    vertices.get(i + 2)
//            );
//
//            List<Integer> vertexTexIndices = hasTextures ? Arrays.asList(
//                    textures.get(i),
//                    textures.get(i + 1),
//                    textures.get(i + 2)
//            ) : null;
//
//            List<Integer> normalIndices = hasNormals ? Arrays.asList(
//                    normals.get(i),
//                    normals.get(i + 1),
//                    normals.get(i + 2)
//            ) : null;
//
//            polygons.add(new Polygon(vertexIndices, vertexTexIndices, normalIndices));
//        }
//
//
//        if (size % 2 == 1) {
//            int last = size - 1;
//            List<Integer> vertexIndices = Arrays.asList(
//                    vertices.get(0),
//                    vertices.get(last - 1),
//                    vertices.get(last)
//            );
//
//            List<Integer> vertexTexIndices = hasTextures ? Arrays.asList(
//                    textures.get(0),
//                    textures.get(last - 1),
//                    textures.get(last)
//            ) : null;
//
//            List<Integer> normalIndices = hasNormals ? Arrays.asList(
//                    normals.get(0),
//                    normals.get(last - 1),
//                    normals.get(last)
//            ) : null;
//
//            polygons.add(new Polygon(vertexIndices, vertexTexIndices, normalIndices));
//        } else {
//
//        }
//
//        return polygons;
//    }
    public static List<Polygon> triangulateEven(Polygon polygon) {
        List<Polygon> triangles = new ArrayList<>();
        List<Integer> vertices = polygon.getVertexIndices();
        List<Integer> textures = polygon.getTextureVertexIndices();
        List<Integer> normals = polygon.getNormalIndices();

        int n = vertices.size();
        boolean hasTextures = textures != null && !textures.isEmpty();
        boolean hasNormals = normals != null && !normals.isEmpty();
        boolean[] verticesArray = new boolean[n];
        for (int step = 1; step < n; step *= 2) {
            for (int i = 0; i < n-2*step+1; i += step * 2) {
                if (i + step < n) {
                    int i1 = i;
                    int i2 = i + step;
                    int i3 = (i + 2 * step) % n;

                    if (!vertices.get(i1).equals(vertices.get(i2)) &&
                            !vertices.get(i2).equals(vertices.get(i3)) &&
                            !vertices.get(i1).equals(vertices.get(i3))) {

                        List<Integer> triVertices = Arrays.asList(
                                vertices.get(i1), vertices.get(i2), vertices.get(i3)
                        );

                        List<Integer> triTextures = null;
                        if (hasTextures && i3 < textures.size()) {
                            triTextures = Arrays.asList(
                                    textures.get(i1), textures.get(i2), textures.get(i3)
                            );
                        }

                        List<Integer> triNormals = null;
                        if (hasNormals && i3 < normals.size()) {
                            triNormals = Arrays.asList(
                                    normals.get(i1), normals.get(i2), normals.get(i3)
                            );
                        }

                        triangles.add(new Polygon(triVertices, triTextures, triNormals));
                    }
                }
            }
        }

        return triangles;
    }
    public static void main(String[] args) {
        List<Integer> v = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10,11);
        List<Integer> vt = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10,11);
        List<Integer> nv = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10,11);
        Polygon polygon = new Polygon(v,vt,nv);
        List<Polygon> res = triangulate(polygon);
        for (Polygon p : res) {
            for (int i = 0; i < p.getVertexIndices().size(); i++) {
                System.out.print(p.getVertexIndices().get(i)-1);
                //System.out.print(' ');
                //System.out.print(p.getTextureVertexIndices().get(i)-1);
                //System.out.print(' ');
                System.out.print(p.getNormalIndices().get(i)-1);
                //System.out.print("://");
                System.out.print(' ');
                //System.out.println();
            }
            System.out.println("***");
        }
//        List<Integer> v = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10,11);
//        List<Integer> vt = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10,11);
//        List<Integer> nv = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10,11);
//        Polygon polygon = new Polygon(v,vt,nv);
//        List<Polygon> res = triangulate(polygon);
//        for (Polygon p : res) {
//            for (int i = 0; i < p.getVertexIndices().size(); i++) {
//                System.out.print(p.getVertexIndices().get(i)-1);
//                System.out.print(' ');
//                //System.out.print(p.getTextureVertexIndices().get(i)-1);
//                //System.out.print(' ');
//                //System.out.print(p.getNormalIndices().get(i)-1);
//                //System.out.print("://");
//                //System.out.println();
//            }
//            System.out.println("***");
//        }
//    }
    }
}

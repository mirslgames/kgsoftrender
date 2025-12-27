package com.cgvsu.modelOperations;

import com.cgvsu.math.vectors.Vector3f;

import com.cgvsu.model.Vertex;


import java.util.List;

public class NormalCalculation {


//    public static Vector3f getModelCenter(List<Vector3f> vertices) {
//        Vector3f sum = new Vector3f(0, 0, 0);
//        for (Vector3f v : vertices) {
//            sum = sum.add(v);
//        }
//        return sum.divide(vertices.size());
//    }
    public static Vector3f calculatePolygonNormal(List<Vertex> vertices, List<Integer> polygon, int start, int end) {
        if (start - end + 1 < 3) return new Vector3f(0, 0, 0);

        int i1 = polygon.get(start);
        int i2 = polygon.get(start + 1);
        int i3 = polygon.get(start + 2);

        if (i1 >= vertices.size() || i2 >= vertices.size() || i3 >= vertices.size()) {
            return new Vector3f(0, 0, 0);
        }

        Vertex v1 = vertices.get(i1);
        Vertex v2 = vertices.get(i2);
        Vertex v3 = vertices.get(i3);

        Vector3f pos1 = v1.position;
        Vector3f pos2 = v2.position;
        Vector3f pos3 = v3.position;
        Vector3f edge1 = pos2.subtracted(pos1);
        Vector3f edge2 = pos3.subtracted(pos1);
        Vector3f normal = edge1.crossed(edge2);


        if (normal.len() < 1e-6f) {
            float ref = (float)(1.0 / Math.sqrt(3.0));
            normal = new Vector3f(ref, ref, ref);
        } else {
            normal.normalize();
        }
        return normal;
    }

//    public static Vector3f ensureRightHanded(Vector3f v1, Vector3f v2, Vector3f v3, Vector3f normal) {
//
//        if (!isRightHanded(v1, v2, v3)) {
//            normal = normal.multiply(-1);
//        }
//        return normal.normalize();
//    }

    public static boolean isRightHanded(Vector3f v1, Vector3f v2, Vector3f v3) {

        Vector3f edge1 = v2.subbed(v1);
        Vector3f edge2 = v3.subbed(v1);

        Vector3f normal = edge1.crossed(edge2);
        Vector3f reference = new Vector3f(0, 1, 0);

        return normal.dot(reference) >= 0;
    }
    public static float computePolygonArea( List<Vertex> vertices, List<Integer> polygon, int start, int end) {
        if (end - start + 1 < 3) return 0f;

        int i1 = polygon.get(start);
        int i2 = polygon.get(start + 1);
        int i3 = polygon.get(start + 2);

        if (i1 >= vertices.size() || i2 >= vertices.size() || i3 >= vertices.size()) {

            return 0f;
        }

        Vector3f v1 = vertices.get(i1).position;
        Vector3f v2 = vertices.get(i2).position;
        Vector3f v3 = vertices.get(i3).position;

        Vector3f edge1 = v2.subbed(v1);
        Vector3f edge2 = v3.subbed(v1);
        return edge1.crossed(edge2).len() * 0.5f;
    }
//    public static void main(String[] args) throws IOException, ReaderException {
//        Path fileName = Path.of("D:/SecondProject/Task3/ObjNormalCreate/src/com/cgvsu/Penguin.obj");
//        System.out.println("EXISTS=" + Files.exists(fileName) + ", FILE=" + fileName.toAbsolutePath());
//        if (!Files.exists(fileName)) {
//            System.err.println("Ошибка: файл не найден! Проверь путь!");
//            return;
//        } else {
//            System.out.println("FILE SIZE=" + Files.size(fileName));
//        }
//        String fileContent = Files.readString(fileName);
//        System.out.println("fileContent length=" + fileContent.length());
//        if (fileContent.length() > 0) {
//           System.out.println("fileContent head: '" + fileContent.substring(0, Math.min(200, fileContent.length())) + "'");
//        }
//
//        System.out.println("Loading model ...");
//        ModelReader reader = ModelReaderFactory.create("obj");
//        Model model = reader.read(fileContent);
//        System.out.println("Normals: " + model.normals.size());
//
//        if (!model.polygons.isEmpty()) {
//            System.out.println("First polygon indices: " + model.normals.size());
//            System.out.println("Vertices count: " + model.vertices.size());
//        }
//        ModelReassured modelFixed = new ModelReassured(model);
//        System.out.println("Normals (recomputed): " + modelFixed.normals.size());
//        System.out.println();
//
//        float EPS = 0.001f;
//        MyVertexNormalCalc calc = new MyVertexNormalCalc();
//        List<Vector3f> recomputedVertexNormals = calc.calculateVertexNormals(model);
//        int polyLimit = 15;
//        System.out.println("\n=== Сравнение нормалей вершин для первых " + polyLimit + " полигонов ===\n");
//        for (int polyIdx = 0; polyIdx < Math.min(polyLimit, model.polygons.size()); polyIdx++) {
//            Polygon poly = model.polygons.get(polyIdx);
//            List<Integer> vIdx = poly.getVertexIndices();
//            List<Integer> nIdx = poly.getNormalIndices();
//            if (vIdx.size() != nIdx.size()) {
//                System.out.printf("[ПОЛИГОН %d] Ошибка: количество вершин (%d) и нормалей (%d) не совпадает!\n", polyIdx, vIdx.size(), nIdx.size());
//                continue;
//            }
//            for (int vi = 0; vi < vIdx.size(); vi++) {
//                int vertIdx = vIdx.get(vi);
//                int normIdx = nIdx.get(vi);
//                if (vertIdx < 0 || vertIdx >= recomputedVertexNormals.size() || normIdx < 0 || normIdx >= model.normals.size()) {
//                    System.out.printf("  [vertex#%d] Индекс вне диапазона: v=%d, n=%d\n", vi, vertIdx, normIdx);
//                    continue;
//                }
//                Vector3f rec = recomputedVertexNormals.get(vertIdx);
//                Vector3f orig = model.normals.get(normIdx);
//                Vector3f diff = rec.subtract(orig);
//                float dist = diff.length();
//                boolean ok = dist < EPS;
//                float dot = rec.normalize().dot(orig.normalize());
//                System.out.printf("Dot=%.3f | ", dot);
//                System.out.printf("Полигон %d, вершина %d: Ваша нормаль %s | Оригинал %s | Dist=%.6f | OK=%b\n", polyIdx
//                        , vertIdx, rec.getX() + " " + rec.getY() + " " + rec.getZ(),
//                        orig.getX() + " " +orig.getY() + " " + orig.getZ(), dist, ok);
//            }
//
//        }
//        List<Vector3f> normals = calc.calculateVertexNormals(model);
//        Model newModel = new Model();
//        newModel.textureVertices = model.textureVertices;
//        newModel.polygons = model.polygons;
//        newModel.vertices = model.vertices;
//        newModel.normals = new ArrayList<>(normals);
//
//        Path fileNameRes = Path.of("D:/SecondProject/Task3/ObjNormalCreate/src/com/cgvsu/Penguin_test.obj");
//        ObjWriter.writeModelToFile(newModel,fileNameRes.toString());
//    }
}

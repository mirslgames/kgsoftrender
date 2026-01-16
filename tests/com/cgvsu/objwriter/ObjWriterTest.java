package com.cgvsu.objwriter;

import com.cgvsu.math.vectors.Vector2f;
import com.cgvsu.math.vectors.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Vertex;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ObjWriterTest {

    @TempDir
    Path tempDir;

    private static String v(float x, float y, float z) {
        return String.format(Locale.US, "v %f %f %f", x, y, z);
    }

    private static String vn(float x, float y, float z) {
        return String.format(Locale.US, "vn %f %f %f", x, y, z);
    }

    private static String vt(float u, float v) {
        return String.format(Locale.US, "vt %f %f", u, v);
    }

    @Test
    public void testWritingModelVVn() throws IOException {
        Model model = new Model();
        model.modelName = "testModelPyramid";
        model.vertices = new ArrayList<>();
        model.polygons = new ArrayList<>();
        model.polygonsBoundaries = new ArrayList<>();

        Vertex v1 = new Vertex(-1, -1, 0);
        Vertex v2 = new Vertex(-1, 1, 0);
        Vertex v3 = new Vertex(1, 1, 0);
        Vertex v4 = new Vertex(1, -1, 0);
        Vertex v5 = new Vertex(0, 0, 1);

        // нормали (чтобы тест не зависел от MyVertexNormalCalc)
        v1.normal = new Vector3f(0, 0, -1);
        v2.normal = new Vector3f(0, 0, -1);
        v3.normal = new Vector3f(0, 0, -1);
        v4.normal = new Vector3f(0, 0, -1);
        v5.normal = new Vector3f(0, 0,  1);

        model.vertices.add(v1);
        model.vertices.add(v2);
        model.vertices.add(v3);
        model.vertices.add(v4);
        model.vertices.add(v5);

        model.polygonsBoundaries.add(0);
        model.polygons.addAll(List.of(0, 1, 2, 3));

        model.polygonsBoundaries.add(4);
        model.polygons.addAll(List.of(0, 1, 4));

        model.polygonsBoundaries.add(7);
        model.polygons.addAll(List.of(1, 2, 4));

        model.polygonsBoundaries.add(10);
        model.polygons.addAll(List.of(2, 3, 4));

        model.polygonsBoundaries.add(13);
        model.polygons.addAll(List.of(3, 0, 4));

        Path outFile = tempDir.resolve("pyramid.obj");

        boolean ok = ObjWriter.writeModelToFile(model, outFile.toString());

        assertTrue(ok,
                "writeModelToFile(): вернул false — запись модели не удалась (файл не был создан или запись прервана).");

        assertTrue(Files.exists(outFile),
                "Файл после записи не найден: " + outFile + " (ожидалось, что ObjWriter создаст файл).");

        List<String> actual = Files.readAllLines(outFile);

        List<String> expected = new ArrayList<>();
        expected.add("# Exported by ObjWriter");
        expected.add("# Model: testModelPyramid");
        expected.add("");

        expected.add(v(-1, -1, 0));
        expected.add(v(-1,  1, 0));
        expected.add(v( 1,  1, 0));
        expected.add(v( 1, -1, 0));
        expected.add(v( 0,  0, 1));
        expected.add("");

        expected.add(vn(0, 0, -1));
        expected.add(vn(0, 0, -1));
        expected.add(vn(0, 0, -1));
        expected.add(vn(0, 0, -1));
        expected.add(vn(0, 0,  1));
        expected.add("");

        expected.add("f 1//1 2//2 3//3 4//4");
        expected.add("f 1//1 2//2 5//5");
        expected.add("f 2//2 3//3 5//5");
        expected.add("f 3//3 4//4 5//5");
        expected.add("f 4//4 1//1 5//5");

        assertEquals(expected.size(), actual.size(),
                "Разное количество строк в файле.\n" +
                        "Ожидалось: " + expected.size() + "\n" +
                        "Получилось: " + actual.size());

        for (int i = 0; i < expected.size(); i++) {
            String exp = expected.get(i);
            String act = actual.get(i);

            assertEquals(exp, act,
                    "Несовпадение в строке №" + (i + 1) + "\n" +
                            "Ожидалось: " + exp + "\n" +
                            "Получилось: " + act);
        }
    }

    @Test
    public void testWritingModelVVnVt() throws IOException {
        Model model = new Model();
        model.modelName = "testModelPyramid";
        model.hasTexture = true;

        model.vertices = new ArrayList<>();
        model.polygons = new ArrayList<>();
        model.polygonsBoundaries = new ArrayList<>();
        model.polygonsTextureCoordinateIndices = new ArrayList<>();

        Vertex v1 = new Vertex(-1, -1, 0);
        Vertex v2 = new Vertex(-1,  1, 0);
        Vertex v3 = new Vertex( 1,  1, 0);
        Vertex v4 = new Vertex( 1, -1, 0);
        Vertex v5 = new Vertex( 0,  0, 1);

        v1.normal = new Vector3f(0, 0, -1);
        v2.normal = new Vector3f(0, 0, -1);
        v3.normal = new Vector3f(0, 0, -1);
        v4.normal = new Vector3f(0, 0, -1);
        v5.normal = new Vector3f(0, 0,  1);

        //Добавляем UV для каждой вершины
        v1.getOrAddTextureCoordinate(new Vector2f(0.0f, 0.0f));
        v2.getOrAddTextureCoordinate(new Vector2f(0.0f, 1.0f));
        v3.getOrAddTextureCoordinate(new Vector2f(1.0f, 1.0f));
        v4.getOrAddTextureCoordinate(new Vector2f(1.0f, 0.0f));
        v5.getOrAddTextureCoordinate(new Vector2f(0.5f, 0.5f));

        model.vertices.add(v1);
        model.vertices.add(v2);
        model.vertices.add(v3);
        model.vertices.add(v4);
        model.vertices.add(v5);

        //На каждый добавленный индекс вершины (угол) локальный индекс uv будет 0
        model.polygonsBoundaries.add(0);
        model.polygons.addAll(List.of(0, 1, 2, 3));
        model.polygonsTextureCoordinateIndices.addAll(List.of(0, 0, 0, 0));

        model.polygonsBoundaries.add(4);
        model.polygons.addAll(List.of(0, 1, 4));
        model.polygonsTextureCoordinateIndices.addAll(List.of(0, 0, 0));

        model.polygonsBoundaries.add(7);
        model.polygons.addAll(List.of(1, 2, 4));
        model.polygonsTextureCoordinateIndices.addAll(List.of(0, 0, 0));

        model.polygonsBoundaries.add(10);
        model.polygons.addAll(List.of(2, 3, 4));
        model.polygonsTextureCoordinateIndices.addAll(List.of(0, 0, 0));

        model.polygonsBoundaries.add(13);
        model.polygons.addAll(List.of(3, 0, 4));
        model.polygonsTextureCoordinateIndices.addAll(List.of(0, 0, 0));

        Path outFile = tempDir.resolve("pyramid_with_vt.obj");

        boolean ok = ObjWriter.writeModelToFile(model, outFile.toString());

        assertTrue(ok, "writeModelToFile(): вернул false");
        assertTrue(Files.exists(outFile), "Файл после записи не найден: " + outFile);

        List<String> actual = Files.readAllLines(outFile);

        List<String> expected = new ArrayList<>();
        expected.add("# Exported by ObjWriter");
        expected.add("# Model: testModelPyramid");
        expected.add("");

        expected.add(v(-1, -1, 0));
        expected.add(v(-1,  1, 0));
        expected.add(v( 1,  1, 0));
        expected.add(v( 1, -1, 0));
        expected.add(v( 0,  0, 1));
        expected.add("");

        expected.add(vt(0.0f, 0.0f));
        expected.add(vt(0.0f, 1.0f));
        expected.add(vt(1.0f, 1.0f));
        expected.add(vt(1.0f, 0.0f));

        expected.add(vt(0.0f, 0.0f));
        expected.add(vt(0.0f, 1.0f));
        expected.add(vt(0.5f, 0.5f));

        expected.add(vt(0.0f, 1.0f));
        expected.add(vt(1.0f, 1.0f));
        expected.add(vt(0.5f, 0.5f));

        expected.add(vt(1.0f, 1.0f));
        expected.add(vt(1.0f, 0.0f));
        expected.add(vt(0.5f, 0.5f));

        expected.add(vt(1.0f, 0.0f));
        expected.add(vt(0.0f, 0.0f));
        expected.add(vt(0.5f, 0.5f));
        expected.add("");

        expected.add(vn(0, 0, -1));
        expected.add(vn(0, 0, -1));
        expected.add(vn(0, 0, -1));
        expected.add(vn(0, 0, -1));
        expected.add(vn(0, 0,  1));
        expected.add("");

        expected.add("f 1/1/1 2/2/2 3/3/3 4/4/4");
        expected.add("f 1/5/1 2/6/2 5/7/5");
        expected.add("f 2/8/2 3/9/3 5/10/5");
        expected.add("f 3/11/3 4/12/4 5/13/5");
        expected.add("f 4/14/4 1/15/1 5/16/5");

        assertEquals(expected.size(), actual.size(),
                "Разное количество строк в файле.\nОжидалось: " + expected.size() + "\nПолучилось: " + actual.size());

        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), actual.get(i),
                    "Несовпадение в строке №" + (i + 1) +
                            "\nОжидалось: " + expected.get(i) +
                            "\nПолучилось: " + actual.get(i));
        }
    }

}

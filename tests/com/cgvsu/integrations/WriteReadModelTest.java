package com.cgvsu.integrations;

import com.cgvsu.math.vectors.Vector2f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Vertex;
import com.cgvsu.objreader.ObjReader;
import com.cgvsu.objwriter.ObjWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WriteReadModelTest {

    @TempDir
    Path tempDir;

    private static final float EPS = 1e-5f;

    private Path copyResourceToTemp(String resourceName) throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            assertNotNull(is, "Не найден тестовый файл в resources: " + resourceName);
            Path dst = tempDir.resolve(resourceName);
            Files.copy(is, dst, StandardCopyOption.REPLACE_EXISTING);
            return dst;
        }
    }

    private static Model read(Path path) throws Exception {
        String content = Files.readString(path);
        return ObjReader.readModelFromFile(content, path.getFileName().toString(), new HashMap<>());
    }

    private static boolean allHaveNormals(Model m) {
        for (Vertex v : m.vertices) if (v.normal == null) return false;
        return true;
    }


    private static void assertModelsEqual(Model expected, Model actual) {
        assertNotNull(expected);
        assertNotNull(actual);

        assertNotNull(expected.vertices);
        assertNotNull(actual.vertices);
        assertEquals(expected.vertices.size(), actual.vertices.size(), "Разное количество вершин");

        boolean compareNormals = allHaveNormals(expected) && allHaveNormals(actual);
        boolean compareTex = expected.getHasTextureVertex() && actual.getHasTextureVertex();

        for (int i = 0; i < expected.vertices.size(); i++) {
            Vertex e = expected.vertices.get(i);
            Vertex a = actual.vertices.get(i);

            assertNotNull(e.position);
            assertNotNull(a.position);

            assertEquals(e.position.getX(), a.position.getX(), EPS, "X вершины " + (i + 1));
            assertEquals(e.position.getY(), a.position.getY(), EPS, "Y вершины " + (i + 1));
            assertEquals(e.position.getZ(), a.position.getZ(), EPS, "Z вершины " + (i + 1));

            if (compareNormals) {
                assertNotNull(e.normal);
                assertNotNull(a.normal);
                assertEquals(e.normal.getX(), a.normal.getX(), EPS, "NX вершины " + (i + 1));
                assertEquals(e.normal.getY(), a.normal.getY(), EPS, "NY вершины " + (i + 1));
                assertEquals(e.normal.getZ(), a.normal.getZ(), EPS, "NZ вершины " + (i + 1));
            }
        }

        assertNotNull(expected.polygons);
        assertNotNull(actual.polygons);
        assertEquals(expected.polygons.size(), actual.polygons.size(), "Разный размер списка индексов полигонов");
        for (int i = 0; i < expected.polygons.size(); i++) {
            assertEquals(expected.polygons.get(i), actual.polygons.get(i), "Индекс полигона в позиции " + i);
        }

        assertNotNull(expected.polygonsBoundaries);
        assertNotNull(actual.polygonsBoundaries);
        assertEquals(expected.polygonsBoundaries.size(), actual.polygonsBoundaries.size(), "Разный размер boundaries");
        for (int i = 0; i < expected.polygonsBoundaries.size(); i++) {
            assertEquals(expected.polygonsBoundaries.get(i), actual.polygonsBoundaries.get(i), "Boundary в позиции " + i);
        }

        //Сравнение UV по углам, а не по вершинам
        if (compareTex) {
            assertNotNull(expected.polygonsTextureCoordinateIndices);
            assertNotNull(actual.polygonsTextureCoordinateIndices);

            assertEquals(expected.polygonsTextureCoordinateIndices.size(), actual.polygonsTextureCoordinateIndices.size(),
                    "Разный размер polygonsTextureCoordinateIndices");

            for (int corner = 0; corner < expected.polygons.size(); corner++) {
                Vector2f euv = expected.getTextureCoordinateForPolygonVertex(corner);
                Vector2f auv = actual.getTextureCoordinateForPolygonVertex(corner);

                assertNotNull(euv, "UV ожидаемый is null в углу " + corner);
                assertNotNull(auv, "UV актуальный is null в углу corner " + corner);

                assertEquals(euv.getX(), auv.getX(), EPS, "U в углу " + corner);
                assertEquals(euv.getY(), auv.getY(), EPS, "V в углу " + corner);
            }
        }
    }

    @Test
    public void testReadingAndWritingAndReadingTestModel() throws Exception {
        Path src = copyResourceToTemp("Dichlorvose.obj");

        Model m1 = read(src);

        Path out = tempDir.resolve("roundtrip.obj");
        assertTrue(ObjWriter.writeModelToFile(m1, out.toString()),
                "ObjWriter вернул false при записи roundtrip.obj (запись модели не удалась)");
        assertTrue(Files.exists(out),
                "Файл roundtrip.obj не появился после записи: " + out);

        Model m2 = read(out);

        assertModelsEqual(m1, m2);
    }

    @Test
    public void testReadingAndWritingAndReadingAndWritingAngReadingTestModel() throws Exception {
        Path src = copyResourceToTemp("Dichlorvose.obj");

        Model m1 = read(src);

        Path out1 = tempDir.resolve("step1.obj");
        assertTrue(ObjWriter.writeModelToFile(m1, out1.toString()), "ObjWriter вернул false");
        Model m2 = read(out1);

        Path out2 = tempDir.resolve("step2.obj");
        assertTrue(ObjWriter.writeModelToFile(m2, out2.toString()), "ObjWriter вернул false");
        Model m3 = read(out2);

        assertModelsEqual(m2, m3);
    }

    @Test
    public void testWritingAndReadingBuildModel() throws Exception {
        Model m1 = new Model();
        m1.modelName = "pyramid";
        m1.vertices = new ArrayList<>();
        m1.polygons = new ArrayList<>();
        m1.polygonsBoundaries = new ArrayList<>();
        m1.hasTexture = false;

        Vertex v1 = new Vertex(-1, -1, 0);
        Vertex v2 = new Vertex(-1,  1, 0);
        Vertex v3 = new Vertex( 1,  1, 0);
        Vertex v4 = new Vertex( 1, -1, 0);
        Vertex v5 = new Vertex( 0,  0, 1);

        m1.vertices.add(v1);
        m1.vertices.add(v2);
        m1.vertices.add(v3);
        m1.vertices.add(v4);
        m1.vertices.add(v5);

        m1.polygonsBoundaries.add(0);
        m1.polygons.addAll(List.of(0, 1, 2, 3));

        m1.polygonsBoundaries.add(4);
        m1.polygons.addAll(List.of(0, 1, 4));

        m1.polygonsBoundaries.add(7);
        m1.polygons.addAll(List.of(1, 2, 4));

        m1.polygonsBoundaries.add(10);
        m1.polygons.addAll(List.of(2, 3, 4));

        m1.polygonsBoundaries.add(13);
        m1.polygons.addAll(List.of(3, 0, 4));

        Path out = tempDir.resolve("pyramid.obj");
        assertTrue(ObjWriter.writeModelToFile(m1, out.toString()), "ObjWriter вернул false");
        assertTrue(Files.exists(out), "Файл не существует");

        String content = Files.readString(out);
        Model m2 = ObjReader.readModelFromFile(content, out.getFileName().toString(), new HashMap<>());

        assertModelsEqual(m1, m2);
    }


}

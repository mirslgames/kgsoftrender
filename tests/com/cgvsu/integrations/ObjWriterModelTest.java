package com.cgvsu.objwriter;

import com.cgvsu.model.Model;
import com.cgvsu.objreader.ObjReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ObjWriterModelTest {

    @TempDir
    Path tempDir;

    @Test
    void deletePolygonThenDeleteVertexShiftIndicesWriteAndReadBackOk() throws Exception {
        String obj =
                "v 0 0 0\n" +
                        "v 1 0 0\n" +
                        "v 0 1 0\n" +
                        "v 0 0 1\n" +
                        "v 2 2 2\n" +
                        "f 1 2 3\n" + // полигон 0 (удалим)
                        "f 1 3 4\n"; // полигон 1 (останется)

        Model model = ObjReader.readModelFromFile(obj, "integration.obj", new HashMap<>());
        assertNotNull(model);

        assertEquals(5, model.vertices.size());
        assertEquals(List.of(0, 3), model.polygonsBoundaries);
        assertEquals(6, model.polygons.size());

        assertTrue(model.deletePolygonFromIndex(0, false));
        assertEquals(1, model.polygonsBoundaries.size());
        assertEquals(List.of(0), model.polygonsBoundaries);
        assertEquals(3, model.polygons.size());
        assertEquals(List.of(0, 2, 3), model.polygons);

        assertTrue(model.deleteVertexFromIndex(1));
        assertEquals(4, model.vertices.size());
        assertEquals(List.of(0, 1, 2), model.polygons);

        assertAllPolygonIndicesInRange(model);

        Path out = tempDir.resolve("out.obj");
        assertTrue(ObjWriter.writeModelToFile(model, out.toString()));
        assertTrue(Files.exists(out));
        assertTrue(Files.size(out) > 0);

        String written = Files.readString(out);
        Model readBack = ObjReader.readModelFromFile(written, "out.obj", new HashMap<>());
        assertNotNull(readBack);

        assertEquals(4, readBack.vertices.size());
        assertEquals(List.of(0), readBack.polygonsBoundaries);
        assertEquals(List.of(0, 1, 2), readBack.polygons);

        assertAllPolygonIndicesInRange(readBack);
    }

    private static void assertAllPolygonIndicesInRange(Model m) {
        for (int idx : m.polygons) {
            assertTrue(idx >= 0 && idx < m.vertices.size(),
                    "индекс полигона вне диапазона: " + idx + ", vertices=" + m.vertices.size());
        }
    }
}

package com.cgvsu.objreader;

import com.cgvsu.math.vectors.Vector2f;
import com.cgvsu.math.vectors.Vector3f;
import com.cgvsu.model.Model;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ObjReaderTest {

    private static void assertObjReaderException(ObjReaderException ex, int expectedLine, String expectedReasonFragment) {
        String msg = ex.getMessage();
        assertNotNull(msg, "Сообщение исключения не должно быть null");

        assertTrue(msg.contains(String.valueOf(expectedLine)),
                "В сообщении должна быть строка " + expectedLine + ", но сообщение такое:\n" + msg);

        assertTrue(msg.contains(expectedReasonFragment),
                "В сообщении должна быть причина: '" + expectedReasonFragment + "'.\n" +
                        "Фактически сообщение такое:\n" + msg);
    }

    private static ArrayList<String> arrToList(String[] items) {
        ArrayList<String> result = new ArrayList<>();
        for (int i = 0; i < items.length; i++) {
            result.add(items[i]);
        }
        return result;
    }

    @Test
    void testSafeModelNameFromFilenameNullOrBlank() {
        assertEquals("model", ObjReader.safeModelNameFromFilename(null),
                "Если filename == null, имя модели должно быть 'model'");
        assertEquals("model", ObjReader.safeModelNameFromFilename(""),
                "Если filename пустая строка, имя модели должно быть 'model'");
        assertEquals("model", ObjReader.safeModelNameFromFilename("   "),
                "Если filename состоит из пробелов, имя модели должно быть 'model'");
    }

    @Test
    void testSafeModelNameFromFilenameRemovesObjExtension() {
        assertEquals("house", ObjReader.safeModelNameFromFilename("house.obj"),
                "Должен отрезаться суффикс .obj");
        assertEquals("house", ObjReader.safeModelNameFromFilename("house.OBJ"),
                "Должен отрезаться суффикс .OBJ (регистр не важен)");
    }

    @Test
    void testSafeModelNameFromFilenameKeepsNonObj() {
        assertEquals("modelname", ObjReader.safeModelNameFromFilename("  modelname  "),
                "Если нет расширения .obj — возвращается имя файла после trim()");
        assertEquals("scene.fbx", ObjReader.safeModelNameFromFilename("scene.fbx"),
                "Если расширение другое — строка возвращается как есть");
    }

    @Test
    void testDetectFaceFormatV() {
        assertEquals(FaceFormat.V, ObjReader.detectFaceFormat("1", 7),
                "Строка '1' должна распознаваться как формат V");
    }

    @Test
    void testDetectFaceFormatVVt() {
        assertEquals(FaceFormat.V_VT, ObjReader.detectFaceFormat("1/2", 7),
                "Строка '1/2' должна распознаваться как формат V_VT");
    }

    @Test
    void testDetectFaceFormatVVn() {
        assertEquals(FaceFormat.V_VN, ObjReader.detectFaceFormat("1//3", 7),
                "Строка '1//3' должна распознаваться как формат V_VN");
    }

    @Test
    void testDetectFaceFormatVVtVn() {
        assertEquals(FaceFormat.V_VT_VN, ObjReader.detectFaceFormat("1/2/3", 7),
                "Строка '1/2/3' должна распознаваться как формат V_VT_VN");
    }

    @Test
    void testDetectFaceFormatEmptyThrows() {
        ObjReaderException ex = assertThrows(ObjReaderException.class,
                () -> ObjReader.detectFaceFormat("   ", 10),
                "Пустая строка в face должна приводить к исключению");
        assertObjReaderException(ex, 10, "Пустой элемент в face.");
    }

    @Test
    void testDetectFaceFormatInvalidThrows() {
        ObjReaderException ex1 = assertThrows(ObjReaderException.class,
                () -> ObjReader.detectFaceFormat("/2/3", 11),
                "Если первая часть пустая — формат должен считаться некорректным");
        assertObjReaderException(ex1, 11, "Некорректный формат индексов");

        ObjReaderException ex2 = assertThrows(ObjReaderException.class,
                () -> ObjReader.detectFaceFormat("1/", 12),
                "Если не указан vt — формат должен считаться некорректным");
        assertObjReaderException(ex2, 12, "Некорректный формат индексов");

        ObjReaderException ex3 = assertThrows(ObjReaderException.class,
                () -> ObjReader.detectFaceFormat("1/2/3/4", 13),
                "Слишком много частей — формат должен считаться некорректным");
        assertObjReaderException(ex3, 13, "Некорректное число слов для face.");
    }

    @Test
    void testParseVertexOk() {
        ArrayList<String> tokens = arrToList(new String[]{"1.01", "1.02", "1.03"});
        Vector3f v = ObjReader.parseVertex(tokens, 5);

        assertEquals(1.01f, v.getX(), 1e-6f, "X должен быть равен 1.01");
        assertEquals(1.02f, v.getY(), 1e-6f, "Y должен быть равен 1.02");
        assertEquals(1.03f, v.getZ(), 1e-6f, "Z должен быть равен 1.03");
    }

    @Test
    void testParseVertexNotFloatThrows() {
        ArrayList<String> tokens = arrToList(new String[]{"ab", "o", "ba"});

        ObjReaderException ex = assertThrows(ObjReaderException.class,
                () -> ObjReader.parseVertex(tokens, 10),
                "Нечисловые координаты вершины должны приводить к исключению");

        assertObjReaderException(ex, 10, "Ошибка парсинга float значения.");
    }

    @Test
    void testParseVertexTooFewThrows() {
        ArrayList<String> tokens = arrToList(new String[]{"1.0", "2.0"});

        ObjReaderException ex = assertThrows(ObjReaderException.class,
                () -> ObjReader.parseVertex(tokens, 10),
                "Если координат меньше трёх — должно быть исключение");

        assertObjReaderException(ex, 10, "Неправильное количество аргументов вершин.");
    }

    @Test
    void testParseVertexTooManyThrows() {
        ArrayList<String> tokens = arrToList(new String[]{"1.0", "2.0", "3.0", "4.0"});

        ObjReaderException ex = assertThrows(ObjReaderException.class,
                () -> ObjReader.parseVertex(tokens, 10),
                "Если координат больше трёх — должно быть исключение");

        assertObjReaderException(ex, 10, "Более 3 координат у вершины");
    }

    @Test
    void testParseTextureVertexOk() {
        ArrayList<String> tokens = arrToList(new String[]{"0.5", "1.0"});
        Vector2f t = ObjReader.parseTextureVertex(tokens, 3);

        assertEquals(0.5f, t.getX(), 1e-6f, "U должен быть равен 0.5");
        assertEquals(1.0f, t.getY(), 1e-6f, "V должен быть равен 1.0");
    }

    @Test
    void testParseTextureVertexNotFloatThrows() {
        ArrayList<String> tokens = arrToList(new String[]{"aa", "bb"});

        ObjReaderException ex = assertThrows(ObjReaderException.class,
                () -> ObjReader.parseTextureVertex(tokens, 8),
                "Нечисловые координаты vt должны приводить к исключению");

        assertObjReaderException(ex, 8, "Ошибка парсинга float значения.");
    }

    @Test
    void testParseTextureVertexTooFewThrows() {
        ArrayList<String> tokens = arrToList(new String[]{"0.5"});

        ObjReaderException ex = assertThrows(ObjReaderException.class,
                () -> ObjReader.parseTextureVertex(tokens, 9),
                "Если у vt меньше двух координат — должно быть исключение");

        assertObjReaderException(ex, 9, "Неправильное количество аргументов текстурных вершин.");
    }

    @Test
    void testParseNormalOk() {
        ArrayList<String> tokens = arrToList(new String[]{"0", "1", "0"});
        Vector3f n = ObjReader.parseNormal(tokens, 4);

        assertEquals(0f, n.getX(), 1e-6f, "NX должен быть 0");
        assertEquals(1f, n.getY(), 1e-6f, "NY должен быть 1");
        assertEquals(0f, n.getZ(), 1e-6f, "NZ должен быть 0");
    }

    @Test
    void testParseNormalNotFloatThrows() {
        ArrayList<String> tokens = arrToList(new String[]{"x", "y", "z"});

        ObjReaderException ex = assertThrows(ObjReaderException.class,
                () -> ObjReader.parseNormal(tokens, 6),
                "Нечисловые координаты vn должны приводить к исключению");

        assertObjReaderException(ex, 6, "Ошибка парсинга float значения.");
    }

    @Test
    void testParseNormalTooFewThrows() {
        ArrayList<String> tokens = arrToList(new String[]{"1", "0"});

        ObjReaderException ex = assertThrows(ObjReaderException.class,
                () -> ObjReader.parseNormal(tokens, 6),
                "Если у vn меньше трёх координат — должно быть исключение");

        assertObjReaderException(ex, 6, "Неправильное количество аргументов для нормалей.");
    }

    @Test
    void testParseFaceV() {
        ArrayList<String> faceTokens = arrToList(new String[]{"1", "2", "3"});

        ArrayList<Integer>[] face = ObjReader.parseFace(faceTokens, 20);

        assertNotNull(face, "parseFace должен возвращать массив списков");
        assertEquals(List.of(0, 1, 2), face[0], "V-индексы должны стать относительно 0: [0,1,2]");
        assertTrue(face[1].isEmpty(), "Список vt индексов должен быть пустым для формата V");
        assertTrue(face[2].isEmpty(), "Список vn индексов должен быть пустым для формата V");
    }

    @Test
    void testParseFaceVVt() {
        ArrayList<String> faceTokens = arrToList(new String[]{"1/1", "2/2", "3/3"});

        ArrayList<Integer>[] face = ObjReader.parseFace(faceTokens, 21);

        assertEquals(List.of(0, 1, 2), face[0], "V-индексы должны быть [0,1,2]");
        assertEquals(List.of(0, 1, 2), face[1], "VT-индексы должны быть [0,1,2]");
        assertTrue(face[2].isEmpty(), "VN-индексы должны быть пустыми для формата V_VT");
    }

    @Test
    void testParseFaceVVn() {
        ArrayList<String> faceTokens = arrToList(new String[]{"1//1", "2//2", "3//3"});

        ArrayList<Integer>[] face = ObjReader.parseFace(faceTokens, 22);

        assertEquals(List.of(0, 1, 2), face[0], "V-индексы должны быть [0,1,2]");
        assertTrue(face[1].isEmpty(), "VT-индексы должны быть пустыми для формата V_VN");
        assertEquals(List.of(0, 1, 2), face[2], "VN-индексы должны быть [0,1,2]");
    }

    @Test
    void testParseFaceVVtVn() {
        ArrayList<String> faceTokens = arrToList(new String[]{"1/1/1", "2/2/2", "3/3/3"});

        ArrayList<Integer>[] face = ObjReader.parseFace(faceTokens, 23);

        assertEquals(List.of(0, 1, 2), face[0], "V-индексы должны быть [0,1,2]");
        assertEquals(List.of(0, 1, 2), face[1], "VT-индексы должны быть [0,1,2]");
        assertEquals(List.of(0, 1, 2), face[2], "VN-индексы должны быть [0,1,2]");
    }

    @Test
    void testParseFaceTooFewVerticesThrows() {
        ArrayList<String> faceTokens = arrToList(new String[]{"1", "2"});

        ObjReaderException ex = assertThrows(ObjReaderException.class,
                () -> ObjReader.parseFace(faceTokens, 30),
                "Face должен иметь минимум 3 вершины");

        assertObjReaderException(ex, 30, "Face должен иметь минимум 3 вершины");
    }

    @Test
    void testParseFaceMixedFormatsThrows() {
        ArrayList<String> faceTokens = arrToList(new String[]{"1/1", "2/2", "3//3"});

        ObjReaderException ex = assertThrows(ObjReaderException.class,
                () -> ObjReader.parseFace(faceTokens, 31),
                "Если в одном face разные форматы — должно быть исключение");

        assertObjReaderException(ex, 31, "нельзя смешивать разные форматы");
    }

    @Test
    void testParseFaceZeroIndexThrows() {
        ObjReaderException ex = assertThrows(ObjReaderException.class,
                () -> ObjReader.parseFace(arrToList(new String[]{"0", "1", "2"}), 32),
                "Индекс вершины 0 не поддерживается");

        assertObjReaderException(ex, 32, "Индекс v не может быть 0.");
    }

    @Test
    void testParseFaceNegativeIndexThrows() {
        ObjReaderException ex = assertThrows(ObjReaderException.class,
                () -> ObjReader.parseFace(arrToList(new String[]{"-1", "1", "2"}), 33),
                "Отрицательные индексы не поддерживаются");

        assertObjReaderException(ex, 33, "Индекс v не может быть отрицательным.");
    }

    @Test
    void testParseFaceNotIntThrows() {
        ObjReaderException ex = assertThrows(ObjReaderException.class,
                () -> ObjReader.parseFace(arrToList(new String[]{"a", "2", "3"}), 34),
                "Нечисловые индексы в face должны приводить к исключению");

        assertObjReaderException(ex, 34, "Не удалось спарсить int значение.");
    }

    @Test
    void testParseFaceVtZeroThrows() {
        ObjReaderException ex = assertThrows(ObjReaderException.class,
                () -> ObjReader.parseFace(arrToList(new String[]{"1/0", "2/1", "3/1"}), 35),
                "vt индекс 0 не поддерживается");

        assertObjReaderException(ex, 35, "Индекс vt не может быть 0.");
    }

    @Test
    void testParseFaceVnZeroThrows() {
        ObjReaderException ex = assertThrows(ObjReaderException.class,
                () -> ObjReader.parseFace(arrToList(new String[]{"1//0", "2//1", "3//1"}), 36),
                "vn индекс 0 не поддерживается");

        assertObjReaderException(ex, 36, "Индекс vn не может быть 0.");
    }

    @Test
    void testParseFaceEmptyElementThrows() {
        ObjReaderException ex = assertThrows(ObjReaderException.class,
                () -> ObjReader.parseFace(arrToList(new String[]{"1", " ", "3"}), 37),
                "Пустой элемент внутри face должен приводить к исключению");

        assertObjReaderException(ex, 37, "Пустой элемент в face.");
    }

    @Test
    void testParseFaceWordVAddsZeroBasedIndex() {
        ArrayList<Integer> indices = new ArrayList<>();

        ObjReader.parseFaceWordV("5", indices, 40);

        assertEquals(1, indices.size(), "После добавления индекс должен быть один");
        assertEquals(4, indices.get(0), "Индекс '5' из OBJ должен стать 4 (относительно 0)");
    }

    @Test
    void testParseFaceWordVZeroThrows() {
        ArrayList<Integer> indices = new ArrayList<>();

        ObjReaderException ex = assertThrows(ObjReaderException.class,
                () -> ObjReader.parseFaceWordV("0", indices, 41),
                "Индекс 0 не поддерживается");

        assertObjReaderException(ex, 41, "Индекс v не может быть 0.");
    }

    @Test
    void testParseFaceWordVNegativeThrows() {
        ArrayList<Integer> indices = new ArrayList<>();

        ObjReaderException ex = assertThrows(ObjReaderException.class,
                () -> ObjReader.parseFaceWordV("-2", indices, 42),
                "Отрицательные индексы не поддерживаются");

        assertObjReaderException(ex, 42, "Индекс v не может быть отрицательным.");
    }

    @Test
    void testParseFaceWordVNotIntThrows() {
        ArrayList<Integer> indices = new ArrayList<>();

        ObjReaderException ex = assertThrows(ObjReaderException.class,
                () -> ObjReader.parseFaceWordV("abc", indices, 43),
                "Нечисловой индекс должен приводить к исключению");

        assertObjReaderException(ex, 43, "Не удалось спарсить int значение.");
    }

    @Test
    void testParseFaceWordVt_okAddsIndicesMinusOne() {
        ArrayList<Integer> v = new ArrayList<>();
        ArrayList<Integer> vt = new ArrayList<>();

        ObjReader.parseFaceWordVt("3/7", v, vt, 10);

        assertEquals(1, v.size(), "Список v должен получить 1 индекс");
        assertEquals(1, vt.size(), "Список vt должен получить 1 индекс");
        assertEquals(2, v.get(0), "Индекс v должен сохраняться как (vi-1)");
        assertEquals(6, vt.get(0), "Индекс vt должен сохраняться как (vti-1)");
    }

    @Test
    void testParseFaceWordVt_viZeroThrows() {
        ArrayList<Integer> v = new ArrayList<>();
        ArrayList<Integer> vt = new ArrayList<>();

        ObjReaderException ex = assertThrows(
                ObjReaderException.class,
                () -> ObjReader.parseFaceWordVt("0/2", v, vt, 10),
                "vi=0 должен вызывать ObjReaderException"
        );

        assertTrue(ex.getMessage().contains("Индекс v не может быть 0"),
                "Сообщение должно объяснять, что индекс v не может быть 0");
    }

    @Test
    void testParseFaceWordVt_vtZeroThrows() {
        ArrayList<Integer> v = new ArrayList<>();
        ArrayList<Integer> vt = new ArrayList<>();

        ObjReaderException ex = assertThrows(
                ObjReaderException.class,
                () -> ObjReader.parseFaceWordVt("2/0", v, vt, 10),
                "vt=0 должен вызывать ObjReaderException"
        );

        assertTrue(ex.getMessage().contains("Индекс vt не может быть 0"),
                "Сообщение должно объяснять, что индекс vt не может быть 0");
    }

    @Test
    void testParseFaceWordVt_emptySecondPartThrows() {
        ArrayList<Integer> v = new ArrayList<>();
        ArrayList<Integer> vt = new ArrayList<>();

        ObjReaderException ex = assertThrows(
                ObjReaderException.class,
                () -> ObjReader.parseFaceWordVt("1/", v, vt, 10),
                "Пустой индекс vt должен вызывать ObjReaderException"
        );

        assertTrue(ex.getMessage().contains("Не удалось спарсить int"),
                "Сообщение должно говорить, что int распарсить не удалось");
    }

    @Test
    void testParseFaceWordVn_okAddsIndicesMinusOne() {
        ArrayList<Integer> v = new ArrayList<>();
        ArrayList<Integer> vn = new ArrayList<>();

        ObjReader.parseFaceWordVn("4//9", v, vn, 7);

        assertEquals(1, v.size(), "Список v должен получить 1 индекс");
        assertEquals(1, vn.size(), "Список vn должен получить 1 индекс");
        assertEquals(3, v.get(0), "Индекс v должен сохраняться как (vi-1)");
        assertEquals(8, vn.get(0), "Индекс vn должен сохраняться как (vni-1)");
    }

    @Test
    void testParseFaceWordVn_viZeroThrows() {
        ArrayList<Integer> v = new ArrayList<>();
        ArrayList<Integer> vn = new ArrayList<>();

        ObjReaderException ex = assertThrows(
                ObjReaderException.class,
                () -> ObjReader.parseFaceWordVn("0//2", v, vn, 10),
                "vi=0 должен вызывать ObjReaderException"
        );

        assertTrue(ex.getMessage().contains("Индекс v не может быть 0"),
                "Сообщение должно объяснять, что индекс v не может быть 0");
    }

    @Test
    void testParseFaceWordVn_vnZeroThrows() {
        ArrayList<Integer> v = new ArrayList<>();
        ArrayList<Integer> vn = new ArrayList<>();

        ObjReaderException ex = assertThrows(
                ObjReaderException.class,
                () -> ObjReader.parseFaceWordVn("2//0", v, vn, 10),
                "vn=0 должен вызывать ObjReaderException"
        );

        assertTrue(ex.getMessage().contains("Индекс vn не может быть 0"),
                "Сообщение должно объяснять, что индекс vn не может быть 0");
    }

    @Test
    void testParseFaceWordVn_emptySecondPartThrows() {
        ArrayList<Integer> v = new ArrayList<>();
        ArrayList<Integer> vn = new ArrayList<>();

        ObjReaderException ex = assertThrows(
                ObjReaderException.class,
                () -> ObjReader.parseFaceWordVn("1//", v, vn, 10),
                "Пустой индекс vn должен вызывать ObjReaderException"
        );

        assertTrue(ex.getMessage().contains("Не удалось спарсить int"),
                "Сообщение должно говорить, что int распарсить не удалось");
    }

    @Test
    void testParseFaceWordVtVn_okAddsIndicesMinusOne() {
        ArrayList<Integer> v = new ArrayList<>();
        ArrayList<Integer> vt = new ArrayList<>();
        ArrayList<Integer> vn = new ArrayList<>();

        ObjReader.parseFaceWordVtVn("5/6/7", v, vt, vn, 3);

        assertEquals(1, v.size(), "Список v должен получить 1 индекс");
        assertEquals(1, vt.size(), "Список vt должен получить 1 индекс");
        assertEquals(1, vn.size(), "Список vn должен получить 1 индекс");

        assertEquals(4, v.get(0), "Индекс v должен сохраняться как (vi-1)");
        assertEquals(5, vt.get(0), "Индекс vt должен сохраняться как (vti-1)");
        assertEquals(6, vn.get(0), "Индекс vn должен сохраняться как (vni-1)");
    }

    @Test
    void testParseFaceWordVtVn_viZeroThrows() {
        ArrayList<Integer> v = new ArrayList<>();
        ArrayList<Integer> vt = new ArrayList<>();
        ArrayList<Integer> vn = new ArrayList<>();

        ObjReaderException ex = assertThrows(
                ObjReaderException.class,
                () -> ObjReader.parseFaceWordVtVn("0/2/3", v, vt, vn, 10),
                "vi=0 должен вызывать ObjReaderException"
        );

        assertTrue(ex.getMessage().contains("Индекс v не может быть 0"),
                "Сообщение должно объяснять, что индекс v не может быть 0");
    }

    @Test
    void testParseFaceWordVtVn_vtZeroThrows() {
        ArrayList<Integer> v = new ArrayList<>();
        ArrayList<Integer> vt = new ArrayList<>();
        ArrayList<Integer> vn = new ArrayList<>();

        ObjReaderException ex = assertThrows(
                ObjReaderException.class,
                () -> ObjReader.parseFaceWordVtVn("1/0/3", v, vt, vn, 10),
                "vt=0 должен вызывать ObjReaderException"
        );

        assertTrue(ex.getMessage().contains("Индекс vt не может быть 0"),
                "Сообщение должно объяснять, что индекс vt не может быть 0");
    }

    @Test
    void testParseFaceWordVtVn_tooManyPartsThrows() {
        ArrayList<Integer> v = new ArrayList<>();
        ArrayList<Integer> vt = new ArrayList<>();
        ArrayList<Integer> vn = new ArrayList<>();

        ObjReaderException ex = assertThrows(
                ObjReaderException.class,
                () -> ObjReader.parseFaceWordVtVn("1/2/3/4", v, vt, vn, 10),
                "Строка '1/2/3/4' должна считаться некорректной и приводить к ObjReaderException"
        );

        assertTrue(ex.getMessage().contains("Некорректный формат индексов в face"),
                "Сообщение об ошибке должно объяснять, что формат индексов в face неправильный");

        assertEquals(0, v.size(), "При ошибке индекс v не должен добавляться");
        assertEquals(0, vt.size(), "При ошибке индекс vt не должен добавляться");
        assertEquals(0, vn.size(), "При ошибке индекс vn не должен добавляться");
    }

    @Test
    void testCheckReadDataNoVerticesReturnsFalse() {
        boolean ok = ObjReader.checkReadData(
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
        );

        assertFalse(ok, "Если вершин нет — данных недостаточно, должен вернуться false");
    }

    @Test
    void testCheckReadDataNoFacesReturnsFalse() {
        ArrayList<Vector3f> vertices = new ArrayList<>();
        vertices.add(new Vector3f(0, 0, 0));

        boolean ok = ObjReader.checkReadData(
                vertices,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
        );

        assertFalse(ok, "Если нет ни одного face — данных недостаточно, должен вернуться false");
    }

    @Test
    void testCheckReadDataOkReturnsTrue() {
        ArrayList<Vector3f> vertices = new ArrayList<>();
        vertices.add(new Vector3f(0, 0, 0));
        vertices.add(new Vector3f(1, 0, 0));
        vertices.add(new Vector3f(0, 1, 0));

        ArrayList<ArrayList<Integer>[]> faces = new ArrayList<>();
        faces.add(createFace(
                new int[]{0, 1, 2},  // v
                null,               // vt
                null                // vn
        ));

        boolean ok = ObjReader.checkReadData(
                vertices,
                new ArrayList<>(),
                new ArrayList<>(),
                faces,
                new ArrayList<>()
        );

        assertTrue(ok, "Если есть вершины и корректный face — должен вернуться true");
    }

    @Test
    void testCheckReadDataNullFaceThrows() {
        ArrayList<Vector3f> vertices = new ArrayList<>();
        vertices.add(new Vector3f(0, 0, 0));

        ArrayList<ArrayList<Integer>[]> faces = new ArrayList<>();
        faces.add(null);

        ArrayList<Integer> faceLines = new ArrayList<>();
        faceLines.add(10);

        ObjReaderException ex = assertThrows(
                ObjReaderException.class,
                () -> ObjReader.checkReadData(vertices, new ArrayList<>(), new ArrayList<>(), faces, faceLines),
                "Если face == null — должно быть исключение"
        );

        assertTrue(ex.getMessage().contains("Некорректные данные face"),
                "Сообщение должно объяснять, что face некорректный");
        assertTrue(ex.getMessage().contains("10"),
                "В сообщении должна быть строка face (10)");
    }

    @Test
    void testCheckReadDataFaceWithNullVListThrows() {
        ArrayList<Vector3f> vertices = new ArrayList<>();
        vertices.add(new Vector3f(0, 0, 0));

        ArrayList<ArrayList<Integer>[]> faces = new ArrayList<>();
        // v список == null
        faces.add(new ArrayList[]{null, new ArrayList<Integer>(), new ArrayList<Integer>()});

        ArrayList<Integer> faceLines = new ArrayList<>();
        faceLines.add(11);

        ObjReaderException ex = assertThrows(
                ObjReaderException.class,
                () -> ObjReader.checkReadData(vertices, new ArrayList<>(), new ArrayList<>(), faces, faceLines),
                "Если face[0] == null — должно быть исключение"
        );

        assertTrue(ex.getMessage().contains("Некорректные данные face"),
                "Сообщение должно объяснять, что face некорректный");
        assertTrue(ex.getMessage().contains("11"),
                "В сообщении должна быть строка face (11)");
    }

    @Test
    void testCheckReadDataVertexIndexOutOfRangeThrows() {
        ArrayList<Vector3f> vertices = new ArrayList<>();
        vertices.add(new Vector3f(0, 0, 0));
        vertices.add(new Vector3f(1, 0, 0));
        vertices.add(new Vector3f(0, 1, 0)); // vCount=3 -> допустимо 0..2

        ArrayList<ArrayList<Integer>[]> faces = new ArrayList<>();
        faces.add(createFace(
                new int[]{0, 1, 3},
                null,
                null
        ));

        ArrayList<Integer> faceLines = new ArrayList<>();
        faceLines.add(20);

        ObjReaderException ex = assertThrows(
                ObjReaderException.class,
                () -> ObjReader.checkReadData(vertices, new ArrayList<>(), new ArrayList<>(), faces, faceLines),
                "Если v-индекс выходит за границы — должно быть исключение"
        );

        assertTrue(ex.getMessage().contains("Индекс вершины выходит за границы"),
                "Сообщение должно указывать, что индекс вершины вне диапазона");
        assertTrue(ex.getMessage().contains("0..2"),
                "Сообщение должно содержать допустимый диапазон 0..2");
        assertTrue(ex.getMessage().contains("20"),
                "В сообщении должна быть строка face (20)");
    }

    @Test
    void testCheckReadDataVtUsedButNoVtVerticesThrows() {
        ArrayList<Vector3f> vertices = new ArrayList<>();
        vertices.add(new Vector3f(0, 0, 0));
        vertices.add(new Vector3f(1, 0, 0));
        vertices.add(new Vector3f(0, 1, 0));

        ArrayList<ArrayList<Integer>[]> faces = new ArrayList<>();
        faces.add(createFace(
                new int[]{0, 1, 2},
                new int[]{0, 0, 0},
                null
        ));

        ArrayList<Integer> faceLines = new ArrayList<>();
        faceLines.add(30);

        ObjReaderException ex = assertThrows(
                ObjReaderException.class,
                () -> ObjReader.checkReadData(vertices, new ArrayList<>(), new ArrayList<>(), faces, faceLines),
                "Если face использует vt, но vt-вершин нет — должно быть исключение"
        );

        assertTrue(ex.getMessage().contains("используются vt"),
                "Сообщение должно указывать, что vt используются");
        assertTrue(ex.getMessage().contains("нет ни одной vt"),
                "Сообщение должно указывать, что vt в файле отсутствуют");
        assertTrue(ex.getMessage().contains("30"),
                "В сообщении должна быть строка face (30)");
    }

    @Test
    void testCheckReadDataVtCountMismatchThrows() {
        ArrayList<Vector3f> vertices = new ArrayList<>();
        vertices.add(new Vector3f(0, 0, 0));
        vertices.add(new Vector3f(1, 0, 0));
        vertices.add(new Vector3f(0, 1, 0));

        ArrayList<Vector2f> textures = new ArrayList<>();
        textures.add(new Vector2f(0, 0));

        ArrayList<ArrayList<Integer>[]> faces = new ArrayList<>();
        faces.add(createFace(
                new int[]{0, 1, 2},
                new int[]{0, 0},
                null
        ));

        ArrayList<Integer> faceLines = new ArrayList<>();
        faceLines.add(31);

        ObjReaderException ex = assertThrows(
                ObjReaderException.class,
                () -> ObjReader.checkReadData(vertices, textures, new ArrayList<>(), faces, faceLines),
                "Если количество vt индексов не совпадает с v — должно быть исключение"
        );

        assertTrue(ex.getMessage().contains("Количество vt индексов не совпадает"),
                "Сообщение должно объяснять несоответствие количества vt и v");
        assertTrue(ex.getMessage().contains("31"),
                "В сообщении должна быть строка face (31)");
    }

    @Test
    void testCheckReadDataVtIndexOutOfRangeThrows() {
        ArrayList<Vector3f> vertices = new ArrayList<>();
        vertices.add(new Vector3f(0, 0, 0));
        vertices.add(new Vector3f(1, 0, 0));
        vertices.add(new Vector3f(0, 1, 0));

        ArrayList<Vector2f> textures = new ArrayList<>();
        textures.add(new Vector2f(0, 0));

        ArrayList<ArrayList<Integer>[]> faces = new ArrayList<>();
        faces.add(createFace(
                new int[]{0, 1, 2},
                new int[]{0, 1, 0},
                null
        ));

        ArrayList<Integer> faceLines = new ArrayList<>();
        faceLines.add(32);

        ObjReaderException ex = assertThrows(
                ObjReaderException.class,
                () -> ObjReader.checkReadData(vertices, textures, new ArrayList<>(), faces, faceLines),
                "Если vt индекс выходит за границы — должно быть исключение"
        );

        assertTrue(ex.getMessage().contains("Индекс vt выходит за границы"),
                "Сообщение должно указывать, что индекс vt вне диапазона");
        assertTrue(ex.getMessage().contains("0..0"),
                "Сообщение должно содержать допустимый диапазон 0..0");
        assertTrue(ex.getMessage().contains("32"),
                "В сообщении должна быть строка face (32)");
    }

    @Test
    void testCheckReadDataVnCountMismatchThrowsWhenNormalsExist() {
        ArrayList<Vector3f> vertices = new ArrayList<>();
        vertices.add(new Vector3f(0, 0, 0));
        vertices.add(new Vector3f(1, 0, 0));
        vertices.add(new Vector3f(0, 1, 0));

        ArrayList<Vector3f> normals = new ArrayList<>();
        normals.add(new Vector3f(0, 0, 1));

        ArrayList<ArrayList<Integer>[]> faces = new ArrayList<>();
        faces.add(createFace(
                new int[]{0, 1, 2},
                null,
                new int[]{0, 0}
        ));

        ArrayList<Integer> faceLines = new ArrayList<>();
        faceLines.add(40);

        ObjReaderException ex = assertThrows(
                ObjReaderException.class,
                () -> ObjReader.checkReadData(vertices, new ArrayList<>(), normals, faces, faceLines),
                "Если vn есть в файле и количество vn индексов не совпадает с v — должно быть исключение"
        );

        assertTrue(ex.getMessage().contains("Количество vn индексов не совпадает"),
                "Сообщение должно объяснять несоответствие количества vn и v");
        assertTrue(ex.getMessage().contains("40"),
                "В сообщении должна быть строка face (40)");
    }

    @Test
    void testCheckReadDataVnUsedButNoNormalsSoftModeOk() {
        ArrayList<Vector3f> vertices = new ArrayList<>();
        vertices.add(new Vector3f(0, 0, 0));
        vertices.add(new Vector3f(1, 0, 0));
        vertices.add(new Vector3f(0, 1, 0));

        ArrayList<ArrayList<Integer>[]> faces = new ArrayList<>();
        faces.add(createFace(
                new int[]{0, 1, 2},
                null,
                new int[]{0, 0, 0}
        ));

        ArrayList<Integer> faceLines = new ArrayList<>();
        faceLines.add(50);

        assertDoesNotThrow(
                () -> ObjReader.checkReadData(vertices, new ArrayList<>(), new ArrayList<>(), faces, faceLines),
                "Если vn индексы есть, но vn в файле нет — в мягком режиме не должно быть исключения"
        );

        boolean ok = ObjReader.checkReadData(vertices, new ArrayList<>(), new ArrayList<>(), faces, faceLines);
        assertTrue(ok, "В мягком режиме checkReadData должен вернуть true при корректных v-индексах");
    }

    @Test
    void testCheckReadDataVnIndexOutOfRangeThrowsWhenNormalsExist() {
        ArrayList<Vector3f> vertices = new ArrayList<>();
        vertices.add(new Vector3f(0, 0, 0));
        vertices.add(new Vector3f(1, 0, 0));
        vertices.add(new Vector3f(0, 1, 0));

        ArrayList<Vector3f> normals = new ArrayList<>();
        normals.add(new Vector3f(0, 0, 1)); //vnCount=1 -> допустимо только 0

        ArrayList<ArrayList<Integer>[]> faces = new ArrayList<>();
        faces.add(createFace(
                new int[]{0, 1, 2},
                null,
                new int[]{0, 1, 0} //1 вне диапазона
        ));

        ArrayList<Integer> faceLines = new ArrayList<>();
        faceLines.add(60);

        ObjReaderException ex = assertThrows(
                ObjReaderException.class,
                () -> ObjReader.checkReadData(vertices, new ArrayList<>(), normals, faces, faceLines),
                "Если vn индекс выходит за границы и vn существуют — должно быть исключение"
        );

        assertTrue(ex.getMessage().contains("Индекс vn выходит за границы"),
                "Сообщение должно указывать, что индекс vn вне диапазона");
        assertTrue(ex.getMessage().contains("60"),
                "В сообщении должна быть строка face (60)");
    }

    private static ArrayList<Integer>[] createFace(int[] vIdx, int[] vtIdx, int[] vnIdx) {
        ArrayList<Integer> v = new ArrayList<>();
        for (int x : vIdx) v.add(x);

        ArrayList<Integer> vt = new ArrayList<>();
        if (vtIdx != null) {
            for (int x : vtIdx) vt.add(x);
        }

        ArrayList<Integer> vn = new ArrayList<>();
        if (vnIdx != null) {
            for (int x : vnIdx) vn.add(x);
        }

        ArrayList<Integer>[] face = new ArrayList[3];
        face[0] = v;
        face[1] = vt;
        face[2] = vn;
        return face;
    }

    @Test
    void testReadModelFromFileSimpleTriangle() {
        String obj =
                "# comment\n" +
                        "v 0 0 0\n" +
                        "v 1 0 0\n" +
                        "v 0 1 0\n" +
                        "f 1 2 3\n";

        Model model = ObjReader.readModelFromFile(obj, "triangle.obj", new HashMap<>());

        assertNotNull(model, "Модель не должна быть null");
        assertEquals("triangle", model.modelName, "Имя модели должно получиться из имени файла без .obj");

        assertNotNull(model.vertices, "Список вершин не должен быть null");
        assertEquals(3, model.vertices.size(), "Должно прочитаться 3 вершины");

        assertNotNull(model.polygons, "Список polygon indices не должен быть null");
        assertEquals(List.of(0, 1, 2), model.polygons, "Индексы в face должны стать 0-based");

        assertNotNull(model.polygonsBoundaries, "polygonsBoundaries не должен быть null");
        assertEquals(List.of(0), model.polygonsBoundaries, "Для одного face boundary должен быть [0]");
    }

    @Test
    void testReadModelUvSeamOneVertexHasTwoUvs() {
        String obj =
                "v 0 0 0\n" +
                        "v 1 0 0\n" +
                        "v 1 1 0\n" +
                        "v 0 1 0\n" +
                        "vt 0 0\n" +
                        "vt 1 0\n" +
                        "vt 1 1\n" +
                        "vt 0 1\n" +
                        "vt 0 0.5\n" +
                        "vt 1 0.5\n" +
                        "f 1/1 2/2 3/3\n" +
                        "f 1/5 3/6 4/4\n";

        Model model = ObjReader.readModelFromFile(obj, "seam.obj", new HashMap<>());

        assertTrue(model.getHasTextureVertex(), "Модель должна считаться текстурированной (есть vt для всех углов)");
        assertEquals(model.polygons.size(), model.polygonsTextureCoordinateIndices.size(),
                "polygonsTextureCoordinateIndices должен быть той же длины что polygons");

        assertNotNull(model.vertices.get(0).textureCoordinates, "textureCoordinates не должен быть null");
        assertEquals(2, model.vertices.get(0).textureCoordinates.size(),
                "У вершины 0 должно быть 2 UV-варианта из-за UV-шва");

        Vector2f uv0 = model.getTextureCoordinateForPolygonVertex(0);
        assertNotNull(uv0);
        assertEquals(0f, uv0.getX(), 1e-6f);
        assertEquals(0f, uv0.getY(), 1e-6f);

        Vector2f uv3 = model.getTextureCoordinateForPolygonVertex(3);
        assertNotNull(uv3);
        assertEquals(0f, uv3.getX(), 1e-6f);
        assertEquals(0.5f, uv3.getY(), 1e-6f);
    }

    @Test
    void testCheckReadDataMixVtAndNoVtThrows() {
        ArrayList<Vector3f> vertices = new ArrayList<>();
        vertices.add(new Vector3f(0, 0, 0));
        vertices.add(new Vector3f(1, 0, 0));
        vertices.add(new Vector3f(0, 1, 0));

        ArrayList<Vector2f> textures = new ArrayList<>();
        textures.add(new Vector2f(0, 0));
        textures.add(new Vector2f(1, 0));
        textures.add(new Vector2f(0, 1));

        ArrayList<ArrayList<Integer>[]> faces = new ArrayList<>();
        //полигон 1 с vt
        faces.add(createFace(new int[]{0, 1, 2}, new int[]{0, 1, 2}, null));
        //полигон 2 без vt
        faces.add(createFace(new int[]{0, 1, 2}, null, null));

        ArrayList<Integer> lines = new ArrayList<>();
        lines.add(10);
        lines.add(11);

        ObjReaderException ex = assertThrows(
                ObjReaderException.class,
                () -> ObjReader.checkReadData(vertices, textures, new ArrayList<>(), faces, lines)
        );

        assertTrue(ex.getMessage().contains("нельзя смешивать face с vt"),
                "Сообщение должно объяснять запрет смешивания vt и без vt");
    }

    private static String readResourceText(String resourcePath) {
        try (InputStream is = ObjReaderTest.class.getResourceAsStream(resourcePath)) {
            assertNotNull(is, "Ресурс не найден: " + resourcePath);
            String s = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return s.replace("\uFEFF", "");
        } catch (Exception e) {
            throw new RuntimeException("Не удалось прочитать ресурс: " + resourcePath, e);
        }
    }

    @Test
    void testLoadAllObjModelsFromResources_shouldNotThrow() {
        String base = "/3DModels/SimpleModelsForReaderTests/";


        String[] validFiles = {
                "LoadingTest.obj",
                "NonManifold.obj",
                "NonManifold2.obj",
                "Teapot.obj",
                "Teapot01.obj",
                "Teapot02.obj",
                "TeapotHoudini.obj",
                "TeapotMaterials.obj",
                "TeapotNoUV.obj",
                "TeapotNoUVWithTexVertices.obj",
                "TeapotPolygroups.obj",
                "TeapotWithUniqueUVs.obj",
                "Test02.obj",
                "Test03.obj",
                "Test04.obj",
                "Test05.obj",
                "Test06.obj",
                "Test07.obj",
                "Torus.obj",
                "Torus01.obj",
                "Torus02.obj",
                "Torus03.obj",
                "Triangle.obj",
                "ТестНаРусском.obj"
        };

        for (String file : validFiles) {
            String content = readResourceText(base + file);

            assertDoesNotThrow(() -> {
                Model model = ObjReader.readModelFromFile(content, file, new HashMap<>());
                assertNotNull(model, "Model не должен быть null для " + file);
                assertNotNull(model.vertices, "vertices не должен быть null для " + file);
                assertNotNull(model.polygons, "polygons не должен быть null для " + file);
                assertFalse(model.vertices.isEmpty(), "vertices должен быть не пуст для " + file);
                assertFalse(model.polygons.isEmpty(), "polygons должен быть не пуст для " + file);

                String expectedName = ObjReader.safeModelNameFromFilename(file);
                assertEquals(expectedName, model.modelName, "Неверное имя модели для " + file);
            }, "Не должно падать при чтении: " + file);
        }
    }

    @Test
    void testTeapotInvalidVertexCount_shouldThrow() {
        String base = "/3DModels/SimpleModelsForReaderTests/";
        String file = "TeapotInvalidVertexCount.obj";
        String content = readResourceText(base + file);

        ObjReaderException ex = assertThrows(
                ObjReaderException.class,
                () -> ObjReader.readModelFromFile(content, file, new HashMap<>()),
                "Этот файл должен падать по заданию"
        );

        String msg = ex.getMessage();
        assertNotNull(msg);

        assertTrue(msg.contains("3859"), "Ожидаем строку 3859 в сообщении, msg=" + msg);
        assertTrue(msg.contains("Индекс вершины выходит за границы"), "Ожидаем причину про индекс вершины, msg=" + msg);
        assertTrue(msg.contains("529"), "Ожидаем индекс 529 в сообщении, msg=" + msg);
        assertTrue(msg.contains("0..528"), "Ожидаем диапазон 0..528 в сообщении, msg=" + msg);
    }


}
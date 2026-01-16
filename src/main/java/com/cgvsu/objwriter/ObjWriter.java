package com.cgvsu.objwriter;

import com.cgvsu.math.vectors.Vector2f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Vertex;
import com.cgvsu.modelOperations.MyVertexNormalCalc;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ObjWriter {

    public static boolean writeModelToFile(Model model, String filePath){

        //Проверить на корректность модель
        //Если у какой то отдельной вершины нет vt но в целом у модели есть текстура то мейби стоит записать туда (0,0) чтобы не поехала индексация

        boolean hasNormals = false;

        for (Vertex v : model.vertices) {
            if (v.normal != null) {
                hasNormals = true;
                break;
            }
        }

        if(!hasNormals){
            //Мы при чтении не учитываем нормали из файла, но чтобы записанная модель, могла использоваться в других программах корректно стоит нормали перерассчитывать тоже по умолчанию
            MyVertexNormalCalc calc = new MyVertexNormalCalc();
            calc.calculateVertexNormals(model);

        }

        //Поскольку текстурные координаты мы не можем добавлять в программе, то если модель не имела текстуру при загрузке, то и при сохранении ей не нужны текстурные корды
        boolean hasTexCoords = model.getHasTextureVertex();
        if (hasTexCoords) {
            //Чтобы предотвратить частичный UV у модели
            for (int idx : model.polygonsTextureCoordinateIndices) {
                if (idx < 0) {
                    hasTexCoords = false;
                    break;
                }
            }
        }

        //Для преобразования нашего Vertex в плоский список vt
        ArrayList<Vector2f> globalVt = new ArrayList<>();
        int[] globalVtIndexByCorner = null; //Глобальные индексы vt для конкретного угла полигона (по индексу вершины polygons)

        if (hasTexCoords) {
            int cornersCount = model.polygons.size();
            globalVtIndexByCorner = new int[cornersCount];

            for (int corner = 0; corner < cornersCount; corner++) {
                Vector2f uv = model.getTextureCoordinateForPolygonVertex(corner); //Получаем от конкретного индекса вершины из массива polygons чтобы вычленить нужный UV
                if (uv == null) uv = new Vector2f(0, 0); //Делаем, чтобы не падала запись OBJ

                int globalIdx = findOrAddVt(globalVt, uv);
                globalVtIndexByCorner[corner] = globalIdx;
            }

        }


        Path path = Paths.get(filePath);
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {

            writer.write("# Exported by ObjWriter\n");
            writer.write("# Model: " + model.modelName + "\n\n");

            //Запись вершин по порядку
            for (Vertex v : model.vertices) {
                writer.write(String.format(Locale.US, "v %f %f %f\n", v.position.getX(), v.position.getY(), v.position.getZ()));
            }
            writer.write("\n");

            //Запись текстурных координат
            if (hasTexCoords) {
                for (Vector2f vt : globalVt) {
                    writer.write(String.format(Locale.US, "vt %f %f\n", vt.getX(), vt.getY()));
                }
                writer.write("\n");
            }

            for (Vertex v : model.vertices) {
                writer.write(String.format(Locale.US, "vn %f %f %f\n", v.normal.getX(), v.normal.getY(), v.normal.getZ()));
            }
            writer.write("\n");

            //Запись полигонов
            int polygonsCount = model.polygonsBoundaries.size();
            for (int face = 0; face < polygonsCount; face++) {
                int start = model.polygonsBoundaries.get(face);
                int end = (face + 1 < polygonsCount)
                        ? model.polygonsBoundaries.get(face + 1)
                        : model.polygons.size();

                StringBuilder faceLine = new StringBuilder("f");

                for (int i = start; i < end; i++) {
                    //i индекс угла в плоском массиве
                    int vertexIndex = model.polygons.get(i);

                    //OBJ индексы начинаются с 1
                    int objVIndex = vertexIndex + 1;

                    faceLine.append(" ");

                    //У нас формат может быть либо v/vt/vn либо v//vn
                    if (hasTexCoords) {
                        int objVt = globalVtIndexByCorner[i] + 1; //Какой vt для данного угла i
                        //vn у нас идёт 1:1 с v
                        faceLine.append(objVIndex).append('/').append(objVt).append('/').append(objVIndex);
                    } else {
                        faceLine.append(objVIndex).append("//").append(objVIndex);
                    }
                }

                writer.write(faceLine.toString());
                writer.write("\n");
            }


        } catch (Exception e) {
            throw new RuntimeException("Ошибка записи в OBJ файл " + filePath, e);
        }
        return true;
    }

    private static int findOrAddVt(final ArrayList<Vector2f> globalVt, final Vector2f uv) {
        // Возвращает индекс vt в globalVt относительно нуля, если такого uv ещё нет добавляет и возвращает новый индекс, чтобы uv были уникальны
        for (int i = 0; i < globalVt.size(); i++) {
            if (globalVt.get(i).equals(uv)) {
                return i;
            }
        }
        globalVt.add(new Vector2f(uv.getX(), uv.getY()));
        return globalVt.size() - 1;
    }

}

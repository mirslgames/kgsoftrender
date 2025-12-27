package com.cgvsu.objwriter;

import com.cgvsu.model.Model;
import com.cgvsu.model.Vertex;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Locale;

public class ObjWriter {

    public static boolean writeModelToFile(Model model, String filePath){

        //Проверить на корректность модель
        //Следует проверять что количество текстурных координат совпадает с вершинами
        //Если у какой то отдельной вершины нет vt но в целом у модели есть текстура то мейби стоит записать туда (0,0) чтобы не поехала индексация

        //Также возможно стоит вынести запись вершин, нормалей и vt в отдельный метод для тестов

        String fileName = model.modelName + ".obj";

        boolean hasTexCoords = false;
        boolean hasNormals = false;

        for (Vertex v : model.vertices) {
            if (v.textureCoordinate != null) hasTexCoords = true;
            if (v.normal != null) hasNormals = true;
            if (hasTexCoords && hasNormals) break;
        }

        if(!hasNormals){
            //Перерассчитываем нормали
            //Мы при чтении не учитываем нормали из файла но чтобы записанная модель, могла использоваться в других программах корректно стоит нормали записывать тоже по умолчанию
        }

        //Поскольку мы добавлять текстурные координаты не можем в программе, то если модель не имела текстуру при загрузке, то и при сохранении ей не нужны текстурные корды
        hasTexCoords = hasTexCoords && model.hasTexture;

        Path path = Paths.get(filePath);
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {

            writer.write("# Exported by ObjWriter\n");
            writer.write("# Model: " + model.modelName + "\n\n");

            //Запись вершин по порядку
            for (Vertex v : model.vertices) {
                writer.write(String.format(Locale.US, "v %f %f %f\n", v.position.x, v.position.y, v.position.z));
            }
            writer.write("\n");

            //Запись текстурных координат
            if(hasTexCoords){
                for (Vertex v : model.vertices) {
                    writer.write(String.format(Locale.US, "vt %f %f\n", v.textureCoordinate.x, v.textureCoordinate.y));
                }
                writer.write("\n");
            }

            //Запись нормалей вершин (Жду калькулятор нормалей от леши)
            /*for (Vertex v : model.vertices) {
                writer.write(String.format(Locale.US, "vn %f %f %f\n", v.normal.x, v.normal.y, v.normal.z));
            }
            writer.write("\n");*/

            //Запись полигонов
            int polygonsCount = model.polygonsBoundaries.size();
            for (int face = 0; face < polygonsCount; face++) {
                int start = model.polygonsBoundaries.get(face);
                int end = (face + 1 < polygonsCount)
                        ? model.polygonsBoundaries.get(face + 1)
                        : model.polygons.size();

                StringBuilder faceLine = new StringBuilder("f");

                for (int i = start; i < end; i++) {
                    int vertexIndex = model.polygons.get(i);

                    // OBJ индексы начинаются с 1
                    int objIndex = vertexIndex + 1;

                    faceLine.append(" ");

                    // У нас формат может быть либо v/vt/vn либо v//vn
                    if (hasTexCoords) {
                        faceLine.append(objIndex).append("/").append(objIndex).append("/").append(objIndex);
                    } else{
                        faceLine.append(objIndex).append("//").append(objIndex);
                    }
                }

                writer.write(faceLine.toString());
                writer.write("\n");
            }


        } catch (Exception e) {
            throw new RuntimeException("Failed to write OBJ to " + filePath, e);
        }
        return true;
    }


}

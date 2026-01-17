package com.cgvsu.model;

<<<<<<< Updated upstream
=======


import com.cgvsu.math.vectors.Vector2f;
import com.cgvsu.math.vectors.Vector3f;
import com.cgvsu.modelOperations.MyVertexNormalCalc;
>>>>>>> Stashed changes
import com.cgvsu.modelOperations.TriangulationAlgorithm;
import javafx.scene.image.Image;
import com.cgvsu.math.vectors.Vector2f;
import com.cgvsu.math.vectors.Vector3f;
import com.cgvsu.modelOperations.MyVertexNormalCalc;
import java.util.*;

public class Model {

    public String modelName;
    public ArrayList<Vertex> vertices = new ArrayList<>(); //Вершины у модельки
    public ArrayList<Integer> polygons = new ArrayList<Integer>(); //Индексы на конкретные вершины из списка для полигонов
    public ArrayList<Integer> polygonsBoundaries = new ArrayList<>(); //Номер индекса с которого идут вершины для данного полигона (старт)
    public ArrayList<Integer> polygonsTextureCoordinateIndices = new ArrayList<>();
<<<<<<< Updated upstream

    public boolean hasTexture;
=======
    public boolean hasTextureCoordinates;
>>>>>>> Stashed changes
    public Image texture;
    public String textureName;
    //todo: дефолтная текстура

    //Положение модельки в сцене
    public Transform currentTransform;
    //История трансформаций где последняя должна совпадать с текущей
    public ArrayList<Transform> transformHistory;

    //Отвечает по смыслу за наличие VT, может ли модель использовать текстуру
    public boolean getHasTextureVertex() {
        if (polygonsTextureCoordinateIndices == null || polygons == null || vertices == null ||
                polygonsTextureCoordinateIndices.size() != polygons.size()){
            return false;
        }

        //Если хотя бы у одного угла localUvIndex < 0, считаем что vt нет и использовать текстуру нельзя
        for (int i = 0; i < polygonsTextureCoordinateIndices.size(); i++) {
            if (polygonsTextureCoordinateIndices.get(i) < 0) return false;
        }
        return true;
    }


    //Получаем UV по индексу вершины для угла полигона
    public Vector2f getTextureCoordinateForPolygonVertex(int polygonVertexGlobalIndex) {
        if (!getHasTextureVertex()) return null;
        if (polygonVertexGlobalIndex < 0 || polygonVertexGlobalIndex >= polygons.size()) return null;

        int vIndex = polygons.get(polygonVertexGlobalIndex);
        int uvLocalIndex = polygonsTextureCoordinateIndices.get(polygonVertexGlobalIndex);
        return vertices.get(vIndex).getTextureCoordinate(uvLocalIndex);
    }
<<<<<<< Updated upstream



    public Model() {

    }

    public static Model constructModelFromReadData(
            ArrayList<Vector3f> readVertices,
            ArrayList<Vector2f> readTextureVertices,
            ArrayList<Vector3f> readNormals,
            ArrayList<ArrayList<Integer>[]> readPolygonsIndices,
            String filename,
            boolean resultCheck
    ) {
        if (resultCheck) {
            try {
                Model result = new Model();
                result.modelName = filename;
                result.currentTransform = new Transform(0, 0, 0, 0, 0, 0, 1, 1, 1);
                result.hasTexture = false;

                result.vertices = new ArrayList<>();
                for (int i = 0; i < readVertices.size(); i++) {
                    Vertex vertex = new Vertex();
                    vertex.position = readVertices.get(i);
                    vertex.normal = null;
                    result.vertices.add(vertex);
                }

                boolean fileHasVt = readTextureVertices != null && !readTextureVertices.isEmpty();
                result.polygons = new ArrayList<>();
                result.polygonsBoundaries = new ArrayList<>();
                result.polygonsTextureCoordinateIndices = new ArrayList<>();


                for (ArrayList<Integer>[] polygon : readPolygonsIndices) {
                    if (polygon == null || polygon.length < 1 || polygon[0] == null || polygon[0].size() < 3) {
                        throw new RuntimeException("Некорректный полигон");
                    }

                    ArrayList<Integer> vertexIds = polygon[0];
                    ArrayList<Integer> textureVertexIds = (polygon.length > 1) ? polygon[1] : null;
                    result.polygonsBoundaries.add(result.polygons.size()); //Формируем массив с границами полигонов


                    for (int i = 0; i < vertexIds.size(); i++) {
                        int vertexIndex = vertexIds.get(i);
                        result.polygons.add(vertexIndex);

                        int localUvIndex = -1;
                        if (fileHasVt && textureVertexIds != null && !textureVertexIds.isEmpty()) {
                            int globalVtIndex = textureVertexIds.get(i); //Получаем индекс vt от полигона
                            Vector2f uv = readTextureVertices.get(globalVtIndex); //Получаем конкретную UV по индексу vt из прочитанных данных
                            localUvIndex = result.vertices.get(vertexIndex).getOrAddTextureCoordinate(uv); //Получаем локальный индекс UV внутри Vertex
                        }
                        result.polygonsTextureCoordinateIndices.add(localUvIndex);
                    }
                }

                MyVertexNormalCalc calc = new MyVertexNormalCalc();
                calc.calculateVertexNormals(result);

                return result;
            } catch (Exception exception) {
                throw new RuntimeException("Ошибка при построении модели на основе прочитанных данных: " + exception.getMessage());
            }
        }
        throw new RuntimeException("Прочитанные данные не корректны");
    }

=======
    // Положение модельки в сцене
    public Transform currentTransform;
    // История трансформаций где последняя должна совпадать с текущей
    public ArrayList<Transform> transformHistory;
    /**
     * Триангулирует все полигоны в модели, сохраняя индексы UV (локальные) синхронно с вершинами.
     */
>>>>>>> Stashed changes
    public void triangulate() {
        if (polygonsBoundaries == null || polygonsBoundaries.isEmpty()) {
            return;
        }

        ArrayList<Integer> newPolygons = new ArrayList<>();
        ArrayList<Integer> newTextureLocalIndices = new ArrayList<>();
        ArrayList<Integer> newBoundaries = new ArrayList<>();

        int polygonCount = polygonsBoundaries.size();
        for (int polygonInd = 0; polygonInd < polygonCount; polygonInd++) {
            int start = polygonsBoundaries.get(polygonInd);
            int end = (polygonInd + 1 < polygonCount)
                    ? polygonsBoundaries.get(polygonInd + 1)
                    : polygons.size();

            int vertexCount = end - start;
            if (vertexCount < 3) {
                continue;
            }

            List<Integer> polygonVertices = new ArrayList<>(polygons.subList(start, end));
            List<Integer> polygonVtLocal = new ArrayList<>(polygonsTextureCoordinateIndices.subList(start, end));

            //Триангулируем по позициям 0..N-1 и применяем те же позиции к обоим спискам.
            List<List<Integer>> trianglesPos = TriangulationAlgorithm.triangulatePositions(vertexCount);
            for (List<Integer> triPos : trianglesPos) {
                newBoundaries.add(newPolygons.size());
                for (int pos : triPos) {
                    newPolygons.add(polygonVertices.get(pos));
                    newTextureLocalIndices.add(polygonVtLocal.get(pos));
                }
            }
        }

        polygons = newPolygons;
        polygonsTextureCoordinateIndices = newTextureLocalIndices;
        polygonsBoundaries = newBoundaries;
    }
<<<<<<< Updated upstream
=======
    public static Model constructModelFromReadData(
            final ArrayList<Vector3f> readVertices,
            final ArrayList<Vector2f> readTextureVertices,
            final ArrayList<Vector3f> readNormals,
            final ArrayList<ArrayList<Integer>[]> readPolygonsIndices,
            final String modelName,
            final boolean dataIsValid
    ) {
        // dataIsValid оставлен ради совместимости с текущим ObjReader.
        // Если checkReadData вернул false — в нормальной ситуации сюда вообще не должны попадать.
        if (!dataIsValid) {
            throw new IllegalArgumentException("Некорректные данные для построения модели.");
        }

        Model result = new Model();
        result.modelName = modelName;

        // 1) Создаём геометрические вершины
        result.vertices = new ArrayList<>(readVertices.size());
        for (Vector3f p : readVertices) {
            result.vertices.add(new Vertex(p));
        }

        // 2) Если vt отсутствуют или ObjReader их очистил — считаем, что UV нет.
        final boolean fileHasVt = readTextureVertices != null && !readTextureVertices.isEmpty();

        // 3) Плоские массивы полигонов (v-индексы) + параллельный массив локальных UV индексов
        result.polygons = new ArrayList<>();
        result.polygonsBoundaries = new ArrayList<>();
        result.polygonsTextureCoordinateIndices = new ArrayList<>();

        for (ArrayList<Integer>[] face : readPolygonsIndices) {
            if (face == null || face.length < 1 || face[0] == null || face[0].size() < 3) {
                continue;
            }

            ArrayList<Integer> vIdx = face[0];
            ArrayList<Integer> vtIdx = (face.length > 1) ? face[1] : null;

            // boundary = индекс первого угла этого полигона в плоских массивах
            result.polygonsBoundaries.add(result.polygons.size());

            for (int i = 0; i < vIdx.size(); i++) {
                int vertexIndex = vIdx.get(i);
                result.polygons.add(vertexIndex);

                int localUvIndex = -1;
                if (fileHasVt && vtIdx != null && !vtIdx.isEmpty()) {
                    int globalVtIndex = vtIdx.get(i);
                    Vector2f uv = readTextureVertices.get(globalVtIndex);
                    localUvIndex = result.vertices.get(vertexIndex).getOrAddTextureCoordinate(uv);
                    result.hasTextureCoordinates = true;
                }
                result.polygonsTextureCoordinateIndices.add(localUvIndex);
            }
        }

        // 4) Нормали из файла игнорируем (они тоже могут быть с отдельной индексацией как vt).
        // Чтобы освещение сразу работало красиво, пересчитаем нормали по геометрии.
        new MyVertexNormalCalc().calculateVertexNormals(result);

        return result;
    }




>>>>>>> Stashed changes
}


package com.cgvsu.objreader;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Vertex;
import com.cgvsu.sceneview.SceneManager;
import javafx.scene.Scene;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class ObjReader {

	//todo: Доделать ридер, добавить врайтер для модельки, а также тестами покрыть

	private static final String OBJ_VERTEX_TOKEN = "v";
	private static final String OBJ_TEXTURE_TOKEN = "vt";
	private static final String OBJ_NORMAL_TOKEN = "vn";
	private static final String OBJ_FACE_TOKEN = "f";

	public static Model read(String fileContent, String filename) {
		Model result = new Model();

		ArrayList<Vector3f> readVertices = new ArrayList<>();
		ArrayList<Vector2f> readTextureVertices = new ArrayList<>();
		ArrayList<Vector3f> readNormals = new ArrayList<>();
		ArrayList<ArrayList<Integer>[]> readPolygonsIndices = new ArrayList<>();

		int lineInd = 0;
		Scanner scanner = new Scanner(fileContent);
		while (scanner.hasNextLine()) {
			final String line = scanner.nextLine().trim();
			ArrayList<String> wordsInLine = new ArrayList<String>(Arrays.asList(line.split("\\s+")));
			if (wordsInLine.isEmpty()) {
				continue;
			}

			final String token = wordsInLine.get(0);
			wordsInLine.remove(0);

			++lineInd;
			switch (token) {
				// Для структур типа вершин методы написаны так, чтобы ничего не знать о внешней среде.
				// Они принимают только то, что им нужно для работы, а возвращают только то, что могут создать.
				// Исключение - индекс строки. Он прокидывается, чтобы выводить сообщение об ошибке.
				// Могло быть иначе. Например, метод parseVertex мог вместо возвращения вершины принимать вектор вершин
				// модели или сам класс модели, работать с ним.
				// Но такой подход может привести к большему количеству ошибок в коде. Например, в нем что-то может
				// тайно сделаться с классом модели.
				// А еще это портит читаемость
				// И не стоит забывать про тесты. Чем проще вам задать данные для теста, проверить, что метод рабочий,
				// тем лучше.
				case OBJ_VERTEX_TOKEN -> readVertices.add(parseVertex(wordsInLine, lineInd));
				case OBJ_TEXTURE_TOKEN -> readTextureVertices.add(parseTextureVertex(wordsInLine, lineInd));
				case OBJ_NORMAL_TOKEN -> readNormals.add(parseNormal(wordsInLine, lineInd));
				case OBJ_FACE_TOKEN -> readPolygonsIndices.add(parseFace(wordsInLine, lineInd));
				default -> {}
			}
		}

		result = constructModelFromReadData(readVertices, readTextureVertices, readNormals, readPolygonsIndices);
		String targetModelName =  filename.substring(0, filename.length() - 4); //Удаляем .obj
		result.modelName = validateAndCorrectDuplicateModelName(targetModelName);
		return result;
	}

	protected static String validateAndCorrectDuplicateModelName(String targetModelName){
		String result = targetModelName;
		if (SceneManager.historyModelName.containsKey(targetModelName)){
			int c = SceneManager.historyModelName.get(targetModelName);
			result += String.format(" (%d)", ++c);
		}
		return result;
	}

	protected static Model constructModelFromReadData(ArrayList<Vector3f> readVertices,
		ArrayList<Vector2f> readTextureVertices,
		ArrayList<Vector3f> readNormals,
		ArrayList<ArrayList<Integer>[]> readPolygonsIndices){

		Model result = new Model();

		if (checkReadData(readVertices, readTextureVertices, readNormals, readPolygonsIndices)){


			ArrayList<Vertex> modelVertices = new ArrayList<>();
			ArrayList<Integer> modelVertexIndices = new ArrayList<>();
			ArrayList<Integer> modelPolygonsIndices = new ArrayList<>();

			for(int i = 0; i < readVertices.size(); i++){
				Vertex currentVertex = new Vertex();
				currentVertex.position = readVertices.get(i);
				currentVertex.normal = null;
				currentVertex.textureCoordinate = null;
				modelVertices.add(currentVertex);
			}

			if (readNormals.isEmpty()){
				//Надо рассчитать нормали вершин у модельки (пометить флаг в классе модельки а просчитать позже)
				//Возможно сделать так что мы всегда игнорируем считанные нормали вершин и сами перерасчитываем их
			}
			if (readTextureVertices.isEmpty()){
				//Помечаем что модель без текстуры в классе model
			}

			int polygonIndexCount = 0; // Счетчик для индексов полигонов относительно массива индексов на вершины
			//Теперь прокинуть полигоны в индексы на вершины только
			for (ArrayList<Integer>[] polygon : readPolygonsIndices){
				modelPolygonsIndices.add(polygonIndexCount);
				for (Integer vertexIndex : polygon[0]){
					modelVertexIndices.add(vertexIndex);
					polygonIndexCount++;
				}
			}

			result.vertices = modelVertices;
			result.polygons = modelVertexIndices;
			result.polygonsBoundaries = modelPolygonsIndices;
		}

		//Сделать обработку ошибки при загрузке модели
		return result;
	}

	protected static boolean checkReadData(ArrayList<Vector3f> readVertices,
		ArrayList<Vector2f> readTextureVertices,
		ArrayList<Vector3f> readNormals,
		ArrayList<ArrayList<Integer>[]> readPolygonsIndices){

		if (readVertices == null || readVertices.isEmpty()){
			return false;
		}
		/*if (!readTextureVertices.isEmpty() && readVertices.size() != readTextureVertices.size()){
			return false;
		}*/

		return true;
	}

	// Всем методам кроме основного я поставил модификатор доступа protected, чтобы обращаться к ним в тестах
	protected static Vector3f parseVertex(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		try {
			return new Vector3f(
					Float.parseFloat(wordsInLineWithoutToken.get(0)),
					Float.parseFloat(wordsInLineWithoutToken.get(1)),
					Float.parseFloat(wordsInLineWithoutToken.get(2)));

		} catch(NumberFormatException e) {
			throw new ObjReaderException("Failed to parse float value.", lineInd);

		} catch(IndexOutOfBoundsException e) {
			throw new ObjReaderException("Too few vertex arguments.", lineInd);
		}
	}

	protected static Vector2f parseTextureVertex(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		try {
			return new Vector2f(
					Float.parseFloat(wordsInLineWithoutToken.get(0)),
					Float.parseFloat(wordsInLineWithoutToken.get(1)));

		} catch(NumberFormatException e) {
			throw new ObjReaderException("Failed to parse float value.", lineInd);

		} catch(IndexOutOfBoundsException e) {
			throw new ObjReaderException("Too few texture vertex arguments.", lineInd);
		}
	}

	protected static Vector3f parseNormal(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		try {
			return new Vector3f(
					Float.parseFloat(wordsInLineWithoutToken.get(0)),
					Float.parseFloat(wordsInLineWithoutToken.get(1)),
					Float.parseFloat(wordsInLineWithoutToken.get(2)));

		} catch(NumberFormatException e) {
			throw new ObjReaderException("Failed to parse float value.", lineInd);

		} catch(IndexOutOfBoundsException e) {
			throw new ObjReaderException("Too few normal arguments.", lineInd);
		}
	}

	protected static ArrayList<Integer>[] parseFace(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		ArrayList<Integer> onePolygonVertexIndices = new ArrayList<Integer>();
		ArrayList<Integer> onePolygonTextureVertexIndices = new ArrayList<Integer>();
		ArrayList<Integer> onePolygonNormalIndices = new ArrayList<Integer>();

		for (String s : wordsInLineWithoutToken) {
			if (s.contains("//")){
				parseFaceWordDoubleSlash(s, onePolygonVertexIndices, onePolygonNormalIndices, lineInd);
			} else {
				parseFaceWordSingleSlash(s, onePolygonVertexIndices, onePolygonTextureVertexIndices, onePolygonNormalIndices, lineInd);
			}
		}
		ArrayList<Integer>[] resultFace = new ArrayList[3];
		resultFace[0] = onePolygonVertexIndices;
		resultFace[1] = onePolygonTextureVertexIndices;
		resultFace[2] = onePolygonNormalIndices;
		return resultFace;
	}


	protected static void parseFaceWordDoubleSlash(String wordInLine,
		ArrayList<Integer> onePolygonVertexIndices,
		ArrayList<Integer> onePolygonNormalIndices,
		int lineInd){

		try {
			String[] wordIndices = wordInLine.split("//");
			onePolygonVertexIndices.add(Integer.parseInt(wordIndices[0]) - 1);
			onePolygonNormalIndices.add(Integer.parseInt(wordIndices[1]) - 1);
		} catch(NumberFormatException e) {
			throw new ObjReaderException("Failed to parse int value.", lineInd);
		}

	}


	protected static void parseFaceWordSingleSlash(
			String wordInLine,
			ArrayList<Integer> onePolygonVertexIndices,
			ArrayList<Integer> onePolygonTextureVertexIndices,
			ArrayList<Integer> onePolygonNormalIndices,
			int lineInd) {
		try {
			String[] wordIndices = wordInLine.split("/");
			switch (wordIndices.length) {
				case 1 -> {
					onePolygonVertexIndices.add(Integer.parseInt(wordIndices[0]) - 1);
				}
				case 2 -> {
					onePolygonVertexIndices.add(Integer.parseInt(wordIndices[0]) - 1);
					onePolygonTextureVertexIndices.add(Integer.parseInt(wordIndices[1]) - 1);
				}
				case 3 -> {
					onePolygonVertexIndices.add(Integer.parseInt(wordIndices[0]) - 1);
					onePolygonNormalIndices.add(Integer.parseInt(wordIndices[2]) - 1);
					if (!wordIndices[1].equals("")) {
						onePolygonTextureVertexIndices.add(Integer.parseInt(wordIndices[1]) - 1);
					}
				}
				default -> {
					throw new ObjReaderException("Invalid element size.", lineInd);
				}
			}

		} catch(NumberFormatException e) {
			throw new ObjReaderException("Failed to parse int value.", lineInd);

		} catch(IndexOutOfBoundsException e) {
			throw new ObjReaderException("Too few arguments.", lineInd);
		}
	}
}

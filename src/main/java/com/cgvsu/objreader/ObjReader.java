package com.cgvsu.objreader;


import com.cgvsu.math.vectors.Vector2f;
import com.cgvsu.math.vectors.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Vertex;
import com.cgvsu.modelOperations.MyVertexNormalCalc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public class ObjReader {

	private static final String OBJ_VERTEX_TOKEN = "v";
	private static final String OBJ_TEXTURE_TOKEN = "vt";
	private static final String OBJ_NORMAL_TOKEN = "vn";
	private static final String OBJ_FACE_TOKEN = "f";


	public static Model readModelFromFile(String fileContent, String filename, HashMap<String, Integer> historyModelName) {
		ArrayList<Vector3f> readVertices = new ArrayList<>();
		ArrayList<Vector2f> readTextureVertices = new ArrayList<>();
		ArrayList<Vector3f> readNormals = new ArrayList<>();
		ArrayList<ArrayList<Integer>[]> readPolygonsIndices = new ArrayList<>();

		ArrayList<Integer> faceLineNumbers = new ArrayList<>();

		int lineInd = 0;
		Scanner scanner = new Scanner(fileContent);
		while (scanner.hasNextLine()) {
			++lineInd;

			String rawLine = scanner.nextLine().replace("\uFEFF", "");

			int commentPos = rawLine.indexOf('#');
			if (commentPos >= 0) {
				rawLine = rawLine.substring(0, commentPos); //Чтобы комментарии справа не ломали ридер
			}

			final String line = rawLine.trim();

			if (line.isBlank()) continue;

			ArrayList<String> wordsInLine = new ArrayList<String>(Arrays.asList(line.split("\\s+")));
			if (wordsInLine.isEmpty()) {
				continue;
			}

			final String token = wordsInLine.get(0);
			wordsInLine.remove(0);

			switch (token) {
				case OBJ_VERTEX_TOKEN -> readVertices.add(parseVertex(wordsInLine, lineInd));
				case OBJ_TEXTURE_TOKEN -> readTextureVertices.add(parseTextureVertex(wordsInLine, lineInd));
				case OBJ_NORMAL_TOKEN -> readNormals.add(parseNormal(wordsInLine, lineInd));
				case OBJ_FACE_TOKEN -> {
					readPolygonsIndices.add(parseFace(wordsInLine, lineInd));
					faceLineNumbers.add(lineInd);
				}
				default -> {}
			}
		}

		boolean resultCheck = checkReadData(readVertices, readTextureVertices, readNormals, readPolygonsIndices, faceLineNumbers);
		return Model.constructModelFromReadData(readVertices, readTextureVertices, readNormals, readPolygonsIndices, safeModelNameFromFilename(filename), resultCheck);
	}

	protected static String safeModelNameFromFilename(String filename) {
		if (filename == null || filename.isBlank()) return "model";
		String f = filename.trim();
		if (f.length() >= 4 && f.toLowerCase().endsWith(".obj")) {
			return f.substring(0, f.length() - 4);
		}
		return f;
	}

	protected static FaceFormat detectFaceFormat(String wordInLine, int lineInd) {
		if (wordInLine == null || wordInLine.isBlank()) {
			throw new ObjReaderException("Пустой элемент в face.", lineInd);
		}

		if (!wordInLine.contains("/")) {
			return FaceFormat.V;
		}

		String[] parts = wordInLine.split("/", -1); //сохраняем пустые части для 1//3

		if (parts.length == 2) {
			if (parts[0].isEmpty() || parts[1].isEmpty()) {
				throw new ObjReaderException("Некорректный формат индексов в face: " + wordInLine, lineInd);
			}
			return FaceFormat.V_VT;
		}

		if (parts.length == 3) {
			if (parts[0].isEmpty()) {
				throw new ObjReaderException("Некорректный формат индексов в face: " + wordInLine, lineInd);
			}
			if (parts[1].isEmpty() && !parts[2].isEmpty()) {
				return FaceFormat.V_VN;
			}
			if (!parts[1].isEmpty() && !parts[2].isEmpty()) {
				return FaceFormat.V_VT_VN;
			}
			throw new ObjReaderException("Некорректный формат индексов в face: " + wordInLine, lineInd);
		}

		throw new ObjReaderException("Некорректное число слов для face.", lineInd);
	}


	protected static boolean checkReadData(ArrayList<Vector3f> readVertices,
										   ArrayList<Vector2f> readTextureVertices,
										   ArrayList<Vector3f> readNormals,
										   ArrayList<ArrayList<Integer>[]> readPolygonsIndices,
										   ArrayList<Integer> faceLineNumbers) {

		if (readVertices == null || readVertices.isEmpty()) return false;
		if (readPolygonsIndices == null || readPolygonsIndices.isEmpty()) return false;

		int vCount = readVertices.size();
		int vtCount = (readTextureVertices == null) ? 0 : readTextureVertices.size();
		int vnCount = (readNormals == null) ? 0 : readNormals.size();

		boolean usesVtInFaces = false;
		boolean foundFaceWithoutVt = false; //Чтобы не смешивать полигоны с UV и без UV, у нас единый подход
		int firstFaceWithoutVtLine = -1;


		for (int faceIdx = 0; faceIdx < readPolygonsIndices.size(); faceIdx++) {
			int line = (faceLineNumbers != null && faceIdx < faceLineNumbers.size())
					? faceLineNumbers.get(faceIdx)
					: -1;

			ArrayList<Integer>[] face = readPolygonsIndices.get(faceIdx);
			if (face == null || face.length < 3 || face[0] == null) {
				throw new ObjReaderException("Некорректные данные face (пустые списки индексов).", line);
			}

			ArrayList<Integer> v = face[0];
			ArrayList<Integer> vt = face[1];
			ArrayList<Integer> vn = face[2];

			//Проверка vertex-индексов
			for (int i = 0; i < v.size(); i++) {
				int idx = v.get(i);
				if (idx < 0 || idx >= vCount) {
					throw new ObjReaderException(
							"Индекс вершины выходит за границы: " + idx + ". Допустимо 0.." + (vCount - 1),
							line
					);
				}
			}

			//Если используются vt — они должны существовать и быть в диапазоне
			if (vt != null && !vt.isEmpty()) {
				usesVtInFaces = true;
				if (vtCount == 0) {
					throw new ObjReaderException("В face используются vt, но в файле нет ни одной vt.", line);
				}
				if (vt.size() != v.size()) {
					throw new ObjReaderException("Количество vt индексов не совпадает с количеством v индексов в face.", line);
				}
				for (int i = 0; i < vt.size(); i++) {
					int idx = vt.get(i);
					if (idx < 0 || idx >= vtCount) {
						throw new ObjReaderException(
								"Индекс vt выходит за границы: " + idx + ". Допустимо 0.." + (vtCount - 1),
								line
						);
					}
				}
			} else {
				foundFaceWithoutVt = true;
				if (firstFaceWithoutVtLine < 0) firstFaceWithoutVtLine = line;
			}


			if (vn != null && !vn.isEmpty()) {
				if (vnCount == 0) {
					//Игнорируем vn, потому что всё равно пересчитываем нормали сами
					continue;
				}
				if (vn.size() != v.size()) {
					throw new ObjReaderException("Количество vn индексов не совпадает с количеством v индексов в face.", line);
				}
				for (int i = 0; i < vn.size(); i++) {
					int idx = vn.get(i);
					if (idx < 0 || idx >= vnCount) {
						throw new ObjReaderException("Индекс vn выходит за границы: " + idx, line);
					}
				}
			}
		}

		if (usesVtInFaces && foundFaceWithoutVt) {
			throw new ObjReaderException("В одном OBJ-файле нельзя смешивать face с vt и face без vt.", firstFaceWithoutVtLine);
		}

		//Если vt нигде не использовались — считаем, что текстуры нет
		if (!usesVtInFaces && readTextureVertices != null) {
			readTextureVertices.clear();
		}

		return true;
	}

	// Всем методам кроме основного я поставил модификатор доступа protected, чтобы обращаться к ним в тестах
	protected static Vector3f parseVertex(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		if (wordsInLineWithoutToken.size() > 3){
			throw new ObjReaderException("Более 3 координат у вершины", lineInd);
		}
		try {
			return new Vector3f(
					Float.parseFloat(wordsInLineWithoutToken.get(0)),
					Float.parseFloat(wordsInLineWithoutToken.get(1)),
					Float.parseFloat(wordsInLineWithoutToken.get(2)));

		} catch(NumberFormatException e) {
			throw new ObjReaderException("Ошибка парсинга float значения.", lineInd);

		} catch(IndexOutOfBoundsException e) {
			throw new ObjReaderException("Неправильное количество аргументов вершин.", lineInd);
		}
	}

	protected static Vector2f parseTextureVertex(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		try {
			return new Vector2f(
					Float.parseFloat(wordsInLineWithoutToken.get(0)),
					Float.parseFloat(wordsInLineWithoutToken.get(1)));

		} catch(NumberFormatException e) {
			throw new ObjReaderException("Ошибка парсинга float значения.", lineInd);

		} catch(IndexOutOfBoundsException e) {
			throw new ObjReaderException("Неправильное количество аргументов текстурных вершин.", lineInd);
		}
	}

	protected static Vector3f parseNormal(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		try {
			return new Vector3f(
					Float.parseFloat(wordsInLineWithoutToken.get(0)),
					Float.parseFloat(wordsInLineWithoutToken.get(1)),
					Float.parseFloat(wordsInLineWithoutToken.get(2)));

		} catch(NumberFormatException e) {
			throw new ObjReaderException("Ошибка парсинга float значения.", lineInd);

		} catch(IndexOutOfBoundsException e) {
			throw new ObjReaderException("Неправильное количество аргументов для нормалей.", lineInd);
		}
	}

	protected static ArrayList<Integer>[] parseFace(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {

		if(wordsInLineWithoutToken == null || wordsInLineWithoutToken.size() < 3){
			throw new ObjReaderException("Face должен иметь минимум 3 вершины", lineInd);
		}

		ArrayList<Integer> onePolygonVertexIndices = new ArrayList<Integer>();
		ArrayList<Integer> onePolygonTextureVertexIndices = new ArrayList<Integer>();
		ArrayList<Integer> onePolygonNormalIndices = new ArrayList<Integer>();

		FaceFormat faceFormat = detectFaceFormat(wordsInLineWithoutToken.get(0), lineInd);;

		for (String s : wordsInLineWithoutToken) {
			FaceFormat current = detectFaceFormat(s, lineInd);

			if(faceFormat != current){
				throw new ObjReaderException("В одном face нельзя смешивать разные форматы индексов.", lineInd);
			}

			switch (current) {
				case V -> parseFaceWordV(s, onePolygonVertexIndices, lineInd);
				case V_VT -> parseFaceWordVt(s, onePolygonVertexIndices, onePolygonTextureVertexIndices, lineInd);
				case V_VN -> parseFaceWordVn(s, onePolygonVertexIndices, onePolygonNormalIndices, lineInd);
				case V_VT_VN -> parseFaceWordVtVn(s, onePolygonVertexIndices, onePolygonTextureVertexIndices, onePolygonNormalIndices, lineInd);
			}

			/*if (s.contains("//")){
				parseFaceWordDoubleSlash(s, onePolygonVertexIndices, onePolygonNormalIndices, lineInd);
			} else {
				parseFaceWordSingleSlash(s, onePolygonVertexIndices, onePolygonTextureVertexIndices, onePolygonNormalIndices, lineInd);
			}*/

		}

		ArrayList<Integer>[] resultFace = new ArrayList[3];
		resultFace[0] = onePolygonVertexIndices;
		resultFace[1] = onePolygonTextureVertexIndices;
		resultFace[2] = onePolygonNormalIndices;
		return resultFace;
	}

	protected static void parseFaceWordV(String wordInLine, ArrayList<Integer> v, int lineInd) {
		try {
			int vi = Integer.parseInt(wordInLine);
			if (vi == 0) throw new ObjReaderException("Индекс v не может быть 0.", lineInd);
			if (vi < 0) throw new ObjReaderException("Индекс v не может быть отрицательным.", lineInd); //Мы не поддерживаем отрицательные индексы
			v.add(vi - 1); //Чтобы индексы с 0 начинались для списка (в obj формате с 1)
		} catch (NumberFormatException e) {
			throw new ObjReaderException("Не удалось спарсить int значение.", lineInd);
		}
	}

	protected static void parseFaceWordVt(String wordInLine, ArrayList<Integer> v, ArrayList<Integer> vt, int lineInd) {
		try {
			String[] parts = wordInLine.split("/", -1);
			if (parts.length != 2) {
				throw new ObjReaderException("Некорректный формат индексов в face: " + wordInLine, lineInd);
			}
			int vi = Integer.parseInt(parts[0]);
			int vti = Integer.parseInt(parts[1]);
			if (vi == 0) throw new ObjReaderException("Индекс v не может быть 0.", lineInd);
			if (vi < 0) throw new ObjReaderException("Индекс v не может быть отрицательным.", lineInd);
			if (vti == 0) throw new ObjReaderException("Индекс vt не может быть 0.", lineInd);
			if (vti < 0) throw new ObjReaderException("Индекс vt не может быть отрицательным.", lineInd);
			v.add(vi - 1);
			vt.add(vti - 1);
		} catch (NumberFormatException e) {
			throw new ObjReaderException("Не удалось спарсить int значение.", lineInd);
		} catch (IndexOutOfBoundsException e) {
			throw new ObjReaderException("Неправильное количество аргументов", lineInd);
		}
	}

	protected static void parseFaceWordVn(String wordInLine, ArrayList<Integer> v, ArrayList<Integer> vn, int lineInd) {
		try {
			String[] parts = wordInLine.split("//", -1);
			if (parts.length != 2) {
				throw new ObjReaderException("Некорректный формат индексов в face: " + wordInLine, lineInd);
			}
			int vi = Integer.parseInt(parts[0]);
			int vni = Integer.parseInt(parts[1]);
			if (vi == 0) throw new ObjReaderException("Индекс v не может быть 0.", lineInd);
			if (vi < 0) throw new ObjReaderException("Индекс v не может быть отрицательным.", lineInd);
			if (vni == 0) throw new ObjReaderException("Индекс vn не может быть 0.", lineInd);
			if (vni < 0) throw new ObjReaderException("Индекс vn не может быть отрицательным.", lineInd);
			v.add(vi - 1);
			vn.add(vni - 1);
		} catch (NumberFormatException e) {
			throw new ObjReaderException("Не удалось спарсить int значение.", lineInd);
		} catch (IndexOutOfBoundsException e) {
			throw new ObjReaderException("Неправильное количество аргументов", lineInd);
		}
	}

	protected static void parseFaceWordVtVn(String wordInLine, ArrayList<Integer> v, ArrayList<Integer> vt, ArrayList<Integer> vn, int lineInd) {
		try {
			String[] parts = wordInLine.split("/", -1);
			if (parts.length != 3) {
				throw new ObjReaderException("Некорректный формат индексов в face: " + wordInLine, lineInd);
			}
			int vi = Integer.parseInt(parts[0]);
			int vti = Integer.parseInt(parts[1]);
			int vni = Integer.parseInt(parts[2]);

			if (vi == 0) throw new ObjReaderException("Индекс v не может быть 0.", lineInd);
			if (vi < 0) throw new ObjReaderException("Индекс v не может быть отрицательным.", lineInd);
			if (vti == 0) throw new ObjReaderException("Индекс vt не может быть 0.", lineInd);
			if (vti < 0) throw new ObjReaderException("Индекс vt не может быть отрицательным.", lineInd);
			if (vni == 0) throw new ObjReaderException("Индекс vn не может быть 0.", lineInd);
			if (vni < 0) throw new ObjReaderException("Индекс vn не может быть отрицательным.", lineInd);

			v.add(vi - 1);
			vt.add(vti - 1);
			vn.add(vni - 1);
		} catch (NumberFormatException e) {
			throw new ObjReaderException("Не удалось спарсить int значение.", lineInd);
		} catch (IndexOutOfBoundsException e) {
			throw new ObjReaderException("Неправильное количество аргументов", lineInd);
		}
	}


}

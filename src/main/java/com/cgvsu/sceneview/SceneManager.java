package com.cgvsu.sceneview;

import com.cgvsu.math.vectors.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.render_engine.Camera;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;

public class SceneManager {
    public static Camera activeCamera;
    public static ArrayList<Camera> cameras = new ArrayList<>();
    public static Dictionary<String, Camera> cacheNameCameras = new Hashtable<>(); //Чтобы быстро получить ссылку на камеру
    public static Model activeModel;
    public static ArrayList<Model> models = new ArrayList<>();
    public static Dictionary<String, Model> cacheNameSceneModels = new Hashtable<>(); // чтобы быстро найти по имени модель а не перебирать все загруженные
    public static HashMap<String, Integer> historyModelName = new HashMap<>();

    public static ArrayList<Model> originalModels = new ArrayList<>(); //Клон оригинальных  загруженных моделей
    public static Dictionary<String, Model> originalCacheNameSceneModels = new Hashtable<>();

    public static boolean drawMesh;
    public static boolean useTexture;
    public static boolean useLight;

    public static boolean isSceneEntitySelect; //По сути отвечает только за модель

    public static float lightIntensity;


    public static void initialize(){
        activeCamera = new Camera(
                new Vector3f(0, 0, 40),
                new Vector3f(0, 0, 0),
                1.0F, 1, 0.01F, 200);
        activeCamera.cameraName = "Начальная камера";
        cacheNameCameras.put(activeCamera.cameraName, activeCamera);
        cameras.add(activeCamera);
        lightIntensity = 1f;
        isSceneEntitySelect = false;

    }

    public static void createNewCamera(){
        Camera newCamera = new Camera(
                new Vector3f(0, 0, 40),
                new Vector3f(0, 0, 0),
                1.0F, 1, 0.01F, 200);
        newCamera.cameraName = String.format("Камера %d", Camera.cameraId);
        cacheNameCameras.put(newCamera.cameraName, newCamera);
        cameras.add(newCamera);
    }

    public static void deleteCameraFromScene(String cameraName){
        Camera targetCamera = cacheNameCameras.get(cameraName);
        cameras.remove(targetCamera);
        cacheNameCameras.remove(cameraName);
    }

    public static void loadModelToScene(Model model){
        models.add(model);
        cacheNameSceneModels.put(model.modelName, model);
    }

    public static void loadOriginalModelToScene(Model model){
        originalModels.add(model);
        originalCacheNameSceneModels.put(model.modelName, model);
    }


    public static boolean removeModelFromScene(Model model) {
        if (model == null) return false;
        return removeModelFromScene(model.modelName);
    }

    public static boolean removeModelFromScene(String modelName) {
        if (modelName == null) return false;

        Model model = cacheNameSceneModels.get(modelName);
        if (model == null) return false;

        for(int i = 0; i < models.size(); i++){
            if (models.get(i).modelName.equals(modelName)){
                models.remove(i);
            }
        }
        cacheNameSceneModels.remove(modelName);

        if (activeModel != null && activeModel.modelName != null && activeModel.modelName.equals(model.modelName)) {
            activeModel = null;
            isSceneEntitySelect = false;
        }

        for(int i = 0; i < originalModels.size(); i++){
            if (originalModels.get(i).modelName.equals(modelName)){
                originalModels.remove(i);
            }
        }
        originalCacheNameSceneModels.remove(modelName);

        return true;
    }

    public static Model getOriginalModelFromModifiedModel(Model model){
        return originalCacheNameSceneModels.get(model.modelName);
    }

}

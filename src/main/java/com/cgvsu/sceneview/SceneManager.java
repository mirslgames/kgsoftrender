package com.cgvsu.sceneview;

import com.cgvsu.math.vectors.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.render_engine.Camera;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;

public class SceneManager {
    public static Camera activeCamera;
    public static ArrayList<Camera> cameras;
    public static Model activeModel;
    public static ArrayList<Model> models = new ArrayList<>();
    public static Dictionary<String, Model> cacheNameSceneModels = new Hashtable<>(); // чтобы быстро найти по имени модель а не перебирать все загруженные
    public static HashMap<String, Integer> historyModelName = new HashMap<>();
    //todo: Подумать как организовать источник света + SceneEntity


    public static boolean drawMesh;
    public static boolean useTexture;
    public static boolean useLight;

    public static boolean isSceneEntitySelect;

    public static float lightIntensity;


    public static void initialize(){
        activeCamera = new Camera(
                new Vector3f(0, 0, 100),
                new Vector3f(0, 0, 0),
                1.0F, 1, 0.01F, 100);
        isSceneEntitySelect = false;

    }

    public static void loadModelToScene(Model model){
        models.add(model);
        cacheNameSceneModels.put(model.modelName, model);
    }


    public static boolean removeModelFromScene(Model model) {
        if (model == null) return false;
        return removeModelFromScene(model.modelName);
    }

    public static boolean removeModelFromScene(String modelName) {
        if (modelName == null) return false;

        Model model = cacheNameSceneModels.get(modelName);
        if (model == null) return false;

        models.remove(model);
        cacheNameSceneModels.remove(modelName);

        if (activeModel.modelName.equals(model.modelName)) {
            activeModel = null;
            isSceneEntitySelect = false;
        }
        //historyModelName не трогаем
        return true;
    }
}

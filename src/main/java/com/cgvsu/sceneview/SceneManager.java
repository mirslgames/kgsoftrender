package com.cgvsu.sceneview;

import com.cgvsu.model.Model;
import com.cgvsu.render_engine.Camera;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;

public class SceneManager {
    public static Camera activeCamera;
    public static ArrayList<Camera> cameras;
    public static Model activeModel;
    public static ArrayList<Model> models = new ArrayList<>();
    public static Dictionary<String, Model> cacheNameSceneModels = new Hashtable<>();

    public static boolean drawMesh;
    public static boolean useTexture;
    public static boolean useLight;

    public static float positionXValue;
    public static float positionYValue;
    public static float positionZValue;
    public static float rotationXValue;
    public static float rotationYValue;
    public static float rotationZValue;
    public static float scaleXValue;
    public static float scaleYValue;
    public static float scaleZValue;


    public static boolean isSceneEntitySelect;


    public static void initialize(){
        activeCamera = new Camera(
                new Vector3f(0, 0, 100),
                new Vector3f(0, 0, 0),
                1.0F, 1, 0.01F, 100);
        isSceneEntitySelect = false;

    }

    public static void loadModelToScene(String modelName, Model model){
        cacheNameSceneModels.put(modelName, model);
    }
}

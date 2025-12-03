package com.cgvsu.sceneview;

import com.cgvsu.model.Model;
import com.cgvsu.render_engine.Camera;

import javax.vecmath.Vector3f;

public class SceneManager {
    public static Camera activeCamera;
    public static Model activeModel;

    public static boolean drawMesh;
    public static boolean useTexture;
    public static boolean useLight;

    public static void initialize(){
        activeCamera = new Camera(
                new Vector3f(0, 0, 100),
                new Vector3f(0, 0, 0),
                1.0F, 1, 0.01F, 100);
    }
}

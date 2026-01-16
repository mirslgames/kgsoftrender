package com.cgvsu.sceneview;

public enum RenderMode {
    ONE_FRAME, //Один кадр по кнопке
    EVERY_FRAME, //Каждый кадр
    EVERY_TRANSFORM_FRAME, //Каждую трансформацию
    EVERY_CAMERA_MOTION_FRAME, //Каждое движение камерой
    EVERY_CAMERA_MOTION_TRANSFORM_FRAME, //Каждое движение камерой и трансформацией
}

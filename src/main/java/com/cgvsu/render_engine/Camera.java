package com.cgvsu.render_engine;


import com.cgvsu.math.matrixs.Matrix4f;
import com.cgvsu.math.vectors.Vector3f;


public class Camera {

    public Camera(
            final Vector3f position,
            final Vector3f target,
            final float fov,
            final float aspectRatio,
            final float nearPlane,
            final float farPlane) {
        this.position = position;
        this.target = target;
        this.fov = fov;
        this.aspectRatio = aspectRatio;
        this.nearPlane = nearPlane;
        this.farPlane = farPlane;
    }

    public void setPosition(final Vector3f position) {
        this.position = position;
    }

    public void setTarget(final Vector3f target) {
        this.target = target;
    }

    public void setAspectRatio(final float aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getTarget() {
        return target;
    }

    public void movePosition(final Vector3f translation) {
        this.position.add(translation);
    }

    public void moveTarget(final Vector3f translation) {
        this.target.add(target);
    }

    Matrix4f getViewMatrix() {
        return GraphicConveyor.lookAt(position, target);
    }

    Matrix4f getProjectionMatrix() {
        return GraphicConveyor.perspective(fov, aspectRatio, nearPlane, farPlane);
    }
    public Vector3f getRayToPoint(Vector3f worldPosition) {
         //проверяем входные данные
        if (worldPosition == null ||
                Float.isNaN(worldPosition.getX()) ||
                Float.isNaN(worldPosition.getY()) ||
                Float.isNaN(worldPosition.getZ())) {
            System.err.println("ERROR: Invalid world position in getRayToPoint");
            return new Vector3f(0, 0, -1); // возвращаем что-то безопасное
        }


        Vector3f cameraPos = this.getPosition();
        Vector3f ray = worldPosition.subbed(cameraPos);


        if (ray.len() == 0) {
            System.err.println("WARNING: Zero-length ray in getRayToPoint");
            return new Vector3f(0, 0, -1);
        }

        return ray.normalize();
    }
    private Vector3f position;
    private Vector3f target;
    private float fov;
    private float aspectRatio;
    private float nearPlane;
    private float farPlane;
}
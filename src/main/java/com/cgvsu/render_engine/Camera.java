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
        radius = position.subbed(target).len();

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
        this.target.add(translation);
    }

    public void zoomCamera(float deltaS) {
        radius = clamp(radius - deltaS, (float) (Math.max(nearPlane * 1.2, 50f)), 400f);
        updatePositionFromAngles();
    }

    public float getNearPlane() {
        return nearPlane;
    }

    public void rotateCamera(float yawDeg, float pitchDeg) {

        yaw += yawDeg;
        pitch += pitchDeg;

        yaw = wrapAngle(yaw);
        pitch = clamp(pitch, -89f, 89f);

        updatePositionFromAngles();

    }

    private float clamp(float v, float lo, float hi) {
        if (v < lo) {
            return lo;
        }
        if (v > hi) {
            return hi;
        }
        return v;
    }

    private float wrapAngle(float ang) {
        ang %= 360f;
        if (ang <= -180f) ang += 360f;
        if (ang > 180f) ang -= 360f;
        return ang;
    }

    private void updatePositionFromAngles() {
        double yawRad = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(pitch);



        float cosPitch = (float) Math.cos(pitchRad);
        float x = radius * cosPitch * (float) Math.sin(yawRad);
        float y = radius * (float) Math.sin(pitchRad);
        float z = radius * cosPitch * (float) Math.cos(yawRad);

        Vector3f offset = new Vector3f(x, y, z);
        position = target.added(offset);

        Vector3f forward = target.subbed(position).normalized();
        Vector3f right = forward.crossed(new Vector3f(0, 1, 0));

        if (right.len() < EPS) {
            right = new Vector3f(1, 0, 0);
        } else {
            right = right.normalized();
        }

        cameraUp = right.crossed(forward).normalized();
    }

    Matrix4f getViewMatrix() {
        return GraphicConveyor.lookAt(position, target, cameraUp);
    }

    Matrix4f getProjectionMatrix() {
        return GraphicConveyor.perspective(fov, aspectRatio, nearPlane, farPlane);
    }

    private Vector3f position;
    private Vector3f target;
    private float fov;
    private float aspectRatio;
    private float nearPlane;
    private float farPlane;

    private float pitch = 0f;
    private float yaw = 0f;

    private final float EPS = 10e-6f;
    private float radius;

    private Vector3f cameraUp = new Vector3f(0f, 1f, 0f);
}
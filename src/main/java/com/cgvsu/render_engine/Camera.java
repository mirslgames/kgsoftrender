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
        cameraId++;
    }

    public static int cameraId = 0;
    public String cameraName;

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

    public void moveCamera(float deltaXpx, float deltaYpx, int viewportW, int viewportH) {
        float d = radius;

        float vSpan = 2f * d * (float) Math.tan(fov * 0.5); // высота видимой области
        float unitsPerPxY = vSpan / (float) viewportH;
        float unitsPerPxX = (vSpan * aspectRatio) / (float) viewportW;

        Vector3f forward = target.subbed(position).normalized();
        Vector3f worldUp = new Vector3f(0, 1, 0);

        Vector3f right = worldUp.crossed(forward);
        if (right.len() < EPS) {
            right = new Vector3f(1, 0, 0);
        }
        else {
            right = right.normalized();
        }

        Vector3f up = forward.crossed(right).normalized();

        Vector3f move =
                right.multiplied(-deltaXpx * unitsPerPxX)
                        .added(up.multiplied(deltaYpx * unitsPerPxY));

        position.add(move);
        target.add(move);
    }

    public void zoomCamera(float deltaS) {
        radius = clamp(radius - deltaS, 0.5f, Float.MAX_VALUE);
        updatePositionFromAngles();
    }

    public float getNearPlane() {
        return nearPlane;
    }

    public void rotateCamera(float yawDeg, float pitchDeg) {

        Vector3f worldUp = new Vector3f(0, 1, 0);

        Vector3f offset = position.subbed(target); // <- радиус-вектор между позицией и точкой куда смотрит камера

        offset = rotateAroundAxis(offset, worldUp, (float) Math.toRadians(yawDeg)); // <- повернули

        Vector3f forward = offset.normalized();

        Vector3f right = worldUp.crossed(forward);
        if (right.len() < EPS) {
            right = new Vector3f(1, 0, 0);
        }
        else {
            right = right.normalized();
        }

        Vector3f newOffset = rotateAroundAxis(offset, right, (float) Math.toRadians(pitchDeg));

        Vector3f newForward = newOffset.multiplied(-1).normalized();
        if (Math.abs(newForward.dot(worldUp)) < 0.9995f) {
            offset = newOffset;
            forward = newForward;
        }

        position = target.added(offset);
        radius = offset.len();

        right = worldUp.crossed(forward);
        if (right.len() < EPS) {
            right = new Vector3f(1, 0, 0);
        }
        else right = right.normalized();

        cameraUp = forward.crossed(right).normalized();

        yaw = (float) Math.toDegrees(Math.atan2(offset.getX(), offset.getZ()));
        pitch = (float) Math.toDegrees(Math.asin(offset.getY() / radius));

    }

    private Vector3f rotateAroundAxis(Vector3f v, Vector3f axis, float angleRad) {
        Vector3f k = axis.normalized();
        float cos = (float) Math.cos(angleRad);
        float sin = (float) Math.sin(angleRad);

        Vector3f term1 = v.multiplied(cos);
        Vector3f term2 = k.crossed(v).multiplied(sin);
        Vector3f term3 = k.multiplied(k.dot(v) * (1f - cos));

        return term1.added(term2).added(term3);
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
        Vector3f worldUp = new Vector3f(0, 1, 0);

        Vector3f right = worldUp.crossed(forward);
        if (right.len() < EPS) right = new Vector3f(1, 0, 0);
        else right = right.normalized();

        cameraUp = forward.crossed(right).normalized();
    }

    public Matrix4f getViewMatrix() {
        return GraphicConveyor.lookAt(position, target, cameraUp);
    }

    public Matrix4f getProjectionMatrix() {
        return GraphicConveyor.perspective(fov, aspectRatio, nearPlane, farPlane);
    }

    public Vector3f getRayToPoint(Vector3f worldPosition) {
        if (worldPosition == null ||
                Float.isNaN(worldPosition.getX()) ||
                Float.isNaN(worldPosition.getY()) ||
                Float.isNaN(worldPosition.getZ())) {
            return new Vector3f(0, 0, -1); // возвращаем что-то безопасное
        }


        Vector3f cameraPos = this.getPosition();
        Vector3f ray = worldPosition.subbed(cameraPos);


        if (ray.len() == 0) {
            return new Vector3f(0, 0, -1);
        }

        return ray.normalize();
    }

    public void returnToDefaultCamera() {
        pitch = 0f;
        yaw = 0f;

        position = new Vector3f(0, 0, 50f);
        target = new Vector3f(0, 0, 0);

        cameraUp = new Vector3f(0f, 1f, 0f);

        radius = position.subbed(target).len();
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
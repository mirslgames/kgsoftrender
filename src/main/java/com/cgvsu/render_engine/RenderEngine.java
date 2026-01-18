package com.cgvsu.render_engine;

import com.cgvsu.math.matrixs.Matrix4f;
import com.cgvsu.math.point.Point2f;
import com.cgvsu.math.vectors.Vector2f;
import com.cgvsu.math.vectors.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Vertex;
import com.cgvsu.modelOperations.Rasterization;
import com.cgvsu.modelOperations.TextureMapping;
import com.cgvsu.modelOperations.ZBuffer;
import com.cgvsu.sceneview.SceneManager;
import com.cgvsu.service.ThemeSettings;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.ArrayList;

import static com.cgvsu.render_engine.GraphicConveyor.*;

public class RenderEngine {

    public static void render(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model mesh,
            final int width,
            final int height) {

        Matrix4f modelMatrix = rotateScaleTranslate(
                mesh.currentTransform.scaleX, mesh.currentTransform.scaleY, mesh.currentTransform.scaleZ,
                mesh.currentTransform.rotationX, mesh.currentTransform.rotationY, mesh.currentTransform.rotationZ,
                mesh.currentTransform.positionX, mesh.currentTransform.positionY, mesh.currentTransform.positionZ
        );

        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projMatrix = camera.getProjectionMatrix();

        Vector3f targetView = viewMatrix.multiplyOnVector(camera.getTarget());
        boolean forwardMinusZ = targetView.getZ() < 0;

        Matrix4f modelView = new Matrix4f(viewMatrix.getMatrix());
        modelView.multiply(modelMatrix);

        final int nPolygons = mesh.polygonsBoundaries.size();
        graphicsContext.setStroke(Color.web(ThemeSettings.wireframeColor));
        graphicsContext.setLineWidth(ThemeSettings.wireframeWidth);

        float near = camera.getNearPlane();

        for (int polygonInd = 0; polygonInd < nPolygons; ++polygonInd) {
            int startIndex = mesh.polygonsBoundaries.get(polygonInd);
            int endIndex = (polygonInd + 1 < nPolygons)
                    ? mesh.polygonsBoundaries.get(polygonInd + 1)
                    : mesh.polygons.size();

            int n = endIndex - startIndex;
            if (n < 2) continue;

            ArrayList<Vector3f> viewPts = new ArrayList<>(n);
            for (int i = startIndex; i < endIndex; ++i) {
                int vertexIndex = mesh.polygons.get(i);
                Vector3f p = mesh.vertices.get(vertexIndex).position;
                Vector3f viewP = modelView.multiplyOnVector(new Vector3f(p.getX(), p.getY(), p.getZ()));
                viewPts.add(viewP);
            }

            for (int i = 0; i < n; i++) {
                Vector3f a0 = viewPts.get(i);
                Vector3f b0 = viewPts.get((i + 1) % n);

                Vector3f a = new Vector3f(a0.getX(), a0.getY(), a0.getZ());
                Vector3f b = new Vector3f(b0.getX(), b0.getY(), b0.getZ());

                Vector3f[] seg = clipLineToNear(a, b, near, forwardMinusZ);
                if (seg == null) continue;

                Point2f pa = vertexToPoint(projMatrix.multiplyOnVector(seg[0]), width, height);
                Point2f pb = vertexToPoint(projMatrix.multiplyOnVector(seg[1]), width, height);

                if (!Float.isFinite(pa.getX()) || !Float.isFinite(pa.getY()) ||
                        !Float.isFinite(pb.getX()) || !Float.isFinite(pb.getY())) {
                    continue;
                }

                graphicsContext.strokeLine(pa.getX(), pa.getY(), pb.getX(), pb.getY());
            }
        }
    }

    public static void renderWithRenderingMods(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model mesh,
            final int width,
            final int height,
            ZBuffer zBuffer) {

        Matrix4f modelMatrix = rotateScaleTranslate(
                mesh.currentTransform.scaleX, mesh.currentTransform.scaleY, mesh.currentTransform.scaleZ,
                mesh.currentTransform.rotationX, mesh.currentTransform.rotationY, mesh.currentTransform.rotationZ,
                mesh.currentTransform.positionX, mesh.currentTransform.positionY, mesh.currentTransform.positionZ
        );

        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();

        Matrix4f modelViewMatrix = new Matrix4f(viewMatrix.getMatrix());
        modelViewMatrix.multiply(modelMatrix);

        float near = camera.getNearPlane();
        Vector3f targetView = viewMatrix.multiplyOnVector(camera.getTarget());
        boolean forwardMinusZ = targetView.getZ() < 0;

        // Нормаль-матрица (inverse-transpose верхней 3x3 modelMatrix)
        final float[] normalMatrix = buildNormalMatrix3x3(modelMatrix);

        Color baseColor = Color.GREEN;
        final int nPolygons = mesh.polygonsBoundaries.size();

        for (int polygonInd = 0; polygonInd < nPolygons; ++polygonInd) {
            final int startIndex = mesh.polygonsBoundaries.get(polygonInd);
            final int endIndex = (polygonInd + 1 < nPolygons)
                    ? mesh.polygonsBoundaries.get(polygonInd + 1)
                    : mesh.polygons.size();

            // Этот рендерер работает по треугольникам (берём первые 3 индекса полигона).
            if (endIndex - startIndex < 3) continue;

            Point2f p1 = null, p2 = null, p3 = null;

            // view-depth (положительный), меньше = ближе.
            float zD1 = 0, zD2 = 0, zD3 = 0;

            Vertex v1 = null, v2 = null, v3 = null;

            // Оригинальные world позиции
            Vector3f w1Orig = null, w2Orig = null, w3Orig = null;

            // Копии worldPos для Rasterization (чтобы она их не мутировала)
            Vector3f w1Work = null, w2Work = null, w3Work = null;

            Vector2f t1 = null, t2 = null, t3 = null;

            boolean shouldSkipPolygon = false;

            for (int local = 0; local < 3; ++local) {
                int polyIndex = startIndex + local;
                if (polyIndex >= mesh.polygons.size()) {
                    shouldSkipPolygon = true;
                    break;
                }

                int vertexIndex = mesh.polygons.get(polyIndex);
                if (vertexIndex < 0 || vertexIndex >= mesh.vertices.size()) {
                    shouldSkipPolygon = true;
                    break;
                }

                Vertex vertex = mesh.vertices.get(vertexIndex);
                Vector3f pos = vertex.position;
                Vector3f posVec = new Vector3f(pos.getX(), pos.getY(), pos.getZ());

                // world pos (для освещения)
                Vector3f worldPos = modelMatrix.multiplyOnVector(posVec);

                // view pos (для клиппинга/глубины)
                Vector3f viewPos = modelViewMatrix.multiplyOnVector(posVec);

                // Near-plane check в view-space
                if (forwardMinusZ) {
                    if (viewPos.getZ() > -near) { // камера смотрит вдоль -Z
                        shouldSkipPolygon = true;
                        break;
                    }
                } else {
                    if (viewPos.getZ() < near) {  // камера смотрит вдоль +Z
                        shouldSkipPolygon = true;
                        break;
                    }
                }

                // Проекция
                Vector3f projected = projectionMatrix.multiplyOnVector(viewPos);
                Point2f screenPoint = vertexToPoint(projected, width, height);

                if (!Float.isFinite(screenPoint.getX()) || !Float.isFinite(screenPoint.getY())) {
                    shouldSkipPolygon = true;
                    break;
                }

                // Положительная глубина вдоль направления взгляда
                float zViewDepth = forwardMinusZ ? -viewPos.getZ() : viewPos.getZ();
                if (!Float.isFinite(zViewDepth) || zViewDepth <= 1e-6f) {
                    shouldSkipPolygon = true;
                    break;
                }

                Vector2f texCoord = null;
                if (SceneManager.useTexture && mesh.texture != null) {
                    texCoord = mesh.getTextureCoordinateForPolygonVertex(polyIndex);
                }

                if (local == 0) {
                    p1 = screenPoint;
                    zD1 = zViewDepth;
                    v1 = vertex;
                    w1Orig = worldPos;
                    w1Work = new Vector3f(worldPos.getX(), worldPos.getY(), worldPos.getZ());
                    t1 = texCoord;
                } else if (local == 1) {
                    p2 = screenPoint;
                    zD2 = zViewDepth;
                    v2 = vertex;
                    w2Orig = worldPos;
                    w2Work = new Vector3f(worldPos.getX(), worldPos.getY(), worldPos.getZ());
                    t2 = texCoord;
                } else {
                    p3 = screenPoint;
                    zD3 = zViewDepth;
                    v3 = vertex;
                    w3Orig = worldPos;
                    w3Work = new Vector3f(worldPos.getX(), worldPos.getY(), worldPos.getZ());
                    t3 = texCoord;
                }
            }

            if (shouldSkipPolygon) continue;
            if (p1 == null || p2 == null || p3 == null) continue;
            if (isTriangleOutsideScreen(p1, p2, p3, width, height)) continue;

            // фолбэк face normal (world)
            final Vector3f faceNormalWorld = computeFaceNormalWorld(w1Orig, w2Orig, w3Orig);

            boolean shouldRenderFill = !SceneManager.drawMesh
                    || (SceneManager.useTexture && mesh.texture != null)
                    || SceneManager.useLight;

            if (shouldRenderFill) {
                // ---- ВАЖНО: делаем final-копии для лямбды ----
                final float fZ1 = zD1, fZ2 = zD2, fZ3 = zD3;
                final Vector3f fW1 = w1Orig, fW2 = w2Orig, fW3 = w3Orig;
                final Vector3f fFaceN = faceNormalWorld;

                final boolean fTextured = SceneManager.useTexture && mesh.texture != null;
                final Image fTexture = mesh.texture;
                final Color fBaseColor = baseColor;

                final float[] fNormalMatrix = normalMatrix;
                final Matrix4f fModelMatrix = modelMatrix;

                Rasterization.PixelCallback callback = (x, y, zIgnored, barycentric, texCoord, normal, worldNormalIgnored, worldPosIgnored) -> {
                    if (barycentric == null || barycentric.length < 3) return;

                    float alpha = barycentric[0];
                    float beta = barycentric[1];
                    float gamma = barycentric[2];

                    // 1) Перспективно-корректная глубина для ZBuffer (view-depth)
                    float depth = perspectiveCorrectDepth(fZ1, fZ2, fZ3, alpha, beta, gamma);
                    if (!Float.isFinite(depth)) return;

                    if (!zBuffer.testAndSet(x, y, depth)) return;

                    // 2) Базовый цвет
                    Color color = fTextured ? TextureMapping.getTextureColor(fTexture, texCoord) : fBaseColor;

                    // 3) Освещение
                    if (SceneManager.useLight) {
                        Vector3f n = normal;
                        if (n == null || n.len() < 1e-6f) {
                            n = fFaceN;
                        }

                        Vector3f worldN = transformNormal(fNormalMatrix, fModelMatrix, n);

                        // мировая позиция пикселя (перспективно-корректно, из ОРИГИНАЛЬНЫХ worldPos)
                        Vector3f worldP = perspectiveCorrectInterpolate3(
                                fW1, fW2, fW3,
                                fZ1, fZ2, fZ3,
                                alpha, beta, gamma
                        );

                        color = TextureMapping.getModifiedColorWithLighting(
                                worldN,
                                worldP,
                                color,
                                SceneManager.lightIntensity
                        );
                    }

                    graphicsContext.getPixelWriter().setColor(x, y, color);
                };

                // В Rasterization отдаём копии worldPos, чтобы её multiply() не портили оригиналы
                Rasterization.rasterizeTriangleWithWorldPos(
                        p1, p2, p3,
                        zD1, zD2, zD3,
                        v1, v2, v3,
                        t1, t2, t3,
                        w1Work, w2Work, w3Work,
                        callback,
                        modelMatrix
                );
            }

            // Wireframe
            if (SceneManager.drawMesh) {
                float bias = 0.0001f;
                Color lineColor = Color.web(ThemeSettings.wireframeColor);
                int lineWidth = (int) ThemeSettings.wireframeWidth;

                Rasterization.rasterizeThickLine(p1, p2, zD1 - bias, zD2 - bias, lineWidth, (x, y, zLine) -> {
                    if (zBuffer.testAndSet(x, y, zLine)) {
                        graphicsContext.getPixelWriter().setColor(x, y, lineColor);
                    }
                });

                Rasterization.rasterizeThickLine(p2, p3, zD2 - bias, zD3 - bias, lineWidth, (x, y, zLine) -> {
                    if (zBuffer.testAndSet(x, y, zLine)) {
                        graphicsContext.getPixelWriter().setColor(x, y, lineColor);
                    }
                });

                Rasterization.rasterizeThickLine(p1, p3, zD1 - bias, zD3 - bias, lineWidth, (x, y, zLine) -> {
                    if (zBuffer.testAndSet(x, y, zLine)) {
                        graphicsContext.getPixelWriter().setColor(x, y, lineColor);
                    }
                });
            }
        }
    }

    private static boolean isTriangleOutsideScreen(Point2f p1, Point2f p2, Point2f p3, int width, int height) {
        float minX = Math.min(Math.min(p1.getX(), p2.getX()), p3.getX());
        float maxX = Math.max(Math.max(p1.getX(), p2.getX()), p3.getX());
        float minY = Math.min(Math.min(p1.getY(), p2.getY()), p3.getY());
        float maxY = Math.max(Math.max(p1.getY(), p2.getY()), p3.getY());
        return (maxX < 0 || minX > width || maxY < 0 || minY > height);
    }

    private static Vector3f[] clipLineToNear(Vector3f a, Vector3f b, float near, boolean forwardMinusZ) {
        float za = a.getZ();
        float zb = b.getZ();

        float planeZ = forwardMinusZ ? -near : near;

        boolean ina = forwardMinusZ ? (za <= planeZ) : (za >= planeZ);
        boolean inb = forwardMinusZ ? (zb <= planeZ) : (zb >= planeZ);

        if (!ina && !inb) return null;
        if (ina && inb) return new Vector3f[]{a, b};

        float denom = (zb - za);
        if (Math.abs(denom) < 1e-8f) return null;

        float t = (planeZ - za) / denom;
        Vector3f p = a.added(b.subbed(a).multiplied(t));

        if (!ina) a = p;
        else b = p;

        return new Vector3f[]{a, b};
    }

    // -------------------- helpers (depth, normals, world pos) --------------------

    // Перспективно-корректная глубина (если используем viewDepth > 0)
    private static float perspectiveCorrectDepth(float z1, float z2, float z3,
                                                 float a, float b, float c) {
        float denom = a / z1 + b / z2 + c / z3;
        if (Math.abs(denom) < 1e-12f) return Float.POSITIVE_INFINITY;
        return 1.0f / denom;
    }

    // Перспективно-корректная интерполяция Vector3f (world position)
    private static Vector3f perspectiveCorrectInterpolate3(
            Vector3f p1, Vector3f p2, Vector3f p3,
            float z1, float z2, float z3,
            float a, float b, float c) {

        float iz1 = 1.0f / z1;
        float iz2 = 1.0f / z2;
        float iz3 = 1.0f / z3;

        Vector3f p1oz = p1.multiplied(iz1);
        Vector3f p2oz = p2.multiplied(iz2);
        Vector3f p3oz = p3.multiplied(iz3);

        Vector3f sum = p1oz.multiplied(a)
                .added(p2oz.multiplied(b))
                .added(p3oz.multiplied(c));

        float iz = a * iz1 + b * iz2 + c * iz3;
        if (Math.abs(iz) < 1e-12f) return new Vector3f(0, 0, 0);

        return sum.multiplied(1.0f / iz);
    }

    // Face normal в world space (на случай битых вершинных нормалей)
    private static Vector3f computeFaceNormalWorld(Vector3f w1, Vector3f w2, Vector3f w3) {
        if (w1 == null || w2 == null || w3 == null) return new Vector3f(0, 1, 0);
        Vector3f e1 = w2.subbed(w1);
        Vector3f e2 = w3.subbed(w1);
        Vector3f n = e1.crossed(e2);
        if (n.len() < 1e-8f) return new Vector3f(0, 1, 0);
        return n.normalized();
    }

    // Строим (L^-1)^T для верхней 3x3 матрицы modelMatrix (нужно для нормалей при scale)
    // Возвращает 9 значений построчно, или null если матрица вырожденная.
    private static float[] buildNormalMatrix3x3(Matrix4f m) {
        float a = m.getValue(0, 0), b = m.getValue(0, 1), c = m.getValue(0, 2);
        float d = m.getValue(1, 0), e = m.getValue(1, 1), f = m.getValue(1, 2);
        float g = m.getValue(2, 0), h = m.getValue(2, 1), i = m.getValue(2, 2);

        float det = a * (e * i - f * h) - b * (d * i - f * g) + c * (d * h - e * g);
        if (Math.abs(det) < 1e-12f) return null;

        float c00 = (e * i - f * h);
        float c01 = -(d * i - f * g);
        float c02 = (d * h - e * g);

        float c10 = -(b * i - c * h);
        float c11 = (a * i - c * g);
        float c12 = -(a * h - b * g);

        float c20 = (b * f - c * e);
        float c21 = -(a * f - c * d);
        float c22 = (a * e - b * d);

        float invDet = 1.0f / det;

        return new float[]{
                c00 * invDet, c01 * invDet, c02 * invDet,
                c10 * invDet, c11 * invDet, c12 * invDet,
                c20 * invDet, c21 * invDet, c22 * invDet
        };
    }

    // Трансформация нормали: inverse-transpose, без translation
    private static Vector3f transformNormal(float[] normalMatrix, Matrix4f modelMatrix, Vector3f n) {
        Vector3f nn = (n == null) ? new Vector3f(0, 1, 0) : n.normalized();

        Vector3f out;
        if (normalMatrix != null) {
            float x = normalMatrix[0] * nn.getX() + normalMatrix[1] * nn.getY() + normalMatrix[2] * nn.getZ();
            float y = normalMatrix[3] * nn.getX() + normalMatrix[4] * nn.getY() + normalMatrix[5] * nn.getZ();
            float z = normalMatrix[6] * nn.getX() + normalMatrix[7] * nn.getY() + normalMatrix[8] * nn.getZ();
            out = new Vector3f(x, y, z);
        } else {
            out = transformDirection(modelMatrix, nn);
        }

        if (out.len() < 1e-8f) return new Vector3f(0, 1, 0);
        return out.normalized();
    }

    // Умножение направления на верхнюю 3x3 без translation и без деления на w
    private static Vector3f transformDirection(Matrix4f m, Vector3f v) {
        float x = v.getX() * m.getValue(0, 0) + v.getY() * m.getValue(0, 1) + v.getZ() * m.getValue(0, 2);
        float y = v.getX() * m.getValue(1, 0) + v.getY() * m.getValue(1, 1) + v.getZ() * m.getValue(1, 2);
        float z = v.getX() * m.getValue(2, 0) + v.getY() * m.getValue(2, 1) + v.getZ() * m.getValue(2, 2);
        return new Vector3f(x, y, z);
    }
}

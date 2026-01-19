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

import static com.cgvsu.math.matrixs.Matrix4f.buildNormalMatrix3x3;
import static com.cgvsu.modelOperations.Rasterization.perspectiveCorrectInterpolate3;
import static com.cgvsu.render_engine.GraphicConveyor.*;

public class RenderEngine {
    /**
     * Метод, который рендерит каждый кадр с учётом режимов отрисовки
     * @param graphicsContext графический контекст для наложения цвета на пиксель
     * @param camera камера, относительно который рендерим кадр(в нашем случае это ActiveCamera)
     * @param mesh сама модель, которую нужно отрендерить
     * @param width ширина экрана
     * @param height высота экрана
     * @param zBuffer z-Буффер, который используется для отсечения задних полигонов и сетке
     *                (сетка отсекается при цвете или текстуре)
     */
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

                // view pos (для глубины)
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
                //Делаем констант копии переменных, чтобы передать их в lambda функцию
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

    // Перспективно-корректная глубина (если используем viewDepth > 0)
    private static float perspectiveCorrectDepth(float z1, float z2, float z3,
                                                 float a, float b, float c) {
        float denom = a / z1 + b / z2 + c / z3;
        if (Math.abs(denom) < 1e-12f) return Float.POSITIVE_INFINITY;
        return 1.0f / denom;
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

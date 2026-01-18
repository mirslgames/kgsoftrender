package com.cgvsu.render_engine;

import java.util.ArrayList;


import com.cgvsu.math.matrixs.Matrix4f;
import com.cgvsu.math.point.Point2f;
import com.cgvsu.math.point.Point3f;
import com.cgvsu.math.vectors.Vector3f;
import com.cgvsu.math.vectors.Vector2f;
import com.cgvsu.model.Vertex;
import com.cgvsu.modelOperations.Rasterization;
import com.cgvsu.modelOperations.TextureMapping;
import com.cgvsu.modelOperations.ZBuffer;
import com.cgvsu.sceneview.SceneManager;
import com.cgvsu.service.ThemeSettings;
import javafx.scene.canvas.GraphicsContext;
import com.cgvsu.render_engine.GraphicConveyor.*;

import com.cgvsu.model.Model;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;


import static com.cgvsu.render_engine.GraphicConveyor.*;

public class RenderEngine {

    public static void render(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model mesh,
            final int width,
            final int height) {
        Matrix4f modelMatrix = rotateScaleTranslate(mesh.currentTransform.scaleX, mesh.currentTransform.scaleY, mesh.currentTransform.scaleZ,
                mesh.currentTransform.rotationX, mesh.currentTransform.rotationY, mesh.currentTransform.rotationZ,
                mesh.currentTransform.positionX, mesh.currentTransform.positionY, mesh.currentTransform.positionZ);

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
            int endIndex = (polygonInd + 1 < nPolygons) ? mesh.polygonsBoundaries.get(polygonInd + 1) : mesh.polygons.size();
            int n = endIndex - startIndex;

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
                        !Float.isFinite(pb.getX()) || !Float.isFinite(pb.getY())) continue;

                graphicsContext.strokeLine(pa.getX(), pa.getY(), pb.getX(), pb.getY());

                if (Float.isNaN(pa.getX()) || Float.isNaN(pa.getY()) || Float.isNaN(pb.getX()) || Float.isNaN(pb.getY()))
                    continue;

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
                mesh.currentTransform.positionX, mesh.currentTransform.positionY, mesh.currentTransform.positionZ);

        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();

        // Шаг 1: Создаем modelView матрицу (V * M)
        Matrix4f modelViewMatrix = new Matrix4f(viewMatrix.getMatrix());
        modelViewMatrix.multiply(modelMatrix);

        // Шаг 2: Получаем параметры камеры для клиппинга
        float near = camera.getNearPlane();
        Vector3f targetView = viewMatrix.multiplyOnVector(camera.getTarget());
        boolean forwardMinusZ = targetView.getZ() < 0;

        Color baseColor = Color.GREEN;
        final int nPolygons = mesh.polygonsBoundaries.size();

        for (int polygonInd = 0; polygonInd < nPolygons; ++polygonInd) {
            final int startIndex = mesh.polygonsBoundaries.get(polygonInd);
            final int endIndex = (polygonInd + 1 < nPolygons)
                    ? mesh.polygonsBoundaries.get(polygonInd + 1)
                    : mesh.polygons.size();

            Point2f p1 = null, p2 = null, p3 = null;
            float zProj1 = 0, zProj2 = 0, zProj3 = 0;
            float zView1 = 0, zView2 = 0, zView3 = 0;
            Vertex v1 = null, v2 = null, v3 = null;
            Vector3f w1 = null, w2 = null, w3 = null;
            Vector2f t1 = null, t2 = null, t3 = null;

            boolean shouldSkipPolygon = false;

            for (int local = 0; local < 3; ++local) {
                int polyIndex = startIndex + local;
                if (polyIndex >= mesh.polygons.size()) {
                    System.err.printf("ERROR: i=%d >= polygons.size=%d\n", polyIndex, mesh.polygons.size());
                    shouldSkipPolygon = true;
                    break;
                }

                int vertexIndex = mesh.polygons.get(polyIndex);
                if (vertexIndex < 0 || vertexIndex >= mesh.vertices.size()) {
                    System.err.printf("ERROR: vertexIndex=%d, vertices.size=%d\n",
                            vertexIndex, mesh.vertices.size());
                    shouldSkipPolygon = true;
                    break;
                }

                Vertex vertex = mesh.vertices.get(vertexIndex);
                Vector3f pos = vertex.position;
                Vector3f posVecmath = new Vector3f(pos.getX(), pos.getY(), pos.getZ());

                // Шаг 3: Мировая позиция для освещения
                Vector3f worldPos = modelMatrix.multiplyOnVector(posVecmath);

                // Шаг 4: Преобразуем в view-space (V * M * vertex)
                Vector3f viewPos = modelViewMatrix.multiplyOnVector(posVecmath);

                // Шаг 5: Клиппинг по near-плоскости в view-space
                // Проверяем, не находится ли вершина за near-плоскостью
                if (forwardMinusZ) {
                    // Камера смотрит вдоль -Z
                    if (viewPos.getZ() > -near) {
                        shouldSkipPolygon = true;
                        break;
                    }
                } else {
                    // Камера смотрит вдоль +Z
                    if (viewPos.getZ() < near) {
                        shouldSkipPolygon = true;
                        break;
                    }
                }

                // Шаг 6: Проекция ПОСЛЕ клиппинга (P * viewPos)
                Vector3f projected = projectionMatrix.multiplyOnVector(viewPos);

                // Получаем экранные координаты
                Point2f resultPoint = vertexToPoint(projected, width, height);
                float zView = viewPos.getZ();  // отрицательное!
                float distance = Math.abs(zView);  // абсолютное расстояние


                float far = camera.getFarPlane();
                float zScreen = (distance - near) / (far - near);
                zScreen = 1.0f - zScreen;

// Ограничиваем [0, 1]
                if (zScreen < 0.0f) zScreen = 0.0f;
                if (zScreen > 1.0f) zScreen = 1.0f;// z в view space для перспективной интерполяции

                Vector2f texCoord = null;
                if (SceneManager.useTexture && mesh.texture != null) {
                    texCoord = mesh.getTextureCoordinateForPolygonVertex(polyIndex);
                }

                if (local == 0) {
                    p1 = resultPoint;
                    zProj1 = zScreen;
                    zView1 = zView;
                    v1 = vertex;
                    w1 = worldPos;
                    t1 = texCoord;
                } else if (local == 1) {
                    p2 = resultPoint;
                    zProj2 = zScreen;
                    zView2 = zView;
                    v2 = vertex;
                    w2 = worldPos;
                    t2 = texCoord;
                } else {
                    p3 = resultPoint;
                    zProj3 = zScreen;
                    zView3 = zView;
                    v3 = vertex;
                    w3 = worldPos;
                    t3 = texCoord;
                }
            }

            if (shouldSkipPolygon) {
                continue;
            }

            if (p1 == null || p2 == null || p3 == null) {
                continue;
            }

            if (isTriangleOutsideScreen(p1, p2, p3, width, height)) {
                continue;
            }

            boolean shouldRenderFill = !SceneManager.drawMesh ||
                    (SceneManager.useTexture && mesh.texture != null) ||
                    SceneManager.useLight;

            // Рендеринг заливки полигона
            if (shouldRenderFill) {
                if (SceneManager.useTexture && mesh.texture != null) {
                    Image texture = mesh.texture;
                    Rasterization.PixelCallback callback = (x, y, z, barycentric, texCoord, normal, worldNormal, worldPosition) -> {
                        if (zBuffer.testAndSet(x, y, z)) {
                            Color color = TextureMapping.getTextureColor(texture, texCoord);
                            if (SceneManager.useLight && normal != null) {
                                color = TextureMapping.getModifiedColorWithLighting(
                                        worldNormal,
                                        worldPosition,
                                        color,
                                        SceneManager.lightIntensity
                                );
                            }
                            graphicsContext.getPixelWriter().setColor(x, y, color);
                        }
                    };

                    Rasterization.rasterizeTriangleWithWorldPos(p1, p2, p3, zProj1, zProj2, zProj3, zView1, zView2, zView3, v1, v2, v3, t1, t2, t3, w1,
                            w2, w3, callback, modelMatrix);
                } else {
                    Rasterization.PixelCallback callback = (x, y, z, barycentric, texCoord, normal, worldNormal, worldPosition) -> {
                        if (zBuffer.testAndSet(x, y, z)) {
                            Color color = baseColor;
                            if (SceneManager.useLight && normal != null) {
                                color = TextureMapping.getModifiedColorWithLighting(
                                        worldNormal,
                                        worldPosition,
                                        color,
                                        SceneManager.lightIntensity
                                );
                            }
                            graphicsContext.getPixelWriter().setColor(x, y, color);
                        }
                    };

                    Rasterization.rasterizeTriangleWithWorldPos(p1, p2, p3, zProj1, zProj2, zProj3, zView1, zView2, zView3, v1, v2, v3, t1, t2, t3, w1,
                            w2, w3, callback, modelMatrix);
                }
            }

            // Рендеринг сетки (wireframe)
            if (SceneManager.drawMesh) {
                // Преобразуем zProj в z для z-buffer перед использованием в wireframe
                float z1 = 1.0f - (zProj1 + 1.0f) * 0.5f;
                float z2 = 1.0f - (zProj2 + 1.0f) * 0.5f;
                float z3 = 1.0f - (zProj3 + 1.0f) * 0.5f;
                
                float bias = 0.0001f;
                Color lineColor = Color.web(ThemeSettings.wireframeColor);
                int lineWidth = (int) ThemeSettings.wireframeWidth;


                Rasterization.rasterizeThickLine(p1, p2, z1 - bias, z2 - bias, lineWidth, (x, y, z) -> {
                    if (zBuffer.testAndSet(x, y, z)) {
                        graphicsContext.getPixelWriter().setColor(x, y, lineColor);
                    }
                });

                Rasterization.rasterizeThickLine(p2, p3, z2 - bias, z3 - bias, lineWidth, (x, y, z) -> {
                    if (zBuffer.testAndSet(x, y, z)) {
                        graphicsContext.getPixelWriter().setColor(x, y, lineColor);
                    }
                });

                Rasterization.rasterizeThickLine(p1, p3, z1 - bias, z3 - bias, lineWidth, (x, y, z) -> {
                    if (zBuffer.testAndSet(x, y, z)) {
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
        if (ina && inb) return new Vector3f[]{a,b};

        float denom = (zb - za);
        if (Math.abs(denom) < 1e-8f) return null;

        float t = (planeZ - za) / denom;
        Vector3f p = a.added(b.subbed(a).multiplied(t));

        if (!ina) a = p; else b = p;
        return new Vector3f[]{a,b};
    }
}
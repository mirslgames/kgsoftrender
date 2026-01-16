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
            final int height)
    {
        Matrix4f modelMatrix = rotateScaleTranslate(mesh.currentTransform.scaleX, mesh.currentTransform.scaleY, mesh.currentTransform.scaleZ,
                mesh.currentTransform.rotationX, mesh.currentTransform.rotationY, mesh.currentTransform.rotationZ,
                mesh.currentTransform.positionX, mesh.currentTransform.positionY, mesh.currentTransform.positionZ);
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();

        Matrix4f modelViewProjectionMatrix = new Matrix4f(projectionMatrix.getMatrix());
        modelViewProjectionMatrix.multiply(viewMatrix);
        modelViewProjectionMatrix.multiply(modelMatrix);

        final int nPolygons = mesh.polygonsBoundaries.size();
        for (int polygonInd = 0; polygonInd < nPolygons; ++polygonInd) {
            final int startIndex = mesh.polygonsBoundaries.get(polygonInd);
            final int endIndex = (polygonInd + 1 < nPolygons)
                    ? mesh.polygonsBoundaries.get(polygonInd + 1)
                    : mesh.polygons.size();

            final int nVerticesInPolygon = endIndex - startIndex; //Под новую архитектуру модели
            // считаем сколько у полигона вершины

            ArrayList<Point2f> resultPoints = new ArrayList<>();

            for (int i = startIndex; i < endIndex; ++i) {
                int vertexIndex = mesh.polygons.get(i);
                Vertex vertex = mesh.vertices.get(vertexIndex);

                Vector3f pos = vertex.position;
                Vector3f posVecmath = new Vector3f(pos.getX(), pos.getY(), pos.getZ());

                Point2f resultPoint = vertexToPoint(
                        modelViewProjectionMatrix.multiplyOnVector(posVecmath), width, height
                );


                resultPoints.add(resultPoint);
            }

            graphicsContext.setStroke(Color.web(ThemeSettings.wireframeColor));
            graphicsContext.setLineWidth(ThemeSettings.wireframeWidth);

            for (int vertexInPolygonInd = 1; vertexInPolygonInd < nVerticesInPolygon; ++vertexInPolygonInd) {
                graphicsContext.strokeLine(
                        resultPoints.get(vertexInPolygonInd - 1).getX(),
                        resultPoints.get(vertexInPolygonInd - 1).getY(),
                        resultPoints.get(vertexInPolygonInd).getX(),
                        resultPoints.get(vertexInPolygonInd).getY());
            }

            if (nVerticesInPolygon > 0)
                graphicsContext.strokeLine(
                        resultPoints.get(nVerticesInPolygon - 1).getX(),
                        resultPoints.get(nVerticesInPolygon - 1).getY(),
                        resultPoints.get(0).getX(),
                        resultPoints.get(0).getY());
        }
    }
//    public static void renderWithRenderingMods1(
//            final GraphicsContext graphicsContext,
//            final Camera camera,
//            final Model mesh,
//            final int width,
//            final int height)
//    {
//        Matrix4f modelMatrix = rotateScaleTranslate(mesh.currentTransform.scaleX, mesh.currentTransform.scaleY, mesh.currentTransform.scaleZ,
//                mesh.currentTransform.rotationX, mesh.currentTransform.rotationY, mesh.currentTransform.rotationZ,
//                mesh.currentTransform.positionX, mesh.currentTransform.positionY, mesh.currentTransform.positionZ);
//        Matrix4f viewMatrix = camera.getViewMatrix();
//        Matrix4f projectionMatrix = camera.getProjectionMatrix();
//
//        Matrix4f modelViewProjectionMatrix = new Matrix4f(projectionMatrix.getMatrix());
//        modelViewProjectionMatrix.multiply(viewMatrix);
//        modelViewProjectionMatrix.multiply(modelMatrix);
//        Color baseColor = Color.GRAY;
//        ZBuffer zBuffer = new ZBuffer(width, height);
//        zBuffer.clear();
//        final int nPolygons = mesh.polygonsBoundaries.size();
//        for (int polygonInd = 0; polygonInd < nPolygons; ++polygonInd) {
//            final int startIndex = mesh.polygonsBoundaries.get(polygonInd);
//            final int endIndex = (polygonInd + 1 < nPolygons)
//                    ? mesh.polygonsBoundaries.get(polygonInd + 1)
//                    : mesh.polygons.size();
//
//            final int nVerticesInPolygon = endIndex - startIndex; //Под новую архитектуру модели
//            // считаем сколько у полигона вершины
//
//            ArrayList<Point2f> projectedPoints = new ArrayList<>();
//            ArrayList<Float> depths = new ArrayList<>();
//            ArrayList<Vertex> originalVertices = new ArrayList<>();
//
//            for (int i = startIndex; i < endIndex; ++i) {
//                if (i >= mesh.polygons.size()) {
//                    System.err.printf("ERROR: i=%d >= polygons.size=%d\n", i, mesh.polygons.size());
//                    break;
//                }
//
//                int vertexIndex = mesh.polygons.get(i);
//
//                // Проверка индекса вершины!
//                if (vertexIndex < 0 || vertexIndex >= mesh.vertices.size()) {
//                    System.err.printf("ERROR: vertexIndex=%d, vertices.size=%d\n",
//                            vertexIndex, mesh.vertices.size());
//                    continue;
//                }
//
//                Vertex vertex = mesh.vertices.get(vertexIndex);
//
//                Vector3f pos = vertex.position;
//                Vector3f posVecmath = new Vector3f(pos.getX(), pos.getY(), pos.getZ());
//                Vector3f transformed = modelViewProjectionMatrix.multiplyOnVector(posVecmath);
//                Point2f resultPoint = vertexToPoint(
//                        transformed, width, height
//                );
//
//                projectedPoints.add(resultPoint);
//                originalVertices.add(vertex);
//                depths.add(transformed.getZ());
//            }
//
//
//            if (!SceneManager.drawMesh && nVerticesInPolygon == 3) {
//                int i1 = mesh.polygons.get(startIndex);
//                int i2 = mesh.polygons.get(startIndex+1);
//                int i3 = mesh.polygons.get(startIndex+2);
//                Vertex v1 = mesh.vertices.get(i1);
//                Vertex v2 = mesh.vertices.get(i2);
//                Vertex v3 = mesh.vertices.get(i3);
//                Point2f p1 = projectedPoints.get(0);
//                Point2f p2 = projectedPoints.get(1);
//                Point2f p3 = projectedPoints.get(2);
//                float z1 = depths.get(0);
//                float z2 = depths.get(1);
//                float z3 = depths.get(2);
//
//                Vector2f t1 = mesh.getTextureCoordinateForPolygonVertex(startIndex);
//                Vector2f t2 = mesh.getTextureCoordinateForPolygonVertex(startIndex + 1);
//                Vector2f t3 = mesh.getTextureCoordinateForPolygonVertex(startIndex + 2);
//
//                if (SceneManager.useTexture && mesh.texture != null) {
//                    Image texture = mesh.texture;
//                    Rasterization.PixelCallback callback = (x, y, z, barycentric, texCoord, normal, worldPosition) -> {
//                        if (zBuffer.testAndSet(x, y, z)) {
//                            Color color = TextureMapping.getTextureColor(texture, texCoord);
//                            if (SceneManager.useLight && normal != null) {
//                                Vector3f cameraRay = SceneManager.activeCamera.getRayToPoint(worldPosition);
//                                color = TextureMapping.getModifiedColorWithLighting(
//                                        cameraRay, normal, color, SceneManager.lightIntensity
//                                );
//                            }
//
//                            graphicsContext.getPixelWriter().setColor(x, y, color);
//                        }
//                    };
//                    Rasterization.rasterizeTriangle(p1, p2, p3, z1, z2, z3, v1, v2, v3, t1, t2, t3, callback);
//                } else {
//                    Rasterization.PixelCallback callback = (x, y, z, barycentric, texCoord, normal, worldPosition) -> {
//                        if (zBuffer.testAndSet(x, y, z)) {
//                            Color color = baseColor;
//                            if (SceneManager.useLight && normal != null) {
//                                Vector3f cameraRay = SceneManager.activeCamera.getRayToPoint(worldPosition);
//                                color = TextureMapping.getModifiedColorWithLighting(
//                                        cameraRay, normal, color, SceneManager.lightIntensity
//                                );
//                            }
//                            graphicsContext.getPixelWriter().setColor(x, y, color);
//                        }
//                    };
//                    Rasterization.rasterizeTriangle(p1, p2, p3, z1, z2, z3, v1, v2, v3, t1, t2, t3, callback);
//                }
//            }
//            else if (SceneManager.drawMesh){
//                graphicsContext.setStroke(Color.web(ThemeSettings.wireframeColor));
//                graphicsContext.setLineWidth(ThemeSettings.wireframeWidth);
//
//                for (int vertexInPolygonInd = 1; vertexInPolygonInd < nVerticesInPolygon; ++vertexInPolygonInd) {
//                    graphicsContext.strokeLine(
//                            projectedPoints.get(vertexInPolygonInd - 1).getX(),
//                            projectedPoints.get(vertexInPolygonInd - 1).getY(),
//                            projectedPoints.get(vertexInPolygonInd).getX(),
//                            projectedPoints.get(vertexInPolygonInd).getY());
//                }
//
//                if (nVerticesInPolygon > 0)
//                    graphicsContext.strokeLine(
//                            projectedPoints.get(nVerticesInPolygon - 1).getX(),
//                            projectedPoints.get(nVerticesInPolygon - 1).getY(),
//                            projectedPoints.get(0).getX(),
//                            projectedPoints.get(0).getY());
//            }
//        }
//    }

    public static void renderWithRenderingMods(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model mesh,
            final int width,
            final int height, ZBuffer zBuffer)
    {
        Matrix4f modelMatrix = rotateScaleTranslate(mesh.currentTransform.scaleX, mesh.currentTransform.scaleY, mesh.currentTransform.scaleZ,
                mesh.currentTransform.rotationX, mesh.currentTransform.rotationY, mesh.currentTransform.rotationZ,
                mesh.currentTransform.positionX, mesh.currentTransform.positionY, mesh.currentTransform.positionZ);
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();

        Matrix4f modelViewProjectionMatrix = new Matrix4f(projectionMatrix.getMatrix());
        modelViewProjectionMatrix.multiply(viewMatrix);
        modelViewProjectionMatrix.multiply(modelMatrix);

        Color baseColor = Color.GREEN;

        final int nPolygons = mesh.polygonsBoundaries.size();

        // НОВОЕ: Вспомогательные матрицы для получения мировых координат
        Matrix4f modelViewMatrix = new Matrix4f(viewMatrix.getMatrix());
        modelViewMatrix.multiply(modelMatrix);

        for (int polygonInd = 0; polygonInd < nPolygons; ++polygonInd) {
            final int startIndex = mesh.polygonsBoundaries.get(polygonInd);
            final int endIndex = (polygonInd + 1 < nPolygons)
                    ? mesh.polygonsBoundaries.get(polygonInd + 1)
                    : mesh.polygons.size();
            Point2f p1 = null, p2 = null, p3 = null;
            float z1 = 0, z2 = 0, z3 = 0;
            Vertex v1 = null, v2 = null, v3 = null;
            Vector3f w1 = null, w2 = null, w3 = null;
            Vector2f t1 = null, t2 = null, t3 = null;

            boolean shouldSkipPolygon = false; // НОВОЕ: флаг для пропуска полигона

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

                // НОВОЕ: получаем мировую позицию для освещения
                Vector3f worldPos = modelMatrix.multiplyOnVector(posVecmath);


                // Проецируем вершину
                Vector3f transformed = modelViewProjectionMatrix.multiplyOnVector(posVecmath);

                // НОВОЕ: Проверяем Z-координату после проецирования
                // Если вершина за камерой (или очень близко к ней), пропускаем весь полигон
                if (transformed.getZ() <= 0.001f) { // небольшой эпсилон
                    shouldSkipPolygon = true;
                    break;
                }

                Point2f resultPoint = vertexToPoint(transformed, width, height);
                float z = transformed.getZ();
                Vector2f texCoord = null;
                if (SceneManager.useTexture && mesh.texture != null) {
                    texCoord = mesh.getTextureCoordinateForPolygonVertex(polyIndex);
                }
                if (local == 0) {
                    p1 = resultPoint;
                    z1 = z;
                    v1 = vertex;
                    w1 = worldPos;
                    t1 = texCoord;
                } else if (local == 1) {
                    p2 = resultPoint;
                    z2 = z;
                    v2 = vertex;
                    w2 = worldPos;
                    t2 = texCoord;
                } else {
                    p3 = resultPoint;
                    z3 = z;
                    v3 = vertex;
                    w3 = worldPos;
                    t3 = texCoord;
                }
            }

            // НОВОЕ: пропускаем полигон, если хотя бы одна вершина за камерой
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
                    // НОВОЕ: передаем мировые позиции для интерполяции
                    Rasterization.rasterizeTriangleWithWorldPos(p1, p2, p3, z1, z2, z3, v1, v2, v3, t1, t2, t3, w1,
                            w2, w3, callback, modelMatrix);
                } else if (!SceneManager.drawMesh) {
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

                    Rasterization.rasterizeTriangleWithWorldPos(p1, p2, p3, z1, z2, z3, v1, v2, v3, t1, t2, t3, w1,
                            w2, w3, callback, modelMatrix);
                }
            }
            if (SceneManager.drawMesh) {
                graphicsContext.setStroke(Color.web(ThemeSettings.wireframeColor));
                graphicsContext.setLineWidth(ThemeSettings.wireframeWidth);


                graphicsContext.strokeLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
                graphicsContext.strokeLine(p2.getX(), p2.getY(), p3.getX(), p3.getY());
                graphicsContext.strokeLine(p3.getX(), p3.getY(), p1.getX(), p1.getY());
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
}
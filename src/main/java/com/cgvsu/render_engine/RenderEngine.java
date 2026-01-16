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
        /*
        СТАРЫЙ РЕНДЕР
        Matrix4f modelMatrix = rotateScaleTranslate(mesh.scaleXValue, mesh.scaleYValue, mesh.scaleZValue,
                mesh.rotationXValue, mesh.rotationYValue, mesh.rotationZValue,
                mesh.positionXValue, mesh.positionYValue, mesh.positionZValue);
        Matrix4f modelMatrix = rotateScaleTranslate(mesh.currentTransform.scaleX, mesh.currentTransform.scaleY, mesh.currentTransform.scaleZ,
                mesh.currentTransform.rotationX, mesh.currentTransform.rotationY, mesh.currentTransform.rotationZ,
                mesh.currentTransform.positionX, mesh.currentTransform.positionY, mesh.currentTransform.positionZ);
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();

        Matrix4f modelViewProjectionMatrix = new Matrix4f(projectionMatrix.getMatrix());
        modelViewProjectionMatrix.multiply(viewMatrix);
        modelViewProjectionMatrix.multiply(modelMatrix);

        final int nPolygons = mesh.polygonsBoundaries.size();

        float near = camera.getNearPlane();

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
    }*/
    }
    public static void renderWithRenderingMods1(
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
        Color baseColor = Color.GRAY;
        ZBuffer zBuffer = new ZBuffer(width, height);
        zBuffer.clear();
        final int nPolygons = mesh.polygonsBoundaries.size();
        for (int polygonInd = 0; polygonInd < nPolygons; ++polygonInd) {
            final int startIndex = mesh.polygonsBoundaries.get(polygonInd);
            final int endIndex = (polygonInd + 1 < nPolygons)
                    ? mesh.polygonsBoundaries.get(polygonInd + 1)
                    : mesh.polygons.size();

            final int nVerticesInPolygon = endIndex - startIndex; //Под новую архитектуру модели
            // считаем сколько у полигона вершины

            ArrayList<Point2f> projectedPoints = new ArrayList<>();
            ArrayList<Float> depths = new ArrayList<>();
            ArrayList<Vertex> originalVertices = new ArrayList<>();

            for (int i = startIndex; i < endIndex; ++i) {
                if (i >= mesh.polygons.size()) {
                    System.err.printf("ERROR: i=%d >= polygons.size=%d\n", i, mesh.polygons.size());
                    break;
                }

                int vertexIndex = mesh.polygons.get(i);

                // Проверка индекса вершины!
                if (vertexIndex < 0 || vertexIndex >= mesh.vertices.size()) {
                    System.err.printf("ERROR: vertexIndex=%d, vertices.size=%d\n",
                            vertexIndex, mesh.vertices.size());
                    continue;
                }

                Vertex vertex = mesh.vertices.get(vertexIndex);

                Vector3f pos = vertex.position;
                Vector3f posVecmath = new Vector3f(pos.getX(), pos.getY(), pos.getZ());
                Vector3f transformed = modelViewProjectionMatrix.multiplyOnVector(posVecmath);
                Point2f resultPoint = vertexToPoint(
                        transformed, width, height
                );

                projectedPoints.add(resultPoint);
                originalVertices.add(vertex);
                depths.add(transformed.getZ());
            }


            if (!SceneManager.drawMesh && nVerticesInPolygon == 3) {
                int i1 = mesh.polygons.get(startIndex);
                int i2 = mesh.polygons.get(startIndex+1);
                int i3 = mesh.polygons.get(startIndex+2);
                Vertex v1 = mesh.vertices.get(i1);
                Vertex v2 = mesh.vertices.get(i2);
                Vertex v3 = mesh.vertices.get(i3);
                Point2f p1 = projectedPoints.get(0);
                Point2f p2 = projectedPoints.get(1);
                Point2f p3 = projectedPoints.get(2);
                float z1 = depths.get(0);
                float z2 = depths.get(1);
                float z3 = depths.get(2);

                Vector2f t1 = mesh.getTextureCoordinateForPolygonVertex(startIndex);
                Vector2f t2 = mesh.getTextureCoordinateForPolygonVertex(startIndex + 1);
                Vector2f t3 = mesh.getTextureCoordinateForPolygonVertex(startIndex + 2);

                if (SceneManager.useTexture && mesh.texture != null) {
                    Image texture = mesh.texture;
                    Rasterization.PixelCallback callback = (x, y, z, barycentric, texCoord, normal, worldPosition) -> {
                        if (zBuffer.testAndSet(x, y, z)) {
                            Color color = TextureMapping.getTextureColor(texture, texCoord);
                            if (SceneManager.useLight && normal != null) {
                                Vector3f cameraRay = SceneManager.activeCamera.getRayToPoint(worldPosition);
                                color = TextureMapping.getModifiedColorWithLighting(
                                        cameraRay, normal, color, SceneManager.lightIntensity
                                );
                            }

                            graphicsContext.getPixelWriter().setColor(x, y, color);
                        }
                    };
                    Rasterization.rasterizeTriangle(p1, p2, p3, z1, z2, z3, v1, v2, v3, t1, t2, t3, callback);
                } else {
                    Rasterization.PixelCallback callback = (x, y, z, barycentric, texCoord, normal, worldPosition) -> {
                        if (zBuffer.testAndSet(x, y, z)) {
                            Color color = baseColor;
                            if (SceneManager.useLight && normal != null) {
                                Vector3f cameraRay = SceneManager.activeCamera.getRayToPoint(worldPosition);
                                color = TextureMapping.getModifiedColorWithLighting(
                                        cameraRay, normal, color, SceneManager.lightIntensity
                                );
                            }
                            graphicsContext.getPixelWriter().setColor(x, y, color);
                        }
                    };
                    Rasterization.rasterizeTriangle(p1, p2, p3, z1, z2, z3, v1, v2, v3, t1, t2, t3, callback);
                }
            }
            else if (SceneManager.drawMesh){
                graphicsContext.setStroke(Color.web(ThemeSettings.wireframeColor));
                graphicsContext.setLineWidth(ThemeSettings.wireframeWidth);

                for (int vertexInPolygonInd = 1; vertexInPolygonInd < nVerticesInPolygon; ++vertexInPolygonInd) {
                    graphicsContext.strokeLine(
                            projectedPoints.get(vertexInPolygonInd - 1).getX(),
                            projectedPoints.get(vertexInPolygonInd - 1).getY(),
                            projectedPoints.get(vertexInPolygonInd).getX(),
                            projectedPoints.get(vertexInPolygonInd).getY());
                }

                if (nVerticesInPolygon > 0)
                    graphicsContext.strokeLine(
                            projectedPoints.get(nVerticesInPolygon - 1).getX(),
                            projectedPoints.get(nVerticesInPolygon - 1).getY(),
                            projectedPoints.get(0).getX(),
                            projectedPoints.get(0).getY());
            }
        }
    }

    public static void renderWithRenderingMods(
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

        Color baseColor = Color.GRAY;
        ZBuffer zBuffer = new ZBuffer(width, height);
        zBuffer.clear();
        final int nPolygons = mesh.polygonsBoundaries.size();

        // НОВОЕ: Вспомогательные матрицы для получения мировых координат
        Matrix4f modelViewMatrix = new Matrix4f(viewMatrix.getMatrix());
        modelViewMatrix.multiply(modelMatrix);

        for (int polygonInd = 0; polygonInd < nPolygons; ++polygonInd) {
            final int startIndex = mesh.polygonsBoundaries.get(polygonInd);
            final int endIndex = (polygonInd + 1 < nPolygons)
                    ? mesh.polygonsBoundaries.get(polygonInd + 1)
                    : mesh.polygons.size();

            ArrayList<Point2f> projectedPoints = new ArrayList<>();
            ArrayList<Float> depths = new ArrayList<>();
            ArrayList<Vertex> originalVertices = new ArrayList<>();
            ArrayList<Vector3f> worldPositions = new ArrayList<>(); // НОВОЕ: мировые координаты
            ArrayList<Vector2f> texCoords = new ArrayList<>();

            boolean shouldSkipPolygon = false; // НОВОЕ: флаг для пропуска полигона

            for (int i = startIndex; i < endIndex; ++i) {
                if (i >= mesh.polygons.size()) {
                    System.err.printf("ERROR: i=%d >= polygons.size=%d\n", i, mesh.polygons.size());
                    shouldSkipPolygon = true;
                    break;
                }

                int vertexIndex = mesh.polygons.get(i);

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
                worldPositions.add(worldPos);

                // Проецируем вершину
                Vector3f transformed = modelViewProjectionMatrix.multiplyOnVector(posVecmath);

                // НОВОЕ: Проверяем Z-координату после проецирования
                // Если вершина за камерой (или очень близко к ней), пропускаем весь полигон
                if (transformed.getZ() <= 0.001f) { // небольшой эпсилон
                    shouldSkipPolygon = true;
                    break;
                }

                Point2f resultPoint = vertexToPoint(transformed, width, height);

                projectedPoints.add(resultPoint);
                originalVertices.add(vertex);
                texCoords.add(mesh.getTextureCoordinateForPolygonVertex(i));
                depths.add(transformed.getZ());
            }

            // НОВОЕ: пропускаем полигон, если хотя бы одна вершина за камерой
            if (shouldSkipPolygon) {
                continue;
            }

            // НОВОЕ: проверяем, что у нас достаточно вершин для рендеринга
            int nValidVertices = projectedPoints.size();
            if (nValidVertices < 3) {
                continue; // не хватает вершин для треугольника
            }

            if (nValidVertices == 3) {
                Vertex v1 = originalVertices.get(0);
                Vertex v2 = originalVertices.get(1);
                Vertex v3 = originalVertices.get(2);
                Point2f p1 = projectedPoints.get(0);
                Point2f p2 = projectedPoints.get(1);
                Point2f p3 = projectedPoints.get(2);
                float z1 = depths.get(0);
                float z2 = depths.get(1);
                float z3 = depths.get(2);
                Vector3f w1 = worldPositions.get(0); // НОВОЕ
                Vector3f w2 = worldPositions.get(1); // НОВОЕ
                Vector3f w3 = worldPositions.get(2);

                Vector2f t1 = texCoords.get(0);
                Vector2f t2 = texCoords.get(1);
                Vector2f t3 = texCoords.get(2);

                if (SceneManager.useTexture && mesh.texture != null) {
                    Image texture = mesh.texture;
                    Rasterization.PixelCallback callback = (x, y, z, barycentric, texCoord, normal, worldPosition) -> {
                        if (zBuffer.testAndSet(x, y, z)) {
                            Color color = TextureMapping.getTextureColor(texture, texCoord);
                            if (SceneManager.useLight && normal != null) {
                                Vector3f cameraRay = SceneManager.activeCamera.getRayToPoint(worldPosition);
                                color = TextureMapping.getModifiedColorWithLighting(
                                        cameraRay, normal, color, SceneManager.lightIntensity
                                );
                            }
                            graphicsContext.getPixelWriter().setColor(x, y, color);
                        }
                    };
                    // НОВОЕ: передаем мировые позиции для интерполяции
                    Rasterization.rasterizeTriangleWithWorldPos(p1, p2, p3, z1, z2, z3, v1, v2, v3, t1, t2, t3, w1,
                            w2, w3, callback, modelMatrix);
                } else if (!SceneManager.drawMesh) {
                    Rasterization.PixelCallback callback = (x, y, z, barycentric, texCoord, normal, worldPosition) -> {
                        if (zBuffer.testAndSet(x, y, z)) {
                            Color color = baseColor;
                            if (SceneManager.useLight && normal != null) {
                                Vector3f cameraRay = SceneManager.activeCamera.getRayToPoint(worldPosition);
                                color = TextureMapping.getModifiedColorWithLighting(
                                        cameraRay, normal, color, SceneManager.lightIntensity
                                );
                            }
                            graphicsContext.getPixelWriter().setColor(x, y, color);
                        }
                    };
                    // НОВОЕ: передаем мировые позиции для интерполяции
                    Rasterization.rasterizeTriangleWithWorldPos(p1, p2, p3, z1, z2, z3, v1, v2, v3, t1, t2, t3, w1,
                            w2, w3, callback, modelMatrix);
                }
            }
            if (SceneManager.drawMesh) {
                graphicsContext.setStroke(Color.web(ThemeSettings.wireframeColor));
                graphicsContext.setLineWidth(ThemeSettings.wireframeWidth);


                for (int vertexInPolygonInd = 1; vertexInPolygonInd < nValidVertices; ++vertexInPolygonInd) {
                    graphicsContext.strokeLine(
                            projectedPoints.get(vertexInPolygonInd - 1).getX(),
                            projectedPoints.get(vertexInPolygonInd - 1).getY(),
                            projectedPoints.get(vertexInPolygonInd).getX(),
                            projectedPoints.get(vertexInPolygonInd).getY());
                }


                graphicsContext.strokeLine(
                        projectedPoints.get(nValidVertices - 1).getX(),
                        projectedPoints.get(nValidVertices - 1).getY(),
                        projectedPoints.get(0).getX(),
                        projectedPoints.get(0).getY());
            }



        }
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
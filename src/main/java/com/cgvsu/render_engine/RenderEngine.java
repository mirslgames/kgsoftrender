package com.cgvsu.render_engine;

import java.util.ArrayList;


import com.cgvsu.math.matrixs.Matrix4f;
import com.cgvsu.math.point.Point2f;
import com.cgvsu.math.point.Point3f;
import com.cgvsu.math.vectors.Vector3f;
import com.cgvsu.model.Vertex;
import com.cgvsu.service.ThemeSettings;
import javafx.scene.canvas.GraphicsContext;
import com.cgvsu.render_engine.GraphicConveyor.*;

import com.cgvsu.model.Model;
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
        Matrix4f modelMatrix = rotateScaleTranslate(
                mesh.scaleXValue, mesh.scaleYValue, mesh.scaleZValue,
                mesh.rotationXValue, mesh.rotationYValue, mesh.rotationZValue,
                mesh.positionXValue, mesh.positionYValue, mesh.positionZValue
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

            final int nVerticesInPolygon = endIndex - startIndex; //Под новую архитектуру модели считаем сколько у полигона вершин

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
        } */
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
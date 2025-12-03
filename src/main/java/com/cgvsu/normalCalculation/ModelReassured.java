package com.cgvsu.normalCalculation;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;
import com.cgvsu.objreader.ObjReader;
import com.cgvsu.objreader.ObjReaderException;
import com.cgvsu.objwriter.ObjWriter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModelReassured extends Model {
    public MyVertexNormalCalc Calc = new MyVertexNormalCalc();
    public ArrayList<Vector3f> vertices = new ArrayList<Vector3f>();
    public ArrayList<Vector2f> textureVertices = new ArrayList<Vector2f>();
    public ArrayList<Vector3f> normals = new ArrayList<Vector3f>();
    public List<Polygon> polygons = new ArrayList<Polygon>();
    public ModelReassured(Model model){
        this.vertices = model.vertices;
        this.textureVertices = model.textureVertices;
        this.normals = new ArrayList<>(Calc.calculateVertexNormals(model));
        List<Polygon> realPolygons = new ArrayList<>();
        for (Polygon p : originalModel.polygons) {
            List<Polygon> temp = TriangulationAlgorithm.triangulate(p);
            realPolygons.addAll(temp);
        }
        this.polygons = new ArrayList<>(realPolygons);
    }

}

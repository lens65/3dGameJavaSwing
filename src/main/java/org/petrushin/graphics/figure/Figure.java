package org.petrushin.graphics.figure;

import java.util.ArrayList;
import java.util.List;

public class Figure {
    private List<Triangle> triangles;

    public Figure() {
        triangles = new ArrayList<>();
    }

    public Figure(List<Triangle> triangles) {
        this.triangles = triangles;
    }

    public Figure(Figure figure){
        this.triangles = figure.getTriangles();
    }

    public List<Triangle> getTriangles() {
        return triangles;
    }

    public void setTriangles(List<Triangle> triangles) {
        this.triangles = triangles;
    }

    public void addTriangle (Triangle triangle){
        triangles.add(triangle);
    }
}

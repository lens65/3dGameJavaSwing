package org.petrushin.graphics.figure;

public class Dot {
    private double x;
    private double y;
    private double z;

    public Dot(){}

    public Dot(double x, double y, double z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Dot(Dot dot){
        this.x = dot.getX();
        this.y = dot.getY();
        this.z = dot.getZ();
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }
}

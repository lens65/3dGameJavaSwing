package org.petrushin.graphics.figure;

public class Triangle {
    private Dot dot1;
    private Dot dot2;
    private Dot dot3;

    public Triangle(){}

    public Triangle(Dot dot1, Dot dot2, Dot dot3) {
        this.dot1 = dot1;
        this.dot2 = dot2;
        this.dot3 = dot3;
    }
    public Triangle(Triangle triangle){
        this.dot1 = triangle.getDot1();
        this.dot2 = triangle.getDot2();
        this.dot3 = triangle.getDot3();
    }

    public Dot getDot1() {
        return dot1;
    }

    public void setDot1(Dot dot1) {
        this.dot1 = dot1;
    }

    public Dot getDot2() {
        return dot2;
    }

    public void setDot2(Dot dot2) {
        this.dot2 = dot2;
    }

    public Dot getDot3() {
        return dot3;
    }

    public void setDot3(Dot dot3) {
        this.dot3 = dot3;
    }

    public Dot[] getAllDots(){
        Dot[] dots = {dot1, dot2, dot3};
        return dots;
    }
}

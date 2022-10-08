package org.petrushin.graphics;

import org.petrushin.graphics.figure.Dot;
import org.petrushin.graphics.figure.Figure;
import org.petrushin.graphics.figure.FiguresReader;
import org.petrushin.graphics.figure.Triangle;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class GamePanel1 extends JPanel implements Runnable{

    private final int WINDOW_WIDTH = 800;
    private final int WINDOW_HEIGHT = 600;
    private KeyHandler keyHandler = new KeyHandler();

    private int Fps = 60;

    private final int fov = 90;
    private double a = (double) WINDOW_HEIGHT/(double) WINDOW_WIDTH;
    private double t = 1.0 / Math.tan(Math.toRadians(fov/2));
    private double maxDistance = 1000.0;
    private double distanceToDisplay = 0.1;
    private double z11 = maxDistance / (maxDistance - distanceToDisplay);
    private double z22 = (maxDistance * distanceToDisplay) / (maxDistance - distanceToDisplay);
    private Dot camera = new Dot(0, 0, 0);
    private Dot light = new Dot (0, 0, -1);
    double lengthLight = Math.sqrt(light.getX() * light.getX() + light.getY() * light.getY() + light.getZ() * light.getZ());

    private double[][] projectionMatrix = {{0.0, 0.0, 0.0, 0.0},{0.0, 0.0, 0.0, 0.0},{0.0, 0.0, 0.0, 0.0},{0.0, 0.0, 0.0, 0.0}};
    private double[][] matrixRotX = {{0.0, 0.0, 0.0, 0.0},{0.0, 0.0, 0.0, 0.0},{0.0, 0.0, 0.0, 0.0},{0.0, 0.0, 0.0, 0.0}};
    private double[][] matrixRotZ = {{0.0, 0.0, 0.0, 0.0},{0.0, 0.0, 0.0, 0.0},{0.0, 0.0, 0.0, 0.0},{0.0, 0.0, 0.0, 0.0}};
    private double theta = 0.0;
    private double beta = 0.0;


    private Figure ship = FiguresReader.readFigureFromFile("F:\\IntelliJ IDEA\\FPSGame\\3dGameJavaSwing\\src\\main\\resources\\tinker.txt");

    public GamePanel1(){
        this.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
        this.addKeyListener(keyHandler);
        this.setFocusable(true);
    }

    Thread gameThread;

    private void setDefaultValues(){
        projectionMatrix[0][0] = a * t;
        projectionMatrix[1][1] = t;
        projectionMatrix[2][2] = z11;
        projectionMatrix[3][2] = -z22;
        projectionMatrix[2][3] = 1.0;
        projectionMatrix[3][3] = 0.0;
        light.setX(light.getX() / lengthLight);
        light.setY(light.getY() / lengthLight);
        light.setZ(light.getZ() / lengthLight);
    }


    public void startGame(){
        setDefaultValues();
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {

        double drawInterval = 1000000000 / Fps;
        double nextDrawTime = System.nanoTime() + drawInterval;

        while (gameThread != null){
            update();
            repaint();

            try {
                double remainingTime = (nextDrawTime - System.nanoTime()) / 1000000;
                if(remainingTime < 0 ) remainingTime = 0;
                Thread.sleep((long) remainingTime);
                nextDrawTime += drawInterval;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void update(){

        if(keyHandler.leftPressed){
            theta++;
            if (theta == 360) theta=0;

        }
        if(keyHandler.rightPressed){
            theta--;
            if (theta == 0) theta=359;
        }

        if(keyHandler.upPressed){
            beta++;
            if(beta == 360) beta = 0;
        }

        if(keyHandler.downPressed){
            beta--;
            if(beta == 0) beta = 360;
        }

        matrixRotZ[0][0] = Math.cos(Math.toRadians(theta));
        matrixRotZ[0][1] = Math.sin(Math.toRadians(theta));
        matrixRotZ[1][0] = -Math.sin(Math.toRadians(theta));
        matrixRotZ[1][1] = Math.cos(Math.toRadians(theta));
        matrixRotZ[2][2] = 1;
        matrixRotZ[3][3] = 1;

        matrixRotX[0][0] = 1;
        matrixRotX[1][1] = Math.cos(Math.toRadians(beta ));
        matrixRotX[1][2] = Math.sin(Math.toRadians(beta ));
        matrixRotX[2][1] = -Math.sin(Math.toRadians(beta ));
        matrixRotX[2][2] = Math.cos(Math.toRadians(beta ));
        matrixRotX[3][3] = 1;
    }



    public void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;



        Map<Triangle, Double> trianglesWD = new HashMap<>();
        List<Triangle> triangles = new ArrayList<>();
        for(Triangle triangle : ship.getTriangles()){

            //расчет положения треугольника после вращения по осям x и z
            Triangle triangleRotatedZ = new Triangle(multiplyProjection(triangle.getDot1(), matrixRotZ), multiplyProjection(triangle.getDot2(), matrixRotZ), multiplyProjection(triangle.getDot3(), matrixRotZ));
            Triangle triangleRotatedZX = new Triangle(multiplyProjection(triangleRotatedZ.getDot1(), matrixRotX), multiplyProjection(triangleRotatedZ.getDot2(), matrixRotX), multiplyProjection(triangleRotatedZ.getDot3(), matrixRotX));


            //ссоздаем копию точки в пространстве что б спроэктировать её на экран (так как значения будут перерасчитаны, нам нужно создать копиию и менять её иначе будет перерасчитана точка в пространстве -> ошибка)
            Triangle triangleTranslated = new Triangle(new Dot(), new Dot(), new Dot());

            //пока что по другому задать не получилось (разберусь позже)
            triangleTranslated.getDot1().setX(triangleRotatedZX.getDot1().getX());
            triangleTranslated.getDot2().setX(triangleRotatedZX.getDot2().getX());
            triangleTranslated.getDot3().setX(triangleRotatedZX.getDot3().getX());

            triangleTranslated.getDot1().setY(triangleRotatedZX.getDot1().getY());
            triangleTranslated.getDot2().setY(triangleRotatedZX.getDot2().getY());
            triangleTranslated.getDot3().setY(triangleRotatedZX.getDot3().getY());


            triangleTranslated.getDot1().setZ(triangleRotatedZX.getDot1().getZ() + 3.0);
            triangleTranslated.getDot2().setZ(triangleRotatedZX.getDot2().getZ() + 3.0);
            triangleTranslated.getDot3().setZ(triangleRotatedZX.getDot3().getZ() + 3.0);

            Dot normal = getNormal(triangleTranslated);



            if( normal.getX() * (triangleTranslated.getDot1().getX() - camera.getX()) + normal.getY() * (triangleTranslated.getDot1().getY() - camera.getY()) + normal.getZ() * ( triangleTranslated.getDot1().getZ() - camera.getZ())< 0){
                //проэкция треугольника на плоскость (3д -> 2д)
                Triangle triangleProjection = new Triangle(multiplyProjection(triangleTranslated.getDot1(), projectionMatrix), multiplyProjection(triangleTranslated.getDot2(), projectionMatrix), multiplyProjection(triangleTranslated.getDot3(), projectionMatrix));

                triangleProjection.getDot1().setX((triangleProjection.getDot1().getX() + 1.0) * WINDOW_WIDTH * 0.5);
                triangleProjection.getDot1().setY((triangleProjection.getDot1().getY() + 1.0) * WINDOW_HEIGHT * 0.5);
                triangleProjection.getDot2().setX((triangleProjection.getDot2().getX() + 1.0) * WINDOW_WIDTH * 0.5);
                triangleProjection.getDot2().setY((triangleProjection.getDot2().getY() + 1.0) * WINDOW_HEIGHT * 0.5);
                triangleProjection.getDot3().setX((triangleProjection.getDot3().getX() + 1.0) * WINDOW_WIDTH * 0.5);
                triangleProjection.getDot3().setY((triangleProjection.getDot3().getY() + 1.0) * WINDOW_HEIGHT * 0.5);

                double dotProduct = normal.getX() * light.getX() + normal.getY() * light.getY() + normal.getZ() * light.getZ();

                Triangle tr = new Triangle(triangleProjection);
                trianglesWD.put(tr,dotProduct );
                triangles.add(tr);
                int x[] = {(int)triangleProjection.getDot1().getX(),(int)triangleProjection.getDot2().getX(), (int)triangleProjection.getDot3().getX()};
                int y[] = {(int)triangleProjection.getDot1().getY(),(int)triangleProjection.getDot2().getY(), (int)triangleProjection.getDot3().getY()};

                }
            }


        triangles.sort((t1, t2) ->{
            double z1 = (t1.getDot1().getZ() + t1.getDot2().getZ() + t1.getDot3().getZ()) / 3;
            double z2 = (t2.getDot1().getZ() + t2.getDot2().getZ() + t2.getDot3().getZ()) / 3;
            if(z1 > z2) {
                return -1;
            } else if (z1 < z2) {
                return 1;
            } else return 0;
        });
        for(Triangle triangle : triangles){
            double dotProduct = trianglesWD.get(triangle);

            int x[] = {(int)triangle.getDot1().getX(),(int)triangle.getDot2().getX(), (int)triangle.getDot3().getX()};
            int y[] = {(int)triangle.getDot1().getY(),(int)triangle.getDot2().getY(), (int)triangle.getDot3().getY()};


            g2.setColor(new Color(0, 0, 255));
            g2.fillPolygon(x, y, 3);
            g2.setColor(new Color(0, 0, 0, dotProduct > 0 ? (int)(dotProduct * 200) : 200));
            g2.fillPolygon(x, y, 3);
        }
        g2.dispose();
    }


    public Dot multiplyProjection(Dot i, double[][] m){
        Dot o = new Dot();
        o.setX(i.getX() * m[0][0] + i.getY() * m[1][0] + i.getZ() * m[2][0] + m[3][0]);
        o.setY(i.getX() * m[0][1] + i.getY() * m[1][1] + i.getZ() * m[2][1] + m[3][1]);
        o.setZ(i.getX() * m[0][2] + i.getY() * m[1][2] + i.getZ() * m[2][2] + m[3][2]);
        double w =i.getX() * m[0][3] + i.getY() * m[1][3] + i.getZ() * m[2][3] + m[3][3];
        if(w != 0.0){
            o.setX(o.getX() / w);
            o.setY(o.getY() / w);
            o.setZ(o.getZ() / w);
        }
        return o;
    }

    public Dot getNormal(Triangle triangle){
        double x1 = triangle.getDot2().getX() - triangle.getDot1().getX();
        double y1 = triangle.getDot2().getY() - triangle.getDot1().getY();
        double z1 = triangle.getDot2().getZ() - triangle.getDot1().getZ();

        double x2 = triangle.getDot3().getX() - triangle.getDot1().getX();
        double y2 = triangle.getDot3().getY() - triangle.getDot1().getY();
        double z2 = triangle.getDot3().getZ() - triangle.getDot1().getZ();

        Dot dot = new Dot((y1 * z2 - z1 * y2), (z1 * x2 - x1 * z2), (x1 * y2 - y1 * x2));

        double length  = Math.sqrt(dot.getX() * dot.getX() + dot.getY() * dot.getY() + dot.getZ() * dot.getZ());
        dot.setX( dot.getX() / length);
        dot.setY( dot.getY() / length);
        dot.setZ( dot.getZ() / length);

        return dot;
    }

}


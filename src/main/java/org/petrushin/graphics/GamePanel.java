package org.petrushin.graphics;

import org.petrushin.graphics.figure.Dot;
import org.petrushin.graphics.figure.Figure;
import org.petrushin.graphics.figure.FiguresReader;
import org.petrushin.graphics.figure.Triangle;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class GamePanel extends JPanel implements Runnable{

    private final int WINDOW_WIDTH = 800;
    private final int WINDOW_HEIGHT = 800;
    private KeyHandler keyHandler = new KeyHandler();

    private int Fps = 60;

    private final int fov = 90;
    private double a = (double) WINDOW_HEIGHT/(double) WINDOW_WIDTH;
    private double t = 1.0 / Math.tan(Math.toRadians(fov/2));
    private double maxDistance = 1000.0;
    private double distanceToDisplay = 10.1;
    private double z11 = maxDistance / (maxDistance - distanceToDisplay);
    private double z22 = (maxDistance * distanceToDisplay) / (maxDistance - distanceToDisplay);
    //точка положения камеры в мировой системе координат
    private Dot camera = new Dot(0.0, 0.0, 0.0);
    //длинна вкторов для системы координат камеры
    private double viewPointDistance = 1.0;
    //Z вектор системы координат камеры
    private Dot viewPoint = new Dot(0.0, 0.0, 1.0);
    //X вектор системы координат камеры
    private Dot cameraX = new Dot(1.0, 0.0, 0.0);
    //Y вектор системы координат камеры
    private Dot cameraY = new Dot(0.0, -1.0, 0.0);



    private Dot[] cameraDots = {camera, viewPoint, cameraX, cameraY};

    //X, Y и Z ветора мировой системы. Они не изменны
    private final Dot worldX = new Dot(1.0, 0.0, 0.0);
    private final Dot worldY = new Dot(0.0, -1.0, 0.0);
    private final Dot worldZ = new Dot(0.0, 0.0, 1.0);




    private Dot light = new Dot (0.0, 0.0, -1.0);
    double lengthLight = Math.sqrt(light.getX() * light.getX() + light.getY() * light.getY() + light.getZ() * light.getZ());

    //матрица для проецировния точек на экран 3д -> 2д
    private double[][] projectionMatrix = {{0.0, 0.0, 0.0, 0.0},{0.0, 0.0, 0.0, 0.0},{0.0, 0.0, 0.0, 0.0},{0.0, 0.0, 0.0, 0.0}};
    //атрица поворота
    private double[][] matrixRot = {{0.0, 0.0, 0.0, 0.0},{0.0, 0.0, 0.0, 0.0},{0.0, 0.0, 0.0, 0.0},{0.0, 0.0, 0.0, 0.0}};

    private double alpha = 0.0;
    private double beta = 0.0;
    private double theta = 0.0;


    private Figure teapot = FiguresReader.readFigureFromFile("F:\\IntelliJ IDEA\\FPSGame\\3dGameJavaSwing\\src\\main\\resources\\teapot.txt");
    private Figure cube = FiguresReader.readFigureFromFile("F:\\IntelliJ IDEA\\FPSGame\\3dGameJavaSwing\\src\\main\\resources\\tinker.txt");
    private Figure mountain = FiguresReader.readFigureFromFile("F:\\IntelliJ IDEA\\FPSGame\\3dGameJavaSwing\\src\\main\\resources\\mountain.txt");

    public GamePanel(){
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

        if(keyHandler.rightPressed){
            alpha--;
            if (alpha == -1) alpha=360;
            rotateCamera();

        }
        if(keyHandler.leftPressed){
            alpha++;
            if (alpha == 361) alpha=0;
            rotateCamera();
        }

        if(keyHandler.upPressed){
            beta++;
            if(beta > 90) beta = 90;
            rotateCamera();
        }

        if(keyHandler.downPressed){
            beta--;
            if(beta < -90) beta =  -90;
            rotateCamera();
        }
        if(keyHandler.moveForwardPressed){
            moveDots(viewPoint, true);
            rotateCamera();
        }
        if(keyHandler.moveBackPressed){
            moveDots(viewPoint, false);
            rotateCamera();
        }
        if(keyHandler.moveRightPressed){
            moveDots(cameraX, true);
            rotateCamera();
        }
        if(keyHandler.moveLeftPressed){
            moveDots(cameraX, false);
            rotateCamera();
        }
    }

    private void rotateCamera() {
        //Углы поворота Эйлера и матрица к ним но эта матрица для углов ZXZ, я использую YXY
        //https://ru.wikipedia.org/wiki/%D0%A3%D0%B3%D0%BB%D1%8B_%D0%AD%D0%B9%D0%BB%D0%B5%D1%80%D0%B0
        //Матрицы для поворотов
        //https://ru.wikipedia.org/wiki/%D0%9C%D0%B0%D1%82%D1%80%D0%B8%D1%86%D0%B0_%D0%BF%D0%BE%D0%B2%D0%BE%D1%80%D0%BE%D1%82%D0%B0
        //матрица для вращения по горизонтали
//        matrixRot1[0][0] = Math.cos(Math.toRadians(alpha));
//        matrixRot1[0][2] = Math.sin(Math.toRadians(alpha));
//        matrixRot1[1][1] = 1;
//        matrixRot1[2][0] = -Math.sin(Math.toRadians(alpha));
//        matrixRot1[2][2] = Math.cos(Math.toRadians(alpha));
//        matrixRot1[3][3] = 1;
////матрица для вращения по вертикали
//        matrixRot[0][0] = 1;
//        matrixRot[1][1] = Math.cos(Math.toRadians(beta));
//        matrixRot[1][2] = -Math.sin(Math.toRadians(beta));
//        matrixRot[2][1] = Math.sin(Math.toRadians(beta));
//        matrixRot[2][2] = Math.cos(Math.toRadians(beta));
//        matrixRot[3][3] = 1;
        //произмедение 2х последних матриц
        matrixRot[0][0] = Math.cos(Math.toRadians(alpha));
        matrixRot[0][1] = Math.sin(Math.toRadians(alpha)) * Math.sin(Math.toRadians(beta));
        matrixRot[0][2] = Math.sin(Math.toRadians(alpha)) * Math.cos(Math.toRadians(beta));
        matrixRot[1][1] = Math.cos(Math.toRadians(beta));
        matrixRot[1][2] = -Math.sin(Math.toRadians(beta));
        matrixRot[2][0] = -Math.sin(Math.toRadians(alpha));
        matrixRot[2][1] = Math.cos(Math.toRadians(alpha)) * Math.sin(Math.toRadians(beta));
        matrixRot[2][2] = Math.cos(Math.toRadians(alpha)) * Math.cos(Math.toRadians(beta));
        matrixRot[3][3] = 1;


        cameraX.changeCoordinate(multiplyProjection(worldX, matrixRot));
        viewPoint.changeCoordinate(multiplyProjection(worldZ, matrixRot));
        cameraY.changeCoordinate(multiplyProjection(worldY, matrixRot));
    }

    //перемещение камеры и векторов системы камеры
    private void moveDots(Dot i, boolean dir){
        i.setX(i.getX() / 10);
        i.setY(i.getY() / 10);
        i.setZ(i.getZ()  / 10);
        //проэкция вектора по которому перемещаемся на мировые вектора, что бы определирть величину смещения точек камеры
        double distanceX = (i.getX() * worldX.getX() + i.getY() * worldX.getY() + i.getZ() * worldX.getZ()) / Math.sqrt(worldX.getX() * worldX.getX() + worldX.getY() * worldX.getY() + worldX.getZ() * worldX.getZ());
        double distanceY = (i.getX() * worldY.getX() + i.getY() * worldY.getY() + i.getZ() * worldY.getZ()) / Math.sqrt(worldY.getX() * worldY.getX() + worldY.getY() * worldY.getY() + worldY.getZ() * worldY.getZ());
        double distanceZ = (i.getX() * worldZ.getX() + i.getY() * worldZ.getY() + i.getZ() * worldZ.getZ()) / Math.sqrt(worldZ.getX() * worldZ.getX() + worldZ.getY() * worldZ.getY() + worldZ.getZ() * worldZ.getZ());

        if(dir) {
            for (Dot dot : cameraDots) {
                dot.setX(dot.getX() + distanceX);
                dot.setY(dot.getY() - distanceY);
                dot.setZ(dot.getZ() + distanceZ);
            }
        } else {
            for (Dot dot : cameraDots) {
                dot.setX(dot.getX() - distanceX);
                dot.setY(dot.getY() + distanceY);
                dot.setZ(dot.getZ() - distanceZ);
            }
        }
    }

    public void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;


        Map<Triangle, Double> trianglesWD = new HashMap<>();
        List<Triangle> triangles = new ArrayList<>();


        for(Triangle triangle : mountain.getTriangles()) {
            //координаты треугольника переведенные из мировой системы в систему камеры
            Triangle triangleRotated = new Triangle(getDotRelationCamera(triangle.getDot1()), getDotRelationCamera(triangle.getDot2()), getDotRelationCamera(triangle.getDot3()));

            //в теории не должно прорисовывать объекты которые за камерой

            if(!checkVisibility(triangleRotated)) continue;

//            if(triangle.getDot1().getZ() <= camera.getZ() || triangle.getDot2().getZ() <= camera.getZ() || triangle.getDot3().getZ() <= camera.getZ()) continue;
//            boolean visible = true;
//            for(Dot dot : triangleRotated.getAllDots()){
//
//                if(Math.toDegrees(Math.acos((dot.getX() * (viewPoint.getX() - camera.getX()) + dot.getY() * (viewPoint.getY() - camera.getY()) + dot.getZ() * (viewPoint.getZ() - camera.getZ())) / (Math.sqrt(dot.getX() * dot.getX() + dot.getY() * dot.getY() + dot.getZ() * dot.getZ())) / Math.sqrt((viewPoint.getX() - camera.getX()) * (viewPoint.getX() - camera.getX()) + (viewPoint.getY() - camera.getY()) * (viewPoint.getY() - camera.getY()) + (viewPoint.getZ() - camera.getZ()) * (viewPoint.getZ() - camera.getZ())))) > 90) visible = false;
//            }
//            if(!visible) continue;



            Dot normal = getNormal(triangleRotated);

            if (normal.getX() * (triangleRotated.getDot1().getX() - camera.getX()) + normal.getY() * (triangleRotated.getDot1().getY() - camera.getY()) + normal.getZ() * (triangleRotated.getDot1().getZ() - camera.getZ()) > 0.0) {

                //проэкция треугольника на плоскость (3д -> 2д)
                Triangle triangleProjection = new Triangle(multiplyProjection(triangleRotated.getDot1(), projectionMatrix), multiplyProjection(triangleRotated.getDot2(), projectionMatrix), multiplyProjection(triangleRotated.getDot3(), projectionMatrix));

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
            g2.setColor(new Color(0, 0, 0, dotProduct < 0 ? (int)(-dotProduct * 150) : 150));
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

    //получения вектора нормали
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

    //перевод координат точки из мировой системы, в координаты системы камеры
    public Dot getDotRelationCamera(Dot i){
        Dot o = new Dot();
        //вектор направленный из камеры на точку
        Dot d = new Dot();
        d.setX(i.getX() - camera.getX());
        d.setY(i.getY() - camera.getY());
        d.setZ(i.getZ() - camera.getZ());

        //координата z для камеры
        double z = (d.getX() * viewPoint.getX() + d.getY() * viewPoint.getY() + d.getZ() * viewPoint.getZ()) / Math.sqrt(viewPoint.getX() * viewPoint.getX() + viewPoint.getY() * viewPoint.getY() + viewPoint.getZ() * viewPoint.getZ());
        double x = (d.getX() * cameraX.getX() + d.getY() * cameraX.getY() + d.getZ() * cameraX.getZ()) / Math.sqrt(cameraX.getX() * cameraX.getX() + cameraX.getY() * cameraX.getY() + cameraX.getZ() * cameraX.getZ());
        double y = (d.getX() * cameraY.getX() + d.getY() * cameraY.getY() + d.getZ() * cameraY.getZ()) / Math.sqrt(cameraY.getX() * cameraY.getX() + cameraY.getY() * cameraY.getY() + cameraY.getZ() * cameraY.getZ());
        //далее нужно спроэктировать этот вектор на вектор направления взгляда, определить длинну проекции -> длинна проекчии Z для дальнейших расчетов проэкции точки на экран
        o.setZ(z);
        o.setX(x);
        o.setY(y);
        return o;
    }

    public boolean checkVisibility(Triangle triangle){
        boolean b = true;

        for(Dot dot : triangle.getAllDots()){
            //проекция точки на плоскость xz амеры
            Dot dotX = new Dot(dot.getX() - camera.getX(), camera.getY(), dot.getZ() - camera.getZ());
            //проекция точки на плоскость yz амеры
            Dot dotY = new Dot ( camera.getX(), dot.getY() - camera.getZ(), dot.getZ() - camera.getZ());

            Dot viewDot = new Dot(viewPoint.getX() - camera.getX(), viewPoint.getY() - camera.getY(), viewPoint.getZ() - camera.getZ());
            if(     Math.toDegrees(Math.acos((dot.getX() * viewDot.getX() + dot.getY() * viewDot.getY() + dot.getZ() * viewDot.getZ()) / (Math.sqrt(dot.getX() * dot.getX() + dot.getY() * dot.getY() + dot.getZ() * dot.getZ()) * Math.sqrt(viewDot.getX() * viewDot.getX() + viewDot.getY() * viewDot.getY() + viewDot.getZ() *viewDot.getZ())))) >= 45) b = false;



//            if(     Math.toDegrees(Math.acos((dotX.getX() * viewDot.getX() + dotX.getY() * viewDot.getY() + dotX.getZ() * viewDot.getZ()) / (Math.sqrt(dotX.getX() * dotX.getX() + dotX.getY() * dotX.getY() + dotX.getZ() * dotX.getZ()) * Math.sqrt(viewDot.getX() * viewDot.getX() + viewDot.getY() * viewDot.getY() + viewDot.getZ() *viewDot.getZ())))) > 45 ||
//                    Math.toDegrees(Math.acos((dotY.getX() * viewDot.getX() + dotY.getY() * viewDot.getY() + dotY.getZ() * viewDot.getZ()) / (Math.sqrt(dotY.getX() * dotY.getX() + dotY.getY() * dotY.getY() + dotY.getZ() * dotY.getZ()) * Math.sqrt(viewDot.getX() * viewDot.getX() + viewDot.getY() * viewDot.getY() + viewDot.getZ() *viewDot.getZ())))) > 45) b =false;
        }

        return b;
    }

}


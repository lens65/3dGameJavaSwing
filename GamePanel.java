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
    private MouseMotionListener mouse = new MouseMotionListener();

    private int Fps = 60;

    private final int fov = 90;
    private double a = (double) WINDOW_HEIGHT/(double) WINDOW_WIDTH;
    private double t = 1.0 / Math.tan(Math.toRadians(fov/2));
    private double maxDistance = 10000.0;
//    private double distanceToDisplay = 10.1;
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


    private Figure teapot = FiguresReader.readFigureFromFile(10.0, 10.0, 10.0, "F:\\IntelliJ IDEA\\FPSGame\\3dGameJavaSwing\\src\\main\\resources\\teapot.txt");
    private Figure cube = FiguresReader.readFigureFromFile(0.0,0.0,5.0, "F:\\IntelliJ IDEA\\FPSGame\\3dGameJavaSwing\\src\\main\\resources\\tinker.txt");
    private Figure cube1 = FiguresReader.readFigureFromFile(0.0,0.0,6.0, "F:\\IntelliJ IDEA\\FPSGame\\3dGameJavaSwing\\src\\main\\resources\\tinker.txt");
    private Figure cube2 = FiguresReader.readFigureFromFile(1.0,0.0,5.0, "F:\\IntelliJ IDEA\\FPSGame\\3dGameJavaSwing\\src\\main\\resources\\tinker.txt");
    private Figure cube3 = FiguresReader.readFigureFromFile(0.5,1.0,5.0, "F:\\IntelliJ IDEA\\FPSGame\\3dGameJavaSwing\\src\\main\\resources\\tinker.txt");
//    private Figure mountain = FiguresReader.readFigureFromFile(0,0,0,"F:\\IntelliJ IDEA\\FPSGame\\3dGameJavaSwing\\src\\main\\resources\\mountain.txt");

    private List<Figure> figures = new ArrayList<>();


    public GamePanel(){
        this.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
        this.addMouseMotionListener(mouse);
        this.addKeyListener(keyHandler);
        this.setFocusable(true);
    }

    Thread gameThread;

    private void setDefaultValues(){
        //добавление стандарных значений в матрицу трансформации
        projectionMatrix[0][0] = a * t;
        projectionMatrix[1][1] = t;
        projectionMatrix[2][2] = z11;
        projectionMatrix[3][2] = -z22;
        projectionMatrix[2][3] = 1.0;
        projectionMatrix[3][3] = 0.0;
        light.setX(light.getX() / lengthLight);
        light.setY(light.getY() / lengthLight);
        light.setZ(light.getZ() / lengthLight);

        //создания списка фигур для отображения
//        figures.add(cube);
//        figures.add(cube1);
//        figures.add(cube2);
//        figures.add(cube3);
        figures.add(teapot);
//        figures.add(mountain);

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

    alpha = 0;
    beta = 0;
    double[][] rotMatrix = new double[4][4];

    if(keyHandler.rightPressed){
        alpha++;

        rotMatrix = correctCameraRotation(worldY, alpha);
        viewPoint = multiplyProjection(viewPoint, rotMatrix);
        cameraX = multiplyProjection(cameraX, rotMatrix);
        cameraY = multiplyProjection(cameraY, rotMatrix);
    } else if(keyHandler.leftPressed){
        alpha--;
        rotMatrix = correctCameraRotation(worldY, alpha);
        viewPoint = multiplyProjection(viewPoint, rotMatrix);
        cameraX = multiplyProjection(cameraX, rotMatrix);
        cameraY = multiplyProjection(cameraY, rotMatrix);
    }

    if(keyHandler.upPressed){
        beta++;
        rotMatrix = correctCameraRotation(cameraX, beta);
        viewPoint = multiplyProjection(viewPoint, rotMatrix);
        cameraY = multiplyProjection(cameraY, rotMatrix);
    } else if(keyHandler.downPressed){
        beta--;
        rotMatrix = correctCameraRotation(cameraX, beta);
        viewPoint = multiplyProjection(viewPoint, rotMatrix);
        cameraY = multiplyProjection(cameraY, rotMatrix);
    }

    if(keyHandler.moveForwardPressed){
        moveDots(viewPoint, true);
    }else if(keyHandler.moveBackPressed){
        moveDots(viewPoint, false);
    }

    if(keyHandler.moveRightPressed){
        moveDots(cameraX, true);
    }else if(keyHandler.moveLeftPressed){
        moveDots(cameraX, false);
    }


}

    private double[][] correctCameraRotation(Dot dot, double theta){
        double[][] matrix = {{1.0, 0.0, 0.0, 0.0},{0.0, 1.0, 0.0, 0.0},{0.0, 0.0, 1.0, 0.0},{0.0, 0.0, 0.0, 1.0}};

        double t = Math.toRadians(theta);
        double cosT = Math.cos(t);
        double sinT = Math.sin(t);

        matrix[0][0] = ((cosT + (1 - cosT) * dot.getX() * dot.getX()));
        matrix[0][1] = (((1 - cosT) * dot.getX() * dot.getY() - sinT * dot.getZ()));
        matrix[0][2] = (((1 - cosT) * dot.getX() * dot.getZ() + sinT * dot.getY()));

        matrix[1][0] = (((1 - cosT) * dot.getY() * dot.getX() + sinT * dot.getZ()));
        matrix[1][1] = ((cosT + (1 - cosT) * dot.getY() * dot.getY()));
        matrix[1][2] = (((1 - cosT) * dot.getY() * dot.getZ() - sinT * dot.getX()));

        matrix[2][0] = (((1 - cosT) * dot.getZ() * dot.getX() - sinT * dot.getY()));
        matrix[2][1] = (((1 - cosT) * dot.getZ() * dot.getY() + sinT * dot.getX()));
        matrix[2][2] = ((cosT + (1 - cosT) * dot.getZ() * dot.getZ()));

        return matrix;
    }

    //перемещение камеры и векторов системы камеры
    private void moveDots(Dot i, boolean dir){
        Dot d = new Dot();

        d.setX(i.getX() / 10);
        d.setY(i.getY() / 10);
        d.setZ(i.getZ()  / 10);

        if(dir) {
            camera.setX(camera.getX() +  d.getX());
            camera.setY(camera.getY() + d.getY());
            camera.setZ(camera.getZ() + d.getZ());
        } else {
            camera.setX(camera.getX() -  d.getX());
            camera.setY(camera.getY() - d.getY());
            camera.setZ(camera.getZ() - d.getZ());
        }

    }

    public void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        Map<Triangle, Double> trianglesWD = new HashMap<>();
        List<Triangle> triangles = new ArrayList<>();
        for(Figure figure : figures) {
            for (Triangle triangle : figure.getTriangles()) {

                //координаты треугольника переведенные из мировой системы в систему камеры
                Triangle triangleRotated = new Triangle(getDotRelationCamera(triangle.getDot1()), getDotRelationCamera(triangle.getDot2()), getDotRelationCamera(triangle.getDot3()));

                Dot normal = getNormal(triangleRotated);

                if ((normal.getX() - camera.getX()) * (triangleRotated.getDot1().getX() - camera.getX()) + (normal.getY() - camera.getY()) * (triangleRotated.getDot1().getY() - camera.getY()) + (normal.getZ() - camera.getZ()) * (triangleRotated.getDot1().getZ() - camera.getZ()) > 0.0) {
                    //проэкция треугольника на плоскость (3д -> 2д)
                    Triangle triangleProjection = new Triangle(multiplyProjection(triangleRotated.getDot1(), projectionMatrix), multiplyProjection(triangleRotated.getDot2(), projectionMatrix), multiplyProjection(triangleRotated.getDot3(), projectionMatrix));
                    //не прорисовывать объект за "спиной"
                    if (triangleProjection.getDot1().getZ() > 1 ||
                            triangleProjection.getDot2().getZ() > 1 ||
                            triangleProjection.getDot3().getZ() > 1
                    ) continue;

                    triangleProjection.getDot1().setX((triangleProjection.getDot1().getX() + 1.0) * WINDOW_WIDTH * 0.5);
                    triangleProjection.getDot1().setY((triangleProjection.getDot1().getY() + 1.0) * WINDOW_HEIGHT * 0.5);
                    triangleProjection.getDot2().setX((triangleProjection.getDot2().getX() + 1.0) * WINDOW_WIDTH * 0.5);
                    triangleProjection.getDot2().setY((triangleProjection.getDot2().getY() + 1.0) * WINDOW_HEIGHT * 0.5);
                    triangleProjection.getDot3().setX((triangleProjection.getDot3().getX() + 1.0) * WINDOW_WIDTH * 0.5);
                    triangleProjection.getDot3().setY((triangleProjection.getDot3().getY() + 1.0) * WINDOW_HEIGHT * 0.5);

                    double dotProduct = normal.getX() * light.getX() + normal.getY() * light.getY() + normal.getZ() * light.getZ();

                    Triangle tr = new Triangle(triangleProjection);
                    trianglesWD.put(tr, dotProduct);
                    triangles.add(tr);
                }

            }
            triangles.sort((t1, t2) -> {
                double z1 = (t1.getDot1().getZ() + t1.getDot2().getZ() + t1.getDot3().getZ()) / 3;
                double z2 = (t2.getDot1().getZ() + t2.getDot2().getZ() + t2.getDot3().getZ()) / 3;
                if (z1 > z2) {
                    return -1;
                } else if (z1 < z2) {
                    return 1;
                } else return 0;
            });
            for (Triangle triangle : triangles) {
                double dotProduct = trianglesWD.get(triangle);

                int x[] = {(int) triangle.getDot1().getX(), (int) triangle.getDot2().getX(), (int) triangle.getDot3().getX()};
                int y[] = {(int) triangle.getDot1().getY(), (int) triangle.getDot2().getY(), (int) triangle.getDot3().getY()};


                g2.setColor(new Color(0, 0, 255));
                g2.fillPolygon(x, y, 3);
                g2.setColor(new Color(0, 0, 0, dotProduct < 0 ? (int) (-dotProduct * 150) : 150));
                g2.fillPolygon(x, y, 3);
            }
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

        //координата x, y, z для камеры
        double z = (d.getX() * viewPoint.getX() + d.getY() * viewPoint.getY() + d.getZ() * viewPoint.getZ());
        double x = (d.getX() * cameraX.getX() + d.getY() * cameraX.getY() + d.getZ() * cameraX.getZ());
        double y = (d.getX() * cameraY.getX() + d.getY() * cameraY.getY() + d.getZ() * cameraY.getZ());

        //далее нужно спроэктировать этот вектор на вектор направления взгляда, определить длинну проекции -> длинна проекчии Z для дальнейших расчетов проэкции точки на экран
        o.setZ(z);
        o.setX(x);
        o.setY(y);
        return o;
    }
}


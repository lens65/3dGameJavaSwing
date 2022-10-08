package org.petrushin.graphics;

import javax.swing.*;
import java.awt.*;

public class MainWindow {

    private JFrame window  = new JFrame();
//    private GamePanel gamePanel = new GamePanel();
    private GamePanel1 gamePanel = new GamePanel1();

    //создание главного окна приложения
    public void drawWindow(){
        //при нажатии на крестик программа заканчивает выполнение
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //запретить изменять размер окна
//        window.setResizable(false);
        window.add(gamePanel);
        window.pack();
        window.setLocationRelativeTo(null);
        //разместить окно по центру экрана (при размере окна 800х600рх)
//        window.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width - WINDOW_WIDTH) / 2 , (Toolkit.getDefaultToolkit().getScreenSize().height  - WINDOW_HEIGHT) / 2);
        //сделать окно видимым
        window.setVisible(true);
        //задать стандартный размер окна
//        window.setSize(WINDOW_WIDTH,WINDOW_HEIGHT);
        gamePanel.startGame();
    }
}

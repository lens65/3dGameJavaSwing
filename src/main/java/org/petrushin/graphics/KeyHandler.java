package org.petrushin.graphics;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener {

    public boolean upPressed;
    public boolean downPressed;
    public boolean leftPressed;
    public boolean rightPressed;
    public boolean moveForwardPressed;
    public boolean moveBackPressed;
    public boolean moveRightPressed;
    public boolean moveLeftPressed;

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {

        int code = e.getKeyCode();

        if(code == KeyEvent.VK_W){
            upPressed = true;
        }
        if(code == KeyEvent.VK_S){
            downPressed = true;
        }
        if(code == KeyEvent.VK_A){
            leftPressed = true;
        }
        if(code == KeyEvent.VK_D){
            rightPressed = true;
        }
        if(code == KeyEvent.VK_UP){
            moveForwardPressed = true;
        }
        if(code == KeyEvent.VK_DOWN){
            moveBackPressed = true;
        }
        if(code == KeyEvent.VK_RIGHT){
            moveRightPressed = true;
        }
        if(code == KeyEvent.VK_LEFT){
            moveLeftPressed = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();

        if(code == KeyEvent.VK_W){
            upPressed = false;
        }
        if(code == KeyEvent.VK_S){
            downPressed = false;
        }
        if(code == KeyEvent.VK_A){
            leftPressed = false;
        }
        if(code == KeyEvent.VK_D){
            rightPressed = false;
        }
        if(code == KeyEvent.VK_UP){
            moveForwardPressed = false;
        }
        if(code == KeyEvent.VK_DOWN){
            moveBackPressed = false;
        }
        if(code == KeyEvent.VK_RIGHT){
            moveRightPressed = false;
        }
        if(code == KeyEvent.VK_LEFT){
            moveLeftPressed = false;
        }
    }

}

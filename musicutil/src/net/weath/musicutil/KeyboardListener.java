package net.weath.musicutil;

import java.awt.event.MouseEvent;

public interface KeyboardListener {

    void keyPressed(int n, MouseEvent e);

    void keyReleased(int n, MouseEvent e);
}

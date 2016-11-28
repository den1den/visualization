/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package volvis;

import com.jogamp.opengl.GL2;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import util.TFChangeListener;

/**
 * Abstract class to implement an rendering of the dataset to an image
 * @author michel
 */
public abstract class Renderer implements TFChangeListener {
     int winWidth, winHeight;
    boolean visible = false;
    boolean interactiveMode = false;
    public final ArrayList<TFChangeListener> listeners = new ArrayList<>();

    public void setInteractiveMode(boolean flag) {
        interactiveMode = flag;
    }

    public boolean isInteractiveMode() {
        return interactiveMode;
    }
    
    public void setWinWidth(int w) {
        winWidth = w;
    }

    public void setWinHeight(int h) {
        winHeight = h;
    }

    public int getWinWidth() {
        return winWidth;
    }

    public int getWinHeight() {
        return winHeight;
    }

    public void setVisible(boolean flag) {
        visible = flag;
    }

    public boolean getVisible() {
        return visible;
    }

    public void addTFChangeListener(TFChangeListener l) {
        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    @Override
    public void changed() {
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).changed();
        }
    }
    
    public abstract void visualize(GL2 gl);
}

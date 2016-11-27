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
public abstract class Renderer {
     int winWidth, winHeight;
    boolean visible = false;
    boolean interactiveMode = false;
    public final ArrayList<TFChangeListener> listeners = new ArrayList<TFChangeListener>();

    public Renderer() {
        
    }

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
    
    public abstract void visualize(GL2 gl);

    public static void setRGB(BufferedImage image, int i, int j, TFColor voxelColor) {
        int c_alpha = voxelColor.a <= 1.0 ? (int) Math.floor(voxelColor.a * 255) : 255;
        int c_red = voxelColor.r <= 1.0 ? (int) Math.floor(voxelColor.r * 255) : 255;
        int c_green = voxelColor.g <= 1.0 ? (int) Math.floor(voxelColor.g * 255) : 255;
        int c_blue = voxelColor.b <= 1.0 ? (int) Math.floor(voxelColor.b * 255) : 255;
        int pixelColor = (c_alpha << 24) | (c_red << 16) | (c_green << 8) | c_blue;
        image.setRGB(i, j, pixelColor);
    }
}

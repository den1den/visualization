/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package volume;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author michel
 */
public class Volume {
    
    public Volume(int xd, int yd, int zd) {
        data = new short[xd*yd*zd];
        dimX = xd;
        dimY = yd;
        dimZ = zd;
    }
    
    public Volume(File file) {
        
        try {
            VolumeIO reader = new VolumeIO(file);
            dimX = reader.getXDim();
            dimY = reader.getYDim();
            dimZ = reader.getZDim();
            data = reader.getData().clone();
            computeHistogram();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        
    }
    
    /**
     * @param x
     * @param y
     * @param z
     * @return Voxel at (x, y, z)
     */
    public short getVoxel(int x, int y, int z) {
        return data[x + dimX*(y + dimY * z)];
    }
    
    public void setVoxel(int x, int y, int z, short value) {
        data[x + dimX*(y + dimY*z)] = value;
    }

    public void setVoxel(int i, short value) {
        data[i] = value;
    }
    
    /**
     * @param i index of Voxel in linear fashion
     * @return Voxel at i
     */
    public short getVoxel(int i) {
        return data[i];
    }
    
    /**
     * @return size of data in x direction
     */
    public int getDimX() {
        return dimX;
    }
    
    /**
     * @return size of data in y direction
     */
    public int getDimY() {
        return dimY;
    }
    
    /**
     * @return size of data in z direction
     */
    public int getDimZ() {
        return dimZ;
    }

    /**
     * @return minimum intensity
     */
    public short getMinimum() {
        short minimum = data[0];
        for (int i=0; i<data.length; i++) {
            minimum = data[i] < minimum ? data[i] : minimum;
        }
        return minimum;
    }

    /**
     * @return maximum intensity
     */
    public short getMaximum() {
        short maximum = data[0];
        for (int i=0; i<data.length; i++) {
            maximum = data[i] > maximum ? data[i] : maximum;
        }
        return maximum;
    }
 
    /**
     * Get a histogram of all the intensities.
     * @return histogram s.t. histogram[intensity] = frequency
     */
    public int[] getHistogram() {
        return histogram;
    }
    
    private void computeHistogram() {
        histogram = new int[getMaximum() + 1];
        for (int i=0; i<data.length; i++) {
            histogram[data[i]]++;
        }
    }
    
    private int dimX, dimY, dimZ;
    private short[] data;
    private int[] histogram;
}

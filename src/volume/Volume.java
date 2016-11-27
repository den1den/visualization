/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package volume;

import com.jogamp.opengl.math.geom.AABBox;
import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;
import java.io.File;
import java.io.IOException;
import util.VectorMath;

/**
 *
 * @author michel
 */
public class Volume {

    public Volume(int xd, int yd, int zd) {
        data = new short[xd * yd * zd];
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
        return data[x + dimX * (y + dimY * z)];
    }

    public void setVoxel(int x, int y, int z, short value) {
        data[x + dimX * (y + dimY * z)] = value;
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
        for (int i = 0; i < data.length; i++) {
            minimum = data[i] < minimum ? data[i] : minimum;
        }
        return minimum;
    }

    /**
     * @return maximum intensity
     */
    public short getMaximum() {
        short maximum = data[0];
        for (int i = 0; i < data.length; i++) {
            maximum = data[i] > maximum ? data[i] : maximum;
        }
        return maximum;
    }

    /**
     * Get a histogram of all the intensities.
     *
     * @return histogram s.t. histogram[intensity] = frequency
     */
    public int[] getHistogram() {
        return histogram;
    }

    private void computeHistogram() {
        histogram = new int[getMaximum() + 1];
        for (int i = 0; i < data.length; i++) {
            histogram[data[i]]++;
        }
    }

    private int dimX, dimY, dimZ;
    private short[] data;
    private int[] histogram;

    public double[] scaleVector(double[] v) {
        return new double[]{
            v[0] * dimX,
            v[1] * dimY,
            v[2] * dimZ
        };
    }

    public double[] getCenter() {
        return new double[]{
            (double) dimX / 2,
            (double) dimY / 2,
            (double) dimZ / 2
        };
    }

    public double[] getDim() {
        return new double[]{dimX, dimY, dimZ};
    }

    public int getDim(int i) {
        if (i == 0) {
            return dimX;
        }
        if (i == 1) {
            return dimY;
        }
        if (i == 2) {
            return dimZ;
        }
        throw new IllegalArgumentException();
    }
    
    public double[] intersect(double[] p, double[] r){
        assert VectorMath.isUnit(r);
        final double x = p[0];
        final double y = p[1];
        final double z = p[2];
        
        final double dx = r[0];
        final double dy = r[1];
        final double dz = r[2];
        
        double t0 = Double.NEGATIVE_INFINITY, t1 = Double.POSITIVE_INFINITY;
        final double EPSILON = 1./256/256/256;
        
        if(dx > EPSILON){
            double t0_x = - x / dx;
            double t1_x = (dimX - 1 - x) / dx;
            
            t0 = Math.min(t0_x, t1_x);
            t1 = Math.max(t0_x, t1_x);
        }
        if(dy > EPSILON){
            final double t0_y = - y / dy;
            final double t1_y = (dimY - 1 - y) / dy;
            
            t0 = Math.max(t0, Math.min(t0_y, t1_y));
            t1 = Math.min(t1, Math.max(t0_y, t1_y));
        }
        if(dz > EPSILON){
            final double t0_z = - z / dz;
            final double t1_z = (dimZ - 1 - z) / dz;

            t0 = Math.max(t0, Math.min(t0_z, t1_z));
            t1 = Math.min(t1, Math.max(t0_z, t1_z));
        }
        
        if ( t0 < t1 ){
            // intersects
            return new double[]{t0, t1};
        } else {
            return null;
        }
    }

    public int getMinDim() {
        return Math.min(dimX, Math.min(dimY, dimZ));
    }

    /**
     * Shortcut for getting a Voxel
     *
     * @param coord array of (x, y, z) coordinates
     * @return Voxel at (x, y, z)
     */
    public short getVoxel(double[] coord) {
        if (coord[0] < 0 || coord[0] > dimX || coord[1] < 0 || coord[1] > dimY
                || coord[2] < 0 || coord[2] > dimZ) {
            return 0;
        }

        int x = (int) Math.floor(coord[0]);
        int y = (int) Math.floor(coord[1]);
        int z = (int) Math.floor(coord[2]);

        return getVoxel(x, y, z);
    }

    public float getTriVoxel(double[] coord) {
        return getTriVoxel(coord[0], coord[1], coord[2]);
    }

    /**
     * Getting a trilinear interpolated Voxel value
     */
    public float getTriVoxel(double x, double y, double z) {
        int x0 = (int) Math.floor(x);
        int x1 = (int) Math.ceil(x);
        int y0 = (int) Math.floor(y);
        int y1 = (int) Math.ceil(y);
        int z0 = (int) Math.floor(z);
        int z1 = (int) Math.ceil(z);
        
        if (x0 < 0 || x1 > dimX - 1 || y0 < 0 || y1 > dimY - 1 || z0 < 0 || z1 > dimZ - 1) {
            return 0;
        }

        short v000 = getVoxel(x0, y0, z0);
        short v001 = getVoxel(x0, y0, z1);
        short v010 = getVoxel(x0, y1, z0);
        short v011 = getVoxel(x0, y1, z1);
        short v100 = getVoxel(x1, y0, z0);
        short v101 = getVoxel(x1, y0, z1);
        short v110 = getVoxel(x1, y1, z0);
        short v111 = getVoxel(x1, y1, z1);

        double v00, v01, v10, v11;
        if(x0 == x1){
            v00 = (double) (v000);
            v01 = (double) (v001);
            v10 = (double) (v010);
            v11 = (double) (v011);
        } else {
            double dx = (x - x0) / (x1 - x0);
            double dxI = 1 - dx;
            
            v00 = (double) (v000) * dxI + (double) (v100) * dx;
            v01 = (double) (v001) * dxI + (double) (v101) * dx;
            v10 = (double) (v010) * dxI + (double) (v110) * dx;
            v11 = (double) (v011) * dxI + (double) (v111) * dx;
        }
        
        double v0, v1;
        if(y0 == y1){
            v0 = v00;
            v1 = v01;
        } else {
            double dy = (y - y0) / (y1 - y0);
            double dyI = 1 - dy;

            v0 = v00 * dyI + v10 * dy;
            v1 = v01 * dyI + v11 * dy;
        }
        
        double v;
        if(z0 == z1){
            v = v0;
        } else {
            double dz = (z - z0) / (z1 - z0);
            double dzI = 1 - dz;

            v = v0 * dzI + v1 * dz;
        }
        
        return (float) v;
    }
}

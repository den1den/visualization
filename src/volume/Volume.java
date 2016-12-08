/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package volume;

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
    
    public short getVoxelSafe(int x, int y, int z){
        assert x >= 0;
        assert x <= dimX - 1;
        assert y >= 0;
        assert y <= dimY - 1;
        assert z >= 0;
        assert z <= dimZ - 1;
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

    public double[] getMinPos() {
        return VectorMath.getZero();
    }

    public double[] getMaxPos() {
        return new double[]{dimX - 1, dimY - 1, dimZ - 1};
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
    
    public boolean intersect(double[] result, double[] p, double[] r){
        assert VectorMath.isUnit(r);
        final double x = p[0];
        final double y = p[1];
        final double z = p[2];
        
        final double dx = r[0];
        final double dy = r[1];
        final double dz = r[2];
        
        result[0] = Double.NEGATIVE_INFINITY;
        result[1] = Double.POSITIVE_INFINITY;
        final double EPSILON = 1./256/256/256;
        
        if(Math.abs(dx) > EPSILON){
            double t0_x = - x / dx;
            double t1_x = (dimX - 1 - x) / dx;
            
            result[0] = Math.min(t0_x, t1_x);
            result[1] = Math.max(t0_x, t1_x);
        }
        if(Math.abs(dy) > EPSILON){
            final double t0_y = - y / dy;
            final double t1_y = (dimY - 1 - y) / dy;
            
            result[0] = Math.max(result[0], Math.min(t0_y, t1_y));
            result[1] = Math.min(result[1], Math.max(t0_y, t1_y));
        }
        if(Math.abs(dz) > EPSILON){
            final double t0_z = - z / dz;
            final double t1_z = (dimZ - 1 - z) / dz;

            result[0] = Math.max(result[0], Math.min(t0_z, t1_z));
            result[1] = Math.min(result[1], Math.max(t0_z, t1_z));
        }
        
        return result[0] < result[1];
    }

    public int getMinIntersectionLength() {
        return Math.min(dimX, Math.min(dimY, dimZ));
    }

    /**
     * Floored Voxel
     */
    public short getFloorVoxel(double x, double y, double z) {
        if (x < 0 || x > dimX - 1 || y < 0 || y > dimY - 1 || z < 0 || z > dimZ - 1) {
            return 0;
        }

        int xI = (int) Math.floor(x);
        int yI = (int) Math.floor(y);
        int zI = (int) Math.floor(z);

        return getVoxel(xI, yI, zI);
    }

    /**
     * Nearest neighbor Voxel
     */
    public float getNNVoxel(float x, float y, float z){
        if (x < 0 || x > dimX - 1 || y < 0 || y > dimY - 1 || z < 0 || z > dimZ - 1) {
            return 0;
        }

        int xI = (int) Math.round(x);
        int yI = (int) Math.round(y);
        int zI = (int) Math.round(z);

        return getVoxel(xI, yI, zI);
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
    
    public double getMaxIntersectionLength(){
        return Math.sqrt(dimX * dimX + dimY * dimY + dimZ * dimZ);
    }

    public double[] getLogHistogram() {
        double[] logHistogram = new double[histogram.length];
        for (int i = 0; i < histogram.length; i++) {
            logHistogram[i] = Math.log(histogram[i]);
        }
        return logHistogram;
    }
}

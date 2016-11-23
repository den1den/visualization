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

    
    public double[] intersect_inside(double[] dir) {
        double[] r = VectorMath.extend_to_box(dir);
        VectorMath.pairwiseMult(r, 0.5, getDim());
        VectorMath.addVector(r, getCenter());
        return r;
    }

    final double zAngle = Math.tan(Math.sqrt(2));
    final double xyAngle = Math.tan(1 / 2);
    @Deprecated
    public void intersect_inside_old(final double[] result, double[] dir) {
        final double x = Math.abs(dir[0]);
        final double y = Math.abs(dir[1]);
        final double z = Math.abs(dir[2]);

        final double projZ = Math.sqrt(x * x + y * y);
        if (y == x) {
            final double phi = Math.atan(z);
            if(phi > Math.PI / 2) {
                result[0] = 1;
                result[1] = 1;
                result[2] = Math.cos(Math.PI/2 - phi) * Math.sqrt(2);
            } else if (phi < Math.PI / 2) {
                final double th = Math.atan2(y, x);
                final double rProjZlTop = Math.tan(phi);
                result[0] = Math.cos(th) * rProjZlTop;
                result[1] = Math.sin(th) * rProjZlTop;
                result[2] = 1;
            } else {
                result[0] = 1;
                result[1] = 1;
                result[2] = 1;
            }
        } else {
            final double th = Math.atan2(y, x);
            final double rProjZX;
            final double rProjZY;
            final double rProjZl;
            if (y < x) {
                rProjZX = x * Math.cos(x);
                rProjZY = 1;
            } else {
                rProjZX = 1;
                rProjZY = x * Math.cos(Math.PI / 2 - y);
            }
            rProjZl = Math.sqrt(rProjZX * rProjZX + rProjZY * rProjZY);
            if (z == rProjZl) {
                result[0] = rProjZX;
                result[1] = rProjZY;
                result[2] = 1;
            } else {
                final double phi = Math.atan(z);
                if (z < rProjZl) {
                    result[0] = rProjZX;
                    result[1] = rProjZY;
                    result[2] = Math.tan(Math.PI/2 - phi) * rProjZl;
                } else {
                    final double rProjZlTop = Math.tan(phi);
                    result[0] = Math.cos(th) * rProjZlTop;
                    result[1] = Math.sin(th) * rProjZlTop;
                    result[2] = 1;
                }
            }
        }
        result[0] *= dimX;
        result[1] *= dimY;
        result[2] *= dimZ;
    }

    /**
     * Calculate intersection based on
     * {@link com.jogamp.opengl.math.geom.AABBox#getRayIntersection}
     *
     * @param result placeholder for result
     * @param p starting point of ray
     * @param dir direction of ray
     * @return
     */
    @Deprecated
    public double[] intersect(final double[] result, double[] p, double[] dir) {
        final double[] maxT = {-1f, -1f, -1f};
        final float epsilon = 0.01f;

        boolean inside = true;

        // Find candidate planes.
        for (int i = 0; i < 3; i++) {
            if (p[i] < 0) {
                result[i] = 0;
                inside = false;

                // Calculate T distances to candidate planes
                if (0 != Double.doubleToLongBits(dir[i])) {
                    maxT[i] = -p[i] / dir[i];
                }
            } else if (p[i] > getDim(i)) {
                result[i] = getDim(i);
                inside = false;

                // Calculate T distances to candidate planes
                if (0 != Double.doubleToLongBits(dir[i])) {
                    maxT[i] = (getDim(i) - p[i]) / dir[i];
                }
            }
        }

        // Ray origin inside bounding box
        if (inside && false) {
            System.arraycopy(p, 0, result, 0, 3);
            return result;
        }

        // Get largest of the maxT's for final choice of intersection
        int whichPlane = 0;
        if (maxT[1] > maxT[whichPlane]) {
            whichPlane = 1;
        }
        if (maxT[2] > maxT[whichPlane]) {
            whichPlane = 2;
        }

        boolean assumeIntersection = true;
        if (!assumeIntersection) {
            // Check final candidate actually inside box
            if (0 != (Double.doubleToLongBits(maxT[whichPlane]) & 0x80000000)) {
                return null;
            }

            switch (whichPlane) {
                case 0:
                    result[1] = p[1] + maxT[whichPlane] * dir[1];
                    if (result[1] < -epsilon || result[1] > dimY + epsilon) {
                        return null;
                    }
                    result[2] = p[2] + maxT[whichPlane] * dir[2];
                    if (result[2] < -epsilon || result[2] > dimZ + epsilon) {
                        return null;
                    }
                    break;
                case 1:
                    result[0] = p[0] + maxT[whichPlane] * dir[0];
                    if (result[0] < -epsilon || result[0] > dimX + epsilon) {
                        return null;
                    }
                    result[2] = p[2] + maxT[whichPlane] * dir[2];
                    if (result[2] < -epsilon || result[2] > dimZ + epsilon) {
                        return null;
                    }
                    break;
                case 2:
                    result[0] = p[0] + maxT[whichPlane] * dir[0];
                    if (result[0] < -epsilon || result[0] > dimX + epsilon) {
                        return null;
                    }
                    result[1] = p[1] + maxT[whichPlane] * dir[1];
                    if (result[1] < -epsilon || result[1] > dimY + epsilon) {
                        return null;
                    }
                    break;
                default:
                    throw new Error();
            }
        } else {
            switch (whichPlane) {
                case 0:
                    result[1] = p[1] + maxT[whichPlane] * dir[1];
                    result[2] = p[2] + maxT[whichPlane] * dir[2];
                    break;
                case 1:
                    result[0] = p[0] + maxT[whichPlane] * dir[0];
                    result[2] = p[2] + maxT[whichPlane] * dir[2];
                    break;
                case 2:
                    result[0] = p[0] + maxT[whichPlane] * dir[0];
                    result[1] = p[1] + maxT[whichPlane] * dir[1];
                    break;
                default:
                    throw new Error();
            }
        }
        return result; // ray hits box
    }
}

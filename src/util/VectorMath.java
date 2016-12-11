/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

/**
 *
 * @author michel
 */
public class VectorMath {

    // assign coefficients c0..c2 to vector v
    public static void setVector(double[] v, double c0, double c1, double c2) {
        v[0] = c0;
        v[1] = c1;
        v[2] = c2;
    }

    // assign coefficients c0..c2 to vector v
    public static void setVector(double[] v, double[] w) {
        v[0] = w[0];
        v[1] = w[1];
        v[2] = w[2];
    }

    // compute dotproduct of vectors v and w
    public static double getDotProduct(double[] v, double[] w) {
        double r = 0;
        for (int i=0; i<3; i++) {
            r += v[i] * w[i];
        }
        return r;
    }

    // compute distance between vectors v and w
    public static double distance(double[] v, double[] w) {
        double[] tmp = new double[3];
        VectorMath.setVector(tmp, v[0]-w[0], v[1]-w[1], v[2]-w[2]);
        return Math.sqrt(VectorMath.getDotProduct(tmp, tmp));
    }

    // compute dotproduct of v and w
    public static double[] setCrossProduct(double[] v, double[] w, double[] r) {
        r[0] = v[1] * w[2] - v[2] * w[1];
        r[1] = v[2] * w[0] - v[0] * w[2];
        r[2] = v[0] * w[1] - v[1] * w[0];
        return r;
    }
    
    public static double getLength(double[] v) {
        return Math.sqrt(v[0]*v[0] + v[1]*v[1] + v[2]*v[2]);
    }

    // compute length of vector v
    public static double length(double[] v) {
        return getLength(v);
    }

    public static void setAddVector(double[] v, double[] add) {
        v[0] += add[0];
        v[1] += add[1];
        v[2] += add[2];
    }
    
    public static void setAddVector(double[] v, double scalar, double[] add) {
        v[0] += scalar * add[0];
        v[1] += scalar * add[1];
        v[2] += scalar * add[2];
    }

    public static void setScale(double[] v, double s) {
        v[0] *= s;
        v[1] *= s;
        v[2] *= s;
    }
    
    public static double[] to_spherical_unit(double[] cart){
        assert Math.sqrt(cart[0] * cart[0] + cart[1] * cart[1] + cart[2] * cart[2]) == 1;
        return new double[]{
            1,
            Math.atan2(cart[1], cart[0]),
            Math.acos(cart[2] / 1)
        };
    }

    public static double[] getCopy(double[] v) {
        return new double[]{
            v[0],
            v[1],
            v[2]
        };
    }

    public static void setCopySign(double[] v, double[] sign) {
        Math.copySign(v[0], sign[0]);
        Math.copySign(v[1], sign[1]);
        Math.copySign(v[2], sign[2]);
    }

    public static void setPairwiseMult(double[] v, double[] m) {
        v[0] *= m[0];
        v[1] *= m[1];
        v[2] *= m[2];
    }

    public static void setPairwiseMult(double[] v, double d, double[] m) {
        v[0] *= d * m[0];
        v[1] *= d * m[1];
        v[2] *= d * m[2];
    }

    public static double[] getZero() {
        return new double[3];
    }

    public static void setNormalize(double[] v) {
        double l = getLength(v);
        setScale(v, 1.0/l);
    }

    public static boolean isUnit(double[] v) {
        return length(v) == 1;
    }

    public static double length2(double[] v) {
        return v[0] * v[0] + v[1] * v[1] + v[2] * v[2];
    }

    public static double[] newVector(double x, double y, double z) {
        return new double[]{
            x, y, z
        };
    }

    public static double[] getAddVector(double[] v, double s, double[] d) {
        return new double[]{
            v[0] + s * d[0],
            v[1] + s * d[1],
            v[2] + s * d[2]
        };
    }

    public static double[] getNormalized(double[] v) {
        double l = getLength(v);
        return new double[]{
            v[0] / l,
            v[1] / l,
            v[2] / l
        };
    }

    public static double[] getScale(double[] v, double s) {
        return new double[]{
            v[0] * s,
            v[1] * s,
            v[2] * s
        };
    }
}

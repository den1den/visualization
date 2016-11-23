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
    public static double dotproduct(double[] v, double[] w) {
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
        return Math.sqrt(VectorMath.dotproduct(tmp, tmp));
    }

    // compute dotproduct of v and w
    public static double[] crossproduct(double[] v, double[] w, double[] r) {
        r[0] = v[1] * w[2] - v[2] * w[1];
        r[1] = v[2] * w[0] - v[0] * w[2];
        r[2] = v[0] * w[1] - v[1] * w[0];
        return r;
    }
    
    // compute length of vector v
    public static double length(double[] v) {
        return Math.sqrt(v[0]*v[0] + v[1]*v[1] + v[2]*v[2]);
    }

    public static void addVector(double[] v, double[] add) {
        v[0] += add[0];
        v[1] += add[1];
        v[2] += add[2];
    }
    
    public static void addVector(double[] v, double scalar, double[] add) {
        v[0] += scalar * add[0];
        v[1] += scalar * add[1];
        v[2] += scalar * add[2];
    }

    public static double[] scale(double[] v, double s) {
        v[0] *= s;
        v[1] *= s;
        v[2] *= s;
        return v;
    }

    public static float[] asFloat(double[] v) {
        return new float[]{
            (float) v[0],
            (float) v[1],
            (float) v[2]
        };
    }
    
    public static double[] to_spherical_unit(double[] cart){
        assert Math.sqrt(cart[0] * cart[0] + cart[1] * cart[1] + cart[2] * cart[2]) == 1;
        return new double[]{
            1,
            Math.atan2(cart[1], cart[0]),
            Math.acos(cart[2] / 1)
        };
    }

    public static double[] copy(double[] v) {
        double[] c = new double[3];
        System.arraycopy(v, 0, c, 0, 3);
        return c;
    }

    public static void copysign(double[] v, double[] sign) {
        Math.copySign(v[0], sign[0]);
        Math.copySign(v[1], sign[1]);
        Math.copySign(v[2], sign[2]);
    }

    public static void pairwiseMult(double[] v, double[] m) {
        v[0] *= m[0];
        v[1] *= m[1];
        v[2] *= m[2];
    }

    public static void pairwiseMult(double[] v, double d, double[] m) {
        v[0] *= d * m[0];
        v[1] *= d * m[1];
        v[2] *= d * m[2];
    }

    public static double[] zero() {
        return new double[3];
    }

    public static double[] norm(double[] v) {
        double l = len(v);
        return scale(v, 1.0/l);
    }

    public static double len(double[] v) {
        return Math.sqrt(v[0]*v[0] + v[1] * v[1] + v[2] * v[2]);
    }
    
    public static double[] extend_to_box(double[] v){
        double[] r = extend_to_box_positive(v);
        VectorMath.copysign(r, v);
        return r;
    }

    static double[] extend_to_box_positive(double[] v) {
        final double x = Math.abs(v[0]);
        final double y = Math.abs(v[1]);
        final double z = Math.abs(v[2]);
        // intersect with x == 1
        double[] ix = VectorMath.copy(v);
        VectorMath.scale(ix, 1 / x);
        if (ix[1] > 1) {
            // intersect with y == 1
            double[] iy = VectorMath.copy(v);
            VectorMath.scale(iy, 1 / y);
            if (iy[2] <= 1) {
                return iy;
            }
            // intersect with z == 1
            double[] iz = VectorMath.copy(v);
            VectorMath.scale(iz, 1 / z);
            return iz;
        } else {
            // intersect with z == 1
            double[] iz = VectorMath.copy(v);
            VectorMath.scale(iz, 1 / z);
            if (iz[0] <= 1) {
                return iz;
            }
            return ix;
        }
    }
}

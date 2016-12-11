/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package volume;

import java.io.Serializable;
import util.VectorMath;

/**
 * Vector
 *
 * @author michel
 */
public class VoxelGradient implements Serializable {

    private static final long serialVersionUID = -8890477117370292870L;

    public final float x, y, z;
    public final float mag;

    public VoxelGradient() {
        x = y = z = mag = 0.0f;
    }

    public VoxelGradient(float gx, float gy, float gz) {
        x = gx;
        y = gy;
        z = gz;
        mag = (float) Math.sqrt(x * x + y * y + z * z);
    }

    public double dot(double[] v) {
        return v[0] * x + v[1] * y + v[2] * z;
    }

}

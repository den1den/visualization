/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package volvis.raycaster;

import java.awt.image.BufferedImage;
import util.VectorMath;
import volume.Volume;
import volvis.TFColor;
import volvis.TransferFunction;


public class Mip extends RaycastRenderer.RendererClass {

    public Mip(RaycastRenderer r) {
        super(r);
    }

    @Override
    protected void render(double[] viewVec, double[] uVec, double[] vVec) {
        // image
        BufferedImage image = r.getImage();
        final int imageCenter = image.getWidth() / 2;
        final int imageHeight = r.image.getWidth();
        final int imageWidth = r.image.getWidth();

        // volume
        Volume volume = r.getVolume();
        final double[] volumeCenter = volume.getCenter();

        // color
        final TransferFunction tf = r.getTF();

        // q = sample on a line through the origin of the volume data
        double[] q = new double[3];
        double[] ts = new double[2]; // intersection points with bounding box

        double[] dq = VectorMath.getCopy(viewVec);
        double dv = (double) (volume.getMinIntersectionLength()) / (r.steps + 1);
        VectorMath.setScale(dq, dv);

        for (int j = 0; j < imageHeight; j++) {
            for (int i = 0; i < imageWidth; i++) {
                // q = projection of a pixel to the 'slicer'-plane through image origin
                VectorMath.setVector(q, volumeCenter);
                VectorMath.setAddVector(q, (i - imageCenter), uVec);
                VectorMath.setAddVector(q, (j - imageCenter), vVec);

                // calculate raycast intersection
                if (!volume.intersect(ts, q, viewVec)) {
                    // No intersection
                    image.setRGB(i, j, 0);
                    continue;
                }
                final double t0 = ts[0];
                final double t1 = ts[1];

                VectorMath.setAddVector(q, t0, viewVec);

                int steps = (int) Math.ceil((t1 - t0) / dv); // assert |viewVec|=1

                float maxVoxel = 0;

                for (int k = 0; k < steps + 1; k++) {
                    float voxel = r.getVoxel(q[0], q[1], q[2]);
                    if (voxel > maxVoxel) {
                        maxVoxel = voxel;
                    }

                    VectorMath.setAddVector(q, dq);
                }
                TFColor color = tf.getColor((int) maxVoxel);
                r.setPixel(i, j, color);

                if (i % 100 == 0 && j == i) {
                    // System.out.printf("i=%d, j=%d, steps=%d\n", i, j, steps);
                }
            }
        }
    }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package volvis.raycaster;

import java.awt.image.BufferedImage;
import util.VectorMath;
import volume.GradientVolume;
import volume.Volume;
import volvis.TFColor;
import volvis.TransferFunction;
import static volvis.raycaster.RaycastRenderer.setPixel;


public class Mip extends RaycastRenderer.RendererClass {

    public Mip(RaycastRenderer r) {
        super(r);
    }

    @Override
    protected void render(double[] view, double[] uVec, double[] vVec) {
        final double[] q = new double[3];
        final double[] lambdas = new double[2];
        final boolean interactive = r.isInteractiveMode();

        // image
        final BufferedImage image = r.getImage();
        final int imageCenter = image.getWidth() / 2;
        final int imageHeight = image.getWidth();
        final int imageWidth = image.getWidth();

        // volume
        final Volume volume = r.getVolume();
        final TransferFunction tf = r.getTF();
        final double[] volumeCenter = volume.getCenter();

        // set sampeling vector s.t. at least `r.steps` are made throught the volume
        double dView = ((double) volume.getMinIntersectionLength()) / (r.steps - 1);
        final double[] dq = VectorMath.getScale(view, dView);

        for (int j = 0; j < imageHeight; j++) {
            for (int i = 0; i < imageWidth; i++) {
                // foreach pixel
                VectorMath.setVector(q, volumeCenter);
                VectorMath.setAddVector(q, (i - imageCenter), uVec);
                VectorMath.setAddVector(q, (j - imageCenter), vVec);
                // vector q is on the ray of this pixel

                // calculate raycast intersection
                if (!volume.intersect(lambdas, q, view)) {
                    // No intersection
                    image.setRGB(i, j, 0);
                    continue;
                }

                // closest intersection
                final double lambda_0 = lambdas[0];
                final int stepsBack = -(int) Math.ceil(lambda_0 / dView);
                // furthest intersection
                final double lambda_1 = lambdas[1];
                final int stepsFurther = (int) Math.ceil(lambda_1 / dView);

                // set q at furthest point
                VectorMath.setAddVector(q, -stepsBack * dView, view);

                // prepare pixel
                final int steps = stepsBack + stepsFurther;
                
                float maxVoxel = 0;
                for (int k = 0; k < steps + 1; k++) {
                    float voxel = r.getVoxelValue(q[0], q[1], q[2]);
                    if (voxel > maxVoxel) {
                        maxVoxel = voxel;
                    }

                    VectorMath.setAddVector(q, dq);
                }
                TFColor color = tf.getColor((int) maxVoxel);
                setPixel(image, i, j, color);
            }
        }
    }
    
}

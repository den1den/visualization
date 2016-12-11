/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package volvis.raycaster;

import gui.TransferFunction2DEditor;
import java.awt.image.BufferedImage;
import util.VectorMath;
import volume.GradientVolume;
import volume.Volume;
import volvis.TFColor;
import static volvis.raycaster.RaycastRenderer.setPixel;


public class Compositing extends RaycastRenderer.RendererClass {

    public Compositing(RaycastRenderer r) {
        super(r);
    }

    @Override
    protected void render(double[] view, double[] uVec, double[] vVec) {
        final double[] q = new double[3];
        final double[] lambdas = new double[2];
        final int[] voxelPos = new int[3];

        // image
        final BufferedImage image = r.getImage();
        final int imageCenter = image.getWidth() / 2;
        final int imageHeight = image.getWidth();
        final int imageWidth = image.getWidth();

        // volume
        final Volume volume = r.getVolume();
        final GradientVolume gv = r.getGradients();
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

                // prepare pixel value
                double pixelColorR = 0, pixelColorG = 0, pixelColorB = 0;
                double cumAlpha = 1;
                for (int s = 0; s < stepsBack + stepsFurther; s++) {
                    TFColor sampledC = r.getTFColor(q[0], q[1], q[2]);
                    pixelColorR += sampledC.r * sampledC.a * cumAlpha;
                    pixelColorG += sampledC.g * sampledC.a * cumAlpha;
                    pixelColorB += sampledC.b * sampledC.a * cumAlpha;
                    cumAlpha = cumAlpha * (1 - sampledC.a);

                    VectorMath.setAddVector(q, dq);
                }
                
                setPixel(image, i, j, 1, pixelColorR, pixelColorG, pixelColorB);

                if (i % 100 == 0 && j == i) {
                    // System.out.printf("i=%d, j=%d, steps=%d\n", i, j, steps);
                }
            }
        }
    }
    
}

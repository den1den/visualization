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
import volume.VoxelGradient;
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
        final boolean interactive = r.isInteractiveMode();
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

        final double alphaCorrectionFactor = 300.0 / r.steps;

        int missedRays = 0, cutoffRays = 0, totalSamples = 0;
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
                    missedRays++;
                    continue;
                }

                // closest intersection
                final double lambda_0 = lambdas[0];
                final int stepsBack = -(int) Math.ceil(lambda_0 / dView);
                // furthest intersection
                final double lambda_1 = lambdas[1];
                final int stepsFurther = (int) Math.ceil(lambda_1 / dView);

                // set q at furthest point
                double distance = 0;
                VectorMath.setAddVector(q, -stepsBack * dView, view);

                // prepare pixel
                double pixelColorR = 0, pixelColorG = 0, pixelColorB = 0;
                double cumAlpha = 0; // 'empty space' is transparant
                final int steps = stepsBack + stepsFurther;
                for (int s = 0; s < steps; s++) {
                    // sample allong the ray, from front to back
                    final TFColor sampleColor = r.getTFColor(q[0], q[1], q[2]);
                    final double baseColorR = sampleColor.r;
                    final double baseColorG = sampleColor.g;
                    final double baseColorB = sampleColor.b;
                    
                    final double sampleAlpha = 1 - Math.pow(1 - sampleColor.a, alphaCorrectionFactor);

                    double I;
                    if (!r.shading || interactive) {
                        I = 1;
                    } else {
                        r.getPosition(voxelPos, q[0], q[1], q[2]);
                        final VoxelGradient vGradient = gv.getGradient(voxelPos[0], voxelPos[1], voxelPos[2]);
                        double dot = vGradient.normalDot(vVec);
                        if (dot > 0) {
                            double l_dot_n = dot;
                            double n_dot_h = Math.pow(dot, r.phongAlpha);

                            double distFactor;
                            distFactor = 1.0 / (r.options.phongK1 + r.options.phongK2 * distance);
                            //distFactor = 1;

                            I = (r.phongKa + distFactor * (r.phongKd * l_dot_n + r.phongKs * n_dot_h));
                        } else {
                            // skip
                            I = 1;
                        }
                    }
                    
                    final double cumAlphaFactor = (1 - cumAlpha);
                    pixelColorR += I * baseColorR * sampleAlpha * cumAlphaFactor;
                    pixelColorG += I * baseColorG * sampleAlpha * cumAlphaFactor;
                    pixelColorB += I * baseColorB * sampleAlpha * cumAlphaFactor;

                    cumAlpha = cumAlpha + (1 - cumAlpha) * sampleAlpha;
                    if (interactive && cumAlpha >= 0.7) {
                        cutoffRays++;
                        s = stepsBack + stepsFurther;
                    }
                    VectorMath.setAddVector(q, dq);
                    distance += dView;
                    totalSamples++;
                }
                // the background is black

                setPixel(image, i, j, 1, pixelColorR, pixelColorG, pixelColorB);
            }
        }
        if (!interactive) {
            int hitRays = imageWidth * imageHeight - missedRays;
            System.out.printf("missedRays/rays = %.3f, cutoffRays/hitRays = %.3f, totalSamples=%d\n",
                    ((double) missedRays) / (imageWidth * imageHeight),
                    ((double) cutoffRays) / (hitRays),
                    totalSamples
            );
        }
    }

}

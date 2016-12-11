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
import volume.VoxelGradient;
import volvis.TFColor;
import static volvis.raycaster.RaycastRenderer.setPixel;

/**
 *
 * @author dennis
 */
public class TF2D extends RaycastRenderer.RendererClass {

    private final float EPSILON_GRADIENT = 0.001f;
    private final float EPSILON_VOXEL = 0.001f;

    public TF2D(RaycastRenderer r) {
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

        // 2d transfer function
        final TransferFunction2DEditor.TriangleWidget t = r.getIsoContourTriangle();
        final double baseColorR = t.color.r;
        final double baseColorG = t.color.g;
        final double baseColorB = t.color.b;
        final double baseAlpha = t.color.a;
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
                    r.getPosition(voxelPos, q[0], q[1], q[2]);
                    final float vIntensity = volume.getVoxel(voxelPos[0], voxelPos[1], voxelPos[2]);
                    final VoxelGradient vGradient = gv.getGradient(voxelPos[0], voxelPos[1], voxelPos[2]);
                    
                    double sampleAlpha;
                    float voxelDiff = Math.abs(vIntensity - t.baseIntensity);
                    if (vGradient.mag <= EPSILON_GRADIENT) {
                        if (voxelDiff <= EPSILON_VOXEL) {
                            sampleAlpha = 1;
                        } else {
                            sampleAlpha = 0;
                        }
                    } else {
                        // |gradient| > 0
                        if (voxelDiff <= t.radius * vGradient.mag) {
                            sampleAlpha = 1 - 1 / t.radius * voxelDiff / vGradient.mag;
                        } else {
                            sampleAlpha = 0;
                        }
                    }
                    sampleAlpha *= baseAlpha;
                    sampleAlpha = 1 - Math.pow(1 - sampleAlpha, alphaCorrectionFactor);
                    
                    final double cumAlphaFactor = (1 - cumAlpha);
                    
                    if (!r.shading) {
                        pixelColorR += baseColorR * sampleAlpha * cumAlphaFactor;
                        pixelColorG += baseColorG * sampleAlpha * cumAlphaFactor;
                        pixelColorB += baseColorB * sampleAlpha * cumAlphaFactor;
                        
                        totalSamples++;
                        cumAlpha = cumAlpha + (1 - cumAlpha) * sampleAlpha;
                    } else {
                        double dot = vGradient.dot(vVec);
                        if (dot > 0) {
                            double l_dot_n = dot;
                            double n_dot_h = Math.pow(dot, r.phongAlpha);

                            double distFactor = 1.0 / (r.options.phongK1 + r.options.phongK2 * distance);
                            distFactor = 1;

                            final double phongR = r.phongKa + distFactor * (sampleAlpha * r.phongKd * l_dot_n + r.phongKs * n_dot_h);
                            final double phongG = r.phongKa + distFactor * (sampleAlpha * r.phongKd * l_dot_n + r.phongKs * n_dot_h);
                            final double phongB = r.phongKa + distFactor * (sampleAlpha * r.phongKd * l_dot_n + r.phongKs * n_dot_h);

                            pixelColorR += baseColorB * phongR * cumAlphaFactor;
                            pixelColorG += baseColorG * phongG * cumAlphaFactor;
                            pixelColorB += baseColorB * phongB * cumAlphaFactor;
                            
                            totalSamples++;
                            cumAlpha = cumAlpha + (1 - cumAlpha) * sampleAlpha;
                        } else {
                            // skip
                        }
                    }
                    
                    if (false && cumAlpha >= 0.6) {
                        cutoffRays++;
                        s = stepsBack + stepsFurther;
                    }
                    VectorMath.setAddVector(q, dq);
                    distance += dView;
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

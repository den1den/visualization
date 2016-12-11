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
import volvis.raycaster.RaycastRenderer;
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
    protected void render(double[] viewVec, double[] uVec, double[] vVec) {
        // image
        BufferedImage image = r.getImage();
        final int imageCenter = image.getWidth() / 2;
        final int imageHeight = image.getWidth();
        final int imageWidth = image.getWidth();

        // volume
        final Volume volume = r.getVolume();
        final GradientVolume gv = r.getGradients();
        final double[] volumeCenter = volume.getCenter();

        // q = sample on a line through the origin of the volume data
        double[] q = new double[3];
        double[] ts = new double[2]; // intersection points with bounding box
        int[] voxelPos = new int[3];

        double[] dq = VectorMath.getCopy(viewVec);
        double dv = (double) (volume.getMinIntersectionLength()) / (r.steps);
        VectorMath.setScale(dq, dv);

        final TransferFunction2DEditor.TriangleWidget t = r.getIsoContourTriangle();
        TFColor color = t.color;

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

                double color_r = 0, color_g = 0, color_b = 0;
                double cumAlpha = color.a;

                for (int k = 0; k < steps + 1; k++) {
                    r.getPosition(voxelPos, q[0], q[1], q[2]);
                    if (!volume.outRange(voxelPos[0], voxelPos[1], voxelPos[2])) {
                        float voxel = volume.getVoxel(voxelPos[0], voxelPos[1], voxelPos[2]);
                        VoxelGradient gradient = gv.getGradient(voxelPos[0], voxelPos[1], voxelPos[2]);

                        double alpha;
                        float voxelDiff = Math.abs(voxel - t.baseIntensity);
                        if (gradient.mag <= EPSILON_GRADIENT) {
                            if (voxelDiff <= EPSILON_VOXEL) {
                                alpha = 1;
                            } else {
                                alpha = 0;
                            }
                        } else {
                            // |gradient| > 0
                            if (voxelDiff <= t.radius * gradient.mag) {
                                alpha = 1 - 1 / t.radius * Math.abs((voxel - t.baseIntensity) / gradient.mag);
                            } else {
                                alpha = 0;
                            }
                        }
                        color_r += color.r * alpha * cumAlpha;
                        color_g += color.g * alpha * cumAlpha;
                        color_b += color.b * alpha * cumAlpha;
                        cumAlpha = cumAlpha * (1 - alpha);
                        if (cumAlpha == 0) {
                            break;
                        }
                    }
                    VectorMath.setAddVector(q, dq);
                }

                setPixel(image, i, j, 1, color_r, color_g, color_b);

                if (i % 100 == 0 && j == i) {
                    // System.out.printf("i=%d, j=%d, steps=%d\n", i, j, steps);
                }
            }
        }
    }

}

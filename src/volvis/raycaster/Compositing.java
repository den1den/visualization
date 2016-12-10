/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package volvis.raycaster;

import java.awt.Image;
import java.awt.image.BufferedImage;
import util.VectorMath;
import volume.Volume;
import volvis.TFColor;
import volvis.TransferFunction;


public class Compositing extends RaycastRenderer.RendererClass {

    public Compositing(RaycastRenderer r) {
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

        final int minSteps;
        if (r.isInteractiveMode()) {
            minSteps = r.minSteps;
        } else {
            minSteps = r.targetSteps;
        }
        final int maxSteps = (int) Math.ceil(volume.getMaxIntersectionLength() / volume.getMinIntersectionLength() * minSteps);

        // q = sample on a line through the origin of the volume data
        double[] q = new double[3];
        double[] ts = new double[2]; // intersection points with bounding box

        double[] dq = VectorMath.getCopy(viewVec);
        double dv = (double) (volume.getMinIntersectionLength()) / (minSteps + 1);
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

                double red = 0, g = 0, b = 0;
                double cumAlpha = 1;

                for (int k = 0; k < steps + 1; k++) {
                    TFColor sampledC = r.getColor(q[0], q[1], q[2]);
                    red += sampledC.r * sampledC.a * cumAlpha;
                    g += sampledC.g * sampledC.a * cumAlpha;
                    b += sampledC.b * sampledC.a * cumAlpha;
                    cumAlpha = cumAlpha * (1 - sampledC.a);

                    VectorMath.setAddVector(q, dq);
                }
                
                r.setPixel(i, j, 1, red, g, b);

                if (i % 100 == 0 && j == i) {
                    // System.out.printf("i=%d, j=%d, steps=%d\n", i, j, steps);
                }
            }
        }
    }
    
}

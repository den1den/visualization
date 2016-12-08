/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package volvis.raycaster;

import util.VectorMath;
import volvis.TFColor;


public class Compositing extends RaycastRenderer.RendererClass {

    @Override
    protected void render(double[] viewVec, double[] uVec, double[] vVec) {
        // image
        final int imageCenter = data.image.getWidth() / 2;

        // volume
        final double[] volumeCenter = data.volume.getCenter();

        final int minSteps;
        if (data.isInteractiveMode()) {
            minSteps = data.getMinSteps();
        } else {
            minSteps = data.targetSteps;
        }
        final int maxSteps = (int) Math.ceil(data.volume.getMaxIntersectionLength() / data.volume.getMinIntersectionLength() * minSteps);

        // q = sample on a line through the origin of the volume data
        double[] q = new double[3];
        double[] ts = new double[2]; // intersection points with bounding box

        double[] dq = VectorMath.getCopy(viewVec);
        double dv = (double) (data.volume.getMinIntersectionLength()) / (minSteps + 1);
        VectorMath.setScale(dq, dv);

        for (int j = 0; j < data.image.getHeight(); j++) {
            for (int i = 0; i < data.image.getWidth(); i++) {
                // q = projection of a pixel to the 'slicer'-plane through image origin
                VectorMath.setVector(q, volumeCenter);
                VectorMath.setAddVector(q, (i - imageCenter), uVec);
                VectorMath.setAddVector(q, (j - imageCenter), vVec);

                // calculate raycast intersection
                if (!data.volume.intersect(ts, q, viewVec)) {
                    // No intersection
                    data.image.setRGB(i, j, 0);
                    continue;
                }
                final double t0 = ts[0];
                final double t1 = ts[1];

                VectorMath.setAddVector(q, t0, viewVec);

                int steps = (int) Math.ceil((t1 - t0) / dv); // assert |viewVec|=1

                double r = 0, g = 0, b = 0;
                double cumAlpha = 1;

                for (int k = 0; k < steps + 1; k++) {
                    TFColor sampledC = data.tFunc.getColor((int) data.getVoxel(q[0], q[1], q[2]));
                    r += sampledC.r * sampledC.a * cumAlpha;
                    g += sampledC.g * sampledC.a * cumAlpha;
                    b += sampledC.b * sampledC.a * cumAlpha;
                    cumAlpha = cumAlpha * (1 - sampledC.a);

                    VectorMath.setAddVector(q, dq);
                }

                data.setPixel(new TFColor(r, g, b, 1), i, j);

                if (i % 100 == 0 && j == i) {
                    System.out.printf("i=%d, j=%d, steps=%d\n", i, j, steps);
                }
            }
        }
    }
    
}

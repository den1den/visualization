/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package volvis.raycaster;

import util.VectorMath;
import volvis.TFColor;

/**
 *
 * @author dennis
 */
public class CenterSlicer extends RaycastRenderer.RendererClass{

    @Override
    protected void render(double[] viewVec, double[] uVec, double[] vVec) {
        // image is square
        int imageCenter = data.image.getWidth() / 2;

        double[] volumeCenter = new double[3];
        VectorMath.setVector(volumeCenter, data.volume.getDimX() / 2, data.volume.getDimY() / 2, data.volume.getDimZ() / 2);

        // sample on a plane through the origin of the volume data
        double max = data.volume.getMaximum();
        TFColor voxelColor = new TFColor();

        for (int j = 0; j < data.image.getHeight(); j++) {
            for (int i = 0; i < data.image.getWidth(); i++) {
                double x = uVec[0] * (i - imageCenter) + vVec[0] * (j - imageCenter)
                        + volumeCenter[0];
                double y = uVec[1] * (i - imageCenter) + vVec[1] * (j - imageCenter)
                        + volumeCenter[1];
                double z = uVec[2] * (i - imageCenter) + vVec[2] * (j - imageCenter)
                        + volumeCenter[2];

                float val = data.getVoxel(x, y, z);

                data.setPixel(i, j, val);
            }
        }
    }
    
}

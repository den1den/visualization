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
import static volvis.raycaster.RaycastRenderer.setPixel;

/**
 *
 * @author dennis
 */
public class CenterSlicer extends RaycastRenderer.RendererClass{

    public CenterSlicer(RaycastRenderer r) {
        super(r);
    }

    @Override
    protected void render(double[] view, double[] uVec, double[] vVec) {
        // image
        final BufferedImage image = r.getImage();
        final int imageCenter = image.getWidth() / 2;
        final int imageHeight = image.getWidth();
        final int imageWidth = image.getWidth();

        // volume
        final Volume volume = r.getVolume();
        final float max = r.volume.getMaximum();
        final double[] volumeCenter = volume.getCenter();

        for (int j = 0; j < imageHeight; j++) {
            for (int i = 0; i < imageWidth; i++) {
                // foreach pixel
                // sample on a plane through the origin of the volume data
                double x = uVec[0] * (i - imageCenter) + vVec[0] * (j - imageCenter)
                        + volumeCenter[0];
                double y = uVec[1] * (i - imageCenter) + vVec[1] * (j - imageCenter)
                        + volumeCenter[1];
                double z = uVec[2] * (i - imageCenter) + vVec[2] * (j - imageCenter)
                        + volumeCenter[2];
                
                float v = r.getVoxelValue(x, y, z) / max;
                setPixel(image, i, j, 1, v, v, v);
            }
        }
    }
    
}

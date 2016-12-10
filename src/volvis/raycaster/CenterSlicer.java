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
    protected void render(double[] viewVec, double[] uVec, double[] vVec) {
        // image
        BufferedImage image = r.getImage();
        final int imageHeight = image.getHeight();
        final int imageWidth = image.getHeight();
        final int imageCenter = imageWidth / 2;
        

        // volume
        Volume volume = r.getVolume();
        final double[] volumeCenter = volume.getCenter();

        // color
        VectorMath.setVector(volumeCenter, volume.getDimX() / 2, volume.getDimY() / 2, volume.getDimZ() / 2);

        // sample on a plane through the origin of the volume data
        TFColor color;

        for (int j = 0; j < imageHeight; j++) {
            for (int i = 0; i < imageWidth; i++) {
                double x = uVec[0] * (i - imageCenter) + vVec[0] * (j - imageCenter)
                        + volumeCenter[0];
                double y = uVec[1] * (i - imageCenter) + vVec[1] * (j - imageCenter)
                        + volumeCenter[1];
                double z = uVec[2] * (i - imageCenter) + vVec[2] * (j - imageCenter)
                        + volumeCenter[2];
                
                color = r.getColor(x, y, z);
                setPixel(image, i, j, color);
            }
        }
    }
    
}

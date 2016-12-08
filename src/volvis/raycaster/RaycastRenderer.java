/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package volvis.raycaster;

import volume.ApxGradientVolume;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;
import gui.RaycastRendererPanel;
import gui.TransferFunction2DEditor;
import gui.TransferFunctionEditor;
import java.awt.image.BufferedImage;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import util.TFChangeListener;
import util.VectorMath;
import volume.GradientVolume;
import volume.ZeroGradientVolume;
import volume.Volume;
import volvis.Renderer;
import volvis.TFColor;
import volvis.TransferFunction;

/**
 *
 * @author michel
 */
public class RaycastRenderer extends Renderer {

    protected Volume volume = null;
    protected GradientVolume gradients = null;
    RaycastRendererPanel panel;
    public TransferFunction tFunc;
    TransferFunctionEditor tfEditor;
    TransferFunction2DEditor tfEditor2D;

    public RaycastOption OPTION;
    public ValueFunction VAL_FUNC;
    public int targetSteps = 100;

    private int getMinSteps() {
        int s = Math.min(targetSteps, (int)(10.0 / panel.getSpeed() * targetSteps));
        System.out.println("s = "+s);
        return s;
    }
    
    public enum ValueFunction{
        TRI_LINEAR,
        ROUND_DOWN,
        NEAREST
    }

    public RaycastRenderer() {
        panel = new RaycastRendererPanel(this);
        OPTION = RaycastOption.COMPOSITE;
        VAL_FUNC = ValueFunction.ROUND_DOWN;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()+"(OPTION=" + OPTION.toString() + ", VAL_FUNC=" + VAL_FUNC.toString() + ")";
    }

    /**
     * Initialize this renderer
     *
     * @param vol the new volume to use
     */
    public void setVolume(Volume vol) {
        System.out.println("Assigning volume");
        volume = vol;

        System.out.println("Computing gradients");
        gradients = new ApxGradientVolume(vol);

        // set up image for storing the resulting rendering
        // the image width and height are equal to the length of the volume diagonal
        int imageSize = (int) Math.floor(Math.sqrt(vol.getDimX() * vol.getDimX() + vol.getDimY() * vol.getDimY()
                + vol.getDimZ() * vol.getDimZ()));
        if (imageSize % 2 != 0) {
            imageSize = imageSize + 1;
        }
        image = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_ARGB);
        
        // create a standard TF of the dataset.
        // This maps an intensity value to some color value
        tFunc = new TransferFunction(volume.getMinimum(), volume.getMaximum());

        // Link the editor to our tFunc
        tFunc.addTFChangeListener(this);
        double[] logHistogram = volume.getLogHistogram();
        tfEditor = new TransferFunctionEditor(tFunc, logHistogram);

        tfEditor2D = new TransferFunction2DEditor(volume, gradients);
        tfEditor2D.addTFChangeListener(this);

        System.out.println("Finished initialization of "+toString());
    }
    
    public RaycastRendererPanel getPanel() {
        return panel;
    }

    public TransferFunction2DEditor getTF2DPanel() {
        return tfEditor2D;
    }

    public TransferFunctionEditor getTFPanel() {
        return tfEditor;
    }

    /**
     * Do GL stuff
     *
     * @param gl
     */
    private void drawBoundingBox(GL2 gl) {
        gl.glPushAttrib(GL2.GL_CURRENT_BIT);
        gl.glDisable(GL2.GL_LIGHTING);
        gl.glColor4d(1.0, 1.0, 1.0, 1.0);
        gl.glLineWidth(1.5f);
        gl.glEnable(GL.GL_LINE_SMOOTH);
        gl.glHint(GL.GL_LINE_SMOOTH_HINT, GL.GL_NICEST);
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

        gl.glBegin(GL.GL_LINE_LOOP);
        gl.glVertex3d(-volume.getDimX() / 2.0, -volume.getDimY() / 2.0, volume.getDimZ() / 2.0);
        gl.glVertex3d(-volume.getDimX() / 2.0, volume.getDimY() / 2.0, volume.getDimZ() / 2.0);
        gl.glVertex3d(volume.getDimX() / 2.0, volume.getDimY() / 2.0, volume.getDimZ() / 2.0);
        gl.glVertex3d(volume.getDimX() / 2.0, -volume.getDimY() / 2.0, volume.getDimZ() / 2.0);
        gl.glEnd();

        gl.glBegin(GL.GL_LINE_LOOP);
        gl.glVertex3d(-volume.getDimX() / 2.0, -volume.getDimY() / 2.0, -volume.getDimZ() / 2.0);
        gl.glVertex3d(-volume.getDimX() / 2.0, volume.getDimY() / 2.0, -volume.getDimZ() / 2.0);
        gl.glVertex3d(volume.getDimX() / 2.0, volume.getDimY() / 2.0, -volume.getDimZ() / 2.0);
        gl.glVertex3d(volume.getDimX() / 2.0, -volume.getDimY() / 2.0, -volume.getDimZ() / 2.0);
        gl.glEnd();

        gl.glBegin(GL.GL_LINE_LOOP);
        gl.glVertex3d(volume.getDimX() / 2.0, -volume.getDimY() / 2.0, -volume.getDimZ() / 2.0);
        gl.glVertex3d(volume.getDimX() / 2.0, -volume.getDimY() / 2.0, volume.getDimZ() / 2.0);
        gl.glVertex3d(volume.getDimX() / 2.0, volume.getDimY() / 2.0, volume.getDimZ() / 2.0);
        gl.glVertex3d(volume.getDimX() / 2.0, volume.getDimY() / 2.0, -volume.getDimZ() / 2.0);
        gl.glEnd();

        gl.glBegin(GL.GL_LINE_LOOP);
        gl.glVertex3d(-volume.getDimX() / 2.0, -volume.getDimY() / 2.0, -volume.getDimZ() / 2.0);
        gl.glVertex3d(-volume.getDimX() / 2.0, -volume.getDimY() / 2.0, volume.getDimZ() / 2.0);
        gl.glVertex3d(-volume.getDimX() / 2.0, volume.getDimY() / 2.0, volume.getDimZ() / 2.0);
        gl.glVertex3d(-volume.getDimX() / 2.0, volume.getDimY() / 2.0, -volume.getDimZ() / 2.0);
        gl.glEnd();

        gl.glBegin(GL.GL_LINE_LOOP);
        gl.glVertex3d(-volume.getDimX() / 2.0, volume.getDimY() / 2.0, -volume.getDimZ() / 2.0);
        gl.glVertex3d(-volume.getDimX() / 2.0, volume.getDimY() / 2.0, volume.getDimZ() / 2.0);
        gl.glVertex3d(volume.getDimX() / 2.0, volume.getDimY() / 2.0, volume.getDimZ() / 2.0);
        gl.glVertex3d(volume.getDimX() / 2.0, volume.getDimY() / 2.0, -volume.getDimZ() / 2.0);
        gl.glEnd();

        gl.glBegin(GL.GL_LINE_LOOP);
        gl.glVertex3d(-volume.getDimX() / 2.0, -volume.getDimY() / 2.0, -volume.getDimZ() / 2.0);
        gl.glVertex3d(-volume.getDimX() / 2.0, -volume.getDimY() / 2.0, volume.getDimZ() / 2.0);
        gl.glVertex3d(volume.getDimX() / 2.0, -volume.getDimY() / 2.0, volume.getDimZ() / 2.0);
        gl.glVertex3d(volume.getDimX() / 2.0, -volume.getDimY() / 2.0, -volume.getDimZ() / 2.0);
        gl.glEnd();

        gl.glDisable(GL.GL_LINE_SMOOTH);
        gl.glDisable(GL.GL_BLEND);
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glPopAttrib();

    }
    
    /**
     * Is called on an request to update the image, does GL stuff
     *
     * @param gl
     */
    @Override
    public void visualize(GL2 gl) {

        if (volume == null) {
            return;
        }

        drawBoundingBox(gl);

        gl.glGetDoublev(GL2.GL_MODELVIEW_MATRIX, viewMatrix, 0);

        long startTime = System.currentTimeMillis();
        
        calcImage();
        
        long endTime = System.currentTimeMillis();
        double runningTime = (endTime - startTime);
        panel.setSpeed(runningTime);

        Texture texture = AWTTextureIO.newTexture(gl.getGLProfile(), image, false);

        gl.glPushAttrib(GL2.GL_LIGHTING_BIT);
        gl.glDisable(GL2.GL_LIGHTING);
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

        // draw rendered image as a billboard texture
        texture.enable(gl);
        texture.bind(gl);
        double halfWidth = image.getWidth() / 2.0;
        gl.glPushMatrix();
        gl.glLoadIdentity();
        gl.glBegin(GL2.GL_QUADS);
        gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        gl.glTexCoord2d(0.0, 0.0);
        gl.glVertex3d(-halfWidth, -halfWidth, 0.0);
        gl.glTexCoord2d(0.0, 1.0);
        gl.glVertex3d(-halfWidth, halfWidth, 0.0);
        gl.glTexCoord2d(1.0, 1.0);
        gl.glVertex3d(halfWidth, halfWidth, 0.0);
        gl.glTexCoord2d(1.0, 0.0);
        gl.glVertex3d(halfWidth, -halfWidth, 0.0);
        gl.glEnd();
        texture.disable(gl);
        texture.destroy(gl);
        gl.glPopMatrix();

        gl.glPopAttrib();

        if (gl.glGetError() > 0) {
            System.out.println("some OpenGL error: " + gl.glGetError());
        }

    }
    
    protected BufferedImage image;
    protected double[] viewMatrix = new double[4 * 4];

    public enum RaycastOption {
        SLICER,
        MIP,
        COMPOSITE, TF2D
    }

    /**
     * Sets the RGB values in this.image on update
     *
     * @param viewMatrix the current view orientation
     */
    private void calcImage() {

        // clear image
        for (int j = 0; j < image.getHeight(); j++) {
            for (int i = 0; i < image.getWidth(); i++) {
                image.setRGB(i, j, 0);
            }
        }

        /**
         * Vector in the direction of viewing
         */
        double[] viewVec = VectorMath.newVector(viewMatrix[2], viewMatrix[6], viewMatrix[10]);
        /**
         * Vector to the right side of the screen
         */
        double[] uVec = VectorMath.newVector(viewMatrix[0], viewMatrix[4], viewMatrix[8]);
        /**
         * Vector in the upwards direction of the screen
         */
        double[] vVec = VectorMath.newVector(viewMatrix[1], viewMatrix[5], viewMatrix[9]);
        

        switch (OPTION) {
            case SLICER:
                centerSlicer(uVec, vVec);
                break;
            case MIP:
                mip(viewVec, vVec, uVec);
                break;
            case COMPOSITE:
                compositing(viewVec, vVec, uVec);
                break;
            case TF2D:
                tf2d(viewVec, vVec, uVec);
                break;
            default:
                throw new AssertionError(OPTION.name());
        }

    }

    private void centerSlicer(double[] uVec, double[] vVec) {
        // image is square
        int imageCenter = image.getWidth() / 2;
        
        double[] volumeCenter = new double[3];
        VectorMath.setVector(volumeCenter, volume.getDimX() / 2, volume.getDimY() / 2, volume.getDimZ() / 2);

        // sample on a plane through the origin of the volume data
        double max = volume.getMaximum();
        TFColor voxelColor = new TFColor();

        for (int j = 0; j < image.getHeight(); j++) {
            for (int i = 0; i < image.getWidth(); i++) {
                double x = uVec[0] * (i - imageCenter) + vVec[0] * (j - imageCenter)
                        + volumeCenter[0];
                double y = uVec[1] * (i - imageCenter) + vVec[1] * (j - imageCenter)
                        + volumeCenter[1];
                double z = uVec[2] * (i - imageCenter) + vVec[2] * (j - imageCenter)
                        + volumeCenter[2];
                
                float val = getVoxel(x, y, z);
                
                setPixel(i, j, val);
            }
        }
    }

    public float getVoxel(double x, double y, double z) throws AssertionError {
        float val;
        switch(VAL_FUNC){
            case TRI_LINEAR:
                val = volume.getTriVoxel(x, y, z);
                break;
            case ROUND_DOWN:
                val = volume.getFloorVoxel(x, y, z);
                break;
            case NEAREST:
                val = volume.getNNVoxel((float)x, (float)y, (float)z);
                break;
            default:
                throw new AssertionError(VAL_FUNC.name());
        }
        return val;
    }

    private void mip(double[] viewVec, double[] vVec, double[] uVec) {
        // image
        final int imageCenter = image.getWidth() / 2;

        // volume
        final double[] volumeCenter = volume.getCenter();

        final int minSteps;
        if (isInteractiveMode()) {
            minSteps = getMinSteps();
        } else {
            minSteps = targetSteps;
        }
        final int maxSteps = (int) Math.ceil(volume.getMaxIntersectionLength() / volume.getMinIntersectionLength() * minSteps);

        // q = sample on a line through the origin of the volume data
        double[] q = new double[3];
        double[] ts = new double[2]; // intersection points with bounding box

        double[] dq = VectorMath.getCopy(viewVec);
        double dv = (double) (volume.getMinIntersectionLength()) / (minSteps + 1);
        VectorMath.setScale(dq, dv);
        
        for (int j = 0; j < image.getHeight(); j++) {
            for (int i = 0; i < image.getWidth(); i++) {
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

                float maxVoxel = 0;

                for (int k = 0; k < steps + 1; k++) {
                    float voxel = getVoxel(q[0], q[1], q[2]);
                    if(voxel > maxVoxel){
                        maxVoxel = voxel;
                    }

                    VectorMath.setAddVector(q, dq);
                }

                setPixel(tFunc.getColor((int)maxVoxel), i, j);

                if (i % 100 == 0 && j == i) {
                    System.out.printf("i=%d, j=%d, steps=%d\n", i, j, steps);
                }
            }
        }
    }
    
    private void setPixel(int i, int j, float val) {
        setPixel(tFunc.getColor(Math.round(val)), i, j);
    }

    private void setPixel(TFColor color, int i, int j) {
        int c_alpha = color.a <= 1.0 ? (int) Math.floor(color.a * 255) : 255;
        int c_red = color.r <= 1.0 ? (int) Math.floor(color.r * 255) : 255;
        int c_green = color.g <= 1.0 ? (int) Math.floor(color.g * 255) : 255;
        int c_blue = color.b <= 1.0 ? (int) Math.floor(color.b * 255) : 255;
        int pixelColor = (c_alpha << 24) | (c_red << 16) | (c_green << 8) | c_blue;
        image.setRGB(i, j, pixelColor);
    }
    
    private void compositing(double[] viewVec, double[] vVec, double[] uVec){
        // image
        final int imageCenter = image.getWidth() / 2;
        
        // volume
        final double[] volumeCenter = volume.getCenter();

        final int minSteps;
        if (isInteractiveMode()) {
            minSteps = getMinSteps();
        } else {
            minSteps = targetSteps;
        }
        final int maxSteps = (int) Math.ceil(volume.getMaxIntersectionLength() / volume.getMinIntersectionLength() * minSteps);

        // q = sample on a line through the origin of the volume data
        double[] q = new double[3];
        double[] ts = new double[2]; // intersection points with bounding box
        
        double[] dq = VectorMath.getCopy(viewVec);
        double dv = (double)(volume.getMinIntersectionLength()) / (minSteps + 1);
        VectorMath.setScale(dq, dv);

        for (int j = 0; j < image.getHeight(); j++) {
            for (int i = 0; i < image.getWidth(); i++) {
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
                
                double r = 0, g = 0, b = 0;
                double cumAlpha = 1;
                
                for (int k = 0; k < steps + 1; k++) {
                    TFColor sampledC = tFunc.getColor((int) getVoxel(q[0], q[1], q[2]));
                    r += sampledC.r * sampledC.a * cumAlpha;
                    g += sampledC.g * sampledC.a * cumAlpha;
                    b += sampledC.b * sampledC.a * cumAlpha;
                    cumAlpha = cumAlpha * (1 - sampledC.a);
                    
                    VectorMath.setAddVector(q, dq);
                }
                
                setPixel(new TFColor(r, g, b, 1), i, j);
                
                if(i % 100 == 0 && j == i){
                    System.out.printf("i=%d, j=%d, steps=%d\n", i, j, steps);
                }
            }
        }
    }
    
    private void tf2d(double[] viewVec, double[] vVec, double[] uVec) {
        
        
        
        // image
        final int imageCenter = image.getWidth() / 2;

        // volume
        final double[] volumeCenter = volume.getCenter();

        final int minSteps;
        if (isInteractiveMode()) {
            minSteps = getMinSteps();
        } else {
            minSteps = targetSteps;
        }
        final int maxSteps = (int) Math.ceil(volume.getMaxIntersectionLength() / volume.getMinIntersectionLength() * minSteps);

        // q = sample on a line through the origin of the volume data
        double[] q = new double[3];
        double[] ts = new double[2]; // intersection points with bounding box

        double[] dq = VectorMath.getCopy(viewVec);
        double dv = (double) (volume.getMinIntersectionLength()) / (minSteps + 1);
        VectorMath.setScale(dq, dv);

        for (int j = 0; j < image.getHeight(); j++) {
            for (int i = 0; i < image.getWidth(); i++) {
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

                double r = 0, g = 0, b = 0;
                double cumAlpha = 1;

                for (int k = 0; k < steps + 1; k++) {
                    TFColor sampledC = tFunc.getColor((int) getVoxel(q[0], q[1], q[2]));
                    r += sampledC.r * sampledC.a * cumAlpha;
                    g += sampledC.g * sampledC.a * cumAlpha;
                    b += sampledC.b * sampledC.a * cumAlpha;
                    cumAlpha = cumAlpha * (1 - sampledC.a);

                    VectorMath.setAddVector(q, dq);
                }

                setPixel(new TFColor(r, g, b, 1), i, j);

                if (i % 100 == 0 && j == i) {
                    System.out.printf("i=%d, j=%d, steps=%d\n", i, j, steps);
                }
            }
        }
    }
}

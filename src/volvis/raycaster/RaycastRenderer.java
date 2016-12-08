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
import util.VectorMath;
import volume.GradientVolume;
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

    public ValueFunction VAL_FUNC;
    public int targetSteps = 100;

    int getMinSteps() {
        int s = Math.min(targetSteps, (int) (10.0 / panel.getSpeed() * targetSteps));
        System.out.println("s = " + s);
        return s;
    }

    public enum ValueFunction {
        TRI_LINEAR,
        ROUND_DOWN,
        NEAREST
    }

    public RaycastRenderer() {
        panel = new RaycastRendererPanel(this);
        VAL_FUNC = ValueFunction.ROUND_DOWN;
        last = this;
        rendererClass = new CenterSlicer();
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

        System.out.println("Finished initialization of " + toString());
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
        if (volume == null || rendererClass == null) {
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

        rendererClass.render(viewVec, uVec, vVec);
    }

    public float getVoxel(double x, double y, double z) throws AssertionError {
        float val;
        switch (VAL_FUNC) {
            case TRI_LINEAR:
                val = volume.getTriVoxel(x, y, z);
                break;
            case ROUND_DOWN:
                val = volume.getFloorVoxel(x, y, z);
                break;
            case NEAREST:
                val = volume.getNNVoxel((float) x, (float) y, (float) z);
                break;
            default:
                throw new AssertionError(VAL_FUNC.name());
        }
        return val;
    }

    void setPixel(int i, int j, float val) {
        setPixel(tFunc.getColor(Math.round(val)), i, j);
    }

    void setPixel(TFColor color, int i, int j) {
        int c_alpha = color.a <= 1.0 ? (int) Math.floor(color.a * 255) : 255;
        int c_red = color.r <= 1.0 ? (int) Math.floor(color.r * 255) : 255;
        int c_green = color.g <= 1.0 ? (int) Math.floor(color.g * 255) : 255;
        int c_blue = color.b <= 1.0 ? (int) Math.floor(color.b * 255) : 255;
        int pixelColor = (c_alpha << 24) | (c_red << 16) | (c_green << 8) | c_blue;
        image.setRGB(i, j, pixelColor);
    }

    RendererClass rendererClass;

    public void setRendererClass(RendererClass rendererClass) {
        this.rendererClass = rendererClass;
        changed();
    }
    
    private static RaycastRenderer last = null;

    static public abstract class RendererClass {

        RaycastRenderer data;

        public RendererClass() {
            this.data = getLast();
        }

        protected abstract void render(double[] viewVec, double[] uVec, double[] vVec);
    }

    /**
     * @return the last
     */
    public static RaycastRenderer getLast() {
        return last;
    }
}

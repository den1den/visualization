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
import gui.RaycastRendererPanel.ValueFunction;
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

    public RendererClass getDefault() {
        return new Compositing(this);
    }

    protected Volume volume = null;
    protected TransferFunction tFunc = null;
    protected GradientVolume gradients = null;
    protected TransferFunctionEditor tfEditor = null;
    protected TransferFunction2DEditor tfEditor2D = null;
    protected final RaycastRendererPanel options;
    private BufferedImage full_image;
    private BufferedImage fast_image;
    protected double[] viewMatrix = new double[4 * 4];
    
    ValueFunction valueFunction;
    int steps;
    boolean shading;
    RendererClass rendererClass = null;
    private final float FAST_IMAGE_SCALE = 1.5f;

    public RaycastRenderer(RaycastRendererPanel options) {
        this.options = options;
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
        full_image = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_ARGB);
        int fastImageSize = (int) (imageSize / FAST_IMAGE_SCALE);
        if (fastImageSize % 2 != 0) {
            fastImageSize = fastImageSize + 1;
        }
        fast_image = new BufferedImage(fastImageSize, fastImageSize, BufferedImage.TYPE_INT_ARGB);

        // create a standard TF of the dataset.
        // This maps an intensity value to some color value
        tFunc = new TransferFunction(volume.getMinimum(), volume.getMaximum());

        // Link the editor to our tFunc
        tFunc.addTFChangeListener(this);
//        double[] logHistogram = volume.getScaledHistogram();
        double[] logHistogram = volume.getScaledHistogram();
        tfEditor = new TransferFunctionEditor(tFunc, logHistogram);

        tfEditor2D = new TransferFunction2DEditor(volume, gradients);
        tfEditor2D.addTFChangeListener(this);

        options.resetTimings();
        
        System.out.println("Finished initialization of " + toString());
    }

    public TransferFunction2DEditor getTF2DPanel() {
        return tfEditor2D;
    }

    public TransferFunctionEditor getTFPanel() {
        return tfEditor;
    }

    public GradientVolume getGradients() {
        return gradients;
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
        double lastCalcImageTime = (endTime - startTime);
        options.setLastImageCalcTime(lastCalcImageTime);

        BufferedImage image = getImage();
        Texture texture = AWTTextureIO.newTexture(gl.getGLProfile(), image, false);

        gl.glPushAttrib(GL2.GL_LIGHTING_BIT);
        gl.glDisable(GL2.GL_LIGHTING);
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

        // draw rendered image as a billboard texture
        texture.enable(gl);
        texture.bind(gl);
        double halfWidth = image.getWidth() / 2.0;
        if(isInteractiveMode()){
            halfWidth *= FAST_IMAGE_SCALE;
        }
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

        options.setLastVisualizeTime(System.currentTimeMillis() - startTime, steps);
    }

    protected double phongKs;
    protected double phongKd;
    protected double phongKa;
    protected double phongAlpha;
    
    /**
     * Sets the RGB values in this.image on update
     *
     * @param viewMatrix the current view orientation
     */
    private void calcImage() {
        if (isInteractiveMode()) {
            steps = options.getEstSteps();
            valueFunction = ValueFunction.fastest();
            shading = false;
        } else {
            steps = options.getMaxSteps();
            valueFunction = options.getValueFunction();
            shading = options.isShading();
            if(shading){
                phongKs = options.getPhongKs();
                phongKd = options.getPhongKd();
                phongKa = options.getPhongKa();
                phongAlpha = options.getPhongAlpha();
            }
        }

        // clear image
        BufferedImage image = getImage();
        for (int j = 0; j < image.getHeight(); j++) {
            for (int i = 0; i < image.getWidth(); i++) {
                image.setRGB(i, j, 0);
            }
        }

        /**
         * Vector in the direction of viewing
         */
        double[] viewVec = VectorMath.newVector(viewMatrix[2], viewMatrix[6], viewMatrix[10]);
        assert VectorMath.getLength(viewVec) == 1;
        /**
         * Vector to the right side of the screen
         */
        double[] uVec = VectorMath.newVector(viewMatrix[0], viewMatrix[4], viewMatrix[8]);
        /**
         * Vector in the upwards direction of the screen
         */
        double[] vVec = VectorMath.newVector(viewMatrix[1], viewMatrix[5], viewMatrix[9]);
        
        if(isInteractiveMode()){
            VectorMath.setScale(uVec, FAST_IMAGE_SCALE);
            VectorMath.setScale(vVec, FAST_IMAGE_SCALE);
        }
        
        options.setActualStepsToTake(steps);
        rendererClass.render(viewVec, uVec, vVec);
    }

    void getPosition(int[] result, double x, double y, double z){
        int xI, yI, zI;
        switch (valueFunction) {
            case TRI_LINEAR:
            case NEAREST:
                xI = (int) Math.round(x);
                yI = (int) Math.round(y);
                zI = (int) Math.round(z);
                break;
            case ROUND_DOWN:
                xI = (int) x;
                yI = (int) y;
                zI = (int) z;
                break;
            default:
                throw new AssertionError(valueFunction.name());
        }
        result[0] = xI;
        result[1] = yI;
        result[2] = zI;
    }
    
    public float getVoxelValue(double x, double y, double z) throws AssertionError {
        switch (valueFunction) {
            case TRI_LINEAR:
                return volume.getTriVoxel(x, y, z);
            case ROUND_DOWN:
                return volume.getFloorVoxel(x, y, z);
            case NEAREST:
                return volume.getNNVoxel((float) x, (float) y, (float) z);
            default:
                throw new AssertionError(valueFunction.name());
        }
    }

    TFColor getTFColor(double x, double y, double z) {
        float voxelValue = getVoxelValue(x, y, z);
        return tFunc.getColor((int) voxelValue);
    }

    static void setPixel(BufferedImage image, int i, int j, double a, double r, double g, double b) {
        int p = getPixel(a, r, g, b);
        image.setRGB(i, j, p);
    }

    static void setPixel(BufferedImage image, int i, int j, TFColor color) {
        int p = getPixel(color.a, color.r, color.g, color.b);
        image.setRGB(i, j, p);
    }

    TransferFunction getTF() {
        return tFunc;
    }

    static int getPixel(TFColor color) {
        return getPixel(color.a, color.r, color.g, color.b);
    }

    static int getPixel(double a, double r, double g, double b) {
        int c_alpha = a <= 1.0 ? (int) Math.floor(a * 255) : 255;
        int c_red = r <= 1.0 ? (int) Math.floor(r * 255) : 255;
        int c_green = g <= 1.0 ? (int) Math.floor(g * 255) : 255;
        int c_blue = b <= 1.0 ? (int) Math.floor(b * 255) : 255;
        return (c_alpha << 24) | (c_red << 16) | (c_green << 8) | c_blue;
    }

    public void setRendererClass(RendererClass rendererClass) {
        this.rendererClass = rendererClass;
        options.resetTimings();
        changed();
    }

    public TransferFunction getTFunction() {
        return tFunc;
    }

    BufferedImage getImage() {
        if(isInteractiveMode()){
            return fast_image;
        } else {
            return full_image;
        }
    }

    Volume getVolume() {
        return volume;
    }

    int getApparentHeight() {
        return full_image.getHeight();
    }

    int getApparentWidth() {
        return full_image.getWidth();
    }

    TransferFunction2DEditor.TriangleWidget getIsoContourTriangle() {
        return tfEditor2D.triangleWidget;
    }

    /**
     * Inner class but then for seperate files
     */
    static public abstract class RendererClass {

        final protected RaycastRenderer r;

        public RendererClass(RaycastRenderer r) {
            this.r = r;
        }

        protected abstract void render(double[] view, double[] uVec, double[] vVec);
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package volvis;

/**
 * RGB color with alpha value
 * @author michel
 */
public class TFColor {

    static TFColor interpolate(TFColor a, TFColor b, double alpha) {
        return new TFColor(
                a.r * alpha + b.r * (1-alpha),
                a.g * alpha + b.g * (1 - alpha),
                a.b * alpha + b.b * (1 - alpha),
                a.a * alpha + b.a * (1 - alpha)
        );
    }
    public double r, g, b, a;

    public TFColor() {
        r = g = b = a = 1.0;
    }
    
    public TFColor(double red, double green, double blue, double alpha) {
        r = red;
        g = green;
        b = blue;
        a = alpha;
    }
    
    @Override
    public String toString() {
        String text = "(" + r + ", " + g + ", " + b + ", " + a + ")";
        return text;
    }
}

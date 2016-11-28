/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package volvis;

import java.awt.Color;
import java.util.ArrayList;
import util.TFChangeListener;

/**
 *
 * @author michel
 */
public class TransferFunction {

    private ArrayList<TFChangeListener> listeners = new ArrayList<TFChangeListener>();

    /**
     * Simple transfer function between two points
     *
     * @param min domain
     * @param max domain
     */
    public TransferFunction(short min, short max) {
        sMin = min;
        sMax = max;
        sRange = sMax - sMin;
        controlPoints = new ArrayList<ControlPoint>();

        controlPoints.add(new ControlPoint(min, new TFColor(.5, .5, .5, .1)));
        controlPoints.add(new ControlPoint(max, new TFColor(1.0, 1.0, 1.0, 1.0)));

        LUTsize = sRange;
        LUT = new TFColor[LUTsize];

        buildLUT();

    }

    public void addRainbowControllPoints() {
        addControlPoint(0, 1.0, 0.0, 0.0, 0.0);
        addControlPoint(42, 1.0, 1.0, 0.0, 0.8);
        addControlPoint(84, 0.0, 1.0, 0.0, 0.8);
        addControlPoint(126, 0.0, 1.0, 1.0, 0.8);
        addControlPoint(168, 0.0, 0.0, 1.0, 0.8);
        addControlPoint(210, 1.0, 0.0, 1.0, 0.8);
        addControlPoint(255, 1.0, 1.0, 1.0, 1.0);
    }

    public void addDefaultControlPoints(String filename) {
        if (true) {
            //addRainbowControllPoints();
            //return;
        }
        if (filename.equals("orange.fld")) {
            // control points for orange data set
            addControlPoint(0, 0.0, 0.0, 0.0, 0.0);
            addControlPoint(40, 0.0, 0.0, 0.0, 0.0);
            addControlPoint(75, 1.0, 0.666, 0.0, 1.0);
            addControlPoint(103, 0.0, 0.0, 0.0, 0.5);
            addControlPoint(205, 0.0, 0.0, 0.0, 0.0);
        } else if (filename.equals("bonsai.fld")) {
            addControlPoint(0, 0.0, 0.0, 0.0, 0.0);
            addControlPoint(29, 0.0, 1.0, 0.2, 0.08);
            addControlPoint(34, 0.0, 1.0, 0.0, 0.89);
            addControlPoint(61, 0.6, 0.2, 0.0, 0.06);
            addControlPoint(74, 0.5329329329329329, 0.2, 0.0, 0.77);
            addControlPoint(124, 0.47927927927927927, 0.2, 0.0, 0.97);
            addControlPoint(143, 0.47047047047047047, 0.2, 0.0, 0.97);
            addControlPoint(155, 0.4330779759351188, 0.2, 0.0, 0.37);
            addControlPoint(192, 0.4, 0.2, 0.0, 0.0);
            addControlPoint(193, 0.0, 0.2, 0.2, 0.99);
            addControlPoint(253, 0.0, 0.0, 0.0, 1.0);
        } else if (filename.equals("stent8.fld")) {
            addControlPoint(0, 0.0, 0.0, 0.0, 0.0);
            addControlPoint(2, 0.0, 0.0, 0.0, 0.0);
            addControlPoint(29, 0.0, 0.0, 0.0, 0.0);
            addControlPoint(49, 0.8, 0.0, 0.0, 0.31);
            addControlPoint(56, 0.0, 0.0, 0.0, 0.0);
            addControlPoint(64, 0.0, 0.0, 1.0, 0.02);
            addControlPoint(82, 0.6042796197266785, 0.6042796197266785, 0.6042796197266785, 0.0);
            addControlPoint(86, 0.6166161646725248, 0.6166161646725248, 0.6166161646725248, 0.22);
            addControlPoint(169, 0.9328187441472342, 0.9328187441472342, 0.9328187441472342, 0.2);
            addControlPoint(171, 0.9702637864534498, 0.9702637864534498, 0.9702637864534498, 1.0);
            addControlPoint(255, 1.0, 1.0, 1.0, 1.0);
        } else if (filename.equals("backpack8_small.fld")) {
            addControlPoint(0, 0.5, 0.5, 0.5, 0.0);
            addControlPoint(21, 0.6438976377952756, 0.6438976377952756, 0.6438976377952756, 0.04);
            addControlPoint(68, 0.746843849269885, 0.746843849269885, 0.746843849269885, 0.09);
            addControlPoint(71, 0.832267301344561, 0.832267301344561, 0.832267301344561, 0.03);
            addControlPoint(149, 0.9297376505066912, 0.9297376505066912, 0.9297376505066912, 0.0);
            addControlPoint(245, 0.9858124101984665, 0.9858124101984665, 0.9858124101984665, 0.27);
            addControlPoint(254, 1.0, 1.0, 1.0, 1.0);
        } else {
            System.out.println("No default transfer function known for " + filename);
        }
    }

    public int getMinimum() {
        return sMin;
    }

    public int getMaximum() {
        return sMax;
    }

    public void addTFChangeListener(TFChangeListener l) {
        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    public ArrayList<ControlPoint> getControlPoints() {
        return controlPoints;
    }

    public TFColor getColor(int value) {
        return LUT[computeLUTindex(value)];
    }

    public TFColor getColorLinInter(double val) {
        double low = Math.floor(val);
        double high = Math.ceil(val);
        if (low == high) {
            return getColor((int) val);
        }
        TFColor lowC = getColor((int) low);
        TFColor highC = getColor((int) high);
        return TFColor.interpolate(lowC, highC, high - val);
    }

    public int addControlPoint(int value, double alpha) {
        TFColor c = getColor(value);
        return addControlPoint(value, c.r, c.g, c.b, alpha);
    }

    public int addControlPoint(int value) {
        TFColor c = getColor(value);
        return addControlPoint(value, c.r, c.g, c.b, c.a);
    }

    public int addControlPoint(int value, double r, double g, double b, double a) {
        if (value < sMin || value > sMax) {
            return -1;
        }
        a = Math.floor(a * 100) / 100.0;

        ControlPoint cp = new ControlPoint(value, new TFColor(r, g, b, a));
        int idx = 0;
        while (idx < controlPoints.size() && controlPoints.get(idx).compareTo(cp) < 0) {
            idx++;
        }

        if (controlPoints.get(idx).compareTo(cp) == 0) {
            controlPoints.set(idx, cp);
        } else {
            controlPoints.add(idx, cp);
        }

        buildLUT();
        return idx;
    }

    public void removeControlPoint(int idx) {
        controlPoints.remove(idx);
        buildLUT();
    }

    public void updateControlPointScalar(int index, int s) {
        controlPoints.get(index).value = s;
        buildLUT();
    }

    public void updateControlPointAlpha(int index, double alpha) {
        alpha = Math.floor(alpha * 100) / 100.0;
        controlPoints.get(index).color.a = alpha;
        buildLUT();
    }

    public void updateControlPointColor(int idx, Color c) {
        ControlPoint cp = controlPoints.get(idx);
        cp.color.r = c.getRed() / 255.0;
        cp.color.g = c.getGreen() / 255.0;
        cp.color.b = c.getBlue() / 255.0;
        buildLUT();
    }

    public void changed() {
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).changed();
        }
    }

    private int computeLUTindex(int value) {
        int idx = ((LUTsize - 1) * (value - sMin)) / sRange;
        return idx;
    }

    /**
     * Builds an lookup table
     */
    private void buildLUT() {

        for (int i = 1; i < controlPoints.size(); i++) {
            ControlPoint prev = controlPoints.get(i - 1);
            ControlPoint next = controlPoints.get(i);
            //System.out.println(prev.value + " " + prev.color + " -- " + next.value + " " + next.color);
            double range = next.value - prev.value;
            for (int k = prev.value; k <= next.value; k++) {
                double frac = (k - prev.value) / range;
                TFColor newcolor = new TFColor();
                newcolor.r = prev.color.r + frac * (next.color.r - prev.color.r);
                newcolor.g = prev.color.g + frac * (next.color.g - prev.color.g);
                newcolor.b = prev.color.b + frac * (next.color.b - prev.color.b);
                newcolor.a = prev.color.a + frac * (next.color.a - prev.color.a);
                LUT[computeLUTindex(k)] = newcolor;
            }

        }

    }

    /**
     * A point along a line with a color value associated with it
     */
    public class ControlPoint implements Comparable<ControlPoint> {

        public int value;
        public TFColor color;

        public ControlPoint(int v, TFColor c) {
            value = v;
            color = c;
        }

        @Override
        public int compareTo(ControlPoint t) {
            return (value < t.value ? -1 : (value == t.value ? 0 : 1));
        }

        @Override
        public String toString() {
            return new String("(" + value + ") -> " + color.toString());
        }

    }
    private short sMin, sMax;
    private int sRange;
    /**
     * Look up table
     */
    private TFColor[] LUT;
    private int LUTsize = 4095;
    private ArrayList<ControlPoint> controlPoints;

    public class TFPrinter implements TFChangeListener {

        private String filename;

        public TFPrinter(String filename) {
            this.filename = filename;
        }

        @Override
        public void changed() {
            System.out.print("if(filename.equals(\"" + filename + "\")){");
            for (ControlPoint p : controlPoints) {
                System.out.print("addControlPoint(" + p.value + ", " + p.color.r + ", " + p.color.g + ", " + p.color.b + ", " + p.color.a + "); ");
            }
            System.out.println("\n}");
        }

    }
}

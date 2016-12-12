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
//            addRainbowControllPoints();
//            return;
        }
        switch (filename) {
            case "orange.fld":
                addControlPoint(0, 0.0, 0.0, 0.0, 0.0);
                addControlPoint(21, 0.0, 0.0, 0.0, 0.0);
                addControlPoint(38, 0.25, 0.25, 0.25, 0.07);
                addControlPoint(41, 1.0, 0.4, 0.0, 0.24);
                addControlPoint(43, 0.4473684210526316, 0.2979473684210526, 0.0, 0.1);
                addControlPoint(72, 0.4842105263157895, 0.32248421052631576, 0.0, 0.36);
                addControlPoint(78, 1.0, 0.666, 0.0, 0.76);
                addControlPoint(103, 0.0, 0.0, 0.0, 0.41);
                addControlPoint(205, 0.0, 0.0, 0.0, 0.0);
                break;
            case "bonsai.fld":
                addControlPoint(0, 0.0, 0.0, 0.0, 0.0);
                addControlPoint(24, 0.0, 1.0, 0.2, 0.0);
                addControlPoint(30, 0.0, 1.0, 0.4, 0.043);
                addControlPoint(34, 0.075, 0.9, 0.0, 0.0444);
                addControlPoint(36, 0.0, 1.0, 0.2, 0.0);
                addControlPoint(38, 0.1819607843137255, 0.8384313725490196, 0.14666666666666667, 0.043);
                addControlPoint(41, 0.4549019607843137, 0.596078431372549, 0.06666666666666667, 0.0458);
                addControlPoint(45, 0.2788451404421975, 0.6282064794104034, 0.0, 0.0);
                addControlPoint(45, 0.40588903668756515, 0.4141032397052017, 0.0, 0.4908);
                addControlPoint(51, 0.5329329329329329, 0.2, 0.0, 0.157);
                addControlPoint(135, 0.47047047047047047, 0.2, 0.0, 2.0E-4);
                addControlPoint(142, 0.46604924251983076, 0.2, 0.0, 2.0E-4);
                addControlPoint(148, 0.45681158622335094, 0.2, 0.0, 0.0);
                addControlPoint(155, 0.43602685955627135, 0.2, 0.0, 0.0);
                addControlPoint(171, 0.2, 0.2, 0.0, 0.402);
                addControlPoint(253, 0.0, 0.0, 0.0, 0.3872);
                break;
            case "stent8.fld":
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
                break;
            case "backpack8_small.fld":
                addControlPoint(0, 0.5, 0.5, 0.5, 0.0);
                addControlPoint(21, 0.6438976377952756, 0.6438976377952756, 0.6438976377952756, 0.04);
                addControlPoint(68, 0.746843849269885, 0.746843849269885, 0.746843849269885, 0.09);
                addControlPoint(71, 0.832267301344561, 0.832267301344561, 0.832267301344561, 0.03);
                addControlPoint(149, 0.9297376505066912, 0.9297376505066912, 0.9297376505066912, 0.0);
                addControlPoint(245, 0.9858124101984665, 0.9858124101984665, 0.9858124101984665, 0.27);
                addControlPoint(254, 1.0, 1.0, 1.0, 1.0);
                break;
            case "tooth.fld":
                addControlPoint(0, 0.0, 0.0, 0.0, 0.0);
                addControlPoint(440, 0.0, 0.0, 0.0, 0.0);
                addControlPoint(494, 0.0, 0.0, 1.0, 0.0148);
                addControlPoint(519, 0.0, 0.0, 1.0, 0.0276);
                addControlPoint(541, 1.0, 1.0, 1.0, 0.0);
                addControlPoint(673, 1.0, 1.0, 1.0, 0.0);
                addControlPoint(711, 0.8113207547169812, 0.5283018867924528, 0.6226415094339622, 0.1236);
                addControlPoint(746, 0.6, 0.0, 0.2, 0.129);
                addControlPoint(777, 0.6752941176470588, 0.18823529411764706, 0.35058823529411764, 0.3168);
                addControlPoint(786, 0.7151702786377709, 0.28792569659442724, 0.43034055727554177, 0.0);
                addControlPoint(812, 0.7928511117365606, 0.4821277793414016, 0.5857022234731213, 0.0092);
                addControlPoint(846, 0.8806260643905603, 0.7015651609764009, 0.7612521287811207, 0.0092);
                addControlPoint(871, 1.0, 1.0, 1.0, 0.0);
                addControlPoint(922, 0.875, 0.875, 0.975, 0.0);
                addControlPoint(1082, 0.8067408781694496, 0.8067408781694496, 0.96134817563389, 0.0);
                addControlPoint(1167, 0.933172079367006, 0.933172079367006, 0.9866344158734012, 0.0342);
                addControlPoint(1211, 0.8693467336683417, 0.8693467336683417, 0.8693467336683417, 0.056);
                addControlPoint(1265, 0.9233122132401136, 0.9233122132401136, 0.9233122132401136, 0.0114);
                addControlPoint(1300, 1.0, 1.0, 1.0, 0.0328);
                break;
                //caveties?: case "tooth.fld": addControlPoint(0, 0.0, 0.0, 0.0, 0.0); addControlPoint(440, 0.0, 0.0, 0.0, 0.0); addControlPoint(541, 1.0, 1.0, 1.0, 0.0); addControlPoint(906, 1.0, 0.6, 0.6, 0.01); addControlPoint(1117, 1.0, 0.8, 0.8, 0.024); addControlPoint(1142, 0.8, 0.8, 0.8, 0.04); addControlPoint(1155, 1.0, 1.0, 1.0, 0.4296); addControlPoint(1189, 1.0, 1.0, 1.0, 0.4346); addControlPoint(1199, 1.0, 1.0, 1.0, 0.0); addControlPoint(1208, 0.8, 0.8, 0.0, 0.8642); addControlPoint(1237, 0.6, 0.8, 0.0, 0.8642); addControlPoint(1300, 1.0, 1.0, 1.0, 0.0); break;
            case "pig8.fld":
                addControlPoint(0, 0.5, 0.5, 0.5, 0.0);
                addControlPoint(34, 0.5001183602265802, 0.5001183602265802, 0.5001183602265802, 0.0);
                addControlPoint(36, 0.5750887701699352, 0.4250887701699351, 0.37508877016993514, 0.0236);
                addControlPoint(38, 0.8, 0.2, 0.0, 0.4096);
                addControlPoint(46, 0.0, 0.0, 0.0, 0.014);
                addControlPoint(48, 0.4, 0.2, 0.0, 0.2556);
                addControlPoint(53, 0.5025375236403082, 0.5025375236403082, 0.5025375236403082, 0.0);
                addControlPoint(79, 0.5030013815480944, 0.5030013815480944, 0.5030013815480944, 0.0);
                addControlPoint(85, 1.0, 1.0, 0.0, 0.1172);
                addControlPoint(94, 0.503921568627451, 0.503921568627451, 0.503921568627451, 0.0);
                addControlPoint(104, 0.5515450980392157, 0.47474509803921566, 0.5323450980392157, 0.1328);
                addControlPoint(119, 0.6031372549019608, 0.44313725490196076, 0.5631372549019608, 0.0);
                addControlPoint(121, 0.929698599439776, 0.2430700280112045, 0.758041456582633, 0.1114);
                addControlPoint(134, 0.9841254901960784, 0.20972549019607845, 0.7905254901960784, 0.2064);
                addControlPoint(152, 1.0, 0.2, 0.8, 0.1142);
                addControlPoint(172, 0.6134516848077813, 0.6134516848077813, 0.5884516848077813, 0.0);
                addControlPoint(255, 1.0, 1.0, 1.0, 0.0);
                break;
            default:
                System.out.println("No default transfer function known for " + filename);
                break;
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
        a = Math.floor(a * ALPHA_PRECISION) / ALPHA_PRECISION;

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
    }

    public void updateControlPointAlpha(int index, double alpha) {
        controlPoints.get(index).color.a = alpha;
    }

    public void updateControlPointScaledAlpha(int index, double sAlpha) {
        controlPoints.get(index).setScaled(sAlpha);
    }

    public void updateControlPointColor(int idx, Color c) {
        ControlPoint cp = controlPoints.get(idx);
        cp.color.r = c.getRed() / 255.0;
        cp.color.g = c.getGreen() / 255.0;
        cp.color.b = c.getBlue() / 255.0;
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
    public void buildLUT() {

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

    public static final double SCALE_BASE = 2;
    public static final double ALPHA_PRECISION = 5000;
    public static final String ALPHA_FORMAT = "%." + (int) Math.ceil(Math.log10(ALPHA_PRECISION)) + "f";

    /**
     * A point along a line with a color value associated with it
     */
    public class ControlPoint implements Comparable<ControlPoint> {

        public int value;
        private TFColor color;

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
            return "(" + value + ") -> " + color.toString();
        }

        public double getScaled() {
            double val = Math.pow(color.a, 1 / SCALE_BASE);
            return val;
        }

        public void setScaled(double alpha) {
            double val = Math.pow(alpha, SCALE_BASE);
            val = Math.floor(val * ALPHA_PRECISION) / ALPHA_PRECISION;
            color.a = val;
        }

        public TFColor getColor() {
            return color;
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

        final boolean LATEX = true;

        @Override
        public void changed() {
            System.out.print("case \"" + filename + "\": ");
            for (ControlPoint p : controlPoints) {
                System.out.print("addControlPoint(" + p.value + ", " + p.color.r + ", " + p.color.g + ", " + p.color.b + ", " + p.color.a + "); ");
            }
            System.out.println("\nbreak;");
            if (LATEX) {
                System.out.println("\n\n\\begin{table}\n"
                        + "\\centering "
                        + "\\begin{tabular}{|r|rrrr|} "
                        + "\\hline \n"
                        + "\\multicolumn{1}{|c|}{Value} & \\multicolumn{1}{c}{Red} & \\multicolumn{1}{c}{Green} & \\multicolumn{1}{c}{Blue} & \\multicolumn{1}{c|}{$\\alpha$} \\\\ \\hline");
                for (ControlPoint p : controlPoints) {
                    System.out.printf("$%d$ & $%.2f$ & $%.2f$ & $%.2f$ & $%.2f$ \\\\ \\hline\n",
                            p.value, p.color.r, p.color.g, p.color.b, p.color.a);
                }
                System.out.printf("\\end{tabular}"
                        + "\\caption{Transfer function " + filename + "}"
                        + "\\label{tab:tf-" + filename + "}"
                        + "\\end{table}\n");
            }
        }

    }
}

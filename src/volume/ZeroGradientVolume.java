/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package volume;

/**
 * 
 * @author michel
 */
public class ZeroGradientVolume extends GradientVolume {

    public ZeroGradientVolume(Volume vol) {
        super(vol);
    }

    @Override
    protected void compute() {
        // this just initializes all gradients to the vector (0,0,0)
        for (int i=0; i<data.length; i++) {
            data[i] = zero;
        }
    }
    
}

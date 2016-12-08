/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package volume;

import volume.GradientVolume;
import volume.Volume;
import volume.VoxelGradient;

/**
 *
 * @author dennis
 */
public class ApxGradientVolume extends GradientVolume {

    /**
     *
     * @param vol
     */
    public ApxGradientVolume(Volume vol) {
        super(vol);
    }

    @Override
    protected void compute() {
        // this just initializes all gradients to the vector (0,0,0)
        for (int z = 0; z < super.dimZ; z++) {
            for (int y = 0; y < super.dimY; y++) {
                for (int x = 0; x < super.dimX; x++) {
                    float gx, gy, gz;

                    if(x==0){
                        gx = (float) (super.volume.getVoxelSafe(x + 1, y, z) - super.volume.getVoxelSafe(x, y, z)) / 2;
                    } else if (x == super.dimX - 1){
                        gx = (float) (super.volume.getVoxelSafe(x, y, z) - super.volume.getVoxelSafe(x - 1, y, z)) / 2;
                    } else {
                        gx = (float) (super.volume.getVoxelSafe(x + 1, y, z) - super.volume.getVoxelSafe(x - 1, y, z)) / 2;
                    }
                    if(y==0){
                        gy = (float) (super.volume.getVoxelSafe(x, y + 1, z) - super.volume.getVoxelSafe(x, y, z)) / 2;
                    } else if (y == super.dimY - 1){
                        gy = (float) (super.volume.getVoxelSafe(x, y, z) - super.volume.getVoxelSafe(x, y - 1, z)) / 2;
                    } else {
                        gy = (float) (super.volume.getVoxelSafe(x, y + 1, z) - super.volume.getVoxelSafe(x, y - 1, z)) / 2;
                    }
                    if(z==0){
                        gz = (float) (super.volume.getVoxelSafe(x, y, z + 1) - super.volume.getVoxelSafe(x, y, z)) / 2;
                    } else if (z == super.dimZ - 1){
                        gz = (float) (super.volume.getVoxelSafe(x, y, z) - super.volume.getVoxelSafe(x, y, z - 1)) / 2;
                    } else {
                        gz = (float) (super.volume.getVoxelSafe(x, y, z + 1) - super.volume.getVoxelSafe(x, y, z - 1)) / 2;
                    }

                    VoxelGradient v = new VoxelGradient(gx, gy, gz);
                    
                    int i = x + dimX * (y + dimY * z);
                    
                    this.data[i] = v;
                }
            }
        }
    }

}

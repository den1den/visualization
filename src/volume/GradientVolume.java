/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package volume;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dennis
 */
public abstract class GradientVolume {
    
    protected int dimX;
    protected int dimY;
    protected int dimZ;
    private GradientVolumeData data;
    protected Volume volume;
    protected double maxmag;
    
    private static class GradientVolumeData implements Serializable{
        private static final long serialVersionUID = 1053487204699477253L;
        final protected VoxelGradient[] d;

        public GradientVolumeData(int size) {
            this.d = new VoxelGradient[size];
        }

        public GradientVolumeData(VoxelGradient[] data) {
            this.d = data;
        }
    }

    public GradientVolume(Volume vol) {
        volume = vol;
        dimX = vol.getDimX();
        dimY = vol.getDimY();
        dimZ = vol.getDimZ();
        maxmag = -1.0;
        calcData();
    }

    public VoxelGradient getGradient(int x, int y, int z) {
        return data.d[x + dimX * (y + dimY * z)];
    }

    public void setGradient(int x, int y, int z, VoxelGradient value) {
        data.d[x + dimX * (y + dimY * z)] = value;
    }

    public void setVoxel(int i, VoxelGradient value) {
        data.d[i] = value;
    }

    public VoxelGradient getVoxel(int i) {
        return data.d[i];
    }

    public int getDimX() {
        return dimX;
    }

    public int getDimY() {
        return dimY;
    }

    public int getDimZ() {
        return dimZ;
    }
    
    private void calcData(){
        data = new GradientVolumeData(dimX * dimY * dimZ);
        compute();
    }

    protected abstract void compute();

    public double getMaxGradientMagnitude() {
        if (maxmag >= 0) {
            return maxmag;
        } else {
            double magnitude = data.d[0].mag;
            for (int i = 0; i < data.d.length; i++) {
                magnitude = data.d[i].mag > magnitude ? data.d[i].mag : magnitude;
            }
            maxmag = magnitude;
            return magnitude;
        }
    }

    private boolean tryCache(Path cache) {
        if(!Files.exists(cache)){
            return false;
        }
        try(ObjectInputStream io = new ObjectInputStream(Files.newInputStream(cache))){
            Object o = io.readObject();
            this.data = (GradientVolumeData) o;
            return true;
        } catch (IOException | ClassNotFoundException | ClassCastException ex) {
            Logger.getLogger(GradientVolume.class.getName()).log(Level.SEVERE, null, ex);
            try {
                Files.deleteIfExists(cache);
            } catch (IOException ex1) {
                // pass
            }
        }
        return false;
    }

    private void writeCache(Path cache) {
        if(Files.exists(cache)){
            return;
        }
        try (ObjectOutputStream io = new ObjectOutputStream(Files.newOutputStream(cache))) {
            io.writeObject(this.data);
            System.out.println("written to cache "+cache);
        } catch (IOException | ClassCastException ex) {
            Logger.getLogger(GradientVolume.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected int getLength() {
        return data.d.length;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package volume;

import junit.framework.TestCase;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import util.VectorMath;

/**
 *
 * @author dennis
 */
public class VolumeTest extends TestCase {
    
    public VolumeTest() {
    }
    
    Volume instance = null;
    
    @Before
    @Override
    public void setUp() {
        instance = new Volume(100, 100, 100);
        for (int x = 0; x < instance.getDimX(); x++) {
            for (int y = 0; y < instance.getDimY(); y++) {
                for (int z = 0; z < instance.getDimZ(); z++) {
                    instance.setVoxel(x, y, z, (short) (Math.random() * Short.MAX_VALUE));
                }
            }
        }
    }

    /**
     * Test of intersect method, of class Volume.
     */
    @Test
    public void testIntersect() {
        System.out.println("intersect");
        double[] p;
        double[] r;
        double[] expResult0, expResult1;
        double[] t;
        
        System.out.println("intersect l:(-1,-1,-1)+x(1,1,1)");
        r = VectorMath.getNormalized(new double[]{1, 1, 1});
        p = new double[]{-1, -1, -1};
        expResult0 = instance.getMinPos();
        expResult1 = instance.getMaxPos();
        t = instance.intersect(p, r);
        assertArrayEquals(VectorMath.getAddVector(p, t[0], r), expResult0);
        assertArrayEquals(VectorMath.getAddVector(p, t[1], r), expResult1);
        
        
    }
    
    void assertArrayEquals(double[] arr, double[] arr2){
        for (int i = 0; i < arr.length; i++) {
            assertEquals(arr[i], arr2[i]);
        }
    }
    
}

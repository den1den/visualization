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
    public void setUp() {
        instance = new Volume(10, 10, 10);
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                for (int z = 0; z < 10; z++) {
                    instance.setVoxel(x, y, z, (short) (Math.random() * Short.MAX_VALUE));
                }
            }
        }
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of intersect method, of class Volume.
     */
    @Test
    public void testIntersect() {
        System.out.println("intersect");
        double[] p = instance.getCenter();
        double[] dir = new double[]{0, 1, 0};
        double[] expResult = new double[]{p[0], instance.getDimY(), p[1]};
        double[] result = instance.intersect_inside(dir);
        assertArrayEquals(expResult, result);
        
        dir = VectorMath.norm(new double[]{1, 1, 1});
        expResult = instance.getDim();
        result = instance.intersect_inside(dir);
        assertArrayEquals(expResult, result);
        
        dir = VectorMath.norm(new double[]{-1, -1, -1});
        expResult = VectorMath.zero();
        result = instance.intersect_inside(dir);
        assertArrayEquals(expResult, result);
    }
    
    void assertArrayEquals(double[] arr, double[] arr2){
        for (int i = 0; i < arr.length; i++) {
            assertEquals(arr[i], arr2[i]);
        }
    }
    
}

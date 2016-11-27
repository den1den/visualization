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
        double[] p;
        double[] r;
        double[] expResult;
        double[] result;
        
        r = new double[]{1, 0, 0};
        p = new double[]{-10, 0, 0};
        expResult = new double[]{0, 0, 0};
        result = instance.intersect(p, r);
        VectorMath.setAddVector(p, result[0], r);
        assertArrayEquals(p, expResult);
        
        r = new double[]{2, 5, 1};
        p = new double[]{0, -.1, 0};
        result = instance.intersect(p, r);
        VectorMath.setAddVector(p, result[0], r);
        assertEquals(0, p[0]);
    }
    
    void assertArrayEquals(double[] arr, double[] arr2){
        for (int i = 0; i < arr.length; i++) {
            assertEquals(arr[i], arr2[i]);
        }
    }
    
}

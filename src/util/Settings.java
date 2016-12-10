package util;


import java.io.File;
import java.nio.file.FileSystems;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Dennis
 */
public class Settings {
    //public static File defaultFile = new File("/home/dennis/repos/Visualization/set1_data/orange.fld");
    public static final File set1_data = FileSystems.getDefault().getPath("set1_data").toFile();
    public static final File defaultFile = new File(set1_data, "orange.fld");
    
}

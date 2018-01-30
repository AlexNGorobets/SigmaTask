/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import java.nio.file.*;
import sigmatask.TextManager;
/**
 *
 * @author Liolik
 */
public class TextManagerTest {
    
    public TextManagerTest() {
    }
    
    private static File dir;
    
    @BeforeClass
    public static void setUpClass() throws IOException{
        
        dir = Files.createTempDirectory(null).toFile();
    }
    
    @AfterClass
    public static void tearDownClass() {
        
        if (dir == null) { 
            return; 
        } 
        dir.delete(); 
        System.out.println(dir.exists()); 
        
    }

    /**
     * Test of hDDLoadTxt method, of class TextManager.
     */
    @Test
    public void testHDDLoadTxt_String() throws Exception {
        
        FileOutputStream f = null;
        String str;
        int[] expected = {1, 3, 5, 7, 11, 13, 17, 19};
        try{
            f = new FileOutputStream(dir.getAbsolutePath()+"readTesting.txt");
            for (int i : expected){
                f.write(i);
            }
        } finally {
            if (f != null) {
            f.close();
            }
        }
            str = TextManager.hDDLoadTxt(dir.getAbsolutePath()+"readTesting.txt");
            int[] actual = new int [str.length()];
            for (int i=0; i<str.length(); i++) {
                actual[i]=str.codePointAt(i);
            }
            assertArrayEquals(expected, actual);
    }
  
}

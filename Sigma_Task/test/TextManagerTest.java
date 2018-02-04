/*
 * This unit developed as Unit Tests for testing task for Sigma Software.
 */

import java.io.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import java.nio.file.*;
import sigmatask.TextManager;

/**
 * @author Alex N Gorobets
 */
public class TextManagerTest {
    
    private static File dir;
    
    @BeforeClass
    public static void setUpClass() throws IOException{
        
        /*Making temporary dir*/
        dir = Files.createTempDirectory(null).toFile();
    }
    
    @AfterClass
    public static void tearDownClass() {
        
        /*Removing temporary dir*/
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
        int[] expected = {1, 2, 3, 5, 7, 11, 13, 17, 19};
        int[] actual;
        
        /* Writing temporary file */
        try{
            f = new FileOutputStream(dir.getAbsolutePath() + "readTesting.txt");
            for (int i : expected){
                f.write(i);
            }
        } finally {
            if (f != null) {
            f.close();
            }
        }
        /* Reading file testing */
        str = TextManager.hDDLoadTxt(dir.getAbsolutePath() + "readTesting.txt");
        actual = new int [str.length()];
        for (int i = 0; i < str.length(); i++) {
            actual[i] = str.codePointAt(i);
        }
        assertArrayEquals(expected, actual);
    }

    /**
     * Test of MakeItAsDictionary method, of class TextManager.
     */
    @Test
    public void testMakeItAsDictionary() {
        
        String srsTxt = "I follow the Moskva. Down to Gorky Park"
                      + " listening to the wind of change";
        
        String expected = "I\nfollow\nthe\nMoskva\nDown\nto\nGorky\nPark\n"
                + "listening\nto\nthe\nwind\nof\nchange";
        
        /* Delimiter of made dictionary will be "\n" */
        String actual = TextManager.MakeItAsDictionary(srsTxt, "[\\s.]", "\n" );
        assertEquals( expected, actual);
    }

    /**
     * Test of hDDLoadTxt method, of class TextManager.
     */
    @Test
    public void testHDDLoadTxt_String_String() throws Exception {
                FileOutputStream f = null;
        String str;
        int[] expected = {1, 2, 3, 5, 7, 11, 13, 17, 19};
        int[] actual;
        
        /* Writing temporary file */
        try{
            f = new FileOutputStream(dir.getAbsolutePath() + "readTesting.txt");
            for (int i : expected){
                f.write(i);
            }
        } finally {
            if (f != null) {
            f.close();
            }
        }
        /* Reading file testing with defined encoding */
        str = TextManager.hDDLoadTxt(dir.getAbsolutePath() + 
                "readTesting.txt", "cp1251");
        actual = new int [str.length()];
        for (int i = 0; i < str.length(); i++) {
            actual[i] = str.codePointAt(i);
        }
        assertArrayEquals(expected, actual);
    }

    /**
     * Test of libRu_ParseHTML method, of class TextManager.
     */
    @Test
    public void testLibRu_ParseHTML() {
                
        String html = "<html>"  //lib.ru-like html-example
                    + "<dd>&nbsp;&nbsp;I follow the Moskva. Down to Gorky Park "
                    + "<dd>&nbsp;&nbsp;listening to the wind of chang&#1105;\n"
                    + "</html>";
        String expected = "I follow the Moskva. Down to Gorky Park "
                        + "listening to the wind of changё\n";
        String actual = TextManager.libRu_ParseHTML(html);
        assertEquals( expected, actual);
    }
   
}

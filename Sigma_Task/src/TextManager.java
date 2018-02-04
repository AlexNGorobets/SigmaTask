/*
 * This unit developed by Alexey Gorobets as testing task for Sigma Software 
 * Development. Described class TextManager can be used by SigmaTask.java file 
 * with main method for convinient demonstration of given capabilities, 
 * which described bellow.
 */
package sigmatask;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Described class TextManager consist of few static methods designed to 
 * getting text as String for future using
 */
public class TextManager {
    
    /**
     * Method for downloading a text file from a hard disk.
     */
    public static String hDDLoadTxt(String path) throws IOException{ 
        
        /*initializing stream vars to reading from a file*/
        FileInputStream fileHandler = null;  
        InputStreamReader fReader = null;
        StringBuilder sbText = new StringBuilder();     //Text line
        int i;
        
        /*The main block of working with the stream*/
        try{
            fileHandler = new FileInputStream(path);    //Stream Settings
            fReader= new InputStreamReader(fileHandler);   
            
            /*Getting the text field with the name of the encoding*/
            String Enc = fReader.getEncoding(); 
            while ((i = fReader.read()) != -1) {        //Reading of file
                sbText.append((char) i);
            }         
            System.out.println("File read by using encoding: " + Enc 
                    + "\nTotal chars:\t " + String.valueOf(sbText.length()));
     
        /*Exception Handling Block*/
        } catch (FileNotFoundException e) { //file not found
            System.out.println("Exception:" + e + "\ncheck the Path");
            return "-1";
        } catch (IOException e) {   //Other exceptions (HDD failure for example)
            e.printStackTrace();
            return "-1";
        } finally {         //Guaranteed, closing streams
            if (fReader != null) {
                fReader.close();           
            }
            if (fileHandler != null) {
                fileHandler.close();
            }
        return sbText.toString();
        }    
    }
    /**
     * Overloaded Method for downloading a text file from a hard disk, 
     * with specifying file encoding 
     */
    public static String hDDLoadTxt(String path, 
            String encoding) throws IOException{
    /*Totally, it similar to the previous one*/
    
        /*initializing stream vars to reading from a file*/
        FileInputStream fileHandler = null;
        InputStreamReader fReader = null;
        StringBuilder sbText = new StringBuilder();     //Text line
        int i;
 
        /*The main block of working with the stream*/
        try {
            fileHandler = new FileInputStream(path);    //Stream Settings
            fReader = new InputStreamReader(fileHandler, encoding); 
            String Enc = fReader.getEncoding();
            while ((i = fReader.read())!= -1) {         //Reading of file
                sbText.append((char) i);
            }         
            System.out.println("File read by using encoding: " + Enc 
                    + "\nTotal chars:\t " + String.valueOf(sbText.length()));
            
        /*Exception Handling Block*/
        } catch (FileNotFoundException e) { //file not found
            System.out.println("Exception:" + e + "\ncheck the Path");
            return "-1";
        } catch (IOException e) {           //Other exceptions
            e.printStackTrace();
            return "-1";
        } finally {                  //Guaranteed, closing streams
            if (fReader != null) {
                fReader.close();           
            }
            if (fileHandler != null) {
                fileHandler.close();
            }
        }    
        return sbText.toString();
    }
    
    /**
     * Method URL_LibRuGetHTML return HTML response from website lib.ru. 
     * There is a big set of classic literature where each can be presented at 
     * one HTML page. You have only choose the work and copy URL to this method/
     */
    public static String libRu_GetHTML(String urlValue) throws IOException {
            
        /*initialization of variables*/
        StringBuilder response = new StringBuilder();
        URL urlObj = new URL(urlValue); //URL object
        BufferedReader reader = null;   //The  Reader
            
        try {   /* Attempt to read from urlObj */
            reader = new BufferedReader(
                    new InputStreamReader(urlObj.openStream(), "cp1251"));
                
            /* Loop for reading all lines of the answer */
            for (String line; (line = reader.readLine()) != null;) {
                response.append(line+"\n");
            }
        } catch (IOException e) {  
                e.printStackTrace();    //Printing an exclusion report
        } finally {
            if (reader != null){
                reader.close();   //Closing the thread (in any way)
            }                
        }
        return response.toString(); //HTML returns
    }
    
    public static String libRu_ParseHTML(String libRu_HTML) {
        
        /* Vars start-initializations */
        int i;
        int start = libRu_HTML.indexOf("<dd>&nbsp;&nbsp;");
        int finish = libRu_HTML.lastIndexOf("<dd>&nbsp;&nbsp;");
        finish = libRu_HTML.indexOf("\n", finish) + 1;
        
        /* Getting body of HTML */
        libRu_HTML = libRu_HTML.substring(start, finish);
        /* Removing tags*/
        libRu_HTML = libRu_HTML.replaceAll("<dd>&nbsp;&nbsp;", "");
        libRu_HTML = libRu_HTML.replaceAll("<.+>", "");
        
        /* special-characters replacing 
        (which written by '&#unicode;'-sequences*/
        StringBuilder Text = new StringBuilder(libRu_HTML);
        Pattern p = Pattern.compile("&#(\\d+);"); 
        Matcher m = p.matcher(Text);
        start=0;
        while (m.find(start)) {  //encountered spec.char loop
            start = m.start();
            finish = m.end();
            i= Integer.parseInt(Text.substring(start+2, finish-1));
            Text.replace(start, finish, String.valueOf((char) i));
            start++;
        } 
    return Text.toString();
    }
    
    /**
     * MakeItAsDictionary returns string formatted as dictionary:
     * srsText - text which used to make the dictionary
     * regexFilter - regex for non-alphabetic characters, by with of which in 
     * the source text are defined gaps between words, for example:"[ \\n,.]".
     * If the set of letters used is known in advance, you can specify it using 
     * inversion, for example: "[^а-я^ё^a-z^']". Specification of alphabet 
     * guarantees a precise result.
     * A space and a newline character ("[\n ]") can not be part of the alphabet
     * resDelimiter - separator for the generated dictionary, for example: "\n"
     */
    
    public static String MakeItAsDictionary(String srsText, 
            String regexFilter, String resDelimiter ){
        
        String resDict;
        
        /*replace all unspecified characters with the resDelimiter*/
        resDict = srsText.replaceAll(regexFilter, " ");   
        resDict = resDict.replaceAll("\\s+", resDelimiter);
        
        return resDict;
    }
}

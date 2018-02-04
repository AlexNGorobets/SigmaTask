/*
 * This unit developed by Alexey Gorobets as testing task for 
 * Sigma Software Development. 
 * 
 * Derived subtasks:
    1. Load SourceText
    2. To get Dictionary with it
    3. Remove from text all whitespaces and punctuation
    4. Try to restore whitespaces using the dictionary
 */

package sigmatask;
import java.io.*;

/**
 * Described class SigmaTask consist of main script 
 * for solving task demonstration.
 */
public class SigmaTask {
    
    
    /**
     * Main script of developed research
     */
    public static void main(String[] args) throws IOException{
        
        /*Variables which corresponds to processing text on three stages*/
        String sourceText;
        String unspacedText;    //without any spaces and punctuation
        String resText;         //with restored spaces
        long elapsedTime;       //Time marks
        long startTime;     
        String simpleDict;  //Dictionary as string with delimiters
        String delimiter = "\n";

        String html = TextManager.libRu_GetHTML(
                "http://az.lib.ru/t/tolstoj_lew_nikolaewich/text_0040.shtml");
        sourceText = TextManager.libRu_ParseHTML(html);
        //System.out.print(sourceText);
        
        /* Loading from hardware case*/
        //sourceText=TextManager.hDDLoadTxt("War_nPeace.txt");
       
        /* Creating the dictionary by using the given text */
        /* Simple dictionary at first */
        simpleDict = TextManager.MakeItAsDictionary(sourceText, 
                "[\\s\\n,.:;!?\\[\\]\\(\\)—_/*\\-]", "\n");
         
        /* Tree-dictionary making */
        startTime = System.currentTimeMillis();
        TreeDictionary tDict = new TreeDictionary(simpleDict, delimiter);
        elapsedTime = System.currentTimeMillis() - startTime;
        System.out.print("Elapsed Time :" + elapsedTime + " ms\n");
        //tDict.showDict();    //Printing all words from dictionary
        
        /*Preparing of text without spacing and punctuation*/
        unspacedText = sourceText.replaceAll("[ ,.:;!?—_/]", "");
                
        /*Restoring whitespaces by alphabetical-sorted dictionary*/
        startTime = System.currentTimeMillis();
        resText = tDict.spaceRestore_Fast(unspacedText);  
        elapsedTime = System.currentTimeMillis() - startTime;
        System.out.print("Elapsed Time :" + elapsedTime + " ms\n");
        System.out.println("Restored text:\n"+resText);

    }
}
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
 * Described class SigmaTask consist of main script of solving task demonstration.
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
        long elapsedTime;   //Time marks
        long startTime;     
    
        /*Attention! The correct way to .txt-file should be specified here*/
        sourceText=TextManager.hDDLoadTxt("War_nPeace.txt");
        
        /*Also you can use overloaded method to specify file encoding*/
        //sourceText=TextManager.hDDLoadTxt("War_nPeace1.txt", "unicode");
        //System.out.println("Source text:\n"+sourceText);

        /*Creating the dictionary by using the given text*/
        startTime = System.currentTimeMillis();
        TreeDict_Cyr ru_dct = new TreeDict_Cyr(sourceText);
        elapsedTime = System.currentTimeMillis()-startTime;
        System.out.print("Elapsed Time :"+elapsedTime+" ms\n");

        //ru_dct.showDict();    //Printing all words from dictionary
        
        /*Preparing of text without spacing and punctuation*/
        //unspacedText=sourceText.toLowerCase();    
        //unspacedText=unspacedText.replaceAll("[^а-я^ё^a-z^0-9^]", ""); 
        unspacedText=sourceText.replaceAll("[^А-Я^а-я^ё^Ё^A-Z^a-z^0-9^]", ""); 
        System.out.println("Text without delimiters:\n"+unspacedText);
        
        /*Restoring whitespaces by alphabetical-sorted dictionary*/
        ru_dct.sortByDemand();
        startTime = System.currentTimeMillis();
        resText=ru_dct.spaceRestore_Fast(unspacedText);
        elapsedTime = System.currentTimeMillis()-startTime;
        System.out.print("Elapsed Time :"+elapsedTime+" ms\n");
        //System.out.println("Restored text:\n"+resText);
        
        /*Restoring whitespaces by dictionary with demand sorting*/
        ru_dct.sortByDemand();
        startTime = System.currentTimeMillis();
        resText=ru_dct.spaceRestore_Fast(unspacedText);
        elapsedTime = System.currentTimeMillis()-startTime;
        System.out.print("Elapsed Time :"+elapsedTime+" ms\n");
        //System.out.println("Restored text:\n"+resText);
    
    }
}
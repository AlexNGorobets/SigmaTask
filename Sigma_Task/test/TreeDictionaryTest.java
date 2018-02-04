/*
 * This unit developed as Unit Tests for testing task for Sigma Software.
 */

import org.junit.Test;
import static org.junit.Assert.*;
import sigmatask.TreeDictionary;

/**
 * @author Alex N Gorobets
 */
public class TreeDictionaryTest {

    /**
     * Simple test of constructor
     */
    public TreeDictionaryTest() {
        
        /* Independent pre-testing of constructor */
        String simpleDict = "мама мыла wash мыла раму";
        TreeDictionary D = new TreeDictionary(simpleDict, " ");
        String regexActual = D.getRegexCharsOutOfAlphabet();
        String regexExpected = "[^л-м^a^h^s^w^а^р^у^ы]";
        
        /* Checking main properties */
        int wordsActual = D.getContainedWords();
        int wordsExpected = 4;
        assertEquals(regexExpected, regexActual);
        assertEquals(wordsExpected, wordsActual);
        wordsActual = D.getProcessedWords();
        wordsExpected = 5;
        assertEquals(wordsExpected, wordsActual);
    }   
    
    /**
     * Test of toStringAll method, of class TreeDictionary.
     */
    @Test
    public void testToStringAll() {
    /* Independent testing of simple method, which used in other tests
     */
        
        String simpleDict = "Африка-America-Гватемала-Beirut"
                          + "-Венеция-Montenegro-Бали";
        TreeDictionary D = new TreeDictionary(simpleDict, "-");
        String actual = D.toStringAll();
        String expected = "america\nbeirut\nmontenegro\nафрика\nбали\n"
                        + "венеция\nгватемала\n";
        assertEquals(expected, actual);
    }
    
    /**
     * Test of spaceRestore_Fast method, of class TreeDictionary.
     */
    @Test
    public void testSpaceRestore_Fast() {
    /* There is the most complicated method, 
     * so it tested by all escape characters
     */
    
        String srsText = "[god] helps those, who help \"themselves government"
                       + " - all ^the rest";
        TreeDictionary D = new TreeDictionary(srsText, ",?\\s");
        String unspaced = srsText.replaceAll("[^a-z^\\-]", "");
        String actual = D.spaceRestore_Fast(unspaced);
        String expected = " god helps those who help themselves government"
                        + " - all ^the rest ";
        assertEquals(expected, actual);
    }


    /**
     * Test of sortByDemand method, of class TreeDictionary.
     */
    @Test
    public void testSortByDemand() {
    /* Demand is defined as number of hits to each letter with defined prefix
     * So there will be prefixes in the follows "popularity":
     * 'за'
     * 'зам'
     * 'заман'
     * 'ом'
     */
        String srsText = "омон омлет заболевание зав заманчиво замах заманил";
        TreeDictionary D = new TreeDictionary(srsText, " ");
        D.sortByDemand();
        String actual = D.toStringAll();
        String expected = "заманчиво\nзаманил\nзамах\nзаболевание\n"
                        + "зав\nомон\nомлет\n";
        assertEquals(expected, actual);
    }

    /**
     * Test of sortByAlphabet method, of class TreeDictionary.
     */
    @Test
    public void testSortByAlphabet() {
    
        /* Multi language alphabet means order by unicode */
        String srsText = "борода янтарь авангард guy needs the job";
        TreeDictionary D = new TreeDictionary(srsText, " ");
        D.sortByAlphabet();
        String actual = D.toStringAll();
        String expected = "guy\njob\nneeds\nthe\nавангард\nборода\nянтарь\n";
        assertEquals(expected, actual);
    }
    
}

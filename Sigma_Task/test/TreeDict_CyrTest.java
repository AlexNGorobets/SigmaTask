/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import org.junit.Test;
import static org.junit.Assert.*;
import sigmatask.TreeDict_Cyr;

/**
 *
 * @author Liolik
 */
public class TreeDict_CyrTest {
    
    public TreeDict_CyrTest() {
    }

    /**
     * Test of spaceRestore_Fast method, of class TreeDict_Cyr.
     */
    @Test
    public void testSpaceRestore_Fast() {
        String srsText = "Вроде Ане надо.. Ане-то? надо! А не, не надо Ане";
        TreeDict_Cyr D = new TreeDict_Cyr(srsText);
        String unspaced = srsText.replaceAll("[^А-Я^а-я^ё^Ё^A-Z^a-z^0-9^]", "");
        String actual = D.spaceRestore_Full(unspaced);
        String expected = " Вроде Ане надо Ане то надо Ане не надо Ане ";
        assertEquals(expected, actual);
    }

    /**
     * Test of spaceRestore_Full method, of class TreeDict_Cyr.
     */
    @Test
    public void testSpaceRestore_Full() {
        String srsText = "Небо не болей - воду не лей, во всяком случае по понедельникам";
        TreeDict_Cyr D = new TreeDict_Cyr(srsText);
        String unspaced = srsText.replaceAll("[^А-Я^а-я^ё^Ё^A-Z^a-z^0-9^]", "");
        String actual = D.spaceRestore_Full(unspaced);
        String expected = " Небо не болей воду не лей во всяком случае по понедельникам ";
        assertEquals(expected, actual);
    }

    /**
     * Test of sortByDemand method, of class TreeDict_Cyr.
     */
    @Test
    public void testSortByDemand() {
        
        String srsText="омон омлет заболевание зав заманчиво замах заманил";
        TreeDict_Cyr D = new TreeDict_Cyr(srsText);
        D.sortByDemand();
        String actual = D.toStringAll();
        String expected = "заманчиво\nзаманил\nзамах\nзаболевание\nзав\nомон\nомлет\n";
        assertEquals(expected, actual);

    }

    /**
     * Test of sortByAlphabet method, of class TreeDict_Cyr.
     */
    @Test
    public void testSortByAlphabet() {

        String srsText="борода янтарь авангард рыба суша икра войлок";
        TreeDict_Cyr D = new TreeDict_Cyr(srsText);
        D.sortByAlphabet();
        String actual = D.toStringAll();
        String expected = "авангард\nборода\nвойлок\nикра\nрыба\nсуша\nянтарь\n";
        assertEquals(expected, actual);
    }
    
}

/*
 * This unit developed by Alexey Gorobets as testing task 
 * for Sigma Software Development. Described class TreeDict can be used 
 * by SigmaTask.java file with main method for convinient demonstration 
 * of given capabilities, which described bellow.
 */

package sigmatask;
        
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The TreeDictionary class is a specific way of storing the words.
 * Such a dictionary is created as constructor 
 * is called and can be used by the spaceRestore method for restoring white 
 * spaces in this text.
 */

public class TreeDictionary{
/*
 * Technically this is an array where each element is an object which 
 * corresponds to the letter from dictionary for each case of a certain sequence
 * of preceding letters. The letters without "prehistory" (the first letters of 
 * words) correspond to the first N elements of the TreeDict array, where N is
 * a number of characters in the alphabet.
 * This way, first letters are arranged in the unicode-growing order. 
 * The dictionary does not take into account register of letters.
 * Each element of the array TreeDict (by the first dimension) stores another 
 * array which also called Node. Node is a list of NodeLink objects, 
 * each of which have a link to the next possible letter. The NodeLink object 
 * (private class in this file) contains the symbol of next letter, the number 
 * of Node corresponding to it, and the number of passes through this link 
 * during the dictionary formation.   
 */  
    /** The dictionary */
    private ArrayList<ArrayList<NodeLink>> treeDict; 

    /** the Alphabet characters obtained from srsTxt */
    private String alphabet = "";
    
    /** regular expression to determine out-of-alphabet charcters */
    private String RegexCharsOutOfAlphabet = "[";
    
    /** The number of letters in the dictionary */
    private int dict_Q_Letters = 0;
    
    /** Unicode of beginning character of used character ranges */
    private int[] uRngBeginAt;
    
    /** Unicode of ending character of ranges */
    private int[] uRngEndAt;
    
    /** Alphabet position of beginning character of used character ranges */
    private int[] uncd2Dict_Corr;
    
    /** The number of processed words (which stored in the sourse text) */
    private int processedWords = 0;
    
    /** Total number of stored words in the dictionary */
    private int containedWords = 0;
    
    /**
     * The TreeDictionary constructor recieves any String. Typically srsTxt is
     * a dictionary where words devided by delimiter ("\n" for example).
     * Such dictionary (srsTxt) can consist of duplicate words. 
     * delimiter can be defined  by regex such as "[,.\\s]" in case 
     * where dictionary just is a text.
     * Escape characters '[',']','"' can't be part of alphabet, it should be 
     * removed from dictionary. Otherwise it will be done automatically which 
     * makes possible glued words.
     * Regex escape characters '-','^' can be part of alphabet.
     * The instance of the TreeDictionary class can be used to restore spaces 
     * in the text using the spaceRestore_ methods.
     */
    public TreeDictionary(String srsTxt, String dilimeter){
    /* The dictionary is filled with words without taking into account the 
     * suffixes and endings (any different letter means different words)
     * The criteria of the last letter in the word is the reference to the node 
     * with Char = ' '.
     */
    
    /**Vars for preprocessing of srsTxt*/
    char[] charSrs; //srsTxt as chars
    int[] intSrs;   //srsTxt as unicode int
    ArrayList<int[]> uncdRngs = new ArrayList();
    String metaCh = "^-";

    /**Vars for TreeDictionary making*/
    int i, j;           
    int newAdr_toWrite; //1-st dimension adress to write next Node
    int nextAdr_toFind; //1-st dim adress to search through the 2-nd dimension
    int charExist;  //2-nd dim adress (NodeLinks.address =-1 if it isn't exist)
    char ch;            //The next letter of searched word
    int repeatedNodes = 0;  //The counter of repeated uses of all NodeLinks


    /* Pre-processing of the input text srsTxt */
    srsTxt = srsTxt.toLowerCase();
    srsTxt=srsTxt.replaceAll("[\\[\\]\"]", ""); 
    RegexCharsOutOfAlphabet = srsTxt.replaceAll(dilimeter, "");  
    charSrs = RegexCharsOutOfAlphabet.toCharArray();
    intSrs = new int[charSrs.length];
    for (i = 0; i < charSrs.length; i++) { //Gatting it as unicode numbers array
        intSrs[i] = Character.codePointAt(charSrs, i);
    }
    Arrays.sort(intSrs);    //sorting by ascending  unicode

    /* Defining parameters of used unicode ranges There are: Start_Unicode,
    Finish_Unicode, unicode_Range, alphabet_position of each range */
    uncdRngs.add(new int[4]); 
    uncdRngs.get(0)[0] = intSrs[0];         //First range begins at
    alphabet += (char)intSrs[0];
    for (i = 1 ;i < intSrs.length; i++) {   //character loop
        if (intSrs[i] - intSrs[i-1] > 1) {      //character range defining
            uncdRngs.get(uncdRngs.size() - 1)[1] = intSrs[i - 1];//Range ends at
            uncdRngs.get(uncdRngs.size() - 1)[2] = intSrs[i - 1]
                    -uncdRngs.get(uncdRngs.size() - 1)[0] + 1;   //Range length
            /* The range beginning position in the alphabet: */
            uncdRngs.get(uncdRngs.size() -1 )[3] = dict_Q_Letters;  
            dict_Q_Letters += uncdRngs.get(uncdRngs.size() - 1)[2];
            uncdRngs.add(new int[4]);
            uncdRngs.get(uncdRngs.size() - 1)[0] = intSrs[i];//Next range begins
            alphabet += (char) intSrs[i];
        } else if (intSrs[i] - intSrs[i - 1]==1) {
            alphabet += (char) intSrs[i];   //Alphabet makes in unicode order
        }
    }
    System.out.println("Obtained alphabet: " + alphabet);
    /* the last range: */
    uncdRngs.get(uncdRngs.size() - 1)[1] = intSrs[intSrs.length - 1];
    uncdRngs.get(uncdRngs.size() -1)[2] = intSrs[intSrs.length - 1]
            - uncdRngs.get(uncdRngs.size() - 1)[0] + 1;
    uncdRngs.get(uncdRngs.size() - 1)[3] = dict_Q_Letters;
    /* Is number of characters in the alphabet from now */
    dict_Q_Letters += uncdRngs.get(uncdRngs.size() - 1)[2];   
    
    /* Sorting ranges - the longer is first */
    uncdRngs.sort((int[] one, int[] other) 
            -> Integer.compare(other[2], one[2]));  
 
    uRngBeginAt = new int[uncdRngs.size()]; //Initializing of ranges properties
    uRngEndAt = new int[uncdRngs.size()];
    uncd2Dict_Corr = new int[uncdRngs.size()];
    
    RegexCharsOutOfAlphabet = "[";
    for (i = 0; i < uncdRngs.size(); i++) {       //Ranges loop
        uRngBeginAt[i] = uncdRngs.get(i)[0];      //Copying range parameters 
        uRngEndAt[i] = uncdRngs.get(i)[1];        //to one-dimensional arrays
        uncd2Dict_Corr[i] = uncdRngs.get(i)[3];

        /* Regex for non-alphabet characters obtaining */
        /* First symbol of range*/
        if (metaCh.contains(String.valueOf((char) uRngBeginAt[i]))) {
            RegexCharsOutOfAlphabet += "^\\\\"; 
        } else {    //escaping RegEx metacharacters (if it included by alphabet)
            RegexCharsOutOfAlphabet += "^";
        }  
        RegexCharsOutOfAlphabet += (char) uRngBeginAt[i];   //Regex forming
        /* Second symbol of range*/
        if (uRngBeginAt[i] != uRngEndAt[i]) {
            if (metaCh.contains(String.valueOf((char) uRngEndAt[i]))) {
                RegexCharsOutOfAlphabet += "-\\\\"; //escaping metacharacters
            } else {
            RegexCharsOutOfAlphabet += "-";
        }
            RegexCharsOutOfAlphabet += (char) uRngEndAt[i];
        }
    }
            
    RegexCharsOutOfAlphabet += "]"; //Regex for non-alphabet is ready
    System.out.println("regex for non-alphabet characters: "
            +RegexCharsOutOfAlphabet);
    
    /*Making Tree Dictionary*/
    treeDict = new ArrayList<>(dict_Q_Letters);
    
    /*All delimiters will be replaced by spaces*/
    srsTxt = srsTxt.replaceAll(
            RegexCharsOutOfAlphabet.replace("]", "^ ]"), " ");
    srsTxt = srsTxt.replaceAll("\\s+", " ");     //Remove duplicate spaces
    srsTxt += " ";  //The last word in the text should ending on whitespace
    char[] str = srsTxt.toCharArray();  //Source text is char-array now
    
    /*The initial filling of the dictionary with "letters of the alphabet", 
     * Ceep in mind that: any char of the first letter of word is defined only 
     * by its order in alphabet, and char of all subsequent ones is specified in 
     * the corresponding NodrLink*/
    for (i = 0; i < dict_Q_Letters; i++) {     
       treeDict.add(new ArrayList<>()); 
    }

    /* filling dictionary (Next Node will be just after alphabetic Nodes) */
    newAdr_toWrite = dict_Q_Letters;
    
    /* Char-by-char processing of the words. 
     * i - the iterator of the letter in the string.
     * If there is a space at the beginning of the line, it is omitted*/
    for (i = (str[0]==' ') ? 1 : 0; i < str.length-1; i++) {
        nextAdr_toFind = defineLetterNum(str,i);/*The first letter of the word*/
        do {    //The processing loop of each character in the current word 
            ch = str[i + 1];    //Next char
            
            /*The search loop of ch in the current Node of the given word*/
            charExist = -1;   
            for (j = 0; j < treeDict.get(nextAdr_toFind).size(); j++) {
                if (treeDict.get(nextAdr_toFind).get(j).getLetter() == ch) {
                    charExist=j;    
                    break; 
                }   
            }   //charExist = -1, if sutable char not found
            
            /*Decision block*/
            if (charExist != -1) {    /* sutable char found */
                treeDict.get(nextAdr_toFind).get(charExist).add1Hit();
                if (ch != ' ') {//Getting nextAdr_toFind for the next iteration
                    nextAdr_toFind = treeDict.get(nextAdr_toFind).
                            get(charExist).getAddress();
                }
            }else{              /* sutable char does not exist */
                if (ch != ' ') {  //Adding new letter
                    treeDict.get(nextAdr_toFind).       // new NodeLink adding
                            add(new NodeLink(ch, newAdr_toWrite));
                    treeDict.add(new ArrayList<>());    //new Node adding
                    /*it will be processed in the next iteration of loop */
                    nextAdr_toFind = newAdr_toWrite++;    
                    repeatedNodes++;
                }else{            //Adding whitespace (without new Node)
                    treeDict.get(nextAdr_toFind).add(new NodeLink(' ', -1));
                    containedWords++;
                }   
            }       //The increment of the while-loop, 
            i++;    //so on the spaces there is a double iteration.
        } while (ch != ' ');
        processedWords++;
    }
    
    /*Printing statistics*/
    System.out.println("Tree Dictionary just formed:\nTotal words processed: "
            + String.valueOf(processedWords)+" of which the origin ones: "
            + String.valueOf(containedWords));
    System.out.println("Repeated using Nodes: " 
            + String.valueOf(repeatedNodes));
    treeDict.toArray();     //Converting to a static array
    this.sortByDemand();    //Sorting by default, because all whitespaces 
    }                       //should be at the ends of their Node lists
    
    /**
     * The spaceRestore_Fast method uses a TreeDictionary instance recover 
     * spaces in the input String text. For each uncertainty, a longer word 
     * is preferred. This method returns the first suitable option of restored 
     * whitespaces as String.
     * It is assumed that the input text doesn't have any spaces or punctuation.
     * If there are any such in the input text, they will be regarded as a 
     * "context change", which indicates the explicit ending of the previous 
     * word, as in the case of foreign letters or numbers.
     * The register of letters is not taken into account when text in process.
     */
    public String spaceRestore_Fast(String unspTxt){
    /* In this implementation, recursion does not implemented
     */
    
    /* Variables for extracting known-language fragments of text: */
    StringBuilder resStr = new StringBuilder(unspTxt);   //output string
    
    /* Reducing the register of source string (dictionary is in lower case) */
    unspTxt=unspTxt.toLowerCase();    
    String crntSubStr;  //The current fragment (before the "change of context")
    int startInd = 0;   //The address of the beginning fragment
    int finishInd = 0;  //end of the current text fragment
    
    /* Pattern(Regex) for non alphabet symbols */
    Pattern abc_OnlyP = Pattern.compile(this.getRegexCharsOutOfAlphabet());
    
    /* Regex for alphabetic letters */
    String no_abcRegEx = this.getRegexCharsOutOfAlphabet().
            replaceAll("\\^", ""); 
    Pattern no_abcP = Pattern.compile(no_abcRegEx);
         
    /*Variables for whitespaces restoring:*/
    char[] strChars;    //current fragment as an array of letters
    int l, i;           //l - iterator letters in a String
    int crntNodeAdr;    //Node Addresses in dictionary (current and next)
    int nextNodeAdr;   
    char nextCh;        //The next char in the String
    int PassedWhiteSpacesOffset = 0;    //Spacing counter
    int BrPointsCreatedCntr = 0;        //created branch-point counter
    int BrPointsUsedCntr = 0;           //Number of and returns on them

    /*Stacks for processing the current fragment*/
    Stack<Integer> BranchingPoints = new Stack<>(); 
    Stack<Integer> whiteSpcesAdr = new Stack<>();   

    while (startInd+finishInd<unspTxt.length() -1) { /*Text fragments loop*/
        
        /*Search for ending of with-in-alphabetic fragment*/
        startInd += finishInd;    //the start index calculating
        Matcher txt_Abc = abc_OnlyP.matcher(unspTxt.substring(startInd)); 
        if (txt_Abc.find()) {       //extracting workable fragment
            finishInd = txt_Abc.start();
        } else {    //Search may be not successful at the end of the input text
            finishInd = unspTxt.substring(startInd).length();
        }
        /* The fragment should ending at whitespace (it means end of the word)*/
        crntSubStr = unspTxt.substring(startInd, startInd+finishInd) + " ";   
        strChars = crntSubStr.toCharArray();   //Now we work with the char array
        l = 0;            //The address of the current char in the fragment
        crntNodeAdr = -1; //Node adress for the first letter of the word

        /*The loop of letters in the current fragment*/
        while (l < strChars.length - 1) {
            if (crntNodeAdr == -1) {    //searching the first letter of the word
                crntNodeAdr = defineLetterNum(strChars,l);
            }
            nextCh = strChars[l + 1];   //Next char in string
            nextNodeAdr = -2;   //-2 means there is no suitable address found

            /* The search cycle in the current Node of the next letter */
            for (i = 0; i < treeDict.get(crntNodeAdr).size(); i++) {
                if (treeDict.get(crntNodeAdr).get(i).getLetter() == nextCh) {
                    nextNodeAdr = treeDict.get(crntNodeAdr).get(i).getAddress();
                    break;  //The Node is found (for next letter in the line)
                }            
            }
            
            /* Working out block: */
            if (nextNodeAdr >= 0) {   /*If the next letter is found*/
                
                /* Checking the presence of the branch point. If it's possible 
                 * to insert whitespace in the given place, it (space) has been 
                 * written as the last NodeLink in current Node*/
                if (treeDict.get(crntNodeAdr).get(treeDict.
                        get(crntNodeAdr).size() - 1).getLetter() == ' ') {
                    BranchingPoints.push(l);
                    BrPointsCreatedCntr++;  //Registration of new branch point        
                }
                crntNodeAdr = nextNodeAdr;   
                
            /* If the next letter is not found, we should checking that 
            is it possible to insert a space */
            } else {        
                if (!(treeDict.get(crntNodeAdr).isEmpty())
                        && (treeDict.get(crntNodeAdr).get(treeDict.
                                get(crntNodeAdr).size()-1).getLetter() ==' ')) { 
                    whiteSpcesAdr.push(l);  //Registration of the new whitespace
                    crntNodeAdr = -1; 
                
                /*If it isn't possible to insert whitespace, We go back 
                to the last branch point*/
                } else {    
                    crntNodeAdr = -1;//The next char is a new letter of the word
                    if (BranchingPoints.empty()) {
                        System.out.println("Unresolved fragment "
                                + "has been gotten");
                        return resStr.toString();
                    }
                    l = BranchingPoints.pop();  //Return the l position
                    while (!(whiteSpcesAdr.empty()) 
                            && (whiteSpcesAdr.peek() > l)) { 
                        whiteSpcesAdr.pop();    //Removing extra spaces
                    }
                    whiteSpcesAdr.push(l);      //Registering new whitespace
                    BrPointsUsedCntr++; 
                }
            }    
            l++;    //moving the position of the current letter
        }
       
        /* The positions of the spaces for the current fragment  
        have been calculated, so now they will be pasted in string*/
        BranchingPoints.clear();  //Clearing stacks
        l = whiteSpcesAdr.size(); //Number of whitespaces found in last fragment
        while (!whiteSpcesAdr.empty()) {    //Pasting whitespaces 
            resStr.insert(startInd + whiteSpcesAdr.pop()+
                    1 + PassedWhiteSpacesOffset, ' '); 
        }
        resStr.insert(startInd + PassedWhiteSpacesOffset, ' ');
        /* Updating the correction of space coordinates for the next iteration*/
        PassedWhiteSpacesOffset += l + 1;   
     
        /* Extraction of the "foreigner" fragment */
        startInd += finishInd;  //the start index calculating          
        Matcher txt_noAbc = no_abcP.matcher(unspTxt.substring(startInd));
        if (txt_noAbc.find()) { 
            finishInd = txt_noAbc.start();
        }else{      //Search may be not successful at the end of the input text
            finishInd=unspTxt.substring(startInd).length();    
        }
    }
    
    /* Final report on text spacing: */
    System.out.println("Spacing is done \nWhitespaces are arranged total: "
            + PassedWhiteSpacesOffset);
    System.out.println("Branch points formed total: " + BrPointsCreatedCntr
            + "\nhave been used from it : " + BrPointsUsedCntr);
    return resStr.toString();   //Returning result as String
    }
    
    /**
     * defineLetterNum - private method. Defines the sequence number of the 
     * letter in the alphabet for the specified element of the char array. 
     * It is used to determine the Node number (1-st dimension) of the 
     * dictionary for the first letter of each word.
     */
    private int defineLetterNum(char[] ch, int i){  
        
        /* Define UniCode for this character */
        int alphabetAdr = Character.codePointAt(ch, i);
        int r = 0;  //number of range
            
        /* Determine the sequence number of char in the alphabet */
        while (!((alphabetAdr >= uRngBeginAt[r]) 
                && (alphabetAdr <= uRngEndAt[r]))) {
            r++;    //Coorect range finding
        }
        return uncd2Dict_Corr[r] + alphabetAdr - uRngBeginAt[r];
    }
    
    /**
     * The showDictInsides method prints all fields af all NodeLink 
     * of each Node in the created dictionary
     */
    public void showDictInsides(){
        
        /*Print of general information*/
        System.out.println("Total size of dictionary: "
                + String.valueOf(treeDict.size()) + " Nodes");
        for (int i = 0; i < treeDict.size(); i++) {         //Node loop
            System.out.println(" Node List №: " + String.valueOf(i)
                    + "\t has size of: "+ String.valueOf(treeDict.get(i).size())
                    + " nodeLinks:");
            for (int j = 0; j < treeDict.get(i).size(); j++) {  //nodeLink loop
                treeDict.get(i).get(j).showLink();
            }
        }
    }
    
     /** 
      * The showDict method prints all words contained in the TreeDict 
      * object in groups by the first letter of the word. In each group, words
      * are output in the order in which they are storing in the TreeDictionary.
      */
    void showDict(){
        
        /*The Alphabet is in unicode order:*/
        for (int l = 0; l < dict_Q_Letters; l++) {  /*alphabet loop*/
            System.out.println("words by letter: " + alphabet.charAt(l)); 
            
            /* Recursive call of the special private method to bypass 
             * all records on all branch points 
             * prefix means the same as "prehistory" */
            String prefix = alphabet.substring(l, l + 1);
            reсPrintCharacter(l, prefix);
        }
    }
    
    /**
     * reсPrintCharacter - private method used to print words 
     * from the dictionary. It used only by showDict() method.
     * Provides a recursive bypass of all leafes of the dictionary tree
     */
    private void reсPrintCharacter(int id, String prefix){
        
        /* Node Link loop for current Node*/
        for (int i = 0; i < treeDict.get(id).size(); i++) {
            if (treeDict.get(id).get(i).getLetter() == ' ') {  
                System.out.println(prefix);  //Printing the word
                
            /* While a space is not encountered, There is a recursive call 
            of this function with new set of prefix letters*/        
            } else {
                reсPrintCharacter(treeDict.get(id).get(i).getAddress(), prefix
                        + String.valueOf(treeDict.get(id).get(i).getLetter()));
            }
        }
    }
    
    /** 
     * Method toStringAll  returns all words contained in the TreeDict 
     * object in that order at which thay stored
     * Any way it still in groups by the first letter of the word.
     */
    public String toStringAll(){
        
        /*Var declaretion*/
        String strDict = "";//Result string
        String prefix;      //Moving deep string
        String add;         //Gotted string for current alphabet letter
        
        for (int l = 0; l  <dict_Q_Letters; l++) {  /*alphabet loop*/

            /* Recursive call of the special private method to bypass all 
            records on all branch points */
            prefix = alphabet.substring(l, l + 1);
            add = reсGetCharacter(l, prefix, 0);
            if (!add.equals(prefix)) {  //excepting cases where no one word  
                strDict += add;         //exist for current alphabet letter
            }
        }
        return strDict.toString();
    }
    
    /**
     * reсGetCharacter - private method used to get all words from the Tree
     * Dictionary as String. It used only by toStringAll() method. Provides a  
     * recursive bypass of all leafes of the dictionary tree and returns String
     */
    private String reсGetCharacter(int id, String prefix, int recLvl ){
        
        int r;  //Mark of the beginning of last word
                //recLvl - The level of recursion
        
        /* NodeLink loop for current Node*/
        for (int i = 0; i < treeDict.get(id).size(); i++) {
            
            /* Copying the similar "prehistory" of previous word */
            if (prefix.charAt(prefix.length() - 1) == '\n') {
                r = prefix.lastIndexOf("\n", prefix.length() - 2);
                prefix += prefix.substring(r + 1, r + recLvl + 2);
            }
            if (treeDict.get(id).get(i).getLetter() == ' ') {  
                return prefix + "\n";  //end-of-the-word marking
                
            /* While a space is not encountered, There is a recursive call of 
            this function with new set of prefix letters*/        
            } else {
                prefix = reсGetCharacter(treeDict.get(id).get(i).getAddress(), 
                        prefix + String.valueOf(treeDict.get(id).get(i).
                                getLetter()), recLvl + 1);  //Recursive call
            }
        }
        return prefix;
    }
    
    
    /**
     * sortByDemand - Method for sorting by "popularity" of NodeLinks inside 
     * each Node. The demand is determined by NodeLink.hits
     * Important: the whitespace character is always in the end of the Node list
     */
    public void sortByDemand(){    
        for(int i = 0; i < treeDict.size(); i++){ //Node loop
            Collections.sort(treeDict.get(i), (NodeLink one, NodeLink other) -> 
                   Integer.compare( (other.getAddress() != -1) ? other.getHits() 
                            : 0, (one.getAddress() != -1) ? one.getHits() : 0));
        }
    }
    
    /**
     * sortByAlphabet - Method for sorting in alphabetical order 
     * (ascending Unicode), 
     * BUT the whitespace character always remains in the the end
     */
    public void sortByAlphabet(){
        for(int i = 0; i < treeDict.size(); i++){   //Node loop
          Collections.sort(treeDict.get(i), (NodeLink one, NodeLink other) -> 
                  Integer.compare( (one.getAddress() != -1) ? one.getLetter() : 
                          Integer.MAX_VALUE , (other.getAddress()!=-1) ? 
                                  other.getLetter() : Integer.MAX_VALUE ));
        }
    }
    
    /**
     * Private method getRegexCharsOutOfAlphabet Returns a regular expression 
     * that specifies all characters that do not includes to the alphabet
     */
    public String getRegexCharsOutOfAlphabet(){
        return RegexCharsOutOfAlphabet;
    }

    /**
     * Returns the number of words which have been processed through the Tree-dictionary making
     */
    public int getProcessedWords(){
        return processedWords;
    }
    /**
     * Returns the number of words contained in the Tree-dictionary instance
     */
    public int getContainedWords(){
        return containedWords;
    }
    
    /**
     * NodeLink - The private class used by the TreeDict dictionary. 
     * Its instance is a record of a particular letter in a particular word 
     * from the dictionary (the line in a list which stores in a Node).
     */
    class NodeLink {    
        
        /**The Node address in the dictionary that corresponds to this link 
         * (if the link is without an address, then = -1) */
        private int address;
        
        /**The letter corresponding to the given link (all in the lower case) */
        private char letter; 
        
        /**The number of uses of this link during the dictionary generation */
        private int hits;
        
        /**
         * NodeLink method - constructor for link formation. 
         * There shouldn't be other setters, except for add1Hit.
         */ 
        NodeLink(char ch, int adr){
            
            letter = ch;
            address = adr;
            hits = 1;
        }
        
        /**
         * showLink - private method for printing one specific NodeLink.
         * It used only by showDictInsides method
         */
        private void showLink(){
            System.out.println("Id: " + address + "\t char:'"
                                      + letter + "' \t times used:" + hits);
        }
        
        /**
         * Get-method for charcter of given NodeLink         
         */
        char getLetter(){
            return letter;
        }
        
        /**
         * Get-method for value of demand for given NodeLink         
         */
        int getHits(){
            return hits;
        }
        
        /**
         * Get-method for address of Node link to which written in that NodeLink      
         */
        int getAddress(){
            return address;
        }
        
        /**
         * add1Hit пincrements the usage count of this NodeLink.hits
         */
        void add1Hit(){
            hits++;
        }
    }
}
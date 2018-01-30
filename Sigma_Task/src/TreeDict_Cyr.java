/*
 * This unit developed by Alexey Gorobets as testing task for Sigma Software Development. 
 * Described class TreeDict_Cyr can be used by SigmaTask.java file with main method 
 * for convinient demonstration of given capabilities, which described bellow.
 */

package sigmatask;
        
import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The TreeDict_Cyr class is a specific way of storing the words of the Russian 
 * language (in Cyrillic symbols). Such a dictionary is created as constructor 
 * is called and can be used by the spaceRestore method for restoring white 
 * spaces in this text.
 */

public class TreeDict_Cyr{
/* Technically this is an array where each element is an object which corresponds 
 * to the letter from dictionary for each case of a certain sequence of preceding letters.
 * The letters without "prehistory" (the first letters of words) correspond to 
 * the first 33 elements of the TreeDict_Cyr array in almost alphabetical order. 
 * The exception is that the letter 'ё' which stores in the end of the alphabet.
 * This way, first letters are arranged in the unicode-growing order. 
 * The dictionary does not take into account register of letters.
 * Each element of the array TreeDict_Cyr (by the first dimension) stores another 
 * array which also called Node. Node is a list of NodeLink objects, each of which 
 * have a link to the next possible letter. The NodeLink object (private class 
 * in this file) contains the symbol of next letter, the number of Node corresponding 
 * to it, and the number of passes through this link during the dictionary formation.   
 */  
    /**The dictionary*/
    private ArrayList<ArrayList<NodeLink>> treeDict; 
    
    /** regular expression to determine out-of-alphabet charcters*/
    private String RegexCharsOutOfAlphabet="[^а-я^ё]";
    
    /**The number of letters in the dictionary*/
    private final int DICT_Q_LETTERS=33;
    
    /**Unicode of the first letter of the alphabet: 'а'*/
    private final int START_LETTER_UTF8=1072;
    
    /**Unicode of the last letter of the alphabet: 'я'*/
    private final int FINISH_LETTER_UTF8=1103;
    
    /**Unicode of special letter of the alphabet: : 'ё'*/
    private final int OUT_OF_BOUNDS_LETTER_UTF8=1105;
    
    /**The number of processed words (which stored in the sourse text) */
    private int processedWords;
    
    /**Total number of stored words in the dictionary*/
    private int containedWords;
    
    /**
     * The TreeDict_Cyr constructor recieves any String-sequence of characters 
     * which contains Cyrillic symbolst. The object of the TreeDict_Cyr class 
     * can be used to restore spaces in the text using the spaceRestore_ methods.
     */
    public TreeDict_Cyr(String ru_Str){
    /* The dictionary is filled with words without taking into account the suffixes and endings (any different letter means different words)
     * The criteria of the last letter in the word is the reference to the node with Char = ' '
     * The address given by int covers about 20GB of records (NodeLinks) in given format.
     */
    
    treeDict = new ArrayList<>(DICT_Q_LETTERS); //Initialization of first 33 letters of the alphabet
    int i, j;           
    int newAdr_toWrite; //1-st dimension adress of treeDict to write next Node
    int nextAdr_toFind; //1-st dimension adress of treeDict in which it can be find suitable letter through the 2-nd dimension
    int charExist;      //2-nd dimension adress of treeDict (in current Node) which cooresponds just-find-letter (eq. to -1 if it's not)
    char ch;            //The next letter of searched word, if current letter is last, it will be eq. to ' '
    int repeatedNodes;  //The counter of repeated uses of all NodeLinks
             
    /*Pre-processing of the input text ru_Str*/
    ru_Str=ru_Str.toLowerCase();    
    System.out.println("regex for non-alphabet characters: "+RegexCharsOutOfAlphabet);
    
    /*All punctuation will be replaced by spaces*/
    ru_Str=ru_Str.replaceAll(RegexCharsOutOfAlphabet.replace("]", "^ ]"), " ");
    ru_Str=ru_Str.replaceAll("\\s+", " ");  //Remove duplicate spaces
    ru_Str+=" ";    //The last word in the text should ending on whitespace (because it is the symbol of the end of the word)
    char[] str=ru_Str.toCharArray();    //Source text is char-array now
    
    /*The initial filling of the dictionary with "letters of the alphabet", 
     * Ceep in mind that: any char of the first letter of word is defined only by 
     * its order in alphabet, and char of all subsequent ones is specified in 
     * the corresponding NodrLink
     */
    for (i=0; i<DICT_Q_LETTERS; i++) {     
       treeDict.add(new ArrayList<>()); 
    }

    /*filling dictionary*/
    processedWords=0;   //Initializing
    containedWords=0;
    repeatedNodes=0;
    newAdr_toWrite=DICT_Q_LETTERS;  //Next Node will be just after alphabetic Nodes
    
    /* Char-by-char processing of the words. i - the iterator of the letter in the string.
     * If there is a space at the beginning of the line, it is omitted*/
    for (i=(str[0]==' ') ? 1 : 0; i<str.length-1; i++) {
        nextAdr_toFind=LetterNum(str,i);    //The address (in the alphabet) of the first letter of the word
        do {    //The processing loop of each character in the current word (including the first one)
            ch=str[i+1];    //Next char
            
            /*The search loop for the first occurrence of ch in the current Node of the given word*/
            charExist=-1;   
            for (j=0; j<treeDict.get(nextAdr_toFind).size(); j++) {
                if (treeDict.get(nextAdr_toFind).get(j).getLetter()==ch) {
                    charExist=j;    
                    break;  //Next character of this word is found at charExist-adress in the current Node
                }   
            }   //charExist=-1, sutable char not found
            
            /*Decision block*/
            if (charExist!=-1) {//If NodeLink for next char (in curren "prehistory") does exist:
                treeDict.get(nextAdr_toFind).get(charExist).add1Hit();  //then the statistics of the "visit" of this nodeLink will be updated
                if (ch!=' ') {  //If the next char - Not a whitespace, then we get nextAdr_toFind for the next iteration
                    nextAdr_toFind=treeDict.get(nextAdr_toFind).get(charExist).getAddress();
                }   //And if the next char is a whitespace, then nextAdr_toFind will be redefined in the next iteration of the outer loop
            }else{              //If NodeLink for next char does not exist it should be added
                if (ch!=' ') {  //Adding new letter
                    treeDict.get(nextAdr_toFind).add(new NodeLink(ch,newAdr_toWrite));  //First adding NodeLink,
                    treeDict.add(new ArrayList<>());    //and then initializing the corresponding new Node
                    nextAdr_toFind=newAdr_toWrite++;    //it will be processed in the next iteration of loop
                    repeatedNodes++;
                }else{          //Adding whitespace (without new Node)
                    treeDict.get(nextAdr_toFind).add(new NodeLink(' ',-1));
                    containedWords++;
                }   
            }
            i++;    //The internal increment of the while-loop (until the end of the word), so on the spaces there is a double iteration.
        } while (ch!=' ');
        processedWords++;
    }
    
    /*Printing statistics*/
    System.out.println("The dictionary just formed:\nTotal words processed: "+
            String.valueOf(processedWords)+" of which the origin ones: "+String.valueOf(containedWords));
    System.out.println("Repeated using Nodes: "+String.valueOf(repeatedNodes));
    treeDict.toArray();     //Converting to a static array
    this.sortByDemand();    //Sorting by default, because all whitespaces should be at the ends of their Node lists
    }                       //Ceep in mind: the whitespace in the dictionary means the end of the word
    
    /**
     * The spaceRestore_Fast method uses a dictionary built by appropriate 
     * constructor to recover spaces in the input String text.
     * For each uncertainty, a longer word is preferred.
     * This method returns the first suitable  option (case) of restored whitespaces.
     * It is assumed that the input text doesn't have any spaces or punctuation.
     * If there are any such in the input text, they will be regarded as a "context
     * change", which indicates the explicit ending of the previous word, 
     * as in the case of foreign letters or numbers.
     * The register of letters is not taken into account when text in process.
     */
    public String spaceRestore_Fast(String srsTxt){
    /* In this implementation, recursion does not implemented to resolve ambiguities
     */
    
    /*Variables for extracting Russian-language fragments of text:*/
    StringBuilder resStr = new StringBuilder(srsTxt);   //initialization of the output string
    
    /*Reducing the register of source string, because the dictionary is in lower case*/
    srsTxt=srsTxt.toLowerCase();    
    String crntSubStr;  //The current fragment (before the "change of context")
    int startInd=0, finishInd=0;   //The address of the beginning and end of the current text fragment (respectively)
    
    /*Pattern(Regex) for non Cyrilic, it will be used to extract Russian-language fragments*/
    Pattern cyrOnlyP = Pattern.compile(this.getRegexCharsOutOfAlphabet());
    
    /*Regex for Cyrilic, it will be used to extraction of other fragments*/
    String noCyrRegEx=this.getRegexCharsOutOfAlphabet().replaceAll("\\^", ""); 
    Pattern noCyrP = Pattern.compile(noCyrRegEx);   //Pattern(Regex) for extracting fragments of foreign languages
         
    /*Variables for whitespaces restoring:*/
    char[] strChars;    //current fragment as an array of letters
    int l, i;           //l - iterator letters in a String
    int crntNodeAdr, nextNodeAdr;   //Node Addresses in dictionary (current and next)
    char nextCh;    //The next char in the String
            
    /*Spacing counters, created branch-point counter and returns on them, respectively*/
    int PassedWhiteSpacesOffset=0, BrPointsCreatedCntr=0, BrPointsUsedCntr=0;

    /*Stacks for processing the current fragment*/
    Stack<Integer> BranchingPoints = new Stack<>(); //Branch points are positions in the line of other possible spaces
    Stack<Integer> whiteSpcesAdr = new Stack<>();   //The char addresses with followed whitespaces
    
    /*Fragment loop*/
    while (startInd+finishInd<srsTxt.length()-1) {
        
        /*Search for ending of Cyrillic fragment*/
        startInd+=finishInd;    //the start index calculating
        Matcher txt_Cyr = cyrOnlyP.matcher(srsTxt.substring(startInd)); //Matcher initialization
        if (txt_Cyr.find()) {                       //and its using to extract Cyrillic fragment
            finishInd=txt_Cyr.start();
        } else {    //Search may not be successful at the end of the input text
            finishInd=srsTxt.substring(startInd).length();
        }

        /*the fragment should ending at whitespace, since it is the symbol of the end of the word*/
        crntSubStr=srsTxt.substring(startInd, startInd+finishInd)+" ";   
        strChars=crntSubStr.toCharArray();    //Now we work with the char array
        l=0;            //The address of the current char in the fragment
        crntNodeAdr=-1; //Node adressfor the first letter of the word

        /*The loop of letters in the current fragment*/
        while (l<strChars.length-1) {
            if (crntNodeAdr == -1) {    //crntNodeAdr = -1 indicates that the first letter of the word is searched for
                crntNodeAdr=LetterNum(strChars,l);  //The address of the first letter of the new word.
            }
            nextCh=strChars[l+1];    //Next char in string
            nextNodeAdr=-2;     //-2 will mean that the suitable address for the next Node haven't been found

            /*The search cycle in the current Node of the next letter (of a given string)*/
            for (i=0; i<treeDict.get(crntNodeAdr).size(); i++) {
                if (treeDict.get(crntNodeAdr).get(i).getLetter()==nextCh) {
                    nextNodeAdr=treeDict.get(crntNodeAdr).get(i).getAddress() ;
                    break;  //The next Node is found (for the next letter in the line)
                }            
            }
            
            /*Block of conditions for following processing of received nextNodeAdr:*/
            if (nextNodeAdr>=0) {   /*If the next letter is found*/
                
                /* Checking the presence of the branch point. If it's possible 
                 * to insert whitespace in the given place, it (space) has been 
                 * written as the last NodeLink in current Node*/
                if (treeDict.get(crntNodeAdr).get(treeDict.get(crntNodeAdr).size()-1).getLetter()==' ') {
                    BranchingPoints.push(l);
                    BrPointsCreatedCntr++;      //Registration of the found branch point        
                }
                crntNodeAdr=nextNodeAdr;    
            } else {        /* If the next letter is not found, we should checking that is it possible to insert a space */
                if (!(treeDict.get(crntNodeAdr).isEmpty()) && (treeDict.get(crntNodeAdr).get(treeDict.get(crntNodeAdr).size()-1).getLetter()==' ')) { 
                    whiteSpcesAdr.push(l);  //Registration of the new whitespace
                    crntNodeAdr=-1;     //The next char will be processed as a new letter of the word
                } else {    /*If it isn't possible to insert whitespace, We go back to the last branch point*/
                    crntNodeAdr=-1; //The next char is a new letter of the word
                    l=BranchingPoints.pop();    //Return the position of the current character in the string
                    while (!(whiteSpcesAdr.empty())&&(whiteSpcesAdr.peek()>l)) { 
                        whiteSpcesAdr.pop();    //Removing extra spaces that have 
                    }                           //been defined after the branch point
                    whiteSpcesAdr.push(l);      //Registering new whitespace
                    BrPointsUsedCntr++; 
                }
            }    
            l++;    //moving the position of the current letter
        }
        
        /* The positions of the spaces for the current frame have been calculated, 
         * so now they will be pasted in string*/
        BranchingPoints.clear();    //Clearing stacks
        l=whiteSpcesAdr.size();     //Number of whitespaces found in last fragment
        while (!whiteSpcesAdr.empty()) {    //Pasting whitespaces 
            resStr.insert(startInd+whiteSpcesAdr.pop()+1+PassedWhiteSpacesOffset, ' '); 
        }
        resStr.insert(startInd+PassedWhiteSpacesOffset, ' ');   //Pasting one in the beginning
        PassedWhiteSpacesOffset+=l+1;   //Updating the correction for the space coordinates (for the next iteration)
        
        /*Extraction of the foreigner fragment (search for the first Cyrillic letter)*/
        startInd+=finishInd;    //the start index calculating          
        Matcher txt_noCyr = noCyrP.matcher(srsTxt.substring(startInd)); //Matcher initialization
        if (txt_noCyr.find()) { //Searchin for the end of foreigner fragment
            finishInd=txt_noCyr.start();
        }else{      //Search may not be successful at the end of the input text
            finishInd=srsTxt.substring(startInd).length();    
        }
    }
    
    /*Final report on text spacing:*/
    System.out.println("Spacing is done \nWhitespaces are arranged total: "+PassedWhiteSpacesOffset);
    System.out.println("Branch points formed total: "+BrPointsCreatedCntr+"\nhave been used from it : "+BrPointsUsedCntr);
    return resStr.toString();   //Returning result as String
    }
    
    /**
     * Method spaceRestore_Full uses a dictionary built by appropriate 
     * constructor to recover spaces in the input String text. 
     * This method searches all possible cases of spacing for each fragment of 
     * the text and choose the option in which the least number of spaces are arranged.
     * You have to get a lot of patience to try it.
     * It is assumed that the input text doesn't have any spaces or punctuation.
     * If there are any such in the input text, they will be regarded as a "context
     * change", which indicates the explicit ending of the previous word, 
     * as in the case of foreign letters or numbers.
     * The register of letters is not taken into account when text in process.
     */
    public String spaceRestore_Full(String srsTxt){
    /* In this implementation, recursion does not implemented to resolve ambiguities
     */
    
    /*Variables for extracting Russian-language fragments of text:*/
    StringBuilder resStr = new StringBuilder(srsTxt);   //Output string
    
    /*Reducing the register of source string, because the dictionary is in lower case*/
    srsTxt=srsTxt.toLowerCase();    
    String crntSubStr;  //The current fragment (before the "change of context")
    int startInd=0, finishInd=0;   //The address of the beginning and end of the current text fragment (respectively)
    
    /*Pattern(Regex) for non Cyrilic, it will be used to extract Russian-language fragments*/
    Pattern cyrOnlyP = Pattern.compile(this.getRegexCharsOutOfAlphabet());
    
    /*Regex for Cyrilic, it will be used to extraction of other fragments*/
    String noCyrRegEx=this.getRegexCharsOutOfAlphabet().replaceAll("\\^", ""); 
    Pattern noCyrP = Pattern.compile(noCyrRegEx);   //Pattern(Regex) for extracting fragments of foreign languages
            
    /* Variables for processing of white spaces recover cases */
    Stack<Integer> bestCase = new Stack<>();   // The case with the least number of white spaces
    int bestL;              // Number of spaces in bestCase
    int caseCntr;           //The counter of possible spacing variants for each fragment
    int txtRangeCntr=0;     //Sequence number of the fragment
    
    /*Variables for whitespaces restoring:*/
    char[] strChars;    //current fragment as an array of letters
    int l, i;           //l - iterator letters in a String 
    int crntNodeAdr, nextNodeAdr;   //Node Addresses in dictionary (current and next)
    char nextCh;        //The next char in the String
            
    /*Spacing counters, created branch-point counter and returns to them, respectively*/
    int PassedWhiteSpacesOffset=0, BrPointsCreatedCntr=0, BrPointsUsedCntr=0;

    /*Stacks for processing the current fragment*/
    Stack<Integer> BranchingPoints = new Stack<>(); //Branch points are positions in the line of other possible spaces
    Stack<Integer> whiteSpcesAdr = new Stack<>();   //The char addresses with followed whitespaces
    
    /*Fragment loop*/
    while (startInd+finishInd<srsTxt.length()-1) {
        
        /*Search for ending of Cyrillic fragment*/
        startInd+=finishInd;    //the start index calculating
        Matcher txt_Cyr = cyrOnlyP.matcher(srsTxt.substring(startInd)); //Matcher initialization
        if (txt_Cyr.find()) {                       //and its using to extract Cyrillic fragment
            finishInd=txt_Cyr.start();
        } else {    //Search may not be successful at the end of the input text
            finishInd=srsTxt.substring(startInd).length();
        }
        
        /*the fragment should ending at whitespace, since it is the symbol of the end of the word*/
        crntSubStr=srsTxt.substring(startInd, startInd+finishInd)+" ";   
        txtRangeCntr++;
        strChars=crntSubStr.toCharArray();    //Now we work with the char array
        l=0;            //The address of the current char in the fragment
        crntNodeAdr=-1; //Node adressfor the first letter of the word
        bestL=Integer.MAX_VALUE;    //The smallest number of whitespaces reached
        caseCntr=0;
        do {
            /*The loop of letters in the current fragment*/
            while (l<strChars.length-1) {
                if (crntNodeAdr==-1) {   //crntNodeAdr = -1 indicates that the first letter of the word is searched for
                    crntNodeAdr=LetterNum(strChars,l);  //The address of the first letter of the new word.
                }
                nextCh=strChars[l+1];    //Next char in string
                nextNodeAdr=-2; //-2 will mean that the suitable address for the next Node haven't been found
            
                /*The search cycle in the current Node of the next letter (of a given string)*/
                for (i=0; i<treeDict.get(crntNodeAdr).size(); i++) {
                    if (treeDict.get(crntNodeAdr).get(i).getLetter()==nextCh) {
                        nextNodeAdr=treeDict.get(crntNodeAdr).get(i).getAddress() ;
                        break;  //The next Node is found (for the next letter in the line)
                    }            
                }

                /*Block of conditions for following processing of received nextNodeAdr:*/
                if (nextNodeAdr>=0) {   /*If the next letter is found*/
                
                    /* Checking the presence of the branch point. If it's possible 
                     * to insert whitespace in the given place, it (space) has been 
                     * written as the last NodeLink in current Node*/
                    if (treeDict.get(crntNodeAdr).get(treeDict.get(crntNodeAdr).size()-1).getLetter()==' ') {
                        BranchingPoints.push(l);
                            BrPointsCreatedCntr++;  //Registration of the found branch point            
                    }
                    crntNodeAdr=nextNodeAdr;
                } else {        /* If the next letter is not found, we should checking that is it possible to insert a space */
                    if (!(treeDict.get(crntNodeAdr).isEmpty()) && 
                            (treeDict.get(crntNodeAdr).get(treeDict.get(crntNodeAdr).size()-1).getLetter()==' ')) { 
                    whiteSpcesAdr.push(l);  //Registration of the new whitespace
                    crntNodeAdr=-1;     //The next char will be processed as a new letter of the word
                    } else {    /*If it isn't possible to insert whitespace, We go back to the last branch point*/
                        crntNodeAdr=-1; //The next char is a new letter of the word
                        if (BranchingPoints.empty()) {
                            break;
                        }
                        l=BranchingPoints.pop();    //Return the position of the current character in the string
                        while (!(whiteSpcesAdr.empty())&&(whiteSpcesAdr.peek()>l)) { 
                            whiteSpcesAdr.pop();    //Removing extra spaces that have
                        }                           //been defined after the branch point
                        whiteSpcesAdr.push(l);      //Registering new whitespace
                        BrPointsUsedCntr++;     
                        }
                }
                l++;    //moving the position of the current letter
            }
            
            /*Registration of successful whitespaces restoring*/
            if ( l==strChars.length-1 ){ caseCntr++; }  //counting possible variants of spacing for future Checking
            if ((l==strChars.length-1)&&(bestL>=whiteSpcesAdr.size())) { //The choosing of the case with the least number of spaces
                bestL=whiteSpcesAdr.size();     //Copy coordinates for the best case
                bestCase = (Stack<Integer>) whiteSpcesAdr.clone();
            }
            
            /*If there are no branch points, then the fragment is completely worked out*/
            if ( BranchingPoints.empty() ) {
                break;
            }
            /* Otherwise, going to the last branch point */
            crntNodeAdr=-1; // The next letter is the first in a new word
            l=BranchingPoints.pop()+1;  //Moving back the position of the current character in the string
            while (!(whiteSpcesAdr.empty())&&(whiteSpcesAdr.peek()>=l)) { 
                whiteSpcesAdr.pop();    //Removing extra whitespaces
            }
            whiteSpcesAdr.push(l);      //Registering a new whitespace
            BrPointsUsedCntr++; 
        }while (!whiteSpcesAdr.empty());
    
            
        /* The positions of the spaces for the current frame have been calculated, 
         * so now they will be pasted in string*/
        bestL=bestCase.size();
        while (!bestCase.empty()) {     //Pasting whitespaces 
            resStr.insert(startInd+bestCase.pop()+1+PassedWhiteSpacesOffset, ' '); 
        }
        whiteSpcesAdr.clear();          //Clearing stacks
        BranchingPoints.clear();
        resStr.insert(startInd+PassedWhiteSpacesOffset, ' ');   //Pasting one in the beginning
        System.out.println("Processed fragment №: "+txtRangeCntr+" sutable cases: "+caseCntr);
        PassedWhiteSpacesOffset+=bestL+1;   //Updating the correction for the space coordinates (for the next iteration)
                
        /*Extraction of the foreigner fragment (search for the first Cyrillic letter)*/
        startInd+=finishInd;        //the start index calculating     
        Matcher txt_noCyr = noCyrP.matcher(srsTxt.substring(startInd)); //Matcher initialization
        if (txt_noCyr.find()) {     //Searchin for the end of foreigner fragment
            finishInd=txt_noCyr.start();
        } else {    //Search may not be successful at the end of the input text
            finishInd=srsTxt.substring(startInd).length();  
        }
    }
    
    /*Final report on text spacing:*/
    System.out.println("Spacing is done \nWhitespaces are arranged total: : "+PassedWhiteSpacesOffset);
    System.out.println("Branch points formed total: "+BrPointsCreatedCntr+"\nChecked from it: "+BrPointsUsedCntr);
    return resStr.toString();   //Returning result as String
    }
    
    /**
     * LetterNum - private method. Defines the sequence number of the letter in 
     * the alphabet for the specified element of the char array. It is used to 
     * determine the Node number (1-st dimension) of the dictionary for the 
     * first letter of each word.
     */
    private int LetterNum(char[] ch, int i){  
        
        /*Define UniCode for this character*/
        int alphabetAdr=Character.codePointAt(ch, i);
            
            /*Determine the sequence number of the letter in the alphabet that we are use*/
            if(alphabetAdr<=FINISH_LETTER_UTF8){ //Letterthe is within 'а'-'я' (without 'ё')
                alphabetAdr-=START_LETTER_UTF8;  //The number of letter (first one is 0)
            }else{              //otherwise it is  'ё'
                alphabetAdr=DICT_Q_LETTERS-1;    //In our alphabet it lies in the end
            }
        return alphabetAdr;
    }
    
    /**
     * The showDictInsides method prints all fields af all NodeLink 
     * of each Node in the created dictionary
     */
    public void showDictInsides(){
        
        /*Print of general information*/
        System.out.println("Total size of dictionary: "+String.valueOf(treeDict.size())+" Nodes");
        for (int i=0;i<treeDict.size();i++) {               //Node loop
            System.out.println(" Node List №: "+String.valueOf(i)+
                    "\t has size of: "+String.valueOf(treeDict.get(i).size())+" nodeLinks:");
            for (int j=0;j<treeDict.get(i).size();j++) {    //nodeLink loop
                treeDict.get(i).get(j).showLink();
            }
        }
    }
    
     /** 
      * The showDict method prints all words contained in the TreeDict_Cyr 
      * object in groups by the first letter of the word. In each group, words
      * are output in the order in which they are storing in the 
      * TreeDict_Cyr dictionary.
      */
    void showDict(){
        
        /*All used alphabet in the correct order in uppercase:*/
        char[] alphabet = {'А','Б','В','Г','Д','Е','Ж','З','И','Й','К','Л','М','Н','О',
            'П','Р','С','Т','У','Ф','Х','Ц','Ч','Ш','Щ','Ъ','Ы','Ь','Э','Ю','Я','Ё'};
        
        for (int l=0; l<DICT_Q_LETTERS; l++) {    /*alphabet loop*/
            System.out.println("слова на букву: "+alphabet[l]); 
            
            /* Recursive call of the special private method to bypass all records
             * on all branch points (prefix means the same as "prehistory")*/
            String prefix=String.valueOf(alphabet[l]).toLowerCase();
            reсPrintCharacter(l, prefix);
        }
    }
    
    /**
     * reсPrintCharacter - private method used to print words from the dictionary.
     * It used only by showDict() method.
     * Provides a recursive bypass of all leafes of the dictionary tree
     */
    private void reсPrintCharacter(int id, String prefix){
        
        /* Node Link loop for current Node*/
        for (int i=0; i<treeDict.get(id).size(); i++) {
            if (treeDict.get(id).get(i).getLetter()==' ') {  
                System.out.println(prefix);  //Print occurs when a whitespace is encountered
                
            /* While a space is not encountered, There is a recursive call of this 
             * function with new set of prefix letters*/        
            } else {
                reсPrintCharacter(treeDict.get(id).get(i).getAddress(), 
                        prefix+String.valueOf(treeDict.get(id).get(i).getLetter()));
            }
        }
    }
    
    /** 
     * Method toStringAll  returns all words contained in the TreeDict_Cyr 
     * object in that order at which thay stored
     * Any way it still in groups by the first letter of the word.
     */
    public String toStringAll(){
        
        /*All used alphabet in the correct order in uppercase:*/
        char[] alphabet = {'А','Б','В','Г','Д','Е','Ж','З','И','Й','К','Л','М','Н','О',
            'П','Р','С','Т','У','Ф','Х','Ц','Ч','Ш','Щ','Ъ','Ы','Ь','Э','Ю','Я','Ё'};
        String strDict="";  //Result string
        String prefix;      //Moving deep string
        String add;         //Gotted string for current alphabet letter
        
        for (int l=0; l<DICT_Q_LETTERS; l++) {    /*alphabet loop*/

            /* Recursive call of the special private method to bypass all records
             * on all branch points (prefix means the same as "prehistory")*/
            prefix=String.valueOf(alphabet[l]).toLowerCase();
            add=reсGetCharacter(l, prefix, 0);
            if (!add.equals(prefix)) {  //except cases where no one word for currenl
                strDict+=add;                   //for currenl alphabet letter
            }
        }
        return strDict.toString();
    }
    
    /**
     * reсGetCharacter - private method used to get all words from the dictionary
     * as String. It used only by toStringAll() method. Provides a recursive 
     * bypass of all leafes of the dictionary tree and returns String
     */
    private String reсGetCharacter(int id, String prefix, int recLvl ){
        
        int r;  //Mark of the beginning of last word
        //recLvl - The level of recursion
        
        /* NodeLink loop for current Node*/
        for (int i=0; i<treeDict.get(id).size(); i++) {
            if (treeDict.get(id).get(i).getLetter()==' ') {  
                return prefix+"\n";  //end of the word if whitespace is encountered
                
            /* While a space is not encountered, There is a recursive call of this 
               function with new set of prefix letters*/        
            } else {
                if (prefix.charAt(prefix.length()-1)=='\n') {   //First following of word through the Dictiomary
                    r=prefix.lastIndexOf("\n", prefix.length()-2);  //is accompanied by copying correct "prehistory"
                    prefix+=prefix.substring(r+1, r+recLvl+2);          //of word with similar beginning
                }
                prefix=reсGetCharacter(treeDict.get(id).get(i).getAddress(), //Recursive call
                        prefix+String.valueOf(treeDict.get(id).get(i).getLetter()), recLvl+1);
            }
        }
        return prefix;
    }
    
    
    /**
     * sortByDemand - Method for sorting by "popularity" of NodeLinks inside 
     * each Node. The demand is determined by NodeLink.hits.
     * Important: the whitespace character is always at the end of the Node list.
     */
    public void sortByDemand(){    
        for(int i=0;i<treeDict.size();i++){ //Node loop
            Collections.sort(treeDict.get(i), (NodeLink one, NodeLink other) -> 
                    Integer.compare( (other.getAddress()!=-1) ? other.getHits() 
                            : 0, (one.getAddress()!=-1) ? one.getHits() : 0));
        }
    }
    
    /**
     * sortByAlphabet - Method for sorting in alphabetical order 
     * (ascending Unicode), BUT the whitespace character always remains at the end
     */
    public void sortByAlphabet(){
        for(int i=0;i<treeDict.size();i++){ //Node loop
          Collections.sort(treeDict.get(i), (NodeLink one, NodeLink other) -> 
                  Integer.compare( (one.getAddress()!=-1)?one.getLetter():65535 , 
                          (other.getAddress()!=-1)?other.getLetter():65535 ));
        }
    }
    
    /**
     * Private method getRegexCharsOutOfAlphabet Returns a regular expression 
     * that specifies all characters that do not includes to the alphabet
     */
    String getRegexCharsOutOfAlphabet(){
        return RegexCharsOutOfAlphabet;
    }

    /**
     * NodeLink - The private class used by the TreeDict_Cyr dictionary. 
     * Its instance is a record of a particular letter in a particular word 
     * from the dictionary (the line in a list which stores in a Node).
     */
    class NodeLink {    
        
        /**The Node address in the dictionary that corresponds to this link 
         * (if the link is without an address, then = -1) */
        private int address;
        
        /**The letter corresponding to the given link (all letters in the lower case)*/
        private char letter; 
        
        /**The number of uses of this link during the dictionary generation*/
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
            System.out.println("Id: "+address+"\t char:'"+letter+"' \t times used:"+hits);
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
         * Get-method for address of Node, link to which written in that NodeLink      
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

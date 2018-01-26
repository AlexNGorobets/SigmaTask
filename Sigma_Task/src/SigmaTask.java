/*
Задачи:
1. Загрузить текст+
2. Сформировать из него словарь+
3. Удалить из текста знаки препинания и пробелы+
4. Восстановить пробелы при помощи словаря+

Концепция:
Самый быстрый способ поиска в массиве реализуем, когда содержимое (или ключ) может быть интерпретировано как адрес.
В контексте данной задачи, будет реализован словарь с древовидной структурой.
Технически это контейнер (тут, видимо коллекция) где каждый элемент является объектом соответствующим букве из словаря.
Т.е. (в первом приближении), на пример, в русском алфавите 33 буквы - первые 33 значения из контейнера соответствуют словам начинающимся на а,б..я,ё 
 Под "значением" здесь понимается экземпляр класса "Node". Такой объект содержит наименование буквы (char),
 количество вызовов этого узла в процессе формирования словаря (для последующего ускорения),
 а также свою коллекцию "ссылок" (номеров в массиве) на возможные вторые буквы.
Каждый Node второй буквы имеет свой список ссылок на третьи буквы, и.т.д.

Планируя логику поиска по словарю отметим, что в такой реализации для того чтобы определить какая буква лежит по ссылке (NodeLink)
 потребуется перейти по ней, поэтому имеет смысл хранить букву каждого Node вместе с массивом ссылок в вышестоящем узле.
 Кроме того планируется (для ускорения поиска необходимого слова в словаре) хранить ссылки на нижестоящие узлы в порядке убывания 
 частоты их использования, потому и этот параметр (количество вызовов данного узла) лучше тоже хранить в отдельном поле списка ссылок.
 Так получается, что если хранить узлы без дублирования информации, то каждый Node теперь представляет собой только список ссылок на 
 нижестоящие узлы в формате:Адрес, символ, повторяемость. Такая ссылка является экземпляром класса nodeLink.
Кроме того, такая реализация при разрешении неоднозначностей (при расстановке пробелов) позволит обойтись 
 без рекурсии поскольку можно будет хранить точки ветвления в отдельном массиве (позиции в тексте )

-Критерием последней буквы в слове будет считаться ссылка на узел с Char=' '.
-Адрес заданный int покрывает около 20Гб записей (NodeLink-ов) в данном формате.

Пути модификации:
1.Сделать общий интерфейс для всех словарей и только один метод для восстановления пробелов (на пример суперкласс - универсальный словарь)
2.Добавить в Класс словаря запреты на чтение по несуществующим адресам (и посмотреть на сколько упадет быстродействие).
3.В базовой реализации в начале списка существуют записи каждой буквы алфавита для первой буквы слова.
 Это позволяет интерпретировать букву как номер и обратиться по этому номеру в соответствующую ячейку массива.
 Если реализовать такой подход ("адресного доступа") для вторых (третьих,четвертых и.т.д.) букв каждого слова
 то можно получить ощутимый прирост скорости поиска нужного слова.
 Также можно реализовать tradeoff между необходимой памятью и скоростью поиска задавая "уровень адресного доступа":
 например, если вторая буква слова будет найдена не путем прохода списка Node а сразу по адресу,
 то нужно памяти не менее 33*34 = 1122 записи (где 33 - количество букв в алфавите, 34 - алфавите с пробелом)
 для реализации "адресного доступа третьего уровня"  нужно соответственно памяти не менее 33*34+33*33*34=38 148 записей.
 */
package sigmatask;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.function.Predicate;

public class SigmaTask {

    public static void main(String[] args) throws IOException{
        String unspacedText;    //Текст без пробелов и знаков препинания
        String resText;         //Целевой текст с расставленными пробелами
    String sourceText=SigmaTask.hDDLoadTxt("War_nPeace.txt"); //Загрузка текста из локального диска
    //String sourceText=SigmaTask.hDDLoadTxt("War_nPeace1.txt", "unicode");   //Вызов перегруженного метода для уточнения кодировки
    
    long elapsedTime;
    long startTime;     //Метки времени
    startTime = System.currentTimeMillis();
    TreeDict_Cyr ru_dct = new TreeDict_Cyr(sourceText);
    elapsedTime = System.currentTimeMillis()-startTime;
    System.out.print("Elapsed Time :"+elapsedTime+" ms\n");
   
    ru_dct.sortByAlphabet();
    //ru_dct.showDict();
    unspacedText=sourceText.toLowerCase();                    //Теперь в тексте только буквы нижнего регистра
    unspacedText=unspacedText.replaceAll("[^а-я^ё^a-z^0-9^]", "");   //Убрать знаки препинания, иностранный текст и символы переноса
    //unspacedText=sourceText.replaceAll("[^А-Я^а-я^ё^Ё^A-Z^a-z^0-9^]", "");   //Убрать знаки препинания, иностранный текст и символы переноса
            //System.out.println("Подготовлен текст:\n"+unspacedText);
    startTime = System.currentTimeMillis();
    resText=ru_dct.spaceRestore(unspacedText);
    elapsedTime = System.currentTimeMillis()-startTime;
    System.out.print("Elapsed Time :"+elapsedTime+" ms\n");
            //System.out.println("Обработан текст:\n"+resText);
    ru_dct.sortByDemand();
    startTime = System.currentTimeMillis();
    resText=ru_dct.spaceRestore(unspacedText);
    elapsedTime = System.currentTimeMillis()-startTime;
    System.out.print("Elapsed Time :"+elapsedTime+" ms\n");

    }
        
    public static String hDDLoadTxt(String path)throws IOException{ //Метод для загрузки текста из файла .txt с жесткого диска ПК
        int i;
        FileInputStream fileHandler =null;  //Инициализация идентификаторов потока для чтения из файла
        InputStreamReader fReader=null;
        StringBuilder sbText = new StringBuilder();    //Инициализация динамической строки (текстового контейнера)
     try{
         fileHandler=new FileInputStream(path);
         fReader= new InputStreamReader(fileHandler);   //Настройка потока
         String Enc=fReader.getEncoding();  //Получение текстового поля с наименованием кодировки
         while((i = fReader.read())!=-1) {  //Считывание файла (по два байта)
            sbText.append((char)i);
        }         
         System.out.println("Файл считан при помощи кодировки: "+Enc+"\nВсего символов:\t "+String.valueOf(sbText.length()));
     } catch (FileNotFoundException e){ //Исключение "файл не найден"
         System.out.println("Исключение:" + e + "\nпроверьте правильность пути");
         return "-1";
     } catch (IOException e) {  //Другие исключения (например отказ HDD)
         e.printStackTrace();
         return "-1";
     }finally{                  //Гарантированное закрытие потоков
         if (fReader !=null){
           fReader.close();           
         }
         if (fileHandler !=null){
           fileHandler.close();
         }
      return sbText.toString();
     }    
    }
    public static String hDDLoadTxt(String path, String encoding)throws IOException{
        int i;
        FileInputStream fileHandler =null;  //Инициализация идентификаторов потока для чтения из файла
        InputStreamReader fReader=null;
        StringBuilder sbText = new StringBuilder();    //Инициализация динамической строки (текстового контейнера)
     try{
         fileHandler=new FileInputStream(path);
         fReader= new InputStreamReader(fileHandler,encoding);   //Настройка потока
         String Enc=fReader.getEncoding();  //Получение текстового поля с наименованием кодировки
         while((i = fReader.read())!=-1) {  //Считывание файла (по два байта)
            sbText.append((char)i);
        }         
         System.out.println("Файл считан при помощи кодировки: "+Enc);
     } catch (FileNotFoundException e){ //Исключение "файл не найден"
         System.out.println("Исключение:" + e + "\nпроверьте правильность пути");
         return "-1";
     } catch (IOException e) {  //Другие исключения (например отказ HDD)
         e.printStackTrace();
         return "-1";
     }finally{                  //Гарантированное закрытие потоков
         if (fReader !=null){
           fReader.close();           
         }
         if (fileHandler !=null){
           fileHandler.close();
         }
      return sbText.toString();
     }    
    }

}
 class TreeDict_Cyr{
    private ArrayList<ArrayList<NodeLink>> treeDict; //Сам словарь
    private String RegexCharsOutOfAlphabet="[^а-я^ё]";  //regular expression для выделения всех символов не принадлежащих алфавиту
    private final int DICT_Q_LETTERS=33;     //Количество букв в алфавите
    private int processedWords;              //Размер выборки (слов) при формировании словаря
    private int containedWords;              //Количество слов в словаре
    private final int START_LETTER_UTF8=1072;           //Юникод буквы 'а'
    private final int FINISH_LETTER_UTF8=1103;          //Юникод буквы 'я'
    private final int OUT_OF_BOUNDS_LETTER_UTF8=1105;   //Юникод буквы 'ё'
    //private final int lstChar=
    TreeDict_Cyr(String ru_Str){
    treeDict = new ArrayList<>(DICT_Q_LETTERS); //По крайней мере 33 буквы в  _Ru алфавите
    int i, j, newAdr_toWrite, charExist, nextAdr_toFind;
    char ch;
    ru_Str=ru_Str.toLowerCase();                    //В алфавите только буквы нижнего регистра
    System.out.println("regex для символов не входящих в алфавит: "+RegexCharsOutOfAlphabet);
    ru_Str=ru_Str.replaceAll(RegexCharsOutOfAlphabet.replace("]", "^ ]"), " ");   //Убрать (заменить на пробелы) знаки препинания, иностранный текст и символы переноса
    ru_Str=ru_Str.replaceAll("\\s+", " ");          //Удалить повторяющиеся пробелы
    ru_Str+=" ";                //Последнее слово в тексте должно оканчиваться пробелом (поскольку это символ окончания слова)
                    //System.out.println("текст :\n"+ru_Str);
    char[] str=ru_Str.toCharArray();    //обучающий текст теперь массив char-ов
    for (i=0;i<DICT_Q_LETTERS;i++){     //Начальное наполнение словаря "буквами алфавита"
       treeDict.add(new ArrayList<>());
    }
                    //System.out.println("длина текста :"+Str.length);    
    processedWords=0;       //Наполнение (обучение) словаря:
    containedWords=0;
    int repeatedNodes=0;     //Счетчик повторного использования узлов
    newAdr_toWrite=DICT_Q_LETTERS;  //Следующий Node сразу после алфавита
    for (i=(str[0]==' ') ? 1 : 0;i<str.length-1;i++){   //Последовательная обработка самих слов (побуквенно). i - порядковый номер буквы в строке. Если в начале строки есть пробел он пропускается
        nextAdr_toFind=LetterNum(str,i);    //Адрес (в алфавите) первой буквы слова
        do{ //Цикл обработки первого (в первой итерации), а затем и остальных символов в текущем слове
            ch=str[i+1];    //Вторая (следующая) буква текущего слова
            charExist=-1;   //поиск первого (и единственного) вхождения ch в текущий Node данного слова
            for (j=0;j<treeDict.get(nextAdr_toFind).size();j++){    //Если нужный ch так и не будет найден, то charExist останется =-1
                if(treeDict.get(nextAdr_toFind).get(j).getLetter()==ch){
                    charExist=j;    //След. символ данного слова найден по адресу charExist в текущем Node-е
                    break;
                }
            }
            if(charExist!=-1){  //Если запись для след. символа в текущей последовательности (от начала слова) уже существует,
                        //System.out.println("посещено: "+String.valueOf(nextAdr_toFind)+" эта буква: '"+Str[i]+"' след. ch: '"+ch+"' ,адрес для следующей буквы: "+String.valueOf(treeDict.get(nextAdr_toFind).get(j).getAddress()));
                treeDict.get(nextAdr_toFind).get(charExist).add1Hit();    // то статистика "посещения" этого nodeLink-а будет обновлена
                if (ch!=' '){   //Если след. символ пробел, то nextAdr_toFind будет все равно переопределен в следующей итерации внешнего цикла
                    nextAdr_toFind=treeDict.get(nextAdr_toFind).get(charExist).getAddress();
                }
            }else{
                if (ch!=' '){  
                        //System.out.println("nextAdr_toFind: "+String.valueOf(nextAdr_toFind)+" эта буква: '"+Str[i]+"' след. ch: '"+ch+"' ,адрес для следующей буквы: "+Integer.toString(newAdr_toWrite));
                    treeDict.get(nextAdr_toFind).add(new NodeLink(ch,newAdr_toWrite));   //Добавление новой записи
                    treeDict.add(new ArrayList<>());     //Инициализация следующего Node
                    nextAdr_toFind=newAdr_toWrite++;    // в следующей итерации цикла именно он и будет обрабатываться
                            repeatedNodes++;
                }else{
                        //System.out.println("nextAdr_toFind: "+String.valueOf(nextAdr_toFind)+" эта буква: '"+Str[i]+"' след. ch: '"+ch+"' ,адрес для следующей буквы: "+Integer.toString(-1));
                    treeDict.get(nextAdr_toFind).add(new NodeLink(' ',-1));   //Добавление новой записи об окончании слова
                    containedWords++;   //Счётчик внесенных в словарь слов
                }   
            }
            i++;    //Внутренний инкремент цикла while (до конца слова)
        }while(ch!=' ');    // для пробела новый Node сформирован не будет
        processedWords++;   //Счётчик обработанных слов
    }
    System.out.println("Словарь сформирован:\nВсего обработано слов: "+String.valueOf(processedWords)+" из них оригинальных слов: "+String.valueOf(containedWords));
    System.out.println("Повторных проходов по узлам: "+String.valueOf(repeatedNodes));
    treeDict.toArray();     //Конвертация в статический массив
    this.sortByDemand();
    }
    
    public String spaceRestore(String srsTxt){
    StringBuilder resStr = new StringBuilder(srsTxt);   //Инициализация выходной строки
    srsTxt=srsTxt.toLowerCase();    //Приведение исходной строки в нижний регистр (запасное), т.к. словарь составлен в нижнем регистре
    String crntSubStr;  //Текущий фрагмент
    int finishInd=0, startInd=0;  //Адрес конца и начала выделяемого фрагмента текста.
    Pattern cyrOnlyP = Pattern.compile(this.getRegexCharsOutOfAlphabet());  //Regex для не Cyrilic алфавита (Шаблон), он будет 
    String noCyrRegEx;                                                      // использован для выделения русскоязычных фрагментов
    noCyrRegEx=this.getRegexCharsOutOfAlphabet().replaceAll("\\^", ""); //Regex для Cyrilic алфавита, он будет использован для выделения остальных фрагментов
    Pattern noCyrP = Pattern.compile(noCyrRegEx);   //Шаблон для выделения иностранных фрагментов
    char[] strChars;    //Переменные непосредственно для восстановления
    int l, i, crntNodeAdr, nextNodeAdr;
    int PassedWhiteSpacesOffset=0, BrPointsCreatedCntr=0, BrPointsUsedCntr=0;    //Счетчики пробелов, созданных точек ветвления и возвратов по ним соответственно
    char nextCh;
    Stack<Integer> BranchingPoints = new Stack<>(); //Массив, который содержит точки ветвления ("съезды") - положения в строке других возможных пробелов
    Stack<Integer> whiteSpcesAdr = new Stack<>();   //Массив адресов char-ов во фрагменте Str, за которыми будут вставлены пробелы
    while(startInd+finishInd<srsTxt.length()-1){
        startInd+=finishInd;     //Подготовка стартового индекса для выделение Cyrilic текста
        Matcher txt_Cyr = cyrOnlyP.matcher(srsTxt.substring(startInd)); //Инициализация Matcher-а для выделения Кириллицы
        if (txt_Cyr.find()){
            finishInd=txt_Cyr.start();
            //System.out.println("не Cyrilic текст с адреса: "+finishInd);
        }else{
            //System.out.println("Совпадение не найдено");
            finishInd=srsTxt.substring(startInd).length();    //Поиск может быть неуспешен при окончании входного текста
        }
        crntSubStr=srsTxt.substring(startInd, startInd+finishInd)+" ";   //Дополняем фрагмент пробелом в конце, поскольку это символ конца слова
                //System.out.println("фрагмент Ру_текста:\n"+crntSubStr);
        //Восстанавливаем пробелы в текущем фрагменте (crntSubStr):
        strChars=crntSubStr.toCharArray();    //Теперь работаем с массивом char-ов
        l=0;    //Адрес текущего char-а в строке (фрагменте)
        crntNodeAdr=-1; //Первая буква первого слова в строке (для неё будет определен порядковый номер в алфавите)
        while (l<strChars.length-1){
            if(crntNodeAdr==-1){
                crntNodeAdr=LetterNum(strChars,l);  //Адрес первой буквы нового слова.
            }
            nextCh=strChars[l+1];    //Следующая буква в строке
            nextNodeAdr=-2;     //-2 будет означать, что подходящий адрес для следующего узла так и не был найден
            for(i=0;i<treeDict.get(crntNodeAdr).size();i++){    //Цикл поиска в текущем Node следующей (в строке) буквы либо символа окончания слова
                if (treeDict.get(crntNodeAdr).get(i).getLetter()==nextCh||treeDict.get(crntNodeAdr).get(i).getLetter()==' '){
                    nextNodeAdr=treeDict.get(crntNodeAdr).get(i).getAddress() ;    //Найден следующий узел (если =-1  то это конец слова)
                    break;
                }            
            }   //Поскольку в Node (списке NodeLink-ов) пробел всегда хранится в конце списка, пробел может быть обнаружен только если подходящая буква отсутствует
            if (nextNodeAdr>=0){   //Поэтому проверяем наличие точки ветвтления только если в предыдущем цикле была найдена буква 
                if(treeDict.get(crntNodeAdr).get(treeDict.get(crntNodeAdr).size()-1).getLetter()==' '){ //т.к. если  цикл поиска дошел до конца и не обнаружив нужной буквы нашел пробел - это не точка ветвления
                    BranchingPoints.push(l);
                    BrPointsCreatedCntr++;      //Регистрация найденной точки ветвления           
                }
                crntNodeAdr=nextNodeAdr;    //Подготовка адреса Node для следующей итерации
                        //System.out.print(String.valueOf(strChars[l]));
            }else if (nextNodeAdr==-1){ //Если в словаре есть только символ окончания слова (пробел)
                        //System.out.print(String.valueOf(strChars[l])+"\n");
                whiteSpcesAdr.push(l);  //Учет нового потенциального пробела
                crntNodeAdr=-1; //следующая буква в строке будет обрабатываться как новая буква слова
            }else { //nextNodeAdr=-2 - в словаре нет ничего подходящего, значит возвращаемся к предыдущей точке ветвления
                crntNodeAdr=-1;     //Новый адрес "текущего" узла для следующей итерации соответствует пробелу
                l=BranchingPoints.pop();    //Возврат положения текущего символа в строке
                while (!whiteSpcesAdr.empty()&&whiteSpcesAdr.peek()>l){ //Убираем лишние пробелы, адрес которых старше нового положения текущего символа в строке
                    whiteSpcesAdr.pop();    //Удалять последний адрес пробела, пока не будет встречен предстоящий новому l
                }
                whiteSpcesAdr.push(l);  //Поскольку в точке ветвления хранятся только пробелы его надо зарегистрировать
                        //System.out.println(" Возврат на : l="+l+" \t ,буква "+strChars[l]+"_");
                BrPointsUsedCntr++; 
            }
            l++;
        }
        BranchingPoints.clear();    //Очистка стека для точек ветвления в пройденном (только-что) фрагменте.
        l=whiteSpcesAdr.size();     //Количество найденных пробелов (чтобы можно было отделить начало фрагмента)
        while(!whiteSpcesAdr.empty()){    //Вставка полученных пробелов в результирующую строку
            resStr.insert(startInd+whiteSpcesAdr.pop()+1+PassedWhiteSpacesOffset, ' '); 
        }
        resStr.insert(startInd+PassedWhiteSpacesOffset, ' ');     //Вставка пробела в начало обработанного фрагмента
        PassedWhiteSpacesOffset+=l+1;   //Обновление поправки на координату пробелов (для следующей итерации)
        startInd+=finishInd;    //Подготовка индекса начала (следующей) иностранной подстроки для новой итерации            
        Matcher txt_noCyr = noCyrP.matcher(srsTxt.substring(startInd)); //Инициализация Matcher-а для выделения "инородных" фрагментов текста
        if (txt_noCyr.find()){  //Поиск окончания фрагмента из символов не принадлежащих Cyrilic алфавиту
            finishInd=txt_noCyr.start();
                //System.out.println("Cyrilic текст с адреса: "+finishInd);
        }else{
                //System.out.println("Совпадение не найдено");
            finishInd=srsTxt.substring(startInd).length();    //Поиск может быть неуспешным при окончании входного текста
        }
    }
    System.out.println("Spacing is done \nПробелов расставлено: "+PassedWhiteSpacesOffset);
    System.out.println("Точек ветвления сформировано: "+BrPointsCreatedCntr+"\nИз них было использовано: "+BrPointsUsedCntr);
    return resStr.toString();   //Возврат обработанного текста в формате String
    }
    public int LetterNum(char[] ch,int i){  //Метод определяет порядковый номер буквы в алфавите для заданного элемента char-массива
        int alphabetAdr=Character.codePointAt(ch, i);
            if(alphabetAdr<=FINISH_LETTER_UTF8){ //Если первая буква принадлежит 'а'-'я' (без 'ё')
                alphabetAdr-=START_LETTER_UTF8;  //Порядковый номер буквы в алфавите (первый элемент алфавита имеет номер 0)
            }else{              //Иначе это буква 'ё'
                alphabetAdr=DICT_Q_LETTERS-1;    //здесь буква ё в конце алфавита
            }
        return alphabetAdr;
    }
    void showDictInsides(){
        System.out.println("Общий размер массива: "+String.valueOf(treeDict.size())+" записей");
        for (int i=0;i<treeDict.size();i++) {           //Итерации Node-ов
            System.out.println("Размер Node_List №: "+String.valueOf(i)+"\t составляет:"+String.valueOf(treeDict.get(i).size())+" записей nodeLink:");
            for(int j=0;j<treeDict.get(i).size();j++){  //Итерации строк nodeLink
                treeDict.get(i).get(j).showLink();
            }
        }
    }
    void showDict(){
        char[] alphabet = {'А','Б','В','Г','Д','Е','Ж','З','И','Й','К','Л','М','Н','О','П','Р','С','Т','У','Ф','Х','Ц','Ч','Ш','Щ','Ъ','Ы','Ь','Э','Ю','Я','Ё'};
        for(int l=0;l<DICT_Q_LETTERS;l++){
            System.out.println("слова на букву: "+alphabet[l]);
            String prefix=String.valueOf(alphabet[l]).toLowerCase();
            reсPrintCharacter(l, prefix);
        }
    }
    public void reсPrintCharacter(int id, String prefix){   //Рекурсивный метод для showDict (для обхода всех листьев дерева treeDict)
        for(int i=0;i<treeDict.get(id).size();i++){
            if(treeDict.get(id).get(i).getLetter()==' '){   //Печать происходит когда встречен пробел
                System.out.println(prefix);
            }
            else{   //При каждом переходе к нижестоящему узлу обновляется массив букв соответствующих вышестоящим узлам
                reсPrintCharacter(treeDict.get(id).get(i).getAddress(), prefix+String.valueOf(treeDict.get(id).get(i).getLetter()));
            }
                    
        }
    }
    void sortByDemand(){    //Сортировка по убыванию NodeLink.hits в каждом Node_List-е, НО символ пробела всегда в конце
        for(int i=0;i<treeDict.size();i++){
            Collections.sort(treeDict.get(i), (NodeLink one, NodeLink other) -> Integer.compare( (other.getAddress()!=-1)?other.getHits():0, (one.getAddress()!=-1)?one.getHits():0));
        }
    }
    void sortByAlphabet(){    //Сортировка в алфавитном порядка (по возрастанию юникода), НО символ пробела всегда в конце
        for(int i=0;i<treeDict.size();i++){     
          Collections.sort(treeDict.get(i), (NodeLink one, NodeLink other) -> Integer.compare( (one.getAddress()!=-1)?one.getLetter():65535 , (other.getAddress()!=-1)?other.getLetter():65535 ));
        }
    }
    String getRegexCharsOutOfAlphabet(){    //Возвращает регулярное выражение определяющее все символы не принадлежащие алфавиту
        return RegexCharsOutOfAlphabet;
    }
    char getChar(int nodeAdr , int linkAdr){    //Возвращает символ (letter) из указанного NodeLink-а
        return treeDict.get(nodeAdr).get(linkAdr).getLetter();
    }
    int getNextNodeAdr(int nodeAdr , int linkAdr){  //Возвращает адрес (следующего) Node записанный в поле address указанного NodeLink-а
        return treeDict.get(nodeAdr).get(linkAdr).getAddress() ;
    }
    class NodeLink {    //Экземпляр - запись конкретной буквы в конкретном слове из словаря (строка списка Node)
        private char letter;    
        private int address, hits;    
        NodeLink(char ch, int adr){
            letter = ch;    //Буква соответствующая нижестоящему узлу
            address = adr;   //Адрес нижестоящего узла в первом измерении treeDict
            hits = 1;       //и "актуальность" этого узла (количество обращений к этой ссылке в процессе построения словаря)
        }
        void showLink(){
            System.out.println("Id: "+address+"\t char:'"+letter+"' \t times used:"+hits);
        }
        char getLetter(){
            return letter;
        }
        int getHits(){
            return hits;
        }
        int getAddress(){
            return address;
        }
        void add1Hit(){
            hits++;
        }
    }
}

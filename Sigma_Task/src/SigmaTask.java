/*
Задачи:
1. Загрузить текст+
2. Сформировать из него словарь
3. Удалить из текста знаки припинания и пробелы+
4. Востановить пробелы при помощи словаря
Концепция:Самый быстрый способ поиска в массиве реализуем когда ключ может быть интерпретирован как адрес.
В контексте данной задачи, будет реализован словарь с древовидной структурой.
Технически это контейнер (тут, видимо коллекция) где каждый элемент является обьектом соответствующим букве из словаря.
Т.е., на пример, в русском алфавите 33 буквы - первые 33 значения из контейнера соотвтетствуют словам начинающимся на а,б, и.т.д
Под "значением" здесь понимается экземпляр класса "Node". Такой обьект содержит наименование буквы (char),
 количество вызовов этого узла в процессе формирования словаря (для последуюзего ускорения),
 а также ссвою коллекцию "ссылок" (номеров в массиве) на возможные вторые буквы.
Каждый Node второй буквы имеет свой список ссылок на третьи буквы.
Так в процессе поиска необходимого слова будет произведено Node-запросов (случайного чтения) по количеству букв в искомом слове.
При этом каждый Node-запрос сопровождается поиском лищь одной буквы в небоьшом подмассиве возможных ссылок.
Набранная статистика позволяет отсортировать дерево от каждого узла в порядке убывания вроятности.
Кроме того такая реализация при разрешении неоднозначностей (при расстановке пробелов) позволит обойтись 
 без рекурсии поскольку можно будет хранить точки ветвления в отдельном массиве (позиция в тексте+адрес в словаре)
Рекурсия номинально означает экспоненциальный рост времени обработки, что на практике приводит к частым 
 системным вызовам (смена контекста+копирование переменных в памяти+неспешная работа менеджера памяти ОС),
 а в клинических случаях возможно и переполнение стека (но это совсем писсимистичный взгляд).

Критерием последней буквы в слове будет считаться ссылка на узел с Char=" "
 */
package sigmatask;
import java.io.*;
import java.util.*;
/**
 *
 * @author Liolik
 */
public class SigmaTask {

    public static void main(String[] args) throws IOException{
     int i=0;
     String sourceText=SigmaTask.HDDLoadTxt("War_.txt"); //Загрузка текста из локального диска
     //String sourceText=SigmaTask.HDDLoadTxt("War_.txt", "windows-1251");   //Вызов перегруженного метода для уточнения кодировки
        //System.out.println("Считан текст:\n"+sourceText);
     String unSpasedTxt=sourceText.replaceAll("[^a-z^A-Z^а-я^А-Я^0-9^ ^\n]", ""); //Удаление символов не входящих в Алфавит
     System.out.println("Удалено символов пунктуации:\t"+String.valueOf(sourceText.length()-unSpasedTxt.length()));
     unSpasedTxt=unSpasedTxt.replaceAll("\n"," ");      //Готово для обучения словаря
     //   String[] words=unSpasedTxt.split(unSpasedTxt);
    //    System.out.println("Подготовлен текст:\n"+unSpasedTxt);
    
    int randomInt, tmp, S=100000; //Количество тестовых итераций
    int[] arr=new int [S];
    int[] rand=new int [S];
    ArrayList aLst = new ArrayList();
    LinkedList lLst = new LinkedList();
    Random randomGenerator = new Random();  //Генератор случайных чисел
    long startTime = System.currentTimeMillis();
    for(i=0;i<S;i++){
        arr[i]=i;
    }
    long elapsedTime = System.currentTimeMillis()-startTime;
    System.out.println("Последовательная запись int[] :"+elapsedTime+" миллисекунд");
    for(i=0;i<S;i++){rand[i] = randomGenerator.nextInt(S);} //Массив случайных запросов
    
    startTime = System.currentTimeMillis();
    for(i=0;i<S;i++){
        aLst.add(i);
    }
    elapsedTime = System.currentTimeMillis()-startTime;
    System.out.println("Последовательная запись ArrayList :"+elapsedTime+" миллисекунд");
    
    startTime = System.currentTimeMillis();
    for(i=0;i<S;i++){
        lLst.add(i);
    }
    elapsedTime = System.currentTimeMillis()-startTime;
    System.out.println("Последовательная запись LinkedList :"+elapsedTime+" миллисекунд");
    
    startTime = System.currentTimeMillis();
    for(i=0;i<S;i++){
        tmp=arr[rand[i]];
    }
    elapsedTime = System.currentTimeMillis()-startTime;
    System.out.println("Случайное чтение int[] :"+elapsedTime+" миллисекунд");
        
    Object tmpO;
    startTime = System.currentTimeMillis();
    for(i=0;i<S;i++){
        tmpO=aLst.get(rand[i]);
    }
    elapsedTime = System.currentTimeMillis()-startTime;
    System.out.println("Случайное чтение ArrayList :"+elapsedTime+" миллисекунд");
        
    startTime = System.currentTimeMillis();
    for(i=0;i<S;i++){
        tmpO=lLst.get(rand[i]);
    }
    elapsedTime = System.currentTimeMillis()-startTime;
    System.out.println("Случайное чтение LinkedList :"+elapsedTime+" миллисекунд");

     /*for (i=0;i<65536;i++){ //Полная таблица символов текущей кодировки (UTF-08)
     char tmp=(char)i;
     System.out.println("для символа: "+String.valueOf(tmp)+" Номер в текущей кодировке: "+String.valueOf(i));
     }*/

     
    }
    public static String HDDLoadTxt(String path)throws IOException{
        int i;
        FileInputStream fileHandler =null;  //Инициализайция идентефикаторов потока для чтения из файла
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
    public static String HDDLoadTxt(String path, String encoding)throws IOException{
        int i;
        FileInputStream fileHandler =null;  //Инициализайция идентефикаторов потока для чтения из файла
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

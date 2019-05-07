import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.Timer;

/**
 * LexMain 该类负责实现线程规格说明的的词法分析
 * @author YangJian
 */
public class LexMain {

    private int error_state = 1000;
    private int line_count=0;//用于标记当前读入的是第几行
    private File inFile;
    private PrintWriter fout;
    private static String CHANGE_LINE="\r\n";


    public static void main(String[] args){
        if(args.length==0){
            System.out.println("Please enter the parameter");
            System.exit(-1);
        }else {
            new LexMain().run(args);
        }
    }

    private void run(String[] args) {
        try {
            doInit(Integer.parseInt(args[0]));
            Scanner reader = new Scanner(new FileInputStream(inFile));
            while(reader.hasNextLine()){//用循环来逐行读入测试文件并开始词法分析
                char[] elements = reader.nextLine().toCharArray();
                line_count++;
                if(elements.length==0) continue;
                startAnalysis(elements);
            }
            fout.println("Analysis is completed");
            fout.println();
            fout.flush();
            fout.close();
            reader.close();
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    /**
     * doInit 该函数负责在程序最开始运行的时候处理输入，输出文件
     * 主要判断输出文件是否存在，若不存在，则创建
     * 打开输出文件后向其输入一行当前时间戳以及本次运行要执行的文件名
     * @param mod 该变量主要用于标识本次运行读取哪个文件，1为读取test1, 2为读取test2， 依次类推，0为读取其他文件
     */
    private void doInit(int mod) {
        try {
            switch (mod) {
                case 0:
                    inFile = new File("resource/test0.txt");
                    break;
                case 1:
                    inFile = new File("resource/test1.txt");
                    break;
                case 2:
                    inFile = new File("resource/test2.txt");
                    break;
                case 3:
                    inFile = new File("resource/test3.txt");
                    break;
                default:
                    System.out.println("Please enter the correct parameter type");
                    break;
            }
            File outFile = new File("result/tokenOut.txt");
            if(!outFile.exists()){
                outFile.createNewFile();
            }
            fout = new PrintWriter(new FileWriter(outFile,true));
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            fout.print(df.format(new Date())+" WriteLog: test"+mod+CHANGE_LINE);
            fout.flush();
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    /**
     * startAnalysis 该函数负责处理从文件中读入的已经转换成字符数组的一行
     * @param elements 需要处理的字符数组
     */
    private void startAnalysis(char[] elements) {
        int index = 0;
        while(index<elements.length){//利用循环来遍历读入的整个字符数组
            char element = elements[index];//取当前index的元素
            if(element==' ' || element=='\r' || element=='\n' || element=='\t'){//如果是空格，换行符，制表符则忽略
                index++;
            }else if(element==';' || element=='{' || element=='}'){//如果是上述三个专用符号，调用judgeSingal并返回index
                index=judgeSignal(index,elements);
            }else if(element=='='){//如果是等号调用judgeEqual并返回index
                index=judgeEqual(index,elements);
            }else if(element=='+'){//如果是加号调用judgePlus并返回index
                index=judgePlus(index,elements);
            }else if(element==':'){//如果是冒号调用judgeColon并返回index
                index=judgeColon(index,elements);
            }else if(element=='-'){//如果是减号调用judgeMinus并返回index
                index=judgeMinus(index,elements);
            }else if(element>=48 && element<=57){//如果是数字调用judgeDecimal并返回index
                index=judgeDecimal(index,elements);
            }else if((element>=65 && element<=90) || (element>=97 && element<=122)){//如果是a-z 或者A-Z，调用judgeIdentifier并返回index
                index=judgeIdentifier(index,elements);
            }else {
                judgeError(index,elements);
            }
        }
    }

    /**
     * judgeError 用于判断在开头出现的错误类型
     * @param index 字符位于的下标位置
     * @param elements 字符数组
     */
    private void judgeError(int index,char[] elements){
        if(elements[index]=='.'){
            error_state=1003;
            doErrorLog(error_state);
        }else if(elements[index]=='_'){
            error_state=1002;
            doErrorLog(error_state);
        }else{
            error_state = 1001;
            doErrorLog(error_state);
        }
    }

    /**
     * doErrorLog 该函数负责写入错误日志
     * @param error_state 传入的错误详情，用于区别写入的日志类型，共五种，1001,1002,1003,1004,1111
     *                   写入完成之后控制台输出错误信息并退出程序
     */
    private void doErrorLog(int error_state) {
        switch (error_state){
            case 1001:
                fout.println("Line:"+line_count+" ErrorCode:"+error_state+" UnknownIdentifier");
                fout.println();
                fout.flush();
                fout.close();
                System.err.println("Analysis error, ErrorCode:"+error_state);
                System.exit(-1);
            case 1002:
                fout.println("Line:"+line_count+" ErrorCode:"+error_state+" WrongTypeIdentifier");
                fout.println();
                fout.flush();
                fout.close();
                System.err.println("Analysis error, ErrorCode:"+error_state);
                System.exit(-1);
            case 1003:
                fout.println("Line:"+line_count+" ErrorCode:"+error_state+" InvalidDecimal");
                fout.println();
                fout.flush();
                fout.close();
                System.err.println("Analysis error, ErrorCode:"+error_state);
                System.exit(-1);
            case 1004:
                fout.println("Line:"+line_count+" ErrorCode:"+error_state+" InvalidSpecialIdentifier");
                fout.println();
                fout.flush();
                fout.close();
                System.err.println("Analysis error, ErrorCode:"+error_state);
                System.exit(-1);
            case 1111:
                fout.println("Line:"+line_count+" ErrorCode:"+error_state+" UnknownError");
                fout.println();
                fout.flush();
                fout.close();
                System.err.println("Analysis error, ErrorCode:"+error_state);
                System.exit(-1);
            default:
                System.exit(-1);
        }
    }

    /**
     * judgeSingal 该函数处理对于";","{","}"的判断
     * @param index 字符位于的下标位置
     * @param elements 字符数组
     * @return 返回处理完成后的index值
     */
    private int judgeSignal(int index,char[] elements){
        fout.print(elements[index++]+CHANGE_LINE);
        fout.flush();//重刷缓冲区以达成写入目的
        return index;
    }

    /**
     * judgePlus 该函数用于判断+号和后面的是否合法，考虑到+=>和+123.01都是合法的，所以情况会有些复杂
     * @param index 字符位于的下标位置
     * @param elements 字符数组
     * @return 返回处理完成后的index值
     */
    private int judgePlus(int index,char[] elements){
        try {
            if (elements[index+1] == '='){
                fout.print(elements[index++]);//+号后面肯定会有其他后续输出，故不带换行
                fout.flush();
                index = judgeEqual(index,elements);
                return index;
            }else if(elements[index+1]>=48 && elements[index+1]<=57){
                fout.print(elements[index++]);
                fout.flush();
                //index = judgeDecimal(index,elements);
                return index;
            }else if(elements[index+1]=='.'){//+.123不是错误的语言专用符而是非法浮点数，单独标记
                error_state = 1003;
                doErrorLog(error_state);
            }else{
                error_state = 1004;
                doErrorLog(error_state);
            }
        }catch (ArrayIndexOutOfBoundsException diob){
            error_state = 1004;
            doErrorLog(error_state);
        }catch (Exception e){
            error_state = 1111;
            doErrorLog(error_state);
        }
        return index;
    }

    /**
     * judgeMinus 该函数用于判断+号和后面的是否合法，考虑到->和-123.01都是合法的，所以情况会有些复杂
     * @param index 字符位于的下标位置
     * @param elements 字符数组
     * @return 返回处理完成后的index值
     */
    private int judgeMinus(int index,char[] elements){
        try {
            if (elements[index+1] == '>'){
                StringBuilder result = new StringBuilder();
                result.append(elements[index++]);
                result.append(elements[index++]);
                fout.print(result.toString() + CHANGE_LINE);
                fout.flush();
                return index;
            }else if(elements[index+1]>=48 && elements[index+1]<=57){
                fout.print(elements[index++]);
                fout.flush();
                //index = judgeDecimal(index,elements);
                return index;
            }else if(elements[index+1]=='.'){//+.123不是错误的语言专用符而是非法浮点数，单独标记
                error_state = 1003;
                doErrorLog(error_state);
            }else{
                error_state = 1004;
                doErrorLog(error_state);
            }
        }catch (ArrayIndexOutOfBoundsException diob){
            error_state = 1004;
            doErrorLog(error_state);
        }catch (Exception e){
            error_state = 1111;
            doErrorLog(error_state);
        }
        return index;
    }

    /**
     * judgeEqual 该函数用于判断识别到‘=’号的情况，正确则写入，错误则写入错误日志
     * @param index 字符位于的下标位置
     * @param elements 字符数组
     * @return 返回处理完成后的index值
     */
    private int judgeEqual(int index,char[] elements){
        try {
            if (elements[index + 1] != '>') {
                error_state = 1004;
                doErrorLog(error_state);
                return index;
            } else {
                StringBuilder result = new StringBuilder();
                result.append(elements[index++]);
                result.append(elements[index++]);
                fout.print(result.toString() + CHANGE_LINE);
                fout.flush();
                return index;
            }
        }catch (ArrayIndexOutOfBoundsException aiob){//=有可能是该行最后一个元素，故访问elements[index+1]会产生越界
            error_state = 1004;
            doErrorLog(error_state);
        }catch (Exception e){
            error_state = 1111;
            doErrorLog(1111);
        }
        return index;
    }

    /**
     * judgeColon 该函数用来判断':'的合法性，由于"::"和':'都是合法的，所以要分别判断，且取最长匹配原则
     * @param index 字符位于的下标位置
     * @param elements 字符数组
     * @return 返回处理完成后的index值
     */
    private int judgeColon(int index,char[] elements){
        try{
            if(elements[index+1]==':'){
                StringBuilder result = new StringBuilder();
                result.append(elements[index++]);
                result.append(elements[index++]);
                fout.print(result.toString()+CHANGE_LINE);
                fout.flush();
                return index;
            }else {
                fout.print(elements[index++]+CHANGE_LINE);
                fout.flush();
                return index;
            }
        }catch (ArrayIndexOutOfBoundsException diob){
            fout.print(elements[index++]+CHANGE_LINE);
            fout.flush();
            return index;
        }catch (Exception e){
            error_state = 1111;
            doErrorLog(error_state);
        }
        return index;
    }

    /**
     * judgeIdentifier 该函数用于判断identifier是否合法,注意对于x$y判断的是unknownIdentifier不是WrongTypeIdentifier
     * @param index 字符位于的下标位置
     * @param elements 字符数组
     * @return 返回处理完成后的index值
     */
    private int judgeIdentifier(int index,char[] elements){
        try{
            StringBuilder result = new StringBuilder();
            result.append(elements[index++]);
            while (index<elements.length){
                //这个if判断有点复杂，解释一下
                //当前字符既不是数字，也不是大小写字母,也不是下划线的时候，就该结束了
                if(elements[index]!='_' &&(elements[index]<48 || elements[index]>122
                    || (elements[index]>57 && elements[index]<65)
                    || (elements[index]>90 && elements[index]<97))){
                    break;
                }
                if(elements[index]=='_' && elements[index+1]=='_'){//如果出现连续两个下划线，肯定非法
                    error_state = 1002;
                    doErrorLog(error_state);
                    break;
                }
                result.append(elements[index++]);
            }
            fout.print(result.toString()+CHANGE_LINE);
            fout.flush();
            return index;

        }catch (ArrayIndexOutOfBoundsException aiob){//能进到这里只有一种可能,最后一个字符是下划线，肯定是非法类型
            error_state = 1002;
            doErrorLog(error_state);
        }catch (Exception e){
            error_state = 1111;
            doErrorLog(error_state);
        }
        return index;
    }

    /**
     * judgeDecimal 该函数用来判断浮点数是否合法
     * @param index 字符位于的下标位置
     * @param elements 字符数组
     * @return 返回处理完成后的index值
     */
    private int judgeDecimal(int index,char[] elements){
        StringBuilder result = new StringBuilder();
        int point_cnt = 0;//该变量用于标记当前读入了多少个'.’
        result.append(elements[index++]);
        while(index<elements.length){
            if((elements[index]<48 || elements[index]>57) && elements[index]!='.'){//不是数字也不是'.',退出
                break;
            }
            if(elements[index]=='.'){
                if(point_cnt!=0){//这是第二次出现的'.'了，肯定有问题
                    break;
                }
                point_cnt++;
            }
            result.append(elements[index++]);
        }
        if(result.lastIndexOf(".")==(result.length()-1) || result.lastIndexOf(".")==-1){
            error_state = 1003;
            doErrorLog(error_state);
        }
        fout.print(result.toString()+CHANGE_LINE);
        fout.flush();
        return index;
    }
}

package main.kotlin;

import com.shengsiyuan.kotlin12.MyClass3;

import java.io.FileNotFoundException;

public class HelloJava4 {

    public static void main(String[] args) {
        MyClass3 myClass3 = new MyClass3();

        try {
            myClass3.method();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }

        System.out.println("hello world");
    }
}

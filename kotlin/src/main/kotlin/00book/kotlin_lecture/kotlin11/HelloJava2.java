package main.kotlin;

import com.shengsiyuan.kotlin11.HelloKotlin2Kt;
import com.shengsiyuan.kotlin11.MyClass;

public class HelloJava2 {

    public static void main(String[] args) {
        // 我们无法通过new关键字来创建Kotlin编辑器自动生成的以Kt结尾的类的实例
        // 因为在生成的字节码中没有不带参数的构造方法
//        HelloKotlin2Kt helloKotlin2Kt = new HelloKotlin2Kt();

        MyClass myClass = new MyClass();

        HelloKotlin2Kt.setStr("welcome");
        System.out.println(HelloKotlin2Kt.getStr());

        HelloKotlin2Kt.test();
    }
}

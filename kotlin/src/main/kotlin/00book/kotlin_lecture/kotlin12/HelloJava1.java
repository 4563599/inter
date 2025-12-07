package main.kotlin;

import com.shengsiyuan.kotlin12.HelloKotlin1Kt;

import java.util.ArrayList;
import java.util.List;

public class HelloJava1 {

    public static void main(String[] args) {
        List<String> list = HelloKotlin1Kt.myFilter(new ArrayList<>());
        System.out.println(list);

        List<Integer> list2 = HelloKotlin1Kt.myFilter2(new ArrayList<>());
        System.out.println(list2);
    }
}

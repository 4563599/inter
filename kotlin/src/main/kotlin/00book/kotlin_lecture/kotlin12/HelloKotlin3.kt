package com.shengsiyuan.kotlin12

/*
    MyClass2(int x, String y)
    MyClass2(int x)
 */


class MyClass2 @JvmOverloads constructor(x: Int, y: String = "hello") {

    fun myMethod(a: Int, b: String, c: Int = 2) {
        println("a: $a, b: $b, c: $c")
    }
}
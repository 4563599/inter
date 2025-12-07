package com.shengsiyuan.kotlin12

class MyClass {

    val a: Int
        @JvmName("getAValue")
        get() = 20

    fun getA() = 30
}

fun main(args: Array<String>) {
    val myClass = MyClass()

    println(myClass.getA())
    println(myClass.a)
}
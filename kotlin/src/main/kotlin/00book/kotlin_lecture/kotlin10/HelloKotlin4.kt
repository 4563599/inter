package com.shengsiyuan.kotlin10

fun main(args: Array<String>) {
    val myException = MyException()
//    myException.myMethod()

    println("------------")

    val clazz = MyException()::class.java
    println(clazz)

    println("------------")

    println(MyException().javaClass)
}
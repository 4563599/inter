package com.shengsiyuan.kotlin13

import kotlin.reflect.full.memberFunctions

class MyTestClass3 {

    fun printSomething() {
        println("something")
    }

    fun printNothing() {
        println("")
    }
}

fun main(args: Array<String>) {
    val myTestClass3 = MyTestClass3::class
    println(myTestClass3.memberFunctions)
}
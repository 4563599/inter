package com.shengsiyuan.kotlin13

import kotlin.reflect.full.memberProperties

class MyTestClass6 {
    var name: String = "hello world"
}

fun main(args: Array<String>) {
    val myTestClass6 = MyTestClass6::class
    var testClass6 = MyTestClass6()

    var variableToInvoke = myTestClass6.memberProperties.find { it.name == "name" }

    println(variableToInvoke?.get(testClass6))
    println(variableToInvoke?.call(testClass6))
}
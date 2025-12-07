package com.shengsiyuan.kotlin13

import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties

class MyTestClass7 {
    var name: String = "hello world"
    var authorName: String = "tom"
}

fun main(args: Array<String>) {
    val myTestClass7 = MyTestClass7::class
    var testClass7 = MyTestClass7()

    var variableToInvoke = myTestClass7.memberProperties.find { it.name == "name" }
    println(variableToInvoke?.get(testClass7))

    if (variableToInvoke is KMutableProperty<*>) {
        variableToInvoke.setter.call(testClass7, "welcome")
    }

    println(variableToInvoke?.get(testClass7))
}
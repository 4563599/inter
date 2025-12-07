package com.shengsiyuan.kotlin13

import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaGetter
import kotlin.reflect.jvm.javaSetter


class MyTestClass14 {
    var name = "Java"
    val price = 22.3
}

var myTest = "myTest"

fun main(args: Array<String>) {
    val topProp = ::myTest
    println(topProp.javaField)

    println(topProp.javaGetter)
    println(topProp.javaSetter)

    val n = MyTestClass14::name
    println(n.javaField)
    println(n.javaGetter)
    println(n.javaSetter)

    val prop = MyTestClass14::price
    println(prop.javaField)
    println(prop.javaGetter)
}
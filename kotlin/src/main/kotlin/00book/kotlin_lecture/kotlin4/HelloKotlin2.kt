package com.shengsiyuan.kotlin4

import kotlin.reflect.KProperty

// 委托属性（delegated property）
/*
    有4种情况在实际开发中比较有用：

    1. 可延迟属性。
    2. 观测属性
    3. 非空属性
    4. map属性
 */

class MyDelegate {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): String = "$thisRef, your delegated property name is ${property.name}"

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) = println("$thisRef, new value is $value")
}


class MyPropertyClass {
    var str: String by MyDelegate()
}

fun main(args: Array<String>) {
    val myPropertyClass = MyPropertyClass()

    myPropertyClass.str = "hello world"
    println(myPropertyClass.str)
}
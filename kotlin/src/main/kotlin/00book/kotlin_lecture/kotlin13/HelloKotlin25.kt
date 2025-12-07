package com.shengsiyuan.kotlin13


class MyTestClass13 {
    var name: String = "Spark"
    val price = 20.3
}

var test = "test"

fun main(args: Array<String>) {
    val topProp = ::test

    topProp.set("修改后的值")

    println(topProp.get())
    println(test)

    val myTestClass13 = MyTestClass13()
    var name = MyTestClass13::name

    name.set(myTestClass13, "Ruby")
    println(name.get(myTestClass13))

    val prop = MyTestClass13::price
    println(prop.get(myTestClass13))
}
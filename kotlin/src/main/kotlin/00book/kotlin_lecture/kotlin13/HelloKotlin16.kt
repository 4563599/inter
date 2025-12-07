package com.shengsiyuan.kotlin13

class MyTestClass4(value: Int) {

    constructor(amount: Int, color: String): this(amount) {
        println("secondary constructor")
    }

    constructor(amount: Int, full: Boolean): this(amount) {
        println("secondary constructor")
    }

    fun printSomething() {
        println("something")
    }
}

fun main(args: Array<String>) {
    val myTestClass4 = MyTestClass4::class
    val constructors = myTestClass4.constructors

    println(constructors)
}

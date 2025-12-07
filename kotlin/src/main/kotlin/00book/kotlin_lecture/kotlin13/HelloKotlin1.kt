package com.shengsiyuan.kotlin13


fun main(args: Array<String>) {
    val c = String::class //KClass
    println(c)

    println("-------")

    val c2 = String::class.java
    println(c2)
}
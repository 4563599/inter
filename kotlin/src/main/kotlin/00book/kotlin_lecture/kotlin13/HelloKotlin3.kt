package com.shengsiyuan.kotlin13

/*
    函数（方法）引用

    支持重载

    ::multiplyBy3表示函数类型 (Int) -> Int 的值
 */

fun multiplyBy3(x: Int): Int {
    return 3 * x
}

fun multiplyBy3(s: String): Int {
    return 10
}

fun main(args: Array<String>) {
    val values = listOf(1, 2, 3, 4)
    println(values.map(::multiplyBy3))

    println("-------")

    val values2 = listOf("a", "b", "c", "d")
    println(values2.map(::multiplyBy3))
}


val myReference: (Int) -> Int = ::multiplyBy3
val myReference2: (String) -> Int = ::multiplyBy3

val myReference3: String.(Int) -> Char = String::get
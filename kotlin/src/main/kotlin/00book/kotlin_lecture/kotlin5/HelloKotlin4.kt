package com.shengsiyuan.kotlin5

/*
    内联函数（inline function）

 */

inline fun myCalculate(a: Int, b: Int) = a + b

fun main(args: Array<String>) {
    println(myCalculate(1, 2))
}
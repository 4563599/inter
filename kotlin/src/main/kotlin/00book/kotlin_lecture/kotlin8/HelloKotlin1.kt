package com.shengsiyuan.kotlin8

import java.lang.Integer.parseInt

// 异常，kotlin中的try是个表达式
// Kotlin中是没有checked exception的

fun main(args: Array<String>) {
    val s = "a"

    val result: Int? = try {
        parseInt(s)
    } catch (ex: NumberFormatException) {
        null
    } finally {
        println("finally invoked")
    }

    println(result)
}
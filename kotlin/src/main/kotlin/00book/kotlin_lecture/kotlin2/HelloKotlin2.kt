package com.shengsiyuan.kotlin2

/*
    密封类（sealed class）
 */

sealed class Calculator

class Add: Calculator()

class Subtract: Calculator()

class Multiply: Calculator()

fun calculate(a: Int, b: Int, cal: Calculator) = when (cal) {
    is Add -> a + b
    is Subtract -> a - b
    is Multiply -> a * b
}

fun main(args: Array<String>) {
    println(calculate(1, 2, Add()))
    println(calculate(1, 2, Subtract()))
    println(calculate(1, 2, Multiply()))
}

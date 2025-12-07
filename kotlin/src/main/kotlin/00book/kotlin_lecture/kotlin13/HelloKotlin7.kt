package com.shengsiyuan.kotlin13

/*
    扩展属性
 */

val String.firstChar: Char
    get() = this[0]

fun main(args: Array<String>) {
    println(String::firstChar.get("xyz"))
}
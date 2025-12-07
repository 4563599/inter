package com.shengsiyuan.kotlin5

fun main(args: Array<String>) {
    val strings = arrayOf("hello", "world", "bye", "helloworlD", "welcome")

    strings.filter { it.contains("h") }.forEach{ println(it) }

    println("--------")

    strings.filter{ it.length > 4 }.forEach { println(it) }

    println("--------")

    strings.filter { it.endsWith("d", ignoreCase = true) }.map { it.toUpperCase() }.forEach { println(it) }
}
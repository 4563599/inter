package com.shengsiyuan.coroutines

import kotlin.concurrent.thread

fun main() {
    thread {
        Thread.sleep(1000)
        println("Kotlin Coroutines")
    }

    println("Hello")

    Thread.sleep(2000)

    println("World")
}
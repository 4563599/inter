package com.shengsiyuan.coroutines

import kotlin.concurrent.thread

fun main() {
    repeat(5000) {
        thread {
            Thread.sleep(1000)
            println("A")
        }
    }

    println("Hello World")
}
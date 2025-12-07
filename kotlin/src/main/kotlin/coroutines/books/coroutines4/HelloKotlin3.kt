package com.shengsiyuan.coroutines4

import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

/*
    使用JVM参数：-Dkotlinx.coroutines.debug
 */

private fun log(logMessage: String) = println("[${Thread.currentThread().name}] $logMessage")


fun main() = runBlocking<Unit> {
    val a = async {
        log("hello world")
        10
    }

    val b = async {
        log("welcome")
        20
    }

    log("The result is ${a.await() * b.await()}")
}
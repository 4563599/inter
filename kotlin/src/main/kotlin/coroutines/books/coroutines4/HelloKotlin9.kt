package com.shengsiyuan.coroutines4

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/*
    使用JVM参数：-Dkotlinx.coroutines.debug
 */

fun main() = runBlocking<Unit>(CoroutineName("mycoroutine")) {
    launch(Dispatchers.Default + CoroutineName("helloworld")) {
        println("thread: ${Thread.currentThread().name}")
    }
    println("thread: ${Thread.currentThread().name}")
}
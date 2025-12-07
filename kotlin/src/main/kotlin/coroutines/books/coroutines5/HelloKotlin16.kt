package com.shengsiyuan.coroutines5

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

/*
    Buffer（缓冲）


 */

private fun myMethod(): Flow<Int> = flow {
    for (i in 1..4) {
        delay(100) // 模拟了一个较为耗时的操作，该操作之后才开始流元素的发射
        emit(i)
    }
}

fun main() = runBlocking<Unit> {
    val time = measureTimeMillis {
        myMethod().collect { value ->
            delay(200)
            println(value)
        }
    }

    println(time)
}



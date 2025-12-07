package com.shengsiyuan.coroutines2

import kotlinx.coroutines.*
import java.util.concurrent.CancellationException

/*
    协程的取消与超时
 */

fun main() = runBlocking {
    val myJob = GlobalScope.launch {
        repeat(200) { i ->
            println("hello: $i")
            delay(500)
        }
    }

    delay(1100)
    println("hello world")

//    myJob.cancel(CancellationException("just a try"))
//    myJob.join()

    myJob.cancelAndJoin()

    println("welcome")
}
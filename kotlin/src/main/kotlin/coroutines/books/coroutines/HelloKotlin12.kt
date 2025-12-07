package com.shengsiyuan.coroutines

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/*
    全局协程类似于守护线程（daemon thread）

    使用GlobalScope启动的活动协程并不会保持进程的生命，他们就像是守护线程一样
 */

fun main() {
    GlobalScope.launch {
        repeat(100) { i ->
            println("I am sleeping $i ")

            delay(200)
        }
    }

    Thread.sleep(2000)
}
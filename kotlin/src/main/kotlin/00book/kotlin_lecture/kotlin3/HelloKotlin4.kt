package com.shengsiyuan.kotlin3

class OuterClass4 {

    class NestedClass4 {
        init {
            println("NestedClass4构造块执行")
        }
    }
}

fun main(args: Array<String>) {
    val nestedClass4: OuterClass4.NestedClass4 = OuterClass4.NestedClass4()
}
package com.shengsiyuan.kotlin

fun main(args: Array<String>) {
    println(convert2Uppercase("hello world"))
    println(convert2Uppercase(23))
}

fun convert2Uppercase(str: Any): String? {
    if(str is String) {
        return str.toUpperCase()
    }

    return null
}




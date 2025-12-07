package com.shengsiyuan.kotlin

class CompanionObjectExtension {
    companion object MyObject{

    }
}

fun CompanionObjectExtension.MyObject.method() {
    println("hello world")
}

fun main(args: Array<String>) {
    CompanionObjectExtension.method()
}

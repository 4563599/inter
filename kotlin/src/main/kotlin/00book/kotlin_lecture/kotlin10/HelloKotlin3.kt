package com.shengsiyuan.kotlin10


fun main(args: Array<String>) {
    val myVarargs = MyVarargs()
    val stringArrays = arrayOf("hello", "world", "hello world")

    myVarargs.myMethod(*stringArrays)  // spread operation *
}
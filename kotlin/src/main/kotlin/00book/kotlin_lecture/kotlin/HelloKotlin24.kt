package com.shengsiyuan.kotlin

class MyExtensionProperty

val MyExtensionProperty.name: String
    get() = "hello"

fun main(args: Array<String>) {
    var myExtensionProperty = MyExtensionProperty()
    println(myExtensionProperty.name)
}
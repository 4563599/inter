package com.shengsiyuan.kotlin13

import kotlin.reflect.full.memberProperties

class MyTestClass2(var a: String, val flag: Boolean, var age: Int) {

}


fun main(args: Array<String>) {
    val myTestClass2 = MyTestClass2::class
    println(myTestClass2.memberProperties)
}
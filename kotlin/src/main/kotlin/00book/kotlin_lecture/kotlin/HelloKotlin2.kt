package com.shengsiyuan.kotlin

//import com.shengsiyuan.kotlin2.multiply
import com.shengsiyuan.kotlin2.multiply as myMultiply

fun main(args: Array<String>) {
    val a: Int = 1
    val b = 2

    var c: Int
    c = 3

    var d = 3
    d = 4

    println(d)

    /**
     *
     */

    /*
    /*

     */
     */

    var x = 10
    var y: Byte = 20

    x = y.toInt()

    println(x)

    println(myMultiply(2, 3))

    val m = intArrayOf(1, 2, 3)
//    m = intArrayOf(4, 5)

    m.set(0, 4)

    for(item in m) {
        println(item)
    }

}
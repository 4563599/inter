package com.shengsiyuan.kotlin2

// 泛型函数

fun <T> getValue(item: T): T {
    return item
}

fun main(args: Array<String>) {
    val item = getValue<Int>(3)
    println(item)

    val item2 = getValue("hello")
    println(item2)

    val upperBounds = UpperBoundsClass2<String>()

}

class UpperBoundsClass<T: List<T>> {

}

class UpperBoundsClass2<T> where T: Comparable<T>, T: Any {

}


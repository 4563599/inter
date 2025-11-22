package `01sugar`

import kotlin.collections.iterator

fun main() {
    val mapData = mapOf("a" to 1, "b" to 2)
    for ((k, v) in mapData) {        // 语法糖：for 解构
        println("$k -> $v")
    }
}

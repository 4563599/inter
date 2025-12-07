package com.shengsiyuan.kotlin5

fun String.filter(predicate: (Char) -> Boolean): String {
    val sb = StringBuilder()

    for (index in 0 until length) {
        val element = get(index)

        if (predicate(element)) {
            sb.append(element)
        }
    }

    return sb.toString()
}

fun main(args: Array<String>) {
    val str = "hell7worl9"

    println(str.filter { it.isLetter() })
}








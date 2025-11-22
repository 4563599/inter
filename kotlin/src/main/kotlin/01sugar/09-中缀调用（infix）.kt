package `01sugar`

infix fun Int.add(x: Int) = this + x // 语法糖：infix 中缀函数定义

fun main() {
    val sum = 1 add 2                    // 语法糖：中缀调用
    println("sum=$sum")

    val map = mapOf(1 to "one", 2 to "two") // 语法糖：中缀 `to`
    println(map)
}

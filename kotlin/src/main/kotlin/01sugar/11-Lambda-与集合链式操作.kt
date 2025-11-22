package `01sugar`

fun main() {
    val nums = listOf(-1, 0, 3, 5)
    val doubledPositive = nums
        .filter { it > 0 }   // 语法糖：尾随 lambda + 单参数 it
        .map { it * 2 }      // 语法糖：链式高阶函数
    println(doubledPositive) // [6, 10]
}

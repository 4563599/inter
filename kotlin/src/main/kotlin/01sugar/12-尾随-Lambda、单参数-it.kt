package `01sugar`

fun main() {
    run { println("尾随 lambda，无需括号") } // 语法糖：尾随 lambda
    val nums = listOf(-1, 0, 3, 5)
    val positives = nums.filter { it > 0 } // 语法糖：单参数 it
    println(positives)
}

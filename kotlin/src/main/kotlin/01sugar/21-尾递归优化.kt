package `01sugar`

tailrec fun fact(n: Int, acc: Long = 1): Long = // 语法糖：tailrec 尾递归优化
    if (n <= 1) acc else fact(n - 1, n * acc)

fun main() {
    println(fact(5)) // 120
}

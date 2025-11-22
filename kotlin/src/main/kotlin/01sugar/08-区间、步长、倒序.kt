package `01sugar`

fun main() {
    for (i in 1..3) print("$i ")        // 语法糖：闭区间 1..3
    println()
    for (i in 5 downTo 1 step 2) print("$i ") // 语法糖：downTo + step
    println()
    if (3 in 1..5) println("in range")        // 语法糖：in 区间检查
}

package `01sugar`

fun describe(x: Any): String = when (x) { // 语法糖：when 表达式可返回值
    0, 1 -> "zero or one"
    in 2..9 -> "digit"                     // 语法糖：范围匹配
    is String -> "string len=${x.length}"  // 语法糖：类型匹配
    else -> "other"
}

fun main() {
    val samples: List<Any> = listOf(0, 5, "kotlin", 99.9)
    samples.forEach { println("$it -> ${describe(it)}") }
}

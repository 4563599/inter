package whenexample

/**
 * Kotlin when 简明示例：三个场景覆盖最常用写法。
 */
fun main() {
    println("=== 1. 匹配常量 / 范围 / 类型 ===")
    val values: List<Any?> = listOf(null, 0, 5, "kotlin", 12)
    values.forEach { value ->
        val message = when (value) {
            null -> "值是 null"
            0, 1 -> "是 0 或 1"
            in 2..9 -> "位于 2..9"               // 使用 in 判断区间
            is String -> "字符串：长度=${value.length}" // 自动转成 String
            is Int -> if (value % 2 == 0) "偶数" else "奇数"
            else -> "其他类型"
        }
        println("when($value) -> $message")
    }

    println("\n=== 2. when 作为表达式 ===")
    val score = 75
    
    val grade = when {
        score >= 90 -> "A"        // 无参数 when，条件成立即返回
        score >= 60 -> "B"
        else -> "C"
    }
    println("score=$score -> grade=$grade")

    println("\n=== 3. when 替代 if-else ===")
    val status = 404
    val action = when (status) {
        200 -> "OK"
        in 300..399 -> "重定向"
        in 400..499 -> "客户端错误"
        in 500..599 -> "服务端错误"
        else -> "未知状态"
    }
    println("status=$status -> action=$action")
}

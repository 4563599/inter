package `01sugar`

fun risky(ok: Boolean): String = if (ok) {
    "ok"
} else {
    throw RuntimeException("nope")
}

fun main() {
    val text = try {              // 语法糖：try 也是表达式
        risky(false)
    } catch (e: Exception) {
        "failed: ${e.message}"
    }
    println(text)
}

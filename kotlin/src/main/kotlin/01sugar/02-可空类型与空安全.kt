package `01sugar`

fun main() {
    var s: String? = null
    val len = s?.length              // 语法糖：安全调用
    val lenOrZero = s?.length ?: 0   // 语法糖：Elvis 运算符
    println("len=$len, lenOrZero=$lenOrZero")

    s = "kotlin"
    println("after set, len=${s?.length}")
    // val crash = s!!.length        // 语法糖：非空断言
    println(convert2Int(""))
}

fun convert2Int(str: String): Int? {
    try {
        return str.toInt()
    } catch (ex: NumberFormatException) {
        return null
    }

}

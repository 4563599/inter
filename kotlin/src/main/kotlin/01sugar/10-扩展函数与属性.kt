package `01sugar`

fun String.titleCase(): String = replaceFirstChar { it.uppercase() } // 语法糖：扩展函数
val String.lastChar: Char get() = this[lastIndex] // 语法糖：扩展属性

fun main() {
    println("hello".titleCase())
    println("kotlin".lastChar)
}

package `01sugar`

val appName = "Demo"          // 语法糖：顶层属性
fun topHello() = println(appName) // 语法糖：顶层函数

fun main() {
    topHello()
}

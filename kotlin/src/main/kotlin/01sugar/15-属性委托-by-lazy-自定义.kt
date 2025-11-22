package `01sugar`

val expensive: String by lazy { // 语法糖：属性委托 by lazy
    println("once only")
    "result"
}

fun main() {
    println(expensive) // 触发计算
    println(expensive) // 复用缓存，不再计算
}

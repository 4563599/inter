 package `01sugar`

data class Money(val amount: Int) {
    operator fun plus(other: Money) = Money(amount + other.amount) // 语法糖：运算符重载
}

fun main() {
    val total = Money(5) + Money(7) // 使用自定义 +
    println(total)
}

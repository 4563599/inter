package `01sugar`

@JvmInline value class Email(val value: String) // 语法糖：值类（内联类）
fun send(to: Email) = println("01sugar.send to ${to.value}")

fun main() {
    send(Email("a@b.com"))
}

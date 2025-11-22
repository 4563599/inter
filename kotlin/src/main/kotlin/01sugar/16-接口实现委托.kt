package `01sugar`

interface Logger { fun log(msg: String) }
class ConsoleLogger : Logger { override fun log(msg: String) = println(msg) }
class Service(private val impl: Logger) : Logger by impl // 语法糖：接口实现委托

fun main() {
    Service(ConsoleLogger()).log("hello")
}

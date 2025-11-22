package `01sugar`

fun greet(to: String, msg: String = "Hi", vararg tags: String) = // 语法糖：默认参数 + vararg
    println("$msg $to, tags=${tags.joinToString()}")

fun main() {
    greet("Amy")                            // 语法糖：默认参数
    greet(msg = "Hello", to = "Bob")        // 语法糖：命名参数
    greet("Cat", "Yo", "kotlin", "fun")     // 语法糖：vararg 展开
}

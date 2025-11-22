package `01sugar`

data class User(val id: Int, val name: String) // 语法糖：数据类

fun main() {
    val u = User(1, "Ana")
    println(u)                     // 语法糖：自动 toString
    val (id, n) = u                // 语法糖：解构声明
    println("id=$id, 01sugar.name=$n")
    val u2 = u.copy(name = "Ben")  // 语法糖：copy
    println(u2)
}

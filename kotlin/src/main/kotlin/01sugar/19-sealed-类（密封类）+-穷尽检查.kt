package `01sugar`

sealed class Result           // 语法糖：sealed 限定继承
data class Ok(val data: String) : Result()
data class Err(val msg: String) : Result()

fun handle(r: Result) = when (r) { // 语法糖：sealed + when 穷尽，无需 else
    is Ok -> "data=${r.data}"
    is Err -> "error=${r.msg}"
}

fun main() {
    println(handle(Ok("hi")))
    println(handle(Err("oops")))
}

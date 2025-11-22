package `01sugar`

data class Person(var name: String, var age: Int)

fun main() {
    val p = Person("Ana", 18).apply { // 语法糖：apply 作用域函数
        age = 19
    }

    p.also { println("logging $it") }  // 语法糖：also 作用域函数
    val lenOrNull = p.name.let { it.length } // 语法糖：let 返回结果

    val label = with(p) { "$name-$age" }     // 语法糖：with
    val runResult = p.run { "$name is $age" } // 语法糖：run

    println("lenOrNull=$lenOrNull, label=$label, runResult=$runResult")
}

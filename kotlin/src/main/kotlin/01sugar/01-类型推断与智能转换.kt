val name = "Tom"          // 语法糖：类型推断
var count = 1             // 语法糖：类型推断

fun printLength(x: Any) {
    if (x is String) {    // 语法糖：智能转换
        println("string len=${x.length}")
    } else {
        println("not a string: $x")
    }
}

fun main() {
    printLength(name)
    printLength(123)
    count += 1
    println("count=$count")

    println(sum(2, 3))
    sum3(4,5);
}

fun sum(a: Int, b: Int) = a + b // 语法糖：单表达式函数

fun sum2(a: Int, b: Int): Int { // 普通函数定义
    return a + b
}

fun sum3(a: Int, b: Int): Int { // 普通函数定义
    println("$a + $b" + " = ${a + b}")
    return a + b;
}




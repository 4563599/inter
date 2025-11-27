package com.example.androiddemo.kotlin.Lambda

/**
 * 【小白版】Kotlin Lambda 表达式超详细入门
 *
 * Kotlin 中的 Lambda，本质上是一个可以像变量一样传递的 "代码块" 或 "匿名函数"。
 * 它的目标是让代码更简洁、更灵活。
 *
 * 我们将通过几个简单的步骤，从最原始的写法演进到最简洁的写法。
 */

// 这是一个程序的入口。你可以点击 fun main() 左边的绿色三角形按钮来运行这段代码。
// 运行后，在下方的 Run 窗口中查看打印结果。
fun main() {
    println("======== 第 1 步：我们的目标是什么？ ========")
    // 我们的目标是：定义一个功能，它接收两个整数，返回它们的和。
    // 我们先定义一个接口，来描述这个“功能”的规范。

    val cal = { a: Int, b: Int -> a + b }
    println(cal(1, 2));


    println("\n======== 第 2 步：没有 Lambda 时的“笨重”写法 ========")

    println("\n======== 第 3 步：Lambda 表达式的诞生！ ========")

    println("\n======== 第 4 步：把 Lambda 作为参数传递给函数 ========")
    println(calculate(10, 20, { a: Int, b: Int -> a - b }))

    println("\n======== 第 5 步：Kotlin 的终极语法糖 - 尾随 Lambda ========")
    val result = test(3, 5) { x, y ->
        x * y
    }
    println(result)

    println("\n======== 第 6 步：只有一个参数时的超级简化 - it ========")
}

//operation是参数  (Int, Int) -> Int是函数类型
private fun calculate(a: Int, b: Int, operation: (Int, Int) -> Int): Int {
    return operation(a, b);
}

private fun test(a: Int = 1, b: Int = 2, compute: (x: Int, y: Int) -> Int): Int {
    return compute(a, b)
}


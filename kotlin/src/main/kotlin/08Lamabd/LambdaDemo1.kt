package com.example.androiddemo.kotlin.Lambda

// region 知识点 1: 默认参数 (Default Arguments)
// 我们可以给函数的参数指定一个默认值。
// 如果调用函数的时候不传这个参数，它就会使用这个默认值。
fun test(a: Int = 0, b: Int = 1) = println("a - b = ${a - b}")
// endregion

// region 知识点 5: 重写方法与默认参数
// 这是父类
open class A {
    // 父类方法可以有默认参数
    open fun method(a: Int, b: Int = 4) = a + b
}

// 这是子类，继承自 A
class B: A() {
    // **重点**：当子类重写父类的方法时，签名必须完全匹配，
    // 不能再写默认值 `b: Int = 4` 了，但这个默认值效果依然存在。
    override fun method(a: Int, b: Int) = a + b
}
// endregion

// region 知识点 2: 默认参数的特殊情况
// 如果一个带默认值的参数 (a) 在一个没有默认值的参数 (b) 的前面，
// 那么在调用时，如果你想使用 a 的默认值，就必须用“具名参数”的方式来给 b 传值。
fun test2(a: Int = 1, b: Int) = println("a - b = ${a - b}")
// endregion

// region 知识点 3: 高阶函数 - 接收 Lambda 作为参数
// 一个函数可以接收另一个函数作为参数，这叫“高阶函数”。
// `compute` 参数的类型是 `(Int, Int) -> Unit`，
// 意思是它接收一个“需要两个Int参数、没有返回值”的函数。
fun test3(a: Int = 1, b: Int = 2, compute: (x: Int, y: Int) -> Unit) {
    compute(a, b)
}
// endregion

// region 知识点 4: 具名参数与位置参数
// 这是一个普通的函数，有一些必需参数 (a, d) 和一些默认参数 (b, c)。
fun test4(a: Int, b: Int = 2, c: Int = 3, d: Int) = println("a+b+c+d = ${a + b + c + d}")

// 函数重载：创建一个同名但参数不同的函数
// `vararg` 关键字表示这是一个“可变参数”，可以接收0个或多个 String。
fun test4(vararg strings: String) {
    println("传递了 ${strings.size} 个字符串:")
    strings.forEach { println(it) }
}
// endregion

/**
 * 这是程序的入口，用来演示上面定义的各种函数的调用方法。
 */
fun main() {
    println("======== 知识点 1: 默认参数与具名参数 ========")
    // 调用 test 函数的几种方式
    test()           // a=0, b=1 (全部使用默认值)
    test(2)       // a=2, b=1 (按顺序传参，第一个参数给了 a，b 使用默认值)
    test(b = 2)      // a=0, b=2 (使用“具名参数”，明确指定只给 b 传值，a 使用默认值)
    test(3, 2)    // a=3, b=2 (按顺序给 a 和 b 传值)
    test(a = 3)      // a=3, b=1 (使用“具名参数”，明确指定只给 a 传值，b 使用默认值)

    println("\n======== 知识点 5: 继承中的默认参数 ========")
    println("A().method(1) = ${A().method(1)}") // 输出 1 + 4 = 5
    println("B().method(1) = ${B().method(1)}") // B 继承了 A 的默认值，同样输出 1 + 4 = 5

    println("\n======== 知识点 2: 默认参数顺序问题 ========")
    // test2(3) // 这样写是错的！编译器不知道 3 是给 a 还是 b。
    test2(b = 3)     // 必须使用具名参数，明确告诉编译器 3 是给 b 的。a 会使用默认值 1。

    println("\n======== 知识点 3: 传递 Lambda 的三种方式 ========")
    // 这是这个例子中关于 Lambda 的核心知识点！

    // **方式一：函数引用 (Function Reference)**
    // 如果你有一个已经存在的、并且签名匹配的函数（比如我们定义的 test 函数），
    // 可以使用 `::` 符号把它当作一个“值”传进去。
    test3(2, 3, ::test)

    // **方式二：在括号内传递 Lambda 表达式**
    // 这是最标准的写法，直接在括号里写一个 Lambda 表达式。
    test3(2, 3, {a, b -> println("a - b = ${a - b}")})

    // **方式三：尾随 Lambda (Trailing Lambda)**
    // **Kotlin 最重要的语法糖！** 如果 Lambda 是函数的【最后一个参数】，
    // 你可以把它从括号 `()` 里拿出来，直接写在后面。代码更清晰！
    test3(2, 3) { a, b -> println("a - b = ${a - b}") }

    // **尾随 Lambda + 默认参数 的强大组合**
    // 我们可以省略掉 b，只提供 a，b 会使用默认值 2。
    test3(2) { a, b -> println("a - b = ${a - b}") }
    // 甚至可以把 a 和 b 全都省略掉，它们都会使用默认值。
    test3 { a, b -> println("a - b = ${a - b}") }

    println("\n======== 知识点 4: 具名参数的灵活性 ========")
    test4(1, 2, 3, 4) // 按位置顺序传参
    test4(a = 1, b = 2, c = 3, d = 4) // 全部使用具名参数，顺序可以任意
    test4(a = 1, d = 5) // 只传必需的参数，让 b 和 c 使用默认值，非常方便！
    // **规则**：如果混用位置和具名参数，所有位置参数必须在第一个具名参数之前。
    // test4(a = 1, 5) // 这是错误的！

    println("\n======== `vararg` 可变参数与 `*` 展开操作符 ========")
    test4("hello", "kotlin", "world") // 直接传递多个参数

    // 如果你已经有了一个数组，想把它传给 vararg 函数，怎么办？
    val stringArray = arrayOf("a", "b", "c")
    // test4(stringArray) // 直接传数组是错的！
    // **必须用 `*` (展开操作符 Spread Operator) **
    // `*` 的作用是把数组的每个元素“打散”，变成独立的参数再传进去。
    test4(*stringArray)

    
}

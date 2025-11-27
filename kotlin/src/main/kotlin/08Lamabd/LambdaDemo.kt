package com.example.androiddemo.kotlin.Lambda

/**
 * 【小白版】Kotlin Lambda 语法超详细拆解
 *
 * 目标：把 Lambda 表达式的完整形态，一步步简化，理解每个部分的作用。
 */
class LambdaDemo {

    fun runAllSteps() {
        step1_TheFullName()
        step2_SimplifyTheBody()
        step3_SimplifyTheVariable()
        step4_SingleParameterIt()
        step5_PassLambdaToFunction()
        step6_TrailingLambdaSyntax()
    }

    // 为了后面的演示，我们先准备一个接收 Lambda 的函数
    private fun calculate(a: Int, b: Int, operation: (Int, Int) -> Int): Int {
        return operation(a, b)
    }

    /**
     * 第 1 步：Lambda 的“全名”，最繁琐但最完整的形态。
     * Lambda 就是一个没有名字的函数。我们可以把它存到一个变量里。
     */
    private fun step1_TheFullName() {
        // 语法拆解：
        // 1. `val addFunction:`: 我们在定义一个变量，名字叫 addFunction。
        // 2. `(Int, Int) -> Int`: 这是变量的“类型”。它表示这个变量能存放一个“函数”。
        //    - `(Int, Int)`: 表示这个函数接收两个 Int 类型的参数。
        //    - `-> Int`: 表示这个函数会返回一个 Int 类型的值。
        // 3. `= { ... }`: 我们把一个 Lambda 表达式赋值给这个变量。
        // 4. `a: Int, b: Int`: 在 Lambda 的函数体 `{}` 内部，我们声明了它接收的参数和类型。
        // 5. `-> a + b`: `->` 右边是函数体，也就是具体的实现。这里就是把 a 和 b 加起来。
        //    最后一行表达式的结果就是 Lambda 的返回值。
        val addFunction: (Int, Int) -> Int = { a: Int, b: Int -> a + b }

        println("[Step1] 最完整的 Lambda 写法: " + addFunction(5, 3))
    }

    /**
     * 第 2 步：简化函数体。
     * 当变量已经声明了类型，编译器足够聪明，能推断出 Lambda 参数的类型。
     *
     * 定义多参数Lambda变量时：第2种写法 val name: (Type) -> Return = { ... } 是最简单且最推荐的
     */
    private fun step2_SimplifyTheBody() {
        // 对比第 1 步：
        // 左边的变量类型 `(Int, Int) -> Int` 依然存在。
        // 因为编译器从这里已经知道，这个 Lambda 的两个参数必须是 Int，返回值也必须是 Int。
        // 所以，右边 `{}` 里的 `a: Int, b: Int` 就可以简化成 `a, b`，省略类型声明。
        val addFunction: (Int, Int) -> Int = { a, b -> a + b }

        println("[Step2] 省略 Lambda 参数类型: " + addFunction(5, 3))
    }

    /**
     * 第 3 步：反过来，简化变量。
     * 如果我们在 Lambda 函数体里提供了完整的参数类型，编译器也能反推出变量的类型。
     */
    private fun step3_SimplifyTheVariable() {
        // 对比第 1 步：
        // 我们省略了变量的类型 `: (Int, Int) -> Int`。
        // 这时，为了让编译器知道这个 Lambda 是干嘛的，我们必须在函数体 `{}` 里写上完整的参数类型 `a: Int, b: Int`。
        // 编译器看到 `{ a: Int, b: Int -> ... }`，就会自动推断出 `addFunction` 变量的类型是 `(Int, Int) -> Int`。
        val addFunction = { a: Int, b: Int -> a + b }

        println("[Step3] 省略变量类型: " + addFunction(5, 3))
        // 注意：第2步和第3步的简化，你只能选择一种！类型信息必须在等号的某一侧提供。
    }

    /**
     * 第 4 步：特殊情况，当 Lambda 只有一个参数时，可以用 `it`。
     */
    private fun step4_SingleParameterIt() {
        // 这是一个接收一个 Int，返回一个 Int 的 Lambda。
        // 按照第 2 步的简化规则，我们可以写成这样：
        val square: (Int) -> Int = { num -> num * num }
        println("[Step4] 单参数的普通写法: ${square(5)}")

        // Kotlin 规定：对于只有一个参数的 Lambda，你可以完全省略参数声明（`num ->` 这部分），
        // 然后在函数体内用一个默认的名字 `it` 来代表这个唯一的参数。
        val squareWithIt: (Int) -> Int = { it * it }
        println("[Step4] 单参数的 'it' 写法: ${squareWithIt(5)}")
    }

    /**
     * 第 5 步：把 Lambda 当作参数传给一个函数。
     */
    private fun step5_PassLambdaToFunction() {
        // 回忆一下我们准备的 `calculate` 函数。它第 3 个参数需要一个 `(Int, Int) -> Int` 类型的函数。
        // 我们可以直接把一个 Lambda 写在括号里传进去。
        val result = calculate(10, 5, { a, b -> a - b })
        println("[Step5] 把 Lambda 写在括号里传递: $result")
    }

    /**
     * 第 6 步：尾随 (Trailing) Lambda 语法，一种更清爽的写法。
     */
    private fun step6_TrailingLambdaSyntax() {
        // Kotlin 规则：当 Lambda 表达式是函数的【最后一个参数】时，
        // 你可以把它从括号 `()` 里拿出来，直接写在括号后面。
        // 这仅仅是一种写法的优化，为了代码更好看，功能和第 5 步完全一样！
        
        // 对比第 5 步：
        // `calculate(10, 5, { ... })` 变成了 `calculate(10, 5) { ... }`
        val result = calculate(10, 5) { a, b -> a - b }
        println("[Step6] 使用尾随 Lambda 写法: $result")
    }
}

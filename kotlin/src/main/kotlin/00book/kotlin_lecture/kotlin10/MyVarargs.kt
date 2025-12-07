package com.shengsiyuan.kotlin10

/**
 * MyVarargs 类 - 演示可变参数（vararg）的使用
 * 
 * 可变参数允许函数接受任意数量的参数
 * 在 Kotlin 中使用 vararg 关键字声明
 */
class MyVarargs {
    
    /**
     * 演示可变参数的使用
     * 
     * @param strings 可变数量的字符串参数
     * 
     * 使用方式：
     * 1. 直接传入多个参数：myMethod("a", "b", "c")
     * 2. 使用展开操作符传入数组：myMethod(*array)
     * 
     * 展开操作符（spread operator）：
     * - 使用 * 将数组展开为独立的参数
     * - 例如：*arrayOf("a", "b") 等价于 "a", "b"
     */
    fun myMethod(vararg strings: String) {
        println("接收到 ${strings.size} 个参数:")
        
        // 遍历所有参数
        strings.forEachIndexed { index, value ->
            println("  参数 $index: $value")
        }
        
        println()
        
        // vararg 参数在函数内部是一个数组
        println("vararg 参数的类型: ${strings::class.simpleName}")
        println("vararg 参数是数组: ${strings is Array<*>}")
    }
}

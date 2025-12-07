package com.shengsiyuan.kotlin10

/**
 * MyException 类 - 演示异常处理和类引用
 * 
 * 这个类用于演示：
 * 1. Kotlin 中的异常处理
 * 2. 获取类的 Class 对象（Java 反射）
 * 3. ::class.java 和 javaClass 的使用
 */
class MyException {
    
    /**
     * 演示异常抛出的方法
     * 
     * 在 Kotlin 中：
     * - 所有异常都是非检查异常（unchecked）
     * - 不需要在方法签名中声明 throws
     * - 可以选择性地捕获异常
     */
    fun myMethod() {
        // 抛出一个运行时异常
        throw RuntimeException("这是一个示例异常")
    }
    
    /**
     * 演示异常处理的方法
     * 
     * @param shouldThrow 是否抛出异常
     */
    fun safeMethod(shouldThrow: Boolean = false) {
        if (shouldThrow) {
            throw IllegalArgumentException("参数错误")
        } else {
            println("方法正常执行")
        }
    }
    
    /**
     * 演示 try-catch 的使用
     */
    fun tryCatchExample() {
        try {
            myMethod()
        } catch (e: RuntimeException) {
            println("捕获到异常: ${e.message}")
        } finally {
            println("finally 块总是会执行")
        }
    }
}

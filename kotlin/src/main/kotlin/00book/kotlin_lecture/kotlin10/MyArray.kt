package com.shengsiyuan.kotlin10

/**
 * MyArray 类 - 用于演示数组操作
 * 
 * 这个类展示了如何在 Kotlin 中处理原生类型数组（IntArray）
 * 避免了自动装箱和拆箱的性能开销
 */
class MyArray {
    
    /**
     * 演示原生类型数组的使用
     * 
     * @param array IntArray 类型的数组（不是 Array<Int>）
     * 
     * IntArray vs Array<Int>:
     * - IntArray: 原生类型数组，对应 Java 的 int[]，性能更好
     * - Array<Int>: 对象数组，对应 Java 的 Integer[]，有装箱开销
     */
    fun myArrayMethod(array: IntArray) {
        println("数组长度: ${array.size}")
        println("数组内容:")
        
        // 遍历数组并打印每个元素
        for (element in array) {
            println("  元素值: $element")
        }
    }
}

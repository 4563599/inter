package `07extension`

/**
 * 这个例子演示了 Kotlin 强大的扩展（Extension）功能。
 * 扩展可以让你在不修改源码、不使用继承的情况下，为一个已有的类添加新的函数或属性。
 */

// --- 1. 扩展函数 (Extension Function) ---

// 我们来为 String 类添加一个新函数 `lastChar()`
// fun String.lastChar(): Char 表示我们正在给 String 类添加一个名为 lastChar 的函数。
// 在函数内部，`this` 关键字指向调用该函数的 String 对象实例。
fun String.lastChar(): Char {
    // `this` 就是调用这个函数的字符串本身，比如 "Kotlin"
    return this[this.length - 1]
}

// 再来一个例子：为 Int 类添加一个判断是否是偶数的方法
fun Int.isEven(): Boolean {
    return this % 2 == 0
}


// --- 2. 扩展属性 (Extension Property) ---

// 我们也可以为类添加新的属性。注意：扩展属性不能有“幕后字段”(backing field)，
// 所以它必须定义 getter，并且不能有 initializer。
// 我们为 String 添加一个 `isLong` 属性，判断其长度是否大于10。
val String.isLong: Boolean
    get() = this.length > 10


fun main() {
    println("--- 测试扩展函数 ---")
    val myString = "Hello, Kotlin"
    // 看，我们可以像调用 String 自己的方法一样，调用我们新增的 lastChar()
    println("字符串 \"$myString\" 的最后一个字符是: ${myString.lastChar()}")

    val number = 101
    println("数字 $number 是偶数吗？ ${number.isEven()}")
    println("数字 ${number + 1} 是偶数吗？ ${(number + 1).isEven()}")


    println("\n--- 测试扩展属性 ---")
    val shortString = "short"
    val longString = "This is a very long string"

    // 像访问普通属性一样，访问我们新增的 isLong 属性
    println("字符串 \"$shortString\" 是长的吗？ ${shortString.isLong}")
    println("字符串 \"$longString\" 是长的吗？ ${longString.isLong}")
}

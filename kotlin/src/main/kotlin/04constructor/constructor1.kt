package constructorinfo

/**
 * Kotlin 构造方法备忘：主构造、init、次构造、委托、访问控制、继承调用等示例。
 */
fun main() {

}


open class Parent2(name: String) {

}

class Child2 : Parent2 {
    constructor(name: String) : super(name) {

    }
}

open class Fruit {
    open fun name() {
        println("fruit")
    }
}

class Apple : Fruit() {
    override fun name() {
        println("apple")
    }
}
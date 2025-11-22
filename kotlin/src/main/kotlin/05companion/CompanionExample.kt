package `05companion`

/**
 * 这个例子用来解释Kotlin的伴生对象 (Companion Object)
 */

// 定义一个接口
interface Factory<T> {
    fun create(): T
}

class MyClass private constructor(val name: String) { // 构造方法私有化，强制使用伴生对象来创建实例

    // 这就是伴生对象，它属于MyClass类本身，而不是MyClass的任何一个实例
    // 你可以把它想象成 MyClass 的一个单例“伴侣”
    companion object MyFactory : Factory<MyClass> {
        // 伴生对象可以有自己的属性，这就像Java的 static final 变量
        const val TAG = "MyClass"

        // 伴生对象的方法，可以像Java的 static 方法一样被调用
        fun newInstance(name: String): MyClass {
            println("在伴生对象中调用方法: newInstance")
            return MyClass(name)
        }

        // 因为伴生对象是真正的对象，所以它可以实现接口！
        // 这是 static 无法做到的
        override fun create(): MyClass {
            println("在伴生对象中调用实现的接口方法: create")
            return MyClass("DefaultName")
        }
    }
}

fun main() {
    // 1. 像调用静态方法一样，直接通过类名调用伴生对象的方法
    // 这是最常见的用法，完全替代了Java的静态工厂方法
    val instance1 = MyClass.newInstance("Test1")
    println("创建了实例: ${instance1.name}")

    println("--------------------")

    // 2. 也可以通过伴生对象的名字来调用 (如果起了名字的话)
    val instance2 = MyClass.MyFactory.create()
    println("创建了实例: ${instance2.name}")

    println("--------------------")

    // 3. 像访问静态属性一样，直接通过类名访问伴生对象的属性
    val tag = MyClass.TAG
    println("访问伴生对象的属性: $tag")

    println("--------------------")

    // 4. 证明伴生对象是一个真正的对象：我们可以把它赋值给一个变量
    val factory: Factory<MyClass> = MyClass.MyFactory
    val instance3 = factory.create()
    println("通过赋值给变量后创建了实例: ${instance3.name}")
}

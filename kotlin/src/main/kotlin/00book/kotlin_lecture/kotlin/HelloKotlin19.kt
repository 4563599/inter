package com.shengsiyuan.kotlin

// object declaration，对象声明

object MyObject {
    fun method() {
        println("method")
    }
}

fun main(args: Array<String>) {
    MyObject.method()

    println("-------")

//    MyTest.MyObject.method()

    println("-------")

    MyTest.method() // 类似于静态方法，Kotlin中没有静态方法
    println(MyTest.a)

    println("-------")

    val v = MyTest.MyObject
    println(v.javaClass)

    println("-------")

    D.foo()
    D.bar()

    D.Companion.foo()
    D.Companion.bar()

}

// companion object ，伴生对象
// 在Kotlin中，与Java不同的是，类是没有static方法的。
// 在大多数情况下，Kotlin推荐的做法是使用包级别的函数来作为静态方法
// Kotlin会将包级别的函数当做静态方法来看待

// 如果不提供伴生对象的名字，那么编译器会提供一个默认的名字Companion

// 注意：虽然伴生对象的成员看起来像是Java中的静态成员，但在运行期，他们依旧是真实对象的实例成员
// 在JVM上，可以将伴生对象的成员真正生成为类的静态方法与属性，这是通过@JvmStatic注解来实现的
// 伴生对象在编译后会生成一个静态内部类

class MyTest {

    companion object MyObject {

        var a: Int = 100

        @JvmStatic
        fun method() {
            println("method invoked!")
        }
    }
}

class D {

    companion object {

        @JvmStatic
        fun foo() {

        }

        fun bar() {

        }
    }
}


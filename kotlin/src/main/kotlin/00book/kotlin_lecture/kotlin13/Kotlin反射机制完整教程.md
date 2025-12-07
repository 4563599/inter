# Kotlin 反射机制完整教程

## 📚 什么是反射？

反射（Reflection）是程序在运行时检查、访问和修改自身结构和行为的能力。通过反射，我们可以：
- 在运行时获取类的信息（类名、属性、方法等）
- 动态调用方法和访问属性
- 创建对象实例
- 检查类型信息

---

## 1️⃣ 获取类引用（KClass vs Java Class）

### 示例代码：HelloKotlin1.kt

`kotlin
fun main(args: Array<String>) {
    val c = String::class        // 获取 Kotlin 的 KClass
    println(c)                   // 输出：class kotlin.String

    println("-------")

    val c2 = String::class.java  // 获取 Java 的 Class
    println(c2)                  // 输出：class java.lang.String
}
`

**知识点：**
- ::class 获取 Kotlin 的 KClass 类型
- ::class.java 获取 Java 的 Class 类型
- KClass 是 Kotlin 对反射的封装，提供更多 Kotlin 特性支持

---

## 2️⃣ 运行时类型判断

### 示例代码：HelloKotlin2.kt

`kotlin
fun main(args: Array<String>) {
    val son: Parent = Son()           // 声明类型是 Parent
    val daughter: Parent = Daughter() // 声明类型是 Parent

    println(son::class)               // 输出：class Son（实际类型）
    println(son::class.java)          // 输出：class Son

    println("---------")

    println(daughter::class)          // 输出：class Daughter（实际类型）
    println(daughter::class.java)     // 输出：class Daughter
}

open class Parent
class Son: Parent()
class Daughter: Parent()
`

**知识点：**
- ::class 获取的是对象的**实际运行时类型**，而不是声明类型
- 即使变量声明为父类型，反射也能获取真实的子类类型

---

## 3️⃣ 函数引用（Function Reference）

### 示例代码：HelloKotlin3.kt

`kotlin
// 定义两个重载的函数
fun multiplyBy3(x: Int): Int {
    return 3 * x
}

fun multiplyBy3(s: String): Int {
    return 10
}

fun main(args: Array<String>) {
    val values = listOf(1, 2, 3, 4)
    println(values.map(::multiplyBy3))  // 输出：[3, 6, 9, 12]

    println("-------")

    val values2 = listOf("a", "b", "c", "d")
    println(values2.map(::multiplyBy3)) // 输出：[10, 10, 10, 10]
}

// 函数引用赋值给变量
val myReference: (Int) -> Int = ::multiplyBy3
val myReference2: (String) -> Int = ::multiplyBy3
val myReference3: String.(Int) -> Char = String::get
`

**知识点：**
- 使用 ::函数名 来引用函数
- Kotlin 支持函数重载，会根据上下文自动选择正确的重载版本
- 函数引用可以赋值给变量，类型为函数类型 (参数) -> 返回值
- String::get 是成员函数引用，类型为 String.(Int) -> Char

---

## 4️⃣ 函数组合（Function Composition）

### 示例代码：HelloKotlin4.kt

`kotlin
// 定义函数组合：将两个函数组合成一个新函数
fun <A, B, C> myCompose(f: (B) -> C, g: (A) -> B): (A) -> C {
    return { x -> f(g(x)) }  // 先执行 g，再执行 f
}

fun isEven(x: Int) = 0 == x % 2  // 判断是否为偶数

fun length(s: String) = s.length  // 获取字符串长度

fun main(args: Array<String>) {
    // 组合两个函数：先获取长度，再判断长度是否为偶数
    val evenLength = myCompose(::isEven, ::length)
    val strings = listOf("a", "ab", "abc", "abcd", "abcde")

    println(strings.filter(evenLength))  // 输出：[ab, abcd]（长度为偶数的字符串）
}
`

**知识点：**
- 函数可以作为参数传递和返回
- 函数组合是函数式编程的重要概念
- myCompose(f, g) 创建新函数，等价于 (g(x))

---

## 5️⃣ 属性引用（Property Reference）

### 示例代码：HelloKotlin5.kt

`kotlin
const val a = 3  // 不可变属性
var b = 5        // 可变属性

fun main(args: Array<String>) {
    println(::a)          // 输出：val a: kotlin.Int
    println(::a.get())    // 输出：3（获取属性值）
    println(::a.name)     // 输出：a（获取属性名）

    println("----------")

    ::b.set(10)           // 设置属性值为 10
    println(b)            // 输出：10
    println(::b.get())    // 输出：10
}
`

**知识点：**
- ::属性名 获取属性引用
- 不可变属性（val）返回 KProperty<T> 类型，只有 get() 方法
- 可变属性（var）返回 KMutableProperty<T> 类型，有 get() 和 set() 方法
- 可以通过 
ame 属性获取属性名称

---

## 6️⃣ 类成员属性引用

### 示例代码：HelloKotlin6.kt

`kotlin
fun main(args: Array<String>) {
    val values = listOf("a", "abc", "abcd")
    println(values.map(String::length))  // 输出：[1, 3, 4]

    println("--------")

    val x = MyClass::x
    println(x.get(MyClass(10)))  // 输出：10（需要传入对象实例）
}

class MyClass(val x: Int)
`

**知识点：**
- 访问类的成员属性需要使用 类名::属性名
- 调用 get() 时需要传入对象实例
- String::length 可以直接用于 map 等高阶函数

---

## 7️⃣ 扩展属性引用

### 示例代码：HelloKotlin7.kt

`kotlin
// 定义扩展属性
val String.firstChar: Char
    get() = this[0]

fun main(args: Array<String>) {
    println(String::firstChar.get("xyz"))  // 输出：x
}
`

**知识点：**
- 扩展属性也可以通过 :: 引用
- 调用时需要传入接收者对象（这里是字符串 "xyz"）

---

## 8️⃣ Kotlin 与 Java 互操作

### 示例代码：HelloKotlin8.kt

`kotlin
class T(val x: Int)

fun main(args: Array<String>) {
    println(T::x.javaGetter)  // 输出：public final int T.getX()
    println(T::x.javaField)   // 输出：private final int T.x

    println("---------")

    println(T(10).javaClass)         // 输出：class T
    println(T(10).javaClass.kotlin)  // 输出：class T（转回 KClass）

    println(String.javaClass)        // 输出：class java.lang.String
    println(String.javaClass.kotlin) // 输出：class kotlin.String
}
`

**知识点：**
- javaGetter 获取 Java 的 getter 方法
- javaField 获取 Java 的字段
- javaClass 获取 Java Class 对象
- .kotlin 将 Java Class 转换为 Kotlin KClass

---

## 9️⃣ 构造方法引用（Constructor Reference）

### 示例代码：HelloKotlin9.kt

`kotlin
class B(val x: Int)

fun myMethod(factory: (x: Int) -> B) {
    val b: B = factory(3)  // 使用工厂函数创建对象
    println(b.x)           // 输出：3
}

fun main(args: Array<String>) {
    myMethod(::B)  // 传入构造方法引用
}
`

**知识点：**
- 使用 ::类名 引用构造方法
- 构造方法引用可以作为工厂函数使用
- 要求：
  1. 参数类型和个数必须匹配
  2. 返回类型必须是该类的类型

---

## 🔟 特定对象的方法和属性引用

### 示例代码：HelloKotlin10.kt

`kotlin
fun main(args: Array<String>) {
    val str = "abc"
    val getReference = str::get  // 引用特定对象的方法
    println(getReference(1))     // 输出：b

    println("-------")

    val myProp = "test"::length  // 引用特定对象的属性
    println(myProp.get())        // 输出：4

    println("-------")

    val myProp2 = String::length // 引用类的属性
    println(myProp2.get("test")) // 输出：4（需要传入对象）
}
`

**知识点：**
- 对象::方法 引用特定对象的方法，调用时不需要传入接收者
- 对象::属性 引用特定对象的属性
- 类::属性 引用类的属性，调用时需要传入对象实例

---

## 1️⃣1️⃣ KClass 详解

### 示例代码：HelloKotlin11.kt

```kotlin
fun main(args: Array<String>) {
    val kotlinLang = "kotlin"
    val kclass: KClass<out String> = kotlinLang::class
    println(kclass)  // 输出：class kotlin.String

    println("------------")

    val kclassDataType: KClass<String> = String::class
    println(kclassDataType)  // 输出：class kotlin.String

    println("------------")

    val kclass1: KClass<out String> = "kotlin"::class
    val kclass2: KClass<out String> = "java"::class
    val kclass3: KClass<out String> = "ruby"::class

    println(kclass1)           // 输出：class kotlin.String
    println(kclass2)           // 输出：class kotlin.String
    println(kclass3)           // 输出：class kotlin.String
    println(kclass1 == kclass2)  // 输出：true（都是 String 类型）

    println("------------")

    val kclass4 = Class.forName("java.util.Date").kotlin
    println(kclass4)  // 输出：class java.util.Date

    println(kclass4 == Class.forName("java.util.Date"))         // false
    println(kclass4 == Class.forName("java.util.Date").kotlin)  // true
}
```

**知识点：**
- KClass<out T> 表示 T 或其子类的类型
- 不同字符串对象的 ::class 返回相同的 KClass（因为类型相同）
- 可以通过 Class.forName().kotlin 将 Java Class 转为 KClass
- KClass 和 Java Class 是不同的对象，需要转换后才能比较

---

## 1️⃣2️⃣ 泛型类型参数

### 示例代码：HelloKotlin12.kt

```kotlin
class MyTestClass<K, V> {
    var k: K? = null
    var v: V? = null
}

fun main(args: Array<String>) {
    val myTestClassType = MyTestClass::class
    println(myTestClassType.typeParameters)  // 输出：[K, V]

    println(myTestClassType.typeParameters.size)  // 输出：2

    println("first type: " + myTestClassType.typeParameters[0])   // 输出：K
    println("second type: " + myTestClassType.typeParameters[1])  // 输出：V
}
```

**知识点：**
- 	ypeParameters 获取类的泛型类型参数列表
- 返回的是类型参数的名称（如 K、V），而不是具体类型
- 可以通过索引访问特定的类型参数

---

## 1️⃣3️⃣ 获取父类和接口

### 示例代码：HelloKotlin13.kt

```kotlin
class MySerializable: Serializable, MyInterface

fun main(args: Array<String>) {
    val mySerializableType = MySerializable::class
    println(mySerializableType.superclasses)  
    // 输出：[interface java.io.Serializable, interface MyInterface]
}

interface MyInterface
```

**知识点：**
- superclasses 获取类的所有父类和接口
- 返回一个列表，包含所有直接继承的类型
- 需要导入 kotlin.reflect.full.superclasses

---

## 1️⃣4️⃣ 获取类的所有属性

### 示例代码：HelloKotlin14.kt

```kotlin
class MyTestClass2(var a: String, val flag: Boolean, var age: Int) {
}

fun main(args: Array<String>) {
    val myTestClass2 = MyTestClass2::class
    println(myTestClass2.memberProperties)  
    // 输出：[var MyTestClass2.a: kotlin.String, val MyTestClass2.age: kotlin.Int, val MyTestClass2.flag: kotlin.Boolean]
}
```

**知识点：**
- memberProperties 获取类的所有成员属性
- 包括 var 和 val 属性
- 需要导入 kotlin.reflect.full.memberProperties

---

## 1️⃣5️⃣ 获取类的所有方法

### 示例代码：HelloKotlin15.kt

```kotlin
class MyTestClass3 {
    fun printSomething() {
        println("something")
    }

    fun printNothing() {
        println("")
    }
}

fun main(args: Array<String>) {
    val myTestClass3 = MyTestClass3::class
    println(myTestClass3.memberFunctions)  
    // 输出：包含 printSomething、printNothing 以及继承自 Any 的方法
}
```

**知识点：**
- memberFunctions 获取类的所有成员方法
- 包括自定义方法和继承的方法（如 equals、hashCode、toString）
- 需要导入 kotlin.reflect.full.memberFunctions

---

## 1️⃣6️⃣ 获取构造方法

### 示例代码：HelloKotlin16.kt

```kotlin
class MyTestClass4(value: Int) {

    constructor(amount: Int, color: String): this(amount) {
        println("secondary constructor")
    }

    constructor(amount: Int, full: Boolean): this(amount) {
        println("secondary constructor")
    }

    fun printSomething() {
        println("something")
    }
}

fun main(args: Array<String>) {
    val myTestClass4 = MyTestClass4::class
    val constructors = myTestClass4.constructors

    println(constructors)  
    // 输出：包含主构造方法和两个次构造方法
}
```

**知识点：**
- constructors 获取类的所有构造方法
- 包括主构造方法和次构造方法
- 返回 Collection<KFunction<T>> 类型

---

## 1️⃣7️⃣ 动态调用方法

### 示例代码：HelloKotlin17.kt

```kotlin
class MyTestClass5 {
    fun printSomething(name: String) {
        println("something: name")
    }

    fun printNothing() {
        println("nothing")
    }
}

fun main(args: Array<String>) {
    val myTestClass5 = MyTestClass5::class
    val testClass5 = MyTestClass5()

    // 查找并调用无参方法
    var functionToInvoke = myTestClass5.functions.find { it.name == "printNothing" }
    functionToInvoke?.call(testClass5)  // 输出：nothing

    // 查找并调用带参方法
    var funToInvoke = myTestClass5.functions.find { it.name == "printSomething" }
    funToInvoke?.call(testClass5, "hello world")  // 输出：something: hello world
}
```

**知识点：**
- 使用 ind 根据方法名查找方法
- 使用 call() 动态调用方法
- 第一个参数是对象实例，后续参数是方法参数
- 无参方法只需传入对象实例

---

## 1️⃣8️⃣ 动态访问属性

### 示例代码：HelloKotlin18.kt

```kotlin
class MyTestClass6 {
    var name: String = "hello world"
}

fun main(args: Array<String>) {
    val myTestClass6 = MyTestClass6::class
    var testClass6 = MyTestClass6()

    var variableToInvoke = myTestClass6.memberProperties.find { it.name == "name" }

    println(variableToInvoke?.get(testClass6))   // 输出：hello world
    println(variableToInvoke?.call(testClass6))  // 输出：hello world
}
```

**知识点：**
- 使用 ind 根据属性名查找属性
- 使用 get() 或 call() 获取属性值
- 需要传入对象实例作为参数

---

## 1️⃣9️⃣ 动态修改属性

### 示例代码：HelloKotlin19.kt

```kotlin
class MyTestClass7 {
    var name: String = "hello world"
    var authorName: String = "tom"
}

fun main(args: Array<String>) {
    val myTestClass7 = MyTestClass7::class
    var testClass7 = MyTestClass7()

    var variableToInvoke = myTestClass7.memberProperties.find { it.name == "name" }
    println(variableToInvoke?.get(testClass7))  // 输出：hello world

    // 判断是否为可变属性，然后修改
    if (variableToInvoke is KMutableProperty<*>) {
        variableToInvoke.setter.call(testClass7, "welcome")
    }

    println(variableToInvoke?.get(testClass7))  // 输出：welcome
}
```

**知识点：**
- 使用 is KMutableProperty<*> 判断是否为可变属性
- 通过 setter.call() 修改属性值
- val 属性无法修改，只有 var 属性才能修改

---

## 2️⃣0️⃣ 伴生对象（Companion Object）

### 示例代码：HelloKotlin20.kt

```kotlin
class MyTestClass8 {
    companion object {
        fun method() {
            println("hello world")
        }
    }
}

fun main(args: Array<String>) {
    var myTestClass8 = MyTestClass8::class
    var companionObj = myTestClass8.companionObject

    println(companionObj)  // 输出：class MyTestClass8Companion

    MyTestClass8.method()  // 输出：hello world
}
```

**知识点：**
- companionObject 获取类的伴生对象
- 伴生对象类似于 Java 的静态成员
- 需要导入 kotlin.reflect.full.companionObject

---


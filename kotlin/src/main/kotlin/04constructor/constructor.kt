package constructorinfo

/**
 * Kotlin 构造方法备忘：主构造、init、次构造、委托、访问控制、继承调用等示例。
 */
fun main() {
    println("=== 1. 主构造 + init + 默认参数 ===")
    val alice = User(id = 1, name = "Alice")
    val bob = User(id = 2, name = "Bob", email = "bob@test.com")
    println(alice)
    println(bob)

    println("\n=== 2. 次构造函数 (constructor) ===")
    val tempUser = User(3) // 调用 constructor(id: Int)
    val guest = User()     // 调用 constructor()，再链到上一个
    println(tempUser)
    println(guest)

    println("\n=== 3. 继承时的构造调用顺序 ===")
    val child = Student("Tom", 3)
    println(child)

    println("\n=== 4. 私有构造 + 工厂方法 ===")
    val logger = Logger.create(tag = "Main")
    logger.log("Hello")
}

/**
 * 主构造函数写在 class 声明后面，可直接声明属性。
 * - init 块在创建实例时按出现顺序执行，可做参数校验。
 * - 属性可设置默认值；调用时可省略。
 */
class User(
    val id: Int, var name: String, val email: String = "unknown"
) {
    init {
        require(id > 0) { "id must be positive" }
        println("init: id=$id name=$name email=$email")
    }

    // 次构造函数必须委托到主构造函数（使用 this(...)）。
    constructor(id: Int) : this(id, name = "Temp-$id") {
        println("secondary constructor(id: Int) called")
    }

    // 可以继续链式委托。
    constructor() : this(id = 999) {
        println("secondary constructor() called")
    }

    override fun toString(): String = "User(id=$id, name=$name, email=$email)"
}

/**
 * 继承时：子类主构造需要显示调用父类构造。
 */
open class Person(val name: String) {
    init {
        println("Person init: name=$name")
    }
}

class Student(name: String, val grade: Int) : Person(name) {
    init {
        println("Student init: grade=$grade")
    }

    override fun toString(): String = "Student(name=$name, grade=$grade)"
}

/**
 * 构造函数可以设为 private，配合 companion object 暴露工厂。
 */
class Logger private constructor(private val tag: String) {
    fun log(msg: String) = println("[$tag] $msg")

    companion object {
        fun create(tag: String): Logger {
            println("create Logger with tag=$tag")
            return Logger(tag)
        }
    }
}

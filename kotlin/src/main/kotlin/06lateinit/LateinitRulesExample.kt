package `05companion`

/**
 * 这个例子用通俗的方式解释 lateinit 的3个使用条件。
 */

// 这是一个普通的类，我们将用它作为 lateinit 属性的类型
class Connection

class LateinitRulesExample {

    // --- 遵守所有规则的正确示例 ---
    lateinit var myConnection: Connection


    // --- 规则 1：只能用在类体中声明的`var`属性上 ---

    // 错误1：lateinit 不能用于 val，因为 val 要求声明时或构造块中初始化
    // lateinit val wrongVal: String // 取消注释会报错：'lateinit' modifier is not allowed on properties of a type with a custom 'get' or 'set'

    // 在 primary constructor 中声明的属性，本质上是要求在创建对象时就提供值，
    // 这与 lateinit “延迟”初始化的思想是矛盾的。
    // class WrongConstructor(lateinit var name: String) // 取消注释会报错：'lateinit' modifier is not allowed on primary constructor parameters


    // --- 规则 2：属性不能拥有自定义的setter与getter ---

    // 错误2：lateinit 属性不能有自定义的 getter
    // lateinit var wrongGetter: String
    //     get() = field      // 取消注释会报错：'lateinit' modifier is not allowed on properties of a type with a custom 'get' or 'set'

    // 错误3：lateinit 属性不能有自定义的 setter
    // lateinit var wrongSetter: String
    //     set(value) { field = value } // 取消注释会报错：'lateinit' modifier is not allowed on properties of a type with a custom 'get' or 'set'


    // --- 规则 3：属性类型需要为非空，且不能是原生数据类型 ---

    // 错误4：lateinit 的目的就是为了避免可空类型，所以它不能用于可空类型
    // lateinit var wrongNullable: String? // 取消注释会报错：'lateinit' modifier is not allowed on nullable properties

    // 错误5：lateinit 不能用于 Int, Double, Float, Boolean 等原生数据类型
    // lateinit var wrongInt: Int       // 取消注释会报错：'lateinit' modifier is not allowed on properties of primitive types
    // lateinit var wrongBoolean: Boolean // 取消注释会报错：'lateinit' modifier is not allowed on properties of primitive types


    fun initialize() {
        myConnection = Connection() // 正确地初始化
        println("myConnection is initialized now.")
    }
}

fun main() {
    val example = LateinitRulesExample()

    // 此时访问 myConnection 会崩溃，因为它还没有被初始化
    // example.myConnection 

    example.initialize() // 调用方法，完成延迟初始化

    // 现在访问就安全了
    println("Initialization was successful. Can use myConnection now: ${example.myConnection}")
}

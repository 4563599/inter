package `06lateinit`

/**
 * 这个例子演示了 lateinit (延迟初始化) 的用法和原因。
 */
class MessageService {
    // 假设这个 "message" 的内容需要通过网络加载或者复杂的计算才能得到，
    // 所以我们无法在 MessageService 对象被创建时就立即给它赋值。
    // 我们使用 lateinit 向编译器承诺：我保证在使用它之前，一定会初始化它！
    private lateinit var message: String

    // 模拟一个耗时的初始化过程，比如连接服务器、加载数据等。
    fun initialize() {
        println("正在初始化消息...")
        // 经过一番操作后，message 属性终于被赋值了。
        this.message = "Hello, Kotlin Lateinit!"
        println("初始化完成！")
    }

    fun printMessage() {
        // 在这里，我们直接使用 message 属性。
        // 如果此时 message 还没有被初始化（即 initialize() 方法还没被调用），
        // 程序就会在这里崩溃，并抛出 UninitializedPropertyAccessException。
        println("要打印的消息是: $message")
    }
    
    // 我们还可以检查 lateinit 变量是否已经被初始化
    fun isMessageInitialized(): Boolean {
        // 使用 ::message.isInitialized 语法
        return this::message.isInitialized
    }
}

fun main() {
    val service = MessageService()

    println("--- 场景1：正确的调用顺序 ---")
    println("服务已创建，但消息是否已初始化？ ${service.isMessageInitialized()}")
    service.initialize() // 先初始化
    println("服务已初始化，消息是否已初始化？ ${service.isMessageInitialized()}")
    service.printMessage() // 再使用，一切正常

    println("\n----------------------------------\n")

    println("--- 场景2：错误的调用顺序 (会导致崩溃) ---")
    val badService = MessageService()
    println("新服务已创建，消息是否已初始化？ ${badService.isMessageInitialized()}")
    
    try {
        // 尝试在没有调用 initialize() 的情况下直接使用 message
        badService.printMessage() 
    } catch (e: UninitializedPropertyAccessException) {
        println("果然出错了！捕获到异常: ${e.message}")
    }
}

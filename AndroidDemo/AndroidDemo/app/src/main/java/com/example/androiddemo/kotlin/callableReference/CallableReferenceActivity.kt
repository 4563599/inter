package com.example.androiddemo.kotlin.callableReference

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * CallableReferenceActivity - Kotlin 成员引用操作符 (::) 详解
 *
 * ================================================================
 * 什么是成员引用操作符 (::)?
 * ================================================================
 *
 * :: 是 Kotlin 的成员引用操作符（也叫可调用引用），用于获取：
 * - 函数引用 (Function Reference)
 * - 属性引用 (Property Reference)
 * - 构造函数引用 (Constructor Reference)
 *
 * 为什么要用 :: ?
 * 1. 代码更简洁 - 避免写冗余的 lambda 表达式
 * 2. 可读性更好 - 直接表达"引用某个函数/属性"的意图
 * 3. 性能更优 - 编译器可以更好地优化
 * 4. 函数式编程 - 将函数作为一等公民传递
 *
 * ================================================================
 */
class CallableReferenceActivity : AppCompatActivity() {

    private lateinit var logView: TextView
    private lateinit var logContainer: ScrollView
    private val logBuilder = StringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(buildContentView())
        appendLog("=================================")
        appendLog("Kotlin 成员引用操作符 (::) 演示")
        appendLog("=================================")
        appendLog("✅ 界面加载成功！")
        appendLog("👇 请点击下方按钮查看演示\n")
    }

    private fun buildContentView(): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 48, 32, 48)

            addView(createButton("1. 函数引用 ::function") { openDemo("functionReference") })
            addView(createButton("2. 属性引用 ::property") { openDemo("propertyReference") })
            addView(createButton("3. 构造函数引用 ::Class") { openDemo("constructorReference") })
            addView(createButton("4. 绑定引用 instance::member") { openDemo("boundReference") })
            addView(createButton("5. Android实战: View点击") { openDemo("androidClick") })
            addView(createButton("6. Android实战: 列表操作") { openDemo("listOperations") })
            addView(createButton("清空日志") { clearLog() })

            logView = TextView(this@CallableReferenceActivity).apply {
                textSize = 14f
                setTextIsSelectable(true)
                setPadding(24, 24, 24, 24)
                setBackgroundColor(0xFFF5F5F5.toInt())
                setTextColor(0xFF000000.toInt())  // 黑色文字
                text = "日志区域 - 等待加载..."  // 默认文本，确保可见
            }

            logContainer = ScrollView(this@CallableReferenceActivity).apply {
                setBackgroundColor(0xFFEEEEEE.toInt())  // 浅灰色背景
                addView(logView)
            }

            addView(logContainer, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0
            ).apply { weight = 1f; topMargin = 24 })
        }
    }

    private fun createButton(text: String, onClick: () -> Unit): Button {
        return Button(this).apply {
            this.text = text
            isAllCaps = false
            setOnClickListener { 
                appendLog("按钮被点击: $text\n")
                onClick() 
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 16
            }
        }
    }


    // ==================== 1. 函数引用 ====================
    /**
     * 函数引用: 使用 ::functionName 获取函数的引用
     *
     * 为什么用函数引用？
     * - 避免写 { x -> someFunction(x) } 这样的冗余 lambda
     * - 直接传递函数本身，代码更简洁
     * - 编译器可以进行更好的优化
     */
    private fun demoFunctionReference() {
        appendLog("=== 1. 函数引用 ::function ===\n")

        val numbers = listOf(1, 2, 3, 4, 5)

        // ❌ 传统 lambda 写法 - 冗余
        appendLog("传统 lambda 写法:")
        appendLog("numbers.map { n -> double(n) }")
        val result1 = numbers.map { n -> double(n) }
        appendLog("结果: $result1\n")

        // ✅ 函数引用写法 - 简洁
        appendLog("函数引用写法:")
        appendLog("numbers.map(::double)")
        val result2 = numbers.map(::double)
        appendLog("结果: $result2\n")

        // 函数引用用于 filter
        appendLog("filter 中使用函数引用:")
        appendLog("numbers.filter(::isEven)")
        val evens = numbers.filter(::isEven)
        appendLog("偶数: $evens\n")

        // 函数引用用于 forEach
        appendLog("forEach 中使用函数引用:")
        appendLog("listOf(\"A\", \"B\", \"C\").forEach(::appendLog)")
        listOf("  -> 元素A", "  -> 元素B", "  -> 元素C").forEach(::appendLog)

        appendLog("\n💡 好处: 代码更简洁，意图更清晰\n")
    }

    private fun double(n: Int): Int = n * 2
    private fun isEven(n: Int): Boolean = n % 2 == 0

    // ==================== 2. 属性引用 ====================
    /**
     * 属性引用: 使用 ::propertyName 获取属性的引用
     *
     * 为什么用属性引用？
     * - 可以获取属性的 getter/setter
     * - 用于反射、数据绑定等场景
     * - 在集合操作中提取对象的某个属性
     */
    private fun demoPropertyReference() {
        appendLog("=== 2. 属性引用 ::property ===\n")

        // 示例数据类
        data class User(val name: String, val age: Int)

        val users = listOf(
            User("张三", 25),
            User("李四", 30),
            User("王五", 28)
        )

        // ❌ 传统写法
        appendLog("传统 lambda 写法:")
        appendLog("users.map { it.name }")
        val names1 = users.map { it.name }
        appendLog("结果: $names1\n")

        // ✅ 属性引用写法
        appendLog("属性引用写法:")
        appendLog("users.map(User::name)")
        val names2 = users.map(User::name)
        appendLog("结果: $names2\n")

        // 属性引用用于排序
        appendLog("sortedBy 中使用属性引用:")
        appendLog("users.sortedBy(User::age)")
        val sorted = users.sortedBy(User::age)
        appendLog("按年龄排序: ${sorted.map { "${it.name}(${it.age})" }}\n")

        // 获取属性引用的值
        appendLog("属性引用的高级用法:")
        val nameProperty = User::name
        val user = User("测试用户", 20)
        appendLog("val nameProperty = User::name")
        appendLog("nameProperty.get(user) = ${nameProperty.get(user)}\n")

        appendLog("💡 好处: 提取属性更直观，适合数据处理\n")
    }

    // ==================== 3. 构造函数引用 ====================
    /**
     * 构造函数引用: 使用 ::ClassName 获取构造函数的引用
     *
     * 为什么用构造函数引用？
     * - 将构造函数作为工厂函数传递
     * - 用于依赖注入、对象创建等场景
     * - 配合高阶函数创建对象集合
     */
    private fun demoConstructorReference() {
        appendLog("=== 3. 构造函数引用 ::Class ===\n")

        data class Product(val name: String)

        val productNames = listOf("手机", "电脑", "平板")

        // ❌ 传统写法
        appendLog("传统 lambda 写法:")
        appendLog("productNames.map { Product(it) }")
        val products1 = productNames.map { Product(it) }
        appendLog("结果: $products1\n")

        // ✅ 构造函数引用写法
        appendLog("构造函数引用写法:")
        appendLog("productNames.map(::Product)")
        val products2 = productNames.map(::Product)
        appendLog("结果: $products2\n")

        // 构造函数引用作为工厂
        appendLog("构造函数引用作为工厂函数:")
        fun <T> createItems(count: Int, factory: (String) -> T): List<T> {
            return (1..count).map { factory("Item$it") }
        }
        val items = createItems(3, ::Product)
        appendLog("createItems(3, ::Product) = $items\n")

        appendLog("💡 好处: 简化对象创建，支持函数式工厂模式\n")
    }


    // ==================== 4. 绑定引用 ====================
    /**
     * 绑定引用: 使用 instance::member 获取绑定到特定实例的引用
     *
     * 为什么用绑定引用？
     * - 引用特定对象的方法，而不是类的方法
     * - 避免每次调用时传递对象实例
     * - 常用于回调和事件处理
     */
    private fun demoBoundReference() {
        appendLog("=== 4. 绑定引用 instance::member ===\n")

        val str = "Hello Kotlin"

        // 未绑定引用 - 需要传递实例
        appendLog("未绑定引用 String::length:")
        val unboundLength = String::length
        appendLog("unboundLength.get(\"$str\") = ${unboundLength.get(str)}\n")

        // 绑定引用 - 已绑定到特定实例
        appendLog("绑定引用 str::length:")
        val boundLength = str::length
        appendLog("boundLength.get() = ${boundLength.get()}\n")

        // 绑定方法引用
        appendLog("绑定方法引用示例:")
        val words = listOf("apple", "BANANA", "Cherry")

        // 使用绑定引用检查是否包含某字符
        val containsA = words.filter("a"::equals)  // 等价于 { it == "a" }
        appendLog("words.filter(\"a\"::equals) = $containsA")

        // 绑定到 this 的引用
        appendLog("\n绑定到 this 的引用:")
        appendLog("this::appendLog 可以直接传递给需要 (String) -> Unit 的函数")
        listOf("  -> 消息1", "  -> 消息2").forEach(this::appendLog)

        appendLog("\n💡 好处: 简化回调，避免重复传递实例\n")
    }

    // ==================== 5. Android实战: View点击 ====================
    /**
     * Android 实战: 使用成员引用处理点击事件
     *
     * 为什么在 Android 中用成员引用？
     * - 避免匿名内部类的内存泄漏风险
     * - 代码更简洁，易于维护
     * - 方法可以复用和测试
     */
    private fun demoAndroidClickListener() {
        appendLog("=== 5. Android实战: View点击处理 ===\n")

        appendLog("【传统写法 vs 成员引用写法】\n")

        // ❌ 传统匿名内部类写法
        appendLog("❌ 传统写法 (Java风格):")
        appendLog("""
            button.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    handleClick(v)
                }
            })
        """.trimIndent())

        // ❌ Lambda 写法
        appendLog("\n❌ Lambda 写法:")
        appendLog("button.setOnClickListener { v -> handleClick(v) }")

        // ✅ 成员引用写法
        appendLog("\n✅ 成员引用写法 (推荐):")
        appendLog("button.setOnClickListener(::handleClick)")

        appendLog("\n【实际演示】")
        // 创建一个测试按钮
        val testButton = Button(this).apply {
            text = "测试按钮"
            // ✅ 使用成员引用设置点击监听
            setOnClickListener(::handleClick)
        }
        // 模拟点击
        testButton.performClick()

        appendLog("\n💡 好处:")
        appendLog("• 代码简洁，一行搞定")
        appendLog("• 方法可复用，便于单元测试")
        appendLog("• 避免 lambda 捕获外部变量导致的内存问题\n")
    }

    private fun handleClick(view: View?) {
        appendLog("  -> 按钮被点击! View: ${view?.javaClass?.simpleName}")
    }

    // ==================== 6. Android实战: 列表操作 ====================
    /**
     * Android 实战: 使用成员引用进行列表数据处理
     *
     * 这是 Android 开发中最常见的成员引用使用场景
     */
    private fun demoListOperations() {
        appendLog("=== 6. Android实战: 列表数据处理 ===\n")

        // 模拟从 API 获取的用户数据
        data class UserEntity(
            val id: Int,
            val name: String,
            val email: String,
            val isActive: Boolean
        )

        val users = listOf(
            UserEntity(1, "张三", "zhangsan@example.com", true),
            UserEntity(2, "李四", "lisi@example.com", false),
            UserEntity(3, "王五", "wangwu@example.com", true),
            UserEntity(4, "赵六", "zhaoliu@example.com", true)
        )

        appendLog("原始数据: ${users.size} 个用户\n")

        // 场景1: 提取所有用户名
        appendLog("场景1: 提取用户名列表")
        appendLog("users.map(UserEntity::name)")
        val names = users.map(UserEntity::name)
        appendLog("结果: $names\n")

        // 场景2: 筛选活跃用户
        appendLog("场景2: 筛选活跃用户")
        appendLog("users.filter(UserEntity::isActive)")
        val activeUsers = users.filter(UserEntity::isActive)
        appendLog("活跃用户: ${activeUsers.map(UserEntity::name)}\n")

        // 场景3: 按ID排序
        appendLog("场景3: 按ID降序排序")
        appendLog("users.sortedByDescending(UserEntity::id)")
        val sorted = users.sortedByDescending(UserEntity::id)
        appendLog("排序结果: ${sorted.map(UserEntity::name)}\n")

        // 场景4: 转换为 Map
        appendLog("场景4: 转换为 ID -> Name 的 Map")
        appendLog("users.associate { it.id to it.name }")
        val userMap = users.associate { it.id to it.name }
        appendLog("结果: $userMap\n")

        // 场景5: 分组
        appendLog("场景5: 按活跃状态分组")
        appendLog("users.groupBy(UserEntity::isActive)")
        val grouped = users.groupBy(UserEntity::isActive)
        appendLog("活跃: ${grouped[true]?.map(UserEntity::name)}")
        appendLog("非活跃: ${grouped[false]?.map(UserEntity::name)}\n")

        // 场景6: 链式操作
        appendLog("场景6: 链式操作 - 获取活跃用户的邮箱")
        appendLog("""
            users.filter(UserEntity::isActive)
                 .map(UserEntity::email)
        """.trimIndent())
        val activeEmails = users
            .filter(UserEntity::isActive)
            .map(UserEntity::email)
        appendLog("结果: $activeEmails\n")

        appendLog("💡 总结: 成员引用让数据处理代码更简洁、更易读\n")
    }

    // ==================== 工具方法 ====================

    private fun openDemo(demoType: String) {
        val intent = android.content.Intent(this, CallableReferenceDemoActivity::class.java)
        intent.putExtra("DEMO_TYPE", demoType)
        startActivity(intent)
    }

    private fun appendLog(text: String) {
        runOnUiThread {
            logBuilder.append(text).append('\n')
            logView.text = logBuilder.toString()
            logContainer.post { logContainer.fullScroll(View.FOCUS_DOWN) }
        }
    }

    private fun clearLog() {
        logBuilder.clear()
        logView.text = ""
    }
}

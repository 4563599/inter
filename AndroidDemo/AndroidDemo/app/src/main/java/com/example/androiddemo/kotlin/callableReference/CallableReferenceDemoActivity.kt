package com.example.androiddemo.kotlin.callableReference

import android.os.Bundle
import android.view.View
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * Kotlin 成员引用演示详情页 - 全屏显示日志
 */
class CallableReferenceDemoActivity : AppCompatActivity() {

    private lateinit var logView: TextView
    private lateinit var logContainer: ScrollView
    private val logBuilder = StringBuilder()
    private var demoType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        demoType = intent.getStringExtra("DEMO_TYPE")
        
        setContentView(buildContentView())
        
        runDemo()
    }

    private fun buildContentView(): View {
        // 启用返回按钮
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        // 创建根布局
        val rootLayout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            fitsSystemWindows = true  // 自动适配系统窗口（包括 ActionBar）
        }
        
        logView = TextView(this).apply {
            textSize = 14f
            setTextIsSelectable(true)
            setPadding(32, 32, 32, 48)  // 正常的 padding
            setTextColor(0xFF000000.toInt())
            setBackgroundColor(0xFFFFFFFF.toInt())
            
            // 设置布局参数，确保内容完全显示
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        logContainer = ScrollView(this).apply {
            isFillViewport = false  // 不填充视口，让内容自然滚动
            addView(logView)
        }
        
        // 将 ScrollView 添加到根布局
        val scrollParams = android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT
        )
        rootLayout.addView(logContainer, scrollParams)

        return rootLayout
    }

    private fun runDemo() {
        when (demoType) {
            "functionReference" -> demoFunctionReference()
            "propertyReference" -> demoPropertyReference()
            "constructorReference" -> demoConstructorReference()
            "boundReference" -> demoBoundReference()
            "androidClick" -> demoAndroidClickListener()
            "listOperations" -> demoListOperations()
        }
    }

    // ==================== 1. 函数引用 ====================
    private fun demoFunctionReference() {
        appendLog("=== 函数引用 ::function ===\n")
        appendLog("为什么用函数引用？")
        appendLog("• 避免写冗余的 lambda 表达式")
        appendLog("• 代码更简洁，意图更清晰")
        appendLog("• 编译器可以更好地优化\n")

        val numbers = listOf(1, 2, 3, 4, 5)

        appendLog("❌ 传统 lambda 写法:")
        appendLog("numbers.map { n -> double(n) }")
        val result1 = numbers.map { n -> double(n) }
        appendLog("结果: $result1\n")

        appendLog("✅ 函数引用写法:")
        appendLog("numbers.map(::double)")
        val result2 = numbers.map(::double)
        appendLog("结果: $result2\n")

        appendLog("filter 中使用函数引用:")
        appendLog("numbers.filter(::isEven)")
        val evens = numbers.filter(::isEven)
        appendLog("偶数: $evens\n")

        appendLog("forEach 中使用函数引用:")
        appendLog("listOf(\"A\", \"B\", \"C\").forEach(::appendLog)")
        listOf("  -> 元素A", "  -> 元素B", "  -> 元素C").forEach(::appendLog)

        appendLog("\n✅ 好处: 代码更简洁，意图更清晰")
    }

    private fun double(n: Int): Int = n * 2
    private fun isEven(n: Int): Boolean = n % 2 == 0

    // ==================== 2. 属性引用 ====================
    private fun demoPropertyReference() {
        appendLog("=== 属性引用 ::property ===\n")
        appendLog("为什么用属性引用？")
        appendLog("• 可以获取属性的 getter/setter")
        appendLog("• 在集合操作中提取对象的某个属性\n")

        data class User(val name: String, val age: Int)

        val users = listOf(
            User("张三", 25),
            User("李四", 30),
            User("王五", 28)
        )

        appendLog("❌ 传统 lambda 写法:")
        appendLog("users.map { it.name }")
        val names1 = users.map { it.name }
        appendLog("结果: $names1\n")

        appendLog("✅ 属性引用写法:")
        appendLog("users.map(User::name)")
        val names2 = users.map(User::name)
        appendLog("结果: $names2\n")

        appendLog("sortedBy 中使用属性引用:")
        appendLog("users.sortedBy(User::age)")
        val sorted = users.sortedBy(User::age)
        appendLog("按年龄排序: ${sorted.map { "${it.name}(${it.age})" }}\n")

        appendLog("属性引用的高级用法:")
        val nameProperty = User::name
        val user = User("测试用户", 20)
        appendLog("val nameProperty = User::name")
        appendLog("nameProperty.get(user) = ${nameProperty.get(user)}\n")

        appendLog("✅ 好处: 提取属性更直观，适合数据处理")
    }

    // ==================== 3. 构造函数引用 ====================
    private fun demoConstructorReference() {
        appendLog("=== 构造函数引用 ::Class ===\n")
        appendLog("为什么用构造函数引用？")
        appendLog("• 将构造函数作为工厂函数传递")
        appendLog("• 配合高阶函数创建对象集合\n")

        data class Product(val name: String)

        val productNames = listOf("手机", "电脑", "平板")

        appendLog("❌ 传统 lambda 写法:")
        appendLog("productNames.map { Product(it) }")
        val products1 = productNames.map { Product(it) }
        appendLog("结果: $products1\n")

        appendLog("✅ 构造函数引用写法:")
        appendLog("productNames.map(::Product)")
        val products2 = productNames.map(::Product)
        appendLog("结果: $products2\n")

        appendLog("构造函数引用作为工厂函数:")
        fun <T> createItems(count: Int, factory: (String) -> T): List<T> {
            return (1..count).map { factory("Item$it") }
        }
        val items = createItems(3, ::Product)
        appendLog("createItems(3, ::Product) = $items\n")

        appendLog("✅ 好处: 简化对象创建，支持函数式工厂模式")
    }

    // ==================== 4. 绑定引用 ====================
    private fun demoBoundReference() {
        appendLog("=== 绑定引用 instance::member ===\n")
        appendLog("为什么用绑定引用？")
        appendLog("• 引用特定对象的方法")
        appendLog("• 避免每次调用时传递对象实例\n")

        val str = "Hello Kotlin"

        appendLog("未绑定引用 String::length:")
        val unboundLength = String::length
        appendLog("unboundLength.get(\"$str\") = ${unboundLength.get(str)}\n")

        appendLog("绑定引用 str::length:")
        val boundLength = str::length
        appendLog("boundLength.get() = ${boundLength.get()}\n")

        appendLog("绑定方法引用示例:")
        val words = listOf("apple", "BANANA", "Cherry")

        val containsA = words.filter("a"::equals)
        appendLog("words.filter(\"a\"::equals) = $containsA")

        appendLog("\n绑定到 this 的引用:")
        appendLog("this::appendLog 可以直接传递给需要 (String) -> Unit 的函数")
        listOf("  -> 消息1", "  -> 消息2").forEach(this::appendLog)

        appendLog("\n✅ 好处: 简化回调，避免重复传递实例")
    }

    // ==================== 5. Android实战: View点击 ====================
    private fun demoAndroidClickListener() {
        appendLog("=== Android实战: View点击处理 ===\n")

        appendLog("【传统写法 vs 成员引用写法】\n")

        appendLog("❌ 传统写法 (Java风格):")
        appendLog("""
            button.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    handleClick(v)
                }
            })
        """.trimIndent())

        appendLog("\n❌ Lambda 写法:")
        appendLog("button.setOnClickListener { v -> handleClick(v) }")

        appendLog("\n✅ 成员引用写法 (推荐):")
        appendLog("button.setOnClickListener(::handleClick)")

        appendLog("\n【实际演示】")
        val testButton = android.widget.Button(this).apply {
            text = "测试按钮"
            setOnClickListener(::handleClick)
        }
        testButton.performClick()

        appendLog("\n✅ 好处:")
        appendLog("• 代码简洁，一行搞定")
        appendLog("• 方法可复用，便于单元测试")
        appendLog("• 避免 lambda 捕获外部变量导致的内存问题")
    }

    private fun handleClick(view: View?) {
        appendLog("  -> 按钮被点击! View: ${view?.javaClass?.simpleName}")
    }

    // ==================== 6. Android实战: 列表操作 ====================
    private fun demoListOperations() {
        appendLog("=== Android实战: 列表数据处理 ===\n")

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

        appendLog("场景1: 提取用户名列表")
        appendLog("users.map(UserEntity::name)")
        val names = users.map(UserEntity::name)
        appendLog("结果: $names\n")

        appendLog("场景2: 筛选活跃用户")
        appendLog("users.filter(UserEntity::isActive)")
        val activeUsers = users.filter(UserEntity::isActive)
        appendLog("活跃用户: ${activeUsers.map(UserEntity::name)}\n")

        appendLog("场景3: 按ID降序排序")
        appendLog("users.sortedByDescending(UserEntity::id)")
        val sorted = users.sortedByDescending(UserEntity::id)
        appendLog("排序结果: ${sorted.map(UserEntity::name)}\n")

        appendLog("场景4: 转换为 ID -> Name 的 Map")
        appendLog("users.associate { it.id to it.name }")
        val userMap = users.associate { it.id to it.name }
        appendLog("结果: $userMap\n")

        appendLog("场景5: 按活跃状态分组")
        appendLog("users.groupBy(UserEntity::isActive)")
        val grouped = users.groupBy(UserEntity::isActive)
        appendLog("活跃: ${grouped[true]?.map(UserEntity::name)}")
        appendLog("非活跃: ${grouped[false]?.map(UserEntity::name)}\n")

        appendLog("场景6: 链式操作 - 获取活跃用户的邮箱")
        appendLog("""
            users.filter(UserEntity::isActive)
                 .map(UserEntity::email)
        """.trimIndent())
        val activeEmails = users
            .filter(UserEntity::isActive)
            .map(UserEntity::email)
        appendLog("结果: $activeEmails\n")

        appendLog("✅ 总结: 成员引用让数据处理代码更简洁、更易读")
    }

    // ==================== 工具方法 ====================

    private fun appendLog(text: String) {
        runOnUiThread {
            logBuilder.append(text).append('\n')
            logView.text = logBuilder.toString()
            // 延迟滚动，确保 TextView 已经更新完成
            logContainer.postDelayed({
                logContainer.fullScroll(View.FOCUS_DOWN)
            }, 50)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

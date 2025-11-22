package `01sugar`

class ListBuilder {
    private val items = mutableListOf<String>()
    fun item(text: String) { items += text }
    fun build(): String = items.joinToString(prefix = "[", postfix = "]")
}

fun list(block: ListBuilder.() -> Unit): String =
    ListBuilder().apply(block).build() // 语法糖：带接收者 lambda + apply 组合构建 DSL

fun main() {
    val built = list {
        item("kotlin")
        item("sugar")
    }
    println(built) // [kotlin, sugar]
}

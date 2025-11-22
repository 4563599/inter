package arraydemo

/**
 * Kotlin 数组核心用法演示：创建、读写、常用操作与互操作。
 */
fun main() {
    // 1. 使用 arrayOf 创建通用数组，类型通过元素推断。
    val languages = arrayOf("Kotlin", "Java", "Swift")
    println("languages=${languages.contentToString()}")

    // 2. 使用 intArrayOf 等原生类型数组，避免装箱，适合数值密集场景。
    val numbers = intArrayOf(10, 20, 30, 40)
    println("numbers sum=${numbers.sum()}")

    // 3. 访问 & 修改：通过索引或 set/get，注意越界会抛异常。
    val firstLang = languages[0] // 同 languages.get(0)
    println("firstLang=$firstLang")
    languages[1] = "C#"          // 同 languages.set(1, "C#")
    println("after set languages=${languages.contentToString()}")

    // 4. 初始化：使用 Array(size) { index -> ... } 构造，每个元素由 lambda 决定。
    val squares = Array(5) { index -> index * index }
    println("squares=${squares.contentToString()}")

    // 5. 遍历：forEachIndexed 可同时拿到索引和值。
    languages.forEachIndexed { index, value ->
        println("language[$index]=$value")
    }

    // 6. 与集合互转：toList / toMutableList / toTypedArray 方便与标准库 API 搭配。
    val langList = languages.toMutableList()//会把数组转换成 MutableList<String>，变成一个可增删的列表。
    langList += "Rust"
    println("langList=$langList")
    val backToArray = langList.toTypedArray() //toTypedArray() 会把它转换成一个新的 Array<String>
    println("backToArray=${backToArray.contentToString()}")

    // 7. 排序 & 过滤：数组可调用标准库扩展，返回新的集合或数组。
    val sortedDesc = numbers.sortedArrayDescending()
    println("sortedDesc=${sortedDesc.contentToString()}")
    val evenNumbers = numbers.filter { it % 2 == 0 }
    println("evenNumbers=$evenNumbers")

    // 8. copyOf / copyOfRange：复制数组或截取片段，原数组不变。
    val copy = numbers.copyOf() // 深拷贝：新数组，但元素值相同
    val slice = numbers.copyOfRange(1, 3) // [20, 30]
    println("copy=${copy.contentToString()}, slice=${slice.contentToString()}")

    // 9. 多维数组：本质是数组嵌套数组，可继续应用相同 API。
    val matrix = Array(2) { row -> IntArray(3) { col -> row + col } }
    println("matrix=${matrix.contentDeepToString()}")

    // 10. Java 互操作：数组可直接传给期望相同类型数组的 Java API。
    useJavaApi(numbers)

    val array: IntArray = intArrayOf(1, 2, 3, 4, 5, 6);
    for (item: Int in array) {
        println(item)
    }

    for (i: Int in array.indices) {
        println("array[$i]=${array[i]}")
    }

    for ((index, value) in array.withIndex()) {
        println("array[$index]=$value")
    }
}

// 示例 Java API（这里用 Kotlin 写一个假想方法，实战中可调用真正的 Java 库）。
fun useJavaApi(arr: IntArray) {
    println("Java API received size=${arr.size}")
}

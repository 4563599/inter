# Kotlin 语法糖示例项目

这是一个包含 23 个 Kotlin 语法糖示例的学习项目。

## 项目结构

```
kotlin/
├── src/main/kotlin/          # Kotlin 源代码文件
│   ├── 01-类型推断与智能转换.kt
│   ├── 02-可空类型与空安全.kt
│   ├── 03-字符串模板.kt
│   └── ... (更多示例文件)
├── build.gradle.kts          # Gradle 构建配置
└── settings.gradle.kts       # Gradle 设置文件
```

## 如何在 Android Studio 中运行

### 方法 1：导入项目（推荐）

1. 打开 Android Studio
2. 选择 `File` → `Open`
3. 选择这个项目的根目录（包含 `build.gradle.kts` 的目录）
4. 等待 Gradle 同步完成
5. 打开任意 `.kt` 文件
6. 在文件中的 `main()` 函数旁边会出现绿色的运行按钮 ▶️
7. 点击运行按钮即可执行

### 方法 2：右键运行

1. 在项目导入后，在左侧项目树中找到 `src/main/kotlin/` 目录
2. 右键点击任意包含 `main()` 函数的 `.kt` 文件
3. 选择 `Run '文件名Kt'`

### 方法 3：使用命令行

```bash
# Windows
gradlew.bat run

# 运行指定文件（需要修改 build.gradle.kts 中的 mainClass）
gradlew.bat run --args="参数"
```

## 示例文件列表

1. **01-类型推断与智能转换.kt** - 类型推断和智能类型转换
2. **02-可空类型与空安全.kt** - 空安全操作符（?.、?:、!!）
3. **03-字符串模板.kt** - 字符串插值
4. **04-默认参数、命名参数、可变参数.kt** - 函数参数特性
5. **05-顶层函数与属性.kt** - 顶层声明
6. **06-数据类-+-解构-+-copy.kt** - 数据类特性
7. **07-when-表达式.kt** - when 表达式
8. **08-区间、步长、倒序.kt** - 区间操作
9. **09-中缀调用（infix）.kt** - 中缀函数
10. **10-扩展函数与属性.kt** - 扩展功能
11. **11-Lambda-与集合链式操作.kt** - Lambda 和集合操作
12. **12-尾随-Lambda、单参数-it.kt** - Lambda 语法糖
13. **13-作用域函数（let-apply-also-run-with）.kt** - 作用域函数
14. **14-try-表达式.kt** - try 作为表达式
15. **15-属性委托-by-lazy-自定义.kt** - 属性委托
16. **16-接口实现委托.kt** - 接口委托
17. **17-运算符重载.kt** - 运算符重载
18. **18-解构-for-循环.kt** - 解构声明
19. **19-sealed-类（密封类）+-穷尽检查.kt** - 密封类
20. **20-值类（内联类）.kt** - 值类/内联类
21. **21-尾递归优化.kt** - 尾递归优化
22. **22-小型-DSL-示例（构建列表的字符串）.kt** - DSL 构建
23. **23-if-判断语法糖.kt** - `if` 作为表达式、返回值

## 环境要求

- JDK 17 或更高版本
- Android Studio 或 IntelliJ IDEA
- Kotlin 1.9.21

## 故障排除

### 问题：没有运行按钮

**解决方案：**
- 确保已经通过 `File` → `Open` 导入了整个项目（不是单个文件夹）
- 等待 Gradle 同步完成（查看 Android Studio 底部的状态栏）
- 确保文件包含 `main()` 函数
- 尝试 `File` → `Invalidate Caches / Restart`

### 问题：Gradle 同步失败

**解决方案：**
- 检查网络连接（首次同步需要下载依赖）
- 检查 JDK 配置：`File` → `Project Structure` → `SDK`
- 确保 JDK 版本为 17 或更高

## 学习建议

建议按文件编号顺序学习，每个文件都可以独立运行。每个示例都包含了注释说明使用的语法糖特性。

Happy Coding! 🚀


// ========================================
// Kotlin 项目 Gradle 配置文件
// 适用于 Android Studio Otter (2025.2.1) 及其他版本
// ========================================

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// 1. 插件配置 - 声明项目使用的 Gradle 插件
plugins {
    // Kotlin JVM 插件 - 用于编译 Kotlin 代码到 JVM 字节码
    // 版本 1.9.20 是稳定版本，可根据需要调整
    kotlin("jvm") version "1.9.20"
    
    // Application 插件 - 提供运行应用程序的任务（如 run）
    // 使项目可以直接运行 main 函数
    application
}

// 2. 项目基本信息
group = "com.shengsiyuan"
version = "1.0-SNAPSHOT"

// 3. 仓库配置 - 告诉 Gradle 从哪里下载依赖库
repositories {
    // 阿里云镜像（国内访问更快）
    maven { 
        url = uri("https://maven.aliyun.com/repository/public")
        isAllowInsecureProtocol = false
    }
    maven { 
        url = uri("https://maven.aliyun.com/repository/central")
        isAllowInsecureProtocol = false
    }
    maven {
        url = uri("https://maven.aliyun.com/repository/google")
        isAllowInsecureProtocol = false
    }
    
    // Maven 中央仓库（备用）
    mavenCentral()
    
    // Google 仓库（如果需要 Android 相关依赖）
    google()
}

// 4. 依赖配置 - 声明项目需要的外部库
dependencies {
    // Kotlin 标准库 - 包含 Kotlin 的基础 API
    // 所有 Kotlin 项目都需要这个依赖
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.20")
    
    // ========================================
    // 【核心依赖】Kotlin 协程库
    // ========================================
    // 提供协程相关的所有功能，包括：
    // - launch: 启动协程
    // - async: 异步计算
    // - delay: 挂起函数
    // - runBlocking: 阻塞式协程
    // - Flow: 响应式流
    // - Channel: 协程通道
    // - Dispatchers: 调度器
    // 版本 1.7.3 与 Kotlin 1.9.x 完全兼容
    // 没有这个依赖，所有 kotlinx.coroutines.* 的导入都会报错
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    
    // ========================================
    // Kotlin 反射库 - 提供运行时反射功能
    // ========================================
    // kotlin13 包下的反射代码需要这个依赖
    // 包括：KClass, KFunction, KProperty 等反射 API
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.20")
    
    // ========================================
    // Kotlin 测试库 - 提供测试相关的 API
    // ========================================
    // kotlin2 包下的代码使用了 kotlin.test.assertTrue
    // 所以需要在 main 代码中也可用（使用 implementation 而不是 testImplementation）
    implementation("org.jetbrains.kotlin:kotlin-test:1.9.20")
    
    // ========================================
    // 【可选】JUnit 测试依赖
    // ========================================
    // JUnit 测试框架 - 用于单元测试
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.9.20")
}

// 5. Kotlin 编译器配置
kotlin {
    // 注释掉 jvmToolchain，使用默认的 JDK 版本
    // 你的系统 JDK 是 1.8，所以不指定 toolchain
    // jvmToolchain(17)
}

// 6. 应用程序配置（可选）
application {
    // 设置主类 - 用于 ./gradlew run 命令
    // 格式：完整包名.类名Kt（Kotlin 文件会自动添加 Kt 后缀）
    // 根据你要运行的文件修改这里
    mainClass.set("com.shengsiyuan.coroutines.HelloKotlin4Kt")
}

// 7. Java 和 Kotlin 版本统一配置
// 设置 Java 源代码和目标版本
java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

// 8. 任务配置 - 自定义 Gradle 任务行为
tasks {
    // 配置测试任务
    test {
        // 使用 JUnit Platform 运行测试
        useJUnitPlatform()
    }
    
    // 配置 Java 编译任务（统一使用 1.8）
    withType<JavaCompile> {
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
    }
    
    // 配置 Kotlin 编译任务
    compileKotlin {
        // Kotlin 编译器选项
        kotlinOptions {
            // 设置 JVM 目标版本为 1.8（与 Java 保持一致）
            jvmTarget = "1.8"
            
            // 启用渐进模式（使用最新的语言特性）
            freeCompilerArgs = listOf("-progressive")
        }
    }
    
    // 配置 Kotlin 测试编译任务
    compileTestKotlin {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
}

// ========================================
// 配置说明
// ========================================
// 
// 1. 为什么需要 kotlinx-coroutines-core？
//    - 协程不是 Kotlin 语言的一部分，而是一个独立的库
//    - 你的代码中使用了 launch、delay、runBlocking、Flow 等 API
//    - 没有这个依赖会报错：Unresolved reference: kotlinx
//
// 2. 为什么配置了阿里云镜像？
//    - 国内访问 Maven 中央仓库可能很慢
//    - 阿里云镜像可以大幅提升下载速度
//    - 如果阿里云镜像失败，会自动使用 mavenCentral()
//
// 3. 为什么需要 kotlin-reflect？
//    - kotlin13 包下的反射代码需要这个依赖
//    - 提供 KClass、KFunction、KProperty 等反射 API
//    - 如果不使用反射功能，可以删除这个依赖
//
// 4. JVM 目标版本为什么是 17？
//    - 推荐使用 JDK 17（LTS 版本）
//    - 如果你的 JDK 是 11，改为 jvmToolchain(11)
//    - 如果你的 JDK 是 21，改为 jvmToolchain(21)
//
// ========================================

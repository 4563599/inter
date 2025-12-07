// ========================================
// Gradle 项目设置文件
// ========================================

// 设置项目根目录名称
// 这个名称会在 IDE 和 Gradle 输出中显示
rootProject.name = "kotlin-learning"

// ========================================
// 插件管理配置（可选）
// ========================================
pluginManagement {
    repositories {
        // 阿里云镜像（国内访问更快）
        maven { 
            url = uri("https://maven.aliyun.com/repository/gradle-plugin")
        }
        maven { 
            url = uri("https://maven.aliyun.com/repository/public")
        }
        
        // Gradle 插件门户（备用）
        gradlePluginPortal()
        
        // Google 仓库
        google()
        
        // Maven 中央仓库
        mavenCentral()
    }
}

// ========================================
// 依赖解析管理配置（可选）
// ========================================
dependencyResolutionManagement {
    // 仓库模式：优先使用项目中定义的仓库
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
}

// ========================================
// 如果项目有多个模块（子项目），可以在这里包含
// ========================================
// 例如：
// include("module1")
// include("module2")
// include("coroutines-examples")

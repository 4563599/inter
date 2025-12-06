# 线程池快速参考卡 🚀

## 📊 4种线程池对比表

| 类型 | 创建方法 | 核心线程数 | 最大线程数 | 队列 | 适用场景 |
|------|---------|-----------|-----------|------|---------|
| **Fixed** | `newFixedThreadPool(n)` | n | n | 无界队列 | 并发网络请求、批量处理 |
| **Cached** | `newCachedThreadPool()` | 0 | 无限 | 同步队列 | 图片解码、短时IO |
| **Single** | `newSingleThreadExecutor()` | 1 | 1 | 无界队列 | 文件写入、日志记录 |
| **Scheduled** | `newScheduledThreadPool(n)` | n | 无限 | 延时队列 | 定时刷新、心跳检测 |

---

## 💻 代码速查

### 1. FixedThreadPool - 固定线程池
```java
// 创建：限制并发数为3
ExecutorService pool = Executors.newFixedThreadPool(3);

// 提交任务
pool.execute(() -> {
    // 网络请求、数据处理等
});

// 关闭
pool.shutdown();  // 优雅关闭
```

**使用场景**：
- ✅ 并发网络请求（限制数量避免服务器压力）
- ✅ 批量数据处理
- ✅ CPU密集型计算

**线程数建议**：
- CPU密集型：`Runtime.getRuntime().availableProcessors() + 1`
- IO密集型：`CPU核心数 * 2`

---

### 2. CachedThreadPool - 缓存线程池
```java
// 创建：按需创建，空闲60秒回收
ExecutorService pool = Executors.newCachedThreadPool();

// 提交突发任务
for (int i = 0; i < 10; i++) {
    pool.execute(() -> {
        // 图片解码、短时IO操作
    });
}

pool.shutdown();
```

**使用场景**：
- ✅ 图片解码和压缩
- ✅ 短时间IO操作
- ✅ 突发性任务处理

**注意事项**：
- ⚠️ 极端情况可能创建大量线程
- ⚠️ 不适合长时间运行的任务

---

### 3. SingleThreadExecutor - 单线程池
```java
// 创建：保证顺序执行
ExecutorService pool = Executors.newSingleThreadExecutor();

// 提交任务（按顺序执行）
pool.execute(() -> writeToFile("data1"));
pool.execute(() -> writeToFile("data2"));
pool.execute(() -> writeToFile("data3"));

pool.shutdown();
```

**使用场景**：
- ✅ 数据库写入操作
- ✅ 文件写入（避免冲突）
- ✅ 日志记录
- ✅ 状态更新

**优势**：
- 线程安全，无需加锁
- 任务严格按顺序执行
- 简单可靠

---

### 4. ScheduledThreadPool - 定时线程池
```java
// 创建
ScheduledExecutorService pool = Executors.newScheduledThreadPool(1);

// 延时执行（5秒后执行一次）
pool.schedule(() -> {
    System.out.println("延时任务");
}, 5, TimeUnit.SECONDS);

// 固定频率执行（立即开始，每3秒执行一次）
pool.scheduleAtFixedRate(() -> {
    System.out.println("周期任务");
}, 0, 3, TimeUnit.SECONDS);

// 固定间隔执行（任务完成后等待2秒再执行）
pool.scheduleWithFixedDelay(() -> {
    System.out.println("间隔任务");
}, 0, 2, TimeUnit.SECONDS);

pool.shutdown();
```

**使用场景**：
- ✅ 定时数据同步
- ✅ 心跳检测
- ✅ 缓存清理
- ✅ 健康检查

**方法对比**：
- `schedule`: 延时执行一次
- `scheduleAtFixedRate`: 固定频率（不管任务耗时）
- `scheduleWithFixedDelay`: 固定间隔（任务完成后再等待）

---

## 🎯 Android 特殊处理

### 1. UI线程更新
```java
// 使用Handler
private final Handler mainHandler = new Handler(Looper.getMainLooper());

// 在子线程中
pool.execute(() -> {
    // 后台任务
    String result = doNetworkCall();
    
    // 切换到主线程更新UI
    mainHandler.post(() -> {
        textView.setText(result);
    });
});
```

### 2. 生命周期管理
```java
@Override
protected void onDestroy() {
    super.onDestroy();
    // 必须关闭线程池，避免内存泄露
    if (pool != null && !pool.isShutdown()) {
        pool.shutdownNow();
    }
}
```

### 3. 网络权限
```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.INTERNET" />
```

---

## ⚠️ 常见错误

### ❌ 错误做法1：每次new Thread
```java
// ❌ 不要这样做
for (int i = 0; i < 100; i++) {
    new Thread(() -> doTask()).start();
}
```

### ✅ 正确做法：使用线程池
```java
// ✅ 应该这样做
ExecutorService pool = Executors.newFixedThreadPool(10);
for (int i = 0; i < 100; i++) {
    pool.execute(() -> doTask());
}
pool.shutdown();
```

---

### ❌ 错误做法2：忘记关闭
```java
// ❌ 不要这样做
void someMethod() {
    ExecutorService pool = Executors.newFixedThreadPool(5);
    pool.execute(() -> doTask());
    // 忘记关闭，导致线程泄露
}
```

### ✅ 正确做法：及时关闭
```java
// ✅ 应该这样做
ExecutorService pool = null;
try {
    pool = Executors.newFixedThreadPool(5);
    pool.execute(() -> doTask());
} finally {
    if (pool != null) {
        pool.shutdown();
    }
}
```

---

### ❌ 错误做法3：子线程更新UI
```java
// ❌ 不要这样做
pool.execute(() -> {
    String result = doNetworkCall();
    textView.setText(result);  // 崩溃！
});
```

### ✅ 正确做法：切换到主线程
```java
// ✅ 应该这样做
pool.execute(() -> {
    String result = doNetworkCall();
    runOnUiThread(() -> {
        textView.setText(result);
    });
});
```

---

## 🛡️ 关闭方法对比

```java
// 1. shutdown() - 优雅关闭
pool.shutdown();
// - 不再接受新任务
// - 等待已提交的任务完成
// - 推荐用法

// 2. shutdownNow() - 立即关闭
List<Runnable> tasks = pool.shutdownNow();
// - 立即停止所有任务
// - 返回等待执行的任务列表
// - 尝试中断正在执行的任务

// 3. awaitTermination() - 等待终止
pool.shutdown();
pool.awaitTermination(5, TimeUnit.SECONDS);
// - 阻塞等待线程池完全终止
// - 超时返回false

// 4. 完整关闭流程
pool.shutdown();
try {
    if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
        pool.shutdownNow();
        if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
            System.err.println("线程池无法终止");
        }
    }
} catch (InterruptedException e) {
    pool.shutdownNow();
    Thread.currentThread().interrupt();
}
```

---

## 📚 选择决策树

```
开始
  │
  ├─ 需要定时或周期执行？
  │   └─ 是 → ScheduledThreadPool
  │
  ├─ 必须顺序执行？
  │   └─ 是 → SingleThreadExecutor
  │
  ├─ 短时间突发任务？
  │   └─ 是 → CachedThreadPool
  │
  └─ 需要控制并发数？
      └─ 是 → FixedThreadPool
```

---

## 🎓 学习资源

1. **本项目代码**：`ThreadActivity.java`
   - 600+行详细注释
   - 5个完整示例
   - 真实应用场景

2. **运行项目**：
   - 点击按钮 A → B → C → D → E
   - 观察日志输出
   - 理解线程池行为

3. **官方文档**：
   - [Java ExecutorService](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ExecutorService.html)
   - [Android Threading](https://developer.android.com/guide/background)

---

## ✅ 检查清单

启动 ThreadActivity 前确认：

- [ ] AndroidManifest.xml 中 ThreadActivity 设置为 LAUNCHER
- [ ] INTERNET 权限已添加
- [ ] Gradle 已同步
- [ ] 项目已构建成功
- [ ] 设备/模拟器已连接（API ≥ 24）

运行应用后：

- [ ] 可以看到5个按钮
- [ ] 点击按钮后有日志输出
- [ ] 网络请求能正常执行
- [ ] 文件能正常写入缓存目录

---

**祝学习顺利！🎉**

有任何问题请查看代码中的详细注释，或参考《解决方案.md》。


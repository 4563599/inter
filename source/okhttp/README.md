# MyOkHttp - 从零实现 OkHttp 源码

这是一个教学项目，通过手写 OkHttp 的核心功能，帮助你深入理解其架构设计和实现原理。

## 📁 项目结构

```
android/
├── OkHttp源码学习指南.md          # 核心概念和知识点
├── 分步实现教程.md                 # 详细的实现步骤
├── README.md                       # 项目说明（本文件）
└── src/main/java/com/myokhttp/
    ├── Call.java                   # 请求执行接口
    ├── RealCall.java               # Call 的实现
    ├── OkHttpClient.java           # HTTP 客户端
    ├── Request.java                # 请求对象
    ├── Response.java               # 响应对象
    ├── RequestBody.java            # 请求体
    ├── ResponseBody.java           # 响应体
    ├── Interceptor.java            # 拦截器接口
    ├── RealInterceptorChain.java   # 拦截器链实现
    ├── Dispatcher.java             # 异步请求调度器
    ├── ConnectionPool.java         # 连接池
    ├── RealConnection.java         # 真实连接
    ├── RetryAndFollowUpInterceptor.java    # 重试和重定向
    ├── BridgeInterceptor.java              # 桥接拦截器
    ├── ConnectInterceptor.java             # 连接拦截器
    ├── CallServerInterceptor.java          # 服务器请求拦截器
    └── examples/                           # 示例代码
        ├── SimpleGetExample.java           # 简单 GET 请求
        ├── PostExample.java                # POST 请求
        ├── AsyncExample.java               # 异步请求
        ├── InterceptorExample.java         # 自定义拦截器
        ├── ConnectionPoolExample.java      # 连接池测试
        └── RedirectExample.java            # 重定向测试
```

## 🎯 学习目标

通过本项目，你将学会：

1. **责任链模式**：拦截器链的设计和实现
2. **Builder 模式**：如何优雅地构建不可变对象
3. **连接池管理**：TCP 连接的复用和管理
4. **HTTP 协议**：请求/响应的格式和解析
5. **同步/异步**：线程池和回调机制
6. **网络编程**：Socket 编程和 I/O 操作

## 🚀 快速开始

### 1. 简单的 GET 请求

```java
OkHttpClient client = new OkHttpClient.Builder().build();

Request request = new Request.Builder()
    .url("http://httpbin.org/get")
    .get()
    .build();

Response response = client.newCall(request).execute();
System.out.println(response.body().string());
```

### 2. POST 请求

```java
String json = "{\"name\":\"张三\",\"age\":25}";
RequestBody body = RequestBody.createJson(json);

Request request = new Request.Builder()
    .url("http://httpbin.org/post")
    .post(body)
    .build();

Response response = client.newCall(request).execute();
```

### 3. 异步请求

```java
client.newCall(request).enqueue(new Call.Callback() {
    @Override
    public void onResponse(Call call, Response response) {
        System.out.println("成功: " + response.code());
    }
    
    @Override
    public void onFailure(Call call, IOException e) {
        System.out.println("失败: " + e.getMessage());
    }
});
```

### 4. 自定义拦截器

```java
Interceptor loggingInterceptor = new Interceptor() {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        System.out.println("发送请求: " + request.url());
        
        long startTime = System.currentTimeMillis();
        Response response = chain.proceed(request);
        long duration = System.currentTimeMillis() - startTime;
        
        System.out.println("收到响应: " + response.code() + ", 耗时: " + duration + "ms");
        return response;
    }
};

OkHttpClient client = new OkHttpClient.Builder()
    .addInterceptor(loggingInterceptor)
    .build();
```

## 📖 学习路径

### 第一阶段：理解架构
- 阅读 `OkHttp源码学习指南.md`
- 理解核心组件和设计模式
- 画出架构图

### 第二阶段：动手实现
- 按照 `分步实现教程.md` 逐步实现
- 先实现数据类（Request, Response）
- 再实现拦截器链
- 最后实现连接池

### 第三阶段：运行示例
- 运行 `examples` 目录下的示例代码
- 观察输出，理解执行流程
- 尝试修改代码，加深理解

### 第四阶段：对比源码
- 阅读真实的 OkHttp 源码
- 对比你的实现和真实实现
- 学习更多的优化技巧

## 🎨 核心设计

### 1. 拦截器链（责任链模式）

```
Application Interceptor（应用拦截器）
    ↓
RetryAndFollowUpInterceptor（重试和重定向）
    ↓
BridgeInterceptor（补充 HTTP 头）
    ↓
CacheInterceptor（缓存）
    ↓
ConnectInterceptor（建立连接）
    ↓
Network Interceptor（网络拦截器）
    ↓
CallServerInterceptor（真实网络请求）
```

**为什么这个顺序？**
- 应用拦截器：看到原始请求，可以添加自定义逻辑
- 重试拦截器：处理失败和重定向
- 桥接拦截器：补充必要的 HTTP 头
- 缓存拦截器：避免不必要的网络请求
- 连接拦截器：复用 TCP 连接
- 服务器拦截器：执行真实的网络 I/O

### 2. 连接池

```
ConnectionPool
    ↓
检查是否有可复用的连接
    ↓
如果有 → 使用现有连接
    ↓
如果没有 → 创建新连接
    ↓
使用完毕 → 放回连接池
    ↓
定期清理空闲连接
```

**为什么需要连接池？**
- TCP 连接的建立很耗时（三次握手）
- HTTPS 还需要 TLS 握手
- 连接复用可以显著提升性能

### 3. 同步 vs 异步

**同步请求（execute）：**
- 阻塞当前线程
- 直到获得响应或异常
- 适合后台任务

**异步请求（enqueue）：**
- 提交到线程池
- 通过回调返回结果
- 不阻塞当前线程
- 适合 UI 线程

## 💡 核心知识点

### 1. 不可变对象
Request 和 Response 都是不可变的（所有字段都是 final），确保线程安全。

### 2. Builder 模式
使用 Builder 模式构建复杂对象，链式调用，可读性强。

### 3. 责任链模式
拦截器链是 OkHttp 最核心的设计，每个拦截器专注于一个职责。

### 4. 连接复用
通过连接池复用 TCP 连接，大幅提升性能。

### 5. 线程池管理
Dispatcher 管理异步请求，控制并发数量，避免资源耗尽。

## 🔍 与真实 OkHttp 的对比

| 特性 | MyOkHttp（教学版） | 真实 OkHttp |
|------|-------------------|-------------|
| HTTP 支持 | ✅ HTTP | ✅ HTTP + HTTPS |
| HTTP/2 | ❌ | ✅ |
| WebSocket | ❌ | ✅ |
| 缓存 | ❌ | ✅ 完整的 HTTP 缓存 |
| 连接池 | ✅ 简化版 | ✅ 完整版（支持 HTTP/2） |
| DNS | ❌ | ✅ DNS 预解析 |
| 代理 | ❌ | ✅ 支持多种代理 |
| 证书验证 | ❌ | ✅ 完整的证书链验证 |
| Gzip | 🔶 识别但未解压 | ✅ 透明解压 |

## 🧪 测试建议

### 1. 功能测试
- 测试 GET、POST、PUT、DELETE 等方法
- 测试不同的请求头和请求体
- 测试超时、重试、重定向

### 2. 性能测试
- 测试连接复用的性能提升
- 测试并发请求的表现
- 测试内存和 CPU 使用

### 3. 异常测试
- 测试网络不可用的情况
- 测试服务器返回错误的情况
- 测试超时的情况

## 📚 推荐阅读

1. **OkHttp 官方文档**
   - https://square.github.io/okhttp/

2. **OkHttp 源码**
   - https://github.com/square/okhttp

3. **HTTP 协议规范**
   - RFC 7230-7235

4. **设计模式**
   - 《设计模式：可复用面向对象软件的基础》

5. **Java 网络编程**
   - 《Java 网络编程》

## 🤔 常见问题

### Q: 为什么每个 Call 只能执行一次？
A: 因为 Call 内部维护了执行状态，执行后可能会修改一些内部状态。如果需要重复执行，使用 `call.clone()` 创建新的 Call。

### Q: 为什么需要这么多拦截器？
A: 每个拦截器专注于一个职责，符合单一职责原则。这样代码更清晰，也更容易扩展和维护。

### Q: 连接池是如何判断连接可以复用的？
A: 检查 host 和 port 是否相同，连接是否已关闭，连接是否正在使用中。

### Q: 异步请求的回调在哪个线程？
A: 在 Dispatcher 的线程池中执行，不是主线程。如果要更新 UI，需要切换到主线程。

### Q: 为什么要使用 Builder 模式？
A: Builder 模式可以优雅地构建不可变对象，链式调用可读性强，而且可以设置默认值。

## 📝 作业

1. **实现缓存拦截器**
   - 支持基本的 HTTP 缓存
   - 理解 Cache-Control、ETag 等缓存头

2. **支持 HTTPS**
   - 学习 SSL/TLS
   - 实现证书验证

3. **优化连接池**
   - 支持每个 host 的最大连接数
   - 更智能的连接清理策略

4. **添加更多测试**
   - 单元测试
   - 集成测试
   - 性能测试

## 🙏 致谢

本项目参考了 [Square OkHttp](https://github.com/square/okhttp) 的设计思想，感谢 Square 团队的优秀工作。

## 📄 许可证

本项目仅用于教学目的，请勿用于生产环境。

---

**开始你的 OkHttp 源码学习之旅吧！** 🚀

如果你有任何问题或建议，欢迎提 Issue！


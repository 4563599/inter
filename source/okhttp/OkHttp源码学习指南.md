# OkHttp 源码学习指南

## 📚 学习目标

通过手写一个简化版的 OkHttp，深入理解以下核心知识点：
1. **责任链模式**（拦截器链）
2. **连接池管理**
3. **HTTP 协议实现**
4. **同步/异步请求处理**
5. **缓存机制**
6. **重试和重定向**

---

## 🏗️ OkHttp 核心架构

```
OkHttpClient (客户端配置)
    ↓
Request (请求对象)
    ↓
Call (请求执行器)
    ↓
Interceptor Chain (拦截器链)
    ↓
RealCall (真实网络请求)
    ↓
Response (响应对象)
```

---

## 📖 第一章：核心概念

### 1.1 Request（请求）
包含：
- URL（请求地址）
- Method（GET, POST, PUT, DELETE 等）
- Headers（请求头）
- Body（请求体）

### 1.2 Response（响应）
包含：
- Status Code（状态码）
- Headers（响应头）
- Body（响应体）
- Protocol（协议版本）

### 1.3 Call（调用）
表示一个已准备好执行的请求，可以：
- `execute()` - 同步执行
- `enqueue()` - 异步执行
- `cancel()` - 取消请求

### 1.4 OkHttpClient（客户端）
管理配置和资源：
- 连接池
- 拦截器列表
- 超时设置
- 代理设置等

---

## 📖 第二章：拦截器链（核心设计）

### 2.1 什么是拦截器？
拦截器是一个**责任链模式**的实现，每个拦截器都可以：
1. 在请求发送前处理 Request
2. 决定是否继续链的执行
3. 在响应返回后处理 Response

### 2.2 拦截器的顺序（重要！）
```
Application Interceptors（应用拦截器）
    ↓
RetryAndFollowUpInterceptor（重试和重定向）
    ↓
BridgeInterceptor（桥接拦截器：补充 Headers）
    ↓
CacheInterceptor（缓存拦截器）
    ↓
ConnectInterceptor（连接拦截器）
    ↓
CallServerInterceptor（真实网络请求）
```

### 2.3 为什么这个顺序？
1. **应用拦截器**：最外层，可以看到原始请求
2. **重试拦截器**：处理失败重试和重定向
3. **桥接拦截器**：补充必要的 HTTP 头（如 Content-Type, User-Agent）
4. **缓存拦截器**：避免不必要的网络请求
5. **连接拦截器**：建立与服务器的连接
6. **服务器拦截器**：最内层，执行真实的网络 I/O

---

## 📖 第三章：连接池（Connection Pool）

### 3.1 为什么需要连接池？
- HTTP/1.1 支持 Keep-Alive，可以复用 TCP 连接
- 建立 TCP 连接的开销很大（三次握手）
- 连接池可以显著提升性能

### 3.2 连接池的核心逻辑
```
1. 检查是否有可复用的连接
2. 如果有，直接使用
3. 如果没有，创建新连接
4. 使用完毕后，放回连接池
5. 定期清理空闲连接
```

### 3.3 连接的状态
- **Idle**（空闲）：可以被复用
- **Active**（活动）：正在使用
- **Expired**（过期）：需要关闭

---

## 📖 第四章：同步 vs 异步

### 4.1 同步请求（execute）
```java
Response response = client.newCall(request).execute();
// 阻塞当前线程，直到获得响应
```

### 4.2 异步请求（enqueue）
```java
client.newCall(request).enqueue(new Callback() {
    @Override
    public void onResponse(Response response) {
        // 在工作线程回调
    }
    
    @Override
    public void onFailure(Exception e) {
        // 处理失败
    }
});
```

### 4.3 线程池管理
- 使用 `Dispatcher` 管理异步请求
- 控制并发请求数量
- 管理等待队列

---

## 📖 第五章：HTTP 协议细节

### 5.1 请求格式
```
GET /api/users HTTP/1.1
Host: example.com
User-Agent: MyOkHttp/1.0
Accept: application/json
Connection: keep-alive

[请求体]
```

### 5.2 响应格式
```
HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 123
Connection: keep-alive

[响应体]
```

### 5.3 需要处理的关键点
- 状态行解析
- Header 解析
- Chunked 编码
- Gzip 压缩
- Content-Length vs Transfer-Encoding

---

## 📖 第六章：缓存机制

### 6.1 HTTP 缓存头
- `Cache-Control`：缓存策略
- `Expires`：过期时间
- `ETag`：资源标识
- `Last-Modified`：最后修改时间

### 6.2 缓存策略
1. **强制缓存**：不发请求，直接用缓存
2. **对比缓存**：发请求验证，304 则用缓存
3. **不缓存**：每次都发请求

---

## 📖 第七章：错误处理

### 7.1 需要重试的情况
- 网络超时
- DNS 解析失败
- 连接失败
- 服务器 503（暂时不可用）

### 7.2 需要重定向的情况
- 301 Moved Permanently
- 302 Found
- 307 Temporary Redirect
- 308 Permanent Redirect

### 7.3 不应该重试的情况
- 4xx 客户端错误（除了 408 超时）
- SSL 握手失败
- 请求被取消

---

## 📖 第八章：实现步骤

### Step 1: 创建基础数据类
- Request
- Response
- Headers
- RequestBody
- ResponseBody

### Step 2: 实现 Call 和 RealCall
- execute() 同步执行
- enqueue() 异步执行
- cancel() 取消

### Step 3: 实现拦截器接口
- Interceptor 接口
- Chain 接口
- RealInterceptorChain 实现

### Step 4: 实现各个拦截器
- RetryAndFollowUpInterceptor
- BridgeInterceptor
- CacheInterceptor
- ConnectInterceptor
- CallServerInterceptor

### Step 5: 实现连接池
- RealConnection（真实连接）
- ConnectionPool（连接池）
- 连接复用逻辑

### Step 6: 实现 OkHttpClient
- Builder 模式
- 配置管理
- newCall() 方法

### Step 7: 实现 Dispatcher
- 异步任务管理
- 线程池
- 队列管理

---

## 🎯 核心知识点总结

### 1. 设计模式
- **Builder 模式**：OkHttpClient, Request 的构建
- **责任链模式**：拦截器链
- **工厂模式**：Call 的创建
- **策略模式**：缓存策略

### 2. 并发编程
- 线程池的使用
- 同步与异步
- 线程安全的连接池

### 3. 网络编程
- Socket 编程
- HTTP 协议
- TCP 连接复用
- SSL/TLS

### 4. I/O 操作
- InputStream/OutputStream
- Buffer 的使用
- 流的关闭和异常处理

---

## 📝 学习建议

1. **先理解，再编码**：每写一个类之前，先理解它的职责
2. **逐步实现**：不要一次写完，先实现最简单的版本
3. **对比源码**：实现后对比 OkHttp 真实源码，看看差异
4. **写测试**：为每个功能写测试用例
5. **画图理解**：用流程图画出请求的完整流程

---

## 🔗 参考资源

- OkHttp 官方源码：https://github.com/square/okhttp
- HTTP/1.1 规范：RFC 7230-7235
- Java 网络编程文档

---

## 💡 面试常见问题

1. OkHttp 的拦截器链是如何工作的？
2. 连接池是如何实现连接复用的？
3. OkHttp 如何处理 HTTPS 请求？
4. 同步请求和异步请求的区别？
5. OkHttp 的缓存策略是怎样的？
6. 如何设计一个高性能的 HTTP 客户端？

---

**准备好了吗？让我们开始实现吧！** 🚀


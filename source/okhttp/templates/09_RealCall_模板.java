package com.myokhttp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Call 的真实实现
 * 
 * 【为什么需要这个类？】
 * - 实现 Call 接口的所有方法
 * - 管理请求的执行状态
 * - 构建和执行拦截器链
 * 
 * 【核心职责】
 * 1. 状态管理：executed, canceled
 * 2. 同步执行：execute()
 * 3. 异步执行：enqueue()
 * 4. 构建拦截器链：getResponseWithInterceptorChain()
 * 
 * 【这是连接用户和拦截器链的桥梁】
 * 用户代码 → RealCall → 拦截器链 → 网络请求
 * 
 * @author Your Name
 */
public class RealCall implements Call {
    
    // ========== 字段定义 ==========
    
    /**
     * OkHttpClient 实例
     * 
     * 【为什么需要？】
     * - 获取配置（超时、拦截器等）
     * - 获取资源（连接池、调度器）
     */
    private final OkHttpClient client;
    
    /**
     * 原始请求
     * 
     * 【为什么保存原始请求？】
     * - request() 方法需要返回
     * - 拦截器可能会修改请求，保存原始的
     */
    private final Request originalRequest;
    
    /**
     * 是否已执行
     * 
     * 【为什么需要这个标志？】
     * - Call 只能执行一次
     * - 防止重复执行
     */
    private boolean executed;
    
    /**
     * 是否已取消
     * 
     * 【如何工作？】
     * - cancel() 设置为 true
     * - 拦截器中检查这个标志
     * - 尽快停止执行
     */
    private boolean canceled;

    // ========== 构造函数 ==========
    
    /**
     * 构造函数
     * 
     * 【包级别访问权限】
     * - 只能被 OkHttpClient.newCall() 调用
     * - 用户不能直接 new RealCall()
     */
    public RealCall(OkHttpClient client, Request request) {
        // TODO: 初始化字段
        // this.client = client;
        // this.originalRequest = request;
        
    }

    // ========== 实现 Call 接口 ==========
    
    @Override
    public Request request() {
        // TODO: 返回原始请求
        return null;
    }

    /**
     * 同步执行请求
     * 
     * ⭐⭐⭐⭐⭐ 核心方法之一
     * 
     * 【执行流程】
     * 1. 检查是否已执行
     * 2. 通知 Dispatcher 开始执行
     * 3. 执行拦截器链
     * 4. 通知 Dispatcher 执行完成
     * 5. 返回响应
     * 
     * 【为什么用 synchronized？】
     * - 保护 executed 标志
     * - 防止并发执行
     * 
     * 【为什么用 try-finally？】
     * - 确保 finished() 一定被调用
     * - 即使发生异常也要清理资源
     */
    @Override
    public Response execute() throws IOException {
        // TODO: 步骤 1 - 检查是否已执行
        // synchronized (this) {
        //     if (executed) {
        //         throw new IllegalStateException("每个 Call 只能执行一次");
        //     }
        //     executed = true;
        // }

        // TODO: 步骤 2-5
        // try {
        //     // 2. 通知 Dispatcher
        //     client.dispatcher().executed(this);
        //     
        //     // 3. 执行拦截器链
        //     return getResponseWithInterceptorChain();
        // } finally {
        //     // 4. 通知完成
        //     client.dispatcher().finished(this);
        // }
        
        return null;
    }

    /**
     * 异步执行请求
     * 
     * 【执行流程】
     * 1. 检查是否已执行
     * 2. 创建 AsyncCall
     * 3. 提交给 Dispatcher
     * 4. Dispatcher 在线程池中执行
     * 
     * 【与 execute() 的区别】
     * - execute()：阻塞当前线程
     * - enqueue()：提交到线程池，立即返回
     */
    @Override
    public void enqueue(Callback callback) {
        // TODO: 步骤 1 - 检查是否已执行
        // synchronized (this) {
        //     if (executed) {
        //         throw new IllegalStateException("每个 Call 只能执行一次");
        //     }
        //     executed = true;
        // }

        // TODO: 步骤 2-3
        // 创建 AsyncCall 并提交给 Dispatcher
        // client.dispatcher().enqueue(new AsyncCall(callback));
        
    }

    @Override
    public void cancel() {
        // TODO: 设置 canceled 标志
        // canceled = true;
        
    }

    @Override
    public boolean isExecuted() {
        // TODO: 返回 executed
        return false;
    }

    @Override
    public boolean isCanceled() {
        // TODO: 返回 canceled
        return false;
    }

    @Override
    public Call clone() {
        // TODO: 创建新的 RealCall
        // return new RealCall(client, originalRequest);
        return null;
    }

    // ========== 核心方法：构建拦截器链 ==========
    
    /**
     * 通过拦截器链获取响应
     * 
     * ⭐⭐⭐⭐⭐ 这是最核心的方法！！！
     * 
     * 【职责】
     * 1. 构建完整的拦截器链
     * 2. 按正确的顺序添加拦截器
     * 3. 创建 RealInterceptorChain
     * 4. 执行链并返回响应
     * 
     * 【拦截器的顺序（非常重要！）】
     * 1. Application Interceptors（用户添加）
     *    - 最外层
     *    - 看到完整的原始请求
     *    - 不受重试、重定向影响
     * 
     * 2. RetryAndFollowUpInterceptor（重试和重定向）
     *    - 处理请求失败的重试
     *    - 处理 3xx 重定向
     * 
     * 3. BridgeInterceptor（桥接拦截器）
     *    - 补充必要的 HTTP 头
     *    - User-Agent, Content-Type 等
     * 
     * 4. CacheInterceptor（缓存拦截器）
     *    - 检查缓存
     *    - 避免不必要的网络请求
     *    - 我们的简化版没有实现
     * 
     * 5. ConnectInterceptor（连接拦截器）
     *    - 从连接池获取连接
     *    - 建立 TCP 连接
     * 
     * 6. Network Interceptors（用户添加）
     *    - 看到实际发送到服务器的请求
     *    - 可能被调用多次（重试）
     * 
     * 7. CallServerInterceptor（服务器调用拦截器）
     *    - 最内层
     *    - 执行真实的网络 I/O
     * 
     * 【为什么这个顺序？】
     * - 应用拦截器：最外层，看到原始请求
     * - 重试拦截器：需要在其他拦截器之前，才能重试整个流程
     * - 桥接拦截器：补充 HTTP 头，为后续拦截器准备
     * - 缓存拦截器：在连接之前检查缓存
     * - 连接拦截器：建立连接，为网络请求准备
     * - 网络拦截器：看到实际的网络请求
     * - 服务器拦截器：最内层，执行真实请求
     * 
     * @return 响应对象
     * @throws IOException 执行失败时抛出
     */
    private Response getResponseWithInterceptorChain() throws IOException {
        // TODO: 步骤 1 - 创建拦截器列表
        // List<Interceptor> interceptors = new ArrayList<>();
        
        // TODO: 步骤 2 - 按顺序添加拦截器
        
        // 2.1 添加应用拦截器（用户添加）
        // interceptors.addAll(client.interceptors());
        
        // 2.2 添加重试和重定向拦截器
        // interceptors.add(new RetryAndFollowUpInterceptor(client));
        
        // 2.3 添加桥接拦截器
        // interceptors.add(new BridgeInterceptor());
        
        // 2.4 添加缓存拦截器（我们的简化版跳过）
        // interceptors.add(new CacheInterceptor(client.cache()));
        
        // 2.5 添加连接拦截器
        // interceptors.add(new ConnectInterceptor(client));
        
        // 2.6 添加网络拦截器（用户添加）
        // interceptors.addAll(client.networkInterceptors());
        
        // 2.7 添加服务器调用拦截器
        // interceptors.add(new CallServerInterceptor());

        // TODO: 步骤 3 - 创建拦截器链
        // Interceptor.Chain chain = new RealInterceptorChain(
        //     interceptors,           // 拦截器列表
        //     0,                      // 起始索引
        //     originalRequest,        // 原始请求
        //     client,                 // OkHttpClient
        //     client.connectTimeoutMillis(),  // 连接超时
        //     client.readTimeoutMillis(),     // 读取超时
        //     client.writeTimeoutMillis()     // 写入超时
        // );

        // TODO: 步骤 4 - 执行链
        // return chain.proceed(originalRequest);
        
        return null;
    }

    // ========== 异步请求的内部类 ==========
    
    /**
     * 异步请求的包装类
     * 
     * 【为什么需要这个类？】
     * - 实现 Runnable，可以提交到线程池
     * - 封装回调逻辑
     * - 处理异常和取消
     * 
     * 【为什么是内部类？】
     * - 可以访问 RealCall 的字段
     * - 逻辑上属于 RealCall
     */
    class AsyncCall implements Runnable {
        private final Callback callback;

        AsyncCall(Callback callback) {
            this.callback = callback;
        }

        /**
         * 在线程池中执行
         * 
         * 【执行流程】
         * 1. 执行拦截器链
         * 2. 检查是否已取消
         * 3. 调用回调
         * 4. 通知 Dispatcher 完成
         */
        @Override
        public void run() {
            // TODO: 实现异步执行逻辑
            
            // boolean signalledCallback = false;
            // try {
            //     // 1. 执行请求
            //     Response response = getResponseWithInterceptorChain();
            //     
            //     // 2. 检查是否已取消
            //     if (canceled) {
            //         signalledCallback = true;
            //         callback.onFailure(RealCall.this, new IOException("Canceled"));
            //     } else {
            //         signalledCallback = true;
            //         callback.onResponse(RealCall.this, response);
            //     }
            // } catch (IOException e) {
            //     // 3. 处理异常
            //     if (!signalledCallback) {
            //         callback.onFailure(RealCall.this, e);
            //     }
            // } finally {
            //     // 4. 通知 Dispatcher
            //     client.dispatcher().finished(this);
            // }
            
        }

        /**
         * 获取外部的 RealCall
         * 
         * 【为什么需要这个方法？】
         * - Dispatcher 需要获取 RealCall
         * - 用于管理请求
         */
        public RealCall get() {
            return RealCall.this;
        }
    }
}

/*
【编写提示】

1. 【最重要：理解拦截器的顺序】
   这是 OkHttp 设计的核心，顺序错了功能就错了！
   
   画图理解：
   ```
   用户请求
      ↓
   [Application Interceptor] - 添加全局 header
      ↓
   [RetryAndFollowUp] - 处理重试和重定向
      ↓
   [Bridge] - 补充 HTTP 头
      ↓
   [Cache] - 检查缓存（简化版跳过）
      ↓
   [Connect] - 建立连接
      ↓
   [Network Interceptor] - 记录网络数据
      ↓
   [CallServer] - 执行网络请求
      ↓
   服务器
   ```

2. 【execute() 和 enqueue() 的区别】
   
   execute()：
   ```java
   Response response = call.execute();  // 阻塞
   // 这里才能用 response
   ```
   
   enqueue()：
   ```java
   call.enqueue(new Callback() {
       @Override
       public void onResponse(Call call, Response response) {
           // 在工作线程中回调
           // 不是主线程！
       }
       
       @Override
       public void onFailure(Call call, IOException e) {
           // 处理失败
       }
   });
   // 这里立即返回，不能用 response
   ```

3. 【为什么用 synchronized？】
   ```java
   synchronized (this) {
       if (executed) {
           throw new IllegalStateException("已执行");
       }
       executed = true;
   }
   ```
   
   防止并发执行：
   - 线程1：检查 executed=false，准备执行
   - 线程2：检查 executed=false，准备执行
   - 结果：两个线程都执行了（错误！）
   
   加锁后：
   - 线程1：获得锁，设置 executed=true
   - 线程2：等待锁，发现 executed=true，抛出异常

【使用示例】

同步：
```java
OkHttpClient client = new OkHttpClient();
Request request = new Request.Builder()
    .url("http://example.com")
    .build();

Call call = client.newCall(request);
try {
    Response response = call.execute();
    System.out.println(response.body().string());
} catch (IOException e) {
    e.printStackTrace();
}
```

异步：
```java
Call call = client.newCall(request);
call.enqueue(new Callback() {
    @Override
    public void onFailure(Call call, IOException e) {
        System.out.println("失败: " + e.getMessage());
    }
    
    @Override
    public void onResponse(Call call, Response response) {
        System.out.println("成功: " + response.code());
    }
});
```

【常见错误】

❌ 错误1：拦截器顺序错误
```java
interceptors.add(new CallServerInterceptor());  // 先加了最后的
interceptors.add(new RetryAndFollowUpInterceptor());  // 错误！
```

❌ 错误2：忘记 try-finally
```java
client.dispatcher().executed(this);
return getResponseWithInterceptorChain();
// 忘记 finally 中调用 finished()
```

❌ 错误3：异步回调中没有检查 signalledCallback
```java
try {
    response = getResponseWithInterceptorChain();
    callback.onResponse(call, response);
} catch (IOException e) {
    callback.onFailure(call, e);  // 可能已经回调了 onResponse！
}
```

【测试方法】
```java
// 测试同步执行
Call call = client.newCall(request);
Response response = call.execute();
System.out.println(response.isSuccessful());

// 测试只能执行一次
try {
    call.execute();  // 第二次，应该抛出异常
} catch (IllegalStateException e) {
    System.out.println("正确：不能执行两次");
}

// 测试克隆
Call call2 = call.clone();
Response response2 = call2.execute();  // OK
```

【预计编写时间】1 小时 15 分钟

【难度】⭐⭐⭐⭐⭐

【重点】
- 理解拦截器的顺序（最重要！）
- 理解同步和异步的区别
- 理解为什么只能执行一次
- 掌握 try-finally 的使用
*/


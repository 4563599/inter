package com.myokhttp;

import java.io.IOException;

/**
 * 拦截器接口 - OkHttp 最核心的设计！
 * 
 * 【为什么需要拦截器？】
 * - 分离关注点：每个拦截器专注一个功能
 * - 易于扩展：添加新功能只需加一个拦截器
 * - 灵活组合：可以自由组合不同的拦截器
 * 
 * 【拦截器能做什么？】
 * 1. 修改请求：
 *    - 添加请求头（如 Authorization）
 *    - 修改 URL（如添加公共参数）
 *    - 替换请求体
 * 
 * 2. 短路请求：
 *    - 直接返回缓存，不继续执行
 *    - 请求被拒绝，返回错误响应
 * 
 * 3. 修改响应：
 *    - 解压 gzip
 *    - 添加响应头
 *    - 修改响应体
 * 
 * 4. 其他：
 *    - 记录日志
 *    - 统计耗时
 *    - 监控性能
 * 
 * 【责任链模式】
 * 拦截器链就是责任链模式的实现：
 * - 多个处理器（拦截器）串成一条链
 * - 请求沿着链传递
 * - 每个处理器都可以处理或传递请求
 * 
 * @author Your Name
 */
public interface Interceptor {
    
    /**
     * 拦截方法 - 唯一需要实现的方法
     * 
     * 【典型实现】
     * @Override
     * public Response intercept(Chain chain) throws IOException {
     *     Request request = chain.request();
     *     
     *     // 1. 处理请求（可选）
     *     Request newRequest = request.newBuilder()
     *         .header("User-Ag ent", "MyApp")
     *         .build();
     *     
     *     // 2. 继续执行链
     *     Response response = chain.proceed(newRequest);
     *     
     *     // 3. 处理响应（可选）
     *     Response newResponse = response.newBuilder()
     *         .header("Cache-Control", "max-age=3600")
     *         .build();
     *     
     *     return newResponse;
     * }
     * 
     * 【三个关键步骤】
     * 1. 从 chain 获取 request
     * 2. 调用 chain.proceed() 继续执行
     * 3. 返回 response
     * 
     * 【注意事项】
     * - 必须调用 chain.proceed()，否则请求不会继续
     * - 只能调用一次 proceed()
     * - 可以不调用 proceed()，直接返回响应（短路）
     * 
     * @param chain 拦截器链
     * @return 响应对象
     * @throws IOException 执行失败时抛出
     */
    Response intercept(Chain chain) throws IOException;
    
    /**
     * 拦截器链接口
     * 
     * 【为什么需要这个接口？】
     * - 封装链的状态和操作
     * - 提供 proceed() 方法继续执行
     * - 提供获取配置的方法
     * 
     * 【Chain 的职责】
     * 1. 持有当前请求
     * 2. 提供 proceed() 继续执行下一个拦截器
     * 3. 提供 client() 获取 OkHttpClient
     * 4. 提供超时配置
     */
    interface Chain {
        /**
         * 获取当前的请求
         * 
         * 【为什么需要？】
         * - 拦截器需要知道当前处理的请求
         * - 可能需要修改请求
         * 
         * @return 当前请求
         */
        Request request();
        
        /**
         * 继续执行下一个拦截器
         * 
         * ⭐⭐⭐⭐⭐ 这是最核心的方法！
         * 
         * 【工作原理】
         * - 创建新的 Chain（索引+1）
         * - 调用下一个拦截器的 intercept()
         * - 形成递归调用
         * 
         * 【执行流程】
         * Interceptor A:
         *   → chain.proceed(request)
         *       → Interceptor B:
         *           → chain.proceed(request)
         *               → Interceptor C:
         *                   → 返回 Response
         *               ← 处理 Response
         *           ← 返回 Response
         *       ← 处理 Response
         * 
         * 【为什么可以传入不同的 request？】
         * - 允许拦截器修改请求
         * - 传入新的 request 继续执行
         * 
         * 【注意】
         * - 只能调用一次
         * - 必须传入非 null 的 request
         * 
         * @param request 要继续执行的请求
         * @return 响应对象
         * @throws IOException 执行失败时抛出
         */
        Response proceed(Request request) throws IOException;
        
        /**
         * 获取 OkHttpClient 实例
         * 
         * 【为什么需要？】
         * - 访问客户端配置
         * - 获取连接池、调度器等资源
         * 
         * @return OkHttpClient 实例
         */
        OkHttpClient client();
        
        /**
         * 获取连接超时时间
         * 
         * @return 超时时间（毫秒）
         */
        int connectTimeoutMillis();
        
        /**
         * 获取读取超时时间
         * 
         * @return 超时时间（毫秒）
         */
        int readTimeoutMillis();
        
        /**
         * 获取写入超时时间
         * 
         * @return 超时时间（毫秒）
         */
        int writeTimeoutMillis();
    }
}

/*
【编写提示】

这也是一个接口，只需要声明方法。

【关键理解】

1. 【拦截器的执行顺序很重要！】
   
   假设有拦截器：[A, B, C]
   
   执行顺序：
   A.intercept() 开始
     → A 处理请求
     → A 调用 chain.proceed()
         → B.intercept() 开始
             → B 处理请求
             → B 调用 chain.proceed()
                 → C.intercept() 开始
                     → C 处理请求
                     → C 执行网络请求
                     → C 返回 Response
                 ← C.intercept() 结束
             ← B 处理响应
             ← B 返回 Response
         ← B.intercept() 结束
     ← A 处理响应
     ← A 返回 Response
   A.intercept() 结束

2. 【拦截器的三种行为】
   
   a) 正常执行（最常见）：
   ```java
   public Response intercept(Chain chain) {
       Request request = chain.request();
       // 可能修改 request
       Response response = chain.proceed(request);
       // 可能修改 response
       return response;
   }
   ```
   
   b) 短路（直接返回，不继续执行）：
   ```java
   public Response intercept(Chain chain) {
       Request request = chain.request();
       
       // 检查缓存
       Response cached = getCachedResponse(request);
       if (cached != null) {
           return cached;  // 直接返回，不调用 proceed()
       }
       
       return chain.proceed(request);
   }
   ```
   
   c) 重试（多次调用 proceed）：
   ```java
   public Response intercept(Chain chain) {
       Request request = chain.request();
       
       try {
           return chain.proceed(request);
       } catch (IOException e) {
           // 重试
           return chain.proceed(request);  // 错误！不能多次调用
       }
   }
   ```
   注意：不能在同一个 chain 上多次调用 proceed()

3. 【实际使用示例】
   
   日志拦截器：
   ```java
   class LoggingInterceptor implements Interceptor {
       @Override
       public Response intercept(Chain chain) throws IOException {
           Request request = chain.request();
           
           long startTime = System.currentTimeMillis();
           System.out.println("发送请求: " + request.url());
           
           Response response = chain.proceed(request);
           
           long duration = System.currentTimeMillis() - startTime;
           System.out.println("收到响应: " + response.code() + ", 耗时: " + duration + "ms");
           
           return response;
       }
   }
   ```
   
   认证拦截器：
   ```java
   class AuthInterceptor implements Interceptor {
       @Override
       public Response intercept(Chain chain) throws IOException {
           Request originalRequest = chain.request();
           
           // 添加认证头
           Request authorizedRequest = originalRequest.newBuilder()
               .header("Authorization", "Bearer " + getToken())
               .build();
           
           return chain.proceed(authorizedRequest);
       }
   }
   ```

【常见错误】

❌ 错误1：忘记调用 proceed()
```java
public Response intercept(Chain chain) {
    Request request = chain.request();
    // 忘记调用 chain.proceed()
    return null;  // 错误！
}
```

❌ 错误2：多次调用 proceed()
```java
public Response intercept(Chain chain) {
    chain.proceed(request);  // 第一次
    return chain.proceed(request);  // 错误！不能调用第二次
}
```

❌ 错误3：proceed() 传入 null
```java
public Response intercept(Chain chain) {
    return chain.proceed(null);  // 错误！
}
```

【预计编写时间】30 分钟

【难度】⭐⭐⭐☆☆

【重点】
- 理解责任链模式
- 理解 proceed() 的作用
- 理解拦截器的执行顺序
- 知道拦截器能做什么

【这是 OkHttp 最核心的设计，一定要理解透彻！】
*/


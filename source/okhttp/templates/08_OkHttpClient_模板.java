package com.myokhttp;

import java.util.ArrayList;
import java.util.List;

/**
 * OkHttpClient - HTTP 客户端
 * 
 * 【为什么需要这个类？】
 * - 统一管理所有配置（超时、拦截器等）
 * - 管理共享资源（连接池、线程池）
 * - 提供创建 Call 的工厂方法
 * 
 * 【为什么推荐单例？】
 * 1. 共享连接池：提升性能
 * 2. 共享线程池：节省资源
 * 3. 配置一致：避免配置不一致的问题
 * 
 * 【Builder 模式的好处】
 * 1. 链式调用，代码优雅
 * 2. 配置灵活，可选参数
 * 3. 易于扩展新配置
 * 
 * 【核心职责】
 * 1. 管理配置：超时、重试等
 * 2. 管理资源：连接池、调度器
 * 3. 创建 Call：newCall() 工厂方法
 * 
 * @author Your Name
 */
public class OkHttpClient {
    
    // ========== 资源管理（共享资源）==========
    
    /**
     * 调度器：管理异步请求
     * 
     * 【为什么需要？】
     * - 管理线程池
     * - 控制并发数量
     * - 管理等待队列
     */
    private final Dispatcher dispatcher;
    
    /**
     * 应用拦截器列表
     * 
     * 【什么是应用拦截器？】
     * - 用户添加的拦截器
     * - 在重试、缓存之前执行
     * - 只调用一次，不受重试影响
     */
    private final List<Interceptor> interceptors;
    
    /**
     * 网络拦截器列表
     * 
     * 【什么是网络拦截器？】
     * - 用户添加的拦截器
     * - 在真实网络请求之前执行
     * - 可能被调用多次（重试）
     */
    private final List<Interceptor> networkInterceptors;
    
    /**
     * 连接池
     * 
     * 【为什么需要？】
     * - 复用 TCP 连接
     * - 提升性能
     */
    private final ConnectionPool connectionPool;
    
    // ========== 配置（不可变）==========
    
    /**
     * 连接超时时间（毫秒）
     * 
     * 【默认值】10 秒
     * 【作用】建立 TCP 连接的最大等待时间
     */
    private final int connectTimeout;
    
    /**
     * 读取超时时间（毫秒）
     * 
     * 【默认值】10 秒
     * 【作用】从 Socket 读取数据的最大等待时间
     */
    private final int readTimeout;
    
    /**
     * 写入超时时间（毫秒）
     * 
     * 【默认值】10 秒
     * 【作用】向 Socket 写入数据的最大等待时间
     */
    private final int writeTimeout;
    
    /**
     * 是否跟随重定向
     * 
     * 【默认值】true
     * 【作用】收到 3xx 响应时是否自动重定向
     */
    private final boolean followRedirects;
    
    /**
     * 连接失败时是否重试
     * 
     * 【默认值】true
     * 【作用】网络错误时是否自动重试
     */
    private final boolean retryOnConnectionFailure;

    // ========== 私有构造函数 ==========
    
    /**
     * 私有构造函数，只能通过 Builder 创建
     * 
     * 【为什么私有？】
     * - 强制使用 Builder 模式
     * - 确保正确初始化
     * - 统一创建方式
     */
    private OkHttpClient(Builder builder) {
        // TODO: 从 builder 复制所有字段
        // 注意：
        // 1. 资源类字段直接赋值（dispatcher, connectionPool）
        // 2. List 需要创建新的 ArrayList（防止外部修改）
        //    this.interceptors = new ArrayList<>(builder.interceptors);
        // 3. 配置类字段直接赋值（超时等）
        
    }

    // ========== 工厂方法 ==========
    
    /**
     * 创建一个新的 Call
     * 
     * 【这是工厂方法】
     * - 隐藏 RealCall 的实现细节
     * - 返回 Call 接口
     * 
     * 【使用示例】
     * Call call = client.newCall(request);
     * Response response = call.execute();
     * 
     * @param request 请求对象
     * @return Call 对象
     */
    public Call newCall(Request request) {
        // TODO: return new RealCall(this, request);
        return null;
    }

    // ========== Getter 方法 ==========
    
    public Dispatcher dispatcher() {
        // TODO: 返回 dispatcher
        return null;
    }

    public List<Interceptor> interceptors() {
        // TODO: 返回 interceptors（注意：这里不需要返回副本，因为外部不会修改）
        return null;
    }

    public List<Interceptor> networkInterceptors() {
        // TODO: 返回 networkInterceptors
        return null;
    }

    public ConnectionPool connectionPool() {
        // TODO: 返回 connectionPool
        return null;
    }

    public int connectTimeoutMillis() {
        // TODO: 返回 connectTimeout
        return 0;
    }

    public int readTimeoutMillis() {
        // TODO: 返回 readTimeout
        return 0;
    }

    public int writeTimeoutMillis() {
        // TODO: 返回 writeTimeout
        return 0;
    }

    public boolean followRedirects() {
        // TODO: 返回 followRedirects
        return false;
    }

    public boolean retryOnConnectionFailure() {
        // TODO: 返回 retryOnConnectionFailure
        return false;
    }

    /**
     * 创建一个新的 Builder（基于当前配置）
     * 
     * 【使用场景】
     * - 需要修改某些配置
     * - 创建新的客户端实例
     * 
     * @return 新的 Builder
     */
    public Builder newBuilder() {
        // TODO: return new Builder(this);
        return null;
    }

    // ========== Builder 类 ==========
    
    /**
     * Builder 模式：构建 OkHttpClient
     * 
     * 【为什么用 Builder？】
     * - 配置项很多（10+）
     * - 大部分是可选的
     * - Builder 模式最适合
     */
    public static class Builder {
        // 资源（有默认实例）
        private Dispatcher dispatcher;
        private List<Interceptor> interceptors = new ArrayList<>();
        private List<Interceptor> networkInterceptors = new ArrayList<>();
        private ConnectionPool connectionPool;
        
        // 配置（有默认值）
        private int connectTimeout = 10_000;    // 10 秒
        private int readTimeout = 10_000;       // 10 秒
        private int writeTimeout = 10_000;      // 10 秒
        private boolean followRedirects = true;
        private boolean retryOnConnectionFailure = true;

        /**
         * 默认构造函数
         * 
         * 【初始化默认值】
         * - 创建默认的 Dispatcher
         * - 创建默认的 ConnectionPool
         */
        public Builder() {
            // TODO: 初始化默认资源
            // this.dispatcher = new Dispatcher();
            // this.connectionPool = new ConnectionPool();
            
        }

        /**
         * 基于现有 OkHttpClient 的构造函数
         * 
         * 【使用场景】
         * - client.newBuilder()
         * - 复制现有配置
         */
        private Builder(OkHttpClient client) {
            // TODO: 从 client 复制所有字段
            // 资源：
            // this.dispatcher = client.dispatcher;
            // this.connectionPool = client.connectionPool;
            // 拦截器：
            // this.interceptors = new ArrayList<>(client.interceptors);
            // 配置：
            // this.connectTimeout = client.connectTimeout;
            // ...
            
        }

        /**
         * 添加应用拦截器
         * 
         * 【什么时候执行？】
         * - 在所有内置拦截器之前
         * - 不受重试、重定向影响
         * - 只调用一次
         * 
         * 【使用场景】
         * - 添加全局请求头
         * - 记录请求日志
         * - 添加签名
         * 
         * @param interceptor 拦截器
         * @return this
         */
        public Builder addInterceptor(Interceptor interceptor) {
            // TODO:
            // 1. 检查 interceptor 是否为 null
            // 2. interceptors.add(interceptor);
            // 3. return this;
            
            return null;
        }

        /**
         * 添加网络拦截器
         * 
         * 【什么时候执行？】
         * - 在真实网络请求之前
         * - 可能被调用多次（重试）
         * 
         * 【使用场景】
         * - 记录网络层数据
         * - 访问连接信息
         * 
         * @param interceptor 拦截器
         * @return this
         */
        public Builder addNetworkInterceptor(Interceptor interceptor) {
            // TODO: 与 addInterceptor 类似
            return null;
        }

        /**
         * 设置连接超时时间
         * 
         * 【什么是连接超时？】
         * - 建立 TCP 连接的最大等待时间
         * - 包括 DNS 解析时间
         * 
         * 【建议值】
         * - 快速网络：5-10 秒
         * - 慢速网络：15-30 秒
         * 
         * @param timeout 超时时间（毫秒）
         * @return this
         */
        public Builder connectTimeout(int timeout) {
            // TODO:
            // 1. 检查 timeout >= 0
            // 2. this.connectTimeout = timeout;
            // 3. return this;
            
            return null;
        }

        /**
         * 设置读取超时时间
         * 
         * 【什么是读取超时？】
         * - 从 Socket 读取数据的最大等待时间
         * - 不是整个请求的超时时间
         * 
         * @param timeout 超时时间（毫秒）
         * @return this
         */
        public Builder readTimeout(int timeout) {
            // TODO: 与 connectTimeout 类似
            return null;
        }

        /**
         * 设置写入超时时间
         * 
         * 【什么是写入超时？】
         * - 向 Socket 写入数据的最大等待时间
         * - 主要影响上传大文件
         * 
         * @param timeout 超时时间（毫秒）
         * @return this
         */
        public Builder writeTimeout(int timeout) {
            // TODO: 与 connectTimeout 类似
            return null;
        }

        /**
         * 设置是否跟随重定向
         * 
         * @param followRedirects 是否跟随
         * @return this
         */
        public Builder followRedirects(boolean followRedirects) {
            // TODO:
            // this.followRedirects = followRedirects;
            // return this;
            
            return null;
        }

        /**
         * 设置连接失败时是否重试
         * 
         * @param retry 是否重试
         * @return this
         */
        public Builder retryOnConnectionFailure(boolean retry) {
            // TODO: 与 followRedirects 类似
            return null;
        }

        /**
         * 设置调度器
         * 
         * 【什么时候用？】
         * - 自定义线程池配置
         * - 自定义并发控制
         * 
         * @param dispatcher 调度器
         * @return this
         */
        public Builder dispatcher(Dispatcher dispatcher) {
            // TODO:
            // 1. 检查 dispatcher 是否为 null
            // 2. this.dispatcher = dispatcher;
            // 3. return this;
            
            return null;
        }

        /**
         * 设置连接池
         * 
         * 【什么时候用？】
         * - 自定义连接池大小
         * - 自定义连接保持时间
         * 
         * @param connectionPool 连接池
         * @return this
         */
        public Builder connectionPool(ConnectionPool connectionPool) {
            // TODO: 与 dispatcher 类似
            return null;
        }

        /**
         * 构建 OkHttpClient
         * 
         * @return OkHttpClient 实例
         */
        public OkHttpClient build() {
            // TODO: return new OkHttpClient(this);
            return null;
        }
    }
}

/*
【编写提示】

1. 【理解单例模式】
   虽然 OkHttpClient 可以创建多个实例，但推荐单例：
   
   ```java
   public class HttpClient {
       private static final OkHttpClient instance = new OkHttpClient();
       
       public static OkHttpClient getInstance() {
           return instance;
       }
   }
   ```
   
   为什么？
   - 共享连接池：复用连接，提升性能
   - 共享线程池：节省资源
   - 配置一致：避免问题

2. 【Builder 的默认值】
   在 Builder 的字段声明时设置默认值：
   ```java
   private int connectTimeout = 10_000;  // 默认 10 秒
   ```

3. 【拦截器的复制】
   从 client 复制时，需要创建新的 ArrayList：
   ```java
   this.interceptors = new ArrayList<>(client.interceptors);
   ```

【使用示例】

基本用法：
```java
OkHttpClient client = new OkHttpClient.Builder()
    .connectTimeout(15000)
    .readTimeout(15000)
    .build();

Request request = new Request.Builder()
    .url("https://example.com")
    .build();

Response response = client.newCall(request).execute();
```

添加拦截器：
```java
OkHttpClient client = new OkHttpClient.Builder()
    .addInterceptor(new LoggingInterceptor())
    .addInterceptor(new AuthInterceptor())
    .build();
```

修改现有客户端：
```java
OkHttpClient newClient = existingClient.newBuilder()
    .connectTimeout(30000)  // 修改超时
    .build();
```

【常见错误】

❌ 错误1：每次都创建新的 OkHttpClient
```java
// 错误！
public Response request() {
    OkHttpClient client = new OkHttpClient();  // 每次都创建
    return client.newCall(request).execute();
}
```
正确：复用同一个实例

❌ 错误2：拦截器列表直接赋值
```java
this.interceptors = builder.interceptors;  // 错误！
```
正确：创建新的 ArrayList

❌ 错误3：忘记设置默认值
```java
private Dispatcher dispatcher;  // null！
```
正确：在构造函数中初始化

【测试方法】
```java
// 测试默认配置
OkHttpClient client = new OkHttpClient.Builder().build();
System.out.println(client.connectTimeoutMillis());  // 10000
System.out.println(client.followRedirects());       // true

// 测试自定义配置
OkHttpClient custom = new OkHttpClient.Builder()
    .connectTimeout(5000)
    .followRedirects(false)
    .build();
System.out.println(custom.connectTimeoutMillis());  // 5000
System.out.println(custom.followRedirects());       // false

// 测试拦截器
Interceptor logger = chain -> {
    System.out.println("Logger");
    return chain.proceed(chain.request());
};

OkHttpClient withInterceptor = new OkHttpClient.Builder()
    .addInterceptor(logger)
    .build();
System.out.println(withInterceptor.interceptors().size());  // 1
```

【预计编写时间】45 分钟

【难度】⭐⭐⭐☆☆

【重点】
- 理解为什么推荐单例
- 掌握 Builder 模式
- 理解应用拦截器和网络拦截器的区别
*/


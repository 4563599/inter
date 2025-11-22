package com.myokhttp;

import java.io.IOException;

/**
 * 连接拦截器
 * 
 * 【为什么需要这个拦截器？】
 * - 建立与服务器的TCP连接
 * - 从连接池获取可复用的连接
 * - 管理连接的生命周期
 * 
 * 【这是应用层和网络层的分界点】
 * - 之前的拦截器：处理应用层逻辑（重试、缓存等）
 * - 这个拦截器：建立网络连接
 * - 之后的拦截器：执行真实的网络I/O
 * 
 * 【核心职责】
 * 1. 从连接池获取可复用的连接
 * 2. 如果没有可复用的连接，创建新连接
 * 3. 建立TCP连接
 * 4. 使用完毕后，将连接放回连接池
 * 
 * 【连接复用的好处】
 * - 跳过TCP三次握手（节省50-100ms）
 * - 跳过TLS握手（HTTPS，节省100-200ms）
 * - 显著提升性能
 * 
 * @author Your Name
 */
public class ConnectInterceptor implements Interceptor {
    
    /**
     * OkHttpClient实例
     * 
     * 【为什么需要？】
     * - 获取连接池
     * - 获取超时配置
     */
    private final OkHttpClient client;

    public ConnectInterceptor(OkHttpClient client) {
        this.client = client;
    }

    /**
     * 拦截方法
     * 
     * 【执行流程】
     * 1. 解析URL，提取host和port
     * 2. 从连接池获取可复用的连接
     * 3. 如果没有，创建新连接
     * 4. 执行下一个拦截器（使用这个连接）
     * 5. 使用完毕，放回连接池
     * 
     * 【关键点】
     * - 连接必须在使用后放回连接池
     * - 如果发生异常，需要关闭连接
     * - 使用try-catch-finally确保资源正确管理
     */
    @Override
    public Response intercept(Chain chain) throws IOException {
        // TODO: 步骤1 - 获取请求并解析URL
        // Request request = chain.request();
        
        // 解析URL，提取host和port
        // String[] hostPort = parseUrl(request.url());
        // String host = hostPort[0];
        // int port = Integer.parseInt(hostPort[1]);

        // TODO: 步骤2 - 从连接池获取连接
        // RealConnection connection = client.connectionPool().get(host, port);
        
        // TODO: 步骤3 - 如果没有可复用的连接，创建新连接
        // if (connection == null) {
        //     // 没有可复用的连接，创建新连接
        //     connection = new RealConnection(host, port);
        //     
        //     try {
        //         // 建立TCP连接
        //         connection.connect(
        //             chain.connectTimeoutMillis(),  // 连接超时
        //             chain.readTimeoutMillis()      // 读取超时
        //         );
        //         
        //         // 标记为使用中（引用计数+1）
        //         connection.acquire();
        //     } catch (IOException e) {
        //         // 连接失败
        //         throw new IOException("无法连接到 " + host + ":" + port, e);
        //     }
        // }

        // TODO: 步骤4 - 使用连接执行下一个拦截器
        // try {
        //     // 创建一个包装的Chain，携带连接信息
        //     // 这样CallServerInterceptor可以获取到连接
        //     ConnectedChain connectedChain = new ConnectedChain(chain, connection);
        //     
        //     // 执行下一个拦截器
        //     Response response = chain.proceed(request);
        //     
        //     // 使用完毕，放回连接池
        //     client.connectionPool().put(connection);
        //     
        //     return response;
        // } catch (IOException e) {
        //     // 如果发生异常，关闭连接
        //     try {
        //         connection.close();
        //     } catch (IOException closeException) {
        //         // 忽略关闭异常
        //     }
        //     throw e;
        // }
        
        return null;
    }

    /**
     * 解析URL，提取host和port
     * 
     * 【URL格式】
     * http://example.com:8080/path?query=1
     * ↓
     * host: example.com
     * port: 8080
     * 
     * https://example.com/path
     * ↓
     * host: example.com
     * port: 443（HTTPS默认端口）
     * 
     * 【实现思路】
     * 1. 去除协议（http:// 或 https://）
     * 2. 提取到第一个/之前的部分
     * 3. 分离host和port
     * 4. 如果没有port，使用默认值
     * 
     * @param url URL字符串
     * @return [host, port]
     */
    private String[] parseUrl(String url) {
        // TODO: 实现URL解析
        // try {
        //     // 1. 去除协议
        //     boolean isHttps = false;
        //     if (url.startsWith("http://")) {
        //         url = url.substring(7);
        //     } else if (url.startsWith("https://")) {
        //         url = url.substring(8);
        //         isHttps = true;
        //     }
        //     
        //     // 2. 提取host和port（到第一个/之前）
        //     int slashIndex = url.indexOf("/");
        //     if (slashIndex != -1) {
        //         url = url.substring(0, slashIndex);
        //     }
        //     
        //     // 3. 分离host和port
        //     int colonIndex = url.indexOf(":");
        //     if (colonIndex != -1) {
        //         // 有端口号
        //         String host = url.substring(0, colonIndex);
        //         String port = url.substring(colonIndex + 1);
        //         return new String[]{host, port};
        //     } else {
        //         // 没有端口号，使用默认值
        //         String port = isHttps ? "443" : "80";
        //         return new String[]{url, port};
        //     }
        // } catch (Exception e) {
        //     // 解析失败，返回默认值
        //     return new String[]{"localhost", "80"};
        // }
        
        return null;
    }

    /**
     * 包装的Chain，携带连接信息
     * 
     * 【为什么需要这个类？】
     * - CallServerInterceptor需要获取连接
     * - 通过Chain传递连接信息
     * 
     * 【设计模式：装饰器模式】
     * - 包装原有的Chain
     * - 添加connection()方法
     * - 其他方法委托给原Chain
     * 
     * 【简化说明】
     * 这是一个简化的实现。真实的OkHttp使用更复杂的
     * StreamAllocation来管理连接。
     */
    static class ConnectedChain implements Interceptor.Chain {
        private final Interceptor.Chain delegate;  // 原Chain
        private final RealConnection connection;   // 连接

        ConnectedChain(Interceptor.Chain delegate, RealConnection connection) {
            this.delegate = delegate;
            this.connection = connection;
        }

        /**
         * 获取连接
         * 
         * 【给CallServerInterceptor使用】
         * CallServerInterceptor会调用这个方法获取连接
         * 
         * @return 连接对象
         */
        public RealConnection connection() {
            return connection;
        }

        // ========== 委托方法 ==========
        
        @Override
        public Request request() {
            return delegate.request();
        }

        @Override
        public Response proceed(Request request) throws IOException {
            return delegate.proceed(request);
        }

        @Override
        public OkHttpClient client() {
            return delegate.client();
        }

        @Override
        public int connectTimeoutMillis() {
            return delegate.connectTimeoutMillis();
        }

        @Override
        public int readTimeoutMillis() {
            return delegate.readTimeoutMillis();
        }

        @Override
        public int writeTimeoutMillis() {
            return delegate.writeTimeoutMillis();
        }
    }
}

/*
【编写提示】

1. 【理解连接池的工作流程】
   
   第一次请求：
   ```
   1. connectionPool.get(host, port) → null（没有连接）
   2. 创建new RealConnection(host, port)
   3. connection.connect() → TCP三次握手（100ms）
   4. 使用连接发送请求
   5. connectionPool.put(connection) → 放入连接池
   ```
   
   第二次请求（相同host和port）：
   ```
   1. connectionPool.get(host, port) → 返回连接（0ms）
   2. 直接使用连接发送请求
   3. connectionPool.put(connection) → 放回连接池
   ```
   
   性能对比：
   - 第一次：100ms（建立连接）+ 50ms（请求）= 150ms
   - 第二次：0ms（复用连接）+ 50ms（请求）= 50ms
   - 提升：67%

2. 【连接的生命周期】
   
   ```
   创建 → 建立连接 → 使用中 → 空闲 → 清理
   new     connect()   acquire()  release()  close()
   ```
   
   引用计数：
   - acquire()：引用计数+1，标记为使用中
   - release()：引用计数-1，如果为0则标记为空闲
   - isInUse()：检查是否正在使用（引用计数>0）

3. 【为什么需要try-catch-finally？】
   
   确保资源正确管理：
   ```java
   RealConnection connection = getConnection();
   try {
       Response response = chain.proceed(request);
       connectionPool.put(connection);  // 正常：放回连接池
       return response;
   } catch (IOException e) {
       connection.close();  // 异常：关闭连接
       throw e;
   }
   ```
   
   如果不这样做：
   - 异常时连接没有关闭 → 资源泄漏
   - 连接池中有无效的连接 → 下次使用失败

4. 【URL解析的边界情况】
   
   需要处理的情况：
   - http://example.com → host:example.com, port:80
   - https://example.com → host:example.com, port:443
   - http://example.com:8080 → host:example.com, port:8080
   - http://example.com/path → host:example.com, port:80
   - http://example.com:8080/path?q=1 → host:example.com, port:8080

【使用示例】

这个拦截器自动工作：

```java
// 第一次请求
Request request1 = new Request.Builder()
    .url("http://example.com/api/users")
    .build();

Response response1 = client.newCall(request1).execute();
// ConnectInterceptor：
// 1. connectionPool.get(example.com, 80) → null
// 2. 创建新连接 → TCP握手（100ms）
// 3. 使用连接
// 4. connectionPool.put(connection)

// 第二次请求（相同服务器）
Request request2 = new Request.Builder()
    .url("http://example.com/api/posts")
    .build();

Response response2 = client.newCall(request2).execute();
// ConnectInterceptor：
// 1. connectionPool.get(example.com, 80) → 返回连接（0ms）
// 2. 直接使用连接
// 3. connectionPool.put(connection)
```

【常见错误】

❌ 错误1：忘记放回连接池
```java
Response response = chain.proceed(request);
return response;  // 忘记put(connection)
```
正确：使用后一定要put()

❌ 错误2：异常时没有关闭连接
```java
try {
    return chain.proceed(request);
} catch (IOException e) {
    throw e;  // 忘记关闭连接
}
```
正确：catch中要close()

❌ 错误3：解析URL时忘记处理端口
```java
// URL: http://example.com:8080/api
// 错误：只提取了example.com，丢失了端口8080
// 正确：example.com:8080
```

【测试方法】
```java
// 测试连接复用
OkHttpClient client = new OkHttpClient();

long start1 = System.currentTimeMillis();
Response r1 = client.newCall(request).execute();
long time1 = System.currentTimeMillis() - start1;
System.out.println("第一次：" + time1 + "ms");  // ~150ms

long start2 = System.currentTimeMillis();
Response r2 = client.newCall(request).execute();
long time2 = System.currentTimeMillis() - start2;
System.out.println("第二次：" + time2 + "ms");  // ~50ms

System.out.println("连接数：" + client.connectionPool().connectionCount());  // 1
```

【预计编写时间】1 小时

【难度】⭐⭐⭐⭐☆

【重点】
- 理解连接池的工作原理
- 理解连接的生命周期
- 掌握资源管理（try-catch-finally）
- 理解为什么连接复用能提升性能
*/


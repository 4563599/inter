package com.myokhttp;

import java.io.IOException;

/**
 * 桥接拦截器
 * 
 * 【为什么叫"桥接"？】
 * - 应用层：用户构建的简单 Request
 * - 网络层：符合 HTTP 规范的完整请求
 * - 这个拦截器是连接两者的"桥梁"
 * 
 * 【核心职责】
 * 1. 补充必要的 HTTP 请求头
 * 2. 处理 Content-Type 和 Content-Length
 * 3. 添加 gzip 支持
 * 4. 添加 Keep-Alive
 * 5. 添加 User-Agent
 * 
 * 【为什么需要补充这些头？】
 * - User-Agent：服务器需要知道客户端类型
 * - Content-Type：服务器需要知道请求体格式
 * - Content-Length：服务器需要知道请求体大小
 * - Connection: Keep-Alive：支持连接复用
 * - Accept-Encoding: gzip：支持压缩，节省带宽
 * 
 * @author Your Name
 */
public class BridgeInterceptor implements Interceptor {

    /**
     * 拦截方法
     * 
     * 【执行流程】
     * 1. 获取原始请求
     * 2. 创建 Builder，补充必要的头
     * 3. 执行下一个拦截器
     * 4. 处理响应（如果需要）
     * 5. 返回响应
     */
    @Override
    public Response intercept(Chain chain) throws IOException {
        // TODO: 步骤 1 - 获取原始请求
        // Request userRequest = chain.request();
        // Request.Builder requestBuilder = userRequest.newBuilder();

        // TODO: 步骤 2 - 补充请求头
        
        // 2.1 处理请求体相关的头
        // RequestBody body = userRequest.body();
        // if (body != null) {
        //     // 补充 Content-Type
        //     String contentType = body.contentType();
        //     if (contentType != null) {
        //         requestBuilder.header("Content-Type", contentType);
        //     }
        //
        //     // 补充 Content-Length 或 Transfer-Encoding
        //     long contentLength = body.contentLength();
        //     if (contentLength != -1) {
        //         requestBuilder.header("Content-Length", String.valueOf(contentLength));
        //     } else {
        //         // 长度未知，使用 chunked 编码
        //         requestBuilder.header("Transfer-Encoding", "chunked");
        //     }
        // }

        // 2.2 补充 Host 头
        // 【为什么需要 Host？】
        // - HTTP/1.1 规范要求
        // - 一个 IP 可能对应多个域名（虚拟主机）
        // if (userRequest.header("Host") == null) {
        //     String host = getHost(userRequest.url());
        //     requestBuilder.header("Host", host);
        // }

        // 2.3 补充 Connection 头
        // 【为什么设置 Keep-Alive？】
        // - 支持连接复用
        // - HTTP/1.1 默认就是 Keep-Alive，但显式设置更好
        // if (userRequest.header("Connection") == null) {
        //     requestBuilder.header("Connection", "Keep-Alive");
        // }

        // 2.4 补充 Accept-Encoding 头
        // 【为什么添加 gzip？】
        // - 压缩响应，节省带宽
        // - 加快传输速度
        // - 大部分服务器都支持 gzip
        // boolean transparentGzip = false;
        // if (userRequest.header("Accept-Encoding") == null) {
        //     transparentGzip = true;
        //     requestBuilder.header("Accept-Encoding", "gzip");
        // }

        // 2.5 补充 User-Agent 头
        // 【为什么需要 User-Agent？】
        // - 服务器需要知道客户端类型
        // - 用于统计、兼容性处理
        // - 有些服务器会拒绝没有 User-Agent 的请求
        // if (userRequest.header("User-Agent") == null) {
        //     requestBuilder.header("User-Agent", "MyOkHttp/1.0");
        // }

        // TODO: 步骤 3 - 执行下一个拦截器
        // Request networkRequest = requestBuilder.build();
        // Response networkResponse = chain.proceed(networkRequest);

        // TODO: 步骤 4 - 处理响应
        // Response.Builder responseBuilder = networkResponse.newBuilder()
        //         .request(userRequest);

        // 4.1 处理 gzip 解压
        // 【什么时候需要解压？】
        // - 我们添加了 Accept-Encoding: gzip
        // - 服务器返回了 Content-Encoding: gzip
        // - 需要自动解压给用户
        // if (transparentGzip 
        //     && "gzip".equalsIgnoreCase(networkResponse.header("Content-Encoding"))) {
        //     // 这里简化处理，实际应该解压响应体
        //     // responseBuilder.body(new GzipResponseBody(networkResponse.body()));
        //     System.out.println("响应使用了 gzip 压缩");
        // }

        // TODO: 步骤 5 - 返回响应
        // return responseBuilder.build();
        
        return null;
    }

    /**
     * 从 URL 中提取 Host
     * 
     * 【URL 格式】
     * http://example.com:8080/path?query=1
     * ↓
     * Host: example.com:8080
     * 
     * 【实现思路】
     * 1. 去除协议（http:// 或 https://）
     * 2. 提取到第一个 / 之前的部分
     * 3. 包含端口号（如果有）
     * 
     * @param url URL 字符串
     * @return Host 字符串
     */
    private String getHost(String url) {
        // TODO: 实现这个方法
        // 提示：
        // 1. 去除协议
        //    if (url.startsWith("http://")) {
        //        url = url.substring(7);
        //    } else if (url.startsWith("https://")) {
        //        url = url.substring(8);
        //    }
        // 
        // 2. 提取 host（到第一个 / 之前）
        //    int slashIndex = url.indexOf("/");
        //    if (slashIndex != -1) {
        //        url = url.substring(0, slashIndex);
        //    }
        // 
        // 3. 返回结果（包含端口）
        //    return url;
        
        return null;
    }
}

/*
【编写提示】

1. 【理解每个头的作用】
   
   Content-Type: 
   - 告诉服务器请求体是什么格式
   - application/json, application/x-www-form-urlencoded 等
   
   Content-Length:
   - 告诉服务器请求体的大小
   - 服务器需要知道读取多少字节
   
   Transfer-Encoding: chunked:
   - 当不知道内容长度时使用
   - 分块传输，每块前面标注长度
   
   Host:
   - HTTP/1.1 必需的头
   - 一个 IP 可能对应多个网站
   
   Connection: Keep-Alive:
   - 告诉服务器保持连接
   - 可以复用连接发送多个请求
   
   Accept-Encoding: gzip:
   - 告诉服务器客户端支持 gzip 压缩
   - 服务器可以压缩响应
   
   User-Agent:
   - 客户端标识
   - MyOkHttp/1.0, Chrome/90.0 等

2. 【为什么要补充这些头？】
   
   用户构建的请求可能很简单：
   ```java
   Request request = new Request.Builder()
       .url("http://example.com/api")
       .post(body)
       .build();
   ```
   
   BridgeInterceptor 补充后：
   ```
   POST /api HTTP/1.1
   Host: example.com
   Content-Type: application/json
   Content-Length: 27
   Connection: Keep-Alive
   Accept-Encoding: gzip
   User-Agent: MyOkHttp/1.0
   
   {"name":"test"}
   ```

3. 【Content-Length vs Transfer-Encoding】
   
   Content-Length:
   - 已知内容大小
   - 一次性发送
   
   Transfer-Encoding: chunked:
   - 不知道内容大小
   - 分块发送
   - 每块格式：长度\r\n内容\r\n
   - 最后一块：0\r\n\r\n

【使用示例】

这个拦截器自动工作，用户不需要关心：

```java
// 用户只需要这样写
Request request = new Request.Builder()
    .url("http://example.com/api")
    .post(RequestBody.createJson("{\"test\":1}"))
    .build();

// BridgeInterceptor 会自动补充：
// - Content-Type: application/json; charset=utf-8
// - Content-Length: 10
// - Host: example.com
// - Connection: Keep-Alive
// - Accept-Encoding: gzip
// - User-Agent: MyOkHttp/1.0
```

【常见错误】

❌ 错误1：覆盖用户设置的头
```java
// 错误！
requestBuilder.header("User-Agent", "MyOkHttp");
```
正确：先检查用户是否已设置
```java
if (userRequest.header("User-Agent") == null) {
    requestBuilder.header("User-Agent", "MyOkHttp");
}
```

❌ 错误2：忘记处理端口号
```java
// URL: http://example.com:8080/api
// 错误的 Host: example.com （丢失了端口号）
// 正确的 Host: example.com:8080
```

❌ 错误3：忘记设置 Content-Length
```java
// 没有 Content-Length，服务器不知道读取多少字节
// 可能导致请求失败
```

【测试方法】
```java
// 创建一个简单的请求
Request request = new Request.Builder()
    .url("http://example.com:8080/api/users")
    .post(RequestBody.createJson("{\"name\":\"test\"}"))
    .build();

// 创建拦截器
BridgeInterceptor interceptor = new BridgeInterceptor();

// 创建模拟的 Chain
Chain chain = new MockChain(request);

// 执行拦截器
Response response = interceptor.intercept(chain);

// 验证补充的头
System.out.println(chain.getActualRequest().header("Host"));          // example.com:8080
System.out.println(chain.getActualRequest().header("Content-Type"));  // application/json...
System.out.println(chain.getActualRequest().header("Content-Length"));// 17
System.out.println(chain.getActualRequest().header("User-Agent"));    // MyOkHttp/1.0
```

【预计编写时间】45 分钟

【难度】⭐⭐⭐☆☆

【重点】
- 理解每个 HTTP 头的作用
- 理解为什么要补充这些头
- 掌握 Content-Length 和 Transfer-Encoding 的区别
- 注意不要覆盖用户设置的头
*/


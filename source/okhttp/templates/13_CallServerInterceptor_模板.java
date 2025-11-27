package com.myokhttp;

import java.io.*;
import java.util.Map;

/**
 * 服务器调用拦截器
 * <p>
 * ⭐⭐⭐⭐⭐ 这是拦截器链的最后一个，也是最核心的一个！
 * <p>
 * 【为什么这是最核心的拦截器？】
 * - 这是唯一执行真实网络I/O的拦截器
 * - 实现了HTTP协议的细节
 * - 所有前面的拦截器最终都要到这里
 * <p>
 * 【核心职责】
 * 1. 写入HTTP请求到Socket
 * 2. 读取HTTP响应从Socket
 * 3. 解析响应头和响应体
 * <p>
 * 【HTTP协议实现】
 * 请求格式：
 * GET /path HTTP/1.1\r\n
 * Host: example.com\r\n
 * User-Agent: MyOkHttp\r\n
 * \r\n
 * [请求体]
 * <p>
 * 响应格式：
 * HTTP/1.1 200 OK\r\n
 * Content-Type: text/html\r\n
 * Content-Length: 123\r\n
 * \r\n
 * [响应体]
 * <p>
 * 【关键点】
 * - \r\n 是HTTP行分隔符（CRLF）
 * - 空行（\r\n\r\n）表示头部结束
 * - 根据Content-Length读取响应体
 *
 * @author Your Name
 */
public class CallServerInterceptor implements Interceptor {

    /**
     * 拦截方法
     * <p>
     * 【执行流程】
     * 1. 获取连接（由ConnectInterceptor建立）
     * 2. 写入HTTP请求
     * 3. 读取HTTP响应
     * 4. 解析并返回响应
     */
    @Override
    public Response intercept(Chain chain) throws IOException {
// 1. 拿到之前的拦截器准备好的 Request 和 Connection
        Request request = chain.request();
        RealConnection connection = getConnection(chain);

        // 2. ⭐ 核心动作：往 Socket 里写请求
        System.out.println(">>> 开始写入请求...");
        writeRequest(connection, request);

        // 3. ⭐ 核心动作：从 Socket 里读响应
        System.out.println("<<< 开始读取响应...");
        Response response = readResponse(connection, request);

        return response;
    }

    /**
     * 从Chain中获取连接
     * <p>
     * 【实现技巧】
     * - 检查Chain是否是ConnectedChain
     * - 如果是，直接获取连接
     * - 如果不是，需要重新建立连接（降级方案）
     *
     * @param chain 拦截器链
     * @return 连接对象
     * @throws IOException 获取连接失败时抛出
     */
    private RealConnection getConnection(Chain chain) throws IOException {
        if (chain instanceof ConnectInterceptor.ConnectedChain) {
            return ((ConnectInterceptor.ConnectedChain) chain).connection();
        }
        throw new IllegalStateException("这一步必须有连接！");

    }

    /**
     * 写入HTTP请求
     * <p>
     * 【HTTP请求格式】
     * 请求行：GET /path HTTP/1.1\r\n
     * 请求头：Header-Name: Header-Value\r\n
     * 空行：\r\n
     * 请求体：[body content]
     * <p>
     * 【为什么用\r\n？】
     * - HTTP规范要求使用CRLF（\r\n）作为行分隔符
     * - \r 是回车（Carriage Return）
     * - \n 是换行（Line Feed）
     * <p>
     * 【关键点】
     * - 每一行都以\r\n结尾
     * - 头部和body之间有一个空行（\r\n\r\n）
     * - 字符串使用UTF-8编码
     *
     * @param connection 连接对象
     * @param request    请求对象
     * @throws IOException 写入失败时抛出
     */
    private void writeRequest(RealConnection connection, Request request) throws IOException {
        // 拿到 Socket 的输出流，准备写数据
        OutputStream out = connection.getOutputStream();
        // 包装成 BufferedWriter，为了性能（攒一波再发）
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));

        // 1. 写请求行 (GET /index.html HTTP/1.1)
        String path = getPath(request.url());
        writer.write(request.method() + " " + path + " HTTP/1.1\r\n");

        // 2. 写请求头 (Host: baidu.com)
        for (Map.Entry<String, String> entry : request.headers().entrySet()) {
            writer.write(entry.getKey() + ": " + entry.getValue() + "\r\n");
        }

        // 3. 写空行 (告诉服务器：头写完了，下面是正文)
        writer.write("\r\n");
        writer.flush(); // 必须 flush，否则还在缓冲区里

        // 4. 写请求体 (如果是 POST)
        if (request.body() != null) {
            request.body().writeTo(out);
        }

        /**
         * 读取HTTP响应
         * <p>
         * 【HTTP响应格式】
         * 状态行：HTTP/1.1 200 OK\r\n
         * 响应头：Header-Name: Header-Value\r\n
         * 空行：\r\n
         * 响应体：[body content]
         * <p>
         * 【读取策略】
         * 1. 读取状态行，解析状态码和消息
         * 2. 循环读取响应头，直到空行
         * 3. 根据Content-Length读取响应体
         * <p>
         * 【关键点】
         * - 空行表示头部结束
         * - Content-Length指定响应体长度
         * - 如果没有Content-Length，读取到流结束
         *
         * @param connection 连接对象
         * @param request    请求对象
         * @return 响应对象
         * @throws IOException 读取失败时抛出
         */
        private Response readResponse (RealConnection connection, Request request) throws
        IOException {
            // 拿到 Socket 的输入流
            InputStream in = connection.getInputStream();
            // 这里的 reader 只能用来读头，不能读 Body（因为 Body 可能是二进制图片）
            // 真实 OkHttp 使用 Okio，这里简化用 BufferedReader
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));

            // 1. 读状态行 (HTTP/1.1 200 OK)
            String statusLine = reader.readLine();
            if (statusLine == null) throw new IOException("服务器挂了（空响应）");

            // 简单解析 "HTTP/1.1 200 OK"
            String[] parts = statusLine.split(" ", 3);
            int code = Integer.parseInt(parts[1]);
            String message = parts.length > 2 ? parts[2] : "";

            // 2. 读响应头 (Content-Type: text/html)
            Response.Builder builder = new Response.Builder()
                    .request(request)
                    .code(code)
                    .message(message);

            long contentLength = -1;
            String line;
            // 循环读，直到读到一个空行
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) break; // 读到空行，头结束了

                int colon = line.indexOf(":");
                if (colon != -1) {
                    String name = line.substring(0, colon).trim();
                    String value = line.substring(colon + 1).trim();
                    builder.header(name, value);

                    if (name.equalsIgnoreCase("Content-Length")) {
                        contentLength = Long.parseLong(value);
                    }
                }
            }

            // 3. 读响应体 (Body)
            // 注意：这里不能再用 reader 了，要直接操作底层流
            byte[] bodyBytes;
            if (contentLength > 0) {
                bodyBytes = readExactly(in, (int) contentLength);
            } else if (contentLength == 0) {
                bodyBytes = new byte[0];
            } else {
                // 没有长度，通常是 chunked 编码或者读到关流，简化处理读到完
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] temp = new byte[1024];
                int len;
                // 注意：这种写法会导致长连接失效，因为读到 EOF 意味着连接断了
                while ((len = in.read(temp)) != -1) {
                    buffer.write(temp, 0, len);
                }
                bodyBytes = buffer.toByteArray();
            }

            // 4. 组装最终结果
            String contentType = builder.build().header("Content-Type");
            builder.body(ResponseBody.create(bodyBytes, contentType));

            return builder.build();
        }

        /**
         * 精确读取指定长度的字节
         * <p>
         * 【为什么需要这个方法？】
         * - InputStream.read()不保证读取指定长度
         * - 可能返回少于请求的字节数
         * - 需要循环读取直到满足长度要求
         * <p>
         * 【实现思路】
         * 1. 创建指定长度的字节数组
         * 2. 循环读取，直到填满数组
         * 3. 如果流提前结束，抛出异常
         *
         * @param in     输入流
         * @param length 要读取的长度
         * @return 字节数组
         * @throws IOException 读取失败或流提前结束
         */
        private byte[] readExactly (InputStream in,int length) throws IOException {
            byte[] data = new byte[length];
            int read = 0;
            while (read < length) {
                int count = in.read(data, read, length - read);
                if (count == -1) throw new IOException("还没读完连接就断了");
                read += count;
            }
            return data;
        }
        }

        /**
         * 从URL中提取路径
         * <p>
         * 【URL格式】
         * http://example.com:8080/api/users?id=1
         * ↓
         * /api/users?id=1
         * <p>
         * 【特殊情况】
         * http://example.com → /
         *
         * @param url URL字符串
         * @return 路径部分
         */
        private String getPath (String url){
            if (url.startsWith("http")) {
                int slash = url.indexOf("/", 8); // 跳过 https://
                if (slash != -1) return url.substring(slash);
            }
            return "/";
        }

        /**
         * 解析URL
         */
        private String[] parseUrl (String url){
            try {
                if (url.startsWith("http://")) {
                    url = url.substring(7);
                } else if (url.startsWith("https://")) {
                    url = url.substring(8);
                }

                int slashIndex = url.indexOf("/");
                if (slashIndex != -1) {
                    url = url.substring(0, slashIndex);
                }

                int colonIndex = url.indexOf(":");
                if (colonIndex != -1) {
                    String host = url.substring(0, colonIndex);
                    String port = url.substring(colonIndex + 1);
                    return new String[]{host, port};
                } else {
                    return new String[]{url, "80"};
                }
            } catch (Exception e) {
                return new String[]{"localhost", "80"};
            }
        }
    }

/**
 * CallServerInterceptor 服务器调用拦截器”是 OkHttp 拦截器链中的最后一个拦截器，也是最核心的一个，它的主要作用是执行真实的网络 I/O 操作。
 *
 * 简单来说，之前的拦截器（如重试、桥接、连接）都是在准备工作，而 CallServerInterceptor 是真正**“干活”**（发数据、收数据）的地方。
 */

/*
【编写提示】

1. 【理解HTTP协议格式】
   
   完整的HTTP请求示例：
   ```
   POST /api/users HTTP/1.1\r\n
   Host: example.com\r\n
   Content-Type: application/json\r\n
   Content-Length: 27\r\n
   User-Agent: MyOkHttp/1.0\r\n
   \r\n
   {"name":"test","age":25}
   ```
   
   完整的HTTP响应示例：
   ```
   HTTP/1.1 200 OK\r\n
   Content-Type: application/json\r\n
   Content-Length: 42\r\n
   Connection: keep-alive\r\n
   \r\n
   {"id":1,"name":"test","age":25}
   ```

2. 【为什么需要readExactly()？】
   
   InputStream.read()的行为：
   ```java
   byte[] buffer = new byte[100];
   int n = in.read(buffer);  // 可能返回1-100之间的任何值！
   ```
   
   如果Content-Length=100，但read()只返回50：
   - 错误做法：认为读完了
   - 正确做法：继续读取剩余50字节
   
   readExactly()确保读取完整：
   ```java
   byte[] data = readExactly(in, 100);  // 一定是100字节
   ```

3. 【Content-Length的重要性】
   
   有Content-Length：
   - 知道响应体有多大
   - 精确读取，不多不少
   - 连接可以复用
   
   没有Content-Length：
   - 不知道响应体有多大
   - 读取到流结束（EOF）
   - 连接会被关闭，无法复用

4. 【为什么使用BufferedWriter/BufferedReader？】
   
   不使用Buffer：
   - 每次write()都是系统调用
   - 非常慢
   
   使用Buffer：
   - 数据先写入缓冲区
   - 缓冲区满了或flush()时才写入Socket
   - 减少系统调用，提升性能

【使用示例】

这个拦截器自动工作：

```java
Request request = new Request.Builder()
    .url("http://httpbin.org/get")
    .header("Custom-Header", "value")
    .build();

// CallServerInterceptor自动：
// 1. 写入请求：
//    GET /get HTTP/1.1
//    Host: httpbin.org
//    Custom-Header: value
//    ...
// 
// 2. 读取响应：
//    HTTP/1.1 200 OK
//    Content-Type: application/json
//    Content-Length: 286
//    ...
// 
// 3. 返回Response对象
```

【常见错误】

❌ 错误1：忘记\r\n
```java
writer.write("GET /path HTTP/1.1\n");  // 错误！应该是\r\n
```

❌ 错误2：忘记空行
```java
writer.write("Content-Length: 10\r\n");
// 忘记空行
writer.write("body");  // 错误！
```
正确：
```java
writer.write("Content-Length: 10\r\n");
writer.write("\r\n");  // 空行
writer.write("body");
```

❌ 错误3：读取响应体时没有考虑Content-Length
```java
// 直接读到EOF，连接会被关闭
while ((len = in.read(buffer)) != -1) {
    out.write(buffer, 0, len);
}
```

【测试方法】
```java
// 手动测试HTTP协议
Request request = new Request.Builder()
    .url("http://httpbin.org/get")
    .build();

Response response = client.newCall(request).execute();

// 验证状态码
System.out.println("状态码：" + response.code());  // 200

// 验证响应头
System.out.println("Content-Type：" + response.header("Content-Type"));

// 验证响应体
String body = response.body().string();
System.out.println("响应体长度：" + body.length());
```

【预计编写时间】1 小时 15 分钟

【难度】⭐⭐⭐⭐⭐

【重点】
- 理解HTTP协议格式（最重要！）
- 掌握\r\n的使用
- 理解空行的作用
- 掌握Content-Length的处理
- 理解readExactly()的必要性

这是最核心的拦截器，一定要理解透彻！
*/


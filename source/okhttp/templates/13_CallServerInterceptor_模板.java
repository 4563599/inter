package com.myokhttp;

import java.io.*;
import java.util.Map;

/**
 * 服务器调用拦截器
 * 
 * ⭐⭐⭐⭐⭐ 这是拦截器链的最后一个，也是最核心的一个！
 * 
 * 【为什么这是最核心的拦截器？】
 * - 这是唯一执行真实网络I/O的拦截器
 * - 实现了HTTP协议的细节
 * - 所有前面的拦截器最终都要到这里
 * 
 * 【核心职责】
 * 1. 写入HTTP请求到Socket
 * 2. 读取HTTP响应从Socket
 * 3. 解析响应头和响应体
 * 
 * 【HTTP协议实现】
 * 请求格式：
 * GET /path HTTP/1.1\r\n
 * Host: example.com\r\n
 * User-Agent: MyOkHttp\r\n
 * \r\n
 * [请求体]
 * 
 * 响应格式：
 * HTTP/1.1 200 OK\r\n
 * Content-Type: text/html\r\n
 * Content-Length: 123\r\n
 * \r\n
 * [响应体]
 * 
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
     * 
     * 【执行流程】
     * 1. 获取连接（由ConnectInterceptor建立）
     * 2. 写入HTTP请求
     * 3. 读取HTTP响应
     * 4. 解析并返回响应
     */
    @Override
    public Response intercept(Chain chain) throws IOException {
        // TODO: 步骤1 - 获取请求和连接
        // Request request = chain.request();
        
        // 获取连接（由ConnectInterceptor建立）
        // RealConnection connection = getConnection(chain);
        
        // TODO: 步骤2 - 写入请求
        // writeRequest(connection, request);
        
        // TODO: 步骤3 - 读取响应
        // Response response = readResponse(connection, request);
        
        // TODO: 步骤4 - 返回响应
        // return response;
        
        return null;
    }

    /**
     * 从Chain中获取连接
     * 
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
        // TODO: 步骤1 - 检查是否是ConnectedChain
        // if (chain instanceof ConnectInterceptor.ConnectedChain) {
        //     return ((ConnectInterceptor.ConnectedChain) chain).connection();
        // }
        
        // TODO: 步骤2 - 降级方案：重新建立连接
        // Request request = chain.request();
        // String[] hostPort = parseUrl(request.url());
        // String host = hostPort[0];
        // int port = Integer.parseInt(hostPort[1]);
        // 
        // RealConnection connection = new RealConnection(host, port);
        // connection.connect(
        //     chain.connectTimeoutMillis(),
        //     chain.readTimeoutMillis()
        // );
        // return connection;
        
        return null;
    }

    /**
     * 写入HTTP请求
     * 
     * 【HTTP请求格式】
     * 请求行：GET /path HTTP/1.1\r\n
     * 请求头：Header-Name: Header-Value\r\n
     * 空行：\r\n
     * 请求体：[body content]
     * 
     * 【为什么用\r\n？】
     * - HTTP规范要求使用CRLF（\r\n）作为行分隔符
     * - \r 是回车（Carriage Return）
     * - \n 是换行（Line Feed）
     * 
     * 【关键点】
     * - 每一行都以\r\n结尾
     * - 头部和body之间有一个空行（\r\n\r\n）
     * - 字符串使用UTF-8编码
     * 
     * @param connection 连接对象
     * @param request 请求对象
     * @throws IOException 写入失败时抛出
     */
    private void writeRequest(RealConnection connection, Request request) throws IOException {
        // TODO: 步骤1 - 获取输出流
        // OutputStream out = connection.getOutputStream();
        // BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));

        // TODO: 步骤2 - 写入请求行
        // 格式：GET /path HTTP/1.1\r\n
        // String path = getPath(request.url());
        // writer.write(request.method() + " " + path + " HTTP/1.1\r\n");

        // TODO: 步骤3 - 写入请求头
        // for (Map.Entry<String, String> entry : request.headers().entrySet()) {
        //     writer.write(entry.getKey() + ": " + entry.getValue() + "\r\n");
        // }

        // TODO: 步骤4 - 写入空行（表示请求头结束）
        // writer.write("\r\n");
        // writer.flush();

        // TODO: 步骤5 - 写入请求体（如果有）
        // if (request.body() != null) {
        //     request.body().writeTo(out);
        // }

        // TODO: 步骤6 - 日志
        // System.out.println("已发送请求: " + request.method() + " " + request.url());
    }

    /**
     * 读取HTTP响应
     * 
     * 【HTTP响应格式】
     * 状态行：HTTP/1.1 200 OK\r\n
     * 响应头：Header-Name: Header-Value\r\n
     * 空行：\r\n
     * 响应体：[body content]
     * 
     * 【读取策略】
     * 1. 读取状态行，解析状态码和消息
     * 2. 循环读取响应头，直到空行
     * 3. 根据Content-Length读取响应体
     * 
     * 【关键点】
     * - 空行表示头部结束
     * - Content-Length指定响应体长度
     * - 如果没有Content-Length，读取到流结束
     * 
     * @param connection 连接对象
     * @param request 请求对象
     * @return 响应对象
     * @throws IOException 读取失败时抛出
     */
    private Response readResponse(RealConnection connection, Request request) throws IOException {
        // TODO: 步骤1 - 获取输入流
        // InputStream in = connection.getInputStream();
        // BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));

        // TODO: 步骤2 - 读取状态行
        // HTTP/1.1 200 OK
        // String statusLine = reader.readLine();
        // if (statusLine == null) {
        //     throw new IOException("空响应");
        // }

        // TODO: 步骤3 - 解析状态行
        // 格式：HTTP/1.1 200 OK
        // 分割：["HTTP/1.1", "200", "OK"]
        // String[] parts = statusLine.split(" ", 3);
        // if (parts.length < 2) {
        //     throw new IOException("无效的状态行: " + statusLine);
        // }
        // 
        // int code = Integer.parseInt(parts[1]);
        // String message = parts.length >= 3 ? parts[2] : "";

        // TODO: 步骤4 - 创建Response Builder
        // Response.Builder responseBuilder = new Response.Builder()
        //         .request(request)
        //         .code(code)
        //         .message(message);

        // TODO: 步骤5 - 读取响应头
        // String line;
        // long contentLength = -1;
        // while ((line = reader.readLine()) != null) {
        //     if (line.isEmpty()) {
        //         // 空行，表示响应头结束
        //         break;
        //     }
        //
        //     // 解析响应头：Name: Value
        //     int colonIndex = line.indexOf(":");
        //     if (colonIndex != -1) {
        //         String name = line.substring(0, colonIndex).trim();
        //         String value = line.substring(colonIndex + 1).trim();
        //         responseBuilder.header(name, value);
        //
        //         // 记录Content-Length
        //         if (name.equalsIgnoreCase("Content-Length")) {
        //             try {
        //                 contentLength = Long.parseLong(value);
        //             } catch (NumberFormatException e) {
        //                 // 忽略
        //             }
        //         }
        //     }
        // }

        // TODO: 步骤6 - 读取响应体
        // byte[] bodyBytes;
        // if (contentLength > 0) {
        //     // 已知长度，按长度读取
        //     bodyBytes = readExactly(in, (int) contentLength);
        // } else if (contentLength == 0) {
        //     // 没有响应体
        //     bodyBytes = new byte[0];
        // } else {
        //     // 长度未知，读取到流结束
        //     // 注意：这会关闭连接，无法复用
        //     ByteArrayOutputStream out = new ByteArrayOutputStream();
        //     byte[] buffer = new byte[8192];
        //     int len;
        //     while ((len = in.read(buffer)) != -1) {
        //         out.write(buffer, 0, len);
        //     }
        //     bodyBytes = out.toByteArray();
        // }

        // TODO: 步骤7 - 创建ResponseBody
        // String contentType = responseBuilder.build().header("Content-Type");
        // if (contentType == null) {
        //     contentType = "text/plain";
        // }
        // ResponseBody body = ResponseBody.create(bodyBytes, contentType);
        // responseBuilder.body(body);

        // TODO: 步骤8 - 构建并返回Response
        // Response response = responseBuilder.build();
        // System.out.println("已收到响应: " + code + " " + message + ", 大小: " + bodyBytes.length + " 字节");
        // 
        // return response;
        
        return null;
    }

    /**
     * 精确读取指定长度的字节
     * 
     * 【为什么需要这个方法？】
     * - InputStream.read()不保证读取指定长度
     * - 可能返回少于请求的字节数
     * - 需要循环读取直到满足长度要求
     * 
     * 【实现思路】
     * 1. 创建指定长度的字节数组
     * 2. 循环读取，直到填满数组
     * 3. 如果流提前结束，抛出异常
     * 
     * @param in 输入流
     * @param length 要读取的长度
     * @return 字节数组
     * @throws IOException 读取失败或流提前结束
     */
    private byte[] readExactly(InputStream in, int length) throws IOException {
        // TODO: 实现精确读取
        // byte[] bytes = new byte[length];
        // int offset = 0;
        // while (offset < length) {
        //     int count = in.read(bytes, offset, length - offset);
        //     if (count == -1) {
        //         throw new EOFException("流在预期之前结束");
        //     }
        //     offset += count;
        // }
        // return bytes;
        
        return null;
    }

    /**
     * 从URL中提取路径
     * 
     * 【URL格式】
     * http://example.com:8080/api/users?id=1
     * ↓
     * /api/users?id=1
     * 
     * 【特殊情况】
     * http://example.com → /
     * 
     * @param url URL字符串
     * @return 路径部分
     */
    private String getPath(String url) {
        // TODO: 实现路径提取
        // try {
        //     // 去除协议
        //     if (url.startsWith("http://")) {
        //         url = url.substring(7);
        //     } else if (url.startsWith("https://")) {
        //         url = url.substring(8);
        //     }
        //
        //     // 提取路径（从第一个/开始）
        //     int slashIndex = url.indexOf("/");
        //     if (slashIndex != -1) {
        //         return url.substring(slashIndex);
        //     } else {
        //         return "/";  // 没有路径，返回根路径
        //     }
        // } catch (Exception e) {
        //     return "/";
        // }
        
        return null;
    }

    /**
     * 解析URL
     */
    private String[] parseUrl(String url) {
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


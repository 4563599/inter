package com.myokhttp;

import java.io.IOException;
import java.net.SocketTimeoutException;

/**
 * 重试和重定向拦截器
 * 
 * 【为什么需要这个拦截器？】
 * 1. 网络请求可能失败：超时、连接失败等
 * 2. 服务器可能返回重定向：301, 302, 307等
 * 3. 需要自动处理这些情况，提升用户体验
 * 
 * 【核心职责】
 * 1. 处理请求失败的重试
 * 2. 处理HTTP重定向（3xx状态码）
 * 3. 限制重试和重定向次数
 * 
 * 【重试策略】
 * 可以重试的情况：
 * - 网络超时（SocketTimeoutException）
 * - 连接失败（Connection reset）
 * - DNS解析失败
 * 
 * 不能重试的情况：
 * - 4xx客户端错误（404, 401等）
 * - SSL握手失败
 * - 请求被取消
 * 
 * 【重定向处理】
 * - 301/302/303：改为GET请求
 * - 307/308：保持原有方法
 * - 最多重定向20次
 * 
 * @author Your Name
 */
public class RetryAndFollowUpInterceptor implements Interceptor {
    
    /**
     * 最大重定向次数
     * 
     * 【为什么是20？】
     * - 防止无限重定向循环
     * - HTTP规范建议的值
     * - Chrome、Firefox等浏览器也是20
     */
    private static final int MAX_FOLLOW_UPS = 20;
    
    /**
     * OkHttpClient实例
     * 
     * 【为什么需要？】
     * - 获取配置（是否允许重试、是否跟随重定向）
     */
    private final OkHttpClient client;

    public RetryAndFollowUpInterceptor(OkHttpClient client) {
        this.client = client;
    }

    /**
     * 拦截方法
     * 
     * 【核心逻辑：while(true)循环】
     * 1. 尝试执行请求
     * 2. 如果失败，判断是否可以重试
     * 3. 如果成功，判断是否需要重定向
     * 4. 如果需要重定向，构建新请求继续循环
     * 5. 否则返回响应
     * 
     * 【为什么用while(true)？】
     * - 不知道需要重试/重定向多少次
     * - 直到成功或达到最大次数
     */
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        int followUpCount = 0;  // 重定向计数

        // TODO: 实现循环处理
        // while (true) {
        //     Response response = null;
        //     boolean releaseConnection = true;
        //     
        //     try {
        //         // 1. 尝试执行请求
        //         response = chain.proceed(request);
        //         releaseConnection = false;
        //     } catch (IOException e) {
        //         // 2. 请求失败，判断是否可以重试
        //         if (!client.retryOnConnectionFailure()) {
        //             throw e;  // 不允许重试
        //         }
        //         
        //         if (!isRecoverable(e)) {
        //             throw e;  // 这个异常不能重试
        //         }
        //         
        //         // 可以重试，继续循环
        //         System.out.println("请求失败，尝试重试: " + e.getMessage());
        //         continue;
        //     } finally {
        //         // 如果发生异常，可能需要释放连接
        //         if (releaseConnection && response == null) {
        //             // 连接会在下次重试时重新建立
        //         }
        //     }
        //
        //     // 3. 检查是否需要重定向
        //     Request followUp = followUpRequest(response);
        //     
        //     if (followUp == null) {
        //         // 不需要重定向，返回响应
        //         return response;
        //     }
        //
        //     // 4. 检查重定向次数
        //     followUpCount++;
        //     if (followUpCount > MAX_FOLLOW_UPS) {
        //         throw new IOException("重定向次数过多: " + followUpCount);
        //     }
        //
        //     // 5. 继续处理重定向请求
        //     System.out.println("重定向到: " + followUp.url());
        //     request = followUp;
        // }
        
        return null;
    }

    /**
     * 判断异常是否可以重试
     * 
     * 【判断标准】
     * 1. 超时异常：可以重试
     * 2. 连接被重置：可以重试
     * 3. 其他IOException：可以重试
     * 
     * 【不能重试的情况】
     * - SSL异常
     * - 协议异常
     * - 证书验证失败
     * 
     * @param e 异常
     * @return 是否可以重试
     */
    private boolean isRecoverable(IOException e) {
        // TODO: 实现判断逻辑
        
        // 1. 超时可以重试
        // if (e instanceof SocketTimeoutException) {
        //     return true;
        // }

        // 2. 连接被重置可以重试
        // if (e.getMessage() != null && e.getMessage().contains("Connection reset")) {
        //     return true;
        // }

        // 3. 其他情况，保守起见，允许重试
        // return true;
        
        return false;
    }

    /**
     * 根据响应构建重定向请求
     * 
     * 【返回值】
     * - 需要重定向：返回新的Request
     * - 不需要重定向：返回null
     * 
     * 【处理的状态码】
     * 300: Multiple Choices
     * 301: Moved Permanently
     * 302: Found
     * 303: See Other
     * 307: Temporary Redirect
     * 308: Permanent Redirect
     * 
     * @param response 响应
     * @return 重定向请求，如果不需要重定向返回null
     * @throws IOException 构建请求失败时抛出
     */
    private Request followUpRequest(Response response) throws IOException {
        // TODO: 步骤1 - 检查是否允许重定向
        // if (!client.followRedirects()) {
        //     return null;
        // }

        // TODO: 步骤2 - 获取状态码和方法
        // int code = response.code();
        // String method = response.request().method();
        
        // TODO: 步骤3 - 根据状态码判断
        // switch (code) {
        //     case 300: // Multiple Choices
        //     case 301: // Moved Permanently
        //     case 302: // Found
        //     case 303: // See Other
        //         // 这些状态码需要重定向
        //         break;
        //         
        //     case 307: // Temporary Redirect
        //     case 308: // Permanent Redirect
        //         // 这些状态码保持原有的请求方法
        //         break;
        //         
        //     case 401: // Unauthorized
        //         // 需要身份验证，暂不处理
        //         return null;
        //         
        //     case 407: // Proxy Authentication Required
        //         // 需要代理验证，暂不处理
        //         return null;
        //         
        //     case 408: // Request Timeout
        //         // 超时，可以重试
        //         return response.request();
        //         
        //     case 503: // Service Unavailable
        //         // 服务不可用，可以重试
        //         return response.request();
        //         
        //     default:
        //         // 其他状态码不需要重定向
        //         return null;
        // }

        // TODO: 步骤4 - 获取Location头
        // String location = response.header("Location");
        // if (location == null) {
        //     return null;  // 没有Location头，无法重定向
        // }

        // TODO: 步骤5 - 构建重定向请求
        // Request.Builder builder = response.request().newBuilder();
        
        // 5.1 处理请求方法
        // 对于303，总是使用GET
        // 对于301和302，如果原方法是POST，改为GET
        // if (code == 303 || ((code == 301 || code == 302) && method.equals("POST"))) {
        //     builder.method("GET", null);
        // }
        
        // 5.2 更新URL
        // builder.url(resolveUrl(response.request().url(), location));
        
        // TODO: 步骤6 - 返回新请求
        // return builder.build();
        
        return null;
    }

    /**
     * 解析重定向URL
     * 
     * 【Location可能是两种形式】
     * 1. 绝对URL：http://example.com/new
     * 2. 相对URL：/new 或 new
     * 
     * 【处理方式】
     * - 如果是绝对URL，直接使用
     * - 如果是相对URL，需要拼接到base URL
     * 
     * @param baseUrl 原URL
     * @param location Location头的值
     * @return 完整的URL
     */
    private String resolveUrl(String baseUrl, String location) {
        // TODO: 步骤1 - 检查是否是绝对URL
        // if (location.startsWith("http://") || location.startsWith("https://")) {
        //     return location;  // 绝对URL，直接返回
        // }
        
        // TODO: 步骤2 - 处理相对URL
        // if (location.startsWith("/")) {
        //     // 绝对路径：/api/users
        //     // 需要拼接 scheme://host + location
        //     
        //     // 2.1 找到协议结束位置
        //     int protocolEnd = baseUrl.indexOf("://");
        //     
        //     // 2.2 找到路径开始位置
        //     int pathStart = baseUrl.indexOf("/", protocolEnd + 3);
        //     
        //     // 2.3 拼接
        //     if (pathStart == -1) {
        //         // 原URL没有路径
        //         return baseUrl + location;
        //     }
        //     return baseUrl.substring(0, pathStart) + location;
        // } else {
        //     // 相对路径：api/users
        //     // 需要拼接到当前目录
        //     
        //     int lastSlash = baseUrl.lastIndexOf("/");
        //     return baseUrl.substring(0, lastSlash + 1) + location;
        // }
        
        return null;
    }
}

/*
【编写提示】

1. 【理解while(true)循环】
   
   这是一个无限循环，通过以下方式退出：
   - return response：成功，返回响应
   - throw IOException：失败且不能重试
   - continue：重试或重定向，继续循环
   
   流程：
   ```
   while (true) {
       try {
           response = chain.proceed(request);
           
           // 检查是否需要重定向
           if (需要重定向) {
               request = 新请求;
               continue;  // 继续循环
           }
           
           return response;  // 不需要重定向，退出循环
       } catch (IOException e) {
           if (可以重试) {
               continue;  // 继续循环
           }
           throw e;  // 不能重试，退出循环
       }
   }
   ```

2. 【HTTP重定向的处理】
   
   不同状态码的处理方式：
   
   301 Moved Permanently（永久重定向）：
   - POST → GET
   - 其他方法不变
   
   302 Found（临时重定向）：
   - POST → GET
   - 其他方法不变
   
   303 See Other：
   - 总是改为GET
   
   307 Temporary Redirect：
   - 保持原有方法
   
   308 Permanent Redirect：
   - 保持原有方法

3. 【为什么限制20次？】
   
   防止无限循环：
   ```
   A → 301 → B
   B → 301 → A
   A → 301 → B
   ...无限循环
   ```
   
   限制20次后抛出异常

4. 【Location的三种形式】
   
   绝对URL：
   ```
   Location: http://example.com/new-path
   ```
   
   绝对路径：
   ```
   Location: /new-path
   需要拼接：http://example.com + /new-path
   ```
   
   相对路径：
   ```
   Location: new-path
   原URL：http://example.com/old/path
   需要拼接：http://example.com/old/ + new-path
   ```

【使用示例】

这个拦截器自动工作：

```java
// 用户只需要这样
Request request = new Request.Builder()
    .url("http://example.com/old")  // 会重定向到/new
    .build();

Response response = client.newCall(request).execute();
System.out.println(response.request().url());  // 输出：http://example.com/new

// 可以查看重定向历史
if (response.priorResponse() != null) {
    System.out.println("发生了重定向");
    System.out.println("原状态码：" + response.priorResponse().code());
}
```

【常见错误】

❌ 错误1：无限循环
```java
while (true) {
    // 忘记return或throw，永远不退出
}
```

❌ 错误2：重定向次数没限制
```java
// 没有检查followUpCount
// 可能导致无限重定向
```

❌ 错误3：相对URL处理错误
```java
// 原URL：http://example.com/api/users
// Location: new
// 错误：http://example.com/new
// 正确：http://example.com/api/new
```

【测试方法】
```java
// 测试重定向
OkHttpClient client = new OkHttpClient.Builder()
    .followRedirects(true)
    .build();

Request request = new Request.Builder()
    .url("http://httpbin.org/redirect/2")  // 会重定向2次
    .build();

Response response = client.newCall(request).execute();
System.out.println("最终状态码：" + response.code());  // 200
System.out.println("重定向次数：" + countRedirects(response));  // 2

// 测试禁用重定向
OkHttpClient noRedirect = new OkHttpClient.Builder()
    .followRedirects(false)
    .build();

Response response2 = noRedirect.newCall(request).execute();
System.out.println("状态码：" + response2.code());  // 302
```

【预计编写时间】1 小时

【难度】⭐⭐⭐⭐☆

【重点】
- 理解while(true)循环的退出条件
- 理解不同重定向状态码的处理
- 掌握相对URL的解析
- 注意限制重试和重定向次数
*/


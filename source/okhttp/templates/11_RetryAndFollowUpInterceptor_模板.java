package com.myokhttp;

import java.io.IOException;
import java.net.SocketTimeoutException;

/**
 * 重试和重定向拦截器
 * <p>
 * 【为什么需要这个拦截器？】
 * 1. 网络请求可能失败：超时、连接失败等
 * 2. 服务器可能返回重定向：301, 302, 307等
 * 3. 需要自动处理这些情况，提升用户体验
 * <p>
 * 【核心职责】
 * 1. 处理请求失败的重试
 * 2. 处理HTTP重定向（3xx状态码）
 * 3. 限制重试和重定向次数
 * <p>
 * 【重试策略】
 * 可以重试的情况：
 * - 网络超时（SocketTimeoutException）
 * - 连接失败（Connection reset）
 * - DNS解析失败
 * <p>
 * 不能重试的情况：
 * - 4xx客户端错误（404, 401等）
 * - SSL握手失败
 * - 请求被取消
 * <p>
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
     * <p>
     * 【为什么是20？】
     * - 防止无限重定向循环
     * - HTTP规范建议的值
     * - Chrome、Firefox等浏览器也是20
     */
    private static final int MAX_FOLLOW_UPS = 20;

    /**
     * OkHttpClient实例
     * <p>
     * 【为什么需要？】
     * - 获取配置（是否允许重试、是否跟随重定向）
     */
    private final OkHttpClient client;

    public RetryAndFollowUpInterceptor(OkHttpClient client) {
        this.client = client;
    }

    /**
     * 拦截方法
     * <p>
     * 【核心逻辑：while(true)循环】
     * 1. 尝试执行请求
     * 2. 如果失败，判断是否可以重试
     * 3. 如果成功，判断是否需要重定向
     * 4. 如果需要重定向，构建新请求继续循环
     * 5. 否则返回响应
     * <p>
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

        // ⭐ 核心逻辑：死循环 ⭐
        // 只要没成功，或者服务器让我去别处，我就一直转圈
        while (true) {
            Response response;
            boolean releaseConnection = true;

            try {
                // 1. 尝试去执行请求（把任务传给下一个拦截器）
                response = chain.proceed(request);
                releaseConnection = false; // 成功拿到响应，连接暂时保留
            } catch (IOException e) {
                // 2. 失败了！看看能不能重试
                if (!recover(e, client)) {
                    throw e; // 没救了，抛出异常，结束
                }

                // 还有救！继续下一次循环（重试）
                System.out.println("网络请求失败，正在重试... 错误: " + e.getMessage());
                continue;
            } finally {
                // 如果是致命错误导致 response 为空，释放连接
                if (releaseConnection) {
                    // streamAllocation.release(); // 真实源码会有这个释放操作
                }
            }

            // 3. 成功了！看看服务器有没有让我滚（重定向）
            Request followUp = followUpRequest(response);

            if (followUp == null) {
                // 服务器没让我滚，或者我也不想动了，那就把结果给用户
                return response;
            }

            // 4. 服务器让我滚，我得看看是不是滚太多次了
            // 上一次的响应要关闭，因为它没用了
            response.close();

            if (++followUpCount > MAX_FOLLOW_UPS) {
                throw new IOException("Too many follow-ups: " + followUpCount);
            }

            // 5. 准备好新的请求，开始下一次循环
            request = followUp;
            System.out.println("正在进行第 " + followUpCount + " 次重定向...");
        }
    }

    /**
     * 判断异常是否可以重试
     * <p>
     * 【判断标准】
     * 1. 超时异常：可以重试
     * 2. 连接被重置：可以重试
     * 3. 其他IOException：可以重试
     * <p>
     * 【不能重试的情况】
     * - SSL异常
     * - 协议异常
     * - 证书验证失败
     *
     * @param e 异常
     * @return 是否可以重试
     */
    private boolean isRecoverable(IOException e) {
        // 用户配置了“不允许重试”，那就别试了
        // if (!client.retryOnConnectionFailure()) return false;

        // 1. 这种病通常没救（协议错误、证书错误）
        // if (e instanceof ProtocolException) return false;

        // 2. 超时异常：通常可以重试（网络波动）
        if (e instanceof SocketTimeoutException) {
            return true;
        }

        // 3. 这种通常是服务器断开连接，可以重试
        if (e.getMessage() != null && e.getMessage().contains("Connection reset")) {
            return true;
        }

        // 4. 其他未知网络错误，保守起见试一试
        return true;
    }

    /**
     * 根据响应构建重定向请求
     * <p>
     * 【返回值】
     * - 需要重定向：返回新的Request
     * - 不需要重定向：返回null
     * <p>
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
        if (response == null) throw new IllegalStateException();

        int code = response.code();
        Request userRequest = response.request();
        String method = userRequest.method();

        switch (code) {
            case 307:
            case 308:
                // 307/308 比较严格：原来是 POST，重定向还得是 POST
                // 如果不是 GET/HEAD，通常不自动处理，因为可能有副作用
                if (!method.equals("GET") && !method.equals("HEAD")) {
                    return null;
                }
                // 这里 break 下去，继续处理 Location
            case 301:
            case 302:
            case 303:
                // 301/302/303 比较随意：
                // 如果原来是 POST，通常会变成 GET（浏览器的潜规则）

                // 1. 拿到新地址
                String location = response.header("Location");
                if (location == null) return null;

                // 2. 解析地址（处理相对路径）
                String url = resolveUrl(userRequest.url(), location);

                // 3. 构建新请求
                Request.Builder requestBuilder = userRequest.newBuilder().url(url);

                // 4. 降级策略：POST -> GET
                if (!method.equals("GET") && !method.equals("HEAD")) {
                    // 303 必须变 GET，301/302 习惯变 GET
                    if (code == 303 || code == 301 || code == 302) {
                        requestBuilder.method("GET", null); // Body 被丢弃了
                        requestBuilder.removeHeader("Content-Type");
                        requestBuilder.removeHeader("Content-Length");
                    } else {
                        // 307/308 保持原样
                        requestBuilder.method(method, userRequest.body());
                    }
                }
                return requestBuilder.build();

            case 408: // 请求超时
                // 服务器说你太慢了，可以再试一次
                return userRequest;

            default:
                return null;
        }

        /**
         * 解析重定向URL
         * <p>
         * 【Location可能是两种形式】
         * 1. 绝对URL：http://example.com/new
         * 2. 相对URL：/new 或 new
         * <p>
         * 【处理方式】
         * - 如果是绝对URL，直接使用
         * - 如果是相对URL，需要拼接到base URL
         *
         * @param baseUrl  原URL
         * @param location Location头的值
         * @return 完整的URL
         */
        private String resolveUrl (String baseUrl, String location){
            // 1. 绝对路径 (http://...)
            if (location.startsWith("http")) {
                return location;
            }

            // 2. 相对路径 (/api/v1) -> 拼接到 host 后面
            // 这里简化处理，假设 baseUrl 是标准格式
            // 实际 OkHttp 使用了 HttpUrl 类来处理复杂的拼接
            if (location.startsWith("/")) {
                // baseUrl: http://host:port/path
                // 找到 host 的结束位置
                int protocolEnd = baseUrl.indexOf("://");
                int rootEnd = baseUrl.indexOf("/", protocolEnd + 3);
                String hostUrl = (rootEnd == -1) ? baseUrl : baseUrl.substring(0, rootEnd);
                return hostUrl + location;
            }

            // 3. 纯相对路径 (detail.html) -> 拼接到当前 path 后面
            int lastSlash = baseUrl.lastIndexOf("/");
            return baseUrl.substring(0, lastSlash + 1) + location;
        }
    }

/**
 * 1. 为什么要由 RetryAndFollowUpInterceptor？
 * 核心作用：自动处理“失败”和“指路”，让用户无感。
 *
 * 如果没有这个拦截器，你在写代码时会非常痛苦，因为你必须自己处理以下脏活累活：
 *
 * 自动重试（容错）： 网络是不稳定的。如果网络突然抖了一下（SocketTimeout）或者连接断了（Connection reset），如果没有这个拦截器，你的 App 直接就报错崩溃了。
 *
 * 它的作用：它捕获了异常，根据策略自动帮你重试。用户看到的结果就是“请求成功了”，根本不知道中间其实失败过一次。
 *
 * 自动重定向（导航）： 服务器经常变卦。比如你访问 http://...，服务器返回 301 让你去 https://...。如果没有它，你得自己解析 301，自己读 Location 头，自己重新发起一个请求。
 *
 * 它的作用：它帮你自动处理 301/302/307/308 等状态码，甚至帮你处理 POST 转 GET 的潜规则，最多允许重定向 20 次以防止死循环。
 *
 * 一句话总结：它是**“坚韧的办事员”**，遇到困难（报错）不放弃，被踢皮球（重定向）也愿意跑腿，直到拿到结果或彻底没戏。
 */

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


package com.myokhttp;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP 响应对象
 * <p>
 * 【为什么需要这个类？】
 * - 封装服务器返回的所有信息
 * - 包含状态码、响应头、响应体等
 * <p>
 * 【为什么也设计成不可变？】
 * - 与 Request 相同的理由：线程安全、防止意外修改
 * - 响应可能被缓存，不可变对象更安全
 * <p>
 * 【与 Request 的区别】
 * - Response 包含 Request 的引用（知道这个响应对应哪个请求）
 * - 有 priorResponse 字段（支持重定向链）
 * - 有 isSuccessful() 等便利方法
 * <p>
 * 【priorResponse 的作用】
 * 记录重定向历史，例如：
 * Request A → 301 → Request B → 200
 * 最终 Response 的 priorResponse 指向 301 响应
 *
 * @author Your Name
 */
public class Response {

    // ========== 字段定义 ==========

    /**
     * 对应的请求
     * <p>
     * 【为什么需要？】
     * - 响应总是对应一个请求
     * - 调试时可以看到完整的请求-响应对
     */
    private final Request request;

    /**
     * HTTP 状态码
     * <p>
     * 【常见状态码】
     * - 2xx：成功（200 OK, 201 Created）
     * - 3xx：重定向（301, 302, 304）
     * - 4xx：客户端错误（400, 401, 404）
     * - 5xx：服务器错误（500, 502, 503）
     */
    private final int code;

    /**
     * 状态消息
     * <p>
     * 【示例】
     * - code=200, message="OK"
     * - code=404, message="Not Found"
     */
    private final String message;

    /**
     * 响应头
     */
    private final Map<String, String> headers;

    /**
     * 响应体
     */
    private final ResponseBody body;

    /**
     * 前一个响应（用于重定向链）
     * <p>
     * 【为什么需要？】
     * - 记录重定向历史
     * - 调试时可以追溯整个重定向过程
     * <p>
     * 【示例】
     * 请求 http://example.com/old
     * → 301 重定向到 http://example.com/new
     * → 200 OK
     * <p>
     * 最终 Response:
     * - code = 200
     * - priorResponse.code = 301
     */
    private final Response priorResponse;

    /**
     * 私有构造函数
     */
    private Request(Builder builder) {
        // TODO: 从 builder 复制所有字段
        this.request = builder.request;
        this.code = builder.code;
        this.message = builder.message;
        this.body = builder.body;
        this.priorResponse = builder.priorResponse;

        // 依然是“防御性拷贝”，防止外部修改 Builder 里的 Map 影响到这里
        this.headers = new HashMap<>(builder.headers);
    }

    // ========== Getter 方法 ==========

    public Request request() {
        // TODO: 返回 request
        return request;
    }

    public int code() {
        // TODO: 返回 code
        return code;
    }

    /**
     * 判断是否成功
     * <p>
     * 【为什么这样判断？】
     * - HTTP 规范：2xx 表示成功
     * - code >= 200 && code < 300
     *
     * @return 是否成功
     */
    public boolean isSuccessful() {
        // TODO: return code >= 200 && code < 300;
        return code >= 200 && code < 300;
    }

    public String message() {
        // TODO: 返回 message
        return message;
    }

    public Map<String, String> headers() {
        // TODO: 返回 headers 的副本
        return new HashMap<>(headers); // 返回副本
    }

    public String header(String name) {
        // TODO: 返回 headers.get(name)
        return headers.get(name);
    }

    public ResponseBody body() {
        // TODO: 返回 body
        return body;
    }

    public Response priorResponse() {
        // TODO: 返回 priorResponse
        return priorResponse;
    }

    public Builder newBuilder() {
        // TODO: 返回新的 Builder，基于当前对象
        return new Builder(this);
    }

    @Override
    public String toString() {
        return "Response{code=" + code + ", message=" + message + "}";
    }

    // ========== Builder 类 ==========

    public static class Builder {
        private Request request;
        private int code = -1;  // -1 表示未设置
        private String message;
        private Map<String, String> headers = new HashMap<>();
        private ResponseBody body;
        private Response priorResponse;

        public Builder() {
        }

        private Builder(Response response) {
            this.request = response.request;
            this.code = response.code;
            this.message = response.message;
            this.body = response.body;
            this.priorResponse = response.priorResponse;
            this.headers = new HashMap<>(response.headers);

        }

        public Builder request(Request request) {
            this.request = request;
            return this;
        }

        public Builder code(int code) {
            this.code = code;
            return this;
        }

        public Builder message(String message) {
            // TODO: 设置 message，返回 this
            this.message = message;
            return this;
        }

        public Builder header(String name, String value) {
            // TODO: 设置 header，返回 this
            headers.put(name, value);
            return this;
        }

        public Builder addHeader(String name, String value) {
            // TODO: 调用 header()
            headers.put(name, value);
            return this;
        }

        public Builder body(ResponseBody body) {
            // TODO: 设置 body，返回 this
            this.body = body;
            return this;
        }

        /**
         * 设置前一个响应
         * <p>
         * 【什么时候用？】
         * - RetryAndFollowUpInterceptor 处理重定向时
         * - 将 301/302 响应设置为 priorResponse
         */
        public Builder priorResponse(Response response) {
            // TODO: 设置 priorResponse，返回 this
            this.priorResponse = response;
            return this;
        }

        public Response build() {
            // TODO: 
            // 1. 检查 request 是否为 null
            // 2. 检查 code 是否 < 0
            // 3. return new Response(this);

            if (request == null) throw new IllegalStateException("request == null");
            if (code < 0) throw new IllegalStateException("code < 0: " + code);
            // 名字要改对，是 new Response
            return new Response(this);
        }
    }
}

/*
【编写提示】

1. 【与 Request 非常相似】
   - 都是不可变对象
   - 都用 Builder 模式
   - 实现方式几乎一样

2. 【主要区别】
   - Response 有 code 和 message
   - Response 有 priorResponse（重定向链）
   - Response 有 isSuccessful() 便利方法

3. 【isSuccessful() 的实现】
   return code >= 200 && code < 300;
   
   为什么这样判断？
   - HTTP 规范定义：2xx 表示成功
   - 200 OK, 201 Created, 204 No Content 等

4. 【priorResponse 的使用】
   Response response = ...; // 最终响应（200）
   if (response.priorResponse() != null) {
       System.out.println("发生了重定向");
       System.out.println("前一个状态码：" + response.priorResponse().code());
   }

【测试方法】
Response response = new Response.Builder()
    .request(request)
    .code(200)
    .message("OK")
    .header("Content-Type", "application/json")
    .body(body)
    .build();

System.out.println(response.code());         // 200
System.out.println(response.isSuccessful()); // true
System.out.println(response.header("Content-Type")); // application/json

// 测试重定向链
Response redirect = new Response.Builder()
    .request(oldRequest)
    .code(301)
    .message("Moved Permanently")
    .build();

Response finalResponse = new Response.Builder()
    .request(newRequest)
    .code(200)
    .message("OK")
    .priorResponse(redirect)  // 设置重定向链
    .build();

System.out.println(finalResponse.code()); // 200
System.out.println(finalResponse.priorResponse().code()); // 301

【预计编写时间】45 分钟

【难度】⭐⭐☆☆☆（与 Request 类似）
*/


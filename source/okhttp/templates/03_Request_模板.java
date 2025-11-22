package com.myokhttp;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP 请求对象
 * <p>
 * 【为什么需要这个类？】
 * - 封装 HTTP 请求的所有信息
 * - 提供友好的 API 供用户使用
 * <p>
 * 【为什么设计成不可变对象？】
 * 1. 线程安全：多个线程可以安全共享同一个 Request
 * 2. 防止意外修改：在拦截器之间传递时不会被改变
 * 3. 简化设计：不需要考虑状态变化的复杂性
 * 4. 便于缓存：不可变对象可以安全地作为 Map 的 key
 * <p>
 * 【如何实现不可变？】
 * 1. 所有字段都是 final
 * 2. 私有构造函数，只能通过 Builder 创建
 * 3. 集合字段（headers）返回副本，不返回原对象
 * 4. 提供 newBuilder() 用于创建修改后的新对象
 * <p>
 * 【Builder 模式的好处】
 * 1. 链式调用，代码优雅
 * 2. 参数可选，灵活配置
 * 3. 可读性强，见名知义
 *
 * @author Your Name
 */
public class Request {

    // ========== 字段定义（全部 final，确保不可变）==========

    /**
     * 请求 URL
     * <p>
     * 【为什么是 String 而不是 URL 类？】
     * - String 更简单，不需要处理 URL 解析异常
     * - 在拦截器中可以更灵活地修改
     */
    private final String url;

    /**
     * HTTP 方法（GET, POST, PUT, DELETE 等）
     * <p>
     * 【为什么默认是 GET？】
     * - GET 是最常用的方法
     * - 符合 HTTP 规范的默认行为
     */
    private final String method;

    /**
     * 请求头
     * <p>
     * 【为什么用 Map？】
     * - 快速查找某个头
     * - 键值对形式，符合 HTTP 头的结构
     * <p>
     * 【注意】
     * - 这里存储的是副本，保证不可变性
     * - 真实的 OkHttp 使用更高效的 Headers 类
     */
    private final Map<String, String> headers;

    /**
     * 请求体
     * <p>
     * 【为什么可以为 null？】
     * - GET 请求通常没有请求体
     * - null 表示没有请求体
     */
    private final RequestBody body;

    // ========== 私有构造函数 ==========

    /**
     * 私有构造函数，只能通过 Builder 创建
     * <p>
     * 【为什么私有？】
     * - 强制使用 Builder 模式
     * - 确保对象正确初始化
     * - 统一创建方式
     *
     * @param builder Builder 对象
     */
    private Request(Builder builder) {
        // TODO: 从 builder 中复制所有字段
        // 注意：
        // 1. url, method, body 直接赋值
        // 2. headers 需要创建新的 HashMap（防止外部修改）
        //    this.headers = new HashMap<>(builder.headers);
        this.url = builder.url;
        this.method = builder.method;
        this.body = builder.body;

        // ★★★ 重点知识：防御性拷贝 (Defensive Copy) ★★★
        // 为什么不能直接 this.headers = builder.headers; ？
        // 因为 builder 是可变的，如果外部把 builder 改了，Request 也会跟着变，这就破坏了“不可变性”。
        // 所以必须 new 一个新的 Map 把数据拷过来。
        this.headers = new HashMap<>(builder.headers);

    }

    // ========== Getter 方法 ==========

    /**
     * 获取 URL
     */
    public String url() {
        // TODO: 返回 url
        return url;
    }

    /**
     * 获取 HTTP 方法
     */
    public String method() {
        // TODO: 返回 method
        return method;
    }

    /**
     * 获取所有请求头（返回副本）
     * <p>
     * 【为什么返回副本？】
     * - 保证不可变性
     * - 防止外部修改内部状态
     *
     * @return 请求头的副本
     */
    public Map<String, String> headers() {
        // TODO: 返回 headers 的副本
        // return new HashMap<>(headers);
        return new HashMap<>(headers);
    }

    /**
     * 获取指定的请求头
     *
     * @param name 请求头名称
     * @return 请求头值，不存在返回 null
     */
    public String header(String name) {
        // TODO: 返回 headers.get(name)
        return headers.get(name);
    }

    /**
     * 获取请求体
     *
     * @return 请求体，可能为 null
     */
    public RequestBody body() {
        // TODO: 返回 body
        return body;
    }

    /**
     * 创建一个新的 Builder（基于当前对象）
     * <p>
     * 【为什么需要这个方法？】
     * - Request 是不可变的，不能直接修改
     * - 如果需要修改，创建新的 Builder
     * - 拦截器中经常用到：修改请求后创建新请求
     * <p>
     * 【使用示例】
     * Request newRequest = originalRequest.newBuilder()
     * .header("Authorization", "Bearer token")
     * .build();
     *
     * @return 新的 Builder
     */
    public Builder newBuilder() {
        // TODO: 返回一个新的 Builder，基于当前对象
        // return new Builder(this);
        return new Builder(this);
    }

    @Override
    public String toString() {
        return "Request{method=" + method + ", url=" + url + "}";
    }

    // ========== Builder 内部类 ==========

    /**
     * Builder 模式：用于构建 Request 对象
     * <p>
     * 【为什么用内部类？】
     * - 可以访问 Request 的私有构造函数
     * - 逻辑上属于 Request，放在一起更清晰
     * <p>
     * 【Builder 的特点】
     * 1. 字段可变（用于构建过程）
     * 2. 提供链式调用方法
     * 3. build() 创建不可变的 Request
     */
    public static class Builder {
        // 字段（可变，用于构建）
        private String url;
        private String method = "GET";  // 默认 GET
        private Map<String, String> headers = new HashMap<>();
        private RequestBody body;

        /**
         * 默认构造函数（创建新的 Builder）
         */
        public Builder() {
            // TODO: 不需要做什么，使用字段的默认值
        }

        /**
         * 基于现有 Request 的构造函数（用于修改）
         * <p>
         * 【为什么需要这个构造函数？】
         * - 支持 request.newBuilder()
         * - 复制现有 Request 的所有字段
         *
         * @param request 现有的 Request
         */
        private Builder(Request request) {
            // TODO: 从 request 复制所有字段
            // this.url = request.url;
            // this.method = request.method;
            // this.headers = new HashMap<>(request.headers);
            // this.body = request.body;
            this.url = request.url;
            this.method = request.method;
            this.body = request.body;
            // 把 Request 的头再拷回到 Builder 里
            this.headers = new HashMap<>(request.headers);
        }

    }

    /**
     * 设置 URL
     * <p>
     * 【为什么返回 this？】
     * - 支持链式调用
     * - 代码更优雅
     *
     * @param url URL 字符串
     * @return this（支持链式调用）
     */
    public Builder url(String url) {
        // TODO:
        // 1. 检查 url 是否为 null
        // 2. this.url = url;
        // 3. return this;

        if (url == null) throw new IllegalArgumentException("url == null");
        this.url = url;
        return this; // 链式调用关键
    }

    /**
     * 设置 HTTP 方法和请求体
     * <p>
     * 【为什么方法和请求体一起设置？】
     * - HTTP 规范：某些方法（如 GET）不应该有请求体
     * - 一起设置可以强制检查合法性
     *
     * @param method HTTP 方法
     * @param body   请求体（可以为 null）
     * @return this
     */
    public Builder method(String method, RequestBody body) {
        // TODO:
        // 1. 检查 method 是否为空
        // 2. this.method = method;
        // 3. this.body = body;
        // 4. return this;

        if (method == null || method.length() == 0) {
            throw new IllegalArgumentException("method == null or empty");
        }
        // 可以在这里检查：如果是 POST 必须有 body，如果是 GET 必须 body 为 null（OkHttp 源码有这个检查）
        this.method = method;
        this.body = body;
        return this;
    }

    /**
     * GET 请求（快捷方法）
     * <p>
     * 【为什么提供快捷方法？】
     * - GET 最常用，简化调用
     * - 不需要传 body
     */
    public Builder get() {
        // TODO: return method("GET", null);
        return method("GET", null);
    }

    /**
     * POST 请求（快捷方法）
     *
     * @param body 请求体
     */
    public Builder post(RequestBody body) {
        // TODO: return method("POST", body);
        return method("POST", body);
    }

    /**
     * PUT 请求（快捷方法）
     */
    public Builder put(RequestBody body) {
        // TODO: return method("PUT", body);
        return method("PUT", body);
    }

    /**
     * DELETE 请求（快捷方法）
     */
    public Builder delete() {
        // TODO: return method("DELETE", null);
        return method("DELETE", null);
    }

    /**
     * 设置请求头（覆盖同名的）
     *
     * @param name  请求头名称
     * @param value 请求头值
     * @return this
     */
    public Builder header(String name, String value) {
        // TODO:
        // 1. 检查 name 和 value 是否为 null
        // 2. headers.put(name, value);
        // 3. return this;

        if (name == null || value == null)
            throw new IllegalArgumentException("name or value == null");
        headers.put(name, value); // 覆盖旧值
        return this;
    }

    /**
     * 添加请求头（与 header 相同，为了语义清晰）
     */
    public Builder addHeader(String name, String value) {
        // TODO: return header(name, value);
        return header(name, value);
    }

    /**
     * 移除请求头
     *
     * @param name 请求头名称
     * @return this
     */
    public Builder removeHeader(String name) {
        // TODO:
        // 1. headers.remove(name);
        // 2. return this;

        headers.remove(name);
        return this;
    }

    /**
     * 构建 Request 对象
     * <p>
     * 【这是 Builder 的最后一步】
     * - 检查必要字段
     * - 创建不可变的 Request
     *
     * @return Request 对象
     */
    public Request build() {
        // TODO:
        // 1. 检查 url 是否为 null
        // 2. return new Request(this);

        if (url == null) throw new IllegalStateException("url == null");
        return new Request(this);
    }
}
}

/*
【编写提示】

1. 【先实现 Builder 类】
   因为 Request 的构造函数需要 Builder
   
2. 【字段复制的技巧】
   Map 需要创建新实例：
   this.headers = new HashMap<>(builder.headers);
   
   为什么？
   - 防止外部持有 builder 后修改 headers
   - 保证 Request 的不可变性

3. 【链式调用的实现】
   每个方法都返回 this：
   public Builder url(String url) {
       this.url = url;
       return this;  // ← 关键
   }

4. 【使用示例】
   Request request = new Request.Builder()
       .url("https://example.com/api")
       .header("Content-Type", "application/json")
       .post(body)
       .build();

【常见错误】

❌ 错误1：忘记返回 this
public Builder url(String url) {
    this.url = url;
    // 忘记 return this;
}
正确：每个方法都 return this

❌ 错误2：headers 直接赋值
this.headers = builder.headers;  // 错误！
正确：this.headers = new HashMap<>(builder.headers);

❌ 错误3：忘记检查 null
public Builder url(String url) {
    this.url = url;  // 如果 url 是 null 呢？
}
正确：先检查 if (url == null) throw new IllegalArgumentException()

【测试方法】
Request request = new Request.Builder()
    .url("http://example.com")
    .get()
    .header("User-Agent", "Test")
    .build();
    
System.out.println(request.url());     // http://example.com
System.out.println(request.method());  // GET
System.out.println(request.header("User-Agent")); // Test

// 测试不可变性
Request newRequest = request.newBuilder()
    .header("Authorization", "Bearer token")
    .build();
    
System.out.println(request.header("Authorization"));    // null（原对象未变）
System.out.println(newRequest.header("Authorization")); // Bearer token

【预计编写时间】45 分钟

【难度】⭐⭐⭐☆☆

【重点】
- 理解不可变对象的设计
- 掌握 Builder 模式的实现
- 注意字段复制的方式
*/


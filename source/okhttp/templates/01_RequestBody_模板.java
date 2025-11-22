package com.myokhttp;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * 请求体抽象类
 * <p>
 * 【为什么需要这个类？】
 * - HTTP 请求（如 POST）需要发送数据到服务器
 * - 数据可能是 JSON、表单、文件等不同类型
 * - 这个抽象类定义了请求体的通用接口
 * <p>
 * 【为什么设计成抽象类？】
 * 1. 需要提供默认实现（如工厂方法）
 * 2. 不同类型的请求体有不同的实现
 * 3. 支持扩展（用户可以自定义请求体）
 * <p>
 * 【核心方法】
 * - contentType(): 返回内容类型（MIME 类型）
 * - contentLength(): 返回内容长度（如果未知返回 -1）
 * - writeTo(): 将内容写入输出流
 *
 * @author Your Name
 */
public abstract class RequestBody {

    /**
     * 返回 Content-Type（内容类型）
     * <p>
     * 【常见的 Content-Type】
     * - application/json: JSON 数据
     * - application/x-www-form-urlencoded: 表单数据
     * - multipart/form-data: 文件上传
     * - text/plain: 纯文本
     *
     * @return Content-Type 字符串，如 "application/json; charset=utf-8"
     */
    public abstract String contentType();

    /**
     * 返回内容长度
     * <p>
     * 【为什么可能返回 -1？】
     * - 有些情况下不知道长度（如流式传输）
     * - 返回 -1 表示长度未知
     * - 这时会使用 Transfer-Encoding: chunked
     *
     * @return 内容长度（字节数），未知返回 -1
     */
    public long contentLength() {
        return -1;  // 默认返回 -1 表示未知
    }

    /**
     * 将内容写入输出流
     * <p>
     * 【这是最核心的方法】
     * - 在发送请求时被调用
     * - 将实际的数据写入到 Socket 的输出流
     * - 不同类型的请求体有不同的实现
     *
     * @param out 输出流
     * @throws IOException 写入失败时抛出
     */
    public abstract void writeTo(OutputStream out) throws IOException;

    // ========== 工厂方法：方便创建各种类型的 RequestBody ==========

    /**
     * 创建一个文本类型的 RequestBody
     * <p>
     * 【使用示例】
     * RequestBody body = RequestBody.create("Hello", "text/plain");
     *
     * @param content   文本内容
     * @param mediaType 媒体类型
     * @return RequestBody 对象
     * // 1. Markdown 内容
     * String article = "# 这是一个好商品\n\n推荐购买！**五星好评**。";
     * <p>
     * // 2. 创建 RequestBody，指定类型为 text/markdown
     * RequestBody textBody = RequestBody.create(article, "text/markdown; charset=utf-8");
     */
    public static RequestBody create(String content, String mediaType) {
        // TODO: 实现这个方法
        // 提示：
        // 1. 返回一个匿名内部类
        // 2. 实现 contentType(), contentLength(), writeTo() 三个方法
        // 3. 使用 StandardCharsets.UTF_8 编码

        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        return new RequestBody() {
            @java.lang.Override
            public String contentType() {
                return mediaType;
            }

            @java.lang.Override
            public void writeTo(OutputStream out) throws IOException {
                out.write(bytes);
            }

            @java.lang.Override
            public long contentLength() {
                return bytes.length;
            }
        }

        return null;  // 替换为你的实现
    }

    /**
     * 创建一个 JSON 类型的 RequestBody
     * <p>
     * 【使用示例】
     * RequestBody body = RequestBody.createJson("{\"name\":\"张三\"}");
     *
     * @param json JSON 字符串
     * @return RequestBody 对象
     * String orderJson = "{"
     * + "\"orderId\": \"20231120001\","
     * + "\"product\": \"MacBook Pro\","
     * + "\"price\": 12999"
     * + "}";
     * <p>
     * // 2. 创建 RequestBody
     * RequestBody jsonBody = RequestBody.createJson(orderJson);
     */
    public static RequestBody createJson(String json) {
        // TODO: 实现这个方法
        // 提示：调用 create() 方法，mediaType 为 "application/json; charset=utf-8"

        return create(json, "application/json; charset=utf-8");  // 替换为你的实现
    }

    /**
     * 创建一个表单类型的 RequestBody
     * <p>
     * 【表单格式】
     * key1=value1&key2=value2
     * <p>
     * 【使用示例】
     * RequestBody body = RequestBody.createForm("username=zhangsan&password=123456");
     *
     * @param formData 表单数据字符串
     * @return RequestBody 对象
     * // 1. 模拟表单数据（格式：key=value&key2=value2）
     * String loginData = "username=xiaobai&password=123456&device=android";
     * <p>
     * // 2. 创建 RequestBody
     * RequestBody formBody = RequestBody.createForm(loginData);
     */
    public static RequestBody createForm(String formData) {
        // TODO: 实现这个方法
        // 提示：调用 create() 方法，mediaType 为 "application/x-www-form-urlencoded"

        return create(formData, "application/x-www-form-urlencoded");  // 替换为你的实现
    }

    /**
     * 创建一个字节数组类型的 RequestBody
     * <p>
     * 【使用场景】
     * - 发送二进制数据
     * - 上传文件
     *
     * @param bytes     字节数组
     * @param mediaType 媒体类型
     * @return RequestBody 对象
     * <p>
     * // 1. 模拟一张图片的字节数据（假设这是一张 PNG 图片）
     * byte[] imageBytes = new byte[] { -119, 80, 78, 71, 13, 10, 26, 10 }; // PNG 文件头大概长这样
     * <p>
     * // 2. 创建 RequestBody，注意这里要手动指定具体的媒体类型 "image/png"
     * RequestBody imageBody = RequestBody.create(imageBytes, "image/png");
     */
    public static RequestBody create(byte[] bytes, String mediaType) {
        // TODO: 实现这个方法
        // 提示：
        // 1. contentLength() 返回 bytes.length
        // 2. writeTo() 直接 out.write(bytes)
        return new RequestBody() {
            @java.lang.Override
            public String contentType() {
                return mediaType;
            }

            @java.lang.Override
            public void writeTo(OutputStream out) throws IOException {
                out.write(bytes);
            }

            @java.lang.Override
            public long contentLength() {
                return bytes.length;
            }
        }

    }
}

/*
【编写提示】
1. 先实现 create(String, String) 方法
2. 再实现 createJson() 和 createForm()（它们都调用 create）
3. 最后实现 create(byte[], String)

【测试方法】
可以这样测试：
RequestBody body = RequestBody.createJson("{\"test\":\"hello\"}");
System.out.println(body.contentType());  // 应该输出 application/json; charset=utf-8
System.out.println(body.contentLength()); // 应该输出字节数

【预计编写时间】30 分钟
*/


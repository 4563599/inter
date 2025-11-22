package com.myokhttp;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 响应体抽象类
 * <p>
 * 【为什么需要这个类？】
 * - 服务器返回的数据需要被读取
 * - 数据可能很大，不能一次性全部加载到内存
 * - 需要提供多种读取方式（流、字符串、字节数组）
 * <p>
 * 【为什么设计成抽象类？】
 * 1. 提供默认实现（string(), bytes() 基于 byteStream()）
 * 2. 支持不同来源（网络流、文件、内存）
 * 3. 实现 Closeable：确保资源正确关闭
 * <p>
 * 【为什么实现 Closeable？】
 * - 响应体通常包含 InputStream
 * - 必须在使用后关闭，释放资源
 * - 可以使用 try-with-resources 自动关闭
 * <p>
 * 【重要特性】
 * - 只能读取一次！流读取后就关闭了
 * - 必须关闭，否则可能导致连接泄漏
 *
 * @author Your Name
 */
public abstract class ResponseBody implements Closeable {

    /**
     * 返回 Content-Type
     * <p>
     * 【为什么需要？】
     * - 知道响应的类型（JSON、HTML、图片等）
     * - 用于选择正确的解析方式
     *
     * @return Content-Type 字符串
     */
    public abstract String contentType();

    /**
     * 返回内容长度
     * <p>
     * 【为什么可能返回 -1？】
     * - 服务器可能使用 chunked 编码
     * - 流式传输时不知道总长度
     *
     * @return 内容长度（字节数），未知返回 -1
     */
    public abstract long contentLength();

    /**
     * 返回输入流
     * <p>
     * 【为什么返回流而不是直接返回内容？】
     * - 响应可能很大（如大文件）
     * - 流可以按需读取，节省内存
     * - 支持边读边处理
     * <p>
     * 【注意】
     * - 流只能读取一次
     * - 读取后必须关闭
     * <p>
     * InputStream is = response.body().byteStream();
     * FileOutputStream fos = new FileOutputStream("download.apk");
     *
     * @return 输入流
     */
    public abstract InputStream byteStream();

    /**
     * 将响应体读取为字符串
     * <p>
     * 【实现思路】
     * 1. 获取输入流
     * 2. 使用 ByteArrayOutputStream 读取所有字节
     * 3. 转换为字符串（UTF-8 编码）
     * 4. 关闭流
     * <p>
     * 【为什么在 finally 中关闭流？】
     * - 确保即使发生异常也能关闭
     * - 防止资源泄漏
     * <p>
     * 【注意】
     * - 这个方法会读取整个响应到内存
     * - 如果响应很大，可能导致 OOM
     * - 调用后流被关闭，不能再次读取
     * <p>
     * ResponseBody body = response.body();
     * String json = body.string(); // 读完自动关闭，爽！
     *
     * @return 响应体字符串
     * @throws IOException 读取失败时抛出
     */
    public String string() throws IOException {
        // TODO: 实现这个方法
        // 步骤：
        // 1. InputStream in = byteStream();
        // 2. try { 
        //      使用 ByteArrayOutputStream 读取所有字节
        //      buffer = new byte[8192]
        //      while ((len = in.read(buffer)) != -1) { out.write(buffer, 0, len); }
        //    } finally {
        //      in.close();  // 确保关闭
        //    }
        // 3. 转换为字符串：out.toString("UTF-8")

        InputStream in = byteStream();
        if (inputStream == null) return "";
        try {
            // 准备一个内存输出流，像一个蓄水池
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            // 准备一个小水桶（缓冲区），每次舀 8KB
            byte[] data = new byte[8192];
            int len;
            // 循环舀水，直到干涸（read 返回 -1）
            while ((len = inputStream.read(data)) != -1) {
                buffer.write(data, 0, len);
            }
            // 3. 把蓄水池里的水全部转成字符串
            return buffer.toString(StandardCharsets.UTF_8.name());
        }

        /**
         * 将响应体读取为字节数组
         *
         * 【使用场景】
         * - 读取图片、文件等二进制数据
         * - 需要完整的字节数据
         *
         * 【实现思路】
         * - 与 string() 类似，但不转换为字符串
         * - 直接返回字节数组
         *
         * @return 字节数组
         * @throws IOException 读取失败时抛出
         *
         * byte[] imageBytes = response.body().bytes();
         * // Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, ...);
         */
        public byte[] bytes () throws IOException {
            InputStream inputStream = byteStream();
            if (inputStream == null) return new byte[0];

            try {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] data = new byte[8192];
                int len;
                while ((len = inputStream.read(data)) != -1) {
                    buffer.write(data, 0, len);
                }
                // 这里和 string() 的唯一区别：直接返回字节数组
                return buffer.toByteArray();
            } finally {
                close();
            }
        }

        // ========== 工厂方法 ==========

        /**
         * 创建一个简单的 ResponseBody（内存中的数据）
         *
         * 【使用场景】
         * - 测试时创建模拟响应
         * - 缓存的响应
         *
         * @param content 内容字节数组
         * @param contentType Content-Type
         * @return ResponseBody 对象
         */
        public static ResponseBody create ( byte[] content, String contentType){
            // TODO: 实现这个方法
            // 返回一个匿名内部类，实现：
            // 1. contentType() - 返回参数 contentType
            // 2. contentLength() - 返回 content.length
            // 3. byteStream() - 返回 new ByteArrayInputStream(content)
            // 4. close() - ByteArrayInputStream 不需要关闭，空实现即可

            return new ResponseBody() {
                @Override
                public String contentType() {
                    return contentType;
                }

                @Override
                public long contentLength() {
                    return content.length;
                }

                @Override
                public InputStream byteStream() {
                    // 把字节数组包装成流
                    return new ByteArrayInputStream(content);
                }

                @Override
                public void close() throws IOException {
                    // ByteArrayInputStream 不需要关闭
                }
            };
        }

        /**
         * 从 InputStream 创建 ResponseBody
         *
         * 【使用场景】
         * - 从网络读取的响应
         * - 从文件读取的响应
         *
         * 【为什么需要 contentLength 参数？】
         * - InputStream 本身不知道总长度
         * - 需要从 HTTP 头的 Content-Length 获取
         *
         * @param inputStream 输入流
         * @param contentType Content-Type
         * @param contentLength 内容长度
         * @return ResponseBody 对象
         */
        public static ResponseBody create (InputStream inputStream, String contentType,
        long contentLength){
            // TODO: 实现这个方法
            // 返回一个匿名内部类，实现：
            // 1. contentType() - 返回参数 contentType
            // 2. contentLength() - 返回参数 contentLength
            // 3. byteStream() - 返回参数 inputStream
            // 4. close() - 关闭 inputStream

            return new ResponseBody() {
                @Override
                public String contentType() {
                    return contentType;
                }

                @Override
                public long contentLength() {
                    return contentLength;
                }

                @Override
                public InputStream byteStream() {
                    return inputStream;
                }

                @Override
                public void close() throws IOException {
                    inputStream.close();
                }
            };
        }
    }

/*
【编写提示】

1. 【先实现 string() 方法】
   这是最常用的方法，理解了它就理解了读取流程
   
   典型实现：
   InputStream in = byteStream();
   try {
       ByteArrayOutputStream out = new ByteArrayOutputStream();
       byte[] buffer = new byte[8192];  // 8KB 缓冲区
       int len;
       while ((len = in.read(buffer)) != -1) {
           out.write(buffer, 0, len);
       }
       return out.toString("UTF-8");
   } finally {
       in.close();  // 重要！必须关闭
   }

2. 【为什么使用 8192 字节的缓冲区？】
   - 太小：频繁调用 read()，性能差
   - 太大：浪费内存
   - 8KB 是经验值，平衡性能和内存

3. 【bytes() 方法】
   几乎和 string() 一样，只是最后不转换为字符串

4. 【工厂方法】
   create() 方法都是返回匿名内部类
   注意：ByteArrayInputStream 不需要关闭

【常见错误】

❌ 错误1：忘记关闭流
正确：使用 try-finally 确保关闭

❌ 错误2：多次读取
ResponseBody body = response.body();
String s1 = body.string();  // OK
String s2 = body.string();  // 错误！流已关闭
正确：只读取一次，或者保存结果

❌ 错误3：读取大文件到内存
// 如果是 100MB 的文件
String content = body.string();  // 可能 OOM
正确：使用 byteStream() 边读边处理

【测试方法】
byte[] testData = "Hello, World!".getBytes();
ResponseBody body = ResponseBody.create(testData, "text/plain");
System.out.println(body.string());  // 应该输出 "Hello, World!"
// body.string();  // 再次调用会失败

【预计编写时间】30 分钟

【难度】⭐⭐⭐☆☆
*/


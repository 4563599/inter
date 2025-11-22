package com.myokhttp;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 请求体
 * 用于 POST、PUT 等需要发送数据的请求
 */
public abstract class RequestBody {
    
    /**
     * 返回 Content-Type
     */
    public abstract String contentType();
    
    /**
     * 返回内容长度，如果未知返回 -1
     */
    public long contentLength() {
        return -1;
    }
    
    /**
     * 将内容写入输出流
     */
    public abstract void writeTo(OutputStream out) throws IOException;

    /**
     * 创建一个文本类型的 RequestBody
     */
    public static RequestBody create(String content, String mediaType) {
        return new RequestBody() {
            @Override
            public String contentType() {
                return mediaType;
            }

            @Override
            public long contentLength() {
                return content.getBytes(StandardCharsets.UTF_8).length;
            }

            @Override
            public void writeTo(OutputStream out) throws IOException {
                byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
                out.write(bytes);
                out.flush();
            }
        };
    }

    /**
     * 创建一个 JSON 类型的 RequestBody
     */
    public static RequestBody createJson(String json) {
        return create(json, "application/json; charset=utf-8");
    }

    /**
     * 创建一个表单类型的 RequestBody
     */
    public static RequestBody createForm(String formData) {
        return create(formData, "application/x-www-form-urlencoded");
    }

    /**
     * 创建一个字节数组类型的 RequestBody
     */
    public static RequestBody create(byte[] bytes, String mediaType) {
        return new RequestBody() {
            @Override
            public String contentType() {
                return mediaType;
            }

            @Override
            public long contentLength() {
                return bytes.length;
            }

            @Override
            public void writeTo(OutputStream out) throws IOException {
                out.write(bytes);
                out.flush();
            }
        };
    }
}


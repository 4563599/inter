package com.myokhttp;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 响应体
 * 包含服务器返回的数据
 */
public abstract class ResponseBody implements Closeable {
    
    /**
     * 返回 Content-Type
     */
    public abstract String contentType();
    
    /**
     * 返回内容长度，如果未知返回 -1
     */
    public abstract long contentLength();
    
    /**
     * 返回输入流
     */
    public abstract InputStream byteStream();
    
    /**
     * 将响应体读取为字符串
     */
    public String string() throws IOException {
        InputStream in = byteStream();
        try {
            Charset charset = StandardCharsets.UTF_8;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            return out.toString(charset.name());
        } finally {
            in.close();
        }
    }
    
    /**
     * 将响应体读取为字节数组
     */
    public byte[] bytes() throws IOException {
        InputStream in = byteStream();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            return out.toByteArray();
        } finally {
            in.close();
        }
    }

    /**
     * 创建一个简单的 ResponseBody
     */
    public static ResponseBody create(byte[] content, String contentType) {
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
                return new ByteArrayInputStream(content);
            }

            @Override
            public void close() {
                // ByteArrayInputStream 不需要关闭
            }
        };
    }

    /**
     * 从 InputStream 创建 ResponseBody
     */
    public static ResponseBody create(InputStream inputStream, String contentType, long contentLength) {
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


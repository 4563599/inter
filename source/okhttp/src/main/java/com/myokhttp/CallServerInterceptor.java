package com.myokhttp;

import java.io.*;
import java.util.Map;

/**
 * 服务器调用拦截器
 * 
 * 职责：
 * 这是拦截器链的最后一个拦截器，负责真实的网络 I/O：
 * 1. 写入 HTTP 请求
 * 2. 读取 HTTP 响应
 * 3. 解析响应头和响应体
 * 
 * HTTP 请求格式：
 * GET /path HTTP/1.1
 * Host: example.com
 * User-Agent: MyOkHttp/1.0
 * 
 * [请求体]
 * 
 * HTTP 响应格式：
 * HTTP/1.1 200 OK
 * Content-Type: text/html
 * Content-Length: 123
 * 
 * [响应体]
 */
public class CallServerInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        
        // 获取连接（由 ConnectInterceptor 建立）
        RealConnection connection = getConnection(chain);
        
        // 写入请求
        writeRequest(connection, request);
        
        // 读取响应
        Response response = readResponse(connection, request);
        
        return response;
    }

    /**
     * 从 Chain 中获取连接
     * 这里需要一些技巧，因为我们的简化实现
     */
    private RealConnection getConnection(Chain chain) throws IOException {
        // 检查是否是 ConnectedChain
        if (chain instanceof ConnectInterceptor.ConnectedChain) {
            return ((ConnectInterceptor.ConnectedChain) chain).connection();
        }
        
        // 否则，需要重新建立连接（这是一个降级方案）
        Request request = chain.request();
        String[] hostPort = parseUrl(request.url());
        String host = hostPort[0];
        int port = Integer.parseInt(hostPort[1]);
        
        RealConnection connection = new RealConnection(host, port);
        connection.connect(
            chain.connectTimeoutMillis(),
            chain.readTimeoutMillis()
        );
        return connection;
    }

    /**
     * 写入 HTTP 请求
     */
    private void writeRequest(RealConnection connection, Request request) throws IOException {
        OutputStream out = connection.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));

        // 写入请求行：GET /path HTTP/1.1
        String path = getPath(request.url());
        writer.write(request.method() + " " + path + " HTTP/1.1\r\n");

        // 写入请求头
        for (Map.Entry<String, String> entry : request.headers().entrySet()) {
            writer.write(entry.getKey() + ": " + entry.getValue() + "\r\n");
        }

        // 空行，表示请求头结束
        writer.write("\r\n");
        writer.flush();

        // 写入请求体（如果有）
        if (request.body() != null) {
            request.body().writeTo(out);
        }

        System.out.println("已发送请求: " + request.method() + " " + request.url());
    }

    /**
     * 读取 HTTP 响应
     */
    private Response readResponse(RealConnection connection, Request request) throws IOException {
        InputStream in = connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));

        // 读取状态行：HTTP/1.1 200 OK
        String statusLine = reader.readLine();
        if (statusLine == null) {
            throw new IOException("空响应");
        }

        // 解析状态行
        String[] parts = statusLine.split(" ", 3);
        if (parts.length < 2) {
            throw new IOException("无效的状态行: " + statusLine);
        }

        int code = Integer.parseInt(parts[1]);
        String message = parts.length >= 3 ? parts[2] : "";

        // 读取响应头
        Response.Builder responseBuilder = new Response.Builder()
                .request(request)
                .code(code)
                .message(message);

        String line;
        long contentLength = -1;
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty()) {
                // 空行，表示响应头结束
                break;
            }

            // 解析响应头
            int colonIndex = line.indexOf(":");
            if (colonIndex != -1) {
                String name = line.substring(0, colonIndex).trim();
                String value = line.substring(colonIndex + 1).trim();
                responseBuilder.header(name, value);

                // 记录 Content-Length
                if (name.equalsIgnoreCase("Content-Length")) {
                    try {
                        contentLength = Long.parseLong(value);
                    } catch (NumberFormatException e) {
                        // 忽略
                    }
                }
            }
        }

        // 读取响应体
        byte[] bodyBytes;
        if (contentLength > 0) {
            // 已知长度，按长度读取
            bodyBytes = readExactly(in, (int) contentLength);
        } else if (contentLength == 0) {
            // 没有响应体
            bodyBytes = new byte[0];
        } else {
            // 长度未知，读取到流结束（注意：这会关闭连接）
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            bodyBytes = out.toByteArray();
        }

        // 创建 ResponseBody
        String contentType = responseBuilder.build().header("Content-Type");
        if (contentType == null) {
            contentType = "text/plain";
        }
        ResponseBody body = ResponseBody.create(bodyBytes, contentType);
        responseBuilder.body(body);

        Response response = responseBuilder.build();
        System.out.println("已收到响应: " + code + " " + message + ", 大小: " + bodyBytes.length + " 字节");

        return response;
    }

    /**
     * 精确读取指定长度的字节
     */
    private byte[] readExactly(InputStream in, int length) throws IOException {
        byte[] bytes = new byte[length];
        int offset = 0;
        while (offset < length) {
            int count = in.read(bytes, offset, length - offset);
            if (count == -1) {
                throw new EOFException("流在预期之前结束");
            }
            offset += count;
        }
        return bytes;
    }

    /**
     * 从 URL 中提取路径
     */
    private String getPath(String url) {
        try {
            if (url.startsWith("http://")) {
                url = url.substring(7);
            } else if (url.startsWith("https://")) {
                url = url.substring(8);
            }

            int slashIndex = url.indexOf("/");
            if (slashIndex != -1) {
                return url.substring(slashIndex);
            } else {
                return "/";
            }
        } catch (Exception e) {
            return "/";
        }
    }

    /**
     * 解析 URL
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


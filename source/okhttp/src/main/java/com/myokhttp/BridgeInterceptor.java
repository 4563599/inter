package com.myokhttp;

import java.io.IOException;

/**
 * 桥接拦截器
 * 
 * 职责：
 * 1. 补充必要的 HTTP 请求头
 * 2. 处理 gzip 压缩
 * 3. 处理 Content-Length 和 Content-Type
 * 
 * 为什么需要这个拦截器？
 * - 用户构建的 Request 可能缺少一些必要的 HTTP 头
 * - 这个拦截器像一座"桥梁"，连接应用层和网络层
 */
public class BridgeInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request userRequest = chain.request();
        Request.Builder requestBuilder = userRequest.newBuilder();

        RequestBody body = userRequest.body();
        
        // 补充 Content-Type
        if (body != null) {
            String contentType = body.contentType();
            if (contentType != null) {
                requestBuilder.header("Content-Type", contentType);
            }

            // 补充 Content-Length
            long contentLength = body.contentLength();
            if (contentLength != -1) {
                requestBuilder.header("Content-Length", String.valueOf(contentLength));
            } else {
                // 如果不知道长度，使用 chunked 编码
                requestBuilder.header("Transfer-Encoding", "chunked");
            }
        }

        // 补充 Host
        if (userRequest.header("Host") == null) {
            String host = getHost(userRequest.url());
            requestBuilder.header("Host", host);
        }

        // 补充 Connection: Keep-Alive（支持连接复用）
        if (userRequest.header("Connection") == null) {
            requestBuilder.header("Connection", "Keep-Alive");
        }

        // 补充 Accept-Encoding: gzip（支持压缩）
        boolean transparentGzip = false;
        if (userRequest.header("Accept-Encoding") == null) {
            transparentGzip = true;
            requestBuilder.header("Accept-Encoding", "gzip");
        }

        // 补充 User-Agent
        if (userRequest.header("User-Agent") == null) {
            requestBuilder.header("User-Agent", "MyOkHttp/1.0");
        }

        // 执行请求
        Request networkRequest = requestBuilder.build();
        Response networkResponse = chain.proceed(networkRequest);

        // 处理响应
        Response.Builder responseBuilder = networkResponse.newBuilder()
                .request(userRequest);

        // 如果启用了透明 gzip，并且响应是 gzip 压缩的，需要解压
        if (transparentGzip 
            && "gzip".equalsIgnoreCase(networkResponse.header("Content-Encoding"))) {
            // 这里简化处理，实际应该解压响应体
            // responseBuilder.body(new GzipResponseBody(networkResponse.body()));
            System.out.println("响应使用了 gzip 压缩");
        }

        return responseBuilder.build();
    }

    /**
     * 从 URL 中提取 Host
     */
    private String getHost(String url) {
        // 简化处理
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
            
            return url;
        } catch (Exception e) {
            return "unknown";
        }
    }
}


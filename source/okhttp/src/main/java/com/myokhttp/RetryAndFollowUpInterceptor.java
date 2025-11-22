package com.myokhttp;

import java.io.IOException;
import java.net.SocketTimeoutException;

/**
 * 重试和重定向拦截器
 * 
 * 职责：
 * 1. 处理请求失败的重试
 * 2. 处理 3xx 重定向响应
 * 3. 处理网络超时
 * 
 * 重试策略：
 * - 网络超时：可以重试
 * - DNS 解析失败：可以重试
 * - 连接失败：可以重试
 * - 4xx 客户端错误：不重试
 * - SSL 握手失败：不重试
 */
public class RetryAndFollowUpInterceptor implements Interceptor {
    
    // 最大重定向次数
    private static final int MAX_FOLLOW_UPS = 20;
    
    private final OkHttpClient client;

    public RetryAndFollowUpInterceptor(OkHttpClient client) {
        this.client = client;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        int followUpCount = 0;

        while (true) {
            Response response = null;
            boolean releaseConnection = true;
            
            try {
                // 执行请求
                response = chain.proceed(request);
                releaseConnection = false;
            } catch (IOException e) {
                // 请求失败，判断是否需要重试
                if (!client.retryOnConnectionFailure()) {
                    throw e;
                }
                
                if (!isRecoverable(e)) {
                    throw e;
                }
                
                // 重试
                System.out.println("请求失败，尝试重试: " + e.getMessage());
                continue;
            } finally {
                // 如果需要释放连接但没有获得响应，说明发生了异常
                if (releaseConnection && response == null) {
                    // 连接会在下次重试时重新建立
                }
            }

            // 检查是否需要重定向
            Request followUp = followUpRequest(response);
            
            if (followUp == null) {
                // 不需要重定向，返回响应
                return response;
            }

            // 检查重定向次数
            followUpCount++;
            if (followUpCount > MAX_FOLLOW_UPS) {
                throw new IOException("重定向次数过多: " + followUpCount);
            }

            // 继续处理重定向请求
            System.out.println("重定向到: " + followUp.url());
            request = followUp;
        }
    }

    /**
     * 判断异常是否可以重试
     */
    private boolean isRecoverable(IOException e) {
        // 超时可以重试
        if (e instanceof SocketTimeoutException) {
            return true;
        }

        // 连接被重置可以重试
        if (e.getMessage() != null && e.getMessage().contains("Connection reset")) {
            return true;
        }

        // 默认可以重试
        return true;
    }

    /**
     * 根据响应构建重定向请求
     * 
     * @param response 响应
     * @return 重定向请求，如果不需要重定向返回 null
     */
    private Request followUpRequest(Response response) throws IOException {
        if (!client.followRedirects()) {
            return null;
        }

        int code = response.code();
        String method = response.request().method();
        
        switch (code) {
            case 300: // Multiple Choices
            case 301: // Moved Permanently
            case 302: // Found
            case 303: // See Other
                // 这些状态码需要重定向
                break;
                
            case 307: // Temporary Redirect
            case 308: // Permanent Redirect
                // 这些状态码保持原有的请求方法
                break;
                
            case 401: // Unauthorized
                // 需要身份验证，暂不处理
                return null;
                
            case 407: // Proxy Authentication Required
                // 需要代理验证，暂不处理
                return null;
                
            case 408: // Request Timeout
                // 超时，可以重试
                return response.request();
                
            case 503: // Service Unavailable
                // 服务不可用，可以重试
                return response.request();
                
            default:
                // 其他状态码不需要重定向
                return null;
        }

        // 获取 Location 头
        String location = response.header("Location");
        if (location == null) {
            return null;
        }

        // 构建重定向请求
        Request.Builder builder = response.request().newBuilder();
        
        // 对于 303，总是使用 GET
        // 对于 301 和 302，如果原方法是 POST，改为 GET
        if (code == 303 || ((code == 301 || code == 302) && method.equals("POST"))) {
            builder.method("GET", null);
        }
        
        // 更新 URL
        builder.url(resolveUrl(response.request().url(), location));
        
        return builder.build();
    }

    /**
     * 解析重定向 URL
     * 可能是相对 URL 或绝对 URL
     */
    private String resolveUrl(String baseUrl, String location) {
        // 简化处理：如果 location 是完整 URL，直接返回
        if (location.startsWith("http://") || location.startsWith("https://")) {
            return location;
        }
        
        // 否则拼接到 base URL
        if (location.startsWith("/")) {
            // 绝对路径
            int protocolEnd = baseUrl.indexOf("://");
            int pathStart = baseUrl.indexOf("/", protocolEnd + 3);
            if (pathStart == -1) {
                return baseUrl + location;
            }
            return baseUrl.substring(0, pathStart) + location;
        } else {
            // 相对路径
            int lastSlash = baseUrl.lastIndexOf("/");
            return baseUrl.substring(0, lastSlash + 1) + location;
        }
    }
}


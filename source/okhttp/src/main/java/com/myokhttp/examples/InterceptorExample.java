package com.myokhttp.examples;

import com.myokhttp.*;

import java.io.IOException;

/**
 * 示例4：自定义拦截器
 * 
 * 演示如何添加自定义拦截器来：
 * 1. 记录请求日志
 * 2. 添加自定义请求头
 * 3. 修改请求/响应
 */
public class InterceptorExample {
    
    public static void main(String[] args) {
        // 创建日志拦截器
        Interceptor loggingInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                
                System.out.println("========================================");
                System.out.println("发送请求: " + request.method() + " " + request.url());
                System.out.println("请求头: " + request.headers());
                
                long startTime = System.currentTimeMillis();
                
                // 执行请求
                Response response = chain.proceed(request);
                
                long endTime = System.currentTimeMillis();
                
                System.out.println("收到响应: " + response.code() + " " + response.message());
                System.out.println("耗时: " + (endTime - startTime) + " ms");
                System.out.println("响应头: " + response.headers());
                System.out.println("========================================");
                
                return response;
            }
        };

        // 创建添加认证头的拦截器
        Interceptor authInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request originalRequest = chain.request();
                
                // 添加认证头
                Request newRequest = originalRequest.newBuilder()
                        .header("Authorization", "Bearer my-token-123456")
                        .header("X-Custom-Header", "MyValue")
                        .build();
                
                return chain.proceed(newRequest);
            }
        };

        // 创建客户端，添加拦截器
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)   // 应用拦截器
                .addInterceptor(authInterceptor)      // 认证拦截器
                .build();

        // 发送请求
        Request request = new Request.Builder()
                .url("http://httpbin.org/headers")
                .get()
                .build();

        try {
            Response response = client.newCall(request).execute();
            System.out.println("\n最终响应体:");
            System.out.println(response.body().string());
            response.body().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


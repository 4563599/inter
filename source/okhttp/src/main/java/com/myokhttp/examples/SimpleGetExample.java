package com.myokhttp.examples;

import com.myokhttp.*;

import java.io.IOException;

/**
 * 示例1：简单的 GET 请求
 * 
 * 演示最基本的使用方式
 */
public class SimpleGetExample {
    
    public static void main(String[] args) {
        // 1. 创建 OkHttpClient
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10000)
                .readTimeout(10000)
                .build();

        // 2. 创建 Request
        Request request = new Request.Builder()
                .url("http://httpbin.org/get")
                .get()
                .build();

        // 3. 同步执行请求
        try {
            Response response = client.newCall(request).execute();
            
            System.out.println("状态码: " + response.code());
            System.out.println("响应消息: " + response.message());
            System.out.println("响应体: " + response.body().string());
            
            // 关闭响应体
            response.body().close();
        } catch (IOException e) {
            System.err.println("请求失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}


package com.myokhttp.examples;

import com.myokhttp.*;

import java.io.IOException;

/**
 * 示例3：异步请求
 * 
 * 演示如何使用 enqueue() 进行异步请求
 */
public class AsyncExample {
    
    public static void main(String[] args) {
        // 创建客户端
        OkHttpClient client = new OkHttpClient.Builder().build();

        // 创建请求
        Request request = new Request.Builder()
                .url("http://httpbin.org/delay/2")  // 延迟2秒响应
                .get()
                .build();

        System.out.println("开始异步请求...");
        
        // 异步执行请求
        client.newCall(request).enqueue(new Call.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.err.println("请求失败: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                System.out.println("异步请求成功！");
                System.out.println("线程: " + Thread.currentThread().getName());
                System.out.println("状态码: " + response.code());
                
                // 注意：这里是在工作线程中，不是主线程
                String body = response.body().string();
                System.out.println("响应长度: " + body.length() + " 字符");
                
                response.body().close();
            }
        });

        System.out.println("主线程继续执行，不会阻塞");
        
        // 等待异步请求完成
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}


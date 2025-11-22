package com.myokhttp.examples;

import com.myokhttp.*;

import java.io.IOException;

/**
 * 示例6：重定向处理
 * 
 * 演示 RetryAndFollowUpInterceptor 如何自动处理重定向
 */
public class RedirectExample {
    
    public static void main(String[] args) {
        // 创建客户端（启用重定向）
        OkHttpClient client = new OkHttpClient.Builder()
                .followRedirects(true)  // 默认就是 true
                .build();

        // 访问一个会重定向的 URL
        // httpbin.org/redirect/2 会重定向2次
        Request request = new Request.Builder()
                .url("http://httpbin.org/redirect/2")
                .get()
                .build();

        System.out.println("发送请求到重定向URL...\n");

        try {
            Response response = client.newCall(request).execute();
            
            System.out.println("最终状态码: " + response.code());
            System.out.println("最终URL: " + response.request().url());
            
            // 查看重定向历史
            Response priorResponse = response.priorResponse();
            int redirectCount = 0;
            while (priorResponse != null) {
                redirectCount++;
                System.out.println("重定向 " + redirectCount + ": " + 
                    priorResponse.code() + " -> " + 
                    priorResponse.request().url());
                priorResponse = priorResponse.priorResponse();
            }
            
            System.out.println("\n总共重定向了 " + redirectCount + " 次");
            
            response.body().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


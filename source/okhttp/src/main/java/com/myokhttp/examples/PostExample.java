package com.myokhttp.examples;

import com.myokhttp.*;

import java.io.IOException;

/**
 * 示例2：POST 请求
 * 
 * 演示如何发送 JSON 数据
 */
public class PostExample {
    
    public static void main(String[] args) {
        // 创建客户端
        OkHttpClient client = new OkHttpClient.Builder().build();

        // 准备 JSON 数据
        String json = "{\"name\":\"张三\",\"age\":25}";
        
        // 创建 RequestBody
        RequestBody body = RequestBody.createJson(json);

        // 创建 Request
        Request request = new Request.Builder()
                .url("http://httpbin.org/post")
                .post(body)
                .build();

        // 执行请求
        try {
            Response response = client.newCall(request).execute();
            
            if (response.isSuccessful()) {
                System.out.println("POST 请求成功！");
                System.out.println("响应: " + response.body().string());
            } else {
                System.out.println("请求失败，状态码: " + response.code());
            }
            
            response.body().close();
        } catch (IOException e) {
            System.err.println("请求异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
}


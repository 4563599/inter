package com.myokhttp.examples;

import com.myokhttp.*;

import java.io.IOException;

/**
 * 示例5：连接池复用
 * 
 * 演示连接池如何复用连接，提升性能
 */
public class ConnectionPoolExample {
    
    public static void main(String[] args) {
        // 创建客户端（带连接池）
        OkHttpClient client = new OkHttpClient.Builder()
                .connectionPool(new ConnectionPool())
                .build();

        String url = "http://httpbin.org/get";

        System.out.println("发送3个连续请求到同一个服务器...\n");

        // 发送多个请求到同一个服务器
        for (int i = 1; i <= 3; i++) {
            System.out.println("========== 请求 " + i + " ==========");
            
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            try {
                long startTime = System.currentTimeMillis();
                Response response = client.newCall(request).execute();
                long endTime = System.currentTimeMillis();
                
                System.out.println("状态码: " + response.code());
                System.out.println("耗时: " + (endTime - startTime) + " ms");
                System.out.println("连接池状态 - 总连接数: " + 
                    client.connectionPool().connectionCount() + 
                    ", 空闲连接数: " + 
                    client.connectionPool().idleConnectionCount());
                
                response.body().close();
                
                System.out.println();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // 稍微等待一下
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("\n观察：第2、3个请求会复用连接，速度更快！");
        
        // 查看最终的连接池状态
        System.out.println("\n最终连接池状态:");
        System.out.println("总连接数: " + client.connectionPool().connectionCount());
        System.out.println("空闲连接数: " + client.connectionPool().idleConnectionCount());
    }
}


package com.myokhttp;

import java.io.IOException;

/**
 * 拦截器接口
 * 这是 OkHttp 最核心的设计：责任链模式
 * 
 * 每个拦截器都可以：
 * 1. 在请求发出前处理 Request
 * 2. 决定是否继续执行链
 * 3. 在响应返回后处理 Response
 */
public interface Interceptor {
    
    /**
     * 拦截方法
     * 
     * @param chain 拦截器链，包含下一个拦截器的引用
     * @return 响应对象
     */
    Response intercept(Chain chain) throws IOException;
    
    /**
     * 拦截器链接口
     * 
     * 关键方法：
     * - request(): 获取当前请求
     * - proceed(): 执行下一个拦截器
     */
    interface Chain {
        /**
         * 获取当前的请求
         */
        Request request();
        
        /**
         * 继续执行下一个拦截器
         * 这是责任链模式的核心
         */
        Response proceed(Request request) throws IOException;
        
        /**
         * 获取 OkHttpClient 实例
         */
        OkHttpClient client();
        
        /**
         * 获取当前重试次数
         */
        int connectTimeoutMillis();
        
        /**
         * 获取读取超时时间
         */
        int readTimeoutMillis();
        
        /**
         * 获取写入超时时间
         */
        int writeTimeoutMillis();
    }
}


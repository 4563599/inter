package com.myokhttp;

import java.io.IOException;
import java.util.List;

/**
 * 拦截器链的真实实现
 * 
 * 核心思想：
 * 1. 维护一个拦截器列表和当前索引
 * 2. proceed() 方法创建新的链对象，索引+1
 * 3. 递归调用，形成责任链
 */
public class RealInterceptorChain implements Interceptor.Chain {
    
    private final List<Interceptor> interceptors;
    private final int index;
    private final Request request;
    private final OkHttpClient client;
    private final int connectTimeout;
    private final int readTimeout;
    private final int writeTimeout;
    
    // 用于防止拦截器多次调用 proceed()
    private int calls;

    public RealInterceptorChain(
            List<Interceptor> interceptors,
            int index,
            Request request,
            OkHttpClient client,
            int connectTimeout,
            int readTimeout,
            int writeTimeout
    ) {
        this.interceptors = interceptors;
        this.index = index;
        this.request = request;
        this.client = client;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.writeTimeout = writeTimeout;
    }

    @Override
    public Request request() {
        return request;
    }

    @Override
    public Response proceed(Request request) throws IOException {
        // 检查是否超出拦截器列表范围
        if (index >= interceptors.size()) {
            throw new AssertionError("拦截器列表已执行完毕");
        }

        calls++;

        // 防止拦截器多次调用 proceed()
        if (calls > 1) {
            throw new IllegalStateException(
                "拦截器 " + interceptors.get(index - 1) + " 必须只调用一次 chain.proceed()"
            );
        }

        // 创建下一个拦截器链（索引+1）
        RealInterceptorChain next = new RealInterceptorChain(
            interceptors,
            index + 1,
            request,
            client,
            connectTimeout,
            readTimeout,
            writeTimeout
        );

        // 获取当前拦截器
        Interceptor interceptor = interceptors.get(index);
        
        // 执行当前拦截器的 intercept() 方法
        // 拦截器内部会调用 chain.proceed() 继续执行下一个拦截器
        Response response = interceptor.intercept(next);

        // 检查响应是否为空
        if (response == null) {
            throw new NullPointerException(
                "拦截器 " + interceptor + " 返回了 null"
            );
        }

        return response;
    }

    @Override
    public OkHttpClient client() {
        return client;
    }

    @Override
    public int connectTimeoutMillis() {
        return connectTimeout;
    }

    @Override
    public int readTimeoutMillis() {
        return readTimeout;
    }

    @Override
    public int writeTimeoutMillis() {
        return writeTimeout;
    }
}


package com.myokhttp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Call 的真实实现
 * 负责：
 * 1. 管理请求的执行状态
 * 2. 构建拦截器链
 * 3. 同步/异步执行
 */
public class RealCall implements Call {
    
    private final OkHttpClient client;
    private final Request originalRequest;
    
    // 执行状态
    private boolean executed;
    private boolean canceled;

    public RealCall(OkHttpClient client, Request request) {
        this.client = client;
        this.originalRequest = request;
    }

    @Override
    public Request request() {
        return originalRequest;
    }

    /**
     * 同步执行请求
     * 阻塞当前线程，直到获得响应
     */
    @Override
    public Response execute() throws IOException {
        synchronized (this) {
            if (executed) {
                throw new IllegalStateException("每个 Call 只能执行一次");
            }
            executed = true;
        }

        try {
            // 通知 Dispatcher 开始执行同步请求
            client.dispatcher().executed(this);
            
            // 执行请求并获取响应
            return getResponseWithInterceptorChain();
        } finally {
            // 通知 Dispatcher 请求执行完成
            client.dispatcher().finished(this);
        }
    }

    /**
     * 异步执行请求
     * 在后台线程执行，通过回调返回结果
     */
    @Override
    public void enqueue(Callback callback) {
        synchronized (this) {
            if (executed) {
                throw new IllegalStateException("每个 Call 只能执行一次");
            }
            executed = true;
        }

        // 将异步请求提交给 Dispatcher
        client.dispatcher().enqueue(new AsyncCall(callback));
    }

    @Override
    public void cancel() {
        canceled = true;
    }

    @Override
    public boolean isExecuted() {
        return executed;
    }

    @Override
    public boolean isCanceled() {
        return canceled;
    }

    @Override
    public Call clone() {
        return new RealCall(client, originalRequest);
    }

    /**
     * 核心方法：通过拦截器链获取响应
     * 
     * 拦截器的顺序（重要！）：
     * 1. 应用拦截器（用户自定义）
     * 2. RetryAndFollowUpInterceptor（重试和重定向）
     * 3. BridgeInterceptor（补充请求头）
     * 4. CacheInterceptor（缓存）
     * 5. ConnectInterceptor（建立连接）
     * 6. 网络拦截器（用户自定义）
     * 7. CallServerInterceptor（真实网络请求）
     */
    private Response getResponseWithInterceptorChain() throws IOException {
        // 构建完整的拦截器链
        List<Interceptor> interceptors = new ArrayList<>();
        
        // 1. 添加应用拦截器
        interceptors.addAll(client.interceptors());
        
        // 2. 添加重试和重定向拦截器
        interceptors.add(new RetryAndFollowUpInterceptor(client));
        
        // 3. 添加桥接拦截器（补充 HTTP 头）
        interceptors.add(new BridgeInterceptor());
        
        // 4. 添加缓存拦截器
        // interceptors.add(new CacheInterceptor(client.cache()));
        
        // 5. 添加连接拦截器
        interceptors.add(new ConnectInterceptor(client));
        
        // 6. 添加网络拦截器
        interceptors.addAll(client.networkInterceptors());
        
        // 7. 添加真实的服务器请求拦截器
        interceptors.add(new CallServerInterceptor());

        // 创建拦截器链并开始执行
        Interceptor.Chain chain = new RealInterceptorChain(
            interceptors,
            0,
            originalRequest,
            client,
            client.connectTimeoutMillis(),
            client.readTimeoutMillis(),
            client.writeTimeoutMillis()
        );

        return chain.proceed(originalRequest);
    }

    /**
     * 异步请求的包装类
     * 实现 Runnable 接口，可以提交到线程池执行
     */
    class AsyncCall implements Runnable {
        private final Callback callback;

        AsyncCall(Callback callback) {
            this.callback = callback;
        }

        @Override
        public void run() {
            boolean signalledCallback = false;
            try {
                // 执行请求
                Response response = getResponseWithInterceptorChain();
                
                // 检查是否已取消
                if (canceled) {
                    signalledCallback = true;
                    callback.onFailure(RealCall.this, new IOException("Canceled"));
                } else {
                    signalledCallback = true;
                    callback.onResponse(RealCall.this, response);
                }
            } catch (IOException e) {
                if (!signalledCallback) {
                    callback.onFailure(RealCall.this, e);
                }
            } finally {
                // 通知 Dispatcher 异步请求完成
                client.dispatcher().finished(this);
            }
        }

        public RealCall get() {
            return RealCall.this;
        }
    }
}


package com.myokhttp;

import java.util.ArrayList;
import java.util.List;

/**
 * OkHttpClient：HTTP 客户端
 * 
 * 使用 Builder 模式构建，确保配置的灵活性
 * 
 * 核心职责：
 * 1. 管理配置（超时、拦截器等）
 * 2. 管理资源（连接池、线程池等）
 * 3. 创建 Call 对象
 */
public class OkHttpClient {
    
    private final Dispatcher dispatcher;
    private final List<Interceptor> interceptors;
    private final List<Interceptor> networkInterceptors;
    private final ConnectionPool connectionPool;
    
    // 超时设置（毫秒）
    private final int connectTimeout;
    private final int readTimeout;
    private final int writeTimeout;
    
    // 重试设置
    private final boolean followRedirects;
    private final boolean retryOnConnectionFailure;

    private OkHttpClient(Builder builder) {
        this.dispatcher = builder.dispatcher;
        this.interceptors = new ArrayList<>(builder.interceptors);
        this.networkInterceptors = new ArrayList<>(builder.networkInterceptors);
        this.connectionPool = builder.connectionPool;
        this.connectTimeout = builder.connectTimeout;
        this.readTimeout = builder.readTimeout;
        this.writeTimeout = builder.writeTimeout;
        this.followRedirects = builder.followRedirects;
        this.retryOnConnectionFailure = builder.retryOnConnectionFailure;
    }

    /**
     * 创建一个新的 Call
     */
    public Call newCall(Request request) {
        return new RealCall(this, request);
    }

    public Dispatcher dispatcher() {
        return dispatcher;
    }

    public List<Interceptor> interceptors() {
        return interceptors;
    }

    public List<Interceptor> networkInterceptors() {
        return networkInterceptors;
    }

    public ConnectionPool connectionPool() {
        return connectionPool;
    }

    public int connectTimeoutMillis() {
        return connectTimeout;
    }

    public int readTimeoutMillis() {
        return readTimeout;
    }

    public int writeTimeoutMillis() {
        return writeTimeout;
    }

    public boolean followRedirects() {
        return followRedirects;
    }

    public boolean retryOnConnectionFailure() {
        return retryOnConnectionFailure;
    }

    /**
     * 创建一个新的 Builder
     */
    public Builder newBuilder() {
        return new Builder(this);
    }

    /**
     * Builder 模式：构建 OkHttpClient
     */
    public static class Builder {
        private Dispatcher dispatcher;
        private List<Interceptor> interceptors = new ArrayList<>();
        private List<Interceptor> networkInterceptors = new ArrayList<>();
        private ConnectionPool connectionPool;
        
        // 默认超时设置（毫秒）
        private int connectTimeout = 10_000;
        private int readTimeout = 10_000;
        private int writeTimeout = 10_000;
        
        // 默认重试设置
        private boolean followRedirects = true;
        private boolean retryOnConnectionFailure = true;

        public Builder() {
            dispatcher = new Dispatcher();
            connectionPool = new ConnectionPool();
        }

        private Builder(OkHttpClient client) {
            this.dispatcher = client.dispatcher;
            this.interceptors = new ArrayList<>(client.interceptors);
            this.networkInterceptors = new ArrayList<>(client.networkInterceptors);
            this.connectionPool = client.connectionPool;
            this.connectTimeout = client.connectTimeout;
            this.readTimeout = client.readTimeout;
            this.writeTimeout = client.writeTimeout;
            this.followRedirects = client.followRedirects;
            this.retryOnConnectionFailure = client.retryOnConnectionFailure;
        }

        /**
         * 添加应用拦截器
         * 在重试、缓存等之前执行
         */
        public Builder addInterceptor(Interceptor interceptor) {
            if (interceptor == null) {
                throw new IllegalArgumentException("interceptor == null");
            }
            interceptors.add(interceptor);
            return this;
        }

        /**
         * 添加网络拦截器
         * 在真实网络请求之前执行
         */
        public Builder addNetworkInterceptor(Interceptor interceptor) {
            if (interceptor == null) {
                throw new IllegalArgumentException("interceptor == null");
            }
            networkInterceptors.add(interceptor);
            return this;
        }

        public Builder connectTimeout(int timeout) {
            if (timeout < 0) {
                throw new IllegalArgumentException("timeout < 0");
            }
            this.connectTimeout = timeout;
            return this;
        }

        public Builder readTimeout(int timeout) {
            if (timeout < 0) {
                throw new IllegalArgumentException("timeout < 0");
            }
            this.readTimeout = timeout;
            return this;
        }

        public Builder writeTimeout(int timeout) {
            if (timeout < 0) {
                throw new IllegalArgumentException("timeout < 0");
            }
            this.writeTimeout = timeout;
            return this;
        }

        public Builder followRedirects(boolean followRedirects) {
            this.followRedirects = followRedirects;
            return this;
        }

        public Builder retryOnConnectionFailure(boolean retry) {
            this.retryOnConnectionFailure = retry;
            return this;
        }

        public Builder dispatcher(Dispatcher dispatcher) {
            if (dispatcher == null) {
                throw new IllegalArgumentException("dispatcher == null");
            }
            this.dispatcher = dispatcher;
            return this;
        }

        public Builder connectionPool(ConnectionPool connectionPool) {
            if (connectionPool == null) {
                throw new IllegalArgumentException("connectionPool == null");
            }
            this.connectionPool = connectionPool;
            return this;
        }

        public OkHttpClient build() {
            return new OkHttpClient(this);
        }
    }
}

